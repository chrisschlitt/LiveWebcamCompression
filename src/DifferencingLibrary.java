import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class DifferencingLibrary {
	
	public static Diff getDiff(byte[] a, byte[] b){
		LinkedList<Integer> tmpIndex = new LinkedList<Integer>();
		LinkedList<Byte> tmpValue = new LinkedList<Byte>();
		
		int i = 0;
		int j = 0;
		int smallerLength = Math.min(a.length, b.length);
		int smaller = 1;
		int length = b.length;
		if(a.length < b.length){
			smaller = 0;
		}
		
		while(j < (smallerLength)){
			if(a[i] == b[j]){
				i++;
				j++;
				continue;
			} else {
				tmpIndex.add(j);
				tmpValue.add(b[j]);
				i++;
				j++;
			}
		}
		if(smaller == 0){
			while(j < length){
				tmpIndex.add(j);
				tmpValue.add(b[j]);
				j++;
			}
		}
		
		Iterator<Integer> itrIndex = tmpIndex.iterator();
		Iterator<Byte> itrValue = tmpValue.iterator();
		Integer[] diffIndex = new Integer[tmpIndex.size()];
		Byte[] diffValue = new Byte[tmpIndex.size()];
		int l = 0;
		while(itrIndex.hasNext()){
			diffIndex[l] = itrIndex.next();
			diffValue[l] = itrValue.next();
			l++;
		}
		
		Diff result = new Diff(diffIndex, diffValue, length);
		return result;
		
	}
	
	public static byte[] rebuild(Diff diff, byte[] a){
		byte[] b = new byte[diff.length];
		int i = 0;
		int diffIndexValue = 0;
		while(i < diff.length){
			if(diff.diffIndex[diffIndexValue] == i){
				b[i] = diff.diffValue[diffIndexValue];
				diffIndexValue++;
			} else {
				b[i] = a[i];
			}
			i++;
		}
		
		return b;
	}
	
	public static byte[] compress(byte[] data) throws IOException {  
		   Deflater deflater = new Deflater();  
		   deflater.setInput(data);  
		   ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);   
		   deflater.finish();  
		   byte[] buffer = new byte[1024];   
		   while (!deflater.finished()) {  
		    int count = deflater.deflate(buffer); // returns the generated code... index  
		    outputStream.write(buffer, 0, count);   
		   }  
		   outputStream.close();  
		   byte[] output = outputStream.toByteArray();  
		   // System.out.println("Original: " + data.length + " bytes");  
		   // System.out.println("Compressed: " + output.length + " bytes");  
		   return output;  
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
		
		byte[] a = "CHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRISCHRIS!".getBytes();
		
		byte[] b = new byte[600000];
		for(int i = 0; i<600000; i++){
			b[i] = ((char)i + "").getBytes()[0];
		}
		
		/*
		byte[] b = "CHRIS?".getBytes();

		Diff diff = DifferencingLibrary.getDiff(a, b);
		
		byte[] c = DifferencingLibrary.rebuild(diff, a);
		boolean same = Arrays.equals(b, c);
		System.out.println("The result is rebuilt successfully: " + same);
		*/
		DifferencingLibrary.compress(b);
	}

}
