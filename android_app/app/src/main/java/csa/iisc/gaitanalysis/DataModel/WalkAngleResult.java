package csa.iisc.gaitanalysis.DataModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 16/3/16.
 */
public class WalkAngleResult {

    private List<Double> angles;
    private GlobalConstants.WalkResultType walkResultType;

    public WalkAngleResult(GlobalConstants.WalkResultType walkResultType) {
        this.walkResultType = walkResultType;
    }

    public void addAngle(Double val) {
        if (angles == null)
            angles = new ArrayList<>();

        angles.add(val);
    }


    public void resetAngle() {
        if (angles == null)
            angles = new ArrayList<>();
        else
            angles.clear();
    }

    public List<Double> getAngles() {
        return angles;
    }

    public void setAngles(List<Double> angles) {
        this.angles = angles;
    }
}
