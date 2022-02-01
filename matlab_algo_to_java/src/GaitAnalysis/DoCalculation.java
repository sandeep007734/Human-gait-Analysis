package GaitAnalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jfree.ui.RefineryUtilities;

import Calibration.CalibrationMainDriverCode;
import KneeFlexionExtension.CalculateKneeFlexionExtensionAngle;
import Utils.Utils;

public class DoCalculation {

	private static String dataFolder = "/home/sandeep/IMU/EXL_IMU/Experiments_BIMRA/Experiments_BIMRA_2/";
	private static String imuFolder = "Sofia7/";
	private static String bimraFolder = "3dSofia/";
	private static String[] calibFile = { "S0_0018.txt", "S1_0018.txt" };
	private static String calibResultFile = imuFolder + "calibration_onePlane_java.mat";

	private static String[] dataFileThigh = { "S0_0018.txt", "S0_0019.txt", "S0_0020.txt", "S0_0021.txt", "S0_0022.txt",
			"S0_0023.txt", "S0_0024.txt" };
	private static String[] dataFileShank = { "S1_0018.txt", "S1_0019.txt", "S1_0020.txt", "S1_0021.txt", "S1_0022.txt",
			"S1_0023.txt", "S1_0024.txt" };

	private static String[] bimraFile = { "0185~aa~Walking~03.mdx", "0185~aa~Walking~04.mdx", "0185~aa~Walking~05.mdx",
			"0185~aa~Walking~06.mdx", "0185~aa~Walking~07.mdx", "0185~aa~Walking~08.mdx", "0185~aa~Walking~09.mdx" };

	public static void main(String[] args) throws FileNotFoundException, IOException {
		String calibFile1 = dataFolder + imuFolder + calibFile[0];
		String calibFile2 = dataFolder + imuFolder + calibFile[1];

		List<String> calibData1 = new ArrayList<>();
		List<String> calibData2 = new ArrayList<>();

		System.out.println("Calibration files are:\n" + calibFile1 + "\n" + calibFile2);

		try (BufferedReader br = new BufferedReader(new FileReader(new File(calibFile1)))) {
			String line;
			while ((line = br.readLine()) != null) {
				calibData1.add(line);
			}
		}

		try (BufferedReader br = new BufferedReader(new FileReader(new File(calibFile2)))) {
			String line;
			while ((line = br.readLine()) != null) {
				calibData2.add(line);
			}
		}

		System.out.println("Calib data loaded. Total Entries: " + calibData1.size());

		System.out.println("================================");

		int sizeOfData = calibData1.size();

		SensorData cDataThigh = new SensorData(sizeOfData);
		cDataThigh.loadData(calibData1, sizeOfData);

		sizeOfData = cDataThigh.sizeOfData;
		System.out.println("Total Entries after second order derivation calculation. " + sizeOfData);

		// ==================================================================

		sizeOfData = calibData2.size();

		SensorData cDataShank = new SensorData(sizeOfData);
		cDataShank.loadData(calibData2, sizeOfData);

		sizeOfData = cDataShank.sizeOfData;
		System.out.println("Total Entries after second order derivation calculation. " + cDataShank.sizeOfData);

		System.out.println("================================");

			
		// DO CALIBRATION
		CalibrationMainDriverCode calibrationMainLoop = new CalibrationMainDriverCode();
		int numOfIterations = 50;
		calibrationMainLoop.mainLoop(numOfIterations, sizeOfData, cDataThigh.gyro, cDataShank.gyro, cDataThigh.acc,
				cDataShank.acc, cDataThigh.gyro_s_derv_data, cDataShank.gyro_s_derv_data);
		
		// CALC ANgle
		
		CalculateKneeFlexionExtensionAngle calculateKneeFlexionExtensionAngle = new CalculateKneeFlexionExtensionAngle("Knee", "Knee");
		double[] j1_valf = { 0.8915101923504168, 0.011273280733363564, 0.4528603427954688 };
		double[] j2_valf = { 0.8657219777129791, 0.4877391620711688, 0.11240981757320966 };
		
		double prevAngle = 0;
		for(int i=1;i< cDataThigh.sizeOfData;i++){
			double[] gyro_s_thigh_val = cDataThigh.gyro[i];
			double[] gyro_s_shank_val = cDataShank.gyro[i];
			
			double currAngle = calculateKneeFlexionExtensionAngle.calcAngle(j1_valf, j2_valf, prevAngle, gyro_s_thigh_val, gyro_s_shank_val);
			System.out.println(i);
			Utils.printArray(gyro_s_thigh_val);
			Utils.printArray(gyro_s_shank_val);
			System.out.println(currAngle);
			System.out.println();
			
			calculateKneeFlexionExtensionAngle.angleSet.addValue(currAngle, "angle", Integer.toString(i));
			prevAngle = currAngle;
		}
		
		calculateKneeFlexionExtensionAngle.pack( );
	      RefineryUtilities.centerFrameOnScreen( calculateKneeFlexionExtensionAngle );
	      calculateKneeFlexionExtensionAngle.setVisible( true );
		
		

	}

}

