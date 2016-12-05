import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class Connection {
	private InetAddress serverIP;
	
	public Connection() throws Exception{
		this.serverIP = null;
	}
	
	public boolean sendData(byte[] requestData) throws Exception{
		// Initiate the DatagramSocket
		DatagramSocket socket;
		socket = new DatagramSocket();
		socket.setBroadcast(true);
		
		// Create and send the packet
		DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length, this.serverIP, 8888);
		socket.send(requestPacket);
    	
		socket.close();
		return true;
	}
	
	public InetAddress getServerIP(){
		return this.serverIP;
	}
	
	public class DiscoveryThread implements Runnable {

		@Override
		public void run() {
			try {
				// Listening flag
				boolean continueListening = true;
				
				
		    	// Initiate the DatagramSocket
		    	DatagramSocket socket;
		    	// Listen on port 8888
		    	socket = new DatagramSocket(8888, InetAddress.getByName("0.0.0.0"));
		    	socket.setBroadcast(true);
		    	
				// Continue listening
				while(continueListening){
					// Receive a packet
					byte[] received = new byte[10000];
					DatagramPacket packet = new DatagramPacket(received, received.length);
			        socket.receive(packet);
			        
			        String message = new String(packet.getData()).trim();
			        if(message.equals("DISCOVERY")){
			        	// For testing purposes
			        	System.out.println("Discovery packet received from: " + packet.getAddress().getHostAddress());
			        	
			        	// Respond to the client, confirming this is the server
			        	byte[] sendData = "DISCOVERY_RESPONSE".getBytes();
			        	//Send the response
			        	DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
			        	socket.send(sendPacket);
			        	
			        } else {
			        	// Place wrapper functions here
			        	System.out.println("Data Received From: " + packet.getAddress().getHostAddress());
			        	System.out.println("Data: " + new String(packet.getData()));
			        }
				}
		    	
		    	socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void beginListening(){
		Thread discoveryThread = new Thread(new DiscoveryThread());
		discoveryThread.start();
	}
	
	public InetAddress getIP() throws Exception{
		
		// Initiate the DatagramSocket
		DatagramSocket socket;
		socket = new DatagramSocket();
		socket.setBroadcast(true);
		
		// Create the response header
		byte[] request = "DISCOVERY".getBytes();
		
		// Create and send the packet
		DatagramPacket requestPacket = new DatagramPacket(request, request.length, InetAddress.getByName("255.255.255.255"), 8888);
		socket.send(requestPacket);
		
		// Iterate through all network interfaces and check for the server
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces.hasMoreElements()) {
			NetworkInterface networkInterface = interfaces.nextElement();
			
			// Filter out loopback and interfaces that don't point to servers
		    if (networkInterface.isLoopback() || !networkInterface.isUp()) {
		      continue;
		    }
		    
		    // Iterate over the addresses
		    for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
		      InetAddress broadcast = interfaceAddress.getBroadcast();
		      // If there is no server, don't send a packet
		      if (broadcast == null) {
		        continue;
		      }
		
		      // Found a server, send the packet
		      requestPacket = new DatagramPacket(request, request.length, broadcast, 8888);
		      socket.send(requestPacket);
		
		    }
		}
		
		// Declare the receiving byte array
		byte[] response = new byte[15000];
		// Receive the packet
		DatagramPacket responsePacket = new DatagramPacket(response, response.length);
		socket.receive(responsePacket);
		
		// Check if the server is the desired server by checking the message
		String message = new String(responsePacket.getData()).trim();
		if (message.equals("DISCOVERY_RESPONSE")) {
			// Set the server IP
			this.serverIP = responsePacket.getAddress();
			// Close the socket
			socket.close();
			// Return the IP address
			return responsePacket.getAddress();
		}

		// Close the socket
		socket.close();
		
		// Error, could not find server
		return null;
	}

}
