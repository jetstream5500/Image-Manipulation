public class Jet_Point {
	private double[] vals;

	public Jet_Point(double... vals) {
		this.vals = vals;
	}

	public double get(int i) {
		return vals[i];
	}

	public void set(int i, double val) {
		vals[i]=val;
	}

	public int dimensions() {
		return vals.length;
	}

	public String toString() {
		String str = "(";
		for (double i : vals) {
			str+=i+", ";
		}
		str = str.substring(0,str.length()-2);
		return str+=")";
	}
}
