package csa.iisc.gaitanalysis.Utils;

import csa.iisc.gaitanalysis.DataModel.GlobalConstants;

/**
 * Created by root on 29/2/16.
 */
public class GetDeviceVals {

    public static String plusMinus = "\u00B1";

    public static String getDetails(GlobalConstants.SensorParamType paramType, Byte byteVal) {
        switch (paramType) {
            case swInfo:
                break;
            case hwInfo:
                break;
            case btName:
                break;
            case accFS:
                accMode val = getAccMode(byteVal);
                if (val != null)
                    return val.toString();
                else
                    return "";
            case gyroFS:
                gyroMode gval = getGyroMode(byteVal);
                if (gval != null)
                    return gval.toString();
                else
                    return "";
            case pktType:
                return getPacketType(byteVal);
            case orientAlgo:
                orientAlgoMode oval = getOrientAlgoMode(byteVal);
                if (oval != null)
                    return oval.toString();
                else
                    return "";
            case sampleRate:
                sampleRateMode sval = getSampleRateMode(byteVal);
                if (sval != null)
                    return sval.toString();
                else
                    return "";
            case streamLog:
                streamLogMode stval = getStreamLogMode(byteVal);
                if (stval != null)
                    return stval.toString();
                else
                    return "";
            case swrfd:
                swrfdMode swval = getSwrfdMode(byteVal);
                if (swval != null)
                    return swval.toString();
                else
                    return "";
//                case wakeupMode:
//                    return getWakeupMode(byteVal).toString();


        }
        return "";
    }

    public static Boolean verifyVal(GlobalConstants.SensorParamType paramType, Byte byteVal) {

        if ((paramType == GlobalConstants.SensorParamType.swInfo) || (paramType == GlobalConstants.SensorParamType.hwInfo) || (paramType == GlobalConstants.SensorParamType.btName))
            return true;

        if ((paramType == GlobalConstants.SensorParamType.accFS) || (paramType == GlobalConstants.SensorParamType.gyroFS)) {
            return (byteVal == 0) || (byteVal == 1) || (byteVal == 2) || (byteVal == 3);
        } else if ((paramType == GlobalConstants.SensorParamType.orientAlgo) || (paramType == GlobalConstants.SensorParamType.streamLog) || (paramType == GlobalConstants.SensorParamType.swrfd)) {
            return (byteVal == 0) || (byteVal == 1) || (byteVal == 2);
        } else if (paramType == GlobalConstants.SensorParamType.sampleRate) {
            return (byteVal >= 0) && (byteVal <= 10);
        } else if (paramType == GlobalConstants.SensorParamType.pktType) {
            if ((Utils.getBit(byteVal, 8) == 1) && (Utils.getBit(byteVal, 7) == 0) && (Utils.getBit(byteVal, 6) == 0))
                return true;
            else return byteVal == 0;
        }

        return false;
    }

    public static String getPacketType(Byte val) {

        if (val == 0) {
            return "RAW";
        }

        String ans = "";

        if (Utils.getBit(val, 1) == 1) {
            ans = ans + "A";
        }
        if (Utils.getBit(val, 2) == 1) {
            ans = ans + "G";
        }
        if (Utils.getBit(val, 3) == 1) {
            ans = ans + "M";
        }
        if ((Utils.getBit(val, 4) == 1)) {
            ans = ans + "O";
        }
        if ((Utils.getBit(val, 5) == 1)) {
            ans = ans + "B";
        }


        return ans;
    }

//    public static wakeupMode getWakeupMode(String val) {
//        if (val.equals("0x10")) {
//            return wakeupMode.onlyButton;
//        } else if (val.equals("0x20")) {
//            return wakeupMode.vibFast;
//        } else if (val.equals("0x20")) {
//            return wakeupMode.vibMed;
//        } else if (val.equals("0x22")) {
//            return wakeupMode.vibSlow;
//        } else if (val.equals("0x30")) {
//            return wakeupMode.buttonVibFast;
//        } else if (val.equals("0x31")) {
//            return wakeupMode.buttonVibMed;
//        } else if (val.equals("0x32")) {
//            return wakeupMode.buttonVibSlow;
//        } else {
//            return null;
//        }
//    }

    public static swrfdMode getSwrfdMode(Byte val) {
        if (val == 0) {
            return swrfdMode.immediateStart;
        } else if (val == 1) {
            return swrfdMode.dockRemoveStart;
        } else if (val == 2) {
            return swrfdMode.contLogg;
        } else {
            return null;
        }
    }

    public static streamLogMode getStreamLogMode(Byte val) {
        if (val == 0) {
            return streamLogMode.stream;
        } else if (val == 1) {
            return streamLogMode.log;
        } else if (val == 2) {
            return streamLogMode.streamAndLog;
        } else {
            return null;
        }
    }

    public static sampleRateMode getSampleRateMode(Byte val) {

        if (val == 0) {
            return sampleRateMode.zero;
        } else if (val == 1) {
            return sampleRateMode.one;
        } else if (val == 2) {
            return sampleRateMode.two;
        } else if (val == 3) {
            return sampleRateMode.three;
        } else if (val == 4) {
            return sampleRateMode.four;
        } else if (val == 5) {
            return sampleRateMode.five;
        } else if (val == 6) {
            return sampleRateMode.six;
        } else if (val == 7) {
            return sampleRateMode.seven;
        } else if (val == 8) {
            return sampleRateMode.eight;
        } else if (val == 9) {
            return sampleRateMode.nine;
        } else if (val == 10) {
            return sampleRateMode.ten;
        } else {
            return null;
        }
    }

    public static orientAlgoMode getOrientAlgoMode(Byte val) {
        if (val == 0) {
            return orientAlgoMode.eCompass;
        } else if (val == 1) {
            return orientAlgoMode.Gyro;
        } else if (val == 2) {
            return orientAlgoMode.Kalman;
        } else {
            return null;
        }
    }

    public static gyroMode getGyroMode(Byte val) {
        //val is in hex
        if (val == 0) {
            return gyroMode.twoFifty;
        } else if (val == 1) {
            return gyroMode.fiveHundred;
        } else if (val == 2) {
            return gyroMode.thousand;
        } else if (val == 3) {
            return gyroMode.twoThousand;
        } else {
            return null;
        }
    }

    public static accMode getAccMode(Byte val) {
        //val is in hex
        if (val == 0) {
            return accMode.twoG;
        } else if (val == 1) {
            return accMode.fourG;
        } else if (val == 2) {
            return accMode.eightG;
        } else if (val == 3) {
            return accMode.sixteenG;
        } else {
            return null;
        }
    }

    public enum accMode {
        twoG(plusMinus + "2g"), fourG(plusMinus + "4g"), eightG(plusMinus + "8g"), sixteenG(plusMinus + "16g");

        private final String name;

        accMode(String s) {
            name = s;
        }

        public String toString() {
            return this.name;
        }
    }

    public enum gyroMode {
        twoFifty(plusMinus + "250dps"), fiveHundred(plusMinus + "500dps"), thousand(plusMinus + "1000dps"), twoThousand(plusMinus + "2000dps");
        private final String name;

        gyroMode(String s) {
            name = s;
        }

        public String toString() {
            return this.name;
        }
    }

    public enum orientAlgoMode {
        eCompass("eCompass"), Gyro("Gyroscope"), Kalman("Kalman Filtering");

        private final String name;

        orientAlgoMode(String s) {
            name = s;
        }

        public String toString() {
            return this.name;
        }
    }

    public enum sampleRateMode {
        zero("200 Hz"), one("100 Hz"), two("50 Hz"), three("33.33 Hz"), four("25 Hz"),
        five("20 Hz"), six("16,67 Hz"), seven("12.5 Hz"), eight("10 Hz"), nine("5 Hz"), ten("300 Hz");

        private final String name;

        sampleRateMode(String s) {
            name = s;
        }

        public String toString() {
            return this.name;
        }
    }

    public enum streamLogMode {
        stream(" Stream Bluetooth"), log("Log on Device"), streamAndLog("Stream and Log");

        private final String name;

        streamLogMode(String s) {
            name = s;
        }

        public String toString() {
            return this.name;
        }
    }

    public enum swrfdMode {
        immediateStart("Immediately after Start Command"), dockRemoveStart("After Un dock"), contLogg("Continuous Data Logging ");

        private final String name;

        swrfdMode(String s) {
            name = s;
        }

        public String toString() {
            return this.name;
        }
    }

    public enum wakeupMode {
        onlyButton("Button"), vibFast("High Sensitivity"), vibMed("Medium Sensitivity"), vibSlow("Low Sensitivity"),
        buttonVibFast("Button and High Sensitivity"), buttonVibMed("Button and Medium Sensitivity"), buttonVibSlow("Button and Slow Sensitivity");

        private final String name;

        wakeupMode(String s) {
            name = s;
        }

        public String toString() {
            return this.name;
        }
    }
}
