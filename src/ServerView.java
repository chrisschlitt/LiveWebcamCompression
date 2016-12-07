import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;

public class ServerView extends JFrame {
	
	JRadioButton noCompressionButton = new JRadioButton("None");
	JRadioButton quarterCompressionButton = new JRadioButton("1/4");
	JRadioButton halfCompressionButton = new JRadioButton("1/2");
	
	ButtonGroup buttonGroup = new ButtonGroup();
	
	public ServerView(Webcam webcam) {
		WebcamPanel panel = new WebcamPanel(webcam);
		panel.setFPSDisplayed(false);
		panel.setDisplayDebugInfo(false);
		panel.setImageSizeDisplayed(false);
		panel.setMirrored(true);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());
		
		JLabel label = new JLabel("Sending Video", JLabel.CENTER);
		
		buttonGroup.add(halfCompressionButton);
		buttonGroup.add(quarterCompressionButton);
		buttonGroup.add(noCompressionButton);
		JPanel radioPanel = new JPanel();
		radioPanel.setLayout(new BorderLayout());
		
		JPanel buttons = new JPanel();
		buttons.add(halfCompressionButton);
		buttons.add(quarterCompressionButton);
		buttons.add(noCompressionButton);
		halfCompressionButton.setSelected(true);
		radioPanel.add(buttons);
		
		
		JLabel buttonLabel = new JLabel("Compression Level", JLabel.CENTER);
		buttonPanel.add(buttonLabel, BorderLayout.NORTH);
		buttonPanel.add(radioPanel, BorderLayout.CENTER);
		
		this.add(buttonPanel, BorderLayout.SOUTH);
		this.add(panel, BorderLayout.CENTER);
		this.add(label, BorderLayout.NORTH);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		
	}
	
	
	public void addSelectionListener(ActionListener selectionButtonListener) {
        
        halfCompressionButton.addActionListener(selectionButtonListener);
        noCompressionButton.addActionListener(selectionButtonListener);
        quarterCompressionButton.addActionListener(selectionButtonListener);
        
    }
	
	/*public String getCompressionSelection() {
		if (noCompressionButton.isSelected()) {
			return noCompressionButton.getText();
		} else if (halfCompressionButton.isSelected()) {
			return halfCompressionButton.getText();
		} else {
			return quarterCompressionButton.getText();
		}
	}*/
	
}
