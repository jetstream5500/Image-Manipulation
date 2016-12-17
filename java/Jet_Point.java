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

	public double distance(Jet_Point jp) {
		double dist = 0;
		for (int i = 0; i<this.dimensions() || i<jp.dimensions(); i++) {
			try {
				dist+=Math.pow(this.get(i)-jp.get(i),2);
			} catch (Exception e) {
				if (i>=this.dimensions()) {
					dist+=Math.pow(jp.get(i),2);
				} else {
					dist+=Math.pow(this.get(i),2);
				}
			}
		}
		return Math.sqrt(dist);
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
