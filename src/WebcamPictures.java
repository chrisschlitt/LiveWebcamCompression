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

public class WebcamPictures {
	public static void main(String[] args) {
		
		Webcam webcam = Webcam.getDefault();
		webcam.setViewSize(WebcamResolution.VGA.getSize());
		JFrame frame = new JFrame("Test");
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
		
		JButton serverButton = new JButton("Server");
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(serverButton, BorderLayout.EAST);
		JButton clientButton = new JButton("Client");
		buttonPanel.add(clientButton, BorderLayout.WEST);
		mainMenu.add(top, BorderLayout.NORTH);
		mainMenu.add(buttonPanel, BorderLayout.SOUTH);
		frame.add(mainMenu, BorderLayout.CENTER);
		frame.setResizable(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//frame.pack();
		frame.setSize(640, 557);
		frame.setVisible(true);
		
		
		class TakePicture implements ActionListener {
			private int numPictures;
			
			public TakePicture() {
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
					int img[][] = new int[image.getHeight()][image.getWidth()];
					double expandedImg[][];
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
						
						// Creating average array to be passed for compression 
						img[y][x] = average;
						
						
//						reconstructed.setRGB(x, y, new Color(average, average, average).getRGB());
						x++;
						count++;
					}
					
					
					
					ImageCompression imgCompression = new ImageCompression(img);
					expandedImg = imgCompression.expandedImage;
					count = 0; 
					x = 0;
					y = 0;					
					while (count < imageColors.length) {
						if (x==image.getWidth()) {
							x = 0;
							y++;
						}
						
						average = (int)expandedImg[y][x];	
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
		
		class serverSelect implements ActionListener {
			public void actionPerformed(ActionEvent ev) {
				
				frame.remove(mainMenu);
				WebcamPanel panel = new WebcamPanel(webcam);
				panel.setFPSDisplayed(false);
				panel.setDisplayDebugInfo(false);
				panel.setImageSizeDisplayed(false);
				panel.setMirrored(true);
				
				JPanel buttonPanel = new JPanel();
				JLabel label = new JLabel("Sending Video", JLabel.CENTER);
				JButton takePictureButton = new JButton("Take Picture");
				takePictureButton.addActionListener(new TakePicture());
				buttonPanel.add(takePictureButton, BorderLayout.SOUTH);
				frame.add(buttonPanel, BorderLayout.SOUTH);
				frame.add(panel, BorderLayout.CENTER);
				frame.add(label, BorderLayout.NORTH);
				frame.setResizable(true);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.pack();
				frame.setVisible(true);
			}
		}
		
		class clientSelect implements ActionListener {
			public void actionPerformed(ActionEvent ev) {
				
				frame.remove(mainMenu);
				WebcamPanel panel = new WebcamPanel(webcam);
				panel.setFPSDisplayed(false);
				panel.setDisplayDebugInfo(false);
				panel.setImageSizeDisplayed(false);
				panel.setMirrored(true);
				
				JLabel label = new JLabel("Receiving Video", JLabel.CENTER);
				frame.add(label, BorderLayout.NORTH);
				frame.add(panel, BorderLayout.SOUTH);
				frame.setResizable(true);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.pack();
				frame.setVisible(true);
				
			}
		}
		serverButton.addActionListener(new serverSelect());
		clientButton.addActionListener(new clientSelect());
		
	}
}
