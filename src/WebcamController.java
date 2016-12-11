/**
 * The WebcamController coordinates interaction between the user interface and the 
 * underlying model. It tells the model when to initiate the webchat and passes
 * the user's various streaming choices to the model
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.github.sarxos.webcam.Webcam;

public class WebcamController {
	
	private MenuView menu;
	private DisplayView displayView;
	private Webcam webcam;
	private WebcamModel webcamModel;
	private WaitView waitView;
	
	/**
	 * The constructor for the WebcamController sets up the views and models associated with
	 * the application. It also sets up the webcam to be used by the application
	 * @param menu
	 * @param displayView
	 * @param waitView
	 * @param webcamModel
	 * @param webcam
	 */
	public WebcamController (MenuView menu, DisplayView displayView, WaitView waitView, WebcamModel webcamModel, Webcam webcam) {
		this.webcam = webcam;
		this.menu = menu;
		this.displayView = displayView;
		this.webcamModel = webcamModel;
		this.waitView = waitView;
		waitView.getContentPane().setBackground(Color.BLACK);
    	waitView.setExtendedState(JFrame.MAXIMIZED_BOTH);
    	waitView.setUndecorated(true);
    	waitView.setVisible(true);
		menu.addListener(new JoinAction());
		menu.setVisible(true);
		
	}	

	/**
	 * The CompressionSelect class is an ActionListener 
	 * that passes the user's compression choice to the model
	 */
	class CompressionSelect implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			JRadioButton actedOn = (JRadioButton) ev.getSource();
			String compressionString = actedOn.getText();
			if (compressionString.equals("None")) {
				WebcamController.this.webcamModel.setCompression(1);
			} else if (compressionString.equals("1/2")) {
				WebcamController.this.webcamModel.setCompression(2);
			} else if (compressionString.equals("1/4")) {
				WebcamController.this.webcamModel.setCompression(4);
			}
			webcamModel.connection.resetPreviousSentCounter();
		}
	}
	
	/**
	 * The ColorSelect class is an ActionListener that passes the
	 * user's choice of broadcasting in color or black and white
	 * to the model
	 */
	class ColorSelect implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			JRadioButton actedOn = (JRadioButton) ev.getSource();
			String compressionString = actedOn.getText();
			if (compressionString.equals("Color")) {
				WebcamController.this.webcamModel.setColor(0);
			} else if (compressionString.equals("B/W")) {
				WebcamController.this.webcamModel.setColor(1);
			}
			webcamModel.connection.resetPreviousSentCounter();
		}
	}
	
	/**
	 * The CloseAction class is an ActionListener that closes
	 * the model's connection
	 */
	class CloseAction implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			webcamModel.closeConnection();
		}
	}
	
	/**
	 * The JoinAction class is an ActionListener that displays
	 * the loading page and tells the model to set up the webchat
	 */
	class JoinAction implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			menu.setVisible(false);
			displayView.addSelectionListener(new CompressionSelect());
			displayView.addColorSelect(new ColorSelect());
			displayView.addCloseListener(new CloseAction());
			webcamModel.setupConnection();
			webcamModel.getPicture(webcam);
			waitView.setVisible(false);
			displayView.setVisible(true);
		}
	}
}

