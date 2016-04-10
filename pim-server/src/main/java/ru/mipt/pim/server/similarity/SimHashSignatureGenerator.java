package ru.mipt.pim.server.similarity;

import org.apache.commons.math3.linear.RealVector;

public interface SimHashSignatureGenerator {

	public boolean[] createSignature(RealVector vector);
	
}
