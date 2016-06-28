package ru.mipt.pim.server.similarity;

import org.apache.commons.math3.linear.RealVector;

public class SimHash {

	protected static final long LARGE_PRIME =  433494437;

	private SimHashSignatureGenerator signatureGenerator;

	private int hashesCount;
	
	public SimHash(SimHashSignatureGenerator signatureGenerator, int hashesCount) {
		this.signatureGenerator = signatureGenerator;
		this.hashesCount = hashesCount;
	}
	
	/**
	 * @return array of size this.hashesCount
	 */
	public long[] generateHashes(RealVector tfIdf) {
		return hashSignature(signatureGenerator.createSignature(tfIdf));
	}
	
    /**
     * Hash a signature.
     * The signature is divided in s stages (or bands).
     */
    public long[] hashSignature(boolean[] signature) {
        long[] stageHashes = new long[hashesCount];
        
        // Number of rows per stage
		int stageLength = signature.length / hashesCount;

		for (int i = 0; i < signature.length; i++) {
			long hashForBit = signature[i] ? (i + 1) * LARGE_PRIME : 0;

			// current stage
			int j = Math.min(i / stageLength, hashesCount - 1);
			stageHashes[j] = stageHashes[j] + hashForBit;
		}

        return stageHashes;
    }
}
