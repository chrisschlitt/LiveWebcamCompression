
public class ClientSide {

	public static void main(String[] args) throws Exception {
		
		// Create the client connection
		Connection connection = new Connection();
		connection.getIP();
		
		// Print the IP Address of the server
		System.out.println("Found the server at IP Address: " + connection.getServerIP());
		
		// Send a test Message
		byte[] request = "TEST_MESSAGE".getBytes();
		if(connection.sendData(request)){
			System.out.println("Sent message successfully");
		}
		 
	}

}
