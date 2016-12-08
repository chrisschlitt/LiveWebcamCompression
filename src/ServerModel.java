import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;

public class ServerModel {
	private static int numPictures=1;
	private boolean doneStreaming = false;
	private int compression = 2;
	private Webcam webcam;
	private Thread takePictureThread;
	private Thread serverProcessPictureThread;
	
	private BlockingQueue<BufferedImage> imageQueue = new LinkedBlockingQueue<BufferedImage>();
    // The connection to the server
    private Connection connection; 
    
    /**
     * A method to setup the client server
     */
    public void setupConnection() {
    	
    	connection = new Connection(8888, null);
    	
        // Begin listening for packets
        connection.beginPacketListening();
        // Wait until the client connects
        try {
            connection.discoveryThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Prepare to stream data to the client
        connection.beginStreaming();
        while(!connection.startStreaming){
            // Wait
        }
    }
    
    public void setCompression(int compression) {
    	this.compression = compression;
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
    	private Webcam webcam = ServerModel.this.webcam;
    	public void run() {
    		 try {
                 while (!ServerModel.this.doneStreaming) {
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
                while (!ServerModel.this.doneStreaming) {
                	byte[] compressedBytes;
            		Color color;
            		int r = 0;
            		int g = 0;
            		int b = 0;
            		BufferedImage image = ServerModel.this.imageQueue.take();
            		            		
                    RGBCompression rgbCompression = new RGBCompression(image, ServerModel.this.compression);
                    compressedBytes = rgbCompression.getCompressedImage();
                    
        			sendPicture(compressedBytes);
                }
                
           } catch (Exception e) {
                e.printStackTrace();
           }
                
       }
    }
	
	public void getPicture(Webcam webcam) {
		this.webcam = webcam;
		this.takePictureThread = new Thread(new TakePictureThread());
		this.serverProcessPictureThread = new Thread(new ServerProcessPictureThread());
		this.takePictureThread.start();
		this.serverProcessPictureThread.start();
	}
	
}
