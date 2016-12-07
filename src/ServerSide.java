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
        Connection connection = new Connection(8888, null);
        // Connection connection = new Connection(8888);
        
        // Begin listening
        connection.beginPacketListening();
        try {
            connection.discoveryThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        connection.beginStreaming();
        while(!connection.startStreaming){
            // Wait
        }
    }
    
}
