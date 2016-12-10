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
    // Host Correction Timestamp
    public long fixTime;
    
    // Differencing Library Resources
    public int previousSentCounter;
    public byte[] previousSent;
    public byte[] previousReceived;
    
    // Queues
    public BlockingQueue<StreamData> sendQueue = new LinkedBlockingQueue<StreamData>();
    public BlockingQueue<StreamData> receiveQueue = new LinkedBlockingQueue<StreamData>();
    
    /**
     * Constructor
     *
     * @param packetPort: int - The port to send the discovery packets
     * @param incomingPort: int - The receiving stream port
     * @param outgoingPort: int - The outgoing stream port
     * @param receivingModel: Model - The ClientModel that will be receiving the images (null for sender)
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
			System.out.println("Error discovering server. Listening for Client...");
		}
        
        // Wait until the server is discovered
        try {
			this.discoveryThread.join();
		} catch (InterruptedException e) {
			// Don't print exception
		}
        // Create the end listening thread
        this.endThread = new Thread(new EndThread());
        // Start the end listening thread
        this.endThread.start();
        // Print the stream setup successfully
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
    
    /**
     * A method to get the number of bytes sent during the session
     * 
     * @return bytesSent: long
     */
    public long getSent(){
    	return this.bytesSent;
    }
    
    /**
     * A method to get the number of bytes received during the session
     * 
     * @return bytesReceived: long
     */
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
    	// Discovery packet indicator
    	boolean receivedDiscovery = false;
    	// Stream ready packet indicator
    	boolean receivedStream = false;
    	// Host correction indicator
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
                
                // Create the socket
                socket = new DatagramSocket(Connection.this.packetPort, InetAddress.getByName("0.0.0.0"));
                socket.setBroadcast(true);
                
                // Continue listening
                while(Connection.this.continueListening){
                	
                	// Receive a packet
                    byte[] received = new byte[1000];
                    DatagramPacket packet = new DatagramPacket(received, received.length);
                    socket.receive(packet);

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
                    if(message.equals("DISCOVERY") && !fromAddr.equals(localAddr)){
                    	// Check if discovery type message has already been received
                    	if(!receivedDiscovery){
                    		/*
                    		 * Received a DISCOVERY packet, this is the server
                    		 */
                    		
                    		// Set the server and discovery flags
                    		receivedDiscovery = true;
                    		Connection.this.isServer = true;
                    		
                    		// Server switches the incoming and outgoing streaming ports
                    		int tmpPort = Connection.this.incomingPort;
                    		Connection.this.incomingPort = Connection.this.outgoingPort;
                    		Connection.this.outgoingPort = tmpPort;
                    		
                    		// Print connected to the client
                    		System.out.println("Connected to Client");

                            // Set the client IP
                            Connection.this.connectedComputerIP = packet.getAddress();
                            
                            // Respond to the client, confirming this is the server
                            byte[] sendData = "DISCOVERY_RESPONSE".getBytes();
                            Connection.this.sendPacketData(sendData, packet.getAddress());
                            
                            // Make sure both aren't "connected to the client"
                            // Create timestamp
                            Connection.this.fixTime = System.currentTimeMillis();
                            sendData = ("FIX" + fixTime).getBytes();
                            // Send FIX packet with timestamp
                            Connection.this.sendPacketData(sendData, packet.getAddress());
                    	}
                    } else if (message.startsWith("FIX") && !fromAddr.equals(localAddr) && (Connection.this.isServer == true)) {
                    	// Check if fix message has already been received
                    	if(!receivedFix){
                    		/*
                    		 * Received a FIX packet indicating the other computer is the client, 
                    		 * however you also were assigned the client.  Occurs when DISCOVERY
                    		 * packets are received almost simultaneously
                    		 */
                    		
                    		// Set the received fix packet flag
                    		receivedFix = true;
                    		
                    		// Extract the timestamp
                    		String timestamp = message.substring(3, message.length());
                    		Long time = Long.parseLong(timestamp);
                    		
                    		// Compare the timestamp with the timestamp this computer took
                    		if(Connection.this.fixTime > time){
                    			// This computer received the message second
                    			// Convert this to client
                    			System.out.println("Converting to Client");
                    			// Set the server flag
                    			Connection.this.isServer = false;
                    			// Switch the incoming and outgoing ports back
                    			int tmpPort = Connection.this.incomingPort;
                        		Connection.this.incomingPort = Connection.this.outgoingPort;
                        		Connection.this.outgoingPort = tmpPort;
                        		// Print connected to the client
                        		System.out.println("Connected to Server");
                        		
                        		// Begin the listening stream setup process
                        		Connection.this.beginListeningForStream();
                    		}
                    	}
                    } else if (message.equals("DISCOVERY_RESPONSE") && !fromAddr.equals(localAddr)) {
                    	// Check if discovery type message has already been received
                    	if(!receivedDiscovery){
                    		/*
                    		 * Received a DISCOVERY RESPONSE packet, found the server, this is the client
                    		 */
                    		
                    		// Set the received discovery packet and server flags
                    		receivedDiscovery = true;
                    		Connection.this.isServer = false;
                    		
                    		// Print connected to the server
                    		System.out.println("Connected to Server");

                            // Set the connected computer (client) IP
                            Connection.this.connectedComputerIP = packet.getAddress();
                            
                            // Begin the listening stream setup process
                            Connection.this.beginListeningForStream();
                    	}
                    	
                    } else if (message.equals("STREAMREADY")) {
                    	// Check if stream type message has already been received
                    	if(!receivedStream){
                    		// Set the stream packet received flag
                    		receivedStream = true;
                    		
                    		if(Connection.this.isServer){
                    			// If this is the server, begin the listening stream setup process
                                Connection.this.beginListeningForStream();
                    		}

	                        // Stop listening for packets, connection is complete
	                        Connection.this.continueListening = false;
	                        
	                        // Begin the outgoing streaming process
	                        Connection.this.beginStreaming();
                    	}
                    }
                    
                }
                // Close the socket
                socket.close();

            } catch (Exception e) {
                // Don't print common network errors
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
        // Print listening
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
    	// Send three packets, to ensure delivery
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
            	// Don't print common network packet errors
            }
            
            // Close the socket
            socket.close();
    	}
        
    	// Return success
        return true;
    }
    
    /**
     * A runnable method to handle the sending of packets during network discovery
     * @author christopherschlitt
     *
     */
    public class DiscoverThread implements Runnable {
    	
    	/**
    	 * A method to discover the server on the network, and all subnets
    	 * 
    	 */
    	public void findServer() throws Exception{
    		
    		// Print looking for the server
        	System.out.println("Looking for the Server...");
        	
            // Initiate the DatagramSocket
            DatagramSocket socket;
            socket = new DatagramSocket();
            socket.setBroadcast(true);
            
            // Create the DISCOVERY message
            byte[] request = "DISCOVERY".getBytes();
            
            // Create the packet
            DatagramPacket requestPacket;
            
            // Get the local IP address
            InetAddress tempAddr;
            String localAddr = InetAddress.getLocalHost().toString().split("/")[1];
            String[] ipAddress = InetAddress.getLocalHost().toString().split("/")[1].split("\\.");
            
            // Loop through all IP addresses on the local subnet, then try +/- subnets
            int ipthree = Integer.parseInt(ipAddress[2]);
            int ii = 1;
            
            // Loop through each subnet
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
                
                // Loop through the current subnet
                for(int j=0; j<255; j++){
                	
                    // Check addresses on specified subnet
                    tempAddr = InetAddress.getByName(ipAddress[0] + "." + ipAddress[1] + "." + ipthree + "." + j);
                    
                    // Check if sending message to own IP address and filter out
                    if(localAddr.trim().equals((ipAddress[0] + "." + ipAddress[1] + "." + ipthree + "." + j).trim())){
                        continue;
                    }
                    
                    // Create the packet
                    requestPacket = new DatagramPacket(request, request.length, tempAddr, Connection.this.packetPort);
                    
                    // Send the packet
                    try {
                    	socket.send(requestPacket);
                    } catch (Exception e){
                    	try{
                    		// Try again
                        	socket = new DatagramSocket();
                            socket.setBroadcast(true);
                        	socket.send(requestPacket);
                    		
                    	} catch(Exception ee){
                    		
                    	}
                    }
                    
                    // Check if the discovery response packet has already been received
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
                
                // Check if the discovery response packet has already been received
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
        		// Find the server
				this.findServer();
			} catch (Exception e) {
				System.out.println("Error running server discovery");
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
    	// Number of discovery threads to run
    	int numThreads = 6;
    	// The current thread
    	int threadNumber = 0;
    	// Thread array
    	Thread discoverThreads[] = new Thread[numThreads];
    	
    	// Create and start all the discover threads
    	while((threadNumber < numThreads) && (this.connectedComputerIP == null)){
			discoverThreads[threadNumber] = new Thread(new DiscoverThread());
			discoverThreads[threadNumber].start();
			// Two seconds apart
			Thread.sleep(2000);
			threadNumber++;
    	}
    	threadNumber--;
    	// Join all the threads
    	while(threadNumber >= 0){
    		discoverThreads[threadNumber].join();
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
        
        /**
         * A method to prepare the receiving connection
         *
         * @return success: boolean - A success flag
         */
        public boolean prepareReceivingConnection() throws IOException{
            // Create the socket
        	try{
        		Connection.this.serverSocket = new ServerSocket(Connection.this.incomingPort);
        	} catch(Exception e){
        		System.out.println("Error preparing the incoming stream");
        	}

            // Send the stream ready packet to the server
            byte[] data = "STREAMREADY".getBytes();
            Connection.this.sendPacketData(data, Connection.this.connectedComputerIP);

            // Accept the incoming stream (break until accepted)
            Connection.this.incomingSocket = Connection.this.serverSocket.accept();

            // Create the input stream
            Connection.this.inputStream = new ObjectInputStream(Connection.this.incomingSocket.getInputStream());

            // Return success
            return true;
        }
        

        /**
         * The run method
         *
         * Receives streams and adds them to the received queue
         */
        @Override
        public void run() {
            try {
                // Prepare to receive the stream
                this.prepareReceivingConnection();

                // Set the continue streaming flag
                Connection.this.continueStreaming = true;
                // Continue to listen while streaming
                while(Connection.this.continueStreaming){
                	try{
                    	// Receive the stream data object
                    	StreamData streamData = (StreamData)Connection.this.inputStream.readObject();
                    	
                    	// Check if the data is a full image or a diff of the previous image
                    	if(streamData.isDiff){
                    		// Increment counter
                        	Connection.this.compressedbytesReceived = Connection.this.compressedbytesReceived + (((Diff)streamData.data).diffImage.length / 1024);
                    		// If the data is a diff, rebuild it using the differencing library and the previous image
                    		streamData.data = DifferencingLibrary.rebuild((Diff)streamData.data, Connection.this.previousReceived);
                    		// Set the previous received
                    		Connection.this.previousReceived = (byte[])streamData.data;
                    	} else {
                    		// If the data is a full image, only set the previous received
                    		Connection.this.previousReceived = (byte[])streamData.data;
                    		// Increment counter
                    		Connection.this.compressedbytesReceived = Connection.this.compressedbytesReceived + (((byte[])streamData.data).length / 1024);
                    	}
                    	// Increment counter
                    	Connection.this.bytesReceived = Connection.this.bytesReceived + (((byte[])streamData.data).length / 1024);
                    	
                    	// Check capacity of queue
                    	if (Connection.this.receiveQueue.size()>5){
                    		// If queue is piling up, drop a frame
                    		Connection.this.receiveQueue.take();
                     	}
                    	
                    	// Add the data to the received queue
                    	Connection.this.receiveQueue.put(streamData);
                	} catch(Exception e){
                		// Don't print common network errors
                	}
                	
                }
                
            } catch(Exception e){
                // Broken pipe, exit the app
            	System.out.println("Connection interrupted, shutting down");
            	Connection.this.exit();
            }
        }
    }
    
    /**
     * A method to get the next received data object from the stream
     * @return result: Object - Received object from the stream
     */
    public Object getInbox() {
    	try	{
    		// Get the next StreamData's data object from the received queue
    		Object result = this.receiveQueue.take().data;
    		return result;
    	} catch(Exception e){
    		// Don't print common drop frame errors
    	}
    	// No objects available
    	return null;
    }
    
    /**
     * A method to prepare to receive a stream
     */
    public void beginListeningForStream() throws IOException{
    	
    	// Set the continue listening flag
    	this.continueListening = true;

        // Create the listening thread
        this.listeningThread = new Thread(new ListeningThread());
        // Start the listening thread
        this.listeningThread.start();
    }
    
    /**
     * A method to begin streaming from the server
     */
    public void beginStreaming(){
    	
        // Open the socket to the client
        try {
        	// Create the outgoing streaming socket
            this.streamingSocket = new Socket(this.connectedComputerIP, this.outgoingPort);
        } catch (IOException e) {
            System.out.println("Error creating the outgoing stream");
        }
        
        // Open the output stream
        try {
        	// Create the outgoing stream
            this.outputStream = new ObjectOutputStream(this.streamingSocket.getOutputStream());
        } catch (IOException e) {
        	System.out.println("Error creating the outgoing stream");
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
    
    /**
     * A runnable class to manage the sending of stream data
     * fron an outbox qeueue
     * 
     * @author christopherschlitt
     *
     */
    public class SendThread implements Runnable {  

        /**
         * The run method
         *
         * Place callback functions (receiveImage) here
         */
        @Override
        public void run() {
        	// The difference counter
        	int n = 0;
        	// Set the continue streaming flag
        	Connection.this.continueStreaming = true;
        	// Keep streaming until continueStreaming is false
        	while(Connection.this.continueStreaming){
        		try {
        			// Increment the frame counter
        			n++;
        			// Get the next queued stream data
        			StreamData streamData = Connection.this.sendQueue.take();
        			// Stream the data
                    Connection.this.outputStream.writeObject(streamData);
                    
                    if(n > 20){
                    	// After every 20 frames, flush the output stream
                    	Connection.this.outputStream.reset();
                    }
                } catch (Exception e) {
                	if(Connection.this.continueStreaming){
                		// Don't print common network errors
                	}
                }
        	}
        }
    }
    
    /**
     * A method to reset the previous sent counter
     * 
     */
    public void resetPreviousSentCounter(){
    	this.previousSentCounter = 0;
    }
    
    /**
     * A method to stream data
     * @param o: Object - The data to stream
     */
    public void sendStreamData(Object o){
    	// Check if the system is still streaming
    	if(this.continueStreaming){
    		// Create the stream data object
    		StreamData streamData;

            if(this.previousSent == null || this.previousSentCounter >= 200){
            	/*
            	 * If no initial image has been sent, or every 200 images, send the full image
            	 */
            	// Set the full image
            	this.previousSent = (byte[])o;
            	
            	// Build the stream object
            	streamData = new StreamData(false, o);
            	// Add to the counters
            	this.compressedbytesSent = this.compressedbytesSent + (((byte[])streamData.data).length / 1024);
            	this.bytesSent = this.bytesSent + (((byte[])streamData.data).length / 1024);
            	// Reset the previous counter
            	this.previousSentCounter = 0;
            } else {
            	/*
            	 * If a full image has been sent, just send the difference
            	 */
            	// Create the diff based on the previous image and the current image
            	Diff diff = DifferencingLibrary.getDiff(this.previousSent, (byte[])o);
            	// Set the previous image
            	this.previousSent = (byte[])o;
            	
            	// Build teh stream object
            	streamData = new StreamData(true, diff);
            	// Add to the counters
            	this.compressedbytesSent = this.compressedbytesSent + (diff.diffImage.length / 1024);
            	this.bytesSent = this.bytesSent + (diff.length / 1024);
            	this.previousSentCounter++;
            }
    		try {
    			
    			if (sendQueue.size()>5){
    				// If queue is overloading, drop a frame
    				sendQueue.take();
             	}
    			// Add the stream data to the outgoing queue
    			sendQueue.put(streamData);
			} catch (InterruptedException e) {
				// Don't print common network errors
			}
    	}
    }
    
    /**
     * A runnable class to listen for when the other user ends the chat
     * @author christopherschlitt
     *
     */
    public class EndThread implements Runnable {
    	// A received end packet flag
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

                    // Listen on specified port
                    socket = new DatagramSocket(Connection.this.packetPort, InetAddress.getByName("0.0.0.0"));
                    socket.setBroadcast(true);
                    
                    // Receive a packet
                    byte[] received = new byte[1000];
                    DatagramPacket packet = new DatagramPacket(received, received.length);
                    socket.receive(packet);
                    
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
                    	// If end packet, and not from self
                    	if(!receivedEnd){
                    		// Set the end packet received flag
                    		receivedEnd = true;
                    		// End the chat and the program
                    		Connection.this.exit();
                    	}
                    }
                }
            } catch(Exception e){
            	// Don't print common network errors
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
    	// Bytes sent and received, had differencing library not been used
    	System.out.println("Sent: " + formatter.format(this.getSent()) + " KB");
    	System.out.println("Received: " + formatter.format(this.getReceived()) + " KB");
    	// Actual bytes sent and received with the differencing library
    	System.out.println("Sent (diff): " + formatter.format(this.compressedbytesSent) + " KB");
    	System.out.println("Received (diff): " + formatter.format(this.compressedbytesReceived) + " KB");
    	// Bandwidth saved
    	System.out.println("Saved Sent: " + formatter.format((this.getSent()-this.compressedbytesSent)) + " KB (" + formatter.format((((double)this.getSent()-(double)this.compressedbytesSent)/(double)this.getSent()) * 100) + "%)");
    	System.out.println("Saved Received: " + formatter.format((this.getReceived()-this.compressedbytesReceived)) + " KB (" + formatter.format((((double)this.getReceived()-(double)this.compressedbytesReceived)/(double)this.getReceived()) * 100) + "%)");
    	
    	// Tell the receiving model to stop processes
		this.receivingModel.doneStreaming();
		// Set the continue streaming flag to false
    	this.continueStreaming = false;
		
		try {
			// Close the output stream
			this.outputStream.close();
		} catch (IOException e) {
			// Don't print closing error
		}
		try {
			// Close the input stream
			this.inputStream.close();
		} catch (IOException e) {
			// Don't print closing error
		}
		try {
			// Close the incoming socket
			this.incomingSocket.close();
		} catch (IOException e) {
			// Don't print closing error
		}
		try {
			// Close the outgoing streaming socket
			this.streamingSocket.close();
		} catch (IOException e) {
			// Don't print closing error
		}
		try {
			// Close the server socket
			this.serverSocket.close();
		} catch (IOException e) {
			// Don't print closing error
		}
		
		// End any hanging threads
		this.endThread.interrupt();
		this.discoveryThread.interrupt();
		this.sendThread.interrupt();
		this.listeningThread.interrupt();
		
		// Exit the program
		System.out.println("Goodbye");
		System.exit(0);
    }
    
    /**
     * A method to close the connections
     */
    public void close(){
    	// Send the end packet to the other computer
    	byte[] data = "END".getBytes();
    	try {
			this.sendPacketData(data, this.connectedComputerIP);
		} catch (IOException e) {
			// Don't print closing error
		}
    	// Exit the chat and program
    	this.exit();
    }
}
