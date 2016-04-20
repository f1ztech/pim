package ru.mipt.pim.server.similarity;

import java.io.Serializable;

// function f(x) = a*x + b
public class HashFunction implements Serializable {
	private static final long serialVersionUID = 331020044872377821L;
	
	private long a;
	private long b;
	
	public HashFunction(long a, long b) {
		this.a = a;
		this.b = b;
	}
	
	public long calculate(int x) {
		return a * x + b;
	}
}