package Utils;

public class ComputationUtils {

	public static void printArray(double[] inp){
		for(int ki=0;ki<inp.length;ki++){
			System.out.print(inp[ki]+" ");
		}
		System.out.println();
	}
	
	public static void printArray(Double[] inp){
		for(int ki=0;ki<inp.length;ki++){
			System.out.print(inp[ki]+" ");
		}
		System.out.println();
	}
	
	public static double dotProduct(double[] a, double[] b) {
		double result = 0.0;
		for (int i = 0; i < a.length; i++) {
			result += a[i] * b[i];
		}

		return result;
	}
	
	public static double radToDeg(double radAngle) {
		return (radAngle*180)/3.14;
	}
	
}
