import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;

public class ClientModel {
	private static int numPictures=1;
    // The connection to the server
    private Connection connection;
    private ClientView clientView;
    
    /**
     * A method to setup the client server
     */
    public void setupConnection() throws Exception {
        // Create the connection
        connection = new Connection(8888, this);
        // Begin listening for packets
        connection.beginPacketListening();
        // Discover the server's IP address
        connection.discoverIP();
        // Wait until the server is discovered
        connection.discoveryThread.join();
    }
    
    /**
     * A callback method when a connection received an image
     * 
     * @param compressedImage: byte[] - Received image
     */
	public void receiveImage(byte[] compressedImage) throws Exception {	

         RGBReconstruction rgbReconstruction = new RGBReconstruction(compressedImage);
         BufferedImage reconstructed = rgbReconstruction.getReconstructedImage();
        
		this.clientView.displayImage(reconstructed);
		numPictures++;
	}
	
	public void setClientView(ClientView clientView) {
		this.clientView=clientView;
	}
	
}
