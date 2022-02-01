package csa.iisc.gaitanalysis.Utils;

import csa.iisc.gaitanalysis.DataModel.GlobalConstants;

/**
 * Created by root on 29/2/16.
 */
public class ReadHeader {

    public static byte[] getHeader(GlobalConstants.SensorParamType sensorParamType, boolean isRead) {
        byte head;
        if (isRead)
            head = 0x65;
        else
            head = 0x64;

        byte n = 0, addH = 0, addL = 0;

        switch (sensorParamType) {
            case swInfo:
                addH = 0;
                addL = 2;
                n = 16;
                break;
            case hwInfo:
                addH = 0x0;
                addL = 0x12;
                n = 16;
                break;
            case btName:
                addH = 0x20;
                addL = 0x22;
                n = 16;
                break;
            case accFS:
                addH = 0x30;
                addL = 0x34;
                n = 1;
                break;
            case gyroFS:
                addH = 0x30;
                addL = 0x35;
                n = 1;
                break;
            case pktType:
                addH = 0x30;
                addL = 0x38;
                n = 1;
                break;
            case orientAlgo:
                addH = 0x40;
                addL = 0x4E;
                n = 1;
                break;
            case sampleRate:
                addH = 0x50;
                addL = 0x50;
                n = 1;
                break;
            case streamLog:
                addH = 0x50;
                addL = 0x51;
                n = 1;
                break;
            case swrfd:
                addH = 0x50;
                addL = 0x52;
                n = 1;
                break;
//            case wakeupMode:
//                addH = 0x50;
//                addL = 0x53;
//                n = 1;
//                break;
        }

        byte[] data;
        if (isRead)
            data = new byte[5];
        else
            data = new byte[6];
        addH = 0x00;
        data[0] = head;
        data[1] = n;
        data[2] = addL;
        data[3] = addH;

        return data;
    }
}
