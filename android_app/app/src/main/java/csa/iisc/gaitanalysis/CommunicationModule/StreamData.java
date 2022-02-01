package csa.iisc.gaitanalysis.CommunicationModule;

import android.util.Log;

import csa.iisc.gaitanalysis.Utils.Utils;

/**
 * Created by root on 12/3/16.
 */
public class StreamData {
    private static final String TAG = "StreamData";
    private byte head;           //1
    private byte pktType;        //1
    private short pktCount;      //2
    private byte[] accData;      //6
    private byte[] gyroData;     //6
    private byte[] magData;      //6
    private byte[] oriData;      //8
    private short vBatt;         //2
    private byte chkSum;         //1
    private Boolean isAccPresent = false;
    private Boolean isGyroPresent = false;
    private Boolean isMagPresent = false;
    private Boolean isOrientPresent = false;
    private Boolean isBatteryPresnet = false;
    private boolean isValid = false;
    private double accX, accY, accZ, gyroX, gyroY, gyroZ, magX, magY, magZ;
    private double qZero, qOne, qTwo, qThree;
    private int expectedStreamLength;
    private Byte hb;
    private Byte lb;


    public StreamData(Byte sensorPktType) {
        pktType = 0;
        this.expectedStreamLength = Utils.getDataPacketLength(sensorPktType);
        setPktType(sensorPktType);
    }

    public static int unsignedToBytes(byte b) {
        return b & 0xFF;
    }

    @Override
    public String toString() {
        String val;
        val = pktCount + "," + unsignedToBytes(pktType) + ",";
        if (isAccPresent) {
            val = val + accX + "," + accY + "," + accZ + ",";
        } else {
            val = val + "0,0,0,";
        }
        if (isGyroPresent) {
            val = val + gyroX + "," + gyroY + "," + gyroZ + ",";
        } else {
            val = val + "0,0,0,";
        }
        if (isMagPresent) {
            val = val + magX + "," + magY + "," + magZ + ",";
        } else {
            val = val + "0,0,0,";
        }
        if (isOrientPresent) {
            val = val + qZero + "," + qOne + "," + qTwo + "," + qThree + ",";
        } else {
            val = val + "0,0,0,0,";
        }
        if (isBatteryPresnet) {
            val = val + vBatt;
        } else {
            val = val + "0";
        }
        if (val.substring(val.length() - 1).equals(",")) {
            val = val.substring(0, val.length() - 1);
        }

        return val;
    }

    public byte getHead() {
        return head;
    }

    public Boolean getAccPresent() {
        return isAccPresent;
    }

    public Boolean getBattPresnet() {
        return isBatteryPresnet;
    }

    public Boolean getGyroPresent() {
        return isGyroPresent;
    }

    public Boolean getMagPresent() {
        return isMagPresent;
    }

    public Boolean getOrienPresent() {
        return isOrientPresent;
    }

    public void setData(byte[] data) throws Exception {
        if (data.length < 5) {
            Log.e(TAG, "setData: The length of the data was too short.");
            return;
        }

        //Byte zero contains the default value. 0x20 which I guess marks the start of a new packet.
        head = data[0];

        setPktCount(combineHighLow(data[3], data[2]));

        int startLoc = 4;

        if (isAccPresent) {
            accData = new byte[6];
            System.arraycopy(data, startLoc, accData, 0, 6);
//            double ka = 19.613;
//            double div = 32768;

            double ka = 1;
            double div = 1;

            accX = (ka*combineHighLow(accData[1], accData[0]))/div;
            accY = (ka*combineHighLow(accData[3], accData[2]))/div;
            accZ = (ka*combineHighLow(accData[5], accData[4]))/div;

            startLoc += 6;
        }
        if (isGyroPresent) {
            gyroData = new byte[6];
            System.arraycopy(data, startLoc, gyroData, 0, 6);

            gyroX = combineHighLow(gyroData[1], gyroData[0]);
            gyroY = combineHighLow(gyroData[3], gyroData[2]);
            gyroZ = combineHighLow(gyroData[5], gyroData[4]);

            startLoc += 6;
        }
        if (isMagPresent) {
            magData = new byte[6];
            System.arraycopy(data, startLoc, magData, 0, 6);

            magX = combineHighLow(magData[1], magData[0]);
            magY = combineHighLow(magData[3], magData[2]);
            magZ = combineHighLow(magData[5], magData[4]);

            startLoc += 6;
        }
        if (isOrientPresent) {
            oriData = new byte[8];
            System.arraycopy(data, startLoc, oriData, 0, 8);

//            double div = 16384.0;
            double div = 1;

            qZero =  (combineHighLow(oriData[1], oriData[0])/div);
            qOne =  (combineHighLow(oriData[3], oriData[2])/div);
            qTwo =  (combineHighLow(oriData[5], oriData[4])/div);
            qThree =  (combineHighLow(oriData[7], oriData[6])/div);


            startLoc += 8;
        }

        if (isBatteryPresnet) {
            vBatt = combineHighLow(data[startLoc + 1], data[startLoc]);
            startLoc += 1;
        }

        chkSum = data[startLoc];
        setValid(Utils.checkIntegrity(data));
    }


    /**
     * @param hb The Higher Byte Value
     * @param lb The Lower Byte Value
     * @return The combined value of bytes.
     */
    private short combineHighLow(Byte hb, Byte lb) {
        String shb = String.format("%8s", Integer.toBinaryString(hb & 0xFF)).replace(' ', '0');
        String slb = String.format("%8s", Integer.toBinaryString(lb & 0xFF)).replace(' ', '0');

        String com = shb + slb;
        short res = (short) Integer.parseInt(com, 2);
        return res;
    }

    public short getPktCount() {
        return pktCount;
    }

    public void setPktCount(short pktCount) {
        this.pktCount = pktCount;
        if(pktCount <0){
            setValid(false);
        }
    }

    public byte getPktType() {
        return pktType;
    }

    public void setPktType(byte pktType) {
        this.pktType = pktType;
        getwhichDataIsPresent();
    }

    /**
     * It tell which of the following data is present in the packet. Acceleration, Gyroscope, Magnetometer and Battery
     */
    private void getwhichDataIsPresent() {

        //A
        isAccPresent = Utils.getBit(pktType, 1) == 1;
        //G
        isGyroPresent = Utils.getBit(pktType, 2) == 1;
        //M
        isMagPresent = Utils.getBit(pktType, 3) == 1;
        //O
        isOrientPresent = Utils.getBit(pktType, 4) == 1;
        //B
        isBatteryPresnet = Utils.getBit(pktType, 5) == 1;

    }

    public double getAccX() {
        return accX;
    }

    public double getAccY() {
        return accY;
    }

    public double getAccZ() {
        return accZ;
    }

    public double getvBatt() {
        return vBatt;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }
}

