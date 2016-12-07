import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;



public class ClientModel implements Model {
	private static int numPictures=1;
    // The connection to the server
    private Connection connection;
    private DisplayView clientView;
    
	private boolean doneStreaming = false;
	private int compression = 2;
	private Webcam webcam;
	private Thread takePictureThread;
	private Thread clientProcessPictureThread;
	private BlockingQueue<BufferedImage> imageQueue = new LinkedBlockingQueue<BufferedImage>();
    
    /**
     * A method to setup the client server
     */
    public void setupConnection() throws Exception {
    	connection = new Connection(8888, 6987, 4555, this);
        connection.connectToServer();
    }
    
    public void setDoneStreaming(boolean doneStreaming) {
    	this.doneStreaming=doneStreaming;
    }
    
    
    /**
     * A wrapper method to stream a picture
     * @param compressedImage: byte[] - Image to send
     */
    public void sendPicture(byte[] compressedImage) throws Exception {
        connection.sendStreamData(compressedImage);
    }
    
    
    public class TakePictureThread implements Runnable {
    	private Webcam webcam = ClientModel.this.webcam;
    	public void run() {
    		 try {
                 while (!ClientModel.this.doneStreaming) {
                 	BufferedImage image = webcam.getImage();
                 	
                 	if (imageQueue.size()>5){
                 		imageQueue.take();
                 	}
                 	imageQueue.put(image);
                 	try {
                 	    Thread.sleep(33);
                 	} catch(InterruptedException ex) {
                 	    Thread.currentThread().interrupt();
                 	}
                 }
                 
             } catch (Exception e) {
                 e.printStackTrace();
             }
    	}
    }
    
    
    public class ClientProcessPictureThread implements Runnable {
        /**
         * The run method
         */
        @Override
        public void run() {
            try {
                while (!ClientModel.this.doneStreaming) {
                	byte[] compressedBytes;
            		Color color;
            		int r = 0;
            		int g = 0;
            		int b = 0;
            		BufferedImage image = ClientModel.this.imageQueue.take();
            		
        			int[] imageColors = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        			int count = 0; 
        			int x = 0;
        			int y = 0;
        			int average = 0;
        			int img[][] = new int[image.getHeight()][image.getWidth()];

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
        				
        				x++;
        				count++;
        			}
        			
        			ImageCompression imgCompression = new ImageCompression(img,ClientModel.this.compression);
        			compressedBytes = imgCompression.getCompressedImage();
        			sendPicture(compressedBytes);
                }
                
           } catch (Exception e) {
                e.printStackTrace();
           }
                
       }
    }
    
    /**
     * A callback method when a connection received an image
     * 
     * @param compressedImage: byte[] - Received image
     */
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
		this.clientView.displayImage(reconstructed);
		numPictures++;
	}
	
	public void setView(JFrame clientView) {
		this.clientView=(DisplayView) clientView;
	}
	
	public void getPicture(Webcam webcam) {
		this.webcam = webcam;
		this.takePictureThread = new Thread(new TakePictureThread());
		this.clientProcessPictureThread = new Thread(new ClientProcessPictureThread());
		this.takePictureThread.start();
		this.clientProcessPictureThread.start();
	}
	
}
