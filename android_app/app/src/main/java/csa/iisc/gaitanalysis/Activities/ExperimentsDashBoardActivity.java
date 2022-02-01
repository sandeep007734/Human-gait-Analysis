package csa.iisc.gaitanalysis.Activities;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import csa.iisc.gaitanalysis.DataModel.Experiment;
import csa.iisc.gaitanalysis.DataModel.GlobalConstants;
import csa.iisc.gaitanalysis.DataModel.Sensor;
import csa.iisc.gaitanalysis.DataModel.SensorRepo;
import csa.iisc.gaitanalysis.ListAdapters.ExperimentsListAdapter;
import csa.iisc.gaitanalysis.R;
import csa.iisc.gaitanalysis.Utils.ConfirmDialog;
import csa.iisc.gaitanalysis.Utils.GlobalValues;
import csa.iisc.gaitanalysis.Utils.Utils;


public class ExperimentsDashBoardActivity extends AppCompatActivity {


    private static final String TAG = "ExpDashboardActivity";
    private static final int REQUEST_ENABLE_BT = 1;
    RecyclerView experimentList;
    int PICKFILE_REQUEST_CODE = 98;
    int ASK_TO_READ_FILE = 99;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //Tool Bar
        toolbar = (Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Experiments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, ASK_TO_READ_FILE);
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    GlobalConstants.PERMISSIONS_STORAGE,
                    GlobalConstants.REQUEST_EXTERNAL_STORAGE
            );
        }


        populateExperimentList();

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            finish();
        } else if (!mBluetoothAdapter.isEnabled()) {
            //Prompt user to turn on Bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            List<Sensor> allSensors = Utils.CheckBluetoothState(mBluetoothAdapter, ExperimentsDashBoardActivity.this);
            SensorRepo.setAllSensors(allSensors);

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICKFILE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                File file = new File(uri.toString());
                getListFiles(new File(file.getAbsolutePath() + "/Experiments"), false);
                populateExperimentList();
            }
        } else if (requestCode == ASK_TO_READ_FILE) {
            if (resultCode == RESULT_OK) {
                getListFiles(new File(GlobalValues.getGaitHomePath() + "/Experiments"), false);
                populateExperimentList();
            }

        } else if (requestCode == REQUEST_ENABLE_BT) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter.isEnabled()) {
                SensorRepo.setAllSensors(Utils.CheckBluetoothState(bluetoothAdapter, ExperimentsDashBoardActivity.this));

            }
        }

    }

    //    List<File> files = getListFiles(new File("YOUR ROOT"));
    private void getListFiles(File parentDir, Boolean reset) {


        if (!parentDir.exists()) {
            parentDir.mkdirs();
            Utils.showSimpleMessage(ExperimentsDashBoardActivity.this, "No Experiments", "No Experiments Detected");
            return;
        }

        if (!reset) {
            GlobalValues.getGlobalValues().getAllExperiments().clear();
        }

        File[] files = parentDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (reset) {
                    Utils.resetExperimentInfo(file);
                    continue;
                }

                if (file.isDirectory()) {
                    File[] infoFile = file.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String filename) {
                            return filename.equals("info.json");
                        }
                    });

                    if (infoFile.length == 0) {
                        Experiment experiment = new Experiment();
                        experiment.setName(file.getName());
                        experiment.setAbsPath(file.getAbsolutePath());
                        GlobalValues.getGlobalValues().addExperiment(experiment);
                    } else {
                        try {
                            if (!reset) {
                                Experiment experiment = Utils.readExpFromJson(infoFile[0].getAbsolutePath());
                                if (experiment != null) {
                                    GlobalValues.getGlobalValues().addExperiment(experiment);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }


                }
            }
        }
    }

    private void populateExperimentList() {

        getListFiles(new File(GlobalValues.getGaitHomePath() + "/Experiments"), false);

        // This is the card view, it holds all the AvailableParameters
        experimentList = (RecyclerView) findViewById(R.id.cardViewTrials);
        experimentList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        experimentList.setLayoutManager(llm);

        ExperimentsListAdapter experimentsListAdapter;
        experimentsListAdapter = new ExperimentsListAdapter(ExperimentsDashBoardActivity.this);
        experimentList.setAdapter(experimentsListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int opId = item.getItemId();

        if (opId == R.id.action_new_trial) {
            showNewTrialDialog();
        } else if (opId == R.id.action_exit) {
            finish();
        } else if (opId == R.id.action_reset) {
            ConfirmDialog confirmDialog = new ConfirmDialog();
            confirmDialog.setOnResultListener(new ConfirmDialog.OnResultListener() {
                @Override
                public void OnResultChange(Boolean result) {
                    if (result)
                        getListFiles(new File(GlobalValues.getGaitHomePath() + "/Experiments"), true);
                    getListFiles(new File(GlobalValues.getGaitHomePath() + "/Experiments"), false);
                    experimentList.getAdapter().notifyDataSetChanged();

                }
            });

            confirmDialog.showDialog(ExperimentsDashBoardActivity.this, "Reset", "Are you sure you want to reset all results?");

        } else if (opId == R.id.action_select_home_folder) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("file/*");
            startActivityForResult(intent, PICKFILE_REQUEST_CODE);
        } else if (opId == R.id.action_configure_sensors) {
            Intent sensorDashBoard = new Intent(ExperimentsDashBoardActivity.this, SensorsDashboardActivity.class);
            startActivity(sensorDashBoard);
        } else if (opId == R.id.action_refresh) {
            Snackbar.make(findViewById(android.R.id.content), "Refreshing", Snackbar.LENGTH_SHORT).show();
            populateExperimentList();
        }


        return super.onOptionsItemSelected(item);
    }

    private void showNewTrialDialog() {

        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("New Trial")
                .customView(R.layout.dialog_new_experiment, true).build();

        final TextView tvNewExperimentName = (TextView) dialog.getCustomView().findViewById(R.id.tvNewExperimentName);

        Button btnNewTrialOk = (Button) dialog.getCustomView().findViewById(R.id.btnNewTrialOk);
        btnNewTrialOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String expName = tvNewExperimentName.getText().toString();
                if (expName.equals("")) {
                    Snackbar.make(dialog.getCustomView().findViewById(R.id.scrollViewMainNewExp), "Name cannot be blank", Snackbar.LENGTH_LONG).show();
                } else if (!GlobalValues.getGlobalValues().isNameUnique(expName)) {
                    Snackbar.make(dialog.getCustomView().findViewById(R.id.scrollViewMainNewExp), "Name is not unique", Snackbar.LENGTH_LONG).show();
                } else {
                    Experiment experiment = new Experiment();
                    experiment.setName(expName);
                    experiment.setDateAndTime(Calendar.getInstance().getTime().toString());
                    experiment.setAbsPath(GlobalValues.getGaitExperimentPath() + "/" + expName);

                    Integer idx = GlobalValues.getGlobalValues().getAllExperiments().size();
                    GlobalValues.getGlobalValues().getAllExperiments().add(experiment);

                    experimentList.getAdapter().notifyDataSetChanged();
                    try {
                        Utils.saveExperiment(experiment);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    dialog.dismiss();
                }
            }
        });

        Button btnNewTrialCancel = (Button) dialog.getCustomView().findViewById(R.id.btnNewTrialCancel);
        btnNewTrialCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateExperimentList();
    }


}
