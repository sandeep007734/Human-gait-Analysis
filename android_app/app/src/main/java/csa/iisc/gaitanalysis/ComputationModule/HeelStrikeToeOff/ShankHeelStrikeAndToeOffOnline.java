package csa.iisc.gaitanalysis.ComputationModule.HeelStrikeToeOff;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 24/5/16.
 */

public class ShankHeelStrikeAndToeOffOnline {

    private static final String TAG = "ShankHeelStrikeToeOff";
    private static HeelStrikeAndToeOffDetectionListener heelStrikeAndToeOffDetectionListener;

    public static void setHeelStrikeAndToeOffDetectionListener(HeelStrikeAndToeOffDetectionListener heelStrikeAndToeOffDetectionListener) {
        ShankHeelStrikeAndToeOffOnline.heelStrikeAndToeOffDetectionListener = heelStrikeAndToeOffDetectionListener;
    }

    private static int MAXHS = 3;
    private static int MAXTO = 2;

    static int freq = 100;
    static int gyroT = 5;
    static double maxPeakHeight = gyroT;
    static double minPeakHeight = Math.ceil(maxPeakHeight*0.2);
    static double minPeakDistance = Math.ceil(freq/5);


    public static void calcHSandTO(double[] inputData){
        Log.i(TAG, "calcHSandTO: Calculating Heel Strike and Toe Off. Size of Input Data: "+inputData.length);
        Log.i(TAG, "calcHSandTO: First Three elements are as follow: "+inputData[0]+" "+inputData[1]+" "+inputData[2]);
        Log.i(TAG, "calcHSandTO: maxPeakHeight minPeakHeight minPeakDistance"+" "+maxPeakHeight+" "+minPeakHeight+" "+minPeakDistance);

        int sizeOfData  = inputData.length;
        List<Integer> HSShank = new ArrayList<>();
        List<Integer> TOShank = new ArrayList<>();

        double prev = 0,curr;

        for(int ti=0;ti<sizeOfData;ti++){
            curr = -1*inputData[ti];
            onlineHSandTOShankAlgo(curr, prev, ti);
            if(isEvent==1){
                if(isHS==1){
                    if(HSShank.size() < MAXHS) {
                        HSShank.add(ti - 1);
                        if (heelStrikeAndToeOffDetectionListener != null) {
                            heelStrikeAndToeOffDetectionListener.onHeelStrikeDetected(ti - 1);
                        }
                    }
                }else if(isTO==1){
                    if(TOShank.size()< MAXTO && HSShank.size()!=0){
                        TOShank.add(ti-1);
                        if(heelStrikeAndToeOffDetectionListener!=null){
                            heelStrikeAndToeOffDetectionListener.onToeOffDetected(ti-1);
                        }
                    }
                }
                isEvent=0;
            }

            if(HSShank.size()==MAXHS && TOShank.size()==MAXTO)
                break;

            prev=curr;


        }
    }
    private static int isEvent = 0;
    private static int isIncreasing=0;
    private static int isHS=0;
    private static int isTO=0;
    private static int valsAllowed=0;
    private static int haveWeSeenPeak=0;
    private static double isNearby=0;
    private static int isHSv;
    private static int isTOv;

    private static void onlineHSandTOShankAlgo(double curr, double prev, int ti){
     if(ti==0){
         isHSv = isHS;
         isTOv = isTO;

         return;
     }

        if(isNearby>0)
            isNearby = isNearby-1;
        if(curr>prev)
            isIncreasing=1;

        if(curr<prev && prev>=minPeakHeight && (isIncreasing==1) && (isNearby==0) && ((isHS==1)||(isTO==1))){
            isIncreasing=0;
            isNearby = minPeakDistance;
            if(haveWeSeenPeak==1){
                if(valsAllowed>0){
                    isEvent=1;
                    if(isHS==1){
                        isTO=1;
                        isHS=0;
                    }else if (isTO==1){
                        isHS=1;
                        isTO=0;
                    }

                    isHSv = isHS;
                    isTOv = isTO;
                    valsAllowed = valsAllowed-1;
                    return;
                }else{
                    isEvent=0;
                    isHSv=0;
                    isTOv=0;

                    valsAllowed=2;
                    return;
                }
            }
        }

        if(-prev < maxPeakHeight && (haveWeSeenPeak==0))
            haveWeSeenPeak=1;

        if(haveWeSeenPeak==1){
            if(-curr > maxPeakHeight && isHS==0 && isTO==0){
                isTO=1;
                valsAllowed=1;
            }
        }

        isHSv=0;
        isTOv=0;

    }

    public interface HeelStrikeAndToeOffDetectionListener{
        void onHeelStrikeDetected(int pos);
        void onToeOffDetected(int pos);
    }

}
