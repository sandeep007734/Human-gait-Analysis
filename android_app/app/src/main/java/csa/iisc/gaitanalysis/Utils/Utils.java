package csa.iisc.gaitanalysis.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import csa.iisc.gaitanalysis.ComputationModule.DataModel.DataTransferJVals;
import csa.iisc.gaitanalysis.DataModel.Experiment;
import csa.iisc.gaitanalysis.DataModel.GlobalConstants;
import csa.iisc.gaitanalysis.DataModel.Sensor;
import csa.iisc.gaitanalysis.DataModel.Walk;

/**
 * Created by root on 1/3/16.
 */
public class Utils {

    private static final String TAG = "UtilsFunction";
    static List<Sensor> pairedDevices;
    private static OnDialogResultChangeListener onDialogResultChangeListener;

    public static Experiment readExpFromJson(String absPath) throws IOException {
        Log.i(TAG, "readExpFromJson: Reading Experiment: "+absPath);
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(absPath)));
//            for (String line; (line = br.readLine()) != null; ) {
            String line = br.readLine();
            Experiment experiment = (new Gson()).fromJson(line, Experiment.class);
            return experiment;
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return null;
    }

    public static DataTransferJVals readjvalsFromJson(String absPath) {
        Log.i(TAG, "readExpFromJson: Reading Experiment: "+absPath);
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(absPath)));
//            for (String line; (line = br.readLine()) != null; ) {
            String line = br.readLine();
            DataTransferJVals dataTransferJVals = (new Gson()).fromJson(line, DataTransferJVals.class);
            return dataTransferJVals;
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return null;
    }

    public static List<Sensor> CheckBluetoothState(BluetoothAdapter mBluetoothAdapter, Activity activity) {

        pairedDevices = new ArrayList<>();
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : devices) {
            if (device.getBluetoothClass().toString().equals("87000")) {
                Sensor sensor = new Sensor(activity);
                sensor.setMacAddress(device.getAddress());
                sensor.setName(device.getName());
                sensor.setStreaming(false);
                sensor.setStatus(GlobalConstants.SensorStatus.Disconnected);
                sensor.setId(pairedDevices.size() + 1);
                pairedDevices.add(sensor);
            }
        }


        return pairedDevices;

    }

    public static List<String> strArrtoLost(String[] arr) {
        List<String> val = new ArrayList<>();

        for (int i = 0; i < arr.length; i++) {
            val.add(arr[i]);
        }

        return val;
    }

    public static boolean deleteExperiment(Experiment experiment) {
        Log.d(TAG, "deleteExperiment: Deletting Experiment: " + experiment.getAbsPath());
        if (experiment == null) {
            return false;
        }

        File expDir = new File(experiment.getAbsPath());
        if (expDir.exists()) {
            deleteDirectory(expDir);
        }else {
            Log.i(TAG, "deleteExperiment: The Directory Doesnot Exist");
        }

        return true;
    }

    public static void deleteDirectory(File path) {
//        if (path.exists()) {
//            File[] files = path.listFiles();
//            if (files == null) {
//                return true;
//            }
//            for (int i = 0; i < files.length; i++) {
//                Log.i(TAG, "deleteDirectory: file: "+files[i].getAbsolutePath());
//                if (files[i].isDirectory()) {
//                    deleteDirectory(files[i]);
//                } else {
//                    files[i].delete();
//                }
//            }
//        }
//        return (path.delete());

        if (path.isDirectory()) {
            for (File child : path.listFiles()) {
                Log.i(TAG, "deleteDirectory: " + child.getAbsolutePath());
                deleteDirectory(child);
            }
        }

        path.delete();





    }

    public static boolean deleteFile(String absPath) {
        File fileToDel = new File(absPath);

        if (fileToDel.exists()) {
            try {
                fileToDel.delete();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        return true;
    }

    public static boolean saveExperiment(Experiment experiment) throws IOException {
        Log.i(TAG, "saveExperiment: Savng the Experiment: "+experiment.getName());
        if (experiment == null) {
            return false;
        }

        File expDir = new File(experiment.getAbsPath());
        if (!expDir.exists()) {
            expDir.mkdir();
        } else {
        }

        String absPath = experiment.getAbsPath() + "/info.json";
        String data = (new Gson()).toJson(experiment);

        File file = new File(absPath);
        if (file.exists())
            file.delete();


        FileOutputStream fos = new FileOutputStream(absPath, true);
        try (FileWriter fileWriter = new FileWriter(fos.getFD())) {
            fileWriter.write(data);

            fileWriter.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            fos.getFD().sync();
            fos.close();
        }

    }

    public static boolean saveFile(String absPath, String data) throws IOException {

        File file = new File(absPath);
        if (file.exists())
            file.delete();


        FileOutputStream fos = new FileOutputStream(absPath, true);
        try (FileWriter fileWriter = new FileWriter(fos.getFD())) {
            fileWriter.write(data);
            fileWriter.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            fos.getFD().sync();
            fos.close();
        }

    }

    public static boolean setDeviceInfo(Sensor currSensor, GlobalConstants.SensorParamType currSensorParamType, String text, Byte byteVal) {

        if (!GetDeviceVals.verifyVal(currSensorParamType, byteVal))
            return false;


        switch (currSensorParamType) {
            case swInfo:
                currSensor.getSensorDeviceInfo().setSwRelease(text);
                break;
            case hwInfo:
                currSensor.getSensorDeviceInfo().setHwRelease(text);
                break;
            case btName:
                currSensor.getSensorDeviceInfo().setBtName(text);
                break;
            case accFS:
                //  tvAccfs.setText(text);
                currSensor.getSensorDeviceInfo().setAccFs(text);
                currSensor.getSensorDeviceInfo().setAccFSByteValue(byteVal);
                break;
            case gyroFS:
                //   tvGyroFs.setText(text);
                currSensor.getSensorDeviceInfo().setGyroFS(text);
                currSensor.getSensorDeviceInfo().setGyroFSByteValue(byteVal);
                break;
            case pktType:
                //  tvPktType.setText(text);
                currSensor.getSensorDeviceInfo().setPktType(text);
                currSensor.getSensorDeviceInfo().setPktTypeByteValue(byteVal);
                break;
            case orientAlgo:
                //  tvOrientAlgo.setText(text);
                currSensor.getSensorDeviceInfo().setOrientAlgo(text);
                currSensor.getSensorDeviceInfo().setOrientAlgoByteValue(byteVal);
                break;
            case sampleRate:
                //  tvSampleRate.setText(text);
                currSensor.getSensorDeviceInfo().setSampleRate(text);
                currSensor.getSensorDeviceInfo().setSampleRateByteValue(byteVal);
                break;
            case streamLog:
                currSensor.getSensorDeviceInfo().setStreamLog(text);
                currSensor.getSensorDeviceInfo().setStreamLogByteValue(byteVal);
                break;
            case swrfd:
                currSensor.getSensorDeviceInfo().setSwrfd(text);
                currSensor.getSensorDeviceInfo().setSwrfdByteValue(byteVal);
                break;
            default:
        }

        return true;
    }

    public static boolean checkIntegrity(byte[] data) {

        int len = data.length - 1;
        int sum = 0;
        for (int i = 0; i < len; i++) {
            sum = sum + data[i] & 0xFF;
        }
        int module = sum % 256;

        int actualMod = data[len];

        if (module >= 128) {
            module -= 256;
        }

        boolean isOk = (module == actualMod);
//        if (!isOk) {
//        }
        return isOk;

    }

    public static int getDataPacketLength(Byte pktTypeByteValue) {
        int len = 5;
        if (Utils.getBit(pktTypeByteValue, 1) == 1) {  //A
            len += 6;
        }
        if (Utils.getBit(pktTypeByteValue, 2) == 1) {  //G
            len += 6;
        }
        if (Utils.getBit(pktTypeByteValue, 3) == 1) { //M
            len += 6;
        }
        if (Utils.getBit(pktTypeByteValue, 4) == 1) {  //O
            len += 8;
        }
        if (Utils.getBit(pktTypeByteValue, 5) == 1) { //B
            len += 2;
        }
        return len;
    }

    public static int getBit(byte data, int position) {
        if (position == 0)
            return 0;
        return (data >> (position - 1)) & 1;
    }

    public static byte calcCheckSum(byte[] data, int len) {
        int sum = 0;

        for (int i = 0; i < len; i++) {
            sum = sum + data[i] & 0xFF;
        }

        int module = sum % 256;


        if (module >= 128) {
            module -= 256;
        }
        return (byte) module;

    }

    public static String getMessage(byte[] data, Boolean isCheckSum) {
        int len = data.length;

        if (isCheckSum)
            len -= 1;
        int count = 0;
        for (int i = 0; i < len; i++) {
            if (data[i] != 0) {
                count++;
            }
        }

        byte[] newData = new byte[count];

        System.arraycopy(data, 0, newData, 0, count);

        String str = new String(newData);

        String resultString = str.replaceAll("[^\\x00-\\x7F]", "");

        return resultString;
    }

    public static void resetExperimentInfo(File file) {

        Log.d(TAG, "resetExperimentInfo: RESETTING EVERYTHING for file: " + file);

        Experiment experiment = new Experiment();
        experiment.setName(file.getName());
        experiment.setAbsPath(file.getAbsolutePath());

        if (file.getName().contains("Gopinath")) {

//            experiment.setNoOfSensors(2);
//            experiment.setStatus(GlobalConstants.ExperimentStatus.DATAPRESENT);
            experiment.setDateAndTime(Calendar.getInstance().getTime().toString());

            List<String> calbFiles = new ArrayList<>();
            calbFiles.add("S0_0008.txt");
            calbFiles.add("S1_0008.txt");
            experiment.setCalibrationData(calbFiles, GlobalConstants.JointType.RightKnee);

            experiment.setDataSource(GlobalConstants.DataSource.PC);

            List<Walk> walkList = new ArrayList<>();

            String[] thighFiles = new String[]{"S0_0007.txt", "S0_0012.txt", "S0_0013.txt",
                    "S0_0015.txt", "S0_0016.txt", "S0_0017.txt"};

            String[] shankFiles = new String[]{"S1_0007.txt", "S1_0012.txt", "S1_0013.txt",
                    "S1_0015.txt", "S1_0016.txt", "S1_0017.txt"};

            for (int ti = 0; ti < thighFiles.length; ti++) {
                Walk walk = new Walk();
                walk.addWalkFile(thighFiles[ti]);
                walk.addWalkFile(shankFiles[ti]);

                walkList.add(walk);
            }
            experiment.setWalkData(walkList);


        } else if (file.getName().contains("Sandeep")) {
            // experiment.setNoOfSensors(2);
//            experiment.setStatus(GlobalConstants.ExperimentStatus.DATAPRESENT);
            experiment.setDateAndTime(Calendar.getInstance().getTime().toString());

            List<String> calbFiles = new ArrayList<>();
            calbFiles.add("S0_0019.txt");
            calbFiles.add("S1_0019.txt");
            experiment.setCalibrationData(calbFiles, GlobalConstants.JointType.RightKnee);

            experiment.setDataSource(GlobalConstants.DataSource.PC);

            List<Walk> walkList = new ArrayList<>();

            String[] thighFiles = new String[]{"S0_0021.txt", "S0_0023.txt", "S0_0024.txt", "S0_0025.txt", "S0_0026.txt",
                    "S0_0027.txt", "S0_0029.txt", "S0_0030.txt"};

            String[] shankFiles = new String[]{"S1_0021.txt", "S1_0023.txt", "S1_0024.txt", "S1_0025.txt",
                    "S1_0026.txt", "S1_0027.txt", "S1_0029.txt", "S1_0030.txt"};

            for (int ti = 0; ti < thighFiles.length; ti++) {
                Walk walk = new Walk();
                walk.addWalkFile(thighFiles[ti]);
                walk.addWalkFile(shankFiles[ti]);

                walkList.add(walk);
            }

            experiment.setWalkData(walkList);
//            try {
//                Boolean result = Utils.saveExperiment(experiment);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }


        } else if (file.getName().contains("Letizia")) {
            //experiment.setNoOfSensors(2);
//            experiment.setStatus(GlobalConstants.ExperimentStatus.DATAPRESENT);
            experiment.setDateAndTime(Calendar.getInstance().getTime().toString());

            List<String> calbFiles = new ArrayList<>();
            calbFiles.add("S0_0001.txt");
            calbFiles.add("S1_0001.txt");
            calbFiles.add("S2_0001.txt");
            experiment.setCalibrationData(calbFiles, GlobalConstants.JointType.RightKnee);
            experiment.setCalibrationData(calbFiles, GlobalConstants.JointType.RightAnkle);

            experiment.setDataSource(GlobalConstants.DataSource.PC);

            List<Walk> walkList = new ArrayList<>();


            String[] thighFiles = new String[]{"S0_0008.txt", "S0_0010.txt", "S0_0011.txt"};
            String[] shankFiles = new String[]{"S1_0008.txt", "S1_0010.txt", "S1_0011.txt"};
            String[] ankleFiles = new String[]{"S2_0008.txt", "S2_0010.txt", "S2_0011.txt"};


            for (int ti = 0; ti < thighFiles.length; ti++) {
                Walk walk = new Walk();
                walk.addWalkFile(thighFiles[ti]);
                walk.addWalkFile(shankFiles[ti]);
                walk.addWalkFile(ankleFiles[ti]);

                walkList.add(walk);
            }

            experiment.setWalkData(walkList);


        } else if (file.getName().contains("Sofia")) {
            //experiment.setNoOfSensors(2);
//            experiment.setStatus(GlobalConstants.ExperimentStatus.DATAPRESENT);
            experiment.setDateAndTime(Calendar.getInstance().getTime().toString());

            List<String> calbFiles = new ArrayList<>();
            calbFiles.add("S0_0013.txt");
            calbFiles.add("S1_0013.txt");
            calbFiles.add("S2_0013.txt");
            experiment.setCalibrationData(calbFiles, GlobalConstants.JointType.RightKnee);
            experiment.setCalibrationData(calbFiles, GlobalConstants.JointType.RightAnkle);

            experiment.setDataSource(GlobalConstants.DataSource.PC);

            List<Walk> walkList = new ArrayList<>();

            String[] thighFiles = new String[]{"S0_0018.txt", "S0_0019.txt", "S0_0020.txt", "S0_0021.txt",
                    "S0_0022.txt", "S0_0023.txt", "S0_0024.txt"};
            String[] shankFiles = new String[]{"S1_0018.txt", "S1_0019.txt", "S1_0020.txt",
                    "S1_0021.txt", "S1_0022.txt", "S1_0023.txt", "S1_0024.txt"};
            String[] ankleFiles = new String[]{"S2_0018.txt", "S2_0019.txt", "S2_0020.txt",
                    "S2_0021.txt", "S2_0022.txt", "S2_0023.txt", "S2_0024.txt"};


            for (int ti = 0; ti < thighFiles.length; ti++) {
                Walk walk = new Walk();
                walk.addWalkFile(thighFiles[ti]);
                walk.addWalkFile(shankFiles[ti]);
                walk.addWalkFile(ankleFiles[ti]);

                walkList.add(walk);
            }

            experiment.setWalkData(walkList);
            try {
                Boolean result = Utils.saveExperiment(experiment);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        GlobalValues.getGlobalValues().addExperiment(experiment);


    }

    public static void showSimpleMessage(final Activity activity, String title, String message) {
//        new MaterialDialog.Builder(activity)
//                .title(title)
//                .content(message)
//                .positiveText("OK")
//                .show();

        if (!message.equalsIgnoreCase("ok"))
            Snackbar.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }

    public static void showSimpleMessage(final Activity activity, String message) {
        showSimpleMessage(activity, "Message from Server", message);


    }

    public static void setOnDialogResultChangeListener(OnDialogResultChangeListener onDialogResultChangeListener) {
        Utils.onDialogResultChangeListener = onDialogResultChangeListener;
    }

    private static void setDialogResult(boolean dialogResult) {

        if (onDialogResultChangeListener != null)
            onDialogResultChangeListener.onDialogResultChange(dialogResult);
    }

    public static void showSimpleDialog(Activity activity, String title, String message, String positiveText) {
        try {
            new AlertDialog.Builder(activity)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setDialogResult(true);
                        }
                    })
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "showSimpleDialog: Error While showing Simple Dialog");
            e.printStackTrace();
        }
    }

    public static void showSimpleDialog(Activity activity, String title, String message, String positiveText, String negativeText) {
        try {
            new AlertDialog.Builder(activity)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setDialogResult(true);
                        }
                    })
                    .setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setDialogResult(false);
                        }
                    })
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "showSimpleDialog: Error While showing Simple Dialog");
            e.printStackTrace();
        }
    }

    public interface OnDialogResultChangeListener {
        void onDialogResultChange(boolean result);
    }

}
