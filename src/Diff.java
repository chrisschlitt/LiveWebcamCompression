import java.io.Serializable;

public class Diff {
	byte[] diffImage;
	Integer length;
	
	public Diff(byte[] diffImage, Integer length){
		this.diffImage = diffImage;
		this.length = length;
	}
}