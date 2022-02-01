package csa.iisc.gaitanalysis.ComputationModule.Calibration;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import csa.iisc.gaitanalysis.ComputationModule.GaitAnalysis.ReadData;
import csa.iisc.gaitanalysis.ComputationModule.GaitAnalysis.SensorData;



public class DoCalibration {

    final static double pi = 3.1416;
    private static final String TAG = "DoCalibration";

    private String thighFile;
    private String shankFile;

    private List<double[]> j1_vals_result;
    private ProcessFinishedListeners processFinishedListeners;

    public DoCalibration(String _thighFile, String _kneeFile) {
        this.thighFile = _thighFile;
        this.shankFile = _kneeFile;
        processFinishedListeners = null;
    }

    public void setProcessFinishedListeners(ProcessFinishedListeners processFinishedListeners) {
        this.processFinishedListeners = processFinishedListeners;
    }

    public List<double[]> getJ1_vals_result() {
        return j1_vals_result;
    }

    public Thread runCalibration() {
        SensorData cDataThigh = null, cDataShank = null;
        try {

            List<String> thighdata = (new ReadData()).readData(thighFile);
            List<String> shankData = (new ReadData()).readData(shankFile);

            // TODO make the data matching better. EDIT1: How?
            Integer diff = Math.abs(thighdata.size() - shankData.size());
//			System.out.println("Diff is: "+diff);
            if (thighdata.size() > shankData.size()) {
                for (int i = 0; i < diff; i++) {
                    thighdata.remove(thighdata.size() - 1);
                }
            } else {
                for (int i = 0; i < diff; i++) {
                    shankData.remove(thighdata.size() - 1);
                }
            }

            cDataThigh = new SensorData();
            cDataThigh.loadData(thighdata, thighdata.size());

            cDataShank = new SensorData();
            cDataShank.loadData(shankData, thighdata.size());

        } catch (IOException e) {
            Log.e(TAG, "runCalibration: Error Occurred" );
            e.printStackTrace();
        }

        if (cDataThigh != null || cDataShank != null) {
            CalibrationLoops calibrationLoops = new CalibrationLoops(cDataThigh, cDataShank);
            calibrationLoops.setThreadFinishListeners(new CalibrationLoops.ThreadFinishListeners() {
                @Override
                public void onThreadFinish(List<double[]> j1_vals) {
                    if (j1_vals == null) {
                        Log.e(TAG, "onThreadFinish: There was some issue in the calibration loop");
                    }
                    if (processFinishedListeners != null) {
                        processFinishedListeners.onProcessDone(j1_vals);
                    }
                }

                @Override
                public void onUserCancel() {
                    Log.d(TAG, "onUserCancel: User Cancelled the calibration Process");
                    if (processFinishedListeners != null) {
                        processFinishedListeners.onProcessDone(null);
                    }
                }
            });

            Thread thread = new Thread(calibrationLoops);
            thread.start();
            return thread;
        }

        return null;

    }

    public interface ProcessFinishedListeners {
        void onProcessDone(List<double[]> result);
    }


}

class CalibrationLoops extends Thread implements Runnable {

    private static final String TAG = "CalibrationLoops";
    private SensorData cDataThigh, cDataShank;
    private ThreadFinishListeners threadFinishListeners;

    public CalibrationLoops(SensorData cDataThigh, SensorData cDataShank) {
        this.cDataThigh = cDataThigh;
        this.cDataShank = cDataShank;
        threadFinishListeners = null;

    }


    public void setThreadFinishListeners(ThreadFinishListeners threadFinishListeners) {
        this.threadFinishListeners = threadFinishListeners;
    }


    @Override
    public void run() {
        try {
            //TODO change back to 50
            int numOfIterations = 50;
            runCalibrationLoops(numOfIterations, cDataThigh.sizeOfData, cDataThigh.gyro, cDataShank.gyro, cDataThigh.acc,
                    cDataShank.acc, cDataThigh.gyro_s_derv_data, cDataShank.gyro_s_derv_data);
        } catch (Exception e) {
            Log.e(TAG, "run: ERROR WHILE RUNNING CALIBRATION");
            CalibrationPrimaryLoop.cancelRunning=true;
            e.printStackTrace();

        }
    }

    public void runCalibrationLoops(int numOfIterations, int sizeOfData, double[][] gyro_s_thigh, double[][] gyro_s_shank,
                                    double[][] acc_s_thigh, double[][] acc_s_shank, double[][] gyro_s_derv_thigh,
                                    double[][] gyro_s_derv_shank) {

        CalibrationPrimaryLoop.runLoop(numOfIterations, sizeOfData,
                gyro_s_thigh, gyro_s_shank, acc_s_thigh, acc_s_shank,
                gyro_s_derv_thigh,
                gyro_s_derv_shank);

        if (CalibrationPrimaryLoop.cancelRunning) {
            //it can run next time as this is static
            CalibrationPrimaryLoop.cancelRunning = false;
            if (threadFinishListeners != null) {
                threadFinishListeners.onUserCancel();
            }
            return;
        }
//        double[] j1_valf = {0.8915101923504168, 0.011273280733363564, 0.4528603427954688};
//        double[] j2_valf = {0.8657219777129791, 0.4877391620711688, 0.11240981757320966};

        List<double[]> j1_vals = new ArrayList<>();

        j1_vals.add(CalibrationPrimaryLoop.j1_valf);
        j1_vals.add(CalibrationPrimaryLoop.j2_valf);

        CalibrationSecondaryLoop.runLoop(numOfIterations, sizeOfData, gyro_s_thigh, gyro_s_shank, acc_s_thigh, acc_s_shank,
                gyro_s_derv_thigh, gyro_s_derv_shank, CalibrationPrimaryLoop.j1_valf, CalibrationPrimaryLoop.j2_valf);

        if (threadFinishListeners != null) {
            threadFinishListeners.onThreadFinish(j1_vals);
        }


    }

    public interface ThreadFinishListeners {
        void onThreadFinish(List<double[]> j1_vals);

        void onUserCancel();
    }


}