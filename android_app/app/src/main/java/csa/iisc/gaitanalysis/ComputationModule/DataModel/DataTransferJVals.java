package csa.iisc.gaitanalysis.ComputationModule.DataModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;

public class DataTransferJVals {
	
	private double[] j1_val;
	private double[] j2_val;
	private String absPath;

	public double[] getJ1_val() {
		return j1_val;
	}

	public double[] getJ2_val() {
		return j2_val;
	}

	public DataTransferJVals(double[] j1_val, double[] j2_val, String _absPath) {
		this.j1_val = new double[3];
		this.j2_val = new double[3];
		
		this.j1_val[0] = j1_val[0];
		this.j1_val[1] = j1_val[1];
		this.j1_val[2] = j1_val[2];
		
		this.j2_val[0] = j2_val[0];
		this.j2_val[1] = j2_val[1];
		this.j2_val[2] = j2_val[2];
		
		this.absPath = _absPath;
	}
	
	public String getAbsPath(){
		return this.absPath;
	}
	
	   public static boolean saveExperiment(DataTransferJVals dataTransferJVals) throws IOException {
	        if (dataTransferJVals == null) {
	            return false;
	        }

	        File expDir = new File(dataTransferJVals.getAbsPath());
	        if (!expDir.exists()) {
	            expDir.mkdir();
	        } else {
	        }

	        String absPath = dataTransferJVals.getAbsPath() + "/jVals.json";
	        String data = (new Gson()).toJson(dataTransferJVals);

	        File file = new File(absPath);
	        if (file.exists())
	            file.delete();


	        FileOutputStream fos = new FileOutputStream(absPath, true);
	        try (FileWriter fileWriter = new FileWriter(fos.getFD())) {
	            fileWriter.write(data);

	            fileWriter.close();

	            return true;
	        } catch (Exception e) {
	            e.printStackTrace();
	            return false;
	        } finally {
	         
	            fos.close();
	        }

	    }
}
