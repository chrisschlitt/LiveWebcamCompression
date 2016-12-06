import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;

public class ServerModel {
	private static int numPictures=1;
	
	public void getPicture(Webcam webcam) {
		Color color;
		int r = 0;
		int g = 0;
		int b = 0;
		BufferedImage image = webcam.getImage();
		BufferedImage reconstructed = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		
		try {
			ImageIO.write(image, "PNG", new File("test"+numPictures+".png"));
			int[] imageColors = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
			int count = 0; 
			int x = 0;
			int y = 0;
			int average = 0;
			int img[][] = new int[image.getHeight()][image.getWidth()];
			int[][] expandedImg;
			while (count < imageColors.length) {
				if (x==image.getWidth()) {
					x = 0;
					y++;
				}
				color = new Color(imageColors[count]);
				r = color.getRed();
				g = color.getGreen();
				b = color.getBlue();
				average = (r+g+b)/3;
				
				// Creating average array to be passed for compression 
				img[y][x] = average;
				
				
//				reconstructed.setRGB(x, y, new Color(average, average, average).getRGB());
				x++;
				count++;
			}
			
			
			
			ImageCompression imgCompression = new ImageCompression(img);
			ImageReconstruction imgReconstruction = new ImageReconstruction(imgCompression.getCompressedImage());
			expandedImg = imgReconstruction.getReconstructedImage();
			count = 0; 
			x = 0;
			y = 0;					
			while (count < imageColors.length) {
				if (x==image.getWidth()) {
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
		} catch (IOException ex){
			ex.printStackTrace();
		}
	}

	
	
}
