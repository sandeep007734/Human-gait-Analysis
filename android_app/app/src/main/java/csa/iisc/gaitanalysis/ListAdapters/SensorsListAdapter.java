package csa.iisc.gaitanalysis.ListAdapters;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

import csa.iisc.gaitanalysis.Activities.SensorHomeActivity;
import csa.iisc.gaitanalysis.BluetoothManager.BluetoothSPP;
import csa.iisc.gaitanalysis.DataModel.GlobalConstants;
import csa.iisc.gaitanalysis.DataModel.Sensor;
import csa.iisc.gaitanalysis.DataModel.SensorRepo;
import csa.iisc.gaitanalysis.R;

public class SensorsListAdapter extends RecyclerView.Adapter<SensorsListAdapter.SensorsViewHolder> {


    private static Activity activity;
    private static Boolean showOnlySelected;
    private static String TAG = "Gait Analysis SensorAdapter";
    private List<Sensor> sensorList;
    //  public static List<SelectTournamentViewHolder> tournamentViewHolders = new ArrayList<>();


    public SensorsListAdapter(Activity sensorConnectActivity, boolean showOnlySelected) {
        SensorsListAdapter.activity = sensorConnectActivity;
        SensorsListAdapter.showOnlySelected = showOnlySelected;

        sensorList = new ArrayList<>();
        sensorList = SensorRepo.getAllSensors(showOnlySelected);
    }

    public static Activity getActivity() {
        return activity;
    }

    public static void setActivity(Activity activity) {
        SensorsListAdapter.activity = activity;
    }

    public List<Sensor> getSensorList() {
        return sensorList;
    }

    @Override
    public int getItemCount() {
        return sensorList.size();
    }

    private void viewOnSensorConnected(SensorsViewHolder sensorsViewHolder) {
        sensorsViewHolder.toggleConnect.setBackgroundColor(Color.parseColor("#008000"));
        sensorsViewHolder.toggleConnect.setTextOn("Connected");
        sensorsViewHolder.toggleConnect.setChecked(true);

    }

    private void viewOnSensorDisconnected(SensorsViewHolder sensorsViewHolder) {
        sensorsViewHolder.toggleConnect.setBackgroundColor(Color.RED);
        sensorsViewHolder.toggleConnect.setTextOff("Disconnected");
        sensorsViewHolder.toggleConnect.setChecked(false);

    }

    private void viewOnSensorError(SensorsViewHolder sensorsViewHolder) {
        sensorsViewHolder.toggleConnect.setBackgroundColor(Color.BLACK);
        sensorsViewHolder.toggleConnect.setTextOff("ERROR");
        sensorsViewHolder.toggleConnect.setChecked(false);

    }

    private void viewOnSensorStreamOn(SensorsViewHolder sensorsViewHolder) {
        sensorsViewHolder.toggleStream.setChecked(true);
        sensorsViewHolder.toggleStream.setBackgroundDrawable(ContextCompat.getDrawable(getActivity().getBaseContext(), R.drawable.ripple_blue));
    }

    private void viewOnSensorStreamOff(SensorsViewHolder sensorsViewHolder) {
        sensorsViewHolder.toggleStream.setChecked(false);
        sensorsViewHolder.toggleStream.setBackgroundColor(Color.RED);
    }

    private void setupListeners(final Sensor sensor, final SensorsViewHolder sensorsViewHolder) {
        sensor.setSensorStatusChangeListener(new Sensor.OnSensorStatusChangeListener() {
            @Override
            public void onStatusChanged(GlobalConstants.SensorStatus sensorStatus) {
                switch (sensorStatus) {
                    case Connected:
                        viewOnSensorConnected(sensorsViewHolder);
//                        GetDeviceInfo getDeviceInfo = new GetDeviceInfo(sensor);
//                        getDeviceInfo.populateParamSettings();
                        break;

                    case Connecting:
                        sensorsViewHolder.toggleConnect.setTextOn("Connecting");
                        break;

                    case Disconnected:
                        viewOnSensorDisconnected(sensorsViewHolder);
                        break;

                    case Disconnecting:
//                        sensorsViewHolder.toggleConnect.setTextOff("Disconnecting");
                        break;

                    case Error:
                        viewOnSensorError(sensorsViewHolder);
                        break;

                }
            }

            @Override
            public void onStreamChanged(Boolean isStream) {
                if (isStream) {
                    viewOnSensorStreamOn(sensorsViewHolder);
                } else {
                    viewOnSensorStreamOff(sensorsViewHolder);
                }
            }
        });

        sensor.getBluetoothSPP().setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                sensor.setStatus(GlobalConstants.SensorStatus.Connected);
            }

            public void onDeviceDisconnected() {
                sensor.setStatus(GlobalConstants.SensorStatus.Disconnected);
            }

            public void onDeviceConnectionFailed() {
                sensor.setStatus(GlobalConstants.SensorStatus.Error);
            }
        });

        sensor.setSensorMessageListener(new Sensor.SensorMessageListener() {
            @Override
            public void onNewMessage(String message) {
                Toast.makeText(getActivity().getBaseContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        sensor.setOnSensorDeviceInfoChangeListener(new Sensor.OnSensorDeviceInfoChangeListener() {
            @Override
            public void onDeviceInfoAdd() {
                sensorsViewHolder.imgSensorInfoError.setVisibility(View.GONE);
                sensorsViewHolder.tvSensorParamInfo.setText(
                        sensor.getSensorDeviceInfo().getAccFs() + " " +
                                sensor.getSensorDeviceInfo().getGyroFS() + " " +
                                sensor.getSensorDeviceInfo().getSampleRate() + " " +
                                sensor.getSensorDeviceInfo().getOrientAlgo() + " " +
                                sensor.getSensorDeviceInfo().getPktType()
                );
            }

            @Override
            public void onDeviceInfoAddError() {
                sensorsViewHolder.imgSensorInfoError.setVisibility(View.VISIBLE);
            }
        });

    }


    @Override
    public void onBindViewHolder(final SensorsViewHolder sensorsViewHolder, final int i) {
        //This can be used to populate the card list
        final Sensor sensor = sensorList.get(i);

        sensorsViewHolder.tvPositions.setText(Integer.toString(sensor.getId()));
        sensorsViewHolder.tvSensorName.setText(sensor.getName());
        sensorsViewHolder.tvSensorPos.setText("Position: " + sensor.getPosition());
        sensorsViewHolder.tvMacAdd.setText(sensor.getMacAddress());

        sensorsViewHolder.tvSensorParamInfo.setText(sensor.getSensorDeviceInfo().getAccFs() + " " +
                sensor.getSensorDeviceInfo().getGyroFS() + " " +
                sensor.getSensorDeviceInfo().getSampleRate() + " " +
                sensor.getSensorDeviceInfo().getOrientAlgo() + " " +
                sensor.getSensorDeviceInfo().getPktType()
        );


        setupListeners(sensor, sensorsViewHolder);

        if (sensor.getDeviceInfoValid()) {
            sensorsViewHolder.imgSensorInfoError.setVisibility(View.GONE);
        }
        sensorsViewHolder.imgSensorInfoError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensor.refreshDeviceInfo();
            }
        });

        if (sensor.getStatus() == GlobalConstants.SensorStatus.Disconnected) {
            viewOnSensorDisconnected(sensorsViewHolder);
        } else if (sensor.getStatus() == GlobalConstants.SensorStatus.Connected) {
            viewOnSensorConnected(sensorsViewHolder);
        }

        if (sensor.isStreaming()) {
            viewOnSensorStreamOn(sensorsViewHolder);
        } else {
            viewOnSensorStreamOff(sensorsViewHolder);
        }

        sensorsViewHolder.toggleConnect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (sensor.getStatus() != GlobalConstants.SensorStatus.Connected) {
                        sensor.setStatus(GlobalConstants.SensorStatus.Connecting);
                        sensor.connectToSensor();
                    } else {
                        viewOnSensorConnected(sensorsViewHolder);
                    }
                } else {
                    if (sensor.getStatus() == GlobalConstants.SensorStatus.Connected) {
                        sensor.setStatus(GlobalConstants.SensorStatus.Disconnecting);
                        sensor.disconnectSensor();
                    }
                }
            }
        });

        sensorsViewHolder.toggleStream.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!sensor.isStreaming()) {


                        sensor.startStream();
                        //header is missing
                        String headerText = "ProgrNum,PacketType,";
                        headerText = headerText + "AccX,AccY,AccZ,";
                        headerText = headerText + "GyrX,GyrY,GyrZ,";
                        headerText = headerText + "MagX,MagY,MagZ,";
                        headerText = headerText + "Q0,Q1,Q2,Q3,";
                        headerText = headerText + "Vbat";

                    }
                } else {
                    if (sensor.isStreaming())
                        sensor.stopStream();

                }
            }
        });

        sensorsViewHolder.imgSensorDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SensorHomeActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("sensorIdx", i);

                intent.putExtras(bundle);
                getActivity().startActivity(intent);
            }
        });


        sensorsViewHolder.lyCardInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO temporarily blocking this.
//                Intent intent = new Intent(getActivity(), SensorHomeActivity.class);
//                Bundle bundle = new Bundle();
//                bundle.putInt("sensorIdx", i);
//
//                intent.putExtras(bundle);
//                getActivity().startActivity(intent);
            }
        });

        sensorsViewHolder.lyCardInfo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                chooseSensorPosition(sensor);
                return false;
            }
        });
        // setBluetoothListeners(sensor);
    }

    private void chooseSensorPosition(final Sensor currSensor) {

        final RadioButton rbThigh, rbShank, rbAnkle, rbLeftThigh, rbLeftShank, rbLeftAnkle;


        final MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title("Select Sensor Position")
                .customView(R.layout.dialog_sensorpos, true).build();

        rbThigh = (RadioButton) dialog.getCustomView().findViewById(R.id.rbThigh);
        rbShank = (RadioButton) dialog.getCustomView().findViewById(R.id.rbShank);
        rbAnkle = (RadioButton) dialog.getCustomView().findViewById(R.id.rbAnkle);

        rbLeftThigh = (RadioButton) dialog.getCustomView().findViewById(R.id.rbLeftThigh);
        rbLeftShank = (RadioButton) dialog.getCustomView().findViewById(R.id.rbLeftShank);
        rbLeftAnkle = (RadioButton) dialog.getCustomView().findViewById(R.id.rbLeftAnkle);


        switch (currSensor.getPosition()) {

            case RIGHTTHIGH:
                rbThigh.setChecked(true);
                break;
            case RIGHTSHANK:
                rbShank.setChecked(true);
                break;
            case RIGHTFOOT:
                rbAnkle.setChecked(true);
                break;
            case UNKNOWN:
                break;
            case LEFTTHIGH:
                rbLeftThigh.setChecked(true);
                break;
            case LEFTSHANK:
                rbLeftShank.setChecked(true);
                break;
            case LEFTFOOT:
                rbLeftAnkle.setChecked(true);
                break;
        }



        dialog.getCustomView().findViewById(R.id.btnSensorPosCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.getCustomView().findViewById(R.id.btnSensorPosOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                if (rbThigh.isChecked()) {
//                    currSensor.setPosition(GlobalConstants.SensorPosition.RIGHTTHIGH);
//                }
//                if (rbShank.isChecked()) {
//                    currSensor.setPosition(GlobalConstants.SensorPosition.RIGHTSHANK);
//                }
//                if (rbAnkle.isChecked()) {
//                    currSensor.setPosition(GlobalConstants.SensorPosition.RIGHTFOOT);
//                }


                dialog.dismiss();
            }
        });

        dialog.show();
        // positiveAction.setEnabled(false); // disabled by default
    }

    @Override
    public SensorsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_sensor, viewGroup, false);

        SensorsViewHolder sensorsViewHolder = new SensorsViewHolder(itemView);
        return sensorsViewHolder;
    }

    public static class SensorsViewHolder extends RecyclerView.ViewHolder {

        protected TextView tvPositions, tvSensorName, tvSensorPos, tvMacAdd, tvSensorParamInfo;
        protected ImageView btnCtxtMenu;
        protected LinearLayout lyCardInfo;
        protected ToggleButton toggleConnect, toggleStream;
        protected ImageView imgSensorInfoError, imgSensorDetails;

        public SensorsViewHolder(View v) {
            super(v);

            tvPositions = (TextView) v.findViewById(R.id.tvPosition);
            tvSensorName = (TextView) v.findViewById(R.id.sensorName);
            tvSensorPos = (TextView) v.findViewById(R.id.tvSensorPos);
            //tvStatus = (TextView) v.findViewById(R.id.tvStatus);
            tvMacAdd = (TextView) v.findViewById(R.id.tvmacAdd);
            tvSensorParamInfo = (TextView) v.findViewById(R.id.tvSensorParamInfo);

            btnCtxtMenu = (ImageView) v.findViewById(R.id.btnctxMenu);

            lyCardInfo = (LinearLayout) v.findViewById(R.id.lyCardInfo);

            toggleConnect = (ToggleButton) v.findViewById(R.id.toggleStatus);
            toggleStream = (ToggleButton) v.findViewById(R.id.toggleStream);

            imgSensorInfoError = (ImageView) v.findViewById(R.id.imgSensorInfoError);
            imgSensorDetails = (ImageView) v.findViewById(R.id.imgSensorDetails);


            //  v.setOnCreateContextMenuListener(this);
        }


    }
}