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
	private Connection connection = new Connection(this);
	
	public void setupConnection() {
		connection.beginListening();
	}
	
	public void receiveImage(byte[] compressedImage) throws Exception {
		
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
		
		ImageIO.write(reconstructed, "PNG", new File("reconstructed"+numPictures+".png"));
		numPictures++;
		
		
	}
	
	
}