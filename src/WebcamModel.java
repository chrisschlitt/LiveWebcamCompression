import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;

public class WebcamModel implements Model {
	private static int numPictures=1;
	public boolean doneStreaming = false;
	private int compression = 2;
	private int color = 0;
	private Webcam webcam;
	private Thread takePictureThread;
	private Thread serverProcessPictureThread;
	private Thread clientProcessPictureThread;
	private DisplayView serverView;
	
	private BlockingQueue<BufferedImage> imageQueue = new LinkedBlockingQueue<BufferedImage>();
    // The connection to the server
    public Connection connection; 
    
    /**
     * A method to setup the client server
     */
    public void setupConnection() {
    	
    	connection = new Connection(8888, 4555, 6987, this);
        connection.connect();
    }

    
    public void closeConnection() {
    	// this.doneStreaming();
    	if (connection!=null) {
    		connection.close();
    	}
    }
    
    public void setCompression(int compression) {
    	this.compression = compression;
    }
    
    public void setColor(int color) {
    	this.color = color;
    }
    
    public void doneStreaming(){
    	this.doneStreaming = true;
    	webcam.close();
    	this.serverProcessPictureThread.interrupt();
    	this.clientProcessPictureThread.interrupt();
    	this.takePictureThread.interrupt();
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
                 	/*
                 	try {
                 	    Thread.sleep(33);
                 	} catch(InterruptedException ex) {
                 	    Thread.currentThread().interrupt();
                 	}
                 	*/
                 }
                 
             } catch (Exception e) {
                 // e.printStackTrace();
             }
    	}
    }
    
    
    public class ServerProcessPictureThread implements Runnable {
        /**
         * The run method
         */
        @Override
        public void run() {
        	try{
        		while(!WebcamModel.this.connection.continueStreaming){
        			Thread.sleep(100);
        		}
        	} catch(Exception e){
        		
        	}
            try {
                while (!WebcamModel.this.doneStreaming) {
                	byte[] compressedBytes;
            		Color color;
            		int r = 0;
            		int g = 0;
            		int b = 0;
            		BufferedImage image = WebcamModel.this.imageQueue.take();
            		RGBCompression rgbCompression = new RGBCompression(image, WebcamModel.this.compression, WebcamModel.this.color);
            		compressedBytes = rgbCompression.getCompressedImage();
        			sendPicture(compressedBytes);
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
            int n = 0;
                while (!WebcamModel.this.doneStreaming) {
                	try {
                		// System.out.println("                           Attempting: " + n);
                		byte[] inbox = new byte[1024];
                		try{
                			inbox = (byte[])WebcamModel.this.connection.getInbox();
                		} catch(Exception e){
                			e.getStackTrace();
                		}
                		
                		// System.out.println("Inbox Size: " + inbox.length);
	                	RGBReconstruction rgbReconstruction = new RGBReconstruction(inbox);
	                	// System.out.println("                           Initing: " + n);
	                	BufferedImage reconstructed = rgbReconstruction.getReconstructedImage();
	                	// System.out.println("                           Printing: " + n);
	                	n++;
	                	
	                	
	                	
	                	// ImageIO.write(reconstructed, "PNG", new File("reconstructed"+numPictures+".png"));
	                	WebcamModel.this.serverView.displayImage(reconstructed);
	            		numPictures++;
                	} catch (Exception e) {
                        // e.printStackTrace();
                    }
                }
                
           
                
       }
    }
	
    public void setView(JFrame serverView) {
		this.serverView=(DisplayView) serverView;
	}
    
	public void getPicture(Webcam webcam) {
		this.webcam = webcam;
		this.takePictureThread = new Thread(new TakePictureThread());
		this.serverProcessPictureThread = new Thread(new ServerProcessPictureThread());
		this.clientProcessPictureThread = new Thread(new ClientProcessPictureThread());
		this.takePictureThread.start();
		this.serverProcessPictureThread.start();
		this.clientProcessPictureThread.start();
	}
	
}
