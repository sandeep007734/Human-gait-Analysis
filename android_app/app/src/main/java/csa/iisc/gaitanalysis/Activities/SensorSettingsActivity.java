package csa.iisc.gaitanalysis.Activities;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import csa.iisc.gaitanalysis.BluetoothManager.BluetoothSPP;
import csa.iisc.gaitanalysis.BluetoothManager.BluetoothState;
import csa.iisc.gaitanalysis.DataModel.GlobalConstants;
import csa.iisc.gaitanalysis.DataModel.Sensor;
import csa.iisc.gaitanalysis.DataModel.SensorRepo;
import csa.iisc.gaitanalysis.R;
import csa.iisc.gaitanalysis.Utils.InstanceVals;
import csa.iisc.gaitanalysis.Utils.Utils;

public class SensorSettingsActivity extends AppCompatActivity {

    private static final String TAG = "Sensor Settings";
    //    SensorDeviceInfo currSensorInfo;
    Sensor currSensor;
    LinearLayout lyAccFsValue, lyGyroFsValue, lyPktTypeValue, lyOrienAlgoValue, lySampleRateValue, lyStreamValue, lySwrfdValue;
    TextView accFsValue, gyroFsValue, pktTypeValue, orienAlgoValue, sampleRateValue, streamValue, swrfdValue;
    private Toolbar toolbar;

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_settings);

        toolbar = (Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Get the currSensor

        Integer sensorIdx = getIntent().getExtras().getInt("sensorIdx");
        currSensor = SensorRepo.getSensorAt(sensorIdx);

        setupControls();
        // setBluetoothListeners();
        setParamText();

        currSensor.setOnChangeParametersListener(new Sensor.OnChangeParametersListener() {
            @Override
            public void OnChangeParameters(GlobalConstants.SensorParamType sensorParamType) {
                setParamText();
            }
        });


    }

    private void setParamText() {
        accFsValue.setText(currSensor.getSensorDeviceInfo().getAccFs());
        gyroFsValue.setText(currSensor.getSensorDeviceInfo().getGyroFS());
        pktTypeValue.setText(currSensor.getSensorDeviceInfo().getPktType());
        orienAlgoValue.setText(currSensor.getSensorDeviceInfo().getOrientAlgo());
        sampleRateValue.setText(currSensor.getSensorDeviceInfo().getSampleRate());
        streamValue.setText(currSensor.getSensorDeviceInfo().getStreamLog());
        swrfdValue.setText(currSensor.getSensorDeviceInfo().getSwrfd());
    }

    private void setupControls() {

        lyAccFsValue = (LinearLayout) findViewById(R.id.lyaccFsValue);
        lyGyroFsValue = (LinearLayout) findViewById(R.id.lyGyroFsValue);
        lyPktTypeValue = (LinearLayout) findViewById(R.id.lyPktTypeValue);
        lyOrienAlgoValue = (LinearLayout) findViewById(R.id.lyOrienAlgoValue);
        lySampleRateValue = (LinearLayout) findViewById(R.id.lySampleRateValue);
        lyStreamValue = (LinearLayout) findViewById(R.id.lyStreamValue);
        lySwrfdValue = (LinearLayout) findViewById(R.id.lySwrfdValue);


        accFsValue = (TextView) findViewById(R.id.accFsValue);
        gyroFsValue = (TextView) findViewById(R.id.gyroFsValue);
        pktTypeValue = (TextView) findViewById(R.id.pktTypeValue);
        orienAlgoValue = (TextView) findViewById(R.id.orienAlgoValue);
        sampleRateValue = (TextView) findViewById(R.id.sampleRateValue);
        streamValue = (TextView) findViewById(R.id.streamValue);
        swrfdValue = (TextView) findViewById(R.id.swrfdValue);

        lyAccFsValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAccFsChooser();
            }
        });
        lyGyroFsValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGyroFsChooser();
            }
        });
        lyPktTypeValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPktTypeChooser();
            }
        });
        lyOrienAlgoValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOrientChooser();
            }
        });
        lySampleRateValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSampleChooser();
            }
        });
        lyStreamValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStreamChooser();
            }
        });
        lySwrfdValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSwrfdChooser();
            }
        });
    }

    private void setBluetoothListeners() {
//        if (currSensor.getBluetoothSPP() == null) {
//            currSensor.setBluetoothSPP(new BluetoothSPP(this));
//        }

        if (!currSensor.getBluetoothSPP().isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        }
        currSensor.getBluetoothSPP().setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                Log.d(TAG, "onDataReceived: OnDataReceivedListener of Sensor Setting");
                if (InstanceVals.getExpectedDataType() == InstanceVals.possibleRecType.WRITE) {
                }
            }
        });


    }

    private void showAccFsChooser() {

        RadioButton rbTwog, rbFourg, rbEightg, rbSixteeng;


        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Select Accelerometer Full Scale Range")
                .customView(R.layout.dialog_accfs, true).build();

        rbTwog = (RadioButton) dialog.getCustomView().findViewById(R.id.rbTwog);
        rbFourg = (RadioButton) dialog.getCustomView().findViewById(R.id.rbFourg);
        rbEightg = (RadioButton) dialog.getCustomView().findViewById(R.id.rbEightg);
        rbSixteeng = (RadioButton) dialog.getCustomView().findViewById(R.id.rbSixteeng);

        rbTwog.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Byte val = 0;
                    currSensor.getSensorDeviceInfo().setAccFSByteValue(val);
                }
            }
        });
        rbFourg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Byte val = 1;
                    currSensor.getSensorDeviceInfo().setAccFSByteValue(val);
                }
            }
        });
        rbEightg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Byte val = 2;
                    currSensor.getSensorDeviceInfo().setAccFSByteValue(val);
                }
            }
        });
        rbSixteeng.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Byte val = 3;
                    currSensor.getSensorDeviceInfo().setAccFSByteValue(val);
                }
            }
        });

        switch (currSensor.getSensorDeviceInfo().getAccFSByteValue()) {
            case 0:
                rbTwog.setChecked(true);
                break;
            case 1:
                rbFourg.setChecked(true);
                break;
            case 2:
                rbEightg.setChecked(true);
                break;
            case 3:
                rbSixteeng.setChecked(true);
                break;
        }


        dialog.getCustomView().findViewById(R.id.btnAccFsCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.getCustomView().findViewById(R.id.btnAccFsOK).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                currSensor.getSensorDeviceInfo().setAccFs(GetDeviceVals.getDetails(GlobalConstants.SensorParamType.accFS, currSensor.getSensorDeviceInfo().getAccFSByteValue()));
                currSensor.writeParamValueToSensor(GlobalConstants.SensorParamType.accFS);
                currSensor.saveValue();
                dialog.dismiss();
            }
        });

        dialog.show();
        // positiveAction.setEnabled(false); // disabled by default
    }

    private void showGyroFsChooser() {

        RadioButton rbTwoFifty, rbFiveHundred, rbThousand, rbTwoThousand;


        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Select Gyroscope Full Scale Range")
                .customView(R.layout.dialog_gyrofs, true).build();

        rbTwoFifty = (RadioButton) dialog.getCustomView().findViewById(R.id.rbTwoFifty);
        rbFiveHundred = (RadioButton) dialog.getCustomView().findViewById(R.id.rbFiveHundred);
        rbThousand = (RadioButton) dialog.getCustomView().findViewById(R.id.rbThousand);
        rbTwoThousand = (RadioButton) dialog.getCustomView().findViewById(R.id.rbTwoThousand);

        rbTwoFifty.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Byte val = 0;
                    currSensor.getSensorDeviceInfo().setGyroFSByteValue(val);
                }
            }
        });
        rbFiveHundred.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Byte val = 1;
                    currSensor.getSensorDeviceInfo().setGyroFSByteValue(val);
                }
            }
        });
        rbThousand.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Byte val = 2;
                    currSensor.getSensorDeviceInfo().setGyroFSByteValue(val);
                }
            }
        });
        rbTwoThousand.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Byte val = 3;
                    currSensor.getSensorDeviceInfo().setGyroFSByteValue(val);
                }
            }
        });

        switch (currSensor.getSensorDeviceInfo().getGyroFSByteValue()) {
            case 0:
                rbTwoFifty.setChecked(true);
                break;
            case 1:
                rbFiveHundred.setChecked(true);
                break;
            case 2:
                rbThousand.setChecked(true);
                break;
            case 3:
                rbTwoThousand.setChecked(true);
                break;
        }


        dialog.getCustomView().findViewById(R.id.btnGyroFsCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.getCustomView().findViewById(R.id.btnGyroFsOK).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  String byteStringValue = String.format("%02X ", currSensor.getSensorDeviceInfo().getGyroFSByteValue()).trim();
//                currSensor.getSensorDeviceInfo().setGyroFS(GetDeviceVals.getDetails(GlobalConstants.SensorParamType.gyroFS, currSensor.getSensorDeviceInfo().getGyroFSByteValue()));
                currSensor.writeParamValueToSensor(GlobalConstants.SensorParamType.gyroFS);
                currSensor.saveValue();
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private void showPktTypeChooser() {

        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Select Packet Type (Data to be received)")
                .customView(R.layout.dialog_pkttype, true).build();

        final CheckBox chkAcc, chkGyro, chkMag, chkOrie, chkBatt, chkRaw;

        chkAcc = (CheckBox) dialog.getCustomView().findViewById(R.id.chkAcc);
        chkGyro = (CheckBox) dialog.getCustomView().findViewById(R.id.chkGyro);
        chkMag = (CheckBox) dialog.getCustomView().findViewById(R.id.chkMag);
        chkOrie = (CheckBox) dialog.getCustomView().findViewById(R.id.chkOrie);
        chkBatt = (CheckBox) dialog.getCustomView().findViewById(R.id.chkBatt);
        chkRaw = (CheckBox) dialog.getCustomView().findViewById(R.id.chkRaw);

        byte data = currSensor.getSensorDeviceInfo().getPktTypeByteValue();

        chkRaw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    chkAcc.setChecked(false);
                    chkGyro.setChecked(false);
                    chkMag.setChecked(false);
                    chkOrie.setChecked(false);
                    chkBatt.setChecked(false);
                }
            }
        });

        chkAcc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    chkRaw.setChecked(false);
            }
        });
        chkGyro.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    chkRaw.setChecked(false);
            }
        });
        chkMag.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    chkRaw.setChecked(false);
            }
        });
        chkOrie.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    chkRaw.setChecked(false);
            }
        });
        chkBatt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    chkRaw.setChecked(false);
            }
        });

        if (data == 0) {
            chkRaw.setChecked(true);
        } else {
            chkRaw.setChecked(false);
            if (Utils.getBit(data, 1) == 1) {
                chkAcc.setChecked(true);
            }
            if (Utils.getBit(data, 2) == 1) {
                chkGyro.setChecked(true);
            }
            if (Utils.getBit(data, 3) == 1) {
                chkMag.setChecked(true);
            }
            if (Utils.getBit(data, 4) == 1) {
                chkOrie.setChecked(true);
            }
            if (Utils.getBit(data, 5) == 1) {
                chkBatt.setChecked(true);
            }
        }


        dialog.getCustomView().findViewById(R.id.btnPktTypeCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.getCustomView().findViewById(R.id.btnPktTypeOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String binString;
                if (chkRaw.isChecked()) {
                    binString = "0000000";
                } else {
                    if (chkAcc.isChecked()) {
                        binString = "1";
                    } else {
                        binString = "0";
                    }
                    if (chkGyro.isChecked()) {
                        binString = "1" + binString;
                    } else {
                        binString = "0" + binString;
                    }
                    if (chkMag.isChecked()) {
                        binString = "1" + binString;
                    } else {
                        binString = "0" + binString;
                    }
                    if (chkOrie.isChecked()) {
                        binString = "1" + binString;
                    } else {
                        binString = "0" + binString;
                    }
                    if (chkBatt.isChecked()) {
                        binString = "1" + binString;
                    } else {
                        binString = "0" + binString;
                    }
                    binString = "100" + binString;
                }

                binString = binString.trim();
                int val = Integer.parseInt(binString, 2);
                byte byteVal = (byte) val;


                currSensor.getSensorDeviceInfo().setPktTypeByteValue(byteVal);
//                currSensor.getSensorDeviceInfo().setPktType(GetDeviceVals.getDetails(GlobalConstants.SensorParamType.pktType, currSensor.getSensorDeviceInfo().getPktTypeByteValue()));
                currSensor.writeParamValueToSensor(GlobalConstants.SensorParamType.pktType);
                currSensor.saveValue();
                dialog.dismiss();


            }
        });

        dialog.show();

    }

    private void showOrientChooser() {

        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Select Orientation Algorithm")
                .customView(R.layout.dialog_orientation, true).build();

        RadioButton rbECompass, rbGyro, rbKalman;

        rbECompass = (RadioButton) dialog.getCustomView().findViewById(R.id.rbECompass);
        rbGyro = (RadioButton) dialog.getCustomView().findViewById(R.id.rbGyro);
        rbKalman = (RadioButton) dialog.getCustomView().findViewById(R.id.rbKalman);

        rbECompass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Byte val = 0;
                    currSensor.getSensorDeviceInfo().setOrientAlgoByteValue(val);
                }
            }
        });
        rbGyro.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Byte val = 1;
                    currSensor.getSensorDeviceInfo().setOrientAlgoByteValue(val);
                }
            }
        });
        rbKalman.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Byte val = 2;
                    currSensor.getSensorDeviceInfo().setOrientAlgoByteValue(val);
                }
            }
        });

        switch (currSensor.getSensorDeviceInfo().getOrientAlgoByteValue()) {
            case 0:
                rbECompass.setChecked(true);
                break;
            case 1:
                rbGyro.setChecked(true);
                break;
            case 2:
                rbKalman.setChecked(true);
                break;
        }


        dialog.getCustomView().findViewById(R.id.btnOrieCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.getCustomView().findViewById(R.id.btnOrieOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String byteStringValue = String.format("%02X ", currSensor.getSensorDeviceInfo().getOrientAlgoByteValue()).trim();
//                currSensor.getSensorDeviceInfo().setOrientAlgo(GetDeviceVals.getDetails(GlobalConstants.SensorParamType.orientAlgo, currSensor.getSensorDeviceInfo().getOrientAlgoByteValue()));
                currSensor.writeParamValueToSensor(GlobalConstants.SensorParamType.orientAlgo);
                currSensor.saveValue();
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private void showSampleChooser() {

        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Select Sample Rate")
                .customView(R.layout.dialog_samplerate, true).build();

        RadioButton rbTwo, rbHundred, rbFifty, rbThirtyThree, rbTwentyFive,
                rbTwenty, rbSixteen, rbTwelve, rbTen, rbFive, rbThreeHundred;

        rbTwo = (RadioButton) dialog.getCustomView().findViewById(R.id.rbTwo);
        rbHundred = (RadioButton) dialog.getCustomView().findViewById(R.id.rbHundred);
        rbFifty = (RadioButton) dialog.getCustomView().findViewById(R.id.rbFifty);
        rbThirtyThree = (RadioButton) dialog.getCustomView().findViewById(R.id.rbThirtyTHree);
        rbTwentyFive = (RadioButton) dialog.getCustomView().findViewById(R.id.rbTwentyFive);
        rbTwenty = (RadioButton) dialog.getCustomView().findViewById(R.id.rbTwenty);
        rbSixteen = (RadioButton) dialog.getCustomView().findViewById(R.id.rbSixteen);
        rbTwelve = (RadioButton) dialog.getCustomView().findViewById(R.id.rbTwelve);
        rbTen = (RadioButton) dialog.getCustomView().findViewById(R.id.rbTen);
        rbFive = (RadioButton) dialog.getCustomView().findViewById(R.id.rbFive);
        rbThreeHundred = (RadioButton) dialog.getCustomView().findViewById(R.id.rbThreeHundred);

        rbTwo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Byte val = 0;
                    currSensor.getSensorDeviceInfo().setSampleRateByteValue(val);
                }
            }
        });
        rbHundred.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Byte val = 1;
                    currSensor.getSensorDeviceInfo().setSampleRateByteValue(val);
                }
            }
        });
        rbFifty.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Byte val = 2;
                    currSensor.getSensorDeviceInfo().setSampleRateByteValue(val);
                }
            }
        });

        rbThirtyThree.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Byte val = 3;
                    currSensor.getSensorDeviceInfo().setSampleRateByteValue(val);
                }
            }
        });
        rbTwentyFive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Byte val = 4;
                    currSensor.getSensorDeviceInfo().setSampleRateByteValue(val);
                }
            }
        });
        rbTwenty.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Byte val = 5;
                    currSensor.getSensorDeviceInfo().setSampleRateByteValue(val);
                }
            }
        });
        rbSixteen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Byte val = 6;
                    currSensor.getSensorDeviceInfo().setSampleRateByteValue(val);
                }
            }
        });
        rbTwelve.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Byte val = 7;
                    currSensor.getSensorDeviceInfo().setSampleRateByteValue(val);
                }
            }
        });
        rbTen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Byte val = 8;
                    currSensor.getSensorDeviceInfo().setSampleRateByteValue(val);
                }
            }
        });
        rbFive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Byte val = 9;
                    currSensor.getSensorDeviceInfo().setSampleRateByteValue(val);
                }
            }
        });
        rbThreeHundred.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Byte val = 10;
                    currSensor.getSensorDeviceInfo().setSampleRateByteValue(val);
                }
            }
        });

        switch (currSensor.getSensorDeviceInfo().getSampleRateByteValue()) {
            case 0:
                rbTwo.setChecked(true);
                break;
            case 1:
                rbHundred.setChecked(true);
                break;
            case 2:
                rbFifty.setChecked(true);
                break;
            case 3:
                rbThirtyThree.setChecked(true);
                break;
            case 4:
                rbTwentyFive.setChecked(true);
                break;
            case 5:
                rbTwenty.setChecked(true);
                break;
            case 6:
                rbSixteen.setChecked(true);
                break;
            case 7:
                rbTwelve.setChecked(true);
                break;
            case 8:
                rbTen.setChecked(true);
                break;
            case 9:
                rbFive.setChecked(true);
                break;
            case 10:
                rbThreeHundred.setChecked(true);
                break;
        }


        dialog.getCustomView().findViewById(R.id.btnSampleCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.getCustomView().findViewById(R.id.btnSampleOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String byteStringValue = String.format("%02X ", currSensor.getSensorDeviceInfo().getSampleRateByteValue()).trim();
//                currSensor.getSensorDeviceInfo().setSampleRate(GetDeviceVals.getDetails(GlobalConstants.SensorParamType.sampleRate, currSensor.getSensorDeviceInfo().getSampleRateByteValue()));
                currSensor.writeParamValueToSensor(GlobalConstants.SensorParamType.sampleRate);
                currSensor.saveValue();
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private void showStreamChooser() {

        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Select where to Stream Data")
                .customView(R.layout.dialog_streamlog, true).build();

        RadioButton rbBluetooth, rbLogToDisk, rbStreamAndLog;

        rbBluetooth = (RadioButton) dialog.getCustomView().findViewById(R.id.rbBluetooth);
        rbLogToDisk = (RadioButton) dialog.getCustomView().findViewById(R.id.rbLogToDisk);
        rbStreamAndLog = (RadioButton) dialog.getCustomView().findViewById(R.id.rbStreamAndLog);

        rbBluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Byte val = 0;
                    currSensor.getSensorDeviceInfo().setStreamLogByteValue(val);
                }
            }
        });

        rbLogToDisk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Byte val = 1;
                    currSensor.getSensorDeviceInfo().setStreamLogByteValue(val);
                }
            }
        });

        rbStreamAndLog.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Byte val = 2;
                    currSensor.getSensorDeviceInfo().setStreamLogByteValue(val);
                }
            }
        });

        switch (currSensor.getSensorDeviceInfo().getStreamLogByteValue()) {
            case 0:
                rbBluetooth.setChecked(true);
                break;
            case 1:
                rbLogToDisk.setChecked(true);
                break;
            case 2:
                rbStreamAndLog.setChecked(true);
                break;
        }


        dialog.getCustomView().findViewById(R.id.btnStreamCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.getCustomView().findViewById(R.id.btnStreamOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String byteStringValue = String.format("%02X ", currSensor.getSensorDeviceInfo().getStreamLogByteValue()).trim();
                currSensor.writeParamValueToSensor(GlobalConstants.SensorParamType.streamLog);
                currSensor.saveValue();
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private void showSwrfdChooser() {

        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Select Dock remove behaviour")
                .customView(R.layout.dialog_swrfd, true).build();

        RadioButton rbStartCommand, rbStartNoPower, rbContinuousStreaming;

        rbStartCommand = (RadioButton) dialog.getCustomView().findViewById(R.id.rbStartCommand);
        rbStartNoPower = (RadioButton) dialog.getCustomView().findViewById(R.id.rbStartNoPower);
        rbContinuousStreaming = (RadioButton) dialog.getCustomView().findViewById(R.id.rbContinuousStreaming);

        switch (currSensor.getSensorDeviceInfo().getSwrfdByteValue()) {
            case 0:
                rbStartCommand.setChecked(true);
                break;
            case 1:
                rbStartNoPower.setChecked(true);
                break;
            case 2:
                rbContinuousStreaming.setChecked(true);
                break;
        }

        rbStartCommand.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Byte val = 0;
                    currSensor.getSensorDeviceInfo().setSwrfdByteValue(val);
                }
            }
        });

        rbStartNoPower.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Byte val = 1;
                    currSensor.getSensorDeviceInfo().setSwrfdByteValue(val);
                }
            }
        });

        rbContinuousStreaming.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Byte val = 2;
                    currSensor.getSensorDeviceInfo().setSwrfdByteValue(val);
                }
            }
        });


        dialog.getCustomView().findViewById(R.id.btnSwrfdCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.getCustomView().findViewById(R.id.btnSwrfdOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String byteStringValue = String.format("%02X ", currSensor.getSensorDeviceInfo().getSwrfdByteValue()).trim();
//                currSensor.getSensorDeviceInfo().setSwrfd(GetDeviceVals.getDetails(GlobalConstants.SensorParamType.swrfd, currSensor.getSensorDeviceInfo().getSwrfdByteValue()));
                currSensor.writeParamValueToSensor(GlobalConstants.SensorParamType.swrfd);
                currSensor.saveValue();

                dialog.dismiss();
            }
        });

        dialog.show();

    }


}
