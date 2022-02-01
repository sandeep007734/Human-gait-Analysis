package csa.iisc.gaitanalysis.DataModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import csa.iisc.gaitanalysis.DataModel.GlobalConstants.JointType;
import csa.iisc.gaitanalysis.Utils.Utils;

/**
 * Created by root on 15/3/16.
 */
public class Experiment {
    private String name;
    private String absPath;
    private String dateAndTime;
    private int noOfDataFiles;
    private GlobalConstants.DataSource dataSource;


    private List<String> selectedSensors;
    private List<String> selectedSensorsPositions;

    private JointCalibration rightKneeJoint = new JointCalibration(JointType.RightKnee);
    private JointCalibration rightAnkleJoint = new JointCalibration(JointType.RightAnkle);

    private List<Walk> walkData;
    private WalkTrialChangeListener walkTrialChangeListener;

    public Experiment() {
        this.walkData = new ArrayList<>();
    }

    public GlobalConstants.DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(GlobalConstants.DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<String> getValidCalibration() {
        List<String> result = new ArrayList<>();

        if (rightKneeJoint.isCalibrationDataPresent())
            result.add(rightKneeJoint.getJointType().toString());

        if (rightAnkleJoint.isCalibrationDataPresent())
            result.add(rightAnkleJoint.getJointType().toString());

        return result;
    }

    public List<String> getSelectedSensorsPositions() {
        return selectedSensorsPositions;
    }

    public void setSelectedSensorsPositions(List<String> selectedSensorsPositions) {
        if (this.selectedSensorsPositions == null)
            this.selectedSensorsPositions = new ArrayList<>();
        this.selectedSensorsPositions = selectedSensorsPositions;
    }

    public List<String> getSelectedSensors() {
        return selectedSensors;
    }

    public void setSelectedSensors(List<String> selectedSensors) {
        if (this.selectedSensors == null)
            this.selectedSensors = new ArrayList<>();
        this.selectedSensors = selectedSensors;
    }

    public JointCalibration getJoint(JointType jointType) {

        switch (jointType) {

            case RightKnee:
                return rightKneeJoint;

            case RightAnkle:
                return rightAnkleJoint;

            case LeftKnee:
                break;
            case LeftAnkle:
                break;
        }

        return null;
    }

    public boolean isWalkDataPresent() {
        return !(walkData == null || walkData.size() == 0);
    }

    public void resetAllCalibration() {

        rightKneeJoint.resetCalibration(getAbsPath());
        rightAnkleJoint.resetCalibration(getAbsPath());


        try {
            Utils.saveExperiment(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resetCalibration(JointType jointType) {
        switch (jointType) {

            case RightKnee:
                rightKneeJoint.resetCalibration(getAbsPath());
                break;
            case RightAnkle:
                rightAnkleJoint.resetCalibration(getAbsPath());
                break;
            case LeftKnee:
                break;
            case LeftAnkle:
                break;

        }

        if (walkData.size() == 0) {
            noOfDataFiles = 0;
        }

        try {
            Utils.saveExperiment(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getNextFileNumber() {
        String id = Integer.toString(noOfDataFiles);
        for (int i = 0; i < (4 - id.length()); i++) {
            id = "0" + id;
        }

        noOfDataFiles++;
        return id;
    }

    public String addCalibrationFile(JointType jointType) {
        String absFilePath;
        String nextFileId = getNextFileNumber();

        for (int sid = 0; sid < SensorRepo.getAllSensors(true).size(); sid++) {

            String fileName = "S" + Integer.toString(sid) + "_" + nextFileId + ".txt";
            switch (jointType) {

                case RightKnee:
                    rightKneeJoint.addCalibrationData(fileName);
                    break;
                case RightAnkle:
                    rightAnkleJoint.addCalibrationData(fileName);
                    break;
                case LeftKnee:
                    break;
                case LeftAnkle:
                    break;
            }

            try {
                absFilePath = getAbsPath() + "/" + fileName;
                Utils.saveFile(absFilePath, "");
            } catch (IOException e) {
                e.printStackTrace();
                absFilePath = null;
            }

            Sensor sensor = SensorRepo.getAllSensors(true).get(sid);
            sensor.setFileSaveLocationAbs(absFilePath);
        }
        try {
            Utils.saveExperiment(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return nextFileId;
    }

    public void addWalkingTrial(Walk walk) {
        if (walkData == null)
            walkData = new ArrayList<>();

        walkData.add(walk);
    }

    public String addNewWalkingTrial() {

        String absFilePath;
        String nextFileId = getNextFileNumber();

        Walk newWalk = new Walk();

        for (int sid = 0; sid < SensorRepo.getAllSensors(true).size(); sid++) {

            String fileName = "S" + Integer.toString(sid) + "_" + nextFileId + ".txt";
            newWalk.addWalkFile(fileName);

            try {
                absFilePath = getAbsPath() + "/" + fileName;
                Utils.saveFile(absFilePath, "");
            } catch (IOException e) {
                e.printStackTrace();
                absFilePath = null;
            }

            Sensor sensor = SensorRepo.getAllSensors(true).get(sid);
            sensor.setFileSaveLocationAbs(absFilePath);
        }
        try {
            Utils.saveExperiment(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        addWalkingTrial(newWalk);
        return nextFileId;
    }

    public void removeWalk(Walk walk) {
        walk.removeWallFiles(getAbsPath());
        walkData.remove(walkData.indexOf(walk));
        if (walkTrialChangeListener != null)
            walkTrialChangeListener.onWalkChange();
    }

    public Boolean isCalibrationDataPresent(JointType jointType) {

        switch (jointType) {

            case RightKnee:
                return rightKneeJoint.isCalibrationDataPresent();
            case RightAnkle:
                return rightAnkleJoint.isCalibrationDataPresent();

            case LeftKnee:
                break;
            case LeftAnkle:
        }

        return false;

    }

    public Boolean isCalibrationDone(JointType jointType) {
        switch (jointType) {

            case RightKnee:
                return rightKneeJoint.isCalibrationDone();
            case RightAnkle:
                return rightAnkleJoint.isCalibrationDone();

            case LeftKnee:
                break;
            case LeftAnkle:
                break;
        }

        return false;

    }

    public List<Walk> getWalkData() {
        return walkData;
    }

    public void setWalkData(List<Walk> walkData) {
        this.walkData = walkData;
    }

    public String getAbsPath() {
        return absPath;
    }

    public void setAbsPath(String absPath) {
        this.absPath = absPath;
    }

    public List<String> getCalibrationData(JointType jointType) {
        switch (jointType) {

            case RightKnee:
                return rightKneeJoint.getCalibrationData();
            case RightAnkle:
                return rightAnkleJoint.getCalibrationData();

            case LeftKnee:
                break;
            case LeftAnkle:
                break;
        }

        return null;
    }

    public void setCalibrationData(List<String> calibrationData, JointType jointType) {
        switch (jointType) {

            case RightKnee:
                rightKneeJoint.setCalibrationData(calibrationData);
                break;
            case RightAnkle:
                rightAnkleJoint.setCalibrationData(calibrationData);
                break;
            case LeftKnee:
                break;
            case LeftAnkle:
                break;
        }
    }

    public List<double[]> getCalibrationResult(JointType jointType) {
        switch (jointType) {

            case RightKnee:
                return rightKneeJoint.getCalibrationResult();
            case RightAnkle:
                return rightAnkleJoint.getCalibrationResult();

            case LeftKnee:
                break;
            case LeftAnkle:
                break;
        }
        return null;
    }

    public void setCalibrationResult(List<double[]> calibResult, JointType jointType) {
        switch (jointType) {
            case RightKnee:
                rightKneeJoint.setCalibrationResult(calibResult);
                break;
            case RightAnkle:
                rightAnkleJoint.setCalibrationResult(calibResult);
                break;

            case LeftKnee:
                break;
            case LeftAnkle:
                break;
        }

    }

    public String getDateAndTime() {
        return dateAndTime;
    }

    public void setDateAndTime(String dateAndTime) {
        this.dateAndTime = dateAndTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWalkTrialChangeListener(WalkTrialChangeListener walkTrialChangeListener) {
        this.walkTrialChangeListener = walkTrialChangeListener;
    }

    public interface WalkTrialChangeListener {
        void onWalkChange();
    }


}
