import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

/**
  * This class takes the Image and performs RGB Reconstruction in threads
 * @author jacob
 *
 */
public class RGBReconstruction {
//	Instance Variables
	byte[] rCompressed;
	byte[] gCompressed;
	byte[] bCompressed;
	byte[] grayCompressed;
	BufferedImage reconstructedImage;
	
	/**
	 * Constructor to reconstruct image
	 * @param compressedImage
	 */
	public RGBReconstruction(byte[] compressedImage) {
	
		int[][] redReconstructed;
		int[][] greenReconstructed;
		int[][] blueReconstructed;
	
	synchronized(this) {	
	 
//	 Convert Byte array to int
     IntBuffer intBuf = ByteBuffer.wrap(compressedImage)
			     .order(ByteOrder.BIG_ENDIAN)
			     .asIntBuffer();
     int[] colorArray = new int[intBuf.remaining()];
	 intBuf.get(colorArray);
	 
	 if (colorArray[colorArray.length - 5] == 0) { // If color image was sent
		    rCompressed = new byte[compressedImage.length/3];
			gCompressed = new byte[compressedImage.length/3];
			bCompressed = new byte[compressedImage.length/3];
			
//			Instantiate Reconstruction threads for 3 colors
			setUpRGBCompressed(compressedImage);
			ImageReconstruction redReconstruction = new ImageReconstruction(rCompressed);
			ImageReconstruction greenReconstruction = new ImageReconstruction(gCompressed);
			ImageReconstruction blueReconstruction = new ImageReconstruction(bCompressed);
			
//			Start the threads
			Thread redThread = new Thread(redReconstruction);
		    Thread greenThread = new Thread(greenReconstruction);
		    Thread blueThread = new Thread(blueReconstruction);
		    redThread.start();
		    greenThread.start();
		    blueThread.start();
		    try {
				redThread.join();
			} catch (InterruptedException e) {
			}
		    
		    try {
				greenThread.join();
			} catch (InterruptedException e) {
			}
		    
		    try {
				blueThread.join();
			} catch (InterruptedException e) {
			} 
		    
			redReconstructed = redReconstruction.getReconstructedImage();
			greenReconstructed = greenReconstruction.getReconstructedImage();
			blueReconstructed = blueReconstruction.getReconstructedImage();
	 
	 } else {
		 grayCompressed = new byte[compressedImage.length];
		 grayCompressed = compressedImage;
		 ImageReconstruction grayReconstruction = new ImageReconstruction(grayCompressed);
		 Thread grayThread = new Thread(grayReconstruction);
		 grayThread.start();
		    try {
		    	grayThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
	
		    redReconstructed = grayReconstruction.getReconstructedImage();
			greenReconstructed = grayReconstruction.getReconstructedImage();
			blueReconstructed = grayReconstruction.getReconstructedImage();
	 
	 }
	 
	    
		

		
		reconstructedImage = setUpImage(redReconstructed, greenReconstructed, blueReconstructed);
	}
	}
	
	public BufferedImage getReconstructedImage() {
		return reconstructedImage;
	};
	
	/**
	 * Set up the image from the three layers
	 * @param redReconstructed
	 * @param greenReconstructed
	 * @param blueReconstructed
	 * @return Image
	 */
	private BufferedImage setUpImage(int[][] redReconstructed, int[][] greenReconstructed, int[][] blueReconstructed) {
		BufferedImage reconstructed = new BufferedImage(redReconstructed[0].length, redReconstructed.length, BufferedImage.TYPE_INT_RGB);
		int count = 0, x = 0, y = 0;
		while (count < redReconstructed.length*redReconstructed[0].length) {
			if (x==redReconstructed[0].length) {
				x = 0;
				y++;
			}
			
			
			int red = redReconstructed[y][x];	
			int green = greenReconstructed[y][x];
			int blue = blueReconstructed[y][x];
			
			// Set the red, green and blue colors
			reconstructed.setRGB(x, y, new Color(red, green,blue).getRGB());
			x++;
			count++;
		}
		
		return reconstructed;
	}
	/**
	 * Split the Compressed data into R, G and B layers
	 * @param compressedImage
	 */
	private void setUpRGBCompressed(byte[] compressedImage) {
		int length = compressedImage.length / 3;
		
		int k = 0;
		for (int i=0; i< length; i++) {
			rCompressed[i] = compressedImage[k];
			k++;
		}
		
		for (int i=0; i< length; i++) {
			gCompressed[i] = compressedImage[k];
			k++;
		}
		
		for (int i=0; i< length; i++) {
			bCompressed[i] = compressedImage[k];
			k++;
		}
		
	}
}