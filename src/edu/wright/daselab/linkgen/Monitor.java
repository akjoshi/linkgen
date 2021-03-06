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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
/*
 * Independent of logger. Use this for startup, exit and any important message to display. 
 * For debugging and funtion specific message, use logger.debug, logger.info and logger.trace
 */
public class Monitor {
	static long startTime;
	final boolean STREAM_MODE = ConfigurationParams.MODE_STREAM;

	public Monitor() {
	}

	/*
	 * http://slf4j.42922.n3.nabble.com/Logging-to-file-with-slf4j-Logger-Where-do
	 * -log-file-go-td46087.html It appears that you have misunderstood the
	 * purpose of SLF4J. If you place slf4j-jdk14-1.5.6.jar then slf4j-api will
	 * bind with java.util.logging. Logback will not be used. Only if you place
	 * logback-core.jar and logback-classic.jar on your class path (but not
	 * slf4j-jdk14-1.5.6.jar) will SLF4J API bind with logback. SLF4J binds with
	 * one and only one underlying logging API (per JVM launch).
	 */
	public static void safeExit(ErrorCodes.Error errorCode) {
		stop("Error occurred: Safe Exiting...");
	}

	public static void displayMessage(String message) {
		System.out.println(message);
	}

	public static void error(String message){
		System.err.println(message);
	}
	public static void start(String message) {
		if (message.trim() != "") {
			displayMessage(message);
		}
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		startTime = System.currentTimeMillis();
		displayMessage("Start Time: " + dateFormat.format(date));
	}
	
	public static void displayMessageWithTime(String message){
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		Long end = System.currentTimeMillis();
		if (message.trim() != "") {
			displayMessage(message);
		}
		displayMessage(" Time Now:" + dateFormat.format(date));
		displayMessage("Duration from startTime:" + (end - startTime) / 1000 + " sec");
	}

	public static void stop(String message) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		Long end = System.currentTimeMillis();
		if (message.trim() != "") {
			displayMessage(message);
		}
		displayMessage("End Time:" + dateFormat.format(date));
		displayMessage("Duration:" + (end - startTime) / 1000 + " sec");
		System.exit(1);
	}
}
