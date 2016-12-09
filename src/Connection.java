import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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
    // Send Thread
    public Thread endThread;
    // End Thread
    public Thread sendThread;
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
    public int previousSentCounter;
    public byte[] previousSent;
    public byte[] previousReceived;

    public BlockingQueue<StreamData> sendQueue = new LinkedBlockingQueue<StreamData>();
    public BlockingQueue<StreamData> receiveQueue = new LinkedBlockingQueue<StreamData>();
    
    public long fixTime;
    
    
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
        this.previousSentCounter = 0;
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
    	boolean receivedFix = false;
    	
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
                    // System.out.println("Received Packet: " + message);
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
                            
                            
                            // Make sure both aren't "connected to the client"
                            // Respond to the client, confirming this is the server
                            Connection.this.fixTime = System.currentTimeMillis();
                            sendData = ("FIX" + fixTime).getBytes();
                            // Connection2.this.continueListening = false;
                            Connection.this.sendPacketData(sendData, packet.getAddress());
                    	}
                    } else if (message.startsWith("FIX") && !fromAddr.equals(localAddr) && (Connection.this.isServer == true)) {
                    	if(!receivedFix){
                    		receivedFix = true;
                    		String timestamp = message.substring(3, message.length());
                    		Long time = Long.parseLong(timestamp);
                    		if(Connection.this.fixTime > time){
                    			// Convert this to client
                    			System.out.println("Converting to Client");
                    			Connection.this.isServer = false;
                    			int tmpPort = Connection.this.incomingPort;
                        		Connection.this.incomingPort = Connection.this.outgoingPort;
                        		Connection.this.outgoingPort = tmpPort;
                        		System.out.println("Connected to Server");
                        		Connection.this.beginListeningForStream();
                    		}
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
    	// System.out.println("Sending a packet");
    	for(int i=0; i < 3; i++){
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
            	// e.printStackTrace();
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
                    	try{
                        	socket = new DatagramSocket();
                            socket.setBroadcast(true);
                        	socket.send(requestPacket);
                    		
                    	} catch(Exception ee){
                    		
                    	}
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
    	int numThreads = 6;
    	int threadNumber = 0;
    	Thread discoverThreads[] = new Thread[numThreads];
    	// System.out.println("Beginning discovery");
    	while((threadNumber < numThreads) && (this.connectedComputerIP == null)){
			discoverThreads[threadNumber] = new Thread(new DiscoverThread());
			discoverThreads[threadNumber].start();
			Thread.sleep(2000);
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
        	// System.out.println("Here");
            // Send the stream ready packet to the server
            byte[] data = "STREAMREADY".getBytes();
            // System.out.println("Sending: STREAMREADY (" + System.currentTimeMillis() + ")");
            Connection.this.sendPacketData(data, Connection.this.connectedComputerIP);
            // System.out.println("Here1");
            // Accept the incoming stream (break until accepted)
            Connection.this.incomingSocket = Connection.this.serverSocket.accept();
            // System.out.println("Here2");
            // Create the input stream
            Connection.this.inputStream = new ObjectInputStream(Connection.this.incomingSocket.getInputStream());
            // System.out.println("Finished preparing to receive stream");
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
                while(Connection.this.continueStreaming){
                	try{
                    	

                    	StreamData streamData = (StreamData)Connection.this.inputStream.readObject();

                    	if(streamData.isDiff){
                        	Connection.this.compressedbytesReceived = Connection.this.compressedbytesReceived + (((Diff)streamData.data).diffImage.length / 1024);
                    		streamData.data = DifferencingLibrary.rebuild((Diff)streamData.data, Connection.this.previousReceived);
                    		Connection.this.previousReceived = (byte[])streamData.data;
                    	} else {
                    		Connection.this.previousReceived = (byte[])streamData.data;
                    		Connection.this.compressedbytesReceived = Connection.this.compressedbytesReceived + (((byte[])streamData.data).length / 1024);
                    	}
                    	Connection.this.bytesReceived = Connection.this.bytesReceived + (((byte[])streamData.data).length / 1024);
                    	
                    	
                    	
                    	if (Connection.this.receiveQueue.size()>5){
                    		Connection.this.receiveQueue.take();
                     	}
                    	
                    	Connection.this.receiveQueue.put(streamData);
                	} catch(Exception e){
                		// e.printStackTrace();
                	}
                	
                }
                System.out.println("Stopped Streaming");
            } catch(Exception e){
                // e.getStackTrace();
            	Connection.this.exit();
            }
        }
    }
    
    public Object getInbox() throws InterruptedException{
    	try	{
    		Object result = this.receiveQueue.take().data;
    		// System.out.println("666666666666664Queue size: " + ((Diff)streamData.data).diffImage.length);kkk(was returning diff, should be byte[])
    		return result;
    	} catch(Exception e){
    		e.printStackTrace();
    	}
    	return null;
		
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
        // Create the listening thread
        this.sendThread = new Thread(new SendThread());
        // Start the listening thread
        this.sendThread.start();
    }
    
    
    
    public class SendThread implements Runnable {  

    	

    	

        /**
         * The run method
         *
         * Place callback functions (receiveImage) here
         */
        @Override
        public void run() {
        	int n = 0;
        	Connection.this.continueStreaming = true;
        	while(Connection.this.continueStreaming){
        		try {
        			n++;
        			StreamData streamData = Connection.this.sendQueue.take();
        			// System.out.println(n + "Sending: " + streamData.isDiff);
                    Connection.this.outputStream.writeObject(streamData);
                    if(n > 50){
                    	Connection.this.outputStream.reset();
                    }
                    
                    // System.out.println(n + "Baseball");
                    // this.bytesSent = this.bytesSent + data.length;
                } catch (Exception e) {
                	if(Connection.this.continueStreaming){
                		// e.printStackTrace();
                		// Connection.this.continueStreaming = false;
                		
                	}
                	
                    // System.out.println("BROKEN PIPE");
                    // this.close();
                }
        	}
        	
        }
    }
    
    
    
    /**
     * A method to stream data
     * @param o: Object - The data to stream
     */
    public void sendStreamData(Object o){
    	
    	if(this.continueStreaming){
    		StreamData streamData;
            if(this.previousSent == null || this.previousSentCounter >= 25){
            	// System.out.println("1Queue Size: " + this.sendQueue.size());
            	this.previousSent = (byte[])o;
            	// System.out.println("Sending original data");
            	streamData = new StreamData(false, o);
            	this.compressedbytesSent = this.compressedbytesSent + (((byte[])streamData.data).length / 1024);
            	this.bytesSent = this.bytesSent + (((byte[])streamData.data).length / 1024);
            	this.previousSentCounter = 0;
            } else {
            	// 
            	// System.out.println("Sending diff data");
            	// System.out.println("2Queue Size: " + this.sendQueue.size());
            	Diff diff = DifferencingLibrary.getDiff(this.previousSent, (byte[])o);
            	this.previousSent = (byte[])o;
            	streamData = new StreamData(true, diff);
            	this.compressedbytesSent = this.compressedbytesSent + (diff.diffImage.length / 1024);
            	this.bytesSent = this.bytesSent + (diff.length / 1024);
            	// System.out.println("Time taken to send: " + (System.currentTimeMillis() - startTime));
            	this.previousSentCounter++;
            	
            	
            	
            }
    		try {
    			if (sendQueue.size()>5){
    				sendQueue.take();
             	}
    			sendQueue.put(streamData);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
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
                    // Route the message
                    if(message.equals("END") && !fromAddr.equals(localAddr)){
                    	if(!receivedEnd){
                    		receivedEnd = true;
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
    	// Print stats
    	DecimalFormat formatter = new DecimalFormat("###,###,###");
    	DecimalFormat formatter1 = new DecimalFormat("##");
    	System.out.println("Sent: " + formatter.format(this.getSent()) + " KB");
    	System.out.println("Received: " + formatter.format(this.getReceived()) + " KB");
    	System.out.println("Sent (diff): " + formatter.format(this.compressedbytesSent) + " KB");
    	System.out.println("Received (diff): " + formatter.format(this.compressedbytesReceived) + " KB");
    	System.out.println("Saved Sent: " + formatter.format((this.getSent()-this.compressedbytesSent)) + " KB (" + formatter.format((((double)this.getSent()-(double)this.compressedbytesSent)/(double)this.getSent()) * 100) + "%)");
    	System.out.println("Saved Received: " + formatter.format((this.getReceived()-this.compressedbytesReceived)) + " KB (" + formatter.format((((double)this.getReceived()-(double)this.compressedbytesReceived)/(double)this.getReceived()) * 100) + "%)");
    	
		this.receivingModel.doneStreaming();
    	this.continueStreaming = false;
		
		// this.endThread.join();
		
    	try {
			this.outputStream.close();
		} catch (IOException e4) {
		}
		try {
			this.inputStream.close();
		} catch (IOException e3) {
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
			this.serverSocket.close();
		} catch (IOException e2) {
		}
		
		this.endThread.interrupt();
		this.discoveryThread.interrupt();
		this.sendThread.interrupt();
		this.listeningThread.interrupt();
		System.out.println("Goodbye");
		System.exit(0);
    }
    
    /**
     * A method to close the connections
     */
    public void close(){
    	byte[] data = "END".getBytes();
    	try {
			this.sendPacketData(data, this.connectedComputerIP);
		} catch (IOException e) {
		}
    	this.exit();
    }
}
