import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.*;
import java.awt.Graphics2D;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;

public class WebcamPictures {
	public static void main(String[] args) {
		Webcam webcam = Webcam.getDefault();
		webcam.setViewSize(WebcamResolution.VGA.getSize());

		WebcamPanel panel = new WebcamPanel(webcam);
		panel.setFPSDisplayed(true);
		panel.setDisplayDebugInfo(true);
		panel.setImageSizeDisplayed(true);
		panel.setMirrored(true);
		
		JFrame frame = new JFrame("Test");
		JPanel buttonPanel = new JPanel();
		JButton showDialogButton = new JButton("Take Picture");
		
		class takePicture implements ActionListener {
			private int numPictures;
			takePicture() {
				numPictures = 1;
			}
			public void actionPerformed(ActionEvent e) {
				Color color;
				int r = 0;
				int g = 0;
				int b = 0;
				BufferedImage image = webcam.getImage();
				BufferedImage reconstructed = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		
				try {
					ImageIO.write(image, "PNG", new File("test"+numPictures+".png"));
					int[] imageColors = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
										
					int count = 0; 
					int x = 0;
					int y = 0;
					int average = 0;
					
					while (count < imageColors.length) {
						if (x==image.getWidth()) {
							x = 0;
							y++;
						}
						
						color = new Color(imageColors[count]);
						r = color.getRed();
						g = color.getGreen();
						b = color.getBlue();
						average = (r+g+b)/3;
						reconstructed.setRGB(x, y, new Color(average, average, average).getRGB());
						x++;
						count++;
					}
					ImageIO.write(reconstructed, "PNG", new File("reconstructed"+numPictures+".png"));
					numPictures++;
				} catch (IOException ex){
					ex.printStackTrace();
				}
			}
		}
		
		showDialogButton.addActionListener(new takePicture());
		buttonPanel.add(showDialogButton, BorderLayout.CENTER);
		frame.add(buttonPanel, BorderLayout.SOUTH);
		frame.add(panel, BorderLayout.NORTH);
		frame.setResizable(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
}
