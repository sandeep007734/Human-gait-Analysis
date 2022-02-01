package KneeFlexionExtension;


import Utils.ComputationUtils;

public class CalculateKneeFlexionExtensionAngle  {
	
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
		currAngle = prevAngle+ ComputationUtils.radToDeg((ComputationUtils.dotProduct(gyro_s_thigh, j1_valf)- ComputationUtils.dotProduct(gyro_s_shank, j2_valf))*0.01);
	
		return currAngle;
		
	}
	


	
//	 public CalculateKneeFlexionExtensionAngle( String applicationTitle , String chartTitle )
//	   {
//	      super(applicationTitle);
//
//	  	angleSet = new DefaultCategoryDataset( );
//
//	      JFreeChart lineChart = ChartFactory.createLineChart(
//	         chartTitle,
//	         "Years","Number of Schools",
//	         angleSet,
//	         PlotOrientation.VERTICAL,
//	         true,true,false);
//
//	      ChartPanel chartPanel = new ChartPanel( lineChart );
//	      chartPanel.setPreferredSize( new java.awt.Dimension( 560 , 367 ) );
//	      setContentPane( chartPanel );
//	   }
	 
	

}
