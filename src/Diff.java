import java.io.Serializable;
import java.util.HashMap;

public class Diff implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8612089235662456771L;
	HashMap<Integer, Byte> diff;
	Integer length;
	
	public Diff(HashMap<Integer, Byte> diff, Integer length){
		this.diff = diff;
		this.length = length;
	}
}