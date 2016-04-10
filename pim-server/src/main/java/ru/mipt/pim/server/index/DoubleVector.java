package ru.mipt.pim.server.index;

public class DoubleVector {

	private double[] vector;
	
	public DoubleVector(int dimension) {
		vector = new double[dimension]; // all elements are zeros
	}
	
	public double set(int index, double value) {
		double old = vector[index];
		vector[index] = value;
		return old;
	}

	public double dotProduct(DoubleVector vector2) {
        double agg = 0;
        
        for (int i = 0; i < getLength(); i++) {
            agg += (vector2.get(i) * get(i));
        }
        
        return agg;
	}
	
	private double get(int i) {
		return vector[i];
	}

	public int getLength() {
		return vector.length;
	}
	
}
