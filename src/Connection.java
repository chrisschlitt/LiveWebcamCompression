import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * A class to manage a connection between two computers on the same network
 * The computers may be on different subnets
 */
public class Connection {
	// The IP address of the other computer you are connecting to
    private InetAddress connectedComputerIP;
    // A flag to continue listening
    private boolean continueListening;
    // The specified port to send/receive packets from
    private int port;
    // Receiving Model
    private ClientModel clientModel;
    
    /**
     * Constructor
     * 
     * @param port: int - The port to send/receive packets from
     */
    public Connection(int port, ClientModel receivingModel){
        this.connectedComputerIP = null;
        this.continueListening = false;
        this.port = port;
        this.clientModel = receivingModel;
    }
    
    /**
     * Constructor without a port
     * 
     */
    public Connection(ClientModel receivingModel){
    	this(8888, receivingModel);
    }
    
    /**
     * Constructor without a port
     * 
     */
    public Connection(){
    	this(8888, new ClientModel());
        this.clientModel = clientModel;
    }
    
    /**
     * A method to send a data packet to the other computer
     * 
     * @param requestData: byte[] - The data to send
     * @return boolean: success - The result of the send
     * @throws Exception - If the connection failed due to a network issue
     */
    public boolean sendData(byte[] requestData) throws Exception{
    	
    	System.out.println("Total Size: " + requestData.length);
    	
    	
    	// Initiate the DatagramSocket and DatagramPacket
        DatagramSocket socket;
        DatagramPacket requestPacket;
    	
    	// Send start packet
    	byte[] sendData = "START_PACKET".getBytes();
    	socket = new DatagramSocket();
        socket.setBroadcast(true);
        requestPacket = new DatagramPacket(sendData, sendData.length, this.connectedComputerIP, this.port);
        socket.send(requestPacket);
        socket.close();
        
        // Send size packet
    	sendData = ("SIZE:" + requestData.length).getBytes();
    	socket = new DatagramSocket();
        socket.setBroadcast(true);
        requestPacket = new DatagramPacket(sendData, sendData.length, this.connectedComputerIP, this.port);
        socket.send(requestPacket);
        socket.close();
    	
    	
    	byte[] sendingData;
    	int b = 0;
    	while(b < requestData.length){
    		sendingData = new byte[1024];
    		for(int i = 0; i < 1024; i++){
    			sendingData[i] = requestData[b];
    			b++;
    			if(b >= requestData.length){
    				break;
    			}
        	}
    		socket = new DatagramSocket();
            socket.setBroadcast(true);
            requestPacket = new DatagramPacket(sendingData, sendingData.length, this.connectedComputerIP, this.port);
            socket.send(requestPacket);
            socket.close();
    	}
    	
    	
    	// Send end packet
    	sendData = "END_PACKET".getBytes();
    	socket = new DatagramSocket();
        socket.setBroadcast(true);
        requestPacket = new DatagramPacket(sendData, sendData.length, this.connectedComputerIP, this.port);
        socket.send(requestPacket);
        socket.close();
    	
        return true;
    }
    
    /**
     * A method to send data to a computer other than the connected server
     * 
     * @param requestData: byte[] - The data to send
     * @param ip: InetAddress - IP address of the computer to send the data to
     * @return boolean: success - The result of the send
     * @throws Exception - If the connection failed due to a network issue
     */
    public boolean sendData(byte[] requestData, InetAddress ip) throws Exception{
    	System.out.println("Sending Custom Data to: " + ip);
    	
        // Initiate the DatagramSocket
        DatagramSocket socket;
        socket = new DatagramSocket();
        socket.setBroadcast(true);
        
        // Create and send the packet
        DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length, ip, this.port);
        socket.send(requestPacket);
        
        // Close the socket
        socket.close();
        return true;
    }
    
    /**
     * A method to get the server IP 
     * @return serverIp: InetAddress - The IP address of the server
     */
    public InetAddress getServerIP(){
        return this.connectedComputerIP;
    }
    
    /**
     * A method to check if the computer has connected to another computer
     * 
     * @return result: boolean - An indicator if the computer has been connected to another computer
     */
    public boolean isConnected(){
    	if(this.connectedComputerIP != null){
    		return true;
    	} else {
    		return false;
    	}
    }
    
    /**
     * A method to get the port
     * @return port: int - The port to send/receive packets from
     */
    public int getPort(){
    	return this.port;
    }
    
    /**
     * A runnable class to manage incoming packets
     * 
     * @author christopherschlitt
     *
     */
    public class ListeningThread implements Runnable {
        byte[] receivedImage;
        int receivedImageIndex;
    	/**
    	 * The run method
    	 */
        @Override
        public void run() {
            try {
                // Initiate the DatagramSocket
                DatagramSocket socket;
                // Listen on specified port
                socket = new DatagramSocket(Connection.this.port, InetAddress.getByName("0.0.0.0"));
                socket.setBroadcast(true);
                
                String localAddr = InetAddress.getLocalHost().toString().split("/")[1];
                
                
                
                // Continue listening
                while(Connection.this.continueListening){
                    // Receive a packet
                    byte[] received = new byte[1000];
                    DatagramPacket packet = new DatagramPacket(received, received.length);
                    socket.receive(packet);
                    
                    String message = new String(packet.getData()).trim();

                    String fromAddr = "";
                    try {
                    	fromAddr = packet.getAddress().toString().split("/")[1];
                    } catch (Exception e){
                    	fromAddr = packet.getAddress().getHostAddress().toString();
                    }
                    
                    System.out.println("Received Message From: " + fromAddr);
                    
                    // Route the message
                    if(message.equals("DISCOVERY") && !fromAddr.equals(localAddr)){
                    	// Received discovery message
                        // For testing purposes
                        System.out.println("Discovery packet received from: " + packet.getAddress().getHostAddress());
                        
                        // Set the client IP
                        Connection.this.connectedComputerIP = packet.getAddress();
                        
                        // Respond to the client, confirming this is the server
                        byte[] sendData = "DISCOVERY_RESPONSE".getBytes();
                        Connection.this.sendData(sendData, packet.getAddress());
                        
                    } else if (message.equals("DISCOVERY_RESPONSE") && !fromAddr.equals(localAddr)) {
                    	// Received discovery response
                        // Set the server IP
                        Connection.this.connectedComputerIP = packet.getAddress();
                    } else if(message.equals("START_PACKET")){
                    	System.out.println("Received Start Packet");
                    	this.receivedImageIndex = 0;
                    } else if(message.startsWith("SIZE")){
                    	System.out.println("Received Size Packet (" + message + ")");
                    	
                    	String messageComponents = message.substring(message.indexOf(':')+1, message.length());

                    	System.out.println("Size: " + messageComponents);
                    	this.receivedImage = new byte[Integer.parseInt(messageComponents)];
                    } else if(message.equals("END_PACKET")){
                    	System.out.println("Received End Packet");
                    	Connection.this.clientModel.receiveImage(this.receivedImage);
                    } else if(!fromAddr.equals(localAddr)) {
                    	System.out.println("Received Portion Packet");
                    	byte[] receivedPortion = packet.getData();
                    	for(int i = 0; i < receivedPortion.length; i++){
                    		this.receivedImage[this.receivedImageIndex] = receivedPortion[i];
                    		this.receivedImageIndex++;
                    	}
                        
                    }
                    
                }
                // Close the socket
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * A method to start listening for incoming packets
     */
    public void beginListening(){
    	// Set continue listening flag
    	this.continueListening = true;
    	// Create the thread
        Thread listeningThread = new Thread(new ListeningThread());
        // Start the thread
        listeningThread.start();
    }
    
    /**
     * A method to stop the listening thread
     */
    public void stopListening(){
    	// Set the continue listening flag
    	this.continueListening = false;
    }
    
    /**
     * A mthod to discover the IP address of the server
     * @throws Exception
     */
    public void discoverIP() throws Exception{
        
        // Initiate the DatagramSocket
        DatagramSocket socket;
        socket = new DatagramSocket();
        socket.setBroadcast(true);
        
        // Create the response header
        byte[] request = "DISCOVERY".getBytes();
        
        // Create and send the packet to the local subnet
        DatagramPacket requestPacket = new DatagramPacket(request, request.length, InetAddress.getByName("255.255.255.255"), this.port);
        socket.send(requestPacket);
        socket.close();
        
        // Get the local IP address
        InetAddress tempAddr;
        String localAddr = InetAddress.getLocalHost().toString().split("/")[1];
        String[] ipAddress = InetAddress.getLocalHost().toString().split("/")[1].split("\\.");
        
        // Loop through all IP addresses on the local subnet, then try +/- subnets
        int ipthree = Integer.parseInt(ipAddress[2]);
        int ii = 1;
        while(ii < 255){
        	int add = 0;
        	if(ipthree > 255){
        		ipthree = ipthree - 255;
        		add = 1;
        	}
        	if(ipthree < 0){
        		ipthree = ipthree + 255;
        		add = -1;
        	}
        	
        	// Loop through the local subnet
        	for(int j=0; j<255; j++){
	            // Check addresses on specified subnet
	            tempAddr = InetAddress.getByName(ipAddress[0] + "." + ipAddress[1] + "." + ipthree + "." + j);

	            // Check if sending message to own IP address and filter out
	            if(localAddr.trim().equals((ipAddress[0] + "." + ipAddress[1] + "." + ipthree + "." + j).trim())){
	            	continue;
	            }
	            requestPacket = new DatagramPacket(request, request.length, tempAddr, this.port);
	            
	            socket = new DatagramSocket();
	            socket.setBroadcast(true);
	            socket.send(requestPacket);
	            socket.close();
	            
	            if(this.connectedComputerIP != null){
	            	// If the server has been found, break the loop
	        		break;
	        	}
        	}
        	
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
    
}
