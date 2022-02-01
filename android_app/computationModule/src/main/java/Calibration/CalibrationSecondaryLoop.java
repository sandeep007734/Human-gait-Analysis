package Calibration;

import Jama.Matrix;
import Utils.ComputationUtils;

public class CalibrationSecondaryLoop {

	public static double[] o1f_val = new double[3];
	public static double[] o2f_val = new double[3];

	public static void runLoop(int numOfIterations, int sizeOfData, double[][] gyro_s_knee, double[][] gyro_s_shank,
			double[][] acc_s_knee, double[][] acc_s_shank, double[][] gyro_s_derv_knee, double[][] gyro_s_derv_shank,
			double[] j1_valf, double[] j2_valf) {

//		numOfIterations = 2;
//		sizeOfData = 4;

		System.out.println("\nRunning the secondary loop");
		double[][] x_val = new double[numOfIterations][6];
		double[][] jac_o = new double[sizeOfData][6];
		double[] css_error = new double[sizeOfData];

		double[][] e_val = new double[numOfIterations][sizeOfData];

		x_val[0][0] = 1;
		x_val[0][1] = 0.8;
		x_val[0][2] = 1;
		x_val[0][3] = 1;
		x_val[0][4] = 0.9;
		x_val[0][5] = 0.5;

		css_error[0] = 0;

		double threshold = .0001;
		int k = 0;
		boolean isDebug = false;

		for (k = 0; k < numOfIterations - 1; k++) {

			if (isDebug) {
				System.out.println("===============================================");
				System.out.println("Iteration: " + k);
				System.out.println("Going in the inside loop. Size of Data is: " + sizeOfData);
			}

			for (int i = 0; i < sizeOfData; i++) {

				double[] a1_val = acc_s_knee[i];
				double[] a2_val = acc_s_shank[i];

				double[] g1_val = gyro_s_knee[i];
				double[] g2_val = gyro_s_shank[i];

				double[] gd1_val = gyro_s_derv_knee[i];
				double[] gd2_val = gyro_s_derv_shank[i];

				double fxVal[] = new double[3];
				fxVal[0] = x_val[k][0];
				fxVal[1] = x_val[k][1];
				fxVal[2] = x_val[k][2];

				double sxVal[] = new double[3];
				sxVal[0] = x_val[k][3];
				sxVal[1] = x_val[k][4];
				sxVal[2] = x_val[k][5];

				if (isDebug) {
					System.out.println(i);
					System.out.println("fxVal: " + fxVal[0] + " " + fxVal[1] + " " + fxVal[2]);
					System.out.println("g1_val: " + String.format("%.5f", g1_val[0]) + " "
							+ String.format("%.5f", g1_val[1]) + " " + String.format("%.5f", g1_val[2]));
					System.out.println("gd1_val: " + String.format("%.5f", gd1_val[0]) + " "
							+ String.format("%.5f", gd1_val[1]) + " " + String.format("%.5f", gd1_val[2]));
					System.out.println("sxVal: " + sxVal[0] + " " + sxVal[1] + " " + sxVal[2]);
					System.out.println("g2_val: " + g2_val[0] + " " + g2_val[1] + " " + g2_val[2]);
					System.out.println("gd2_val: " + String.format("%.5f", gd2_val[0]) + " "
							+ String.format("%.5f", gd2_val[1]) + " " + String.format("%.5f", gd2_val[2]));
					System.out.println("a1_val: " + a1_val[0] + " " + a1_val[1] + " " + a1_val[2]);
					System.out.println("a2_val: " + a2_val[0] + " " + a2_val[1] + " " + a2_val[2]);
				}
				jac_o[i] = calcJacbo(fxVal, g1_val, gd1_val, sxVal, g2_val, gd2_val, a1_val, a2_val);

				if (isDebug) {
					System.out.println("Value of jac_o is");
					for (int ki = 0; ki < 6; ki++) {
						System.out.print(jac_o[i][ki] + " ");
					}
					System.out.println();
				}

				e_val[k][i] = calcEo(fxVal, g1_val, gd1_val, sxVal, g2_val, gd2_val, a1_val, a2_val);

				css_error[k] = css_error[k] + Math.pow(e_val[k][i], 2.0);
				if (isDebug) {
					System.out.println("jac_0: " + jac_o[0][0]);
					System.out.println("eval is " + e_val[k][i]);
					System.out.println();
				}

			}

			Matrix mJac = new Matrix(jac_o);
			Matrix mJacT = mJac.transpose();
			Matrix mJz = mJacT.times(-1.0).times(mJac);
			Matrix g = mJz.solve(mJacT);

			// mJac.print(0, 4);
			// mJz.print(0, 4);
			// g.print(0, 4);

			// ======== setting new x_val
			double[][] eValCol = new double[1][sizeOfData];

			for (int i = 0; i < sizeOfData; i++) {
				eValCol[0][i] = e_val[k][i];
			}

			Matrix eValColM = new Matrix(eValCol);
			Matrix sencondPart = g.times(eValColM.transpose());

			double[][] xValCol = new double[1][sizeOfData];

			xValCol[0] = x_val[k];

			Matrix xValColM = new Matrix(xValCol).transpose();
			Matrix evalValue = xValColM.plus(sencondPart);

			double[][] newXVal = evalValue.transpose().getArray();

			if (isDebug) {
				System.out.println("Setting the new values for x_val");
				ComputationUtils.printArray(newXVal[0]);
			}

			x_val[k + 1] = newXVal[0];

			double err = 0.0;
			if (k == 0) {
				err = Math.abs(css_error[k]);
			} else {
				err = Math.abs(css_error[k] - css_error[k - 1]);
			}

			if (isDebug) {
				System.out.println("Error is:" + err);
			}


			if (Math.abs(err) < threshold) {
				System.out.println("Breaking out of the loop");
				break;
			}

		}

		// get the o1f and o2f values
		double o1_val[] = new double[3];
		double o2_val[] = new double[3];

		o1_val[0] = x_val[k][0];
		o1_val[1] = x_val[k][1];
		o1_val[2] = x_val[k][2];

		o2_val[0] = x_val[k][3];
		o2_val[1] = x_val[k][4];
		o2_val[2] = x_val[k][5];

		if (isDebug) {
			System.out.println("Input to dotProduct is");
			ComputationUtils.printArray(o1_val);
			ComputationUtils.printArray(o2_val);
		}
		double dotVal = (ComputationUtils.dotProduct(o1_val, j1_valf) + ComputationUtils.dotProduct(o2_val, j2_valf)) / 2;
		if (isDebug) {
			System.out.println("Dot Product Value is: " + dotVal);
		}

		o1f_val[0] = j1_valf[0] * dotVal;
		o1f_val[1] = j1_valf[1] * dotVal;
		o1f_val[2] = j1_valf[2] * dotVal;

		o1f_val[0] = o1_val[0] - o1f_val[0];
		o1f_val[1] = o1_val[1] - o1f_val[1];
		o1f_val[2] = o1_val[2] - o1f_val[2];

		o2f_val[0] = j2_valf[0] * dotVal;
		o2f_val[1] = j2_valf[1] * dotVal;
		o2f_val[2] = j2_valf[2] * dotVal;

		o2f_val[0] = o2_val[0] - o2f_val[0];
		o2f_val[1] = o2_val[1] - o2f_val[1];
		o2f_val[2] = o2_val[2] - o2f_val[2];

		System.out.println("\n o1f_val is: ");
		for (int ki = 0; ki < 3; ki++) {
			System.out.print(String.format("%.5f", o1f_val[ki]) + " ");
		}
		System.out.println("\n o2f_val is: ");
		for (int ki = 0; ki < 3; ki++) {
			System.out.print(String.format("%.5f", o2f_val[ki]) + " ");
		}
		System.out.println();
	}

//	public static double dotProduct(double[] a, double[] b) {
//		double result = 0.0;
//		for (int i = 0; i < a.length; i++) {
//			result += a[i] * b[i];
//		}
//
//		return result;
//	}

	public static double calcEo(double[] g1, double[] o1, double[] a1, double gd1[], double[] g2, double[] o2,
			double[] a2, double[] gd2) {
		return Math
				.sqrt(Math
						.pow((gd1[1] * o1[2] - gd1[2] * o1[1] - a1[0] + g1[1] * (g1[0] * o1[1] - g1[1] * o1[0])
								+ g1[2] * (g1[0] * o1[2] - g1[2] * o1[0])), 2.0)
						+ Math.pow((a1[1] - gd1[2] * o1[0] + gd1[0] * o1[2]
								+ g1[0] * (g1[0] * o1[1] - g1[1] * o1[0]) - g1[2]
										* (g1[1] * o1[2] - g1[2] * o1[1])),
								2.0)
						+ Math.pow(
								(a1[2] + gd1[1] * o1[0] - gd1[0] * o1[1]
										+ g1[0] * (g1[0] * o1[2]
												- g1[2] * o1[0])
										+ g1[1] * (g1[1] * o1[2] - g1[2] * o1[1])),
								2.0))
				- Math.sqrt(
						Math.pow((gd2[1] * o2[2] - gd1[2] * o2[1] - a2[0]
								+ g2[1] * (g2[0] * o2[1] - g2[1] * o2[0]) + g2[2]
										* (g2[0] * o2[2] - g2[2] * o2[0])),
								2.0)
								+ Math.pow((a2[1] - gd1[2] * o2[0] + gd2[0] * o2[2]
										+ g2[0] * (g2[0] * o2[1] - g2[1] * o2[0])
										- g2[2] * (g2[1] * o2[2] - g2[2] * o2[1])), 2.0)
								+ Math.pow((a2[2] + gd2[1] * o2[0] - gd2[0] * o2[1]
										+ g2[0] * (g2[0] * o2[2] - g2[2] * o2[0])
										+ g2[1] * (g2[1] * o2[2] - g2[2] * o2[1])), 2.0));
	}

	public static double[] calcJacbo(double[] g1, double[] o1, double[] a1, double gd1[], double[] g2, double[] o2,
			double[] a2, double[] gd2) {

		double[] result = new double[6];

		result[0] = -(2 * (gd1[2] + g1[0] * g1[1])
				* (a1[1] - gd1[2] * o1[0] + gd1[0] * o1[2] + g1[0] * (g1[0] * o1[1] - g1[1] * o1[0])
						- g1[2] * (g1[1] * o1[2] - g1[2] * o1[1]))
				- 2 * (gd1[1] - g1[0] * g1[2])
						* (a1[2] + gd1[1] * o1[0] - gd1[0] * o1[1] + g1[0] * (g1[0] * o1[2] - g1[2] * o1[0])
								+ g1[1] * (g1[1] * o1[2] - g1[2] * o1[1]))
				+ 2 * (g1[1]
						* g1[1] + g1[2]
								* g1[2])
						* (gd1[1] * o1[2] - gd1[2] * o1[1]
								- a1[0] + g1[1]
										* (g1[0] * o1[1] - g1[1] * o1[0])
								+ g1[2] * (g1[0] * o1[2]
										- g1[2] * o1[0])))
				/ (2 * Math
						.sqrt(Math
								.pow((gd1[1] * o1[2] - gd1[2] * o1[1] - a1[0]
										+ g1[1] * (g1[0] * o1[1] - g1[1] * o1[0]) + g1[2]
												* (g1[0] * o1[2] - g1[2] * o1[0])),
										2.0)
								+ Math.pow((a1[1] - gd1[2] * o1[0] + gd1[0] * o1[2]
										+ g1[0] * (g1[0] * o1[1] - g1[1] * o1[0])
										- g1[2] * (g1[1] * o1[2] - g1[2] * o1[1])), 2.0)
						+ Math.pow((a1[2] + gd1[1] * o1[0] - gd1[0] * o1[1] + g1[0] * (g1[0] * o1[2] - g1[2] * o1[0])
								+ g1[1] * (g1[1] * o1[2] - g1[2] * o1[1])), 2.0)));

		result[1] = -(2 * (gd1[2] - g1[0] * g1[1])
				* (gd1[1] * o1[2] - gd1[2] * o1[1] - a1[0] + g1[1] * (g1[0] * o1[1] - g1[1] * o1[0])
						+ g1[2] * (g1[0] * o1[2] - g1[2] * o1[0]))
				- 2 * (g1[0] * g1[0] + g1[2] * g1[2])
						* (a1[1] - gd1[2] * o1[0] + gd1[0] * o1[2] + g1[0] * (g1[0] * o1[1] - g1[1] * o1[0])
								- g1[2] * (g1[1] * o1[2] - g1[2] * o1[1]))
				+ 2 * (gd1[0]
						+ g1[1] * g1[2]) * (a1[2]
								+ gd1[1] * o1[0] - gd1[0]
										* o1[1]
								+ g1[0] * (g1[0] * o1[2] - g1[2] * o1[0]) + g1[1]
										* (g1[1] * o1[2]
												- g1[2] * o1[1])))
				/ (2 * Math
						.sqrt(Math
								.pow((gd1[1] * o1[2] - gd1[2] * o1[1] - a1[0]
										+ g1[1] * (g1[0] * o1[1] - g1[1] * o1[0]) + g1[2]
												* (g1[0] * o1[2] - g1[2] * o1[0])),
										2.0)
								+ Math.pow((a1[1] - gd1[2] * o1[0] + gd1[0] * o1[2]
										+ g1[0] * (g1[0] * o1[1] - g1[1] * o1[0])
										- g1[2] * (g1[1] * o1[2] - g1[2] * o1[1])), 2.0)
						+ Math.pow((a1[2] + gd1[1] * o1[0] - gd1[0] * o1[1] + g1[0] * (g1[0] * o1[2] - g1[2] * o1[0])
								+ g1[1] * (g1[1] * o1[2] - g1[2] * o1[1])), 2.0)));

		result[2] = (2 * (gd1[1] + g1[0] * g1[2])
				* (gd1[1] * o1[2] - gd1[2] * o1[1] - a1[0] + g1[1] * (g1[0] * o1[1] - g1[1] * o1[0])
						+ g1[2] * (g1[0] * o1[2] - g1[2] * o1[0]))
				+ 2 * (g1[0] * g1[0] + g1[1] * g1[1])
						* (a1[2] + gd1[1] * o1[0] - gd1[0] * o1[1] + g1[0] * (g1[0] * o1[2] - g1[2] * o1[0])
								+ g1[1] * (g1[1] * o1[2] - g1[2] * o1[1]))
				+ 2 * (gd1[0]
						- g1[1] * g1[2]) * (a1[1]
								- gd1[2] * o1[0] + gd1[0]
										* o1[2]
								+ g1[0] * (g1[0] * o1[1] - g1[1] * o1[0]) - g1[2]
										* (g1[1] * o1[2]
												- g1[2] * o1[1])))
				/ (2 * Math
						.sqrt(Math
								.pow((gd1[1] * o1[2] - gd1[2] * o1[1] - a1[0]
										+ g1[1] * (g1[0] * o1[1] - g1[1] * o1[0]) + g1[2]
												* (g1[0] * o1[2] - g1[2] * o1[0])),
										2.0)
								+ Math.pow((a1[1] - gd1[2] * o1[0] + gd1[0] * o1[2]
										+ g1[0] * (g1[0] * o1[1] - g1[1] * o1[0])
										- g1[2] * (g1[1] * o1[2] - g1[2] * o1[1])), 2.0)
						+ Math.pow((a1[2] + gd1[1] * o1[0] - gd1[0] * o1[1] + g1[0] * (g1[0] * o1[2] - g1[2] * o1[0])
								+ g1[1] * (g1[1] * o1[2] - g1[2] * o1[1])), 2.0)));

		result[3] = (2 * (gd1[2] + g2[0] * g2[1])
				* (a2[1] - gd1[2] * o2[0] + gd2[0] * o2[2] + g2[0] * (g2[0] * o2[1] - g2[1] * o2[0])
						- g2[2] * (g2[1] * o2[2] - g2[2] * o2[1]))
				- 2 * (gd2[1] - g2[0] * g2[2])
						* (a2[2] + gd2[1] * o2[0] - gd2[0] * o2[1] + g2[0] * (g2[0] * o2[2] - g2[2] * o2[0])
								+ g2[1] * (g2[1] * o2[2] - g2[2] * o2[1]))
				+ 2 * (g2[1]
						* g2[1] + g2[2]
								* g2[2])
						* (gd2[1] * o2[2] - gd1[2] * o2[1]
								- a2[0] + g2[1]
										* (g2[0] * o2[1]
												- g2[1] * o2[0])
								+ g2[2] * (g2[0]
										* o2[2] - g2[2]
												* o2[0])))
				/ (2 * Math
						.sqrt(Math
								.pow((gd2[1] * o2[2] - gd1[2] * o2[1] - a2[0]
										+ g2[1] * (g2[0] * o2[1] - g2[1] * o2[0]) + g2[2]
												* (g2[0] * o2[2] - g2[2] * o2[0])),
										2.0)
								+ Math.pow((a2[1] - gd1[2] * o2[0] + gd2[0] * o2[2]
										+ g2[0] * (g2[0] * o2[1] - g2[1] * o2[0])
										- g2[2] * (g2[1] * o2[2] - g2[2] * o2[1])), 2.0)
						+ Math.pow((a2[2] + gd2[1] * o2[0] - gd2[0] * o2[1] + g2[0] * (g2[0] * o2[2] - g2[2] * o2[0])
								+ g2[1] * (g2[1] * o2[2] - g2[2] * o2[1])), 2.0)));

		result[4] = (2 * (gd1[2] - g2[0] * g2[1])
				* (gd2[1] * o2[2] - gd1[2] * o2[1] - a2[0] + g2[1] * (g2[0] * o2[1] - g2[1] * o2[0])
						+ g2[2] * (g2[0] * o2[2] - g2[2] * o2[0]))
				- 2 * (g2[0] * g2[0] + g2[2] * g2[2])
						* (a2[1] - gd1[2] * o2[0] + gd2[0] * o2[2] + g2[0] * (g2[0] * o2[1] - g2[1] * o2[0])
								- g2[2] * (g2[1] * o2[2] - g2[2] * o2[1]))
				+ 2 * (gd2[0]
						+ g2[1] * g2[2]) * (a2[2]
								+ gd2[1] * o2[0] - gd2[0]
										* o2[1]
								+ g2[0] * (g2[0] * o2[2] - g2[2] * o2[0]) + g2[1]
										* (g2[1] * o2[2]
												- g2[2] * o2[1])))
				/ (2 * Math
						.sqrt(Math
								.pow((gd2[1] * o2[2] - gd1[2] * o2[1] - a2[0]
										+ g2[1] * (g2[0] * o2[1] - g2[1] * o2[0]) + g2[2]
												* (g2[0] * o2[2] - g2[2] * o2[0])),
										2.0)
								+ Math.pow((a2[1] - gd1[2] * o2[0] + gd2[0] * o2[2]
										+ g2[0] * (g2[0] * o2[1] - g2[1] * o2[0])
										- g2[2] * (g2[1] * o2[2] - g2[2] * o2[1])), 2.0)
						+ Math.pow((a2[2] + gd2[1] * o2[0] - gd2[0] * o2[1] + g2[0] * (g2[0] * o2[2] - g2[2] * o2[0])
								+ g2[1] * (g2[1] * o2[2] - g2[2] * o2[1])), 2.0)));

		result[5] = -(2 * (gd2[1] + g2[0] * g2[2])
				* (gd2[1] * o2[2] - gd1[2] * o2[1] - a2[0] + g2[1] * (g2[0] * o2[1] - g2[1] * o2[0])
						+ g2[2] * (g2[0] * o2[2] - g2[2] * o2[0]))
				+ 2 * (g2[0] * g2[0] + g2[1] * g2[1])
						* (a2[2] + gd2[1] * o2[0] - gd2[0] * o2[1] + g2[0] * (g2[0] * o2[2] - g2[2] * o2[0])
								+ g2[1] * (g2[1] * o2[2] - g2[2] * o2[1]))
				+ 2 * (gd2[0]
						- g2[1] * g2[2]) * (a2[1]
								- gd1[2] * o2[0] + gd2[0]
										* o2[2]
								+ g2[0] * (g2[0] * o2[1] - g2[1] * o2[0]) - g2[2]
										* (g2[1] * o2[2]
												- g2[2] * o2[1])))
				/ (2 * Math
						.sqrt(Math
								.pow((gd2[1] * o2[2] - gd1[2] * o2[1] - a2[0]
										+ g2[1] * (g2[0] * o2[1] - g2[1] * o2[0]) + g2[2]
												* (g2[0] * o2[2] - g2[2] * o2[0])),
										2.0)
								+ Math.pow((a2[1] - gd1[2] * o2[0] + gd2[0] * o2[2]
										+ g2[0] * (g2[0] * o2[1] - g2[1] * o2[0])
										- g2[2] * (g2[1] * o2[2] - g2[2] * o2[1])), 2.0)
						+ Math.pow((a2[2] + gd2[1] * o2[0] - gd2[0] * o2[1] + g2[0] * (g2[0] * o2[2] - g2[2] * o2[0])
								+ g2[1] * (g2[1] * o2[2] - g2[2] * o2[1])), 2.0)));

		return result;
	}

}
