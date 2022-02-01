package csa.iisc.gaitanalysis.Activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import csa.iisc.gaitanalysis.DataModel.Experiment;
import csa.iisc.gaitanalysis.DataModel.GlobalConstants;
import csa.iisc.gaitanalysis.DataModel.Walk;
import csa.iisc.gaitanalysis.R;
import csa.iisc.gaitanalysis.Utils.GlobalValues;

public class CollectWalkingTrialActivity extends AppCompatActivity {

    private static final String TAG = "ShowGraph";

    Experiment experiment;
    Integer walkIdx;
    private Toolbar toolbar;
    private LineChart mChart, mChart2;

    public static void setProperty(LineChart mChart, String title) {
        mChart.setDescription(title);
        mChart.setNoDataTextDescription("NO Data for the moment");
        mChart.setNoDataText("No Data for the Moment");
        mChart.setHighlightPerDragEnabled(true);
        mChart.setTouchEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setPinchZoom(true);
        mChart.setBackgroundColor(Color.WHITE);


        LineData data = new LineData();
        data.setValueTextColor(Color.BLACK);

        mChart.setData(data);

        Legend l = mChart.getLegend();

        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.BLACK);

        XAxis xl = mChart.getXAxis();
        xl.setTextColor(Color.BLACK);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);

        YAxis yLeftAxis = mChart.getAxisLeft();
        yLeftAxis.setTextColor(Color.BLACK);
//        yLeftAxis.setAxisMaxValue(120f);
        yLeftAxis.setDrawGridLines(true);
        yLeftAxis.setDrawTopYLabelEntry(false);

        YAxis yRighAxis = mChart.getAxisRight();
        yRighAxis.setEnabled(false);
        yRighAxis.setDrawTopYLabelEntry(false);
    }

    public static void addEntry(LineChart mChart, double val, String legend, float visibleEntries) {

        if (mChart == null) {
            Log.d(TAG, "addEntry: mchart was null");
            return;
        }

        //Log.d(TAG, "addEntry: Setting Values: "+val+" "+legend+" "+visibleEntries);
        LineData data = mChart.getData();

        if (data != null) {
            LineDataSet set = (LineDataSet) data.getDataSetByIndex(0);
            if (set == null) {
                set = createSet(legend);
                data.addDataSet(set);
            }

            data.addXValue("");

            data.addEntry(
                    new Entry(
                            (float) val, set.getEntryCount()
                    ), 0);

            mChart.notifyDataSetChanged();

            mChart.setVisibleXRange(1, visibleEntries);

            mChart.moveViewToX(data.getXValCount() - (visibleEntries + 1));
        }
    }

    private static LineDataSet createSet(String legend) {
        LineDataSet set = new LineDataSet(null, legend);
        set.setDrawCubic(true);
        set.setCubicIntensity(.2f);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.BLACK);
//        set.setCircleColor(ColorTemplate.getHoloBlue());
        set.setLineWidth(3f);

        set.setDrawCircles(false);

//        set.setCircleSize(4f);
//        set.setFillAlpha(65);
//        set.setFillColor(ColorTemplate.getHoloBlue());
//        set.setHighLightColor(Color.rgb(244,117,117));
        set.setDrawValues(false);
//        set.setValueTextColor(Color.BLACK);
//        set.setValueTextSize(10f);


        return set;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_graph);

        toolbar = (Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Perform Trial");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mChart = (LineChart) findViewById(R.id.mChart);
        mChart2 = (LineChart) findViewById(R.id.mChart2);

        Integer experimentIdx = getIntent().getIntExtra("experimentIdx", -1);
        walkIdx = getIntent().getIntExtra("walkIdx", -1);
        if (experimentIdx != -1 && walkIdx != -1) {
            experiment = GlobalValues.getGlobalValues().getAllExperiments().get(experimentIdx);
            getSupportActionBar().setTitle(experiment.getName() + ", Walk: " + (walkIdx + 1));

            setProperty(mChart, "Knee Flexion Extension Angle");
            setProperty(mChart2, "Ankle Flexion Extension Angle");

            experiment.getWalkData().get(walkIdx).setWalkChangeListener(new Walk.WalkChangeListener() {
                @Override
                public void onAngleCalculationDone() {

                }

                @Override
                public void onSingleAnglePointCalculation(double angleVal, GlobalConstants.WalkResultType walkResultType) {
                    switch (walkResultType) {

                        case RightKneeFlexionExtensionAngle:
                            try {
                                addEntry(mChart, angleVal, "Knee FE", 600f);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            break;
                        case RightAnkleFlexionExtensionAngle:
                            addEntry(mChart2, angleVal, "Ankle FE", 600f);
                            break;
                        case RightHeeslStrike:
                            break;
                        case RightToeOff:
                            break;
                        case LeftKneeFlexionExtensionAngle:
                            break;
                        case LeftAnkleFlexionExtensionAngle:
                            break;
                        case LeftHeeslStrike:
                            break;
                        case LeftToeOff:
                            break;
                    }

                }
            });

            if (experiment.isCalibrationDone(GlobalConstants.JointType.RightKnee))
                experiment.getWalkData().get(walkIdx).calAngles(GlobalConstants.WalkResultType.RightKneeFlexionExtensionAngle,
                        experiment.getAbsPath(), experiment.getCalibrationResult(GlobalConstants.JointType.RightKnee).get(0),
                        experiment.getCalibrationResult(GlobalConstants.JointType.RightKnee).get(1));

            if (experiment.isCalibrationDone(GlobalConstants.JointType.RightAnkle))
                experiment.getWalkData().get(walkIdx).calAngles(GlobalConstants.WalkResultType.RightAnkleFlexionExtensionAngle,
                        experiment.getAbsPath(), experiment.getCalibrationResult(GlobalConstants.JointType.RightAnkle).get(0),
                        experiment.getCalibrationResult(GlobalConstants.JointType.RightAnkle).get(1));

        }


    }


}
