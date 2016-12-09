import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;

public class DifferencingLibrary {
	
	public static Diff getDiff(byte[] a, byte[] b){
		
		int i = 0;
		int smallerLength = Math.min(a.length, b.length);
		int smaller = 1;
		int length = b.length;
		

		if(a.length < b.length){
			smaller = 0;
		}
		byte[] diffImage = new byte[length];
		while(i < (smallerLength)){
			diffImage[i] = (byte) (b[i] - a[i]);
			i++;
		}
		if(smaller == 0){
			while(i < length){
				diffImage[i] = b[i];
				i++;
			}
		}

		try {
			diffImage = DifferencingLibrary.compress(diffImage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		Diff result = new Diff(diffImage, length);
		return result;
		
	}
	
	public static byte[] rebuild(Diff diff, byte[] a){
		
		byte[] diffImage = diff.diffImage;
		
		try {
			 diffImage = DifferencingLibrary.decompress(diff.diffImage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// byte[] diffImage = diff.diffImage;
		
		
		byte[] b = new byte[diff.length];
		int i = 0;
		while(i < diff.length){
			b[i] = (byte)(a[i] + diffImage[i]);
			i++;
		}
		
		return b;
	}
	
	public static byte[] compress(byte[] data) throws IOException { 
		

		   
		   // Compressor with highest level of compression
		   Deflater compressor = new Deflater();
		   compressor.setLevel(Deflater.BEST_SPEED);
		    
		   // Give the compressor the data to compress
		   compressor.setInput(data);
		   compressor.finish();
		    

		   ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(data.length);
		    
		   // Compress the data
		   byte[] buf = new byte[1024];
		   while (!compressor.finished()) {
		       int count = compressor.deflate(buf);
		       byteArrayOutputStream.write(buf, 0, count);
		   }
		   try {
			   byteArrayOutputStream.close();
		   } catch (IOException e) {
		   }
		    
		   // Get the compressed data
		   byte[] compressedData = byteArrayOutputStream.toByteArray();
		   

		   return compressedData;  
	} 
	
	public static byte[] decompress(byte[] data) throws IOException, DataFormatException {  
		   Inflater inflater = new Inflater();   
		   inflater.setInput(data);  
		   ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);  
		   byte[] buffer = new byte[1024];  
		   while (!inflater.finished()) {  
		    int count = inflater.inflate(buffer);  
		    outputStream.write(buffer, 0, count);  
		   }  
		   outputStream.close();  
		   byte[] output = outputStream.toByteArray();  
		   return output;  
	}  

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		// byte[] a = "CHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRIS!".getBytes();
		
		// byte[] b = "CHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRIS?".getBytes();
		
		
		Path path = Paths.get("test1.txt");
		byte[] a = Files.readAllBytes(path);
		path = Paths.get("test.txt");
		byte[] b = Files.readAllBytes(path);
		
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
