import java.io.Serializable;

/**
 * A class to wrap streaming data
 * 
 * @author christopherschlitt
 *
 */
public class StreamData implements Serializable {
	// The serial version uid
	private static final long serialVersionUID = -4537244685787412499L;
	// A diff or full image flag
	public Boolean isDiff;
	// The object to be streamed
	public Object data;
	
	/**
	 * Constructor
	 * 
	 * @param isDiff: boolean - Diff or full data flag
	 * @param data: Object - the data object to be streamed
	 */
	public StreamData(boolean isDiff, Object data){
		this.isDiff = isDiff;
		this.data = data;
	}
}
