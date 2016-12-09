import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;


public class MenuView extends JFrame {
	
	JButton joinButton = new JButton("");
	JLabel details = new JLabel("Choose server to broadcast or client to receive video", SwingConstants.CENTER);
	
	public MenuView() {
		JPanel mainMenu = new JPanel(new GridLayout(0,1));
		JPanel top = new JPanel(new GridLayout(0,1));
		JLabel welcome = new JLabel("Penn Skype");
		welcome.setHorizontalAlignment(JLabel.CENTER);
		welcome.setVerticalAlignment(JLabel.CENTER);
		Font font = new Font("Arial", Font.BOLD,36);
		welcome.setFont(font);
		welcome.setForeground(Color.WHITE);
		font = new Font("Courier", Font.PLAIN, 20);
		details.setFont(font);
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
		
		this.setSize((int)tk.getScreenSize().getWidth(),(int)tk.getScreenSize().getHeight());
	}
	
	public void addListener(ActionListener clientListener) {
		joinButton.addActionListener(clientListener);
	}
	
}
