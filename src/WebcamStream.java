/**
 * The WebcamStream class is the driver for the application.
 * It initializes the views, controller and model for the app
 * as well as the local webcam
 */
import java.awt.Color;
import javax.swing.JFrame;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

public class WebcamStream {
	public static void main(String[] args) {
		/**
		 * Gets the local webcam
		 */
		Webcam webcam = Webcam.getDefault();
		webcam.setViewSize(WebcamResolution.VGA.getSize());
		MenuView menuView = new MenuView();
		menuView.getContentPane().setBackground(Color.BLACK);
		menuView.setExtendedState(JFrame.MAXIMIZED_BOTH);
		menuView.setUndecorated(true);
		DisplayView displayView = new DisplayView(webcam);
		displayView.setExtendedState(JFrame.MAXIMIZED_BOTH);
		WebcamModel webcamModel = new WebcamModel();
		webcamModel.setView(displayView);
		WaitView waitView = new WaitView();
		/**
		 * Creates a controller with the views, model and webcam
		 */
		WebcamController controller = new WebcamController(menuView, displayView, waitView, webcamModel, webcam);	
	}
}
