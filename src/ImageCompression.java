
import Jama.*; 
//import java.util.Date;

public class ImageCompression {
	private double[][] originalImage; 
	private double[] compressedImage;
	public double[][] expandedImage;
	private int width;
	private int height;
	public ImageCompression(int[][] image) {
		originalImage = new double[image.length][image[0].length];
		
		//Converting original image from int to double
		for(int i = 0; i < image.length; i++)
	    {
	        for(int j = 0; j < image[0].length; j++)
	    		originalImage[i][j] = (double) image[i][j];
		}		
		
		width = image[0].length;
		height = image.length;
		int totalSize = image.length*image[0].length;
		// resize matrix into two rows
		double[][] resizeMatrix = new double[2][totalSize/2];
		resizeMatrix = reshape(originalImage, 2, totalSize/2 );
		 
		Matrix resizeMatrixZ = new Matrix(resizeMatrix);
		
		//Covariance Matrix
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
		
		// Rotation Matrix
		double rotation[][] = new double[2][2];
		rotation[0][0] = Math.cos(theta);
		rotation[0][1] = Math.sin(theta);
		rotation[1][0] = - Math.sin(theta);
		rotation[1][1] = Math.cos(theta);		
		
		Matrix rotationMatrix = new Matrix(rotation);
		Matrix compressedImageMatrix = rotationMatrix.times(resizeMatrixZ);
		double imgTmp[][] = compressedImageMatrix.getArray();
		compressedImage = imgTmp[0];
		imgTmp[1] = new double[imgTmp[0].length];
		Matrix imgTmpMatrix = new Matrix(imgTmp);
		rotationMatrix = rotationMatrix.transpose();
		Matrix expandedImageMatrix = rotationMatrix.times(imgTmpMatrix);
		
		double [][] expandedImageTmp = reshape2(expandedImageMatrix.getArray(), width, height);
		expandedImage = new double[height][width]; 
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
				
			}
	}
	
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
	 
	 public static double[][] reshape2(double[][] A, int m, int n) {
		 
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
		 
		 return output;
	 }
	 
	
}
