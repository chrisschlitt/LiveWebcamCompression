import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;

public class WebcamModel implements Model {
	private static int numPictures=1;
	public boolean doneStreaming = false;
	private int compression = 2;
	private Webcam webcam;
	private Thread takePictureThread;
	private Thread serverProcessPictureThread;
	private DisplayView serverView;
	
	private BlockingQueue<BufferedImage> imageQueue = new LinkedBlockingQueue<BufferedImage>();
    // The connection to the server
    private Connection connection; 
    
    /**
     * A method to setup the client server
     */
    public void setupConnection() {
    	
    	connection = new Connection(8888, 4555, 6987, this);
        connection.connect();
    }
    
    public void closeConnection() {
    	this.doneStreaming();
    	if (connection!=null) {
    		connection.close();
    	}
    }
    
    public void setCompression(int compression) {
    	this.compression = compression;
    }
    
    public void doneStreaming(){
    	Webcam.shutdown();
    	webcam.close();
    	this.doneStreaming = true;
    }
    
    /**
     * A wrapper method to stream a picture
     * @param compressedImage: byte[] - Image to send
     */
    public void sendPicture(byte[] compressedImage) throws Exception {
        connection.sendStreamData(compressedImage);
    }
    
    public class TakePictureThread implements Runnable {
    	private Webcam webcam = WebcamModel.this.webcam;
    	public void run() {
    		 try {
                 while (!WebcamModel.this.doneStreaming) {
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
    
    
    public class ServerProcessPictureThread implements Runnable {
        /**
         * The run method
         */
        @Override
        public void run() {
            try {
                while (!WebcamModel.this.doneStreaming) {
                	byte[] compressedBytes;
            		Color color;
            		int r = 0;
            		int g = 0;
            		int b = 0;
            		BufferedImage image = WebcamModel.this.imageQueue.take();
            		
            		RGBCompression rgbCompression = new RGBCompression(image, WebcamModel.this.compression);
            		compressedBytes = rgbCompression.getCompressedImage();
        			sendPicture(compressedBytes);
                }
                
           } catch (Exception e) {
                e.printStackTrace();
           }
                
       }
    }
    
    public void receiveImage(byte[] compressedImage) throws Exception {	 	
    	RGBReconstruction rgbReconstruction = new RGBReconstruction(compressedImage);
    	BufferedImage reconstructed = rgbReconstruction.getReconstructedImage();
		this.serverView.displayImage(reconstructed);
		numPictures++;
	}
	
    public void setView(JFrame serverView) {
		this.serverView=(DisplayView) serverView;
	}
    
	public void getPicture(Webcam webcam) {
		this.webcam = webcam;
		this.takePictureThread = new Thread(new TakePictureThread());
		this.serverProcessPictureThread = new Thread(new ServerProcessPictureThread());
		this.takePictureThread.start();
		this.serverProcessPictureThread.start();
	}
	
}
