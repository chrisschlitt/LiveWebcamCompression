
public class ServerSide {
    
    public static void main(String[] args) throws Exception {
        // Create the server connection
        Connection connection = new Connection();
        // Begin listening
        connection.beginListening();
    }
    
}
