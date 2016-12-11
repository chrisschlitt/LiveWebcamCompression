/**
 * The Model interface dictates what methods will be available within
 * the application's model. It includes methods to manage the connection
 * between computers, to display the webchat and to close the webchat
 */

import javax.swing.JFrame;
import com.github.sarxos.webcam.Webcam;

public interface Model {
	
	/** 
	 * Sets up the connection between users
	 * @throws Exception
	 */
	public void setupConnection() throws Exception;
	
	/**
	 * Closes the connection between users
	 */
	public void closeConnection();
	
	/**
	 * Sends a byte array from one user to another
	 * @param compressedImage
	 * @throws Exception
	 */
	public void sendPicture(byte[] compressedImage) throws Exception;
	
	/**
	 * Sets the view associated with the model for displaying images
	 * @param view
	 */
	public void setView(JFrame view);
	
	/**
	 * Handles the processing of incoming and outgoing images
	 * @param webcam
	 */
	public void getPicture(Webcam webcam);
	
	/**
	 * Closes the webchat between users
	 */
	public void doneStreaming();
	
}
