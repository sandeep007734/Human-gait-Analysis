package csa.iisc.gaitanalysis.Activities;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import csa.iisc.gaitanalysis.BluetoothManager.BluetoothSPP;
import csa.iisc.gaitanalysis.BluetoothManager.BluetoothState;
import csa.iisc.gaitanalysis.CommunicationModule.StreamData;
import csa.iisc.gaitanalysis.DataModel.GlobalConstants.SensorParamType;
import csa.iisc.gaitanalysis.DataModel.GlobalConstants.SensorStatus;
import csa.iisc.gaitanalysis.DataModel.Sensor;
import csa.iisc.gaitanalysis.DataModel.SensorRepo;
import csa.iisc.gaitanalysis.R;
import csa.iisc.gaitanalysis.Utils.DialogSelection;
import csa.iisc.gaitanalysis.Utils.GetDeviceInfo;
import csa.iisc.gaitanalysis.Utils.InstanceVals;
import csa.iisc.gaitanalysis.Utils.ReadHeader;
import csa.iisc.gaitanalysis.Utils.Utils;

public class SensorHomeActivity extends AppCompatActivity {

    private static Sensor currSensor;
    private static Integer currSensorIdx;
    private static String TAG = "Sensor Home";
    TextView tvMain;
    List<SensorParamType> allParams = new ArrayList<>();
    List<String> streamTextData;
    private TextView tvPosition, tvMacAdd, tvAccfs, tvGyroFs, tvPktType, tvOrientAlgo, tvSampleRate, tvSensorBatt;
    private ToggleButton toggleConnect, toggleStream;
    private Button btnParamMode;
    private SensorParamType currSensorParamType = SensorParamType.swInfo;
    private String paramName = currSensorParamType.toString();
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor__home);

        //Tool Bar
        toolbar = (Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        if (getIntent().getExtras() != null) {
            Integer sensorIdx = getIntent().getExtras().getInt("sensorIdx");
            currSensorIdx = sensorIdx;
            currSensor = SensorRepo.getSensorAt(sensorIdx);
        }


        getSupportActionBar().setTitle(currSensor.getName());

        setupControls();
        setBluetoothListeners();
        setupSensorListeners();

        setTextOnControls();
        if (currSensor != null) {
            if (currSensor.getStatus() == SensorStatus.Connected) {
                viewOnSensorConnected();
            } else {
                viewOnSensorDisconnected();
            }

            if (currSensor.isStreaming()) {
                viewOnSensorStreamOn();
            } else {
                viewOnSensorStreamOff();
            }
        }

    }

    private void setTextOnControls() {
        if (currSensor != null) {
            tvPosition.setText(currSensor.getPosition().toString());
            tvMacAdd.setText(currSensor.getMacAddress());
            tvAccfs.setText(currSensor.getSensorDeviceInfo().getAccFs());
            tvGyroFs.setText(currSensor.getSensorDeviceInfo().getGyroFS());
            tvPktType.setText(currSensor.getSensorDeviceInfo().getPktType());
            tvOrientAlgo.setText(currSensor.getSensorDeviceInfo().getOrientAlgo());
            tvSampleRate.setText(currSensor.getSensorDeviceInfo().getSampleRate());
        } else {
            clearText();
        }
    }

    private void clearText() {
        tvPosition.setText("");
        tvMacAdd.setText("");
        tvAccfs.setText("");
        tvGyroFs.setText("");
        tvPktType.setText("");
        tvOrientAlgo.setText("");
        tvSampleRate.setText("");
    }

    private void viewOnSensorConnected() {
        toggleConnect.setBackgroundColor(Color.parseColor("#008000"));
        toggleConnect.setTextOn("Connected");
        toggleConnect.setChecked(true);

    }

    private void viewOnSensorDisconnected() {
        toggleConnect.setBackgroundColor(Color.RED);
        toggleConnect.setTextOff("Disconnected");
        toggleConnect.setChecked(false);

    }

    private void viewOnError() {
        toggleConnect.setBackgroundColor(Color.BLACK);
        toggleConnect.setTextOff("ERROR");
        toggleConnect.setChecked(false);

    }

    private void viewOnSensorStreamOn() {
        toggleStream.setChecked(true);
        toggleStream.setBackgroundDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ripple_blue));
    }

    private void viewOnSensorStreamOff() {
        toggleStream.setChecked(false);
        toggleStream.setBackgroundColor(Color.RED);
    }

    private void setupControls() {

        streamTextData = new ArrayList<>();


        tvPosition = (TextView) findViewById(R.id.tvSensorPos);
        tvMacAdd = (TextView) findViewById(R.id.tvmacAdd);
        tvAccfs = (TextView) findViewById(R.id.tvAccFS);
        tvGyroFs = (TextView) findViewById(R.id.tvGyroFS);
        tvPktType = (TextView) findViewById(R.id.tvPacketType);
        tvOrientAlgo = (TextView) findViewById(R.id.tvOrientAlgo);
        tvSampleRate = (TextView) findViewById(R.id.tvSampleRate);
        tvSensorBatt = (TextView) findViewById(R.id.tvSensorBatt);

        tvMain = (TextView) findViewById(R.id.tvData);


        toggleConnect = (ToggleButton) findViewById(R.id.toggleConnect);

        toggleConnect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (currSensor.getStatus() != SensorStatus.Connected) {
                        currSensor.setStatus(SensorStatus.Connecting);
                        connectToSensor();
                    }
                } else {
                    if (currSensor.getStatus() != SensorStatus.Disconnected) {
                        currSensor.setStatus(SensorStatus.Disconnecting);
                        disconnectSensor();
                    }
                }
            }
        });

        toggleStream = (ToggleButton) findViewById(R.id.toggleStream);
        toggleStream.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    try {


                        if (!currSensor.isStreaming()) {
                            tvMain.setText("");
//
                            currSensor.startStream();
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {

                    InstanceVals.setExpectedDataType(InstanceVals.possibleRecType.DUMP, 0);

                    if (currSensor.isStreaming()) {
                        currSensor.stopStream();
                        // todo flush this? EDIT1: Nee more ipection
                        if (streamTextData != null) {
                            try {
                                // flushText("SandeepGopiLaura.txt");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            streamTextData.clear();
                        }
                    }
                }
            }
        });

        findViewById(R.id.btnGetParameters).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParameters(currSensorParamType);
            }
        });


        btnParamMode = (Button) findViewById(R.id.btnParamMode);
        btnParamMode.setText(currSensorParamType.toString());
        btnParamMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Parameter to get Info");
                String[] AvailableParameters = {"Software Info", "Hardware Info",
                        "Bluetooth Name", "Acceleration Full Scale", "Gyroscope Full Scale", "Packet Type", "Orientation Algorithm",
                        "Sample Rate", "Stream Algo", "SWRFD", "Wakeup Mode"
                };
                builder.setItems(AvailableParameters, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {

                        currSensorParamType = DialogSelection.getSensprParamType(item);
                        btnParamMode.setText(currSensorParamType.toString());
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

    }

    private void setupSensorListeners() {
        if (currSensor == null) {
            return;
        }
        currSensor.setSensorStatusChangeListener(new Sensor.OnSensorStatusChangeListener() {
            @Override
            public void onStatusChanged(SensorStatus sensorStatus) {
                switch (sensorStatus) {
                    case Connected:
                        viewOnSensorConnected();
                        break;

                    case Connecting:
                        toggleConnect.setTextOn("Connecting");
                        break;

                    case Disconnected:
                        viewOnSensorDisconnected();
                        clearText();
                        break;

                    case Disconnecting:
//                        sensorsViewHolder.toggleConnect.setTextOff("Disconnecting");
                        break;

                    case Error:
                        viewOnError();
                        clearText();
                        break;

                }
            }

            @Override
            public void onStreamChanged(Boolean isStream) {
                if (isStream) {
                    viewOnSensorStreamOn();
                } else {
                    viewOnSensorStreamOff();
                }
            }
        });

        currSensor.setSensorMessageListener(new Sensor.SensorMessageListener() {
            @Override
            public void onNewMessage(String message) {
                Snackbar.make(findViewById(R.id.actSensorHome), message, Snackbar.LENGTH_LONG).setActionTextColor(Color.YELLOW).show();
            }
        });

        currSensor.setOnSensorDeviceInfoChangeListener(new Sensor.OnSensorDeviceInfoChangeListener() {
            @Override
            public void onDeviceInfoAdd() {
                setDeviceInfoToTV();
            }

            @Override
            public void onDeviceInfoAddError() {
                Utils.showSimpleMessage(SensorHomeActivity.this, "Error while getting device Info. Please Try Again");
            }
        });
    }

    private void getParameters(SensorParamType sensorParamType) {

        if (currSensor.getBluetoothSPP() == null) {
            return;
        }
        if (currSensor.getBluetoothSPP().getServiceState() != BluetoothState.STATE_CONNECTED) {
            Snackbar.make(findViewById(R.id.actSensorHome), "Not Connected", Snackbar.LENGTH_LONG).setActionTextColor(Color.YELLOW).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getBaseContext(), "Clicked on OK", Toast.LENGTH_LONG).show();
                }
            }).show();
            return;
        }

        //REad
        byte[] data = ReadHeader.getHeader(sensorParamType, true);

        byte checksum = Utils.calcCheckSum(data, 4);
        data[4] = checksum;
        InstanceVals.setExpectedDataType(InstanceVals.possibleRecType.READ, data[1] + 1);
        if (Utils.checkIntegrity(data))
            currSensor.getBluetoothSPP().send(data, false);


    }

    private void disconnectSensor() {
        currSensor.getBluetoothSPP().disconnect();
        currSensor.getBluetoothSPP().stopService();
    }

    public void connectToSensor() {


        if (!currSensor.getBluetoothSPP().isServiceAvailable()) {
                currSensor.getBluetoothSPP().setupService();
                currSensor.getBluetoothSPP().startService(BluetoothState.DEVICE_OTHER);
            }

            if (currSensor.getBluetoothSPP().getServiceState() != BluetoothState.STATE_CONNECTED) {
                currSensor.getBluetoothSPP().connect(currSensor.getMacAddress());
            }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
        }
    }

    private void setDeviceInfoToTV() {

        tvAccfs.setText(currSensor.getSensorDeviceInfo().getAccFs());

        tvGyroFs.setText(currSensor.getSensorDeviceInfo().getGyroFS());

        tvPktType.setText(currSensor.getSensorDeviceInfo().getPktType());

        tvOrientAlgo.setText(currSensor.getSensorDeviceInfo().getOrientAlgo());

        tvSampleRate.setText(currSensor.getSensorDeviceInfo().getSampleRate());

    }

    private void setBluetoothListeners() {
//        if (currSensor.getBluetoothSPP() == null) {
//            currSensor.setBluetoothSPP(new BluetoothSPP(this));
//        }

        if (!currSensor.getBluetoothSPP().isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        }
        //TODO temporarily disabling it
//        currSensor.getBluetoothSPP().setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
//
//            public void onDataReceived(byte[] data, String message) {
//
//                Log.d(TAG, "onDataReceived: OnDataReceivedListener of SensorHomeActivity");
//                if (InstanceVals.getExpectedDataType() == InstanceVals.possibleRecType.DATASTREAM) {
//
//                    StreamData streamData = new StreamData(currSensor.getSensorDeviceInfo().getPktTypeByteValue());
//
//                    if (streamTextData.size() == 0) {
//                        //header is missing
//                        String headerText = "ProgrNum,PacketType,";
//                        headerText = headerText + "AccX,AccY,AccZ,";
//                        headerText = headerText + "GyrX,GyrY,GyrZ,";
//                        headerText = headerText + "MagX,MagY,MagZ,";
//                        headerText = headerText + "Q0,Q1,Q2,Q3,";
//                        headerText = headerText + "Vbat";
//
//                        tvMain.setText("");
//                        tvMain.setText(headerText + "\n");
//
//                        streamTextData.add(headerText);
//                    }
//
//                    try {
//                        streamData.setData(data);
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                    if (streamData.isValid()) {
//                        streamTextData.add(streamData.toString());
//
//                        tvMain.append(streamData.toString() + "\n");
//
//                        if (streamData.getPktCount() % 10 == 0) {
//                            double shortVal = streamData.getvBatt();
//
//                            while (shortVal > 10) {
//                                shortVal = (shortVal / 10.0);
//                            }
//                            tvSensorBatt.setText(Double.toString(shortVal));
//                        }
//
//                    } else {
////                        tvMain.append("CHECKSUM FAILED\n");
//                    }
//                }
//
//            }
//        });

        currSensor.getBluetoothSPP().setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                currSensor.setStatus(SensorStatus.Connected);

                Snackbar.make(findViewById(R.id.actSensorHome), "Connected", Snackbar.LENGTH_LONG).setActionTextColor(Color.YELLOW).setAction("OK", null).show();
            }

            public void onDeviceDisconnected() {
                currSensor.setStatus(SensorStatus.Disconnected);

                Snackbar.make(findViewById(R.id.actSensorHome), "Disconnected", Snackbar.LENGTH_LONG).setActionTextColor(Color.YELLOW).setAction("OK", null).show();
            }

            public void onDeviceConnectionFailed() {
                currSensor.setStatus(SensorStatus.Error);
                Snackbar.make(findViewById(R.id.actSensorHome), "Error. Please Try Again", Snackbar.LENGTH_LONG).setActionTextColor(Color.YELLOW).setAction("OK", null).show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
        InstanceVals.setExpectedDataType(InstanceVals.possibleRecType.DUMP, 0);
    }

    @Override
    public void onPause() {
        super.onPause();
        InstanceVals.setExpectedDataType(InstanceVals.possibleRecType.DUMP, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTextOnControls();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_sensor_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
//            Intent intent = new Intent(getApplicationContext(), SensorSettingsActivity.class);
//            Bundle bundle = new Bundle();
//            bundle.putString("currSensorInfo", new Gson().toJson(currSensor.getSensorDeviceInfo()));
//            intent.putExtras(bundle);
//            startActivity(intent);

            Intent intent = new Intent(getApplicationContext(), SensorSettingsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("sensorIdx", currSensorIdx);
            intent.putExtras(bundle);
            startActivity(intent);

        } else if (id == R.id.action_refresh) {
            if (currSensor.getStatus() == SensorStatus.Connected) {
                getSensorInfoDetails();
            } else {
                showSnackBar("Sensor is not Connected");
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void getSensorInfoDetails() {
        GetDeviceInfo getDeviceInfo = new GetDeviceInfo(currSensor);

        getDeviceInfo.setOnGotAllValuesListener(new GetDeviceInfo.OnGotAllValuesListener() {
            @Override
            public void OnGotAllValuesChange(Boolean val) {
                if (val) {
                    setupSensorListeners();
                    setBluetoothListeners();
                    setDeviceInfoToTV();
                }
                Log.d(TAG, "OnGotAllValuesChange: In Sensor Home Activity");
            }
        });

        getDeviceInfo.populateParamSettings();
    }

    private void showSnackBar(String message) {
        Snackbar.make(findViewById(R.id.actSensorHome), message, Snackbar.LENGTH_LONG).setActionTextColor(Color.YELLOW).show();
    }

    private void flushText(String filename) throws IOException {

        File path = new File(Environment.getExternalStorageDirectory(), "myFolder");
        path.mkdirs();

        File myPath = new File(path, filename);
        myPath.mkdirs();

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(myPath));

        for (String str : streamTextData) {
            bufferedWriter.write(str);
        }

        bufferedWriter.close();
    }


}
