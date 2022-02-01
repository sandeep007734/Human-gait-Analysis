package csa.iisc.gaitanalysis.Utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;

import java.util.List;

import csa.iisc.gaitanalysis.Activities.CollectCalibrationDataActivity;
import csa.iisc.gaitanalysis.Activities.ShowGraphActivity;
import csa.iisc.gaitanalysis.DataModel.GlobalConstants;
import csa.iisc.gaitanalysis.DataModel.Sensor;
import csa.iisc.gaitanalysis.DataModel.SensorRepo;
import csa.iisc.gaitanalysis.ListAdapters.SelectSensorsAdapter;
import csa.iisc.gaitanalysis.R;



public class UtilsSensor {

    private static CalibrationDataChangeListener calibrationDataChangeListener;
    private static SelectedSensorChangeListener selectedSensorChangeListener;

    public static void setCalibrationDataChangeListener(CalibrationDataChangeListener calibrationDataChangeListener) {
        UtilsSensor.calibrationDataChangeListener = calibrationDataChangeListener;
    }

    public static void showCalibCollectActivity(Activity activity, String walkId) {
        Intent collectCalibration = new Intent(activity, CollectCalibrationDataActivity.class);
        Bundle extras = new Bundle();
        extras.putString("walkId", walkId);
        collectCalibration.putExtras(extras);
        activity.startActivity(collectCalibration);
    }

    public static void showCalibCollectDialog(Activity activity, String walkId) {

        final Dialog dialog = new Dialog(activity, R.style.CustomDialogTheme);
        dialog.setContentView(R.layout.dialog_colectdata);

        final TextView tvIsStreaming = (TextView) dialog.findViewById(R.id.tvIsStreaming);
        final Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(200); //You can manage the time of the blink with this parameter
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);

        LineChart mChart = (LineChart) dialog.findViewById(R.id.mChart);
        ShowGraphActivity.setProperty(mChart, "Data Collection");

        TextView tvCollectWalkId = (TextView) dialog.findViewById(R.id.tvCollectWalkId);
        tvCollectWalkId.setText("Walk Id: " + walkId);

        final Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (calibrationDataChangeListener != null) {
                    calibrationDataChangeListener.onCalibDataChange();
                }
                dialog.dismiss();
            }
        });

        Button btnStartCollection = (Button) dialog.findViewById(R.id.btnStartCollection);
        btnStartCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvIsStreaming.setAnimation(anim);
                tvIsStreaming.setVisibility(View.VISIBLE);
                btnCancel.setEnabled(false);

                for (Sensor sensor : SensorRepo.getAllSensors(true)) {

                    sensor.setCurrLoggingState(GlobalConstants.LoggingStates.LOGGING);
                    sensor.startStream();


                }
            }
        });

        Button btnPauseCollection = (Button) dialog.findViewById(R.id.btnPauseCollection);
        btnPauseCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvIsStreaming.setAnimation(null);
                tvIsStreaming.setVisibility(View.GONE);
                for (Sensor sensor : SensorRepo.getAllSensors(true)) {
                    //  sensor.setFileSaveLocationAbs(saveFileAbsPath);
                    sensor.setCurrLoggingState(GlobalConstants.LoggingStates.PAUSED);
                    // sensor.startStream();


                }
            }
        });

        Button btnStopCollection = (Button) dialog.findViewById(R.id.btnStopCollection);
        btnStopCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvIsStreaming.setAnimation(null);
                tvIsStreaming.setVisibility(View.GONE);
                btnCancel.setEnabled(true);
                for (Sensor sensor : SensorRepo.getAllSensors(true)) {
                    sensor.setCurrLoggingState(GlobalConstants.LoggingStates.STOPPED);
                    sensor.stopStream();
//                    sensor.setFileSaveLocationAbs("");


                }
            }
        });


        dialog.setCancelable(false);

        dialog.show();
    }

    public static void setSelectedSensorChangeListener(SelectedSensorChangeListener selectedSensorChangeListener) {
        UtilsSensor.selectedSensorChangeListener = selectedSensorChangeListener;
    }

    public static void showSelectSensors(Activity activity) {

        final Dialog dialog = new Dialog(activity, R.style.CustomDialogTheme);
        dialog.setContentView(R.layout.dialog_selectsensors);

        //create an ArrayAdaptar from the String Array
        SelectSensorsAdapter dataAdapter = new SelectSensorsAdapter(activity.getBaseContext(),
                R.layout.card_sensor_choose, SensorRepo.getAllSensors(false));
        ListView listView = (ListView) dialog.findViewById(R.id.listAllSensors);
        // Assign adapter to ListView
        listView.setAdapter(dataAdapter);
        listView.setDivider(null);
        listView.setDividerHeight(0);

        Button btnOkSelectSensors = (Button) dialog.findViewById(R.id.btnOkSelectSensors);
        btnOkSelectSensors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedSensorChangeListener != null)
                    selectedSensorChangeListener.onSelectedSensorChange();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public static boolean selectedSensorsReady() {
        List<Sensor> selectedSensors = SensorRepo.getAllSensors(true);
        if (selectedSensors.size() == 0)
            return false;

        for (Sensor sensor : selectedSensors) {
            if (sensor.getPosition() == GlobalConstants.SensorPosition.UNKNOWN || sensor.getStatus() != GlobalConstants.SensorStatus.Connected || !sensor.getDeviceInfoValid())
                return false;
        }


        return true;
    }

    public static void unselectAllSensors() {
        for (Sensor sensor : SensorRepo.getAllSensors(true)) {
            sensor.setSelected(false);
        }
    }

    public static void unplaceAllSensors() {
        for (Sensor sensor : SensorRepo.getAllSensors(true)) {
            sensor.setPosition(GlobalConstants.SensorPosition.UNKNOWN);
        }
    }

    public interface SelectedSensorChangeListener {
        void onSelectedSensorChange();
    }

    public interface CalibrationDataChangeListener {
        void onCalibDataChange();
    }

}
