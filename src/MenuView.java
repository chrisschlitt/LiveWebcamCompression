import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;


public class MenuView extends JFrame {
	
	JButton serverButton = new JButton("Server");
	JButton clientButton = new JButton("Client");
	
	public MenuView() {
		JPanel mainMenu = new JPanel(new GridLayout(0,1));
		JPanel top = new JPanel(new GridLayout(0,1));
		JLabel welcome = new JLabel("Welcome to the Webcam App!", SwingConstants.CENTER);
		JLabel details = new JLabel("Choose server to broadcast or client to receive video", SwingConstants.CENTER);
		
		Font font = new Font("Courier", Font.BOLD,36);
		welcome.setFont(font);
		font = new Font("Courier", Font.PLAIN, 20);
		details.setFont(font);
		top.add(welcome, BorderLayout.NORTH);
		top.add(details, BorderLayout.SOUTH);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(serverButton, BorderLayout.EAST);
		buttonPanel.add(clientButton, BorderLayout.WEST);
		mainMenu.add(top, BorderLayout.NORTH);
		mainMenu.add(buttonPanel, BorderLayout.SOUTH);
		this.add(mainMenu, BorderLayout.CENTER);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(640, 557);
	}
	
	public void addClientListener(ActionListener clientListener) {
		clientButton.addActionListener(clientListener);
	}
	
	public void addServerListener(ActionListener serverListener) {
		serverButton.addActionListener(serverListener);
	}
}
