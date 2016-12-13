import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * This class takes the Image and performs RGB Compression in threads
 * @author jacob
 *
 */
public class RGBCompression {
//	Instance Variables
	int[][] red;
	int[][] green;
	int[][] blue;
	int[][] grey;
	byte[] compressedImage;

	/**
	 * Constructor that initiates the compression threads
	 * @param image
	 * @param ratio
	 * @param color
	 */
	public RGBCompression(BufferedImage image, int ratio, int color) {
        synchronized(this) {		
		red = new int[image.getHeight()][image.getWidth()];
		green = new int[image.getHeight()][image.getWidth()];
		blue = new int[image.getHeight()][image.getWidth()];
		grey = new int[image.getHeight()][image.getWidth()];
		if (color == 0) { // Colored image
//			Get the R, G and B layers
			setUpRGBArrays(image);
			
//			Separate instances are created for the 3 layers
			ImageCompression redCompression = new ImageCompression(red, ratio, color);
			ImageCompression greenCompression = new ImageCompression(green, ratio,color);
			ImageCompression blueCompression = new ImageCompression(blue, ratio,color);
		    
//			Individual threads are started for the three layers
			Thread redThread = new Thread(redCompression);
		    Thread greenThread = new Thread(greenCompression);
		    Thread blueThread = new Thread(blueCompression);
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
		        
//		    Once all the threads are completed set up and return the compressed image
			byte[] redCompressed = redCompression.getCompressedImage();
			byte[] greenCompressed = greenCompression.getCompressedImage();
			byte[] blueCompressed = blueCompression.getCompressedImage();
			compressedImage = new byte[blueCompressed.length * 3];
			compressedImage = mergeArray(redCompressed,greenCompressed);
			compressedImage = mergeArray(compressedImage,blueCompressed);			
		} else {
			
//			If it is grayscale then only one layer of compression is required
			setUpGreyArrays(image);
			ImageCompression greyCompression = new ImageCompression(grey, ratio, color);
			Thread greyThread = new Thread(greyCompression);
			greyThread.start();
			try {
				greyThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			compressedImage = greyCompression.getCompressedImage();
			
		}
		

        }
	}
	/**
	 * Getter method for the compressed image
	 * @return image
	 */
	public byte[] getCompressedImage() {
		return compressedImage;
	}
	
	/**
	 * Helper method to concatenate 1D arrays
	 * @param redCompressed
	 * @param greenCompressed
	 * @return 1D array
	 */
	private byte[] mergeArray(byte[] redCompressed, byte[] greenCompressed) {
		
		byte[] combined = new byte[redCompressed.length + greenCompressed.length];
		int k = 0;
		
		for (int i = 0; i < redCompressed.length; i++) {
			combined[k] = redCompressed[i];
			k++;
		}
		
		for (int i = 0; i < greenCompressed.length; i++) {
			combined[k] = greenCompressed[i];
			k++;
		}
		
		return combined;
	}

	/**
	 * Set up the Gray scale array
	 * @param image
	 */
	private void setUpGreyArrays(BufferedImage image) {
		Color color;
		int[] imageColors = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
		int count = 0; 
		int x = 0;
		int y = 0;
//		Loop across all pixels and find avg of red, green and blue colors
		while (count < imageColors.length) {
			if (x==image.getWidth()) {
				x = 0;
				y++;
			}
			color = new Color(imageColors[count]);
//			System.out.println( x + "," + y);
			grey[y][x] = (color.getRed() + color.getGreen() + color.getBlue())/3;
			
			x++;
			count++;
		}
	}
	
	/**
	 * Split image into Layers
	 * @param image
	 */
	private void setUpRGBArrays(BufferedImage image) {
		Color color;
		int[] imageColors = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
		int count = 0; 
		int x = 0;
		int y = 0;
//		Loop across the entire image and extract the R, G and B layers
		while (count < imageColors.length) {
			if (x==image.getWidth()) {
				x = 0;
				y++;
			}
			color = new Color(imageColors[count]);
//			System.out.println( x + "," + y);
			red[y][x] = color.getRed();
			green[y][x] = color.getGreen();
			blue[y][x] = color.getBlue();
			
			x++;
			count++;
		}
	}
}