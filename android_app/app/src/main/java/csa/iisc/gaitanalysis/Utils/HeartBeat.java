package csa.iisc.gaitanalysis.Utils;

import java.util.Timer;
import java.util.TimerTask;

import csa.iisc.gaitanalysis.BluetoothManager.BluetoothSPP;


public class HeartBeat {

    private static final String TAG = "HeartBeat";
    private BluetoothSPP bluetoothSPP;
    private Timer timer;
    private TimerTask timerTask = new TimerTask() {

        @Override
        public void run() {
            if (bluetoothSPP != null) {
                bluetoothSPP.send("!!", false);
            }
        }
    };

    public HeartBeat(BluetoothSPP bluetoothSPP) {
        this.bluetoothSPP = bluetoothSPP;
    }

    public void start() {
        if (timer != null) {
            return;
        }
        timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 0, 2000);
    }

    public void stop() {
        if (timer == null)
            return;
        timer.cancel();
        timer = null;
    }
}
