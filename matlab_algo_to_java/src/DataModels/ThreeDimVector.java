package DataModels;


public class ThreeDimVector {

	private Double x;
	private Double y;
	private Double z;

	public Double normalOfVec(ThreeDimVector g1) {

		return Math.sqrt(Math.pow(g1.getX(), 2) + Math.pow(g1.getY(), 2) + Math.pow(g1.getZ(), 2));
	}

	public ThreeDimVector crossProduct(ThreeDimVector g1, ThreeDimVector g2) {
		ThreeDimVector g3 = new ThreeDimVector();
		g3.setX(g1.getY() * g2.getZ() - g2.getY() * g1.getZ());
		g3.setY(g2.getX() * g1.getZ() - g1.getX() * g2.getZ());
		g3.setZ(g1.getX() * g2.getY() - g2.getX() * g1.getY());

		return g3;
	}

	@Override
	public String toString() {
		return "ThreeDimVector [x=" + x + ", y=" + y + ", z=" + z + "]";
	}

	public ThreeDimVector(Double x, Double y, Double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public ThreeDimVector() {

	}

	public Double getX() {
		return x;
	}

	public void setX(Double x) {
		this.x = x;
	}

	public Double getY() {
		return y;
	}

	public void setY(Double y) {
		this.y = y;
	}

	public Double getZ() {
		return z;
	}

	public void setZ(Double z) {
		this.z = z;
	}

}

