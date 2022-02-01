package csa.iisc.gaitanalysis.Utils;

import static csa.iisc.gaitanalysis.DataModel.GlobalConstants.SensorParamType;

/**
 * Created by root on 29/2/16.
 */
public class DialogSelection {

    public static SensorParamType getSensprParamType(int item) {

        SensorParamType currSensorParamType = null;
        switch (item) {
            case 0:
                currSensorParamType = SensorParamType.swInfo;
                break;
            case 1:
                currSensorParamType = SensorParamType.hwInfo;
                break;
            case 2:
                currSensorParamType = SensorParamType.btName;
                break;
            case 3:
                currSensorParamType = SensorParamType.accFS;
                break;
            case 4:
                currSensorParamType = SensorParamType.gyroFS;
                break;
            case 5:
                currSensorParamType = SensorParamType.pktType;
                break;
            case 6:
                currSensorParamType = SensorParamType.orientAlgo;
                break;
            case 7:
                currSensorParamType = SensorParamType.sampleRate;
                break;
            case 8:
                currSensorParamType = SensorParamType.streamLog;
                break;
            case 9:
                currSensorParamType = SensorParamType.swrfd;
                break;
//            case 10:
//                currSensorParamType = SensorParamType.wakeupMode;
//                break;
        }

        return currSensorParamType;
    }
}
