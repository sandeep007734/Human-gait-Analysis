package csa.iisc.gaitanalysis.Utils;

import android.os.Environment;

import java.util.ArrayList;
import java.util.List;

import csa.iisc.gaitanalysis.DataModel.Experiment;

/**
 * Created by root on 15/3/16.
 */
public class GlobalValues {

    private static final String TAG = "GlobalValues";
    private static final String gaitHomePath = Environment.getExternalStoragePublicDirectory("GaitApp").getPath();
    private static final String gaitExperimentPath = gaitHomePath + "/Experiments";
    private static final String gaitSensorPath = gaitHomePath + "/Sensors";
    private static volatile GlobalValues instance = null;
    private List<Experiment> allExperiments;

    private GlobalValues() {
        allExperiments = new ArrayList<>();
    }

    public static String getGaitExperimentPath() {
        return gaitExperimentPath;
    }

    public static String getGaitHomePath() {
        return gaitHomePath;
    }

    public static GlobalValues getGlobalValues() {
        if (instance == null) {
            synchronized (GlobalValues.class) {
                if (instance == null) {
                    instance = new GlobalValues();
                }
            }
        }
        return instance;
    }

    public Boolean isNameUnique(String name) {
        for (Experiment experiment : allExperiments) {
            if (experiment.getName().equalsIgnoreCase(name))
                return false;
        }

        return true;
    }

    public void addExperiment(Experiment experiment) {
        this.allExperiments.add(experiment);
    }

    public List<Experiment> getAllExperiments() {
        return this.allExperiments;
    }

    public void setAllExperiments(List<Experiment> _allExperiments) {
        this.allExperiments = _allExperiments;
    }

}
