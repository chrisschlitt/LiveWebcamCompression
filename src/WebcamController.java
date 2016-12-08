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
	private ServerModel serverModel;
	private ClientModel clientModel;
	
	public WebcamController (MenuView menu, DisplayView displayView, ServerModel serverModel, ClientModel clientModel, Webcam webcam) {
		
		this.webcam = webcam;
		this.menu = menu;
		this.displayView = displayView;
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
	
	class ClientCloseAction implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			clientModel.closeConnection();
		}
	}
	
	class ServerCloseAction implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			serverModel.closeConnection();
		}
	}
	
	class ServerSelect implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			displayView.setVisible(true);
			menu.setVisible(false);
			displayView.addSelectionListener(new CompressionSelect());
			displayView.addCloseListener(new ClientCloseAction());
			serverModel.setupConnection();
			serverModel.getPicture(webcam);
			
		}
	}
	
	class ClientSelect implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			displayView.setVisible(true);
			menu.setVisible(false);
			displayView.addSelectionListener(new CompressionSelect());
			displayView.addCloseListener(new ClientCloseAction());
			try {
				clientModel.setupConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
			clientModel.getPicture(webcam);
		}
	}	
}

