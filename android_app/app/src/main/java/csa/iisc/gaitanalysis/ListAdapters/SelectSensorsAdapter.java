package csa.iisc.gaitanalysis.ListAdapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import csa.iisc.gaitanalysis.DataModel.GlobalConstants;
import csa.iisc.gaitanalysis.DataModel.Sensor;
import csa.iisc.gaitanalysis.R;


public class SelectSensorsAdapter extends ArrayAdapter<Sensor> {

    private ArrayList<Sensor> sensorList;
    private Context context;

    public SelectSensorsAdapter(Context context, int textViewResourceId,
                                List<Sensor> sensorList) {
        super(context, textViewResourceId, sensorList);
        this.sensorList = new ArrayList<Sensor>();
        this.sensorList.addAll(sensorList);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        Log.v("ConvertView", String.valueOf(position));

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.card_sensor_choose, null);

            holder = new ViewHolder();
//            holder.placement = (TextView) convertView.findViewById(R.id.sensorPosition);
            holder.sensorName = (CheckBox) convertView.findViewById(R.id.chkSensorName);
            holder.spinPositions = (Spinner) convertView.findViewById(R.id.spinPositions);


            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, GlobalConstants.SensorPosition.getPositons());
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            holder.spinPositions.setAdapter(dataAdapter);



            convertView.setTag(holder);

            final Sensor sensor = sensorList.get(position);

            holder.spinPositions.setSelection(GlobalConstants.SensorPosition.getPositionIdx(sensor.getPosition().toString()));
            if (sensor.isSelected()) {
                holder.sensorName.setChecked(true);

            } else {
                holder.sensorName.setChecked(false);
            }

            holder.spinPositions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    sensor.setPosition(GlobalConstants.SensorPosition.getPositionFromIdx(position));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });




            holder.sensorName.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    Sensor sensor = (Sensor) cb.getTag();
//                    Toast.makeText(context.getApplicationContext(),
//                            "Clicked on Checkbox: " + cb.getText() +
//                                    " is " + cb.isChecked(),
//                            Toast.LENGTH_LONG).show();
                    sensor.setSelected(cb.isChecked());
                }
            });
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Sensor sensor = sensorList.get(position);
//        holder.placement.setText(" (" + sensor.getPosition() + ")");
        holder.sensorName.setText(sensor.getName());
        holder.sensorName.setChecked(sensor.isSelected());
        holder.sensorName.setTag(sensor);

        return convertView;

    }

    private class ViewHolder {
        //        TextView placement;
        CheckBox sensorName;
        Spinner spinPositions;
    }

}