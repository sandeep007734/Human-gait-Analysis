package GaitAnalysis;

import java.util.List;

public class SensorData {

	Integer sizeOfData;
	private Integer deltaT = 2;

	double[][] gyro_s_derv_data;
	double[][] acc;
	double[][] gyro;
	double[][] quaternion;
	double[] timestamp;

	public SensorData(Integer sizeOfData) {
		gyro_s_derv_data = new double[sizeOfData - (4 * deltaT)][3];
		acc = new double[sizeOfData - (4 * deltaT)][3];
		gyro = new double[sizeOfData - (4 * deltaT)][3];
		quaternion = new double[sizeOfData - (4 * deltaT)][4];
		timestamp = new double[sizeOfData - (4 * deltaT)];
	}

	public SensorData() {
		// TODO Auto-generated constructor stub
	}

	// Sorting the Calib Data
	void loadData(List<String> data, Integer _sizeOfData) {

		sizeOfData = _sizeOfData;

		double[] timestamp_s = new double[sizeOfData];
		double[][] acc_s = new double[sizeOfData][3];
		double[][] gyro_s = new double[sizeOfData][3];
		double[][] quarternion_s = new double[sizeOfData][4];

		// remove the line with the title.
		data.remove(0);
		sizeOfData--;
		data.remove(0);
		sizeOfData--;
	

		int idx = 0;
		for (String line : data) {
			//System.out.println(line);
			String[] arr = line.split(",", 20);
//			System.out.println("loop: "+line);
			timestamp_s[idx] = Double.parseDouble(arr[0]);

			// ===================================================================================
			for (int j = 0; j < 3; j++) {
//				System.out.println("acc: "+arr[2 + j]);
				acc_s[idx][j] = Double.parseDouble(arr[2 + j]);
//				System.out.println("read Acc: "+acc_s[idx][j]);
				acc_s[idx][j] = (156.91) * (acc_s[idx][j]) / 32768;
				gyro_s[idx][j] = Double.parseDouble(arr[5 + j]);
				gyro_s[idx][j] = (
						(1000 * 
								(gyro_s[idx][j] / 32768))
						* (3.14 / 180));
			}
			for (int k = 0; k < 4; k++) {
				quarternion_s[idx][k] = Double.parseDouble(arr[11 + k]) / 16384;
			}

			idx++;
		}
//		acc = acc_s;

		//========================================================================

		Integer startIdx = (1 + 2 * deltaT) - 1; // 4
		Integer endIdx = sizeOfData - (2 * deltaT) - 1; // 1107
		
		sizeOfData =  endIdx - startIdx+1;

		for (int i = startIdx; i <= endIdx; i++) {
			for (int j = 0; j < 3; j++) {
				gyro_s_derv_data[i - startIdx][j] = (gyro_s[i - 2 * deltaT][j] - (8 * gyro_s[i - deltaT][j])
						+ (8 * gyro_s[i + 2*deltaT][j]) - gyro_s[i + 2 * deltaT][j]) / (12.0 * deltaT);
				
				gyro[i - startIdx][j] = gyro_s[i][j];
				timestamp[i - startIdx] = timestamp_s[i - startIdx];

			}
			
			for (int k = 0; k < 4; k++) {
				quaternion[i - startIdx][k] = quarternion_s[i][k];
			}
			
			// =============
			acc[i - startIdx] = acc_s[i];
		
		}
		
		
		

	}

}
