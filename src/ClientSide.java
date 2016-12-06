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
        // Create the client connection
        Connection connection = new Connection();
        // Begin listening
        connection.beginListening();
        // Discover the server
        connection.discoverIP();
        
        // Print the IP Address of the server
        System.out.println("Found the server at IP Address: " + connection.getServerIP());
        
        // Send a test Message
        byte[] request = "TEST_MESSAGE_FROM_CLIENT".getBytes();
        if(connection.sendData(request)){
            System.out.println("Sent message successfully");
        } else {
        	System.out.println("Error Sending Message");
        }
        
    }
    
}
