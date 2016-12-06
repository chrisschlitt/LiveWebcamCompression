/**
 * A class to demonstrate the server computer
 * 
 * -In this case, the server is the computer sending the video stream
 * -The server waits until a client is connected
 * 
 * @author christopherschlitt
 *
 */
public class ServerSide {
    
    public static void main(String[] args) throws Exception {
        // Create the server connection over the default port
        Connection connection = new Connection();
        // Connection connection = new Connection(8888);
        
        // Begin listening
        connection.beginListening();
        
        // Wait until the client has connected
        System.out.println("Waiting for client connection...");
        while(!connection.isConnected()){
        	// Wait
        }
        System.out.println("Connected to Client");
        
        // Send a test Message
        byte[] request = "TEST_MESSAGE_FROM_SERVER".getBytes();
        if(connection.sendData(request)){
            System.out.println("Sent message successfully");
        } else {
        	System.out.println("Error Sending Message");
        }
    }
    
}
