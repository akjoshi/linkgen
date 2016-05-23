package edu.wright.daselab.linkgen;

import java.util.ArrayList;
import java.util.Random;

public class GaussianGenerator {
	final int randomSeed = 500;
	final Random rand = new Random(randomSeed);
	private Number lastVal;

	private ArrayList<Integer> elements = new ArrayList<Integer>();

	/**
	 * Set the last value generated. NumberGenerator subclasses must use this
	 * call to properly set the last value, or the {@link #lastValue()} calls
	 * won't work.
	 */
	protected void setLastValue(Number last) {
		lastVal = last;
	}

	/**
	 * Number of items.
	 */
	private long items;
	private long min;
	private long max;

	/******************************* Constructors **************************************/

	/**
	 * Create a zipfian generator for the specified number of items.
	 * 
	 * @param _items
	 *            The number of items in the distribution.
	 */
	public GaussianGenerator(int _items) {
		this(0, _items - 1);
	}

	/**
	 * Create a Gaussian generator for items between min and max.
	 * 
	 * @param _min
	 *            The smallest integer to generate in the sequence.
	 * @param max_subjects
	 *            The largest integer to generate in the sequence.
	 */
	public GaussianGenerator(int _min, long max_subjects) {
		this.min = _min;
		this.max = max_subjects;

	}

	public int nextValue() {
		Double d = rand.nextGaussian();
		int val = (int) Math.round(d * 100 + 500);
		if (!elements.contains(d)) {
			elements.add(val);
		}
		return elements.indexOf(val);

	}

	public static void main(String[] args) {
		int max_subjects = 10000;
		int max_triples = 100000000;
		GaussianGenerator z = new GaussianGenerator(max_subjects);// max. number
																	// of
																	// subject
		long count = 0;
		long val = 0;
		while (true) {
			val = z.nextValue();
			System.out.println(val);
			// count=count+val;
			count = count + 1;

			// System.out.println(count);
			if (count > max_triples)
				break;
		}
		// z.nextValue();
	}
}
