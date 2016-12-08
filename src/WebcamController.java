import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;

public class WebcamController {
	
	private MenuView menu;
	private DisplayView displayView;
	private Webcam webcam;
	private WebcamModel webcamModel;
	
	public WebcamController (MenuView menu, DisplayView displayView, WebcamModel webcamModel, Webcam webcam) {
		
		this.webcam = webcam;
		this.menu = menu;
		this.displayView = displayView;
		this.webcamModel = webcamModel;
		
		menu.addListener(new JoinAction());
		menu.setVisible(true);
		
	}	

	class CompressionSelect implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			JRadioButton actedOn = (JRadioButton) ev.getSource();
			String compressionString = actedOn.getText();
			if (compressionString.equals("None")) {
				WebcamController.this.webcamModel.setCompression(1);
			} else if (compressionString.equals("1/2")) {
				WebcamController.this.webcamModel.setCompression(2);
			} else if (compressionString.equals("1/4")) {
				WebcamController.this.webcamModel.setCompression(2);
			}
		}
	}
	
	class CloseAction implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			webcamModel.closeConnection();
		}
	}
	

	class JoinAction implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			displayView.setVisible(true);
			menu.setVisible(false);
			displayView.addSelectionListener(new CompressionSelect());
			displayView.addCloseListener(new CloseAction());
			webcamModel.setupConnection();
			webcamModel.getPicture(webcam);
			
		}
	}

}

