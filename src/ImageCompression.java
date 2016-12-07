import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import Jama.*; 

public class ImageCompression {
	private double[][] originalImage; 
	private double[] compressedImage;
	private byte[] compressedImageBytes;
	private double[][] expandedImage;
	private int width;
	private int height;
	/**
	 * Constructor takes the original image and compresses it
	 * @param image
	 */
	public ImageCompression(int[][] image, int ratio) {
		originalImage = new double[image.length][image[0].length];
		
		
		
		width = image[0].length;
		height = image.length;
		int totalSize = image.length*image[0].length;
		
		if (ratio != 1)
		{
			//Converting original image from int to double
			for(int i = 0; i < image.length; i++)
		    {
		        for(int j = 0; j < image[0].length; j++)
		    		originalImage[i][j] = (double) image[i][j];
			}
			
		// resize matrix into two rows
		double[][] resizeMatrix = new double[2][totalSize/2];
		resizeMatrix = reshape(originalImage, 2, totalSize/2 );
		 
		Matrix resizeMatrixZ = new Matrix(resizeMatrix);
		
//      Covariance Matrix
		Matrix covariance = resizeMatrixZ.times(resizeMatrixZ.transpose());
		covariance = covariance.times(2/(double)totalSize);

//	    [U,S,V] = svd(X) produces a diagonal matrix S, of the same 
//	    dimension as X and with nonnegative diagonal elements in
//	    decreasing order, and unitary matrices U and V so that
//	    X = U*S*V'.		
		SingularValueDecomposition SVD = new SingularValueDecomposition(covariance);
		double singularValues[] = SVD.getSingularValues();	
		double covArray[][] = covariance.getArray();
		
		// Calculate rotation factor
		double theta = Math.atan((singularValues[0] - covArray[0][0])/covArray[1][0]);
		
		Matrix rotationMatrix = getRotationMatrix(theta);
		Matrix compressedImageMatrix = rotationMatrix.times(resizeMatrixZ);
		double imgTmp[][] = compressedImageMatrix.getArray();
		compressedImage = imgTmp[0];
		
		 
		compressedImageBytes =  setupDataForCompression(compressedImage, theta, ratio);
		}
		else {
			int k = 0;
			double uncompressedImage[] = new uncompressedImage[totalSize];
			//Converting original image from int to double
			for(int i = 0; i < image.length; i++)
		    {
		        for(int j = 0; j < image[0].length; j++)
		    		uncompressedImage[k] = (double) image[i][j];
		        	k++;
		    }
			
			compressedImageBytes =  setupDataForCompression(originalImage, 0, ratio);
		}
	}

	/**
	 * Given theta , get the rotation matrix
	 * @param theta2
	 * @return
	 */
	private Matrix getRotationMatrix(double theta) {
		// Rotation Matrix
		double rotation[][] = new double[2][2];
		rotation[0][0] = Math.cos(theta);
		rotation[0][1] = Math.sin(theta);
		rotation[1][0] = - Math.sin(theta);
		rotation[1][1] = Math.cos(theta);		
		
		Matrix rotationMatrix = new Matrix(rotation);
		return rotationMatrix;
	}	
	
	/**
	 * This method is used to set up the Int array with theta, width and height for compression
	 * @param compressedImage
	 * @param theta
	 * @return int array for compression
	 */
	private byte[] setupDataForCompression(double[] compressedImage, double theta, int ratio) {
		
		 int[] compressedImageInt = new int[compressedImage.length + 4];
		for (int i = 0; i<compressedImage.length; i ++ ) {
			compressedImageInt[i] = (int)compressedImage[i];
		}
		compressedImageInt[compressedImageInt.length - 1] = height;
		compressedImageInt[compressedImageInt.length - 2] = width;
		compressedImageInt[compressedImageInt.length - 3] = (int)(theta*10000000);
		compressedImageInt[compressedImageInt.length - 4] = ratio
        ByteBuffer byteBuffer = ByteBuffer.allocate(compressedImageInt.length * 4);        
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(compressedImageInt);
		return byteBuffer.array();
	}

	/**
	 * Getter for the Compressed Images 
	 * @return
	 */
	public byte[] getCompressedImage() {
		return compressedImageBytes;
	}
	
	
	/**
	 * Reshape the Array
	 * @param A
	 * @param m
	 * @param n
	 * @return
	 */
	static double[][] reshape(double[][] A, int m, int n) {
	        int origM = A.length;
	        int origN = A[0].length;
	        if(origM*origN != m*n){
	            throw new IllegalArgumentException("New matrix must be of same area as matix A");
	        }
	        double[][] B = new double[m][n];
	        double[] A1D = new double[A.length * A[0].length];

	        int index = 0;
	        for(int i = 0;i<A.length;i++){
	            for(int j = 0;j<A[0].length;j++){
	                A1D[index++] = A[i][j];
	            }
	        }

	        index = 0;
	        for(int i = 0;i<n;i++){
	            for(int j = 0;j<m;j++){
	                B[j][i] = A1D[index++];
	            }

	        }
	        return B;
	    } 
}
