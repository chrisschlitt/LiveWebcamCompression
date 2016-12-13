/**
 * The WaitView class represents the menu to be displayed
 * while the system is loading the connection between users
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class WaitView extends JFrame {
	JButton joinButton = new JButton("");
	
	public WaitView() {
		
		JPanel mainMenu = new JPanel(new GridLayout(0,1));
		JPanel top = new JPanel(new GridLayout(0,1));
		JLabel welcome = new JLabel("CONNECTING...");
		welcome.setHorizontalAlignment(JLabel.CENTER);
		welcome.setVerticalAlignment(JLabel.CENTER);
		Font font = new Font("Arial", Font.BOLD,36);
		welcome.setFont(font);
		welcome.setForeground(Color.WHITE);
		font = new Font("Courier", Font.PLAIN, 20);
		top.add(welcome, BorderLayout.NORTH);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setOpaque(false);
		ImageIcon imgico = new ImageIcon(getClass().getResource("icon2.png"));

		joinButton.setIcon(imgico);
		joinButton.setBorderPainted(false);
		joinButton.setFocusPainted(false);
		joinButton.setContentAreaFilled(false);
		buttonPanel.add(joinButton);
		top.setOpaque(false);
		mainMenu.add(top, BorderLayout.NORTH);
		mainMenu.add(top, BorderLayout.NORTH);
		mainMenu.add(buttonPanel, BorderLayout.SOUTH);
		mainMenu.setOpaque(false);
		this.add(mainMenu, BorderLayout.CENTER);
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Toolkit tk = Toolkit.getDefaultToolkit();
		/**
		 * Sets the menu to take up the full screen
		 */
		this.setSize((int)tk.getScreenSize().getWidth(),(int)tk.getScreenSize().getHeight());	
	}
}
