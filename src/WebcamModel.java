/**
 * The WebcamModel handles image capture, compression and transmission
 * as well as the receiving of compressed images and their reconstruction 
 * and display
 */
import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.JFrame;
import com.github.sarxos.webcam.Webcam;

public class WebcamModel implements Model {
	public boolean doneStreaming = false;
	private int compression = 2;
	private int color = 0;
	private int previousCompression = 2;
	private int previousColor = 0;
	private Webcam webcam;
	private Thread takePictureThread;
	private Thread serverProcessPictureThread;
	private Thread clientProcessPictureThread;
	private DisplayView serverView;
	
	/**
	 * A queue is used to store the BufferedImages taken by the webcam before
	 * they are compressed and sent
	 */
	private BlockingQueue<BufferedImage> imageQueue = new LinkedBlockingQueue<BufferedImage>();
    
	/**
	 * The connection object handles connections between users
	 */
    public Connection connection; 
    
    /**
     * A method to setup the connection between users
     */
    public void setupConnection() {
    	connection = new Connection(8888, 4555, 6987, this);
        connection.connect();
    }

    /**
     * Closes the connection between users
     */
    public void closeConnection() {
    	if (connection!=null) {
    		connection.close();
    	}
    }
    
    /**
     * Sets the value representing the level of compression to be used
     * when sending images. 1 represents no compression, 2 represents
     * half compression and 4 represents quarter compression
     * @param compression
     */
    public void setCompression(int compression) {
    	this.compression = compression;
    }
    
    /**
     * Sets the value representing whether images will be sent in color
     * or black and white. 0 represents color, 1 represents black and white
     * @param color
     */
    public void setColor(int color) {
    	this.color = color;
    }
    
    /**
     * Closes the connection between users when the user
     * is done streaming. Stops the processing of incoming
     * and outgoing pictures. 
     */
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
    
    /**
     * The TakePictureThread class represents a thread used
     * to take pictures on the local webcam and put them in
     * the queue for processing.
     */
    public class TakePictureThread implements Runnable {
    	private Webcam webcam = WebcamModel.this.webcam;
    	/**
    	 * While the user is streaming, the run method of the thread
    	 * gets an image from the webcam and puts it in the imageQueue
    	 * so that it can be compressed and sent.
    	 */
    	public void run() {
    		 try {
                 while (!WebcamModel.this.doneStreaming) {
                 	BufferedImage image = webcam.getImage();
                 	
                 	/**
                 	 * Limits the size of the imageQueue
                 	 * so that images don't pile up and 
                 	 * the video displays at a proper speed
                 	 */
                 	if (imageQueue.size()>5){
                 		imageQueue.take();
                 	}
                 	imageQueue.put(image);
                 }
             } catch (Exception e) {
             }
    	}
    }
    
    /**
     * The ServerProcessPicture thread represents a thread used to 
     * take images off the imageQueue and compress and send them
     * to the connected computer
     */
    public class ServerProcessPictureThread implements Runnable {
        @Override
        public void run() {
        	try{
        		while(!WebcamModel.this.connection.continueStreaming){
        			Thread.sleep(100);
        		}
        	} catch(Exception e){
        	}
            try {
            	/**
            	 * While the user is still streaming, take an image off the imageQueue,
            	 * pass it to an RGBCompression object for compression using the specified
            	 * compression level and color, then sends the byte array representing the 
            	 * compressed image to the connected computer
            	 */
                while (!WebcamModel.this.doneStreaming) {
                	byte[] compressedBytes;
            		BufferedImage image = WebcamModel.this.imageQueue.take();
            		if((WebcamModel.this.previousCompression != WebcamModel.this.compression) || (WebcamModel.this.previousColor != WebcamModel.this.color)){
            			WebcamModel.this.previousCompression = WebcamModel.this.compression;
            			WebcamModel.this.previousCompression = WebcamModel.this.compression;
            			WebcamModel.this.connection.resetPreviousSentCounter();
            		}
            		RGBCompression rgbCompression = new RGBCompression(image, WebcamModel.this.compression, WebcamModel.this.color);
            		compressedBytes = rgbCompression.getCompressedImage();
        			sendPicture(compressedBytes);
                }
                
           } catch (Exception e) {
                e.printStackTrace();
           }     
       }
    }
    
    /**
     * The ClientProcessPictureThread represents a thread to handle the
     * receiving of incoming images.
     */
    public class ClientProcessPictureThread implements Runnable {
        /**
         * The run method gets a byte array representing the incoming image
         * from the connection object and passes it into an RGBReconstruction
         * object to reconstruct the image. It then passes the reconstructed image
         * to the view to be displayed to the user
         */
        @Override
        public void run() {
            while (!WebcamModel.this.doneStreaming) {
            	try {
            		byte[] inbox = new byte[1024];
            		try{
            			inbox = (byte[])WebcamModel.this.connection.getInbox();
            		} catch(Exception e){
            			e.getStackTrace();
            		}
                	RGBReconstruction rgbReconstruction = new RGBReconstruction(inbox);
                	BufferedImage reconstructed = rgbReconstruction.getReconstructedImage();
                	WebcamModel.this.serverView.displayImage(reconstructed);
            	} catch (Exception e) {

                }
            }    
       }
    }
	
    /**
     * Sets the associated view so images can be displayed
     */
    public void setView(JFrame serverView) {
		this.serverView=(DisplayView) serverView;
	}
    
    /**
     * Starts the various threads necessary for handling taking images, compressing
     * and sending them as well as receiving images, reconstructing and displaying them
     */
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
