import java.io.Serializable;

/**
 * A class to represent a diff in a differencing library
 * 
 * @author christopherschlitt
 *
 */
public class Diff implements Serializable {
	// The serial version uid
	private static final long serialVersionUID = -9083838097019599265L;
	// The diff byte array
	byte[] diffImage;
	// The original length of the byte array
	Integer length;
	
	/**
	 * Constructor
	 * @param diffImage: byte[] - difference byte array
	 * @param length: Integer - the orginal length of the byte array
	 */
	public Diff(byte[] diffImage, Integer length){
		this.diffImage = diffImage;
		this.length = length;
	}
}