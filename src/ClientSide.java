/**
 * A class to demonstrate the client computer
 * 
 * -In this case, the client is the computer receiving the video stream
 * -The client also handles the discovery of the server
 * 
 * @author christopherschlitt
 *
 */
public class ClientSide {
    
    public static void main(String[] args) throws Exception {
    	// Create the server connection over the default port
        Connection connection = new Connection();
        // Connection connection = new Connection(8888);
        
        // Begin listening
        connection.beginListening();
        // Discover the server
        System.out.println("Looking for server...");
        connection.discoverIP();
        
        while(!connection.isConnected()){
        	// Wait
        }
        
        System.out.println("Finished trying to connect");
        
        // Print the IP Address of the server
        if(connection.getServerIP() != null){
        	System.out.println("Found the server at IP Address: " + connection.getServerIP());
        	
        	// Send a test Message
            byte[] request = "TEST_MESSAGE_FROM_CLIENT".getBytes();
            if(connection.sendData(request)){
                System.out.println("Sent message successfully");
            } else {
            	System.out.println("Error Sending Message");
            }
        } else {
        	System.out.println("Could not find the server");
        }
        
    }
    
}
