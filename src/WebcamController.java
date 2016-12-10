import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;

public class WebcamController {
	
	private MenuView menu;
	private DisplayView displayView;
	private Webcam webcam;
	private WebcamModel webcamModel;
	private WaitView waitView;
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
			webcamModel.connection.resetPreviousSentCounter();
		}
	}
	
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
	
	
	class CloseAction implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			webcamModel.closeConnection();
		}
	}
	
	
	
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

