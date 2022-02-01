package csa.iisc.gaitanalysis.ComputationModule.GaitAnalysis;

import java.util.List;

public class SensorData {

    public Integer sizeOfData;
    public double[][] gyro_s_derv_data;
    public double[][] acc;
    public double[][] gyro;
    public double[][] quaternion;
    public double[] timestamp;
    private Integer deltaT = 2;

    // Sorting the Calib Data
    public void loadData(List<String> data, Integer _sizeOfData) {

//		System.out.println(_sizeOfData);
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


        sizeOfData = 0;
        int idx = 0;
        for (String line : data) {
//			System.out.println("Line is:"+line);
            sizeOfData++;
            String[] arr = line.split(",", 20);
//			System.out.println("loop: "+line);
            timestamp_s[idx] = Double.parseDouble(arr[0]);
            // ===================================================================================
            for (int j = 0; j < 3; j++) {
                acc_s[idx][j] = Double.parseDouble(arr[2 + j]);
                acc_s[idx][j] = (19.613) * (acc_s[idx][j]) / 32768;
                gyro_s[idx][j] = Double.parseDouble(arr[5 + j]);
                gyro_s[idx][j] = (
                        (250 *
                                (gyro_s[idx][j] / 32768))
                                * (3.14 / 180));

            }
            for (int k = 0; k < 4; k++) {
                quarternion_s[idx][k] = Double.parseDouble(arr[11 + k]) / 16384;
            }

            idx++;
        }

//		System.out.println("Zero idx: "+gyro_s[0][0]);
//		System.out.println("New size of Data is: "+sizeOfData);

        gyro_s_derv_data = new double[sizeOfData - (4 * deltaT)][3];

        //========================================================================

        Integer startIdx = (1 + 2 * deltaT) - 1; // 4
        Integer endIdx = sizeOfData - (2 * deltaT) - 1; // 1107
//		System.out.println("Startidx: "+startIdx+" endIdx: "+ endIdx);
        sizeOfData = endIdx - startIdx + 1;

        timestamp = new double[sizeOfData];
        acc = new double[sizeOfData][3];
        gyro = new double[sizeOfData][3];
        quaternion = new double[sizeOfData][4];

        for (int i = startIdx; i <= endIdx; i++) {
            for (int j = 0; j < 3; j++) {
//				System.out.println(gyro_s_derv_data[i - startIdx][j]);
                gyro_s_derv_data[i - startIdx][j] = (gyro_s[i - 2 * deltaT][j] - (
                        8 * gyro_s[i - deltaT][j]
                )
                        + (8 * gyro_s[i + 2 * deltaT][j])
                        - gyro_s[i + 2 * deltaT][j])
                        / (12.0 * deltaT);
//
//				System.out.println(gyro[i - startIdx][j]);

//				System.out.println("idx: "+(i - startIdx)+" idx2(j): "+j+" i: "+i+" startIdx: "+startIdx);
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
