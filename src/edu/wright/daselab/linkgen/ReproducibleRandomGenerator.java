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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.wright.daselab.linkgen.DataTypes.RAND_DATATYPE;

public class ReproducibleRandomGenerator<DT> {
	static Logger logger = LoggerFactory.getLogger(ReproducibleRandomGenerator.class);
	private long seed;
	private DT min;
	private DT max;
	private boolean isRange = false;
	private int amount;

	private static final String namespace = ConfigurationParams.NAMESPACE;

	public ReproducibleRandomGenerator(long seed, int amount) {
		this.seed = seed;
		this.amount = amount;
	}

	@SuppressWarnings("unchecked")
	public <T> T[] genRandom(RAND_DATATYPE DT) {
		T[] randomList = (T[]) new Object[amount];
		Random generator = new Random(seed);
		for (int i = 0; i < amount; i++) {
			randomList[i] = (T) (Object) getRandomValue(generator, DT);
			seed--;
		}
		return randomList;
	}

	@SuppressWarnings(value = { "unchecked" })
	private <T> T getRandomValue(Random generator, RAND_DATATYPE DT) {
		T out;
		switch (DT) {
		case LONG:
		case INTEGER:
			out = (T) (Object) ((Long) (generator.nextLong()));
			break;
		case INT:
			if (isRange) {
				out = (T) (Object) ((int) min + (int) (generator.nextInt() * (((int) max - (int) min) + 1)));
			} else {
				out = (T) (Object) Math.abs((int) (generator.nextInt()));
			}
			break;
		case DOUBLE:
		case DECIMAL:
			out = (T) (Object) ((Double) (generator.nextDouble()));
			break;
		case FLOAT:
			out = (T) (Object) ((Float) (generator.nextFloat()));
			break;
		case BOOLEAN:
			out = (T) (Object) (generator.nextBoolean());
			break;
		case POSITIVE_INTEGER:
			// does not include zero, so add 1.
			out = (T) (Object) Math.abs((int) (generator.nextInt() + 1));
			break;
		case NON_POSITIVE_INTEGER:
			// An integer containing only non-positive values (..,-2,-1,0)
			out = (T) (Object) (-1 * Math.abs((int) (generator.nextInt())));

			break;
		case NEGATIVE_INTEGER:
			// does not include zero, so add -1
			out = (T) (Object) (-1 * Math.abs((int) (generator.nextInt() - 1)));
			break;
		case NON_NEGATIVE_INTEGER:
			// An integer containing only non-negative values (0,1,2,..)
			out = (T) (Object) Math.abs((int) (generator.nextInt()) + 1);
			break;
		case UNSIGNED_INT:
			out = (T) (Object) Util.getUnsignedInt((generator.nextInt()));
			break;
		case STRING:
			out = (T) (Object) RandomStringGenerator.generateRandomString(Math.abs(generator.nextInt()));
			break;
		case DATE:
			int minDay = (int) LocalDate.of(1900, 1, 1).toEpochDay();
			int maxDay = (int) LocalDate.of(2016, 1, 1).toEpochDay();
			long randomDay = minDay + generator.nextInt(maxDay - minDay);
			LocalDate randomBirthDate = LocalDate.ofEpochDay(randomDay);
			out = (T) (Object) randomBirthDate.toString();
			break;
		case TIME:
			// @todo: put the upper limit for the nextLong
			LocalTime time = LocalTime.MIN.plusSeconds(Math.abs(generator.nextInt()));
			out = (T) (Object) time.toString();
			break;
		case DATETIME:
			int minYear = 1000;
			int maxYear = 2016;
			LocalDateTime ldt = LocalDateTime.now().minusDays(Math.abs(generator.nextInt((maxYear - minYear) * 365)));
			out = (T) (Object) ldt.toString();
			break;
		case GYEAR:
			// get GYear between 1 and 2016
			int min = 1000;
			int max = 2016;
			out = (T) (Object) Math.abs((min + (generator.nextInt(max - min + 1))));
			break;
		case ANYURI:
			out = (T) (Object) (namespace + "anyuri_" + Math.abs(generator.nextLong()));
			break;
		default:
			// if not supported, use an int, randomly generated using current
			// timestamp.
			out = null;
			break;
		}
		return out;
	}

	// check datatypes - it can be int, long, float, double, short
	// Queue of Numbers - first in first out
}

// @todo - build better random string generator -using dictionary words or concept based.
class RandomStringGenerator {
	private static final String CHAR_LIST = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
	private static final int STRING_LENGTH = 10;
	private static Random generator;

	public static String generateRandomString(long seed) {
		generator = new Random(seed);
		StringBuffer randomString = new StringBuffer();
		for (int i = 0; i < STRING_LENGTH; i++) {
			int number = generator.nextInt(CHAR_LIST.length()-1);
			randomString.append(CHAR_LIST.charAt(number));
		}
		return randomString.toString();
	}
}
