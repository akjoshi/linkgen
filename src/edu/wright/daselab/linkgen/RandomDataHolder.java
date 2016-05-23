/**
 * 
 * @author Amit Joshi, github.com/akjoshi
 * Data Semantics Lab, Wright State University, Dayton, Ohio, US
 * Copyright (c) 2016
 * License GPL
 * If you use this software, please use following citation:
 * Joshi, A.K., Hitzler, P., Dong, G.: Multi-purpose Synthetic Linked Data Generator https://bitbucket.org/akjoshi/linked-data-generator/
 * Web Id for the resource: http://w3id.org/linkgen
 */
package edu.wright.daselab.linkgen;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.wright.daselab.linkgen.DataTypes.RAND_DATATYPE;

public class RandomDataHolder {
	// number of unique items - this can be obtained directly from config as
	// mimic circularQueue functionality for getting index.
	// queue should be a circular queue.//the numbers can repeat
	// how many unique entities can you have?
	// how many unique literals can you have?
	static Logger logger = LoggerFactory.getLogger(RandomDataHolder.class);
	static HashMap<RAND_DATATYPE, ArrayList<Object>> hmapDatatypeToList = new HashMap<DataTypes.RAND_DATATYPE, ArrayList<Object>>();
	static HashMap<RAND_DATATYPE, Integer> hmapDatatypeToSeed = new HashMap<DataTypes.RAND_DATATYPE, Integer>();
	static HashMap<RAND_DATATYPE, Integer> hmapDatatypeToAmount = new HashMap<DataTypes.RAND_DATATYPE, Integer>();
	static HashMap<RAND_DATATYPE, Integer> indexHolderMap = new HashMap<RAND_DATATYPE, Integer>();

	public RandomDataHolder() {
		loadConfigVariables(0);
	}

	public RandomDataHolder(int batchIndex) {
		loadConfigVariables(batchIndex);
	}

	private void addDefaultValues() {
		for (RAND_DATATYPE rd : RAND_DATATYPE.values()) {
			hmapDatatypeToSeed.put(rd, ConfigurationParams.RAND_SEEDS_XSD_OTHERS);
			hmapDatatypeToAmount.put(rd, ConfigurationParams.NUM_OTHERS);
		}
	}

	private void loadConfigVariables(int incrementSeedBy) {
		// adding batch to seed, helps create more unique values.

		addDefaultValues();
		hmapDatatypeToSeed.put(RAND_DATATYPE.BOOLEAN, ConfigurationParams.RAND_SEEDS_XSD_BOOLEAN + incrementSeedBy);
		hmapDatatypeToSeed.put(RAND_DATATYPE.FLOAT, ConfigurationParams.RAND_SEEDS_XSD_FLOAT + incrementSeedBy);
		hmapDatatypeToSeed.put(RAND_DATATYPE.INT, ConfigurationParams.RAND_SEEDS_XSD_INT + incrementSeedBy);
		hmapDatatypeToSeed.put(RAND_DATATYPE.DOUBLE, ConfigurationParams.RAND_SEEDS_XSD_DOUBLE + incrementSeedBy);
		hmapDatatypeToSeed.put(RAND_DATATYPE.LONG, ConfigurationParams.RAND_SEEDS_XSD_LONG + incrementSeedBy);
		hmapDatatypeToSeed.put(RAND_DATATYPE.STRING, ConfigurationParams.RAND_SEEDS_XSD_STRING + incrementSeedBy);
		hmapDatatypeToSeed.put(RAND_DATATYPE.ANYURI, ConfigurationParams.RAND_SEEDS_XSD_OTHERS + incrementSeedBy);

		hmapDatatypeToAmount.put(RAND_DATATYPE.FLOAT, ConfigurationParams.NUM_FLOAT);
		hmapDatatypeToAmount.put(RAND_DATATYPE.INT, ConfigurationParams.NUM_INT);
		hmapDatatypeToAmount.put(RAND_DATATYPE.DOUBLE, ConfigurationParams.NUM_DOUBLE);
		hmapDatatypeToAmount.put(RAND_DATATYPE.LONG, ConfigurationParams.NUM_LONG);
		hmapDatatypeToAmount.put(RAND_DATATYPE.STRING, ConfigurationParams.NUM_STRING);
	}

	/**
	 * gets next value in the list. if the end of list is reached, returns the
	 * first element.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getNext(RAND_DATATYPE DT) {
		int nextIndex = 0;
		if (indexHolderMap.containsKey(DT)) {
			nextIndex = indexHolderMap.get(DT) + 1;
		}
		if (hmapDatatypeToList.get(DT).size() == nextIndex) {
			nextIndex = 0;
		}
		indexHolderMap.put(DT, nextIndex);
		return (T) hmapDatatypeToList.get(DT).get(nextIndex);
	}

	@SuppressWarnings("unused")
	private <T> void storeRandomData(ArrayList<T> q, RAND_DATATYPE DT, long seed, int amount) {
		// @todo improve to store in invidiual arrayList if required.
		ReproducibleRandomGenerator<T> rg = new ReproducibleRandomGenerator<T>(seed, amount);
		@SuppressWarnings("unchecked")
		T[] elements = (T[]) rg.genRandom(DT);
		addElementToQueue(q, elements);
	}

	private <T> void addElementToQueue(ArrayList<T> q, T[] elements) {
		for (int i = 0; i < elements.length; i++) {
			q.add(elements[i]);
		}
	}

	private <T> void readDataFromQueue(ArrayList<T> q) {
		for (int i = 0; i < q.size(); i++) {
			logger.info(q.get(i).toString());
		}
	}

	public void readAllRandomData() {
		for (RAND_DATATYPE rd : RAND_DATATYPE.values()) {

			if (null != hmapDatatypeToList.get(rd)) {
				logger.info(rd.toString());
				readDataFromQueue(hmapDatatypeToList.get(rd));
			}
		}
	}

	private void storeRandomData(RAND_DATATYPE DT, int seed, int amount) {
		ArrayList<Object> list = new ArrayList<Object>();
		ReproducibleRandomGenerator<Object> rg = new ReproducibleRandomGenerator<Object>(seed, amount);
		Object[] elements = rg.genRandom(DT);
		addElementToQueue(list, elements);
		hmapDatatypeToList.put(DT, list);
	}

	protected void storeRandomData(RAND_DATATYPE rdt) {
		storeRandomData(rdt, hmapDatatypeToSeed.get(rdt), hmapDatatypeToAmount.get(rdt));
	}

	public void run() {
		storeRandomData(RAND_DATATYPE.ANYURI);
		// check what datatypes need to be stored and how many of them need to
		// be store.
		// storeRandomData(RAND_DATATYPE.FLOAT);
		// storeRandomData(RAND_DATATYPE.INT);
		// storeRandomData(RAND_DATATYPE.DOUBLE);
		// storeRandomData(RAND_DATATYPE.LONG);
		// storeRandomData(RAND_DATATYPE.BOOLEAN);
		// storeRandomData(RAND_DATATYPE.STRING);
		// storeRandomData(RAND_DATATYPE.DATE);
		// storeRandomData(RAND_DATATYPE.DATETIME);
		// storeRandomData(RAND_DATATYPE.TIME);
		// storeRandomData(RAND_DATATYPE.ANYURI);
		// storeRandomData(RAND_DATATYPE.GYEAR);
		// storeRandomData(RAND_DATATYPE.UNSIGNED_INT);
		// storeRandomData(RAND_DATATYPE.POSITIVE_INTEGER);
		// storeRandomData(RAND_DATATYPE.NEGATIVE_INTEGER);
		// storeRandomData(RAND_DATATYPE.NON_POSITIVE_INTEGER);
		// storeRandomData(RAND_DATATYPE.NON_NEGATIVE_INTEGER);

		readAllRandomData();
	}

	public static void main(String[] args) {
		RandomDataHolder rdh = new RandomDataHolder();
		rdh.run();
	}
}

/*
 * todo 1. store different common datatypes ex: positiveinteger 2. check all
 * current datatypes 3. generate triples for zipfian 4. add owl:sameAs
 * functionality - sameAsImplement 5. add noise functionality 6. generate
 * triples for gaussian. 7. update void
 */

