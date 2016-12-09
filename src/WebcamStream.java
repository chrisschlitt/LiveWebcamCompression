import java.awt.Color;

import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

public class WebcamStream {
	public static void main(String[] args) {
		
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
		WebcamController controller = new WebcamController(menuView, displayView, webcamModel, webcam);	
	}
}
