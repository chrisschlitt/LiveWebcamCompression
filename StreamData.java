import java.io.Serializable;

public class StreamData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4537244685787412499L;
	/**
	 * 
	 */
	public Boolean isDiff;
	public Object data;
	
	public StreamData(boolean isDiff, Object data){
		this.isDiff = isDiff;
		this.data = data;
	}
}
