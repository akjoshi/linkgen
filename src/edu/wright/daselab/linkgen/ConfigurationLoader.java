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
import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.jena.tdb.base.file.FileException;

public final class ConfigurationLoader {
	//this class should only use System.err and no logger/Monitor
	private static ConfigurationLoader _instance = null;	

	private static final String DEFAULT_CONFIG_FILE_LOCATION = "config.properties";
	public static String CONFIG_FILE_LOCATION = DEFAULT_CONFIG_FILE_LOCATION;

	// make sure that no more than one instance is instantiated.
	public synchronized static ConfigurationLoader getInstance() {
		if (_instance == null)
			_instance = new ConfigurationLoader();
		return _instance;
	}

	public final Configuration getConfig(String filePath) throws FileException {
		//at this point, we haven't loaded the path of log4j.properties file, so ignore any logger errors.
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
		Configuration config = null;
		String message="";
		try {
			File configFile = new File(filePath); // outside the jar file
			if (!configFile.exists()){
				message=ErrorCodes.Error.CONFIG_FILE_NOT_EXISTS.toString() + "FAILED - " + configFile;
				Monitor.error(message);
				throw new FileNotFoundException(message);
				}
			config = new PropertiesConfiguration(configFile);
			if (config.isEmpty()){
				message=ErrorCodes.Error.CONFIG_FILE_EMPTY.toString();
				Monitor.error(message);
				throw new FileException(message);
			}
		} catch (Exception e) {
			message=ErrorCodes.Error.CONFIG_FILE_LOAD_ERROR.toString();
			Monitor.error(message);
			throw new FileException(message);
		}
		return (Configuration) config;
	}

	public final Configuration getConfig() throws FileException {		
		if (!DEFAULT_CONFIG_FILE_LOCATION.equals(CONFIG_FILE_LOCATION)){
			return getConfig(CONFIG_FILE_LOCATION);
		}
		
		File configFile = null;
		File base;
		try {
			base = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI())
					.getParentFile();			
		} catch (URISyntaxException e) {
			throw new FileException(ErrorCodes.Error.CONFIG_FILE_LOAD_ERROR.toString());
		}
		configFile = new File(base, DEFAULT_CONFIG_FILE_LOCATION);
		return getConfig(configFile.getAbsolutePath());
	}

}
