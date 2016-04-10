package ru.mipt.pim.server.similarity;

import java.io.Serializable;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MinHash implements Serializable {

	private static final long serialVersionUID = -4048644413662320363L;

	protected static final long LARGE_PRIME =  433494437;
	
	private int hashFunctionsCount;
	private int hashesCount;
	private List<HashFunction> hashFunctions;

	// function f(x) = a*x + b
	private class HashFunction {
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

	public MinHash(int hashFunctionsCount, int hashesCount) {
		this.hashesCount = hashesCount;
		this.hashFunctionsCount = hashFunctionsCount;
		
        Random r = new Random();
        for (int i = 0; i < hashFunctionsCount; i++) {
        	hashFunctions.add(new HashFunction(r.nextLong(), r.nextLong()));
        }
	}
	
	/**
	 * @return array of size this.hashesCount
	 */
	public long[] generateHashes(Set<?> set) {
		long[] minHashes = new long[hashFunctionsCount];
		for (int i = 0; i < hashFunctionsCount; i++) {
			minHashes[i] = findMinHash(hashFunctions.get(i), set);
		}
		
		return splitToBands(minHashes);
	}

	private long findMinHash(HashFunction function, Set<?> set) {
		long minHash = 0;
		boolean first = true;
		for (Object element : set) {
			long hash = function.calculate(element.hashCode());
			if (first || minHash > hash) {
				minHash = hash;
			}
		}
		return minHash;
	}
	
    /**
	 * Hash a signature. The signature is divided in s stages (or bands).
	 */
	private long[] splitToBands(long[] minHashes) {
		// Create an accumulator for each stage
		long[] result = new long[hashesCount];

		// Number of rows per stage
		int rows = minHashes.length / hashesCount;

		for (int i = 0; i < minHashes.length; i++) {
			int stage = Math.min(i / rows, hashesCount - 1);
			result[stage] = result[stage] + minHashes[i] * LARGE_PRIME;
		}

		return result;
	}
	
}
