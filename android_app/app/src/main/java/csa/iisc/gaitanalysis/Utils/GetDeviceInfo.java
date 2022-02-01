package csa.iisc.gaitanalysis.Utils;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import csa.iisc.gaitanalysis.BluetoothManager.BluetoothSPP;
import csa.iisc.gaitanalysis.BluetoothManager.BluetoothState;
import csa.iisc.gaitanalysis.DataModel.GlobalConstants;
import csa.iisc.gaitanalysis.DataModel.Sensor;

/**
 * Created by root on 13/3/16.
 */
public class GetDeviceInfo {

    private static final String TAG = "Utils GetDeviceInfo";
    List<GlobalConstants.SensorParamType> allParams = new ArrayList<>();
    private Sensor currSensor;
    private GlobalConstants.SensorParamType currSensorParamType;
    private Boolean gotAllValues;
    private OnGotAllValuesListener onGotAllValuesListener;

    public GetDeviceInfo(Sensor sensor) {

        currSensor = sensor;
        setBluetoothListeners();
        onGotAllValuesListener = null;
    }

    public Boolean getGotAllValues() {
        return gotAllValues;
    }

    public void setGotAllValues(Boolean gotAllValues) {
        this.gotAllValues = gotAllValues;
        if (onGotAllValuesListener != null)
            onGotAllValuesListener.OnGotAllValuesChange(gotAllValues);
    }

    public OnGotAllValuesListener getOnGotAllValuesListener() {
        return onGotAllValuesListener;
    }

    public void setOnGotAllValuesListener(OnGotAllValuesListener onGotAllValuesListener) {
        this.onGotAllValuesListener = onGotAllValuesListener;
    }

    private void setBluetoothListeners() {
        currSensor.getBluetoothSPP().setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
//                Log.d(TAG, "onDataReceived: OnDataReceivedListener of Get Device Info");
                if (InstanceVals.getExpectedDataType() == InstanceVals.possibleRecType.READ) {
                    String text;
                    if (currSensorParamType == GlobalConstants.SensorParamType.swInfo || currSensorParamType == GlobalConstants.SensorParamType.hwInfo || currSensorParamType == GlobalConstants.SensorParamType.btName) {
                        text = Utils.getMessage(data, true);
                    } else {
                        // Convert the byteValue in the text
                        text = GetDeviceVals.getDetails(currSensorParamType, data[0]).trim();
                    }
                    Boolean addResult = Utils.setDeviceInfo(currSensor, currSensorParamType, text, data[0]);

                    if (!addResult) {
                        Log.d(TAG, "onDataReceived: There was some issue while fetching the device info");
                        allParams.clear();
                        setGotAllValues(false);
//                        currSensor.setDeviceInfoValid(false);
                        return;
                    } else {
                        // IF this is not empty then it means that we are in process of getting the param values.
                        if (allParams.isEmpty()) {
//                            currSensor.setDeviceInfoValid(true);
                            setGotAllValues(true);
                            InstanceVals.setExpectedDataType(InstanceVals.possibleRecType.DUMP, 0);
                        } else {
                            currSensorParamType = allParams.get(0);
                            allParams.remove(0);
                            getParameters(currSensorParamType);
                        }
                    }
                }


            }
        });
    }

    private void getParameters(GlobalConstants.SensorParamType sensorParamType) {

        if (currSensor.getBluetoothSPP() == null) {
            return;
        }
        if (currSensor.getBluetoothSPP().getServiceState() != BluetoothState.STATE_CONNECTED) {
            return;
        }

        //REad
        byte[] data = ReadHeader.getHeader(sensorParamType, true);

        byte checksum = Utils.calcCheckSum(data, 4);
        data[4] = checksum;
        InstanceVals.setExpectedDataType(InstanceVals.possibleRecType.READ, data[1] + 1);
        if (Utils.checkIntegrity(data))
            currSensor.getBluetoothSPP().send(data, false);
        else
            Log.d(TAG, "getParameters: CHecksum Failed for the received packet");


    }

    public void populateParamSettings() {

        if (currSensor.getStatus() != GlobalConstants.SensorStatus.Connected) {
            return;
        }

        allParams.clear();
//        currSensor.setDeviceInfoValid(false);

        for (GlobalConstants.SensorParamType spt : GlobalConstants.SensorParamType.values()) {
            allParams.add(spt);
        }

        currSensorParamType = allParams.get(0);
        allParams.remove(0);
        getParameters(currSensorParamType);
    }

    public interface OnGotAllValuesListener {
        void OnGotAllValuesChange(Boolean val);
    }
}
