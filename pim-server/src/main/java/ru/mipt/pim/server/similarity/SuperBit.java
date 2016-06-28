/*
 * The MIT License
 *
 * Copyright 2015 Thibault Debatty.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package ru.mipt.pim.server.similarity;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

/**
 * Implementation of Super-Bit Locality-Sensitive Hashing. Super-Bit is an
 * improvement of Random Projection LSH. It computes an estimation of cosine
 * similarity.
 * 
 * Super-Bit Locality-Sensitive Hashing Jianqiu Ji, Jianmin Li, Shuicheng Yan,
 * Bo Zhang, Qi Tian
 * http://papers.nips.cc/paper/4847-super-bit-locality-sensitive-hashing.pdf
 * Advances in Neural Information Processing Systems 25, 2012
 * 
 * Supported input types: - SparseIntegerVector - double[] - others to come...
 * 
 * @author Thibault Debatty
 */
public class SuperBit implements Serializable, SimHashSignatureGenerator {

	private static final long serialVersionUID = 8420482803730104714L;
	
	private RealVector[] hyperplanes;
	private int dimension;

	/**
	 * Initialize SuperBit algorithm. Super-Bit depth N must be [1 .. dimension] and
	 * number of Super-Bit L in [1 .. The resulting code length K = N * L The K
	 * vectors are orthogonalized in L batches of N vectors
	 * 
	 * @param dimension
	 *            data space dimension
	 * @param depth
	 *            Super-Bit depth [1 .. dimension]
	 * @param superBitsAmount
	 *            number of Super-Bit [1 ..
	 */
	public SuperBit(int dimension, int depth, int superBitsAmount) {
		this.dimension = dimension;
		if (dimension <= 0) {
			throw new IllegalArgumentException("Dimension dimension must be >= 1");
		}

		if (depth < 1 || depth > dimension) {
			throw new IllegalArgumentException("Super-Bit depth N must be 1 <= N <= dimension");
		}

		if (superBitsAmount < 1) {
			throw new IllegalArgumentException("Number of Super-Bit L must be >= 1");
		}

		// Input: Data space dimension dimension, Super-Bit depth 1 <= N <= dimension, number of
		// Super-Bit L >= 1,
		// resulting code length K = N * L

		// Generate a random matrix H with each element sampled independently
		// from the normal distribution
		// N (0, 1), with each column normalized to unit length. Denote H = [v1,
		// v2, ..., vK].
		int signatureDimension = depth * superBitsAmount;

		double[][] v = new double[signatureDimension][dimension];
		Random rand = new Random();

		for (int i = 0; i < signatureDimension; i++) {
			double[] vector = new double[dimension];
			for (int j = 0; j < dimension; j++) {
				vector[j] = rand.nextGaussian();
			}

			normalize(vector);
			v[i] = vector;
		}

		// for i = 0 to L - 1 do
		// for j = 1 to N do
		// w_{iN+j} = v_{iN+j}
		// for k = 1 to j - 1 do
		// w_{iN+j} = w_{iN+j} - w_{iN+k} w^T_{iN+k} v_{iN+j}
		// end for
		// wiN+j = wiN+j / | wiN+j |
		// end for
		// end for
		// Output: H˜ = [w1, w2, ..., wK]

		double[][] hyperplanesArr = new double[signatureDimension][dimension];
		for (int i = 0; i <= superBitsAmount - 1; i++) {
			for (int j = 1; j <= depth; j++) {
				java.lang.System.arraycopy(v[i * depth + j - 1], 0, hyperplanesArr[i * depth + j - 1], 0, dimension);

				for (int k = 1; k <= (j - 1); k++) {
					hyperplanesArr[i * depth + j - 1] = sub(hyperplanesArr[i * depth + j - 1], product(dotProduct(hyperplanesArr[i * depth + k - 1], v[i * depth + j - 1]), hyperplanesArr[i * depth + k - 1]));
				}

				normalize(hyperplanesArr[i * depth + j - 1]);

			}
		}

		this.hyperplanes = Arrays.stream(hyperplanesArr).map(ArrayRealVector::new).toArray(size -> new RealVector[size]);
	}

	/**
	 * Initialize SuperBit algorithm. With code length K = 10000 The K vectors
	 * are orthogonalized in dimension batches of 10000/dimension vectors The resulting mean
	 * error is 0.01
	 * 
	 * @param d
	 */
	public SuperBit(int d) {
		this(d, d, 10000 / d);
	}


	/**
	 * Compute the signature of this vector
	 * 
	 * @param vector
	 * @return
	 */
	@Override
	public boolean[] createSignature(RealVector vector) {
		boolean[] sig = new boolean[this.hyperplanes.length];
		for (int i = 0; i < this.hyperplanes.length; i++) {
			sig[i] = (vector.dotProduct(this.hyperplanes[i]) >= 0);
		}
		return sig;
	}

	@Override
	public int getDimension() {
		return this.dimension;
	}

	/* ---------------------- STATIC ---------------------- */

	private static double[] product(double x, double[] v) {
		double[] r = new double[v.length];
		for (int i = 0; i < v.length; i++) {
			r[i] = x * v[i];
		}
		return r;
	}

	private static double[] sub(double[] a, double[] b) {
		double[] r = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			r[i] = a[i] - b[i];
		}
		return r;
	}

	private static void normalize(double[] vector) {
		double norm = norm(vector);
		for (int i = 0; i < vector.length; i++) {
			vector[i] = vector[i] / norm;
		}

	}

	/**
	 * Returns the norm L2 : sqrt(Sum_i( v_i^2))
	 * 
	 * @param v
	 * @return
	 */
	private static double norm(double[] v) {
		double agg = 0;

		for (int i = 0; i < v.length; i++) {
			agg += (v[i] * v[i]);
		}

		return Math.sqrt(agg);
	}

	private static double dotProduct(double[] v1, double[] v2) {
		double agg = 0;

		for (int i = 0; i < v1.length; i++) {
			agg += (v1[i] * v2[i]);
		}

		return agg;
	}
}
