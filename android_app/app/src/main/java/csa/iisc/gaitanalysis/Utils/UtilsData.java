package csa.iisc.gaitanalysis.Utils;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import csa.iisc.gaitanalysis.DataModel.GlobalConstants.SensorPosition;
import csa.iisc.gaitanalysis.DataModel.GlobalConstants.WalkResultType;
import csa.iisc.gaitanalysis.DataModel.SensorRepo;

import static csa.iisc.gaitanalysis.DataModel.GlobalConstants.JointType;


public class UtilsData {

    private static final String TAG = "DataUtils";

    public static List<Integer> isItPossibleToCalc(WalkResultType walkResultType, List<String> filenames) {
        List<SensorPosition> reqPos = getRequiredSensors(walkResultType);
        if (reqPos.size() == 0) {
            Log.d(TAG, "isItPossibleToCalc: No Required Position Found");
            return null;
        }

        List<SensorPosition> availPos = new ArrayList<>();
        for (String file : filenames) {
            SensorPosition sensorPosition = getSensorPosition(file);
            availPos.add(sensorPosition);
        }

        if (availPos.size() == 0) {
            Log.d(TAG, "isItPossibleToCalc: No Sensor Available");
            return null;
        }

        List<Integer> fileIdx = new ArrayList<>();
        for (SensorPosition sensorPosition : reqPos) {
            if (!availPos.contains(sensorPosition))
                return null;
            else
                fileIdx.add(availPos.indexOf(sensorPosition));
        }

        return fileIdx;

    }

    public static List<String> getPossibleJointsFromConnectedSensorsStr() {

        List<SensorPosition> availSensorPos = SensorRepo.getAllSensorsPosition(true);
        List<String> result = new ArrayList<>();

        for (JointType jointType : JointType.values()) {
            List<SensorPosition> reqSensor = getRequiredSensorsJoints(jointType);
            boolean isPoss = true;
            for (SensorPosition sensorPosition : reqSensor) {
                if (!availSensorPos.contains(sensorPosition)) {
                    isPoss = false;
                    break;
                }
            }
            if (isPoss) {
                result.add(jointType.toString());
            }
        }

        return result;
    }

    public static List<SensorPosition> getRequiredSensorsJoints(JointType jointType) {
        List<SensorPosition> reqSensors = new ArrayList<>();
        switch (jointType) {
            case RightKnee:
                reqSensors.add(SensorPosition.RIGHTTHIGH);
                reqSensors.add(SensorPosition.RIGHTSHANK);
                break;
            case RightAnkle:
                reqSensors.add(SensorPosition.RIGHTSHANK);
                reqSensors.add(SensorPosition.RIGHTFOOT);
                break;
            case LeftKnee:
                reqSensors.add(SensorPosition.LEFTTHIGH);
                reqSensors.add(SensorPosition.LEFTSHANK);
                break;
            case LeftAnkle:
                reqSensors.add(SensorPosition.LEFTSHANK);
                reqSensors.add(SensorPosition.LEFTFOOT);
                break;
        }

        return reqSensors;
    }

    public static List<SensorPosition> getRequiredSensors(WalkResultType walkResultType) {
        List<SensorPosition> reqSensors = new ArrayList<>();
        switch (walkResultType) {
            case RightKneeFlexionExtensionAngle:
                reqSensors.add(SensorPosition.RIGHTTHIGH);
                reqSensors.add(SensorPosition.RIGHTSHANK);
                break;
            case RightAnkleFlexionExtensionAngle:
                reqSensors.add(SensorPosition.RIGHTSHANK);
                reqSensors.add(SensorPosition.RIGHTFOOT);
                break;
            case RightHeeslStrike:
                break;
            case RightToeOff:
                break;
            case LeftKneeFlexionExtensionAngle:
                reqSensors.add(SensorPosition.LEFTTHIGH);
                reqSensors.add(SensorPosition.LEFTSHANK);
                break;
            case LeftAnkleFlexionExtensionAngle:
                reqSensors.add(SensorPosition.LEFTSHANK);
                reqSensors.add(SensorPosition.LEFTFOOT);
                break;
            case LeftHeeslStrike:
                break;
            case LeftToeOff:
                break;
        }

        return reqSensors;
    }

    public static SensorPosition getSensorPosition(String fileName) {
        if (fileName == null || fileName.length() < 2) {
            return SensorPosition.UNKNOWN;
        }

        String[] firstTwoLetters = fileName.split("_");
        for (SensorPosition sensorPosition : SensorPosition.values()) {
            if (firstTwoLetters[0].equalsIgnoreCase(sensorPosition.getFilePrefix())) {
                return sensorPosition;
            }
        }
        return SensorPosition.UNKNOWN;
    }
    
}
