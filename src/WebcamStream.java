import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

public class WebcamStream {
	public static void main(String[] args) {
		
		Webcam webcam = Webcam.getDefault();
		webcam.setViewSize(WebcamResolution.VGA.getSize());
		MenuView menuView = new MenuView();
		DisplayView displayView = new DisplayView(webcam);
		WebcamModel webcamModel = new WebcamModel();
		webcamModel.setView(displayView);
		WebcamController controller = new WebcamController(menuView, displayView, webcamModel, webcam);				
	}
}
