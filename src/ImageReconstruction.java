import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import Jama.Matrix;

public class ImageReconstruction implements Runnable{
	private int[][] reconstructedImage;
	double theta;
	int ratio;
	private int width;
	private int height;
	int[] reconstructedArray;
	int totalSize;
	double[][] imgTmp;
	/**
	 * Constructor that gets a compressed image and gived out the reconstructed image
	 * @param compressed
	 */
	public ImageReconstruction(byte[] compressed) {
		 IntBuffer intBuf = ByteBuffer.wrap(compressed)
				     .order(ByteOrder.BIG_ENDIAN)
				     .asIntBuffer();
	     reconstructedArray = new int[intBuf.remaining()];
		 intBuf.get(reconstructedArray);
		 
		 totalSize = reconstructedArray.length;
		 height = reconstructedArray[totalSize-1];
		 width = reconstructedArray[totalSize-2];
		 theta = reconstructedArray[totalSize-3];
		 ratio = reconstructedArray[totalSize-4];
		 
	}
	
	/**
	 * The image that comes out is out of Phase, this method rephases it
	 * @param expandedImageTmp
	 * @return
	 */
	private double[][] rephaseImage(double[][] expandedImageTmp) {
		
		double[][] expandedImage = new double[height][width];
		
		for(int i = 0; i < height; i++) {
		 	 for (int j = 0; j < width; j = j+2) {
		 		
		 		 expandedImage[i][j] = expandedImageTmp[i][j/2];			 
			 }
		 }
			 
		 for(int i = 0; i  < height; i++) {
			 int k = 0;
			 for (int j = 1; j < width; j = j+2) {
				 expandedImage[i][j] = expandedImageTmp[i][(width/2) + k];			 
				 k++;
			 }
		 }
		 expandedImageTmp = expandedImage;
		 expandedImage = new double[height][width];
		 for(int i = 0; i < height; i = i + 2) {
			 expandedImage[i] = expandedImageTmp[i/2]; 
					
		 }	 

		 int k = 0;
		 for(int i = 1; i < height; i = i + 2) {
			expandedImage[i] = expandedImageTmp[(height/2) + k];
		 	k++;						
		 };
		 
		 return  expandedImage;
	}

	/**
	 * Convert from Double to Int array
	 * @param expandedImage
	 * @return
	 */
	private int[][] convertDoubletoInt(double[][] expandedImage) {
		int[][] reconstructedImage = null;
		try{
			reconstructedImage = new int[expandedImage.length][expandedImage[0].length];
			for(int i = 0; i < expandedImage.length; i++) {
				for (int j= 0; j < expandedImage[0].length; j++) {
					reconstructedImage[i][j] = (int)expandedImage[i][j];
				}
			}
		} catch(Exception e){
			
		}
		
		return reconstructedImage;
	}
	
	/**
	 * Get the rotation matrix from theta
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
	 * Construct 2 row array from the compressed data
	 * @param reconstructedArray
	 * @return
	 */
	private void construct2rowArray(int[] reconstructedArray) {
		imgTmp = new double[2][reconstructedArray.length - 5];
		for (int i = 0; i<reconstructedArray.length - 5; i++) {
			imgTmp[0][i] = reconstructedArray[i];
		}

		height = reconstructedArray[reconstructedArray.length - 1];
		width = reconstructedArray[reconstructedArray.length - 2];
		theta = (double)reconstructedArray[reconstructedArray.length - 3]/10000000;
		
	}
	
	/**
	 * Getter for reconstructed Image
	 * @return
	 */
	public int[][] getReconstructedImage(){
		return reconstructedImage;
	}
	
	/**
	 * Reshape from a 2 Row Array to original image shape
	 * @param A
	 * @param m
	 * @param n
	 * @return reshaped array
	 */
	public static double[][] reshape(double[][] A, int m, int n) {
		 
		 int k = 0;
		 double output[][] = new double[n][m];
		 double oneDArray[] = new double[A[0].length*A.length];
		 
		 for (int i= 0; i < A.length; i++) {
			 for ( int j = 0; j < A[0].length ; j++) {
				 oneDArray[k] = A[i][j];
				 k++;
			 }
		 }
		 
		 k = 0;
		 for(int i = 0; i < n; i++) {
			 for (int j = 0; j < m; j++) {
				 output[i][j] = oneDArray[k];
				 k++;
			 }
 		 }
		 
		 return  output;
	 }
	
	public static int[][] reshapeInt(int[][] A, int m, int n) {
		 
		 int k = 0;
		 int output[][] = new int[n][m];
		 int oneDArray[] = new int[A[0].length*A.length];
		 
		 for (int i= 0; i < A.length; i++) {
			 for ( int j = 0; j < A[0].length ; j++) {
				 oneDArray[k] = A[i][j];
				 k++;
			 }
		 }
		 
		 k = 0;
		 for(int i = 0; i < n; i++) {
			 for (int j = 0; j < m; j++) {
				 output[i][j] = oneDArray[k];
				 k++;
			 }
		 }
		 
		 return  output;
	 }

	@Override
	public void run() {
		 if (ratio != 1) {
			 construct2rowArray(reconstructedArray);
			 Matrix imgTmpMatrix = new Matrix(imgTmp);
			 Matrix rotationMatrix = getRotationMatrix(theta);
			 rotationMatrix = rotationMatrix.transpose();
			 Matrix expandedImageMatrix = rotationMatrix.times(imgTmpMatrix);
	
			 double [][] expandedImageTmp = reshape(expandedImageMatrix.getArray(), width, height);
			 double[][] expandedImage = rephaseImage(expandedImageTmp);		 
		 
			 reconstructedImage = convertDoubletoInt(expandedImage);
		 	}
		 	else {
		 		int[][] img = new int[1][width*height];
		 		img[0] = reconstructedArray;
		 		reconstructedImage = reshapeInt(img, width, height);
		 	}
		
	}

}