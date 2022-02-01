package csa.iisc.gaitanalysis.ComputationModule.HeelStrikeToeOff;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import csa.iisc.gaitanalysis.ComputationModule.Utils.ComputationUtils;
import csa.iisc.gaitanalysis.ComputationModule.Utils.CustomFindPeaks;

/**
 * Created by root on 24/5/16.
 */

public class ShankHeelStrikeAndToeOffOffline {

    private static final String TAG = "ShankHeelStrikeToeOff";
    private static HeelStrikeAndToeOffDetectionListener heelStrikeAndToeOffDetectionListener;

    public static void setHeelStrikeAndToeOffDetectionListener(HeelStrikeAndToeOffDetectionListener heelStrikeAndToeOffDetectionListener) {
        ShankHeelStrikeAndToeOffOffline.heelStrikeAndToeOffDetectionListener = heelStrikeAndToeOffDetectionListener;
    }

    private static int MAXHS = 3;
    private static int MAXTO = 2;

    static int freq = 100;
    static int gyroT = 5;
    static double maxPeakHeight = gyroT;
    static double minPeakHeight = Math.ceil(maxPeakHeight*0.2);
    static double minPeakDistance = 1;

    public static List<Integer> HSShank, TOShank;

    public static void calcHSandTO(double[] inputData){
        freq=100;

        double minVal;

        gyroT = 2;
        minPeakHeight = gyroT;
        minPeakDistance = Math.ceil(freq/6);

        CustomFindPeaks.findPeaks(inputData, minPeakHeight, minPeakDistance);
        List<Double> posPeaks = CustomFindPeaks.getCusPeakVal();
        List<Integer> posPeaksIdx = CustomFindPeaks.getCusPeakIdx();

        Log.i(TAG, "calcHSandTO: Total Peaks : "+posPeaksIdx.size());

        minPeakHeight = Math.ceil(gyroT*0.2);
        minPeakDistance=1;

        double[] negInput = new double[inputData.length];
        for(int ip=0;ip<inputData.length;ip++){
            negInput[ip] = -1*inputData[ip];
        }

        CustomFindPeaks.findPeaks(negInput, minPeakHeight, minPeakDistance);
        List<Double> InvposPeaks = CustomFindPeaks.getCusPeakVal();
        List<Integer> InvposPeaksIdx = CustomFindPeaks.getCusPeakIdx();

        Log.i(TAG, "calcHSandTO: Total Negative Peaks : "+InvposPeaksIdx.size());

        HSShank = new ArrayList<>();
        TOShank = new ArrayList<>();

        for(int i=0;i<posPeaksIdx.size()-1;i++){
            int val1 = posPeaksIdx.get(i);
            int val2 = posPeaksIdx.get(i+1);


            List<Double> tmpArr = new ArrayList<>();

            for(int v = val1;v<=val2;v++){
                tmpArr.add(inputData[v]);
            }

             minVal = ComputationUtils.min(tmpArr);
            int minValIdx = tmpArr.indexOf(minVal);

            minValIdx +=val1;

            TOShank.add(minValIdx);

        }

        for(int i=0;i<posPeaksIdx.size();i++){
            int val = posPeaksIdx.get(i);
            int nextMinIdx =0;
            for(int j=0;j<InvposPeaksIdx.size();j++){
                if(InvposPeaksIdx.get(j) >= val){
                    nextMinIdx=InvposPeaksIdx.get(j);
                    break;
                }
            }
            HSShank.add(nextMinIdx);
        }

        Log.i(TAG, "calcHSandTO: Total HS: "+HSShank.size());
        Log.i(TAG, "calcHSandTO: Total Toe Off: "+TOShank.size());

        if(HSShank.size()>1)
        HSShank.remove(0);
        if(TOShank.size()>1)
        TOShank.remove(0);

        while(HSShank.size() > MAXHS){
            HSShank.remove(HSShank.size()-1);
        }

        while(TOShank.size() > MAXTO){
            TOShank.remove(TOShank.size()-1);
        }

    }

    public interface HeelStrikeAndToeOffDetectionListener{
        void onHeelStrikeDetected(int pos);
        void onToeOffDetected(int pos);
    }

}
