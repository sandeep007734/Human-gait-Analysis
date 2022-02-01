package csa.iisc.gaitanalysis.Activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

import csa.iisc.gaitanalysis.ComputationModule.GaitAnalysis.SensorData;
import csa.iisc.gaitanalysis.ComputationModule.HeelStrikeToeOff.ShankHeelStrikeAndToeOffOffline;
import csa.iisc.gaitanalysis.DataModel.Experiment;
import csa.iisc.gaitanalysis.DataModel.GlobalConstants;
import csa.iisc.gaitanalysis.DataModel.Walk;
import csa.iisc.gaitanalysis.R;
import csa.iisc.gaitanalysis.Utils.GlobalValues;
import csa.iisc.gaitanalysis.Utils.Utils;

public class ShowGraphActivity extends AppCompatActivity {

    private static final String TAG = "ShowGraph";

    Experiment experiment;
    Integer walkIdx;
    private Toolbar toolbar;
    private LineChart mChart, mChart2;
    private Button btnCalcHSandTO, btnShowAvg;

    private List<Double> kneeAngleVals;
    private List<Integer> rightHS, rightTO;

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

    public static void addEntry(LineChart mChart, double val, String legend, float visibleEntries, int pos) {
        addEntry(mChart, val, legend, visibleEntries, pos, 0);
    }

    public static void addEntry(LineChart mChart, double val, String legend, float visibleEntries, int pos, int graphID) {

        if (mChart == null) {
            Log.d(TAG, "addEntry: mchart was null");
            return;
        }

        //Log.d(TAG, "addEntry: Setting Values: "+val+" "+legend+" "+visibleEntries);
        LineData data = mChart.getData();

        if (data != null) {
            LineDataSet set = (LineDataSet) data.getDataSetByIndex(graphID);
            if (set == null) {
                if (graphID == 0)
                    set = createSet(legend);
                else if (graphID == 1)
                    set = createSet(legend, GraphLineType.HEELSTRIKE);
                else
                    set = createSet(legend, GraphLineType.TOEOFF);
                data.addDataSet(set);
            }

            data.addXValue("");
            if (pos == -1)
                pos = set.getEntryCount();
//            if (pos > set.getEntryCount())
//                pos = set.getEntryCount();

            data.addEntry(
                    new Entry(
                            (float) val, pos
                    ), graphID);

            mChart.notifyDataSetChanged();
            mChart.setVisibleXRange(1, visibleEntries);
            mChart.moveViewToX(data.getXValCount() - (visibleEntries + 1));
        }
    }

    private enum GraphLineType {ANGLEDATA, HEELSTRIKE, TOEOFF}

    private static LineDataSet createSet(String legend) {
        return createSet(legend, GraphLineType.ANGLEDATA);
    }

    private static LineDataSet createSet(String legend, GraphLineType graphLineType) {
        LineDataSet set = new LineDataSet(null, legend);

        switch (graphLineType) {
            case ANGLEDATA:
                set.setDrawCubic(true);
                set.setCubicIntensity(.2f);
                set.setAxisDependency(YAxis.AxisDependency.LEFT);
                set.setColor(Color.BLACK);
                set.setLineWidth(3f);
                set.setDrawCircles(false);
                set.setDrawValues(false);
                break;
            case HEELSTRIKE:
                set.setDrawCubic(true);
                set.setCubicIntensity(.5f);
                set.setAxisDependency(YAxis.AxisDependency.LEFT);
                set.setColor(Color.RED);
                set.setLineWidth(0f);
                set.setDrawCircles(true);
                set.setDrawValues(true);


                set.setCircleColor(Color.RED);
                set.setCircleSize(4f);
                set.setFillAlpha(65);
                set.setFillColor(ColorTemplate.getHoloBlue());
                set.setHighLightColor(Color.rgb(244, 117, 117));
                set.setValueTextColor(Color.BLACK);
                set.setValueTextSize(10f);
                break;
            case TOEOFF:

                set.setDrawCubic(true);
                set.setCubicIntensity(.5f);
                set.setAxisDependency(YAxis.AxisDependency.LEFT);
                set.setColor(Color.BLUE);
                set.setLineWidth(0f);
                set.setDrawCircles(true);
                set.setDrawValues(true);

                set.setCircleColor(Color.BLUE);
                set.setCircleSize(4f);
                set.setFillAlpha(65);
                set.setFillColor(ColorTemplate.getHoloBlue());
                set.setHighLightColor(Color.rgb(244, 117, 117));
                set.setValueTextColor(Color.BLACK);
                set.setValueTextSize(10f);
                break;
        }


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
                                addEntry(mChart, angleVal, "Knee FE", 600f, -1);
                                kneeAngleVals.add(angleVal);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            break;
                        case RightAnkleFlexionExtensionAngle:
                            addEntry(mChart2, angleVal, "Ankle FE", 600f, -1);
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

            kneeAngleVals = new ArrayList<>();
            rightHS = new ArrayList<>();
            rightTO = new ArrayList<>();

            if (experiment.isCalibrationDone(GlobalConstants.JointType.RightKnee))
                experiment.getWalkData().get(walkIdx).calAngles(GlobalConstants.WalkResultType.RightKneeFlexionExtensionAngle,
                        experiment.getAbsPath(), experiment.getCalibrationResult(GlobalConstants.JointType.RightKnee).get(0),
                        experiment.getCalibrationResult(GlobalConstants.JointType.RightKnee).get(1));

            if (experiment.isCalibrationDone(GlobalConstants.JointType.RightAnkle))
                experiment.getWalkData().get(walkIdx).calAngles(GlobalConstants.WalkResultType.RightAnkleFlexionExtensionAngle,
                        experiment.getAbsPath(), experiment.getCalibrationResult(GlobalConstants.JointType.RightAnkle).get(0),
                        experiment.getCalibrationResult(GlobalConstants.JointType.RightAnkle).get(1));


            btnCalcHSandTO = (Button) findViewById(R.id.btnCalcHSandTO);
            btnCalcHSandTO.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String strHeelStrike="", strToeOff="";
                    SensorData rightShankData;
                    rightShankData = experiment.getWalkData().get(walkIdx).readData(GlobalConstants.SensorPosition.RIGHTSHANK, experiment.getAbsPath());
                    if (rightShankData == null)
                        Toast.makeText(ShowGraphActivity.this, "Error on reading sensor data", Toast.LENGTH_LONG).show();
                    else {
                        double[] inputData = new double[rightShankData.sizeOfData];
                        for (int i = 0; i < rightShankData.sizeOfData; i++) {
                            inputData[i] = rightShankData.gyro[i][0];
                        }

                        ShankHeelStrikeAndToeOffOffline.calcHSandTO(inputData);

                        rightHS.clear();
                        rightTO.clear();



                        for(Integer integer: ShankHeelStrikeAndToeOffOffline.HSShank){
                            rightHS.add(integer);
                            strHeelStrike+=Integer.toString(integer)+" ";
                            Log.i(TAG, "onClick: Heel Strike: "+integer);
                            addEntry(mChart, kneeAngleVals.get(integer), "Heel Strike", 600f, integer, 1);
                        }

                        for(Integer integer: ShankHeelStrikeAndToeOffOffline.TOShank){
                            rightTO.add(integer);
                            strToeOff+=Integer.toString(integer)+" ";
                            Log.i(TAG, "onClick: Toe Off: "+integer);
                            addEntry(mChart, kneeAngleVals.get(integer), "Toe Off", 600f, integer, 2);
                        }

                        Utils.showSimpleDialog(ShowGraphActivity.this,"Heel Strike and Toe off","Heel Strike:\n"+strHeelStrike+"\nToe Off:\n"+strToeOff,"OK");
                    }

                }
            });

            btnShowAvg = (Button) findViewById(R.id.btnShowAvg);
            btnShowAvg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(rightHS.size() < 3 || rightTO.size() < 2){
                        Utils.showSimpleMessage(ShowGraphActivity.this,"Not Enough Heel Strike or Toe Off Detected.");
                        return;
                    }

                    List<Double> avgAngles = new ArrayList<>();

                    for (int i = rightHS.get(0); i < rightHS.get(1); i++) {
                        avgAngles.add(kneeAngleVals.get(i));
                    }
                    Log.i(TAG, "onClick: Size of AvgAngles: "+avgAngles.size());
                    int ctr = 0;
                    for (int i = rightHS.get(1); i <= rightHS.get(2); i++) {
                        if (ctr < avgAngles.size()) {
                            Double oldVal = avgAngles.get(ctr);
                            avgAngles.remove(ctr);
                            avgAngles.add(ctr, (kneeAngleVals.get(i) + oldVal) / 2);
                        }
                        else
                            avgAngles.add(ctr, kneeAngleVals.get(i));

                        ctr++;
                    }

                    Log.i(TAG, "onClick: Size of AvgAngles: "+avgAngles.size());

                  mChart2.clear();
                    setProperty(mChart2, "Average Knee Flexion Extension Angle");

                    for (Double angleVal : avgAngles) {
                        addEntry(mChart2, angleVal, "Avg Knee FE", 600f, -1);
                    }

                }
            });


        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_sensor_graph, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_add_entry) {
            addEntry(mChart, 100, "Heel Strike", 600f, 100, 1);
        }

        return super.onOptionsItemSelected(item);
    }


}
