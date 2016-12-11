/**
 * The MenuView is the initial menu interface for the application.
 * It simply includes a 'join' button that allows the user to connect
 * to a web chat
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class MenuView extends JFrame {
	
	JButton joinButton = new JButton("");
	JLabel welcome;
	
	public MenuView() {
		JPanel mainMenu = new JPanel(new GridLayout(0,1));
		JPanel top = new JPanel(new GridLayout(0,1));
		this.welcome = new JLabel("Penn Skype");
		this.welcome.setHorizontalAlignment(JLabel.CENTER);
		this.welcome.setVerticalAlignment(JLabel.CENTER);
		Font font = new Font("Arial", Font.BOLD,36);
		this.welcome.setFont(font);
		this.welcome.setForeground(Color.WHITE);
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
		 * Set the initial menu to take up the entire screen
		 */
		this.setSize((int)tk.getScreenSize().getWidth(),(int)tk.getScreenSize().getHeight());
	}
	
	/**
	 * Adds an actionlistener to the join button to dictate functionality of the button
	 * @param clientListener
	 */
	public void addListener(ActionListener clientListener) {
		joinButton.addActionListener(clientListener);
	}
	
}
