import javax.swing.JFrame;
import com.github.sarxos.webcam.Webcam;

public interface Model {
	
	public void setupConnection() throws Exception;
	
	public void closeConnection();
	
	public void sendPicture(byte[] compressedImage) throws Exception;
	
	public void receiveImage(byte[] compressedImage) throws Exception;
	
	public void setView(JFrame view);
	
	public void getPicture(Webcam webcam);
	
	public void doneStreaming();
	
}
