package Calibration;

import java.util.ArrayList;
import java.util.List;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import Jama.Matrix;
import Utils.Utils;

//import dataModels.FourDimVector;
//import dataModels.ThreeDimVector;

import java.lang.Math;

public class CalibrationMainDriverCode  {

	final static double pi = 3.1416;
	double phi1, phi2, theta1, theta2;
	double[] j1, j2;

	double g1x, g2x, g1y, g2y, g1z, g2z;
	double[][] g1, g2, l1, l2;

	public void mainLoop(int numOfIterations, int sizeOfData, double[][] gyro_s_thigh, double[][] gyro_s_shank,
			double[][] acc_s_thigh, double[][] acc_s_shank, double[][] gyro_s_derv_thigh,
			double[][] gyro_s_derv_shank) {
//
//		 CalibrationPrimaryLoop.runLoop(numOfIterations, sizeOfData,
//		 gyro_s_thigh, gyro_s_shank, acc_s_thigh, acc_s_shank, gyro_s_derv_thigh,
//		 gyro_s_derv_shank);

		double[] j1_valf = { 0.8915101923504168, 0.011273280733363564, 0.4528603427954688 };
		double[] j2_valf = { 0.8657219777129791, 0.4877391620711688, 0.11240981757320966 };

		System.out.println();
	

//		 CalibrationSecondaryLoop.runLoop(numOfIterations, sizeOfData,
//		 gyro_s_thigh, gyro_s_shank, acc_s_thigh,
//		 acc_s_shank, gyro_s_derv_thigh, gyro_s_derv_shank, j1_valf, j2_valf);

		

		// double[] o1={1,.8,1};
		// double[][] g1={{.0021, -0.0016, .0080}};
		// double[][] gd1={{ -0.0063, 0.0148, -0.0401 }};
		// double[][] o2={{1,.9,.5}};
		// double[][] g2={{ -0.0048,-0.0032,-0.0048}};
		// double[][] gd2={{ 0.0165, -0.0076, 0.0035}};
		// double[][] a1={{ 0.1197,-9.6776, 0.0096}};
		// double[][] a2={{ 0.4836, -9.5627, -0.7901}};
		//
		// CalibrationSecondaryLoop.runLoop(2, 1, g1, g2, a1, a2, gd1,
		// gd2, CalibrationPrimaryLoop.j1_valf, CalibrationPrimaryLoop.j2_valf);

		// double ans[] = CalibrationSecondaryLoop.calcJacbo(g1, o1, a1, gd1,
		// g2, o2, a2, gd2);
		// Utils.printArray(ans);
		//
		// System.out.println(CalibrationSecondaryLoop.calcEo(g1, o1, a1, gd1,
		// g2, o2, a2, gd2));

		// CalibrationSecondaryLoop.runLoop(2, sizeOfData, gyro_s_thigh,
		// gyro_s_shank, acc_s_thigh, acc_s_shank, gyro_s_derv_thigh,
		// gyro_s_derv_shank, CalibrationPrimaryLoop.j1_valf,
		// CalibrationPrimaryLoop.j2_valf);
		
	
	}
	
	 
}
