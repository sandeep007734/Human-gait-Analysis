package csa.iisc.gaitanalysis.ComputationModule.Utils;

import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by root on 25/5/16.
 */

public class CustomFindPeaks {

    static List<Double> cusPeakVal;
    static List<Integer> cusPeakIdx;

    public static List<Double> getCusPeakVal() {
        return cusPeakVal;
    }

    public static List<Integer> getCusPeakIdx() {
        return cusPeakIdx;
    }

    private static final String TAG = "Custom Find Peaks";

    public static void findPeaks(double[] inpData, double minPeakHeight, double minPeakDistance){
        Log.i(TAG, "findPeaks: Finding Peaks, minPeakHeight: "+minPeakHeight+" minPeakDistance: "+minPeakDistance);
        Log.i(TAG, "findPeaks: Max: "+ComputationUtils.max(inpData)+" min: "+ComputationUtils.min(inpData));

        cusPeakVal = new ArrayList<>();
        cusPeakIdx = new ArrayList<>();

        List<Double> peakVal = new ArrayList<>();
        List<Integer> peakIdx = new ArrayList<>();

        int needChange = 0;

        int sizeOfData = inpData.length;

        double prev = ComputationUtils.max(inpData)-10;
        int isIncreasing  = 0;

        for(int ti=0;ti<sizeOfData;ti++){
            double curr = inpData[ti];
            if(curr > prev)
                isIncreasing=1;

            if(curr< prev && prev >= minPeakHeight && isIncreasing==1){
               peakVal.add(prev);
                peakIdx.add(ti-1);

                isIncreasing=0;
            }

            prev = curr;
        }

        Log.i(TAG, "findPeaks: Total Unfiltered Peaks: "+peakIdx.size());



        while(true){

           if(ComputationUtils.sum(peakVal)==0)
               break;

            double mVal = ComputationUtils.max(peakVal);
            int mIdx = peakVal.indexOf(mVal);
            mIdx = peakIdx.get(mIdx);

            cusPeakIdx.add(mIdx);

            List<Integer> nearby = new ArrayList<>();
            for(Integer val: peakIdx){
                if(val >= (mIdx-minPeakDistance) && val<=(mIdx+minPeakDistance)){
                    nearby.add(peakIdx.indexOf(val));
                }
            }

            if(nearby.size()==0)
                break;

            for(int ni=0;ni<nearby.size();ni++){
                int tmp = peakIdx.get(nearby.get(ni));
                nearby.remove(ni);
                nearby.add(ni,tmp);
            }

            List<Integer> c = ComputationUtils.ismember(peakIdx, nearby);

            for(int ci=0;ci<c.size();ci++){
                if(c.get(ci)==1){
                    peakVal.remove(ci);
                    peakVal.add(ci,0.0);
                }
            }

        }

            Collections.sort(cusPeakIdx);
            cusPeakVal.clear();

            for(Integer idx : cusPeakIdx){
                cusPeakVal.add(inpData[idx]);
               // Log.i(TAG, "findPeaks: Peaks idx: "+idx+" val "+inpData[idx]);
            }
    }
}
