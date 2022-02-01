package csa.iisc.gaitanalysis.DataModel;

import csa.iisc.gaitanalysis.Utils.GetDeviceVals;

/**
 * Created by root on 29/2/16.
 */
public class SensorDeviceInfo {

    private static final String TAG = "sensorDeviceInfo";

    private String swRelease;
    private String hwRelease;
    private String btName;
    private String accFs;
    private String gyroFS;
    private String pktType;
    private String orientAlgo;
    private String sampleRate;
    private String streamLog;
    private String swrfd;
    private String wakeUpMode;

    private Byte accFSByteValue;
    private Byte gyroFSByteValue;
    private Byte pktTypeByteValue;
    private Byte orientAlgoByteValue;
    private Byte sampleRateByteValue;
    private Byte streamLogByteValue;
    private Byte swrfdByteValue;


    public SensorDeviceInfo() {
        this.accFs = "";
        this.btName = "";
        this.gyroFS = "";
        this.hwRelease = "";
        this.orientAlgo = "";
        this.pktType = "";
        this.sampleRate = "";
        this.streamLog = "";
        this.swRelease = "";
        this.swrfd = "";
        this.wakeUpMode = "";

        accFSByteValue = 0;
        gyroFSByteValue = 0;
        pktTypeByteValue = 0;
        orientAlgoByteValue = 0;
        sampleRateByteValue = 0;
        streamLogByteValue = 0;
        swrfdByteValue = 0;
    }

    @Override
    public String toString() {
        return "AccFullScale=" + accFs + ", GyrFullScale=" + gyroFS + ", SamplingRate=" + sampleRate + ", PacketType" + pktType;
    }

    public Byte getAccFSByteValue() {
        return accFSByteValue;
    }

    public void setAccFSByteValue(Byte accFSByteValue) {
        this.accFSByteValue = accFSByteValue;
        setAccFs(GetDeviceVals.getDetails(GlobalConstants.SensorParamType.accFS, accFSByteValue));
    }

    public Byte getGyroFSByteValue() {
        return gyroFSByteValue;
    }

    public void setGyroFSByteValue(Byte gyroFSByteValue) {
        this.gyroFSByteValue = gyroFSByteValue;
        setGyroFS(GetDeviceVals.getDetails(GlobalConstants.SensorParamType.gyroFS, gyroFSByteValue));
    }

    public Byte getOrientAlgoByteValue() {
        return orientAlgoByteValue;
    }

    public void setOrientAlgoByteValue(Byte orientAlgoByteValue) {
        this.orientAlgoByteValue = orientAlgoByteValue;
        setOrientAlgo(GetDeviceVals.getDetails(GlobalConstants.SensorParamType.orientAlgo, orientAlgoByteValue));
    }

    public Byte getPktTypeByteValue() {
        return pktTypeByteValue;
    }

    public void setPktTypeByteValue(Byte pktTypeByteValue) {
        this.pktTypeByteValue = pktTypeByteValue;
        setPktType(GetDeviceVals.getDetails(GlobalConstants.SensorParamType.pktType, pktTypeByteValue));
    }

    public Byte getSampleRateByteValue() {
        return sampleRateByteValue;
    }

    public void setSampleRateByteValue(Byte sampleRateByteValue) {
        this.sampleRateByteValue = sampleRateByteValue;
        setSampleRate(GetDeviceVals.getDetails(GlobalConstants.SensorParamType.sampleRate, sampleRateByteValue));
    }

    public Byte getStreamLogByteValue() {
        return streamLogByteValue;
    }

    public void setStreamLogByteValue(Byte streamLogByteValue) {
        this.streamLogByteValue = streamLogByteValue;
        setStreamLog(GetDeviceVals.getDetails(GlobalConstants.SensorParamType.streamLog, streamLogByteValue));
    }

    public Byte getSwrfdByteValue() {
        return swrfdByteValue;
    }

    public void setSwrfdByteValue(Byte swrfdByteValue) {
        this.swrfdByteValue = swrfdByteValue;
        setSwrfd(GetDeviceVals.getDetails(GlobalConstants.SensorParamType.swrfd, swrfdByteValue));
    }

    public String getAccFs() {
        return accFs;
    }

    public void setAccFs(String accFs) {
        this.accFs = accFs;
    }

    public String getBtName() {
        return btName;
    }

    public void setBtName(String btName) {
        this.btName = btName;
    }

    public String getGyroFS() {
        return gyroFS;
    }

    public void setGyroFS(String gyroFS) {
        this.gyroFS = gyroFS;
    }

    public String getHwRelease() {
        return hwRelease;
    }

    public void setHwRelease(String hwRelease) {
        this.hwRelease = hwRelease;
    }

    public String getOrientAlgo() {
        return orientAlgo;
    }

    public void setOrientAlgo(String orientAlgo) {
        this.orientAlgo = orientAlgo;
    }

    public String getPktType() {
        return pktType;
    }

    public void setPktType(String pktType) {
        this.pktType = pktType;
    }

    public String getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(String sampleRate) {
        this.sampleRate = sampleRate;
    }

    public String getStreamLog() {
        return streamLog;
    }

    public void setStreamLog(String streamLog) {
        this.streamLog = streamLog;
    }

    public String getSwRelease() {
        return swRelease;
    }

    public void setSwRelease(String swRelease) {
        this.swRelease = swRelease;
    }

    public String getSwrfd() {
        return swrfd;
    }

    public void setSwrfd(String swrfd) {
        this.swrfd = swrfd;
    }

    public String getWakeUpMode() {
        return wakeUpMode;
    }

    public void setWakeUpMode(String wakeUpMode) {
        this.wakeUpMode = wakeUpMode;
    }
}
