package csa.iisc.gaitanalysis.Utils;

/**
 * Created by root on 29/2/16.
 */
public class InstanceVals {

    private static final String TAG = "Instance Vals";
    private static volatile possibleRecType expectedDataType = possibleRecType.DUMP;
    private static volatile int readlen = 0;

    public static possibleRecType getExpectedDataType() {
        return expectedDataType;
    }

    public static void setExpectedDataType(possibleRecType expectedDataType, int readlen) {
        InstanceVals.expectedDataType = expectedDataType;
        InstanceVals.readlen = readlen;
    }

    public static int getReadlen() {
        return readlen;
    }

    public enum possibleRecType {DATASTREAM, AGMB, READ, DUMP, WRITE}

}
