package csa.iisc.gaitanalysis.ComputationModule.KneeFlexionExtension;


import android.util.Log;

import csa.iisc.gaitanalysis.ComputationModule.Utils.ComputationUtils;

public class CalculateFlexionExtensionAngle {

    private static final String TAG = "KneeFE Calc Module";

    public double calcAngle(double[] j1_valf, double[] j2_valf, double prevAngle, double[] gyro_s_thigh, double[] gyro_s_shank) {

        double currAngle = 0;



//        if (j1_valf[0] < 0) {
//            System.out.println("Makig Changes j1_valf");
//            for (int i = 0; i < 3; i++) {
//                j1_valf[i] = j1_valf[i] * (-1);
//            }
//        }
//
//        if (j2_valf[0] < 0) {
//            System.out.println("Makig Changes j2_valf");
//            for (int i = 0; i < 3; i++) {
//                j2_valf[i] = j2_valf[i] * (-1);
//            }
//        }
        currAngle = prevAngle + ComputationUtils.radToDeg((ComputationUtils.dotProduct(gyro_s_thigh, j1_valf) - ComputationUtils.dotProduct(gyro_s_shank, j2_valf)) * 0.01);

        return currAngle;

    }



}
