package Calibration;

import Jama.Matrix;

public class CalibrationPrimaryLoop {
	
	public static double[] j1_valf = new double[3];
	public static double[] j2_valf = new double[3];
	
	private static double pi=3.14;
	
	public static void runLoop(int numOfIterations, int sizeOfData, double[][] gyro_s_knee, double[][] gyro_s_shank,
			double[][] acc_s_knee, double[][] acc_s_shank, double[][] gyro_s_derv_knee, double[][] gyro_s_derv_shank) {
		System.out.println("Running the Primary Loop");

		boolean isDebug = false;
		
		double phi1, phi2, theta1, theta2;

		phi1 = pi / 6;
		theta1 = pi / 3;
		phi2 = pi / 3;
		theta2 = pi / 6;

		double x_val[][] = new double[numOfIterations][4];
		double j1_val[][] = new double[numOfIterations][3];
		double j2_val[][] = new double[numOfIterations][3];

		x_val[0][0] = phi1;
		x_val[0][1] = theta1;
		x_val[0][2] = phi2;
		x_val[0][3] = theta2;

		double[][] jac = new double[sizeOfData][4];
		double[][] jacT = new double[sizeOfData][4];

		double[][] e_val = new double[numOfIterations][sizeOfData];
		double[] vss_error = new double[sizeOfData];

		vss_error[0] = 0;

		double threshold = .0001;
		int k = 0;

		for (k = 0; k < numOfIterations - 1; k++) {

			if (isDebug) {
				System.out.println("===============================================");
				System.out.println("Iteration: " + k);
				System.out.println("Setting the values for Outer Loop");
			}

			double[] v1 = jVal(x_val[k][0], x_val[k][1]);
			double[] v2 = jVal(x_val[k][2], x_val[k][3]);

			// ================

			j1_val[k][0] = v1[0];
			j1_val[k][1] = v1[1];
			j1_val[k][2] = v1[2];

			j2_val[k][0] = v2[0];
			j2_val[k][1] = v2[1];
			j2_val[k][2] = v2[2];

			if (isDebug) {
				System.out.println("New j1_val is: ");
				for (int ki = 0; ki < 3; ki++) {
					System.out.print(j1_val[k][ki] + " ");
				}
				
				System.out.println();

				System.out.println("New j2_val is: ");
				for (int ki = 0; ki < 3; ki++) {
					System.out.print(j2_val[k][ki] + " ");
				}
				System.out.println();
			}

			// =================
			if (isDebug) {
				System.out.println("Going in the inside loop");
			}

			for (int i = 0; i < sizeOfData; i++) {

				double[] g1_val = gyro_s_knee[i];
				double[] g2_val = gyro_s_shank[i];

				jac[i] = calcJacobDx(x_val[k][0], x_val[k][1], x_val[k][2], x_val[k][3], g1_val[0], g1_val[1],
						g1_val[2], g2_val[0], g2_val[1], g2_val[2]);

				e_val[k][i] = (calcEjacob(x_val[k][0], x_val[k][1], x_val[k][2], x_val[k][3], g1_val[0], g1_val[1],
						g1_val[2], g2_val[0], g2_val[1], g2_val[2]));

				vss_error[k] = vss_error[k] + Math.pow(e_val[k][i], 2.0);

			}
			
			if (isDebug) {
				System.out.println("VSError is " + vss_error[k]);
			}

			Matrix mJac = new Matrix(jac);
			Matrix mJacT = mJac.transpose();
			Matrix mJz = mJacT.times(-1.0).times(mJac);

			Matrix g = mJz.solve(mJacT);

			double[][] eValCol = new double[1][sizeOfData];

			for (int i = 0; i < sizeOfData; i++) {
				eValCol[0][i] = e_val[k][i];
			}

			Matrix eValColM = new Matrix(eValCol);
			Matrix negPart = mJac.inverse().times(eValColM.transpose());

			double[][] xValCol = new double[1][sizeOfData];

			xValCol[0] = x_val[k];

			Matrix xValColM = new Matrix(xValCol).transpose();
			Matrix evalValue = xValColM.minus(negPart);

			// evalValue.print(0, 4);
			double[][] newXVal = evalValue.transpose().getArray();

			if (isDebug) {
				System.out.println("Setting the new values for x_val");
				for (int ki = 0; ki < 4; ki++) {
					System.out.print(newXVal[0][ki] + " ");
				}
				System.out.println();
			}
			
			x_val[k + 1] = newXVal[0];

			double err = 0.0;
			if (k == 0) {
				err = Math.abs(vss_error[k]);
			} else {
				err = Math.abs(vss_error[k] - vss_error[k - 1]);
			}
			
			if(isDebug){
				System.out.println("Error is:" + err);
			}
			
			// TODO set the threshold here
			if (Math.abs(err) < threshold) {
				System.out.println("Breaking out of the loop");
				break;
			}

		}

		System.out.println("j1_val is: ");
		for (int ki = 0; ki < 3; ki++) {
			System.out.print(j1_val[k-1][ki] + " ");
			j1_valf[ki] = j1_val[k-1][ki];
		}
		System.out.println("\nj2_val is: ");
		for (int ki = 0; ki < 3; ki++) {
			System.out.print(j2_val[k-1][ki] + " ");
			j2_valf[ki] = j2_val[k-1][ki];
		}
	}

	
	private static double[] jVal(double phi, double theta) {

		double[] result = new double[3];

		result[0] = Math.cos(phi) * Math.cos(theta);
		result[1] = Math.cos(phi) * Math.sin(theta);
		result[2] = Math.sin(phi);

		return result;
	}
	
	private static double calcEjacob(double phi1, double theta1, double phi2, double theta2, double g1x, double g1y,
			double g1z, double g2x, double g2y, double g2z) {
		return (Math
				.sqrt(Math
						.pow((g1y * Math.cos(phi1) * Math.cos(theta1) - g1x * Math.cos(phi1) * Math.sin(theta1)),
								2.0)
						+ Math.pow((g1x * Math.sin(phi1) - g1z * Math.cos(phi1) * Math.cos(theta1)), 2.0)
						+ Math.pow((g1y * Math.sin(phi1) - g1z * Math.cos(phi1) * Math.sin(theta1)), 2.0))
				- Math.sqrt(
						Math.pow((g2y * Math.cos(phi2) * Math.cos(theta2) - g2x * Math.cos(phi2) * Math.sin(theta2)),
								2.0) + Math.pow((g2x * Math.sin(phi2) - g2z * Math.cos(phi2) * Math.cos(theta2)), 2.0)
								+ Math.pow((g2y * Math.sin(phi2) - g2z * Math.cos(phi2) * Math.sin(theta2)), 2.0)));
	}

	private static double[] calcJacobDx(double phi1, double theta1, double phi2, double theta2, double g1x, double g1y,
			double g1z, double g2x, double g2y, double g2z) {

		double[] result = new double[4];

		double val1 = (2 * (g1x * Math.cos(phi1) + g1z * Math.cos(theta1) * Math.sin(phi1))
				* (g1x * Math.sin(phi1)
						- g1z * Math.cos(phi1) * Math.cos(theta1))
				- 2 * (g1y * Math.cos(phi1) * Math.cos(theta1) - g1x * Math.cos(phi1) * Math.sin(theta1))
						* (g1y * Math.cos(theta1) * Math.sin(phi1) - g1x * Math.sin(phi1) * Math.sin(theta1))
				+ 2 * (g1y
						* Math.cos(
								phi1)
						+ g1z * Math.sin(phi1)
								* Math.sin(
										theta1))
						* (g1y * Math.sin(phi1) - g1z * Math.cos(phi1) * Math.sin(theta1)))
				/ Math.sqrt(
						2 * (Math
								.pow((g1y * Math.cos(phi1) * Math.cos(theta1)
										- g1x * Math.cos(phi1) * Math.sin(theta1)), 2.0)
								+ Math.pow((g1x * Math.sin(phi1) - g1z * Math.cos(phi1) * Math.cos(theta1)), 2.0)
								+ Math.pow((g1y * Math.sin(phi1) - g1z * Math.cos(phi1) * Math.sin(theta1)), 2.0)));

		double val2 = -(2 * (g1x * Math.cos(phi1) * Math.cos(theta1) + g1y * Math.cos(phi1) * Math.sin(theta1))
				* (g1y * Math.cos(phi1) * Math.cos(theta1) - g1x * Math.cos(phi1) * Math.sin(theta1))
				- 2 * g1z
						* Math.cos(phi1) * Math
								.sin(theta1)
						* (g1x * Math.sin(phi1) - g1z * Math.cos(phi1) * Math.cos(theta1))
				+ 2 * g1z
						* Math.cos(
								phi1)
						* Math.cos(theta1) * (g1y * Math.sin(phi1) - g1z * Math.cos(phi1) * Math.sin(theta1)))
				/ Math.sqrt(
						2 * (Math
								.pow((g1y * Math.cos(phi1) * Math.cos(theta1)
										- g1x * Math.cos(phi1) * Math.sin(theta1)), 2.0)
								+ Math.pow((g1x * Math.sin(phi1) - g1z * Math.cos(phi1) * Math.cos(theta1)), 2.0)
								+ Math.pow((g1y * Math.sin(phi1) - g1z * Math.cos(phi1) * Math.sin(theta1)), 2.0)));

		double val3 = -(2 * (g2x * Math.cos(phi2) + g2z * Math.cos(theta2) * Math.sin(phi2))
				* (g2x * Math.sin(phi2)
						- g2z * Math.cos(phi2) * Math.cos(theta2))
				- 2 * (g2y * Math.cos(phi2) * Math.cos(theta2) - g2x * Math.cos(phi2) * Math.sin(theta2))
						* (g2y * Math.cos(theta2) * Math.sin(phi2) - g2x * Math.sin(phi2) * Math.sin(theta2))
				+ 2 * (g2y * Math.cos(phi2)
						+ g2z * Math.sin(phi2)
								* Math.sin(theta2))
						* (g2y * Math.sin(phi2) - g2z * Math.cos(phi2) * Math.sin(theta2)))
				/ (2 * Math
						.sqrt(Math
								.pow((g2y * Math.cos(phi2) * Math.cos(theta2)
										- g2x * Math.cos(phi2) * Math.sin(theta2)), 2.0)
								+ Math.pow((g2x * Math.sin(phi2) - g2z * Math.cos(phi2) * Math.cos(theta2)), 2.0)
								+ Math.pow((g2y * Math.sin(phi2) - g2z * Math.cos(phi2) * Math.sin(theta2)), 2.0)));

		double val4 = (2 * (g2x * Math.cos(phi2) * Math.cos(theta2) + g2y * Math.cos(phi2) * Math.sin(theta2))
				* (g2y * Math.cos(phi2) * Math.cos(theta2) - g2x * Math.cos(phi2) * Math.sin(theta2))
				- 2 * g2z * Math.cos(phi2)
						* Math.sin(
								theta2)
						* (g2x * Math.sin(phi2) - g2z * Math.cos(phi2) * Math.cos(theta2))
				+ 2 * g2z
						* Math.cos(
								phi2)
						* Math.cos(
								theta2)
						* (g2y * Math
								.sin(phi2) - g2z
										* Math.cos(
												phi2)
										* Math.sin(theta2)))
				/ (2 * Math
						.sqrt(Math.pow(Math
								.pow((g2y * Math.cos(phi2) * Math.cos(theta2)
										- g2x * Math.cos(phi2) * Math.sin(theta2)), 2.0)
								+ Math.pow((g2x * Math.sin(phi2) - g2z * Math.cos(phi2) * Math.cos(theta2)), 2.0)
								+ (g2y * Math.sin(phi2) - g2z * Math.cos(phi2) * Math.sin(theta2)), 2.0)));

		result[0] = val1;
		result[1] = val2;
		result[2] = val3;
		result[3] = val4;

		return result;

	}

	
	
}
