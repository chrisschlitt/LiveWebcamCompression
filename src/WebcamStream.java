import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

public class WebcamStream {
	public static void main(String[] args) {
		
		Webcam webcam = Webcam.getDefault();
		webcam.setViewSize(WebcamResolution.VGA.getSize());
		MenuView menu = new MenuView();
		DisplayView displayView = new DisplayView(webcam);
		ServerModel serverModel = new ServerModel();
		ClientModel clientModel = new ClientModel();
		clientModel.setView(displayView);
		serverModel.setView(displayView);
		WebcamController controller = new WebcamController(menu, displayView, serverModel, clientModel, webcam);				
	}
}
