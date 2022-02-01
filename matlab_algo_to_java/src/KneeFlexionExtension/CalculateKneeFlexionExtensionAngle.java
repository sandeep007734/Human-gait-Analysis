package KneeFlexionExtension;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;

import Utils.Utils;

public class CalculateKneeFlexionExtensionAngle extends ApplicationFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public double calcAngle(double[] j1_valf, double[] j2_valf, double prevAngle, double[] gyro_s_thigh, double[] gyro_s_shank) {
		
		double currAngle = 0;
		
		if(j1_valf[0]<0){
			System.out.println("Makig Changes j1_valf");
			for(int i=0;i<3;i++){
				j1_valf[i] = j1_valf[i]*(-1);
			}
		}
		
		if(j2_valf[0]<0){
			System.out.println("Makig Changes j2_valf");
			for(int i=0;i<3;i++){
				j2_valf[i] = j2_valf[i]*(-1);
			}
		}
		currAngle = prevAngle+Utils.radToDeg((Utils.dotProduct(gyro_s_thigh, j1_valf)-Utils.dotProduct(gyro_s_shank, j2_valf))*0.01);
	
		return currAngle;
		
	}
	
	public DefaultCategoryDataset angleSet;
	
	 public CalculateKneeFlexionExtensionAngle( String applicationTitle , String chartTitle )
	   {
	      super(applicationTitle);
	      
	  	angleSet = new DefaultCategoryDataset( );
	  	
	      JFreeChart lineChart = ChartFactory.createLineChart(
	         chartTitle,
	         "Years","Number of Schools",
	         angleSet,
	         PlotOrientation.VERTICAL,
	         true,true,false);
	         
	      ChartPanel chartPanel = new ChartPanel( lineChart );
	      chartPanel.setPreferredSize( new java.awt.Dimension( 560 , 367 ) );
	      setContentPane( chartPanel );
	   }
	 
	

}
