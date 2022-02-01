package csa.iisc.gaitanalysis.Activities;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import csa.iisc.gaitanalysis.DataModel.GlobalConstants;
import csa.iisc.gaitanalysis.DataModel.Sensor;
import csa.iisc.gaitanalysis.DataModel.SensorRepo;
import csa.iisc.gaitanalysis.ListAdapters.SensorsListAdapter;
import csa.iisc.gaitanalysis.R;
import csa.iisc.gaitanalysis.Utils.Utils;

public class SensorsDashboardActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static String TAG = "Gait Analysis";
    private SensorsListAdapter sensorsListAdapter;
    private boolean showOnlySelected = false;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_sensors);

        //Tool Bar
        toolbar = (Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Sensors Dashboard");
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
            showOnlySelected = extras.getBoolean("showOnlySelected", false);
        }

        findViewById(R.id.btnListSensors).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                populateList();
            }
        });

        findViewById(R.id.btnRefresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorsListAdapter.notifyDataSetChanged();
            }
        });

        populateList();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_ENABLE_BT){
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if(bluetoothAdapter.isEnabled()){
                SensorRepo.setAllSensors(Utils.CheckBluetoothState(bluetoothAdapter, SensorsDashboardActivity.this));
                 populateList();
            }
        }
    }

    private void populateList(){
        // This is the card view, it holds all the AvailableParameters
        final RecyclerView recList = (RecyclerView) findViewById(R.id.recycleViewSensors);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        recList.getRecycledViewPool().setMaxRecycledViews(1, 0);

        // This creates the data and then sets them to the card view.
        sensorsListAdapter = new SensorsListAdapter(SensorsDashboardActivity.this, showOnlySelected);
        recList.setAdapter(sensorsListAdapter);
    }



    @Override
    protected void onResume() {
        super.onResume();

        if (sensorsListAdapter != null)
        sensorsListAdapter.notifyDataSetChanged();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_sensor_connect, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int opId = item.getItemId();

        if (opId == R.id.action_connect_all) {
            Utils.showSimpleMessage(SensorsDashboardActivity.this, "Connecting all sensors. Please Wait.");
            for (Sensor sensor : SensorRepo.getAllSensors(true)) {
                if (sensor.getStatus() != GlobalConstants.SensorStatus.Connected) {
                    sensor.connectToSensor();
                }
            }
        } else if (opId == R.id.action_disconnect_all) {
            for (Sensor sensor : SensorRepo.getAllSensors(true)) {
                if (sensor.getStatus() != GlobalConstants.SensorStatus.Disconnected) {
                    sensor.disconnectSensor();
                }
            }
        } else if (opId == R.id.action_stream_all) {
            for (Sensor sensor : SensorRepo.getAllSensors(true)) {
                if (sensor.getStatus() != GlobalConstants.SensorStatus.Streaming) {
                    sensor.startStream();
                }
            }
        } else if (opId == R.id.action_stream_off) {
            for (Sensor sensor : SensorRepo.getAllSensors(true)) {
                sensor.stopStream();
            }
        }


        return super.onOptionsItemSelected(item);
    }


}

