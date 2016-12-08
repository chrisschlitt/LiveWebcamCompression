import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;

public class DifferencingLibrary {
	
	public static Diff getDiff(byte[] a, byte[] b){
		long startTime = System.currentTimeMillis();
		HashMap<Integer, Byte> diff = new HashMap<Integer, Byte>();
		int i = 0;
		int j = 0;
		int smallerLength = Math.min(a.length, b.length);
		int smaller = 1;
		int length = b.length;
		if(a.length < b.length){
			smaller = 0;
		}
		// System.out.println("a.length = " + a.length);
		// System.out.println("b.length = " + b.length);
		while(j < (smallerLength)){
			// System.out.println("Testing i = " + i);
			// System.out.println("Testing j = " + j);
			if(a[i] == b[j]){
				i++;
				j++;
				continue;
			} else {
				// System.out.println("Found a difference at " + j + ", used to be: " + a[i] + " but now is " + b[j]);
				diff.put(j, b[j]);
				i++;
				j++;
			}
		}
		if(smaller == 0){
			while(j < length){
				diff.put(j, b[j]);
				j++;
			}
		}
		Diff result = new Diff(diff, length);
		System.out.println("Time to build diff: " + (System.currentTimeMillis() - startTime));
		return result;
		
	}
	
	public static byte[] rebuild(Diff diff, byte[] a){
		long startTime = System.currentTimeMillis();
		byte[] b = new byte[diff.length];
		int i = 0;
		// int smallerLength = Math.min(a, b);
		while(i < diff.length){
			if(diff.diff.containsKey(i)){
				b[i] = diff.diff.get(i);
			} else {
				b[i] = a[i];
			}
			i++;
		}
		System.out.println("Time to rebuild byte array: " + (System.currentTimeMillis() - startTime));
		
		return b;
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		byte[] a = "CHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREAT!".getBytes();
		byte[] b = "CHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREATCHRISISGREAT?".getBytes();
		System.out.println("Getting difference");
		Diff diff = DifferencingLibrary.getDiff(a, b);
		System.out.println("Rebuilding");
		
		byte[] c = DifferencingLibrary.rebuild(diff, a);
		boolean same = Arrays.equals(b, c);
		int n = 0;
		while(n < b.length){
			if(b[n] != c[n]){
				System.out.println(b[n] + " != " + c[n] + " at index: " + n);
			}
			n++;
		}
		System.out.println("The result is rebuilt successfully: " + same);
		System.out.println("Computed length: " + c.length);
		System.out.println("Actual length: " + b.length);
		
		
		System.out.println("Index Size: " + diff.diff.size());
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        ObjectOutputStream oos=new ObjectOutputStream(baos);
        oos.writeObject(diff.diff);
        oos.close();
        System.out.println("Data Size: " + baos.size());
        System.out.println("As opposed to: " + b.length);
		
		
	}

}
