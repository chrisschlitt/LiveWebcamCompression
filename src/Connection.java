import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

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
    // The port used
    private int port;
    // The output stream
    private ObjectOutputStream outputStream;
    // A flag to continue listening for packets
    public boolean continueListening;
    // A flag to start streaming
    public boolean startStreaming;
    // Discovery Thread
    public Thread discoveryThread;
    // Receiving Thread
    public Thread listeningThread;
    // Output Socket
    public Socket streamingSocket;
    // The receiving model for callbacks
    public ClientModel receivingModel;
    
    /**
     * Constructor
     *
     * @param port: int - The port to send the discovery packets
     * @param receivingModel: ClientModel - The ClientModel that will be receiving the images (null for sender)
     */
    public Connection(int port, ClientModel receivingModel){
    	System.out.println("Initializing Connection");
        // Set the port
        this.port = port;
        // Set the receiving model
        this.receivingModel = receivingModel;
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
     * A runnable class to manage incoming packets
     *
     * @author christopherschlitt
     *
     */
    public class DiscoveryThread implements Runnable {
    	boolean receivedDiscovery = false;
    	
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
                while(Connection.this.continueListening){
                	// System.out.println("Ready to receive a packet");
                    // Listen on specified port
                    socket = new DatagramSocket(Connection.this.port, InetAddress.getByName("0.0.0.0"));
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
                    // System.out.println("Received Packet: " + message);
                    // Route the message
                    if(message.equals("DISCOVERY") && !fromAddr.equals(localAddr)){
                    	if(!receivedDiscovery){
                    		receivedDiscovery = true;
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
                    		// Stop packet listening
                    		Connection.this.continueListening = false;
                    		System.out.println("Connected to Server");
                            // Received discovery response
                            // Set the connected computer (client) IP
                            Connection.this.connectedComputerIP = packet.getAddress();
                            // Prepare the receiving stream
                            Connection.this.beginListeningForStream();
                    	}
                    	
                    } else if (message.equals("STREAMREADY")) {
                		// Received stream ready response
                        // Stop packet listening
                        Connection.this.continueListening = false;
                    }
                    
                    // Close the socket
                    socket.close();
                }
                
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
        // Initiate the DatagramSocket
        DatagramSocket socket;
        socket = new DatagramSocket();
        socket.setBroadcast(true);
        
        // Create and send the packet
        DatagramPacket requestPacket = new DatagramPacket(data, data.length, address, this.port);
        socket.send(requestPacket);
        try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        // Send second packet to ensure delivery
        socket.send(requestPacket);
        
        // Close the socket
        socket.close();
        
        return true;
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
                requestPacket = new DatagramPacket(request, request.length, tempAddr, this.port);
                // System.out.println("Sending a discovery packet to: " + ipAddress[0] + "." + ipAddress[1] + "." + ipthree + "." + j);
                
                // Send the packet
                socket.send(requestPacket);
                // Check if the discovery response packet has been received
                if(this.connectedComputerIP != null){
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
            if(this.connectedComputerIP != null){
                // If the server has been found, break the loop
                break;
            }
        }
        
    }
    
    /**
     * A Runnable class to handle listening for the data stream
     *
     * @author christopherschlitt
     *
     */
    public class ListeningThread implements Runnable {
        // Input Stream
        private ObjectInputStream inputStream;
        // IncomingSocket
        private ServerSocket serverSocket;
        // IncomingSocket
        private Socket incomingSocket;
        // Receiving Model
        private ClientModel receivingModel;
        
        /**
         * A method to prepare the receiving connection
         *
         * @return success: boolean - A success flag
         */
        public boolean prepareReceivingConnection() throws IOException{
        	// System.out.println("Preparing to receive stream");
            // Create the socket
            this.serverSocket = new ServerSocket(4445);
            // Send the stream ready packet to the server
            byte[] data = "STREAMREADY".getBytes();
            Connection.this.sendPacketData(data, Connection.this.connectedComputerIP);
            // Accept the incoming stream (break until accepted)
            this.incomingSocket = this.serverSocket.accept();
            // Create the input stream
            this.inputStream = new ObjectInputStream(this.incomingSocket.getInputStream());
            
            return true;
        }
        
        /**
         * A method to set the receiving model for the thread
         * @param receivingModel: ClientModel - ClientModel with receiveImage method
         */
        public ListeningThread(ClientModel receivingModel){
            this.receivingModel = receivingModel;
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
                // While the stream is open
                System.out.println("Ready to receive strem");
                while(Connection.this.startStreaming){
                	// System.out.println("Ready to receive stream object");
                    // Receive the image
                    this.receivingModel.receiveImage((byte[])this.inputStream.readObject());
                }
            } catch(Exception e){
                e.getStackTrace();
            }
        }
    }
    
    /**
     * A method to prepare to receive a stream
     */
    public void beginListeningForStream() throws IOException{
        // Set the start streaming flag
        this.startStreaming = true;
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
            this.streamingSocket = new Socket(this.connectedComputerIP, 4445);
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
    	// System.out.println("Sending a stream object");
        // Cast the object as a byte array
        byte[] data = (byte[])o;
        // Write the object to the stream
        try {
            this.outputStream.writeObject(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
