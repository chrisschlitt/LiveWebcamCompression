

public class Diff {
	/**
	 * 
	 */
	
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