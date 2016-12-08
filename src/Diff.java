import java.util.HashMap;

public class Diff {
	HashMap<Integer, Byte> diff;
	int length;
	
	public Diff(HashMap<Integer, Byte> diff, int length){
		this.diff = diff;
		this.length = length;
	}
}