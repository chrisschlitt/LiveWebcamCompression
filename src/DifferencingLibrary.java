import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * A differencing library for byte arrays
 * Specifically designed for use on photos
 * in video streaming
 * 
 * @author christopherschlitt
 *
 */
public class DifferencingLibrary {
	
	/**
	 * A method to generate a diff from two byte arrays
	 * 
	 * @param a: byte[] - the first byte array
	 * @param b: byte[] - the second byte array
	 * @return diff: Diff - the resulting diff
	 */
	public static Diff getDiff(byte[] a, byte[] b){
		// Set the length and counters
		int i = 0;
		int smallerLength = Math.min(a.length, b.length);
		int smaller = 1;
		int length = b.length;
		
		// Compare the lengths
		if(a.length < b.length){
			smaller = 0;
		}
		// Generate the difference array
		byte[] diffImage = new byte[length];
		// Loop through each byte
		while(i < (smallerLength)){
			// Get the byte difference
			diffImage[i] = (byte) (b[i] - a[i]);
			i++;
		}
		// If the newer array is larger, enter remaining bytes
		if(smaller == 0){
			while(i < length){
				diffImage[i] = b[i];
				i++;
			}
		}

		// Compress the difference
		try {
			diffImage = DifferencingLibrary.compress(diffImage);
		} catch (IOException e) {
			// Don't print common errors
		}
		
		// Build and return the diff object
		Diff result = new Diff(diffImage, length);
		return result;
		
	}
	
	/**
	 * A method to patch a byte array to create the next image
	 * 
	 * @param diff: Diff - The diff patch
	 * @param a: byte[] - The byte array to be patched
	 * @return b: byte[] - The new byte array
	 */
	public static byte[] rebuild(Diff diff, byte[] a){
		
		// Extract the compressed difference byte array
		byte[] diffImage = diff.diffImage;
		
		// Decompress the difference
		try {
			 diffImage = DifferencingLibrary.decompress(diff.diffImage);
		} catch (Exception e) {
			// Don't print common errors
		}
		
		// Create the resulting byte array
		byte[] b = new byte[diff.length];
		int i = 0;
		// Build the byte array by applying the difference 
		while(i < diff.length){
			b[i] = (byte)(a[i] + diffImage[i]);
			i++;
		}
		
		// Return the new byte array
		return b;
	}
	
	/**
	 * A method to compress a byte array
	 * 
	 * @param data: byte[] - byte array to be compressed
	 * @return compressedData: byte[] - Compressed byte data
	 * 
	 */
	public static byte[] compress(byte[] data) throws IOException { 
	// Create a new compressor with the best speed level
	Deflater compressor = new Deflater();
	compressor.setLevel(Deflater.BEST_SPEED);
	
	// Load the data
	compressor.setInput(data);
	compressor.finish();
	
	// Create the byte output stream
	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(data.length);
	
	// Compress the data
	byte[] bufer = new byte[1024];
	while (!compressor.finished()) {
		int count = compressor.deflate(bufer);
		byteArrayOutputStream.write(bufer, 0, count);
	}
	// Close the stream
	try {
		byteArrayOutputStream.close();
	} catch (IOException e) {
		// Don't print common errors
	}
	
	// Get the compressed data
	byte[] compressedData = byteArrayOutputStream.toByteArray();
	return compressedData;
} 

	/**
	 * A method to decompress a byte array
	 * 
	 * @param data: byte[] - compressed byte array
	 * @return decompressedData: byte[] - decompressed byte array
	 * 
	 */
	public static byte[] decompress(byte[] data) throws IOException, DataFormatException {
		// Create a new decompressor
		Inflater inflater = new Inflater();
		
		// Load the data
		inflater.setInput(data);
		
		// Create the byte array output stream
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
		
		// Decompress the data
		byte[] buffer = new byte[1024];
		while (!inflater.finished()) {
			int count = inflater.inflate(buffer);
			outputStream.write(buffer, 0, count);
		}
		
		// Close the output stream
		outputStream.close();
		
		// Get the decompressed data
		byte[] output = outputStream.toByteArray();
		return output;
	}  

	public static void main(String[] args) throws IOException {
		// Compression and differencing tests
		
		byte[] a = "CHRISCHRISC8RISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCLRISCHRISCERISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRIS!".getBytes();
		byte[] b = "CHRISCHRISCHRISCHRISCHRISC$RISCHRISCHRISCHRITCHRISCHRISCHRISCHRLSCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHPISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRIS?".getBytes();
		
		long start = System.currentTimeMillis();
		Diff diff = DifferencingLibrary.getDiff(a, b);
		System.out.println("Time to compress: " + (System.currentTimeMillis() - start));
		
		start = System.currentTimeMillis();
		byte[] c = DifferencingLibrary.rebuild(diff, a);
		System.out.println("Time to decompress: " + (System.currentTimeMillis() - start));
		
		boolean same = Arrays.equals(b, c);
		System.out.println("The result is rebuilt successfully: " + same);

		System.out.println("Original: " + b.length);
		System.out.println("Compressed: " + diff.diffImage.length);
		
	}

}
