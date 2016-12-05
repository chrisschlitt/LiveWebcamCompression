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
	
	public boolean sendData(byte[] requestData, InetAddress ip) throws Exception{
		// Initiate the DatagramSocket
		DatagramSocket socket;
		socket = new DatagramSocket();
		socket.setBroadcast(true);
		
		// Create and send the packet
		DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length, ip, 8888);
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
					byte[] received = new byte[350000];
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
		
		// Create and send the packet to the local subnet
		DatagramPacket requestPacket = new DatagramPacket(request, request.length, InetAddress.getByName("255.255.255.255"), 8888);
		socket.send(requestPacket);
		
		// Send packet across subnets
		InetAddress tempAddr;
		String[] ipAddress = InetAddress.getLocalHost().toString().split("/")[1].split("\\.");

		for(int i=0; i<255; i++){
			// Check addresses on specified subnet
			tempAddr = InetAddress.getByName(ipAddress[0] + "." + ipAddress[1] + "." + i + ".0");
			requestPacket = new DatagramPacket(request, request.length, tempAddr, 8888);
			socket.send(requestPacket);
	      }
		
		
		// Declare the receiving byte array
		byte[] response = new byte[350000];
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
