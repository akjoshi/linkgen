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

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.wright.daselab.linkgen.ErrorCodes.Error;

/*
 * Maps all values from config.properties to class variables
 */
public final class ConfigurationParams {
	static Logger logger = LoggerFactory.getLogger(ConfigurationParams.class);
	public static Configuration config = ConfigurationLoader.getInstance().getConfig();

	private static enum CONFIG_DATATYPE {
		BOOLEAN, STRING, INT, DOUBLE, LONG,
	}

	private static final String DEFAULT_FILE_LOG4J_PROPERTIES = "log4j.propertiesz";
	final static String LOG_PROPERTIES = getAndLoadLogger("file.log4j.properties");

	private static void isConfigAvailable(String key, String value) {
		if ((value == null) || value.toString().trim().equals("")) {
			logger.error(Error.INVALID_CONFIG_PARAMS.toString() + " : " + key);
			Monitor.safeExit(Error.INVALID_CONFIG_PARAMS);
		}
	}

	private static String getAndLoadLogger(String configkey) {
		String FILE_LOG4J_PROPERTIES = (String) config.getString(configkey);
		if ((null != FILE_LOG4J_PROPERTIES) && (!FILE_LOG4J_PROPERTIES.trim().equals(""))) {
			PropertyConfigurator.configure(FILE_LOG4J_PROPERTIES);
			return FILE_LOG4J_PROPERTIES;
		}
		logger.error("Config File Property Read Error for key:" + configkey);
		FILE_LOG4J_PROPERTIES = DEFAULT_FILE_LOG4J_PROPERTIES;
		File base;
		try {

			base = new File(ConfigurationParams.class.getProtectionDomain().getCodeSource().getLocation().toURI())
					.getParentFile();
			File loggerFile = new File(base, FILE_LOG4J_PROPERTIES);
			System.err.println("Using default " + loggerFile);
			PropertyConfigurator.configure(loggerFile.getAbsolutePath());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return FILE_LOG4J_PROPERTIES;
	}

	private static Object getPropertiesValue(String key, CONFIG_DATATYPE DT) {
		String val = config.getString(key);
		isConfigAvailable(key, val);
		switch (DT) {
		case STRING:
			return val;
		case BOOLEAN:
			return Boolean.parseBoolean(val);
		case INT:
			return Integer.parseInt(val);
		case DOUBLE:
			return Double.parseDouble(val);
		case LONG:
			return Long.parseLong(val);
		default:
			return val;
		}
	}

	// Assign the property keys, treat as constants
	final static boolean MODE_DEBUG = (boolean) getPropertiesValue("debug.mode", CONFIG_DATATYPE.BOOLEAN);
	final static boolean MODE_STREAM = (boolean) getPropertiesValue("stream.mode", CONFIG_DATATYPE.BOOLEAN);
	final static boolean MODE_QUAD_FORMAT = (boolean) getPropertiesValue("quad.format", CONFIG_DATATYPE.BOOLEAN);
	final static boolean GEN_SAMEAS = (boolean) getPropertiesValue("gen.sameas", CONFIG_DATATYPE.BOOLEAN);
	final static boolean GEN_NOISE = (boolean) getPropertiesValue("gen.noise", CONFIG_DATATYPE.BOOLEAN);
	final static int MAX_THREAD = (int) getPropertiesValue("max.thread", CONFIG_DATATYPE.INT);

	// ignored if QUAD_FORMAT IS false
	// final static int NUM_QUAD_GRAPHS = (int)
	// getPropertiesValue("num.quad.graphs", CONFIG_DATATYPE.INT);
	final static long NUM_TRIPLES_PER_OUTPUT = (long) getPropertiesValue("num.triples.per.output", CONFIG_DATATYPE.LONG);
	final static long NUM_TRIPLES_PER_STREAM = (long) getPropertiesValue("num.triples.per.stream", CONFIG_DATATYPE.LONG);
	final static long NUM_DISTINCT_TRIPLES = (long) getPropertiesValue("num.distinct.triples", CONFIG_DATATYPE.LONG);
	final static long NUM_AVG_SUBJECT_FREQUENCY = (long) getPropertiesValue("num.avg.frequency.subject",
			CONFIG_DATATYPE.LONG);

	final static int NUM_STRING = (int) getPropertiesValue("num.string", CONFIG_DATATYPE.INT);
	final static int NUM_INT = (int) getPropertiesValue("num.int", CONFIG_DATATYPE.INT);
	final static int NUM_DOUBLE = (int) getPropertiesValue("num.double", CONFIG_DATATYPE.INT);
	final static int NUM_FLOAT = (int) getPropertiesValue("num.float", CONFIG_DATATYPE.INT);
	final static int NUM_LONG = (int) getPropertiesValue("num.long", CONFIG_DATATYPE.INT);
	final static int NUM_OTHERS = (int) getPropertiesValue("num.others", CONFIG_DATATYPE.INT);

	// alignments
	// final static int NUM_TRIPLES_SAMEAS = (int)
	// getPropertiesValue("num.triples.sameas", CONFIG_DATATYPE.INT);

	// rand seeds
	final static int RAND_SEEDS_XSD_STRING = (int) getPropertiesValue("randseed.xsd.string", CONFIG_DATATYPE.INT);
	final static int RAND_SEEDS_XSD_INT = (int) getPropertiesValue("randseed.xsd.int", CONFIG_DATATYPE.INT);
	final static int RAND_SEEDS_XSD_BOOLEAN = (int) getPropertiesValue("randseed.xsd.boolean", CONFIG_DATATYPE.INT);
	final static int RAND_SEEDS_XSD_FLOAT = (int) getPropertiesValue("randseed.xsd.double", CONFIG_DATATYPE.INT);
	final static int RAND_SEEDS_XSD_DOUBLE = (int) getPropertiesValue("randseed.xsd.float", CONFIG_DATATYPE.INT);
	final static int RAND_SEEDS_XSD_LONG = (int) getPropertiesValue("randseed.xsd.long", CONFIG_DATATYPE.INT);
	final static int RAND_SEEDS_XSD_OTHERS = (int) getPropertiesValue("randseed.xsd.others", CONFIG_DATATYPE.INT);

	final static String FILE_INPUT_ONTOLOGY = (String) getPropertiesValue("file.input.ontology", CONFIG_DATATYPE.STRING);
	final static String FILE_BASEDIR = (String) getPropertiesValue("file.basedir", CONFIG_DATATYPE.STRING);
	final static String FILE_OUTPUT_PREFIX = (String) getPropertiesValue("file.basedir", CONFIG_DATATYPE.STRING);
	final static String FILE_ENTITY = (String) getPropertiesValue("file.entity", CONFIG_DATATYPE.STRING);

	final static String FILE_OUTPUT_DATA_PREFIX = (String) getPropertiesValue("file.output.data.prefix",
			CONFIG_DATATYPE.STRING);
	final static String FILE_OUTPUT_VOID = (String) getPropertiesValue("file.output.void", CONFIG_DATATYPE.STRING);

	final static String NAMESPACE = (String) getPropertiesValue("namespace", CONFIG_DATATYPE.STRING);
	final static String DISTRIBUTION_FUNCTION = (String) getPropertiesValue("distribution.function",
			CONFIG_DATATYPE.STRING);

	final static int NOISE_DATA_TOTAL = (int) getPropertiesValue("noise.data.total", CONFIG_DATATYPE.INT);
	final static int NOISE_DATA_NUM_NOTYPE = (int) getPropertiesValue("noise.data.num.notype", CONFIG_DATATYPE.INT);
	
	final static int NOISE_DATA_NUM_INVALID = (int) getPropertiesValue("noise.data.num.invalid", CONFIG_DATATYPE.INT);
	final static int NOISE_DATA_NUM_DUPLICATE = (int) getPropertiesValue("noise.data.num.duplicate",
			CONFIG_DATATYPE.INT);

	final static double ZIPF_EXPONENT = (double) getPropertiesValue("zipf.exponent", CONFIG_DATATYPE.DOUBLE);
	final static int GAUSSIAN_MEAN = (int) getPropertiesValue("gaussian.mean", CONFIG_DATATYPE.INT);
	final static int GAUSSIAN_DEVIATION = (int) getPropertiesValue("gaussian.deviation", CONFIG_DATATYPE.INT);

	public final static boolean checkStatusOnLoad() throws Exception {
		// using reflection to check all properties/params fields.
		// you can use annotation for better retrieval
		// http://stackoverflow.com/questions/2020202/pitfalls-in-getting-member-variable-values-in-java-with-reflection
		// by this time, none of the values are empty.
		String name = "";
		String value = "";
		logger.info("Displaying all param values:");
		boolean isFine = true;
		Field[] fields = ConfigurationParams.class.getDeclaredFields();
		for (Field field : fields) {
			// check only final static fields
			if (!Modifier.isFinal((field.getModifiers())) || (!Modifier.isStatic(field.getModifiers()))) {
				continue;
			}
			name = field.getName();
			try {
				value = (String) field.get(null).toString();
			} catch (Exception e) {
				Monitor.error(Error.INVALID_CONFIG_PARAMS.toString());
				throw new IllegalArgumentException(Error.INVALID_CONFIG_PARAMS.toString());
			}
			if ((value == null) || value.toString().trim().equals("")) {
				isFine = false;
			}
			String status = isFine ? "OK" : "Failed";
			logger.info(status + " \t" + name + "=" + value);
			if (!isFine)
				throw new IllegalArgumentException(Error.INVALID_CONFIG_PARAMS.toString());
		}
		return isFine;
	}
}
