package DataModels;

public class FourDimVector {

	private Double a;
	private Double b;
	private Double c;
	private Double d;
	
	public FourDimVector(){
		
	}
	
	public FourDimVector(Double a, Double b, Double c, Double d) {
		super();
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}
	public Double getA() {
		return a;
	}
	public void setA(Double a) {
		this.a = a;
	}
	public Double getB() {
		return b;
	}
	public void setB(Double b) {
		this.b = b;
	}
	public Double getC() {
		return c;
	}
	public void setC(Double c) {
		this.c = c;
	}
	public Double getD() {
		return d;
	}
	public void setD(Double d) {
		this.d = d;
	}
	
	
}
