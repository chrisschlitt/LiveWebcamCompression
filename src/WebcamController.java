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
	private ServerView serverView;
	private ClientView clientView;
	private Webcam webcam;
	private ServerModel serverModel;
	private ClientModel clientModel;
	
	public WebcamController (MenuView menu, ServerView serverView, ClientView clientView, ServerModel serverModel, ClientModel clientModel, Webcam webcam) {
		
		this.webcam = webcam;
		/*this.webcam = Webcam.getDefault();
		this.webcam.setViewSize(WebcamResolution.VGA.getSize());*/
		this.menu = menu;
		this.serverView = serverView;
		this.clientView = clientView;
		this.serverModel = serverModel;
		this.clientModel = clientModel;
		
		menu.addClientListener(new ClientSelect());
		menu.addServerListener(new ServerSelect());
		menu.setVisible(true);
		
		
		
	}	

		
	class CompressionSelect implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			JRadioButton actedOn = (JRadioButton) ev.getSource();
			String compressionString = actedOn.getText();
			if (compressionString.equals("None")) {
				WebcamController.this.serverModel.setCompression(1);
			} else if (compressionString.equals("1/2")) {
				WebcamController.this.serverModel.setCompression(2);
			} else if (compressionString.equals("1/4")) {
				WebcamController.this.serverModel.setCompression(2);
			}
		}
	}
	
	
	class ServerSelect implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			serverView.setVisible(true);
			menu.setVisible(false);
			serverView.addSelectionListener(new CompressionSelect());
			serverModel.setupConnection();
			serverModel.getPicture(webcam);
			
		}
	}
	
	class ClientSelect implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			clientView.setVisible(true);
			menu.setVisible(false);
			
			try {
				clientModel.setupConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}	
	
	
	
}

