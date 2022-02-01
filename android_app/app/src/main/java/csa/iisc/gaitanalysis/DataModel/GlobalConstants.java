package csa.iisc.gaitanalysis.DataModel;

import android.Manifest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 29/2/16.
 */
public class GlobalConstants {

    public static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static final int REQUEST_EXTERNAL_STORAGE = 1;

    public enum SensorParamType {
        swInfo("Software Relase"), hwInfo("Hardware Info"), btName("Bluetooth Name"), accFS("Acceleration Full Scale"),
        gyroFS("Gyroscope Full Scale"), pktType("Packet Type"), orientAlgo("Orientation Algorithm"),
        sampleRate("Sample Rate"), streamLog("Stream Log Mode"), swrfd("Start when Dock Remove");
        private final String name;

        SensorParamType(String s) {
            name = s;
        }

        public String toString() {
            return this.name;
        }
    }

    public enum ExperimentStatus {
        CALIBDATAPRESENT("CALIBRATION DATA PRESENT"),
        WALKDATAPRESENT("WALK PRESENT"), CALIBDATAPROCESSED("CALIBRATION WALKPROCESSED"), WALKPROCESSED("ANGLE WALKPROCESSED"), ERROR("ERROR"), NODATA("NO DATA");

        private String name;

        ExperimentStatus(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public enum SensorPosition {
        RIGHTTHIGH("Right Thigh", "S0"), RIGHTSHANK("Right Shank", "S1"), RIGHTFOOT("Right Foot", "S2"),
        LEFTTHIGH("Left Thigh", "S3"), LEFTSHANK("Left Shank", "S4"), LEFTFOOT("Left Foot", "S5"),
        UNKNOWN("Unknown", "Unknown"),;

        private String name;
        private String filePrefix;

        SensorPosition(String name, String filePrefix) {
            this.name = name;
            this.filePrefix = filePrefix;
        }

        public static int getPositionIdx(String pos) {
            int idx = 0;
            for (SensorPosition sensorPosition : SensorPosition.values()) {
                if (pos.equalsIgnoreCase(sensorPosition.toString()))
                    return idx;

                idx++;
            }

            return -1;
        }

        public static SensorPosition getPositionFromIdx(int idx) {
            return SensorPosition.values()[idx];
        }

        public static SensorPosition getPositionFromString(String pos) {
            for (SensorPosition sensorPosition : SensorPosition.values()) {
                if (pos.equalsIgnoreCase(sensorPosition.toString()))
                    return sensorPosition;
            }

            return UNKNOWN;
        }

        public static List<String> getPositons() {
            List<String> positions = new ArrayList<>();

            for (SensorPosition sensorPosition : SensorPosition.values()) {
                positions.add(sensorPosition.toString());
            }

            return positions;
        }

        public String getFilePrefix() {
            return filePrefix;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public enum LoggingStates {STOPPED, LOGGING, PAUSED}

    public enum SensorStatus {Connecting, Connected, Disconnecting, Disconnected, Error, Streaming}

    public enum JointType {
        RightKnee("Right Knee"), RightAnkle("Right Ankle"), LeftKnee("Left Knee"), LeftAnkle("Left Ankle");

        private String name;

        JointType(String name) {
            this.name = name;
        }

        public static JointType getJointTypeFromName(String name) {
            for (JointType jointType : JointType.values()) {
                if (name.equalsIgnoreCase(jointType.toString()))
                    return jointType;
            }
            return null;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public enum WalkResultType {
        RightKneeFlexionExtensionAngle, RightAnkleFlexionExtensionAngle, RightHeeslStrike, RightToeOff,
        LeftKneeFlexionExtensionAngle, LeftAnkleFlexionExtensionAngle, LeftHeeslStrike, LeftToeOff
    }

    public enum DataAxis {XAxis, YAxis, ZAxis}

    public enum DataSource {PC, PHONE}
}
