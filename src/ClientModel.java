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
		//System.out.println("RECEIVING PACKAGE " + numPictures);
		int count;
		int x;
		int y;
		int average;
		ImageReconstruction imgReconstruction = new ImageReconstruction(compressedImage);
		int[][] expandedImg;
		expandedImg = imgReconstruction.getReconstructedImage();
		count = 0; 
		x = 0;
		y = 0;	
		
		BufferedImage reconstructed = new BufferedImage(expandedImg[0].length, expandedImg.length, BufferedImage.TYPE_INT_ARGB);
		while (count < expandedImg.length*expandedImg[0].length) {
			if (x==expandedImg[0].length) {
				x = 0;
				y++;
			}
			
			average = expandedImg[y][x];	
			reconstructed.setRGB(x, y, new Color(average, average, average).getRGB());
			x++;
			count++;
		}
		this.clientView.displayImage(reconstructed);
		numPictures++;
	}
	
	public void setClientView(ClientView clientView) {
		this.clientView=clientView;
	}
	
}
