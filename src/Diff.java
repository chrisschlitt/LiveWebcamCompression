import java.io.Serializable;

public class Diff implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9083838097019599265L;
	byte[] diffImage;
	Integer length;
	
	public Diff(byte[] diffImage, Integer length){
		this.diffImage = diffImage;
		this.length = length;
	}
}