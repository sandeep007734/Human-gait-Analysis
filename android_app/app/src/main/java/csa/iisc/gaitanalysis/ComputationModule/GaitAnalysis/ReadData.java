package csa.iisc.gaitanalysis.ComputationModule.GaitAnalysis;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReadData {

    private static final String TAG = "ReadData";
    public List<String> readData(String filename) throws IOException {
        List<String> data = new ArrayList<>();
        Integer isShown = 0;

//        Log.d(TAG, "readData: Reading data: "+filename);

        try (BufferedReader br = new BufferedReader(new FileReader(new File(filename)))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (isShown > 0) {
                    System.out.println(line);
                    isShown--;
                }
                data.add(line);
            }
        }

        return data;
    }
}
