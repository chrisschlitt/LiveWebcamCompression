import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

public class WebcamStream {
	public static void main(String[] args) {
		
		Webcam webcam = Webcam.getDefault();
		webcam.setViewSize(WebcamResolution.VGA.getSize());
		MenuView menu = new MenuView();
		ServerView serverView = new ServerView(webcam);
		ClientView clientView = new ClientView();
		ServerModel serverModel = new ServerModel();
		ClientModel clientModel = new ClientModel();
		clientModel.setClientView(clientView);
		WebcamController controller = new WebcamController(menu, serverView, clientView, serverModel, clientModel, webcam);
		
		
	}
}
