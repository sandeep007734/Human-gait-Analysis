package csa.iisc.gaitanalysis.ComputationModule.Utils;

import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ComputationUtils {

    private static final String TAG = "ComputationUtils";

    public static void printArray(double[] inp) {
        for (int ki = 0; ki < inp.length; ki++) {
            System.out.print(inp[ki] + " ");
        }
        System.out.println();
    }

    public static void printArray(Double[] inp) {
        for (int ki = 0; ki < inp.length; ki++) {
            System.out.print(inp[ki] + " ");
        }
        System.out.println();
    }

    public static double dotProduct(double[] a, double[] b) {
        double result = 0.0;
        for (int i = 0; i < a.length; i++) {
            result += a[i] * b[i];
        }

        return result;
    }

    public static double radToDeg(double radAngle) {
        return (radAngle * 180) / 3.14;
    }

    public static double min(double[] inpData){
        double minVal;

        if(inpData.length==0)
            return 0;

        minVal = inpData[0];

        for(int i=0;i<inpData.length;i++){
            if(inpData[i] < minVal){
                minVal = inpData[i];
            }
        }

        return minVal;
    }

    public static double min(List<Double> inpData){
        double minVal;

        if(inpData.size()==0) {
            Log.e(TAG, "min: Error, Size is 0");
            return 0;
        }

        minVal = inpData.get(0);

        for(int i=0;i<inpData.size();i++){
            if(inpData.get(i) < minVal){
                minVal = inpData.get(i);
            }
        }

        return minVal;
    }

    public static double max(double[] inpData){
        double maxVal;

        if(inpData.length==0)
            return 0;

        maxVal = inpData[0];

        for(int i=0;i<inpData.length;i++){
            if(inpData[i] > maxVal){
                maxVal = inpData[i];
            }
        }

        return maxVal;
    }

    public static double max(List<Double> inpData){
        double maxVal;

        if(inpData.size()==0)
            return 0;

        maxVal = inpData.get(0);

        for(int i=0;i<inpData.size();i++){
            if(inpData.get(i) > maxVal){
                maxVal = inpData.get(i);
            }
        }

        return maxVal;
    }

    public static double sum(double[] inpData){
        double sumVal=0;

        if(inpData.length==0)
            return 0;

        for(int i=0;i<inpData.length;i++){
           sumVal+=inpData[i];
        }

        return sumVal;
    }

    public static double sum(List<Double> inpData){
        double sumVal=0;

        if(inpData.size()==0)
            return 0;

        for(int i=0;i<inpData.size();i++){
            sumVal+=inpData.get(i);
        }

        return sumVal;
    }

    public static List<Integer> ismember(List<Integer> listA, List<Integer> listB){
        List<Integer> isMem = new ArrayList<>();
        for (Integer val: listA){
            if(listB.contains(val)){
                isMem.add(1);
            }else {
                isMem.add(0);
            }
        }

        return isMem;
    }

}
