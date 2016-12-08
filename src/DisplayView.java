import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;

public class DisplayView extends JFrame {
	JPanel panel = new JPanel();
	JPanel videoComponents = new JPanel();
	ButtonGroup buttonGroup = new ButtonGroup();
	JRadioButton noCompressionButton = new JRadioButton("None");
	JRadioButton quarterCompressionButton = new JRadioButton("1/4");
	JRadioButton halfCompressionButton = new JRadioButton("1/2");
	JButton endButton = new JButton("End");
	JPanel endPanel = new JPanel();
	
	public DisplayView(Webcam webcam) {

		WebcamPanel webPanel = new WebcamPanel(webcam);
		webPanel.setFPSDisplayed(false);
		webPanel.setDisplayDebugInfo(false);
		webPanel.setImageSizeDisplayed(false);
		webPanel.setMirrored(true);
		videoComponents.setLayout(new GridLayout(0,2));
		videoComponents.add(webPanel);
		videoComponents.add(panel);
		
		this.setLayout(new BorderLayout());
		this.add(videoComponents, BorderLayout.CENTER);
		JLabel label = new JLabel("Receiving Video", JLabel.CENTER);
		this.add(label, BorderLayout.NORTH);
		
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());

		
		buttonGroup.add(halfCompressionButton);
		buttonGroup.add(quarterCompressionButton);
		buttonGroup.add(noCompressionButton);
		JPanel radioPanel = new JPanel();
		radioPanel.setLayout(new GridLayout(0,2));
		
		JPanel buttons = new JPanel();
		buttons.add(halfCompressionButton);
		buttons.add(quarterCompressionButton);
		buttons.add(noCompressionButton);
		halfCompressionButton.setSelected(true);
		
		radioPanel.add(buttons);
		endButton.setPreferredSize(new Dimension(100,50));
		endPanel.add(endButton);
		radioPanel.add(endPanel);
		
		
		JLabel buttonLabel = new JLabel("Compression Level", JLabel.CENTER);
		buttonPanel.add(buttonLabel, BorderLayout.NORTH);
		buttonPanel.add(radioPanel, BorderLayout.CENTER);
		
		this.add(buttonPanel, BorderLayout.SOUTH);
		
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
	}
	
	public void displayImage(BufferedImage image) {
		this.panel.removeAll();
		this.panel.add(new JLabel(new ImageIcon(image)));
		this.pack();
	}
	
	public void addCloseListener(ActionListener closeListener) {
		endButton.addActionListener(closeListener);
	}
	
	public void addSelectionListener(ActionListener selectionButtonListener) {
        
        halfCompressionButton.addActionListener(selectionButtonListener);
        noCompressionButton.addActionListener(selectionButtonListener);
        quarterCompressionButton.addActionListener(selectionButtonListener);
        
    }
}
