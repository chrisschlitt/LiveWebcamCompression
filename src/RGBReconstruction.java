import java.awt.Color;
import java.awt.image.BufferedImage;

public class RGBReconstruction {
	
	byte[] rCompressed;
	byte[] gCompressed;
	byte[] bCompressed;
	BufferedImage reconstructedImage;
	
	public RGBReconstruction(byte[] compressedImage) {
		rCompressed = new byte[compressedImage.length/3];
		gCompressed = new byte[compressedImage.length/3];
		bCompressed = new byte[compressedImage.length/3];
		
		setUpRGBCompressed(compressedImage);
		ImageReconstruction redReconstruction = new ImageReconstruction(rCompressed);
		ImageReconstruction greenReconstruction = new ImageReconstruction(gCompressed);
		ImageReconstruction blueReconstruction = new ImageReconstruction(bCompressed);
		
		Thread redThread = new Thread(redReconstruction);
	    Thread greenThread = new Thread(greenReconstruction);
	    Thread blueThread = new Thread(blueReconstruction);
	    redThread.start();
	    greenThread.start();
	    blueThread.start();
	    try {
			redThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	    
	    try {
			greenThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	    
	    try {
			blueThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	    
		
		int[][] redReconstructed = redReconstruction.getReconstructedImage();
		int[][] greenReconstructed = greenReconstruction.getReconstructedImage();
		int[][] blueReconstructed = blueReconstruction.getReconstructedImage();
		
		reconstructedImage = setUpImage(redReconstructed, greenReconstructed, blueReconstructed);
		
	}
	
	public BufferedImage getReconstructedImage() {
		return reconstructedImage;
	};
	
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
			
			reconstructed.setRGB(x, y, new Color(red, green,blue).getRGB());
			x++;
			count++;
		}
		
		return reconstructed;
	}
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