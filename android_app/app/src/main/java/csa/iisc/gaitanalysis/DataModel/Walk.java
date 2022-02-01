package csa.iisc.gaitanalysis.DataModel;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import csa.iisc.gaitanalysis.ComputationModule.GaitAnalysis.ReadData;
import csa.iisc.gaitanalysis.ComputationModule.GaitAnalysis.SensorData;
import csa.iisc.gaitanalysis.ComputationModule.KneeFlexionExtension.CalculateFlexionExtensionAngle;
import csa.iisc.gaitanalysis.Utils.Utils;
import csa.iisc.gaitanalysis.Utils.UtilsData;

/**
 * Created by root on 15/3/16.
 */
public class Walk {


    private List<String> walkFiles;
    private WalkChangeListener walkChangeListener;
    private static final String TAG = "Walk";



    public SensorData readData(GlobalConstants.SensorPosition sensorPosition, String expAbsPath) {
        List<String> dataTxt = null;
        try {
            switch (sensorPosition){

                case RIGHTTHIGH:
                    dataTxt = (new ReadData()).readData(expAbsPath + "/" + walkFiles.get(0));
                    break;
                case RIGHTSHANK:
                    dataTxt = (new ReadData()).readData(expAbsPath + "/" +walkFiles.get(1));
                    break;
                case RIGHTFOOT:
                    dataTxt = (new ReadData()).readData(expAbsPath + "/" +walkFiles.get(2));
                    break;
                case LEFTTHIGH:
                    break;
                case LEFTSHANK:
                    break;
                case LEFTFOOT:
                    break;
                case UNKNOWN:
                    dataTxt=null;
                    break;
            }

            if(dataTxt==null){
                return null;
            }

            SensorData cDataSensor = new SensorData();
            cDataSensor.loadData(dataTxt, dataTxt.size());

            return cDataSensor;
        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;

    }

    public void addWalkFile(String walkFile) {
        if (walkFiles == null)
            walkFiles = new ArrayList<>();

        walkFiles.add(walkFile);
    }

    public Integer getWalkCount() {
        if (walkFiles != null) {
            return walkFiles.size();
        }
        return 0;
    }

    public List<String> getWalkFiles() {
        return walkFiles;
    }

    public void setWalkFiles(List<String> walkFiles) {
        this.walkFiles = walkFiles;
    }

    public void calAngles(final GlobalConstants.WalkResultType walkResultType, final String expAbsPath, final double[] j1_valf, final double[] j2_valf) {

        //TODO remove this custom changes, the value of j1_valf and j2_valf
        //TODO Test1: Using this value makes the KneeFE work correctly. Please fix the calibration code.
        //TODO using these values for all the experiments is actually better than using the wrong code.

//        j1_valf[0] = 0.8649;
//        j1_valf[1] = 0.1706;
//        j1_valf[2] = -0.4720;
//
//        j2_valf[0] = 0.9919;
//        j2_valf[1] = 0.0027;
//        j2_valf[2] = -0.1268;

        Log.i(TAG, "calcAngle: Correcting the orientation of the Joint Axis vectors");

        if (j1_valf[0] < 0)
            j1_valf[0] *= -1;
        if (j1_valf[1] < 0)
            j1_valf[1] *= -1;
        if (j1_valf[2] > 0)
            j1_valf[2] *= -1;

        if (j2_valf[0] < 0)
            j2_valf[0] *= -1;
        if (j2_valf[1] > 0)
            j2_valf[1] *= -1;
        if (j2_valf[2] > 0)
            j2_valf[2] *= -1;


        Thread runAngle = new Thread(new Runnable() {
            @Override
            public void run() {
                String TAG = "DataUtils";
                WalkAngleResult angleResult = new WalkAngleResult(walkResultType);
                List<Integer> fileIdx = new ArrayList<>();
                switch (walkResultType) {

                    case RightKneeFlexionExtensionAngle:
                        fileIdx = UtilsData.isItPossibleToCalc(walkResultType, getWalkFiles());
                        if (fileIdx == null || fileIdx.size() < 2) {
                            Log.d("Walk", "calAngles: Not Possible to Calculate");
                            return;
                        }
                        break;
                    case RightAnkleFlexionExtensionAngle:
                        fileIdx = UtilsData.isItPossibleToCalc(walkResultType, getWalkFiles());
                        if (fileIdx == null || fileIdx.size() < 2) {
                            Log.d("Walk", "calAngles: Not Possible to Calculate");
                            return;
                        }
                        break;
                    case RightHeeslStrike:
                        break;
                    case RightToeOff:
                        break;
                }
                if (fileIdx.size() != 2) {
                    Log.d(TAG, "run: Filesize is not correct.");
                    return;
                }

                try {
                    String upperJointFile = expAbsPath + "/" + getWalkFiles().get(fileIdx.get(0));
                    String lowerJointFile = expAbsPath + "/" + getWalkFiles().get(fileIdx.get(1));


                    List<String> upperJointData = (new ReadData()).readData(upperJointFile);
                    List<String> lowerJointData = (new ReadData()).readData(lowerJointFile);

                    Integer diff = Math.abs(upperJointData.size() - lowerJointData.size());
                    if (upperJointData.size() > lowerJointData.size()) {
                        for (int i = 0; i < diff; i++) {
                            upperJointData.remove(upperJointData.size() - 1);
                        }
                    } else {
                        for (int i = 0; i < diff; i++) {
                            lowerJointData.remove(upperJointData.size() - 1);
                        }
                    }
                    SensorData cUpperJoint = null, cLowerJoint = null;

                    cUpperJoint = new SensorData();
                    cUpperJoint.loadData(upperJointData, upperJointData.size());

                    cLowerJoint = new SensorData();
                    cLowerJoint.loadData(lowerJointData, upperJointData.size());

                    CalculateFlexionExtensionAngle calculateFlexionExtensionAngle = new CalculateFlexionExtensionAngle();


                    double prevAngle = 0;
                    for (int di = 1; di < cUpperJoint.sizeOfData; di++) {
                        double[] gyro_s_thigh_val = cUpperJoint.gyro[di];
                        double[] gyro_s_shank_val = cLowerJoint.gyro[di];

                        double currAngle = calculateFlexionExtensionAngle.calcAngle(j1_valf, j2_valf,
                                prevAngle, gyro_s_thigh_val, gyro_s_shank_val);
                        if (walkChangeListener != null)
                            walkChangeListener.onSingleAnglePointCalculation(currAngle, walkResultType);
                        angleResult.addAngle(currAngle);

                        prevAngle = currAngle;
                    }

                    if (walkChangeListener != null)
                        walkChangeListener.onAngleCalculationDone();
                } catch (Exception e) {

                }
            }
        });

        runAngle.start();

    }

    public void setWalkChangeListener(WalkChangeListener walkChangeListener) {
        this.walkChangeListener = walkChangeListener;
    }

    public void removeWallFiles(String expAbsPath) {
        for (String fileName : walkFiles) {
            Utils.deleteFile(expAbsPath + "/" + fileName);
        }
    }

    public interface WalkChangeListener {
        void onAngleCalculationDone();

        void onSingleAnglePointCalculation(double angleVal, GlobalConstants.WalkResultType walkResultType);
    }
}
