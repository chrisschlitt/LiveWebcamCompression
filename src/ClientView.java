import java.awt.BorderLayout;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.github.sarxos.webcam.Webcam;

public class ClientView extends JFrame {
	JPanel panel = new JPanel();
	public ClientView() {
		JLabel label = new JLabel("Receiving Video", JLabel.CENTER);
		this.add(label, BorderLayout.NORTH);
		this.add(panel, BorderLayout.SOUTH);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
	}
	
	public void displayImage(BufferedImage image) {
		this.panel.add(new JLabel(new ImageIcon(image)));
		this.pack();
	}
}
