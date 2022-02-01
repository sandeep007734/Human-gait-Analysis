package csa.iisc.gaitanalysis.Activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import csa.iisc.gaitanalysis.DataModel.GlobalConstants;
import csa.iisc.gaitanalysis.DataModel.Sensor;
import csa.iisc.gaitanalysis.DataModel.SensorRepo;
import csa.iisc.gaitanalysis.R;

public class CollectCalibrationDataActivity extends AppCompatActivity {

    private static final String TAG = "CollectCalibrationData";
    private static CalibrationDataChangeListener calibrationDataChangeListener;
    Button btnStartCollection, btnPauseCollection, btnStopCollection;
    LineChart mChart, mChart2, mChart3;
    private String walkId;
    private long numDataPoints;

    public static void setCalibrationDataChangeListener(CalibrationDataChangeListener calibrationDataChangeListener) {
        CollectCalibrationDataActivity.calibrationDataChangeListener = calibrationDataChangeListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect_calibration_data);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            Log.d(TAG, "onCreate: No WalkId");
            finish();
        }
        walkId = extras.getString("walkId", "");
        if (walkId.isEmpty()) {
            Log.d(TAG, "onCreate: WalkId is Empty. Aborting");
            finish();
        }

        numDataPoints = 0;

        initEventListeners();
        initChangeListeners();
    }

    private void initChangeListeners() {
        final Sensor sensor = SensorRepo.getAllSensors(true).get(0);
        sensor.setOnNewValidAcquisitionDataListener(new Sensor.OnNewValidAcquisitionDataListener() {
            @Override
            public void onValidDataPointReceived(double val, GlobalConstants.DataAxis axis) {

                float maxPointsToBeShown = 50f;
                switch (axis) {
                    case XAxis:
                        ShowGraphActivity.addEntry(mChart, val, "Data", maxPointsToBeShown,-1);
                        numDataPoints++;
                        ((TextView)findViewById(R.id.tvNoOfDataPoints)).setText(Long.toString(numDataPoints));
                        break;
                    case YAxis:
                        ShowGraphActivity.addEntry(mChart2, val, "Data", maxPointsToBeShown,-1);
                        break;
                    case ZAxis:
                        ShowGraphActivity.addEntry(mChart3, val, "Data", maxPointsToBeShown,-1);
                        break;
                }

            }
        });
    }

    private void initEventListeners() {
//
//        final TextView tvIsStreaming = (TextView) findViewById(R.id.tvIsStreaming);
//        final Animation anim = new AlphaAnimation(0.0f, 1.0f);
//        anim.setDuration(200); //You can manage the time of the blink with this parameter
//        anim.setStartOffset(20);
//        anim.setRepeatMode(Animation.REVERSE);
//        anim.setRepeatCount(Animation.INFINITE);

        mChart = (LineChart) findViewById(R.id.mChart);
        ShowGraphActivity.setProperty(mChart, "Data Collection X");
        mChart2 = (LineChart) findViewById(R.id.mChart2);
        ShowGraphActivity.setProperty(mChart2, "Data Collection Y");
        mChart3 = (LineChart) findViewById(R.id.mChart3);
        ShowGraphActivity.setProperty(mChart3, "Data Collection Z");

        TextView tvCollectWalkId = (TextView) findViewById(R.id.tvCollectWalkId);
        tvCollectWalkId.setText("Walk Id: " + walkId);


        btnStartCollection = (Button) findViewById(R.id.btnStartCollection);
        btnStartCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                for (Sensor sensor : SensorRepo.getAllSensors(true)) {
//                    sensor.setCurrLoggingState(GlobalConstants.LoggingStates.LOGGING);
//                    sensor.startStream();
//                }

                (new SensorRepo()).startStreamAllSensors(true);
            }
        });

        btnPauseCollection = (Button) findViewById(R.id.btnPauseCollection);
        btnPauseCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                tvIsStreaming.setAnimation(null);
//                tvIsStreaming.setVisibility(View.GONE);
                for (Sensor sensor : SensorRepo.getAllSensors(true)) {

                    sensor.setCurrLoggingState(GlobalConstants.LoggingStates.PAUSED);
                    // sensor.startStream();


                }
            }
        });

        btnStopCollection = (Button) findViewById(R.id.btnStopCollection);
        btnStopCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                tvIsStreaming.setAnimation(null);
//                tvIsStreaming.setVisibility(View.GONE);

                for (Sensor sensor : SensorRepo.getAllSensors(true)) {
                    sensor.setCurrLoggingState(GlobalConstants.LoggingStates.STOPPED);
                    sensor.stopStream();


                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (calibrationDataChangeListener != null)
            calibrationDataChangeListener.onCalibrationDataChange();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (calibrationDataChangeListener != null)
            calibrationDataChangeListener.onCalibrationDataChange();
    }

    public interface CalibrationDataChangeListener {
        void onCalibrationDataChange();
    }
}
