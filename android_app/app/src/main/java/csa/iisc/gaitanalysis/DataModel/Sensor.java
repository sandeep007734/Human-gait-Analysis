package csa.iisc.gaitanalysis.DataModel;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import csa.iisc.gaitanalysis.BluetoothManager.BluetoothSPP;
import csa.iisc.gaitanalysis.BluetoothManager.BluetoothState;
import csa.iisc.gaitanalysis.CommunicationModule.StreamData;
import csa.iisc.gaitanalysis.DataModel.GlobalConstants.SensorStatus;
import csa.iisc.gaitanalysis.Utils.GetDeviceInfo;
import csa.iisc.gaitanalysis.Utils.GetDeviceVals;
import csa.iisc.gaitanalysis.Utils.HeartBeat;
import csa.iisc.gaitanalysis.Utils.InstanceVals;
import csa.iisc.gaitanalysis.Utils.ReadHeader;
import csa.iisc.gaitanalysis.Utils.Utils;

/**
 * Created by root on 29/1/16.
 */
public class Sensor {

    private transient static String TAG = "Gait Analysis Sensor";
    private transient static GlobalSensorChangeListener globalSensorChangeListener;
    private transient boolean selected = false;
    private transient BluetoothSPP bluetoothSPP;
    private transient SensorDeviceInfo sensorDeviceInfo;
    private transient Boolean isDeviceInfoValid;
    private transient Activity activity;
    private transient int id;
    private String name;
    private transient GlobalConstants.SensorStatus status;
    private transient String macAddress;
    private transient volatile boolean isStreaming;
    private transient String sensorMessage;
    private GlobalConstants.SensorPosition position;
    private transient String sensorFilePrefix;
    private transient OnSensorStatusChangeListener sensorChangeListener;
    private transient SensorMessageListener sensorMessageListener;
    private transient OnSensorDeviceInfoChangeListener onSensorDeviceInfoChangeListener;
    private transient OnChangeParametersListener onChangeParametersListener;
    private transient HeartBeat heartBeat;
    private transient String fileSaveLocationAbs;
    private transient FileOutputStream fos;
    private transient FileWriter fileWriter;
    private transient BufferedWriter bw;
    private transient PrintWriter out;
    private transient GlobalConstants.LoggingStates currLoggingState;
    private transient int maxTriesForDeviceConnect = 5;
    private transient OnNewValidAcquisitionDataListener onNewValidAcquisitionDataListener;

    private transient List<String> dataToBetWrriteToDisk;

    private int pointsRec = 0;

    public Sensor(Activity activity) {
        this.activity = activity;
        if (this.bluetoothSPP == null) {
            this.bluetoothSPP = new BluetoothSPP(activity.getBaseContext());
            this.bluetoothSPP.enable();
        }

        if (sensorDeviceInfo == null) {
            sensorDeviceInfo = new SensorDeviceInfo();
        }

        sensorChangeListener = null;
        sensorMessageListener = null;

        isDeviceInfoValid = false;
        position = GlobalConstants.SensorPosition.UNKNOWN;

        dataToBetWrriteToDisk = new ArrayList<>();
        //  setBluetoothListeners(this);
    }

    public static void setGlobalSensorChangeListener(GlobalSensorChangeListener globalSensorChangeListener) {
        Sensor.globalSensorChangeListener = globalSensorChangeListener;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public GlobalConstants.LoggingStates getCurrLoggingState() {
        return currLoggingState;
    }

    public void setCurrLoggingState(GlobalConstants.LoggingStates currLoggingState) {


        switch (currLoggingState) {

            case STOPPED:
                if (fileWriter != null) {
                    try {
                        bw.flush();
                        bw.close();
                        out.flush();
                        out.close();

                        fileWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (fos != null) {
                    try {
                        fos.getFD().sync();
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case LOGGING:
                try {


                    File file = new File(fileSaveLocationAbs);
                    Log.d(TAG, "setCurrLoggingState: " + fileSaveLocationAbs);
                    if (file.exists())
                        file.delete();

                    file.createNewFile();

                    fos = new FileOutputStream(fileSaveLocationAbs, true);
                    fileWriter = new FileWriter(fileSaveLocationAbs, true);
                    bw = new BufferedWriter(fileWriter);
                    out = new PrintWriter(bw);

                    out.println(getSensorDeviceInfo().toString());

                    String headerText = "ProgrNum,PacketType,";
                    headerText = headerText + "AccX,AccY,AccZ,";
                    headerText = headerText + "GyrX,GyrY,GyrZ,";
                    headerText = headerText + "MagX,MagY,MagZ,";
                    headerText = headerText + "Q0,Q1,Q2,Q3,";
                    headerText = headerText + "Vbat";

                    out.println(headerText);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
                break;
            case PAUSED:
                break;
        }
        this.currLoggingState = currLoggingState;

    }

    public String getFileSaveLocationAbs() {
        return fileSaveLocationAbs;
    }

    public void setFileSaveLocationAbs(String fileSaveLocationAbs) {
        Log.d(TAG, "setFileSaveLocationAbs: " + fileSaveLocationAbs);
        this.fileSaveLocationAbs = fileSaveLocationAbs;
    }

    public String getSensorFilePrefix() {
        return sensorFilePrefix;
    }

    public void setSensorFilePrefix(String sensorFilePrefix) {
        this.sensorFilePrefix = sensorFilePrefix;
    }

    private void setBluetoothListeners(final Sensor currSensor) {
        if (!currSensor.getBluetoothSPP().isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        }
        currSensor.getBluetoothSPP().setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
//                Log.d(TAG, "onDataReceived: OnDataReceivedListener of Sensors");
                if (InstanceVals.getExpectedDataType() == InstanceVals.possibleRecType.DATASTREAM) {
                    StreamData streamData = new StreamData(currSensor.getSensorDeviceInfo().getPktTypeByteValue());
                    try {
                        streamData.setData(data);
//                        Log.d(TAG, "onDataReceived: " + streamData.toString());
                        if (currLoggingState == GlobalConstants.LoggingStates.LOGGING) {
                            if (streamData.isValid()) {
                                pointsRec++;
                                // dataToBetWrriteToDisk.add(streamData.toString());
                                out.println(streamData.toString());
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (streamData.isValid()) {
                        if (onNewValidAcquisitionDataListener != null) {
                            onNewValidAcquisitionDataListener.onValidDataPointReceived(streamData.getAccX(), GlobalConstants.DataAxis.XAxis);
                            onNewValidAcquisitionDataListener.onValidDataPointReceived(streamData.getAccY(), GlobalConstants.DataAxis.YAxis);
                            onNewValidAcquisitionDataListener.onValidDataPointReceived(streamData.getAccZ(), GlobalConstants.DataAxis.ZAxis);
                        }
//                        if (streamData.getPktCount() % 10 == 0) {
//                            double shortVal = streamData.getvBatt();
//
//                            while (shortVal > 10) {
//                                shortVal = (shortVal / 10.0);
//                            }
//                        }

                    }
                }

            }
        });

        currSensor.getBluetoothSPP().setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            @Override
            public void onDeviceConnected(String name, String address) {
                currSensor.setStatus(GlobalConstants.SensorStatus.Connected);
            }

            @Override
            public void onDeviceDisconnected() {
                currSensor.setStatus(GlobalConstants.SensorStatus.Disconnected);
            }

            @Override
            public void onDeviceConnectionFailed() {
                currSensor.setStatus(GlobalConstants.SensorStatus.Error);
            }
        });

    }

    public OnSensorStatusChangeListener getSensorChsensorCangeListener() {
        return sensorChangeListener;
    }

    public void setSensorStatusChangeListener(OnSensorStatusChangeListener sensorChangeListener) {
        this.sensorChangeListener = sensorChangeListener;
    }

    public void setOnSensorDeviceInfoChangeListener(OnSensorDeviceInfoChangeListener onSensorDeviceInfoChangeListener) {
        this.onSensorDeviceInfoChangeListener = onSensorDeviceInfoChangeListener;
        // TODO check is create issue. Listeners are being overwritten from the sensors home page and is not restored bacl.
    }

    public void setSensorMessageListener(SensorMessageListener sensorMessageListener) {
        this.sensorMessageListener = sensorMessageListener;
    }

    public Boolean getDeviceInfoValid() {
        return isDeviceInfoValid;
    }

    public void setDeviceInfoValid(Boolean deviceInfoValid) {


        isDeviceInfoValid = deviceInfoValid;
        if (deviceInfoValid) {
            if (onSensorDeviceInfoChangeListener != null) {
                onSensorDeviceInfoChangeListener.onDeviceInfoAdd();
            }

        } else {
            if (onSensorDeviceInfoChangeListener != null) {
                onSensorDeviceInfoChangeListener.onDeviceInfoAddError();

            }

        }

        if (Sensor.globalSensorChangeListener != null) {
//            Log.d(TAG, "setDeviceInfoValid: Telling the GLOBAL about device info change");
            Sensor.globalSensorChangeListener.onDeviceInfoChange(deviceInfoValid);
        } else {
            Log.d(TAG, "setStatus: Global Listener is NULL");
        }
    }

    public void saveValue() {
        if (isStreaming()) {
            return;
        }

        if (getStatus() != GlobalConstants.SensorStatus.Connected) {
            setSensorMessage("Sensor " + getName() + " is not connected");
        }
        if (this.bluetoothSPP != null) {
            if (this.bluetoothSPP.getServiceState() == BluetoothState.STATE_CONNECTED) {
                Log.d(TAG, "saveValue: Saving the Values to the Sensor");
                this.bluetoothSPP.send("ff", false);        //Save value
            } else {
                setSensorMessage("Sensor is not connected");
                setStreaming(false);
            }
        }
    }

    public void startStream() {

//        dataToBetWrriteToDisk.clear();
//        InstanceVals.setExpectedDataType(InstanceVals.possibleRecType.DATASTREAM,
//                Utils.getDataPacketLength(this.getSensorDeviceInfo().getPktTypeByteValue()));
//        stopHeartBeat();
//        Log.d(TAG, "startStream: Sending Start command");
//        this.bluetoothSPP.send("==", false);
//        Log.d(TAG, "startStream: Send Command came back");
//        setStreaming(true);

        if (isStreaming()) {
            setSensorMessage("Device is in Streaming Mode");
            return;
        }

        if (!getDeviceInfoValid()) {
            setStreaming(false);
            setSensorMessage("Device Information is Not Valid. Please Refresh");
            return;
        }

        if (getStatus() != SensorStatus.Connected) {
            setStreaming(false);
            setSensorMessage("Sensor " + getName() + " is not connected");
        }
        if (this.bluetoothSPP != null) {
            if (this.bluetoothSPP.getServiceState() == BluetoothState.STATE_CONNECTED) {

                dataToBetWrriteToDisk.clear();
                InstanceVals.setExpectedDataType(InstanceVals.possibleRecType.DATASTREAM,
                        Utils.getDataPacketLength(this.getSensorDeviceInfo().getPktTypeByteValue()));
                stopHeartBeat();
//                Log.d(TAG, "startStream: Sending Start command");
                this.bluetoothSPP.send("==", false);
//                Log.d(TAG, "startStream: Send Command came back");
                setStreaming(true);

            } else {
                setSensorMessage("Sensor is not connected");
                setStreaming(false);
            }
        }

    }

    public void stopStream() {
        if (!isStreaming()) {
            return;
        }

        if (this.bluetoothSPP != null) {
            Log.d(TAG, "stopStream: Values Read: " + pointsRec + " on sensors " + getName());
            pointsRec = 0;
            this.bluetoothSPP.send("::", false);
            setStreaming(false);
            startHeartBeat();
//            if (dataToBetWrriteToDisk != null && dataToBetWrriteToDisk.size() > 0){
////                Log.d(TAG, "stopStream: Flushing the data to disk. "+getName());
//                for (String str : dataToBetWrriteToDisk) {
//                    out.println(str);
//                }
//            }
        
        }
    }

    public void disconnectSensor() {

        if (getStatus() == SensorStatus.Disconnected) {
            return;
        }

        if (this.bluetoothSPP != null) {
            this.bluetoothSPP.disconnect();
            this.bluetoothSPP.stopService();
        }
    }

    public boolean connectToSensor() {

        if (getStatus() == SensorStatus.Connected) {
            return true;
        }

        if (this.bluetoothSPP == null) {
            this.bluetoothSPP = new BluetoothSPP(activity.getBaseContext());
            if (!this.bluetoothSPP.isBluetoothAvailable()) {
                return false;
            }
        }

        if (!this.bluetoothSPP.isBluetoothEnabled()) {
            return false;
        } else {
            if (!this.bluetoothSPP.isServiceAvailable()) {
                this.bluetoothSPP.setupService();
                this.bluetoothSPP.startService(BluetoothState.DEVICE_OTHER);
            }
            if (this.bluetoothSPP.getServiceState() != BluetoothState.STATE_CONNECTED) {
                setStatus(SensorStatus.Connecting);
                setBluetoothListeners(Sensor.this);
                this.bluetoothSPP.connect(this.getMacAddress());

            }
        }
        return this.bluetoothSPP.getServiceState() == BluetoothState.STATE_CONNECTED;
    }

    public BluetoothSPP getBluetoothSPP() {
        return bluetoothSPP;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isStreaming() {
        return isStreaming;
    }

    public void setStreaming(boolean streaming) {
        isStreaming = streaming;

        if (sensorChangeListener != null) {
            sensorChangeListener.onStreamChanged(streaming);
        }
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GlobalConstants.SensorPosition getPosition() {
        return position;
    }

    public void setPosition(GlobalConstants.SensorPosition position) {
        this.position = position;
    }

    public GlobalConstants.SensorStatus getStatus() {
        return status;
    }

    public void setStatus(GlobalConstants.SensorStatus status) {

        if(this.status==status)
            return;

        if(status==SensorStatus.Connecting)
            SensorRepo.upTheSensorConnectingCount();
        if(this.status == SensorStatus.Connecting){
            SensorRepo.downTheSensorConnectingCount();
        }

        if (status == GlobalConstants.SensorStatus.Error) {
            if (maxTriesForDeviceConnect > 0) {
                maxTriesForDeviceConnect--;
                connectToSensor();
            }
        }

        maxTriesForDeviceConnect = 5;

        if (status == GlobalConstants.SensorStatus.Disconnecting || status == GlobalConstants.SensorStatus.Disconnected) {
            if (isStreaming()) {
                setStreaming(false);
            }
            stopHeartBeat();

        }

        if (this.status != null)
            Log.d(TAG, "setStatus: Sensor: " + getName() + " status changed from: " + this.status + " to " + status);

        this.status = status;
        if (sensorChangeListener != null) {
            sensorChangeListener.onStatusChanged(status);
        }

        if (status == GlobalConstants.SensorStatus.Connected) {
            if (!isDeviceInfoValid) {
                refreshDeviceInfo();
                startHeartBeat();
            }

        }

        if (Sensor.globalSensorChangeListener != null) {
            Sensor.globalSensorChangeListener.onDeviceConnectionChange();
        }

    }

    private void stopHeartBeat() {
        if (heartBeat != null) {
            heartBeat.stop();
        }
    }

    private void startHeartBeat() {
        if (getStatus() == SensorStatus.Connected) {
            heartBeat = new HeartBeat(getBluetoothSPP());
            heartBeat.start();
        }
    }

    public void refreshDeviceInfo() {
        if (this.getStatus() == SensorStatus.Connected) {
            isDeviceInfoValid = false;
            GetDeviceInfo getDeviceInfo = new GetDeviceInfo(this);
            getDeviceInfo.populateParamSettings();
            getDeviceInfo.setOnGotAllValuesListener(new GetDeviceInfo.OnGotAllValuesListener() {
                @Override
                public void OnGotAllValuesChange(Boolean val) {
                    setDeviceInfoValid(val);
                    fixTheListeners();
                }
            });
        } else {
            Log.d(TAG, "refreshDeviceInfo: Caanot get device info as the devide is not connected");
        }
    }

    public void fixTheListeners() {
        setBluetoothListeners(Sensor.this);
    }

    public SensorDeviceInfo getSensorDeviceInfo() {
        return sensorDeviceInfo;
    }

    public void setSensorDeviceInfo(SensorDeviceInfo sensorDeviceInfo) {
        this.sensorDeviceInfo = sensorDeviceInfo;

        if (onSensorDeviceInfoChangeListener != null)
            onSensorDeviceInfoChangeListener.onDeviceInfoAdd();
    }

    public String getSensorMessage() {
        return sensorMessage;
    }

    public void setSensorMessage(String sensorMessage) {
        this.sensorMessage = sensorMessage;
        if (sensorMessageListener != null)
            sensorMessageListener.onNewMessage(sensorMessage);

    }

    public OnChangeParametersListener getOnChangeParametersListener() {
        return onChangeParametersListener;
    }

    public void setOnChangeParametersListener(OnChangeParametersListener onChangeParametersListener) {
        this.onChangeParametersListener = onChangeParametersListener;
    }

    public void writeParamValueToSensor(GlobalConstants.SensorParamType sensorParamType) {


        if (getBluetoothSPP() == null) {
            return;
        }
        if (getBluetoothSPP().getServiceState() != BluetoothState.STATE_CONNECTED) {
            return;
        }

        byte[] data = ReadHeader.getHeader(sensorParamType, false);

        if (sensorParamType == GlobalConstants.SensorParamType.accFS)
            data[4] = getSensorDeviceInfo().getAccFSByteValue();
        else if (sensorParamType == GlobalConstants.SensorParamType.gyroFS)
            data[4] = getSensorDeviceInfo().getGyroFSByteValue();
        else if (sensorParamType == GlobalConstants.SensorParamType.pktType)
            data[4] = getSensorDeviceInfo().getPktTypeByteValue();
        else if (sensorParamType == GlobalConstants.SensorParamType.orientAlgo)
            data[4] = getSensorDeviceInfo().getOrientAlgoByteValue();
        else if (sensorParamType == GlobalConstants.SensorParamType.sampleRate)
            data[4] = getSensorDeviceInfo().getSampleRateByteValue();
        else if (sensorParamType == GlobalConstants.SensorParamType.streamLog)
            data[4] = getSensorDeviceInfo().getStreamLogByteValue();
        else if (sensorParamType == GlobalConstants.SensorParamType.swrfd)
            data[4] = getSensorDeviceInfo().getSwrfdByteValue();

        if (!GetDeviceVals.verifyVal(sensorParamType, data[4])) {
            return;
        }

        if (onChangeParametersListener != null) {
            onChangeParametersListener.OnChangeParameters(sensorParamType);
        }


        byte checksum = Utils.calcCheckSum(data, 5);
        data[5] = checksum;

        InstanceVals.setExpectedDataType(InstanceVals.possibleRecType.WRITE, 1);
        getBluetoothSPP().send(data, false);

    }

    public void setOnNewValidAcquisitionDataListener(OnNewValidAcquisitionDataListener onNewValidAcquisitionDataListener) {
        this.onNewValidAcquisitionDataListener = onNewValidAcquisitionDataListener;
    }

    public interface OnNewValidAcquisitionDataListener {
        void onValidDataPointReceived(double val, GlobalConstants.DataAxis axis);
    }

    public interface OnChangeParametersListener {
        void OnChangeParameters(GlobalConstants.SensorParamType sensorParamType);
    }

    public interface SensorMessageListener {
        void onNewMessage(String message);
    }

    public interface OnSensorStatusChangeListener {

        void onStatusChanged(GlobalConstants.SensorStatus sensorStatus);

        void onStreamChanged(Boolean isStream);
    }

    public interface OnSensorDeviceInfoChangeListener {
        void onDeviceInfoAdd();

        void onDeviceInfoAddError();
    }

    public interface GlobalSensorChangeListener {

        void onDeviceInfoChange(Boolean isValid);

        void onDeviceConnectionChange();
    }
}
