package csa.iisc.gaitanalysis.DataModel;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * Created by root on 28/2/16.
 */
public class SensorRepo {

    private static final transient String TAG = "SensprRepo";

    public static List<Sensor> allSensors;
    private static CyclicBarrier gate;

    private static SensorConnectingListener sensorConnectingListener;

    public static void setSensorConnectingListener(SensorConnectingListener sensorConnectingListener) {
        SensorRepo.sensorConnectingListener = sensorConnectingListener;
    }

    public static void upTheSensorConnectingCount(){
        isAnySensorConnecting++;
        if(sensorConnectingListener!=null)
            sensorConnectingListener.onSensorConnectingCountChange(isAnySensorConnecting);
    }

    public static void downTheSensorConnectingCount(){
        isAnySensorConnecting--;
        if(sensorConnectingListener!=null)
            sensorConnectingListener.onSensorConnectingCountChange(isAnySensorConnecting);
    }


    private static int isAnySensorConnecting = 0;

    public interface SensorConnectingListener{
        void onSensorConnectingCountChange(int newCount);
    }


    public static List<Sensor> getAllSensors(Boolean onlySelected) {
        if (onlySelected) {
            List<Sensor> selectedSensor = new ArrayList<>();
            for (Sensor sensor : allSensors) {
                if (sensor.isSelected())
                    selectedSensor.add(sensor);
            }
            return selectedSensor;
        } else {
            return allSensors;
        }
    }

    public static List<String> getAllSensorsName(Boolean onlySelected) {
        List<String> selectedSensor = new ArrayList<>();
        if (onlySelected) {

            for (Sensor sensor : allSensors) {
                if (sensor.isSelected())
                    selectedSensor.add(sensor.getName());
            }
            return selectedSensor;
        } else {
            for (Sensor sensor : allSensors) {
                selectedSensor.add(sensor.getName());
            }
            return selectedSensor;
        }
    }

    public static List<GlobalConstants.SensorPosition> getAllSensorsPosition(Boolean onlySelected) {
        List<GlobalConstants.SensorPosition> selectedSensorPos = new ArrayList<>();
        if (onlySelected) {

            for (Sensor sensor : allSensors) {
                if (sensor.isSelected())
                    selectedSensorPos.add(sensor.getPosition());
            }
            return selectedSensorPos;
        } else {
            for (Sensor sensor : allSensors) {
                selectedSensorPos.add(sensor.getPosition());
            }
            return selectedSensorPos;
        }
    }

    public static List<String> getAllSensorsPositionStr(Boolean onlySelected) {
        List<String> selectedSensorPos = new ArrayList<>();
        if (onlySelected) {

            for (Sensor sensor : allSensors) {
                if (sensor.isSelected())
                    selectedSensorPos.add(sensor.getPosition().toString());
            }
            return selectedSensorPos;
        } else {
            for (Sensor sensor : allSensors) {
                selectedSensorPos.add(sensor.getPosition().toString());
            }
            return selectedSensorPos;
        }
    }

    public static void setAllSensors(List<Sensor> allSensors) {
        if (SensorRepo.allSensors == null) {
            SensorRepo.allSensors = new ArrayList<>();
        } else {
            SensorRepo.allSensors.clear();
        }

        for (Sensor sensor : allSensors) {
            SensorRepo.allSensors.add(sensor);
        }

        Collections.sort(
                SensorRepo.allSensors,
                new Comparator<Sensor>() {
                    public int compare(Sensor lhs, Sensor rhs) {
                        return lhs.getName().compareTo(rhs.getName());
                    }
                }
        );
    }

    public static Sensor getSensorAt(int idx) {
        if (allSensors != null) {
            try {
                Sensor sensor = allSensors.get(idx);
                return sensor;
            } catch (Exception e) {
                return null;
            }

        }
        return null;
    }

    public void startStreamAllSensors(Boolean onlySelected) {
        List<Sensor> selectedSensors = getAllSensors(onlySelected);

        if (selectedSensors.size() == 0) {
            Log.d(TAG, "startStreamAllSensors: No Sensor to Start");
            return;
        }
        Log.d(TAG, "startStreamAllSensors: Barrier Size: " + (selectedSensors.size() + 1));
        gate = new CyclicBarrier(selectedSensors.size() + 1);

        for (Sensor sensor : selectedSensors) {
            sensor.setCurrLoggingState(GlobalConstants.LoggingStates.LOGGING);
            Thread startThread = new StartThread(sensor);
            startThread.start();
        }

        try {
            gate.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    private class StartThread extends Thread implements Runnable {

        private Sensor sensor;

        public StartThread(Sensor sensor) {
            this.sensor = sensor;
        }

        @Override
        public void run() {
            try {
                Log.d(TAG, "run: Waiting at gate. " + sensor.getName());
                SensorRepo.gate.await();
                Log.d(TAG, "run: Proceeding. " + sensor.getName());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
            sensor.startStream();
        }
    }


}
