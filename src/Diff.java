import java.io.Serializable;

public class Diff implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8612089235662456771L;
	// HashMap<Integer, Byte> diff;
	Integer[] diffIndex;
	Byte[] diffValue;
	Integer length;
	
	public Diff(Integer[] diffIndex, Byte[] diffValue, Integer length){
		this.diffIndex = diffIndex;
		this.diffValue = diffValue;
		this.length = length;
	}
}