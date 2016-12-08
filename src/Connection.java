import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * Connection
 *
 * A class to handle discovery and streaming of data
 * between a client and a server
 *
 * @author christopherschlitt
 *
 */
public class Connection {
    // The IP address of the other computer you are connecting to
    private InetAddress connectedComputerIP;
    // The packet/discovery port
    private int packetPort;
    // The incoming port
    private int incomingPort;
    // The outgoing port
    private int outgoingPort;
    // The output stream
    private ObjectOutputStream outputStream;
    // The input stream
    public ObjectInputStream inputStream;
    // IncomingSocket
    private ServerSocket serverSocket;
    // IncomingSocket
    private Socket incomingSocket;
    // A flag to continue listening for packets
    public boolean continueListening;
    // A flag to start streaming
    public boolean startStreaming;
    // A flag to start streaming
    public boolean continueStreaming;
    // Discovery Thread
    public Thread discoveryThread;
    // Receiving Thread
    public Thread listeningThread;
    // End Thread
    public Thread endThread;
    // Output Socket
    public Socket streamingSocket;
    // The receiving model for callbacks
    public Model receivingModel;
    // Server indicator
    public boolean isServer;
    // Data Sent
    public long bytesSent;
    // Data Received
    public long bytesReceived;
    // Data Sent
    public long compressedbytesSent;
    // Data Received
    public long compressedbytesReceived;
    
    // Differencing Library
    public byte[] previousSent;
    public byte[] previousReceived;
    
    
    /**
     * Constructor
     *
     * @param port: int - The port to send the discovery packets
     * @param receivingModel: ClientModel - The ClientModel that will be receiving the images (null for sender)
     */
    public Connection(int packetPort, int incomingPort, int outgoingPort, Model receivingModel){
    	System.out.println("Initializing Connection");
        // Set the ports
    	this.packetPort = packetPort;
        this.incomingPort = incomingPort;
        this.outgoingPort = outgoingPort;
        // Set the receiving model
        this.receivingModel = receivingModel;
    }
    
    /**
     * A method to handle connection to the other computer
     */
    public void connect(){
    	// Begin listening for packets
        this.beginPacketListening();
        // Discover the server's IP address
        try {
			this.discoverIP();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        // System.out.println("Finished discovering (" + this.continueListening + ")");
        // Wait until the server is discovered
        try {
			this.discoveryThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        // Create the end listening thread
        this.endThread = new Thread(new EndThread());
        // Start the end listening thread
        this.endThread.start();
        System.out.println("Stream setup successfully");
    }
    
    
    /**
     * A method to get the connected computer's IP address
     *
     * @return connectedComputerIP: InetAddress - The connected computer's IP address
     */
    public InetAddress getServerIP(){
        return this.connectedComputerIP;
    }
    
    /**
     * A method to check if the computers are listening for packets
     *
     * @return isConnected: boolean
     */
    public boolean isConnected(){
        if(this.connectedComputerIP == null){
            return false;
        }
        return true;
    }
    
    public long getSent(){
    	return this.bytesSent;
    }
    
    public long getReceived(){
    	return this.bytesReceived;
    }
    
    /**
     * A runnable class to manage incoming packets
     *
     * @author christopherschlitt
     *
     */
    public class DiscoveryThread implements Runnable {
    	boolean receivedDiscovery = false;
    	boolean receivedStream = false;
    	
        /**
         * The run method
         */
        @Override
        public void run() {
            try {
                // Initiate the DatagramSocket
                DatagramSocket socket;
                // Get local IP string
                String localAddr = InetAddress.getLocalHost().toString().split("/")[1];
                socket = new DatagramSocket(Connection.this.packetPort, InetAddress.getByName("0.0.0.0"));
                socket.setBroadcast(true);
                // Continue listening
                while(Connection.this.continueListening){
                	// System.out.println("Ready to begin receiving a packet");
                    // Listen on specified port
                    
                    
                    // System.out.println("Ready to receive another packet");
                    
                    // Receive a packet
                    byte[] received = new byte[1000];
                    DatagramPacket packet = new DatagramPacket(received, received.length);
                    System.out.println("Receive Packet? (" + System.currentTimeMillis() + ")");
                    socket.receive(packet);
                    System.out.println("Packet received");
                    // Get package message
                    String message = new String(packet.getData()).trim();
                    // Get package address
                    String fromAddr = "";
                    try {
                        fromAddr = packet.getAddress().toString().split("/")[1];
                    } catch (Exception e){
                        fromAddr = packet.getAddress().getHostAddress().toString();
                    }
                    System.out.println("Received Packet: " + message);
                    // Route the message
                    if(message.equals("DISCOVERY") && !fromAddr.equals(localAddr)){
                    	if(!receivedDiscovery){
                    		receivedDiscovery = true;
                    		Connection.this.isServer = true;
                    		int tmpPort = Connection.this.incomingPort;
                    		Connection.this.incomingPort = Connection.this.outgoingPort;
                    		Connection.this.outgoingPort = tmpPort;
                    		System.out.println("Connected to Client");
                            // Received discovery message
                            // Set the client IP
                            Connection.this.connectedComputerIP = packet.getAddress();
                            
                            // Respond to the client, confirming this is the server
                            byte[] sendData = "DISCOVERY_RESPONSE".getBytes();
                            // Connection2.this.continueListening = false;
                            Connection.this.sendPacketData(sendData, packet.getAddress());
                    	}
                    } else if (message.equals("DISCOVERY_RESPONSE") && !fromAddr.equals(localAddr)) {
                    	if(!receivedDiscovery){
                    		receivedDiscovery = true;
                    		Connection.this.isServer = false;
                    		
                    		
                    		System.out.println("Connected to Server");
                            // Received discovery response
                            // Set the connected computer (client) IP
                            Connection.this.connectedComputerIP = packet.getAddress();
                            // Prepare the receiving stream
                            Connection.this.beginListeningForStream();
                    	}
                    	
                    } else if (message.equals("STREAMREADY")) {
                    	if(!receivedStream){
                    		receivedStream = true;
                    		if(Connection.this.isServer){
                    			// Prepare the receiving stream
                                Connection.this.beginListeningForStream();
                    		}
                    		// System.out.println("Passed");
	                		// Received stream ready response
	                        // Stop packet listening
	                        Connection.this.continueListening = false;
	                        Connection.this.beginStreaming();
                    	}
                    }
                    
                    
                }
             // Close the socket
                socket.close();
                // System.out.println("No more listening");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * A method to start listening for packets
     */
    public void beginPacketListening(){
        // Set the packet listening flag
        this.continueListening = true;
        // Create a new discovery thread
        this.discoveryThread = new Thread(new DiscoveryThread());
        // Start the discovery thread
        this.discoveryThread.start();
        System.out.println("Listening...");
    }
    
    /**
     * A method to send a packet over the local network
     *
     * @param data: byte[] - Data to send
     * @param address: InetAddress - IP address to send the packet to
     * @return success: boolean - Success flag
     */
    public boolean sendPacketData(byte[] data, InetAddress address) throws IOException{
    	System.out.println("Sending a packet");
    	for(int i=0; i < 4; i++){
    		// Initiate the DatagramSocket
            DatagramSocket socket;
            socket = new DatagramSocket();
            socket.setBroadcast(true);
            
            // Create and send the packet
            DatagramPacket requestPacket = new DatagramPacket(data, data.length, address, this.packetPort);
            // System.out.println("Sending packet (" + System.currentTimeMillis() + "): " + data);
            try{
            	socket.send(requestPacket);
            } catch (Exception e){
            	e.printStackTrace();
            }
            
            
            // Close the socket
            socket.close();
    	}
        
        return true;
    }
    
    public class DiscoverThread implements Runnable {
    	
    	public void findServer() throws Exception{
        	System.out.println("Looking for the Server...");
            // Initiate the DatagramSocket
            DatagramSocket socket;
            socket = new DatagramSocket();
            socket.setBroadcast(true);
            
            // Create the response header
            byte[] request = "DISCOVERY".getBytes();
            
            // Create and send the packet to the local subnet
            DatagramPacket requestPacket;
            
            // Get the local IP address
            InetAddress tempAddr;
            String localAddr = InetAddress.getLocalHost().toString().split("/")[1];
            String[] ipAddress = InetAddress.getLocalHost().toString().split("/")[1].split("\\.");
            
            // Loop through all IP addresses on the local subnet, then try +/- subnets
            int ipthree = Integer.parseInt(ipAddress[2]);
            int ii = 1;
            
            // System.out.println("Beginning discovery");
            while(ii < 255){
                // Correct the IP address
                int add = 0;
                if(ipthree > 255){
                    ipthree = ipthree - 255;
                    add = 1;
                }
                if(ipthree < 0){
                    ipthree = ipthree + 255;
                    add = -1;
                }
                
                // Create the socket
                socket = new DatagramSocket();
                socket.setBroadcast(true);
                
                
                // Loop through the local subnet
                for(int j=0; j<255; j++){
                	
                	
                    // Check addresses on specified subnet
                    tempAddr = InetAddress.getByName(ipAddress[0] + "." + ipAddress[1] + "." + ipthree + "." + j);
                    
                    // Check if sending message to own IP address and filter out
                    if(localAddr.trim().equals((ipAddress[0] + "." + ipAddress[1] + "." + ipthree + "." + j).trim())){
                        continue;
                    }
                    
                    // System.out.println("Sending a DISCOVERY message to: " + ipAddress[0] + "." + ipAddress[1] + "." + ipthree + "." + j + "   |   Mine is: " + localAddr);
                    
                    requestPacket = new DatagramPacket(request, request.length, tempAddr, Connection.this.packetPort);
                    // System.out.println("Sending a discovery packet to: " + ipAddress[0] + "." + ipAddress[1] + "." + ipthree + "." + j);
                    
                    // Send the packet
                    try {
                    	socket.send(requestPacket);
                    } catch (Exception e){
                    	socket = new DatagramSocket();
                        socket.setBroadcast(true);
                    	socket.send(requestPacket);
                    }
                    
                    // Check if the discovery response packet has been received
                    if(Connection.this.connectedComputerIP != null){
                        // If the server has been found, break the loop
                        break;
                    }
                 
                }
                // Close the socket
                socket.close();
                
                
                // Restore ipthree counter for math
                ipthree = ipthree + (255 * add);
                
                // Calculate next ipthree
                if(ii % 2 == 0){
                    ipthree = ipthree + ii;
                } else {
                    ipthree = ipthree - ii;
                }
                
                ii++;
                if(Connection.this.connectedComputerIP != null){
                    // If the server has been found, break the loop
                    break;
                }
            }
    	}
    	
        /**
         * The run method
         */
        @Override
        public void run() {
        	try {
				this.findServer();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
    
    /**
     * A method to discover the Server's IP address
     *
     * Notes:
     * Initially, this method was setup to scan and iterate through all computers on the network,
     * Since most enterprise routers block this functionality (including those at Penn), we have
     * changed our discovery mechanism to search IP addresses based on it's local IP address, in
     * an efficient manner
     *
     */
    public void discoverIP() throws Exception{
    	int numThreads = 1;
    	int threadNumber = 0;
    	Thread discoverThreads[] = new Thread[numThreads];
    	// System.out.println("Beginning discovery");
    	while((threadNumber < numThreads) && (this.connectedComputerIP == null)){
			discoverThreads[threadNumber] = new Thread(new DiscoverThread());
			discoverThreads[threadNumber].start();
			Thread.sleep(1000);
			threadNumber++;
    	}
    	threadNumber--;
    	while(threadNumber >= 0){
    		// System.out.println("Waiting to join");
    		discoverThreads[threadNumber].join();
    		// System.out.println("Joined");
    		threadNumber--;
    	}
    	
    }
    
    /**
     * A Runnable class to handle listening for the data stream
     *
     * @author christopherschlitt
     *
     */
    public class ListeningThread implements Runnable {
        // Receiving Model
        private Model receivingModel;
        
        /**
         * A method to prepare the receiving connection
         *
         * @return success: boolean - A success flag
         */
        public boolean prepareReceivingConnection() throws IOException{
        	// System.out.println("Preparing to receive stream");
            // Create the socket
        	try{
        		Connection.this.serverSocket = new ServerSocket(Connection.this.incomingPort);
        	} catch(Exception e){
        		
        		e.printStackTrace();
        		
        	}
        	System.out.println("Here");
            // Send the stream ready packet to the server
            byte[] data = "STREAMREADY".getBytes();
            System.out.println("Sending: STREAMREADY (" + System.currentTimeMillis() + ")");
            Connection.this.sendPacketData(data, Connection.this.connectedComputerIP);
            System.out.println("Here1");
            // Accept the incoming stream (break until accepted)
            Connection.this.incomingSocket = Connection.this.serverSocket.accept();
            System.out.println("Here2");
            // Create the input stream
            Connection.this.inputStream = new ObjectInputStream(Connection.this.incomingSocket.getInputStream());
            System.out.println("Finished preparing to receive stream");
            return true;
        }
        
        /**
         * A method to set the receiving model for the thread
         * @param receivingModel: ClientModel - ClientModel with receiveImage method
         */
        public ListeningThread(Model receivingModel2){
            this.receivingModel = receivingModel2;
        }
        
        
        /**
         * The run method
         *
         * Place callback functions (receiveImage) here
         */
        @Override
        public void run() {
            try {
                // Prepare to receive the stream
                this.prepareReceivingConnection();

                Connection.this.continueStreaming = true;
                // While the stream is open
                // System.out.println("Ready to receive strem");
                while(Connection.this.continueStreaming){
                	// System.out.println("Ready to receive stream object");
                    // Receive the image
                	// long startTime = System.currentTimeMillis();
                	
                	byte[] receivedImage;
                	StreamData streamData = (StreamData)Connection.this.inputStream.readObject();
                	byte[] data = (byte[])streamData.data;
                	Connection.this.compressedbytesReceived = Connection.this.compressedbytesReceived + data.length;
                	// System.out.println("Received object");
                	if(streamData.isDiff){
                		receivedImage = DifferencingLibrary.rebuild((Diff)streamData.data, Connection.this.previousReceived);
                	} else {
                		Connection.this.previousReceived = (byte[])streamData.data;
                		receivedImage = (byte[])streamData.data;
                	}
                	Connection.this.bytesReceived = Connection.this.bytesReceived + receivedImage.length;
                	// System.out.println("Total Time to decompress: " + (System.currentTimeMillis() - startTime));
                	// byte[] data = (byte[])Connection.this.inputStream.readObject();
                	// Connection.this.bytesReceived = Connection.this.bytesReceived + data.length;
                    
                	
                    this.receivingModel.receiveImage(receivedImage);
                }
                System.out.println("Stopped Streaming");
            } catch(Exception e){
                e.getStackTrace();
            }
        }
    }
    
    /**
     * A method to prepare to receive a stream
     */
    public void beginListeningForStream() throws IOException{
    	System.out.println("Initializing incoming stream");
    	this.continueListening = true;
        // Set the start streaming flag
        // this.startStreaming = true;
        // Create the listening thread
        this.listeningThread = new Thread(new ListeningThread(this.receivingModel));
        // Start the listening thread
        this.listeningThread.start();
        // System.out.println("Started the stream listening thread");
    }
    
    /**
     * A method to begin streaming from the server
     */
    public void beginStreaming(){
        // Open the socket to the client
        try {
            this.streamingSocket = new Socket(this.connectedComputerIP, this.outgoingPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Open the output stream
        try {
            this.outputStream = new ObjectOutputStream(this.streamingSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Ready to stream");
        // System.out.println("Ready to send stream");
        // Set the start streaming flag
        this.startStreaming = true;
    }
    
    /**
     * A method to stream data
     * @param o: Object - The data to stream
     */
    public void sendStreamData(Object o){
    	if(this.continueStreaming){
    		// long startTime = System.currentTimeMillis();
    		// System.out.println("Sending a stream object");
            // Cast the object as a byte array
            byte[] data = (byte[])o;
            
            StreamData streamData;
            if(this.previousSent == null){
            	this.previousSent = data;
            	// System.out.println("Sending original data");
            	streamData = new StreamData(false, data);
            	this.compressedbytesSent = this.compressedbytesSent + data.length;
            	this.bytesSent = this.bytesSent + data.length;
            } else {
            	// System.out.println("Sending diff data");
            	Diff diff = DifferencingLibrary.getDiff(this.previousSent, data);
            	streamData = new StreamData(true, diff);
            	this.compressedbytesSent = this.compressedbytesSent + diff.diffImage.length;
            	this.bytesSent = this.bytesSent + data.length;
            	// System.out.println("Time taken to send: " + (System.currentTimeMillis() - startTime));


            }
            
            
            // Write the object to the stream
            try {
                this.outputStream.writeObject(streamData);
                // this.bytesSent = this.bytesSent + data.length;
            } catch (IOException e) {
            	if(this.continueStreaming){
            		e.printStackTrace();
            		this.continueStreaming = false;
            	}
            	
                // System.out.println("BROKEN PIPE");
                // this.close();
            }
    	}
    	
    }
    
    public class EndThread implements Runnable {
    	boolean receivedEnd = false;
    	
        /**
         * The run method
         */
        @Override
        public void run() {
            try {
                // Initiate the DatagramSocket
                DatagramSocket socket;
                // Get local IP string
                String localAddr = InetAddress.getLocalHost().toString().split("/")[1];
                
                // Continue listening
                while(!receivedEnd){
                	// System.out.println("Ready to begin receiving a packet");
                    // Listen on specified port
                    socket = new DatagramSocket(Connection.this.packetPort, InetAddress.getByName("0.0.0.0"));
                    socket.setBroadcast(true);
                    
                    // System.out.println("Ready to receive another packet");
                    
                    // Receive a packet
                    byte[] received = new byte[1000];
                    DatagramPacket packet = new DatagramPacket(received, received.length);
                    // System.out.println("Receive Packet? (" + System.currentTimeMillis() + ")");
                    socket.receive(packet);
                    // System.out.println("Packet received");
                    // Get package message
                    String message = new String(packet.getData()).trim();
                    // Get package address
                    String fromAddr = "";
                    try {
                        fromAddr = packet.getAddress().toString().split("/")[1];
                    } catch (Exception e){
                        fromAddr = packet.getAddress().getHostAddress().toString();
                    }
                    System.out.println("Received Packet: " + message);
                    // Route the message
                    if(message.equals("END") && !fromAddr.equals(localAddr)){
                    	if(!receivedEnd){
                    		receivedEnd = true;
                    		Connection.this.receivingModel.doneStreaming();
                    		Connection.this.exit();
                    	}
                    }
                }
            } catch(Exception e){
            	e.printStackTrace();
            }
        }
    }
    
    /**
     * A method to close the connections
     */
    public void exit(){
    	System.out.println("Sent: " + this.getSent() + " bytes");
    	System.out.println("Received: " + this.getReceived() + " bytes");
    	System.out.println("Sent (compressed): " + this.getSent() + " bytes");
    	System.out.println("Received (compressed): " + this.getReceived() + " bytes");
    	System.out.println("Saved Sent: " + (this.getSent()-this.compressedbytesReceived) + " bytes");
    	System.out.println("Saved Received: " + (this.getReceived()-this.compressedbytesSent) + " bytes");
    	try {
			this.outputStream.close();
		} catch (IOException e4) {
		}
		try {
			this.inputStream.close();
		} catch (IOException e3) {
		}
		try {
			this.serverSocket.close();
		} catch (IOException e2) {
		}
		try {
			this.incomingSocket.close();
		} catch (IOException e1) {
		}
		try {
			this.streamingSocket.close();
		} catch (IOException e) {
		}
		try {
			this.endThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    /**
     * A method to close the connections
     */
    public void close(){
    	byte[] data = "END".getBytes();
    	try {
			this.sendPacketData(data, this.connectedComputerIP);
	    	System.out.println("Send END Packet");
		} catch (IOException e) {
		}
    	this.exit();
    	System.out.println("Stream Ended");
    }
}
