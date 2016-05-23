package edu.wright.daselab.linkgen;

import java.util.ArrayList;
import java.util.Random;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;

public class NoiseGenerator {

	private static int NUM_NOISE_NO_TYPE = ConfigurationParams.NOISE_DATA_NUM_NOTYPE;
	private static int NUM_NOISE_INVALID = ConfigurationParams.NOISE_DATA_NUM_INVALID;
	private static int NUM_NOISE_TOTAL = ConfigurationParams.NOISE_DATA_TOTAL;
	private static int NUM_NOISE_DUPLICATE = ConfigurationParams.NOISE_DATA_NUM_DUPLICATE;

	Node quadGraph;
	RDFWriterForThreads rwf;
	private static ArrayList<Triple> fewValidTriples = new ArrayList<Triple>();
	private static ArrayList<String> validClasses = new ArrayList<String>();

	public static void addTripleForNoise(Triple triple) {
		if (fewValidTriples.size() > (int) (0.5 * NUM_NOISE_DUPLICATE)) {
			return;
		}
		fewValidTriples.add(triple);
	}

	public NoiseGenerator(boolean isQuad) {
		this.rwf = new RDFWriterForThreads(isQuad, "_Noise_");
		validClasses.addAll(SharedDataHolder.domainClassFrequencyMap.keySet());

	}

	public NoiseGenerator(boolean isQuad, String quadGraph) {
		this(isQuad);
		this.quadGraph = NodeFactory.createURI(quadGraph);
	}

	public void write() {
		writeDuplicate();
		writeInvalid();
		writeNoType();
		rwf.write();
	}

	private void writeInvalid() {
		// use some elements from validTriples as reference.
		int size = fewValidTriples.size();
		int totalInvalidToWritePerTriple = NUM_NOISE_INVALID;
		if (NUM_NOISE_INVALID > size) {
			totalInvalidToWritePerTriple = (int) (NUM_NOISE_INVALID / size) + 1;
			;
		}
		for (int i = 0; i < size; i++) {
			genInvalid(fewValidTriples.get(i), totalInvalidToWritePerTriple);
		}
	}

	private void writeNoType() {
		// use some elements from validTriples as reference.
		int size = fewValidTriples.size();
		int totalNoTypesToWritePerTriple = NUM_NOISE_NO_TYPE;
		if (NUM_NOISE_NO_TYPE > size) {
			totalNoTypesToWritePerTriple = (int) (NUM_NOISE_NO_TYPE / size) + 1;
			;
		}
		for (int i = 0; i < size; i++) {
			genNoType(fewValidTriples.get(i), totalNoTypesToWritePerTriple);
		}
	}

	private void genNoType(Triple triple, int num) {
		String classname;
		Node object;
		Triple invalidTriple;
		Random val = new Random(2);

		for (int i = 0; i < validClasses.size(); i++) {
			classname = validClasses.get(i);
			object = NodeFactory.createURI(classname + "/noise_notype_" + val.nextInt(10000));
			invalidTriple = new Triple(triple.getSubject(), triple.getPredicate(), object);
			rwf.addTriple(invalidTriple);
			if (i >= num) {
				break;
			}
		}
	}

	private void genInvalid(Triple valid, int num) {
		// generates random ids with no types as well.
		// not all of these are invalid - some are valid objectClass
		String classname;
		Node object;
		Triple triple;
		Random val = new Random(1);
		for (int i = 0; i < validClasses.size(); i++) {
			classname = validClasses.get(i);
			object = NodeFactory.createURI(classname + "/noise_invalid_" + val.nextInt(10000));
			triple = new Triple(valid.getSubject(), valid.getPredicate(), object);
			rwf.addTriple(triple);
			if (i >= num) {
				break;
			}
		}
	}

	private void writeDuplicate() {
		int totalDuplicatesToWrite = NUM_NOISE_TOTAL - NUM_NOISE_INVALID;
		int size = fewValidTriples.size();
		for (int i = 0; i < size; totalDuplicatesToWrite--, i++) {
			rwf.addTriple(fewValidTriples.get(i));
			if (i <= size - 1 && totalDuplicatesToWrite > 0) {
				i = 0;
			}
			if (totalDuplicatesToWrite <= 0) {
				return;
			}
		}
	}
}
