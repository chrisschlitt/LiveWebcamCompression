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
        // Create the server connection
        Connection connection = new Connection();
        // Begin listening
        connection.beginListening();
    }
    
}
