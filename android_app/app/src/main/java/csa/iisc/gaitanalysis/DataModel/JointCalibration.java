package csa.iisc.gaitanalysis.DataModel;

import android.app.Activity;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import csa.iisc.gaitanalysis.ComputationModule.Calibration.CalibrationPrimaryLoop;
import csa.iisc.gaitanalysis.ComputationModule.Calibration.DoCalibration;
import csa.iisc.gaitanalysis.DataModel.GlobalConstants.JointType;
import csa.iisc.gaitanalysis.Utils.Utils;
import csa.iisc.gaitanalysis.Utils.UtilsData;

/**
 *
 */
public class JointCalibration {

    private static final transient String TAG = "JointCalibration";

    private JointType jointType;
    private List<String> calibrationData;
    private List<double[]> calibrationResult;

    private JointCalibrationChangeListner jointCalibrationChangeListner;

    public JointCalibration(JointType jointType) {
        this.jointType = jointType;

        calibrationData = new ArrayList<>();
        calibrationResult = new ArrayList<>();
    }

    public Boolean isCalibrationDone() {
        if (calibrationResult == null)
            return false;

        return calibrationResult.size() != 0;

    }

    public Boolean isCalibrationDataPresent() {
        if (calibrationData == null)
            return false;

        return calibrationData.size() != 0;

    }

    public void addCalibrationData(String data) {
        if (calibrationData == null) {
            calibrationData = new ArrayList<>();
        }

        calibrationData.add(data);
    }

    public void resetCalibration(String expAbsPath) {
        if (calibrationData != null) {
            for (String fileName : calibrationData) {
                Log.d(TAG, "resetCalibration: Delete: " + expAbsPath + "/" + fileName);
                Utils.deleteFile(expAbsPath + "/" + fileName);
            }
            calibrationData.clear();
        }

        if (calibrationResult != null) {
            calibrationResult.clear();
        }
    }

    public JointType getJointType() {
        return jointType;
    }

    public void setJointType(JointType jointType) {
        this.jointType = jointType;
    }

    public List<String> getCalibrationData() {
        return calibrationData;
    }

    public void setCalibrationData(List<String> calibrationData) {
        if (this.calibrationData == null)
            this.calibrationData = new ArrayList<>();

        this.calibrationData = calibrationData;
    }

    public List<double[]> getCalibrationResult() {
        return calibrationResult;
    }

    public void setCalibrationResult(List<double[]> calibrationResult) {
        Log.d(TAG, "setCalibrationResult: Setting the Calibration Result: " + getJointType().toString());
        if (this.calibrationResult == null)
            this.calibrationResult = new ArrayList<>();
        this.calibrationResult = calibrationResult;
    }

    public void runTheCalibration(Activity activity, String expAbsPath) {

        List<GlobalConstants.SensorPosition> reqSensorPos = UtilsData.getRequiredSensorsJoints(getJointType());

        String fileName1, fileName2;
        fileName1 = "";
        fileName2 = "";
        for (String fileName : getCalibrationData()) {
            if (fileName.contains(reqSensorPos.get(0).getFilePrefix())) {
                fileName1 = fileName;
                break;
            }
        }

        for (String fileName : getCalibrationData()) {
            if (fileName.contains(reqSensorPos.get(1).getFilePrefix())) {
                fileName2 = fileName;
                break;
            }
        }

        if (fileName1.isEmpty() || fileName2.isEmpty()) {
            Utils.showSimpleDialog(activity, "Error", "Proper Calibration file is missing", "OK");
            return;
        }

        Log.d(TAG, "runTheCalibration: " + fileName1);
        Log.d(TAG, "runTheCalibration: " + fileName2);
        if (getCalibrationData().size() > 1) {
            String file1 = expAbsPath + "/" + fileName1;
            String file2 = expAbsPath + "/" + fileName2;

            try {
                Utils.showSimpleMessage(activity, "Running Calibration");
                CalibrationPrimaryLoop.cancelRunning = false;
                performCalibration(file1, file2);
            } catch (Exception e) {
                Utils.showSimpleMessage(activity, "There was some problem while performing Calibration");
                e.printStackTrace();
            }
        } else {
            // Not enough Data to do the calibration
            Utils.showSimpleMessage(activity, "Not enough data to perform calibration." +
                    " Calibration File Present: " + getCalibrationData().size());
        }

    }

    private void performCalibration(String calibFile1, String calibFile2) throws IOException {
        Thread calibrationThread = null;
        if (calibrationThread == null) {
            final DoCalibration doCalibration = new DoCalibration(calibFile1, calibFile2);
            calibrationThread = doCalibration.runCalibration();
            if (calibrationThread != null) {
                doCalibration.setProcessFinishedListeners(new DoCalibration.ProcessFinishedListeners() {
                    @Override
                    public void onProcessDone(List<double[]> result) {

                        boolean isResultValid = true;

                        if (result == null) {
                            isResultValid = false;
                        } else {
                            for (double[] dvalArr : result) {
                                for (double dval : dvalArr) {
                                    if (Double.isNaN(dval)) {
                                        isResultValid = false;
                                        break;
                                    }
                                }
                                if (!isResultValid) {
                                    break;
                                }
                            }
                        }


                        if (jointCalibrationChangeListner != null)
                            jointCalibrationChangeListner.onCalibrationResultChange(isResultValid, result);
                    }
                });

            }
        }
    }

    public void setJointCalibrationChangeListner(JointCalibrationChangeListner jointCalibrationChangeListner) {
        this.jointCalibrationChangeListner = jointCalibrationChangeListner;
    }

    public interface JointCalibrationChangeListner {
        void onCalibrationResultChange(boolean result, List<double[]> resultData);
    }
}
