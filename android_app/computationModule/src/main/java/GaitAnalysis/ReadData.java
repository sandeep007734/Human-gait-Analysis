package GaitAnalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReadData {

	private String fileName;

	public ReadData(String filename) {
		this.fileName = filename;
	}

	public static SensorData readData(String filename) throws FileNotFoundException, IOException {
		List<String> calibData1 = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader(new File(filename)))) {
			String line;
			while ((line = br.readLine()) != null) {
				calibData1.add(line);
			}
		}
		int sizeOfData = calibData1.size();
		SensorData cDataThigh = new SensorData(sizeOfData);
		cDataThigh.loadData(calibData1, sizeOfData);

		sizeOfData = cDataThigh.sizeOfData;

		return cDataThigh;
	}
}
