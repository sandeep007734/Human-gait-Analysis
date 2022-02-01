package csa.iisc.gaitanalysis.Activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import csa.iisc.gaitanalysis.ComputationModule.Calibration.CalibrationPrimaryLoop;
import csa.iisc.gaitanalysis.ComputationModule.DataModel.DataTransferJVals;
import csa.iisc.gaitanalysis.DataModel.Experiment;
import csa.iisc.gaitanalysis.DataModel.GlobalConstants;
import csa.iisc.gaitanalysis.DataModel.JointCalibration;
import csa.iisc.gaitanalysis.DataModel.Sensor;
import csa.iisc.gaitanalysis.DataModel.SensorRepo;
import csa.iisc.gaitanalysis.ListAdapters.WalkListAdapter;
import csa.iisc.gaitanalysis.R;
import csa.iisc.gaitanalysis.Utils.GlobalValues;
import csa.iisc.gaitanalysis.Utils.Utils;
import csa.iisc.gaitanalysis.Utils.UtilsData;
import csa.iisc.gaitanalysis.Utils.UtilsSensor;

//import Calibration.DoCalibration;
//import GaitAnalysis.SensorData;
//import Utils.ComputationUtils;

public class PerformExperimentActivity extends AppCompatActivity {

    private static final String TAG = "PerformActivity";

    public static ProgressDialog progress, sensorConnectProgress;

    private Button btnConnectSelectedSensors, btnPerformWalkingTrial, btnDoCalibration, btnRefreshInfo,
            btnCollectCalibrationData, btnDisconnect;

    private RecyclerView cardViewWalks;
    private TextView tvNoWalkingTrials, tvThreadStatus, tvCalibrationStatus, tvPerformTrialSensorStatus,
            tvPerformTrialPacketType, tvPerformTrialPositions, tvInfoSaveLocation;
    private WalkListAdapter walkListAdapter;
    private RecyclerView recList;
    private ImageView imgBtnShowHideSensors, imgBtnShowHideCalibration, imgBtnShowHideWalkingTrials;
    private ImageView imgRefreshSensorStatus;
    private LinearLayout lyMainDashboard, lySensors, lyCalibration, lyTrials;
    private Experiment experiment;
    private Integer experimentIdx;
    private ImageView imgConfigSensors, imgSelectSensors, imgResetCalibrationData, imgLoadJVals;
    private Spinner spnJointsAvailable, senCaliDataAvailable;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perform_experiment);

        //Tool Bar
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


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            experimentIdx = extras.getInt("experimentIdx", -1);
            if (experimentIdx == -1) {
                // THis is the new Trial.
            } else {
                //Some Old Experiment
                experiment = GlobalValues.getGlobalValues().getAllExperiments().get(experimentIdx);
                getSupportActionBar().setTitle(experiment.getName());
            }

        }

        setUpListeners();

        populateWalkList();
        //populateCalibrationInfo();
        populateSensorInfo();
        setSelectedSensors();
        populateSpinnersPossibleJointType();
        populateSpinnersPossibleCalibJointType();


        if (experiment != null)
            tvInfoSaveLocation.setText("Save path: " + experiment.getAbsPath());

    }

    private void setSelectedSensors() {
        UtilsSensor.unselectAllSensors();
        UtilsSensor.unplaceAllSensors();

        List<String> selectedSensors = experiment.getSelectedSensors();
        List<String> selectedSensorsPositions = experiment.getSelectedSensorsPositions();
        if (selectedSensors == null || selectedSensors.size() == 0 || selectedSensorsPositions == null
                || selectedSensorsPositions.size() == 0 || (selectedSensorsPositions.size() != selectedSensors.size())) {
            return;
        }

        int idx = 0;
        for (Sensor sensor : SensorRepo.getAllSensors(false)) {
            if (selectedSensors.contains(sensor.getName())) {
                sensor.setSelected(true);
                sensor.setPosition(GlobalConstants.SensorPosition.getPositionFromString(selectedSensorsPositions.get(idx)));
                idx++;
            }
        }
    }

    private void populateSensorInfo() {
        int countSensorsConnected = 0;
        if (SensorRepo.allSensors != null) {
            tvPerformTrialPositions.setText("");
            for (Sensor sensor : SensorRepo.getAllSensors(true)) {
                if (sensor.getStatus() == GlobalConstants.SensorStatus.Connected) {
                    countSensorsConnected++;
                    tvPerformTrialPositions.setText(tvPerformTrialPositions.getText() + "\n" + sensor.getPosition() );
                }
            }
            tvPerformTrialSensorStatus.setText("Total Sensors Connected: " + countSensorsConnected);
            tvPerformTrialPacketType.setText("Packet Type: " + checkConnectedSensorsStreamType());


        } else {
            tvPerformTrialSensorStatus.setText("Please Configure Sensors");
            tvPerformTrialPacketType.setText("Please Configure Sensors");
            tvPerformTrialPositions.setText("Please Configure Sensors");
        }
    }

    private String checkConnectedSensorsStreamType() {
        String sensorType = "No Sensor is Connected";
        for (Sensor sensor : SensorRepo.allSensors) {
            if (sensor.getStatus() == GlobalConstants.SensorStatus.Connected) {
                if (sensorType.equalsIgnoreCase("No Sensor is Connected"))
                    sensorType = sensor.getSensorDeviceInfo().getPktType();
                else {
                    if (!sensorType.equalsIgnoreCase(sensor.getSensorDeviceInfo().getPktType())) {
                        sensorType = "Different. Please Correct";
                        break;
                    }
                }
            }
        }

        return sensorType;
    }


    private void viewCalibrationDataNotPresent() {
        //TODO fix this for all the joint types
        tvCalibrationStatus.setText("No Calibration Data Present");

    }

    private void viewCalibrationDataPresent(GlobalConstants.JointType jointType) {

        tvCalibrationStatus.setText("Calibration Data Present. " + experiment.getCalibrationData(jointType).size());

    }

    private void viewCalibrationProcessed(GlobalConstants.JointType jointType) {

        Log.d(TAG, "viewCalibrationProcessed: " + jointType.toString());

        tvCalibrationStatus.setText("Calibration Result for: " + jointType.toString() + "\nj1_valf: " +
                String.format("%.2f", (experiment.getCalibrationResult(jointType).get(0)[0])) + ", " +
                String.format("%.2f", (experiment.getCalibrationResult(jointType).get(0)[1])) + ", " +
                String.format("%.2f", (experiment.getCalibrationResult(jointType).get(0)[2]))
                + "\nj2_valf: " +
                String.format("%.2f", (experiment.getCalibrationResult(jointType).get(1)[0])) + ", " +
                String.format("%.2f", (experiment.getCalibrationResult(jointType).get(1)[1])) + ", " +
                String.format("%.2f", (experiment.getCalibrationResult(jointType).get(1)[2]))
        );

        if (progress != null)
            progress.dismiss();
    }

    private void viewCalibrationRunning() {

        progress = new ProgressDialog(this);
        progress.setMessage("Calibration Running...");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(false);
        progress.setProgress(0);

        progress.setCancelable(false);
        progress.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CalibrationPrimaryLoop.cancelRunning = true;
                dialog.dismiss();
            }
        });

        progress.show();


    }

    private void populateSpinnersPossibleJointType() {

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, UtilsData.getPossibleJointsFromConnectedSensorsStr());
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnJointsAvailable.setAdapter(dataAdapter);
    }

    private void populateSpinnersPossibleCalibJointType() {

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, experiment.getValidCalibration());
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        senCaliDataAvailable.setAdapter(dataAdapter);
    }

    private void setUpSensorEventListeners() {

        btnDisconnect = (Button) findViewById(R.id.btnDisconnect);
        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Sensor sensor : SensorRepo.getAllSensors(true)) {
                    sensor.disconnectSensor();
                }
            }
        });

        imgConfigSensors = (ImageView) findViewById(R.id.imgConfigSensors);
        imgSelectSensors = (ImageView) findViewById(R.id.imgSelectSensors);

        imgConfigSensors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SensorRepo.getAllSensors(true).size() == 0) {
                    Utils.showSimpleMessage(PerformExperimentActivity.this, "Please Select Sensors to be used.");
                    return;
                }

                Intent sensorDashBoard = new Intent(PerformExperimentActivity.this, SensorsDashboardActivity.class);
                Bundle extras = new Bundle();
                extras.putBoolean("showOnlySelected", true);
                sensorDashBoard.putExtras(extras);
                startActivity(sensorDashBoard);
            }
        });

        imgSelectSensors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UtilsSensor.setSelectedSensorChangeListener(new UtilsSensor.SelectedSensorChangeListener() {
                    @Override
                    public void onSelectedSensorChange() {
                        experiment.setSelectedSensors(SensorRepo.getAllSensorsName(true));
                        experiment.setSelectedSensorsPositions(SensorRepo.getAllSensorsPositionStr(true));
                        populateSpinnersPossibleJointType();

                        try {
                            Utils.saveExperiment(experiment);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                UtilsSensor.showSelectSensors(PerformExperimentActivity.this);
            }
        });

        imgRefreshSensorStatus = (ImageView) findViewById(R.id.imgRefreshSensorStatus);
        imgRefreshSensorStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(findViewById(android.R.id.content), "Refreshing", Snackbar.LENGTH_SHORT).show();
                populateSensorInfo();
            }
        });

        btnConnectSelectedSensors = (Button) findViewById(R.id.btnConnectSelectedSensors);
        btnConnectSelectedSensors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SensorRepo.getAllSensors(true).size() == 0) {
                    Utils.showSimpleMessage(PerformExperimentActivity.this, "Please Select Sensors to use.");
                    return;
                }



                for (Sensor sensor : SensorRepo.getAllSensors(true)) {
                    if (sensor.getStatus() != GlobalConstants.SensorStatus.Connected) {
                        sensor.connectToSensor();
                    }
                }
            }
        });

        SensorRepo.setSensorConnectingListener(new SensorRepo.SensorConnectingListener() {
            @Override
            public void onSensorConnectingCountChange(int newCount) {
                Log.i(TAG, "onSensorConnectingCountChange: New Count: "+newCount);
                if(newCount >0){
                    Log.i(TAG, "onSensorConnectingCountChange: Showing the Progress");
                    if(sensorConnectProgress == null || !sensorConnectProgress.isShowing())

                    sensorConnectProgress = new ProgressDialog(PerformExperimentActivity.this);
                    sensorConnectProgress.setIndeterminate(true);
                    sensorConnectProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    sensorConnectProgress.setCancelable(true);
                    sensorConnectProgress.setTitle("Please Wait..");
                    sensorConnectProgress.setMessage("Sensors Connecting..");
                    sensorConnectProgress.show();
                }else{
                    Log.i(TAG, "onSensorConnectingCountChange: Dismissing the progress dialog");
                    sensorConnectProgress.dismiss();
                }
            }
        });
        

        btnRefreshInfo = (Button) findViewById(R.id.btnRefreshInfo);
        btnRefreshInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Trying to fix info");
                for (Sensor sensor : SensorRepo.getAllSensors(true)) {
                    if (!sensor.getDeviceInfoValid())
                        sensor.refreshDeviceInfo();

                    sensor.fixTheListeners();
                }
            }
        });


        tvPerformTrialSensorStatus = (TextView) findViewById(R.id.tvPerformTrialSensorStatus);
        tvPerformTrialPacketType = (TextView) findViewById(R.id.tvPerformTrialPacketType);
        tvPerformTrialPositions = (TextView) findViewById(R.id.tvPerformTrialPositions);
        tvInfoSaveLocation = (TextView) findViewById(R.id.tvInfoSaveLocation);

    }

    private void setUpCalibrationEventListeners() {

        spnJointsAvailable = (Spinner) findViewById(R.id.spnJointsAvailable);
        senCaliDataAvailable = (Spinner) findViewById(R.id.spnCalibDataAvailable);

        senCaliDataAvailable.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                GlobalConstants.JointType jointType = GlobalConstants.JointType.getJointTypeFromName(senCaliDataAvailable.getSelectedItem().toString());
                if (experiment.isCalibrationDone(jointType)) {
                    viewCalibrationProcessed(jointType);
                } else {
                    tvCalibrationStatus.setText("Calibration not done for: " + jointType.toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                tvCalibrationStatus.setText("Select Calibration File to see the reuslt.");
            }
        });


        tvThreadStatus = (TextView) findViewById(R.id.tvThreadStatus);
        tvCalibrationStatus = (TextView) findViewById(R.id.tvCalibrationStatus);

        imgLoadJVals = (ImageView) findViewById(R.id.imgLoadJVals);
        imgLoadJVals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataTransferJVals dataTransferJVals = Utils.readjvalsFromJson(experiment.getAbsPath()+"/jVals.json");
                if(dataTransferJVals==null){
                    Utils.showSimpleDialog(PerformExperimentActivity.this,"No Calib Result","No Calibration Result Found.","OK");
                    return;
                }

                List<double[]> jVals = new ArrayList<double[]>();
                jVals.add(dataTransferJVals.getJ1_val());
                jVals.add(dataTransferJVals.getJ2_val());

                //TODO remove this hardcode
                experiment.setCalibrationResult(jVals, GlobalConstants.JointType.RightKnee);
                viewCalibrationProcessed(GlobalConstants.JointType.RightKnee);

            }
        });

        imgResetCalibrationData = (ImageView) findViewById(R.id.imgResetCalibrationData);
        imgResetCalibrationData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.setOnDialogResultChangeListener(new Utils.OnDialogResultChangeListener() {
                    @Override
                    public void onDialogResultChange(boolean result) {
                        if (result) {
                            experiment.resetAllCalibration();
                            Utils.setOnDialogResultChangeListener(null);
                            populateSpinnersPossibleCalibJointType();
                            viewCalibrationDataNotPresent();
                        }

                        Utils.setOnDialogResultChangeListener(null);
                    }
                });
                Utils.showSimpleDialog(PerformExperimentActivity.this, "Info", "Are you sure you want to reset Calibration Data ?", "OK", "Cancel");

            }
        });


        btnCollectCalibrationData = (Button) findViewById(R.id.btnCollectCalibrationData);
        btnCollectCalibrationData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!UtilsSensor.selectedSensorsReady()) {
                    Utils.showSimpleMessage(PerformExperimentActivity.this, "Sensors not Ready. Please make sure all the selected sensors are ready.");
                    return;
                }

                final GlobalConstants.JointType jointType = GlobalConstants.JointType.getJointTypeFromName(spnJointsAvailable.getSelectedItem().toString());
                if (experiment.isCalibrationDataPresent(jointType)) {
                    Utils.setOnDialogResultChangeListener(new Utils.OnDialogResultChangeListener() {
                        @Override
                        public void onDialogResultChange(boolean result) {
                            if (result) {
                                experiment.resetCalibration(jointType);
                                Log.d(TAG, "onClick: Selected Joint type is: " + jointType.toString());
                                String walkId = experiment.addCalibrationFile(jointType);
                                UtilsSensor.showCalibCollectActivity(PerformExperimentActivity.this, walkId);

                                CollectCalibrationDataActivity.setCalibrationDataChangeListener(new CollectCalibrationDataActivity.CalibrationDataChangeListener() {
                                    @Override
                                    public void onCalibrationDataChange() {
                                        //populateCalibrationInfo();
                                        populateSpinnersPossibleCalibJointType();
                                    }
                                });
                            }

                            Utils.setOnDialogResultChangeListener(null);
                        }
                    });

                    Utils.showSimpleDialog(PerformExperimentActivity.this, "Data Present", "Calibration Data is present for the joint: " + jointType.toString() + ". Replace ?", "OK", "Cancel");
                } else {
                    Log.d(TAG, "onClick: Selected Joint type is: " + jointType.toString());
                    String walkId = experiment.addCalibrationFile(jointType);
                    UtilsSensor.showCalibCollectActivity(PerformExperimentActivity.this, walkId);

                    CollectCalibrationDataActivity.setCalibrationDataChangeListener(new CollectCalibrationDataActivity.CalibrationDataChangeListener() {
                        @Override
                        public void onCalibrationDataChange() {
                            //populateCalibrationInfo();
                            populateSpinnersPossibleCalibJointType();
                        }
                    });
                }


            }
        });

        btnDoCalibration = (Button) findViewById(R.id.btnDoCalibration);
        btnDoCalibration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final GlobalConstants.JointType jointType = GlobalConstants.JointType.getJointTypeFromName(senCaliDataAvailable.getSelectedItem().toString());

                if (!experiment.isCalibrationDataPresent(jointType)) {
                    Utils.showSimpleMessage(PerformExperimentActivity.this, "No Calibration Data Present");
                    return;
                }

                experiment.getJoint(jointType).setJointCalibrationChangeListner(new JointCalibration.JointCalibrationChangeListner() {
                    @Override
                    public void onCalibrationResultChange(boolean isResultValid, final List<double[]> resultData) {
                        if (isResultValid) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    experiment.setCalibrationResult(resultData, jointType);
                                    viewCalibrationProcessed(jointType);
                                    try {
                                        Utils.saveExperiment(experiment);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } else {
//                            Utils.showSimpleDialog(PerformExperimentActivity.this,"Error","There seems to be an error in the Calibration process. Please try again.","OK");
                            Log.e(TAG, "onCalibrationResultChange: Error in the calibration process.");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    viewCalibrationDataPresent(jointType);
                                }
                            });
                        }

                    }
                });


                if (experiment.isCalibrationDone(jointType)) {
                    Utils.setOnDialogResultChangeListener(new Utils.OnDialogResultChangeListener() {
                        @Override
                        public void onDialogResultChange(boolean result) {
                            if (!result) {
                                return;
                            } else {
                                viewCalibrationRunning();
                                experiment.getJoint(jointType).runTheCalibration(PerformExperimentActivity.this, experiment.getAbsPath());
                            }

                            Utils.setOnDialogResultChangeListener(null);
                        }
                    });

                    Utils.showSimpleDialog(PerformExperimentActivity.this,
                            "Calibration Already Done", "Run Calibration again ? It wil take time.", "Yes", "Cancel");
                } else {
                    viewCalibrationRunning();
                    experiment.getJoint(jointType).runTheCalibration(PerformExperimentActivity.this, experiment.getAbsPath());
                }


            }
        });


    }

    private void setupWalkEventListeners() {
        cardViewWalks = (RecyclerView) findViewById(R.id.cardViewWalks);
        tvNoWalkingTrials = (TextView) findViewById(R.id.tvNoWalkingTrials);


        btnPerformWalkingTrial = (Button) findViewById(R.id.btnPerformWalkingTrial);
        btnPerformWalkingTrial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String walkId = experiment.addNewWalkingTrial();
                UtilsSensor.showCalibCollectActivity(PerformExperimentActivity.this, walkId);

                if (walkListAdapter == null || walkListAdapter.getItemCount() == 0) {
                    cardViewWalks.setVisibility(View.GONE);
                    tvNoWalkingTrials.setVisibility(View.VISIBLE);
                } else {
                    cardViewWalks.setVisibility(View.VISIBLE);
                    tvNoWalkingTrials.setVisibility(View.GONE);
                }

                recList.getAdapter().notifyDataSetChanged();
            }
        });

        experiment.setWalkTrialChangeListener(new Experiment.WalkTrialChangeListener() {
            @Override
            public void onWalkChange() {
                populateWalkList();
            }
        });

    }

    private void setUpListeners() {

        Sensor.setGlobalSensorChangeListener(new Sensor.GlobalSensorChangeListener() {
            @Override
            public void onDeviceInfoChange(Boolean isValid) {
                populateSensorInfo();
            }

            @Override
            public void onDeviceConnectionChange() {
                populateSensorInfo();
            }
        });
        setUpSensorEventListeners();
        setUpCalibrationEventListeners();
        setupWalkEventListeners();


        lyCalibration = (LinearLayout) findViewById(R.id.lyCalibration);
        lySensors = (LinearLayout) findViewById(R.id.lySensors);
        lyTrials = (LinearLayout) findViewById(R.id.lyTrials);

        imgBtnShowHideSensors = (ImageView) findViewById(R.id.imgBtnShowHideSensors);
        imgBtnShowHideCalibration = (ImageView) findViewById(R.id.imgBtnShowHideCalibration);
        imgBtnShowHideWalkingTrials = (ImageView) findViewById(R.id.imgBtnShowHideWalkingTrials);


        imgBtnShowHideWalkingTrials.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lyTrials.getVisibility() == View.VISIBLE) {
                    lyTrials.setVisibility(View.GONE);
                    imgBtnShowHideWalkingTrials.setImageDrawable(getDrawable(R.drawable.ic_keyboard_arrow_down_black_48dp));
                } else {
                    lyTrials.setVisibility(View.VISIBLE);
                    imgBtnShowHideWalkingTrials.setImageDrawable(getDrawable(R.drawable.ic_keyboard_arrow_up_black_48dp));
                }
            }
        });

        imgBtnShowHideSensors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lySensors.getVisibility() == View.VISIBLE) {
                    lySensors.setVisibility(View.GONE);
                    imgBtnShowHideSensors.setImageDrawable(getDrawable(R.drawable.ic_keyboard_arrow_down_black_48dp));
                } else {
                    lySensors.setVisibility(View.VISIBLE);
                    imgBtnShowHideSensors.setImageDrawable(getDrawable(R.drawable.ic_keyboard_arrow_up_black_48dp));
                }
            }
        });

        imgBtnShowHideCalibration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lyCalibration.getVisibility() == View.VISIBLE) {
                    lyCalibration.setVisibility(View.GONE);
                    imgBtnShowHideCalibration.setImageDrawable(getDrawable(R.drawable.ic_keyboard_arrow_down_black_48dp));
                } else {
                    lyCalibration.setVisibility(View.VISIBLE);
                    imgBtnShowHideCalibration.setImageDrawable(getDrawable(R.drawable.ic_keyboard_arrow_up_black_48dp));
                }
            }
        });


    }

    private void populateWalkList() {
        // This is the card view, it holds all the AvailableParameters
        recList = (RecyclerView) findViewById(R.id.cardViewWalks);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        walkListAdapter = new WalkListAdapter(PerformExperimentActivity.this, experiment, experimentIdx);
        recList.setAdapter(walkListAdapter);


        if (experiment != null) {


            if (!experiment.isWalkDataPresent()) {
                cardViewWalks.setVisibility(View.GONE);
                tvNoWalkingTrials.setVisibility(View.VISIBLE);
            } else {
                cardViewWalks.setVisibility(View.VISIBLE);
                tvNoWalkingTrials.setVisibility(View.GONE);
                for (int wi = 0; wi < experiment.getWalkData().get(0).getWalkCount(); wi++) {
                    walkListAdapter.addItem("Walk " + Integer.toString(wi + 1));
                }
                recList.getAdapter().notifyDataSetChanged();

                if (walkListAdapter.getItemCount() == 0) {
                    cardViewWalks.setVisibility(View.GONE);
                    tvNoWalkingTrials.setVisibility(View.VISIBLE);
                } else {
                    cardViewWalks.setVisibility(View.VISIBLE);
                    tvNoWalkingTrials.setVisibility(View.GONE);
                }

            }
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
//        try {
//            Utils.saveExperiment(experiment);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateSensorInfo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_performtrial, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int opId = item.getItemId();

        if (opId == R.id.action_save_trial) {
            finish();
        } else if (opId == R.id.action_discard_Trial) {
            Utils.setOnDialogResultChangeListener(new Utils.OnDialogResultChangeListener() {
                @Override
                public void onDialogResultChange(boolean result) {
                    if (result) {
                        Utils.deleteExperiment(experiment);
                        finish();
                    }

                    Utils.setOnDialogResultChangeListener(null);
                }
            });

            Utils.showSimpleDialog(PerformExperimentActivity.this, "Delete", "Are you sure you want to delete the experiment: " + experiment.getName(), "OK", "Cancel");


        }


        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onStart() {
        super.onStart();

    }
}
