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
	
	public WebcamController (MenuView menu, ServerView serverView, ClientView clientView, ServerModel serverModel, Webcam webcam) {
		
		this.webcam = webcam;
		/*this.webcam = Webcam.getDefault();
		this.webcam.setViewSize(WebcamResolution.VGA.getSize());*/
		this.menu = menu;
		this.serverView = serverView;
		this.clientView = clientView;
		this.serverModel = serverModel;
		
		menu.addClientListener(new ClientSelect());
		menu.addServerListener(new ServerSelect());
		menu.setVisible(true);
		
		
		
	}	

		
	class CompressionSelect implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			JRadioButton actedOn = (JRadioButton) ev.getSource();
			System.out.println(actedOn.getText());
		}
	}
	
	
	class ServerSelect implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			serverView.setVisible(true);
			menu.setVisible(false);
			serverView.addSelectionListener(new CompressionSelect());
			
			serverModel.getPicture(webcam);
			
		}
	}
	
	class ClientSelect implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			clientView.setVisible(true);
			menu.setVisible(false);
		}
	}	
	
	
	
}

