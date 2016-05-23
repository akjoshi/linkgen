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
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * creates a void.ttl file for storing statistics about the generated dataset.
 * void spec defined in https://www.w3.org/TR/void/#statistics
 */
public class VoidGenerator {
	static Logger logger = LoggerFactory.getLogger(VoidGenerator.class);
	private final static String voidFile = ConfigurationParams.FILE_OUTPUT_VOID;

	private static HashMap<VOID_STAT, Long> statMap = new HashMap<VoidGenerator.VOID_STAT, Long>();
	private static StringBuilder sb = new StringBuilder();
	private boolean isQuad = ConfigurationParams.MODE_QUAD_FORMAT;

	public VoidGenerator(){
		System.out.println("Generating void..");
	}
	public static enum VOID_STAT {
		TRIPLES("triples"), ENTITIES("entities"), CLASSES("classes"), PROPERTIES("properties"), DISTINCT_SUBJECTS(
				"distinctSubjects"), DISTINCT_OBJECTS("distinctObjects");
		// We use void:triples and void:entites over void:documents.
		private final String voidPropertyName;

		private VOID_STAT(String voidPropertyName) {
			this.voidPropertyName = "void:" + voidPropertyName;
		}
	}

	public static void addStat_DEPRECATED(VOID_STAT vs, long number) {
		statMap.put(vs, number);
	}

	private static void addStat(VOID_STAT vs, long number) {
		statMap.put(vs, number);
	}

	public void writeVoid(long totalTriples) {
		addStat(VOID_STAT.CLASSES, SharedDataHolder.classFrequencyMap.size());
		addStat(VOID_STAT.PROPERTIES, SharedDataHolder.propertySet.size());
		addStat(VOID_STAT.DISTINCT_SUBJECTS, SharedDataHolder.subjectFrequencyMap.size());
		addStat(VOID_STAT.TRIPLES, totalTriples);
		generateVoidStatements();
		writeToFile();
	}

	private void appendLine(String str) {
		sb.append(str);
		sb.append(System.getProperty("line.separator"));
	}

	private void generateVoidStatements() {
		Date date = new Date();
		String today = new SimpleDateFormat("yyyy-MM-dd").format(date);
		String format = "<http://www.w3.org/ns/formats/N-Triples>";
		if (isQuad) {
			format = "<http://www.w3.org/ns/formats/N-Quads>";
		}
		appendLine("@prefix void: <http://rdfs.org/ns/void#> .");
		appendLine("@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .");
		appendLine("@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .");
		appendLine("@prefix owl: <http://www.w3.org/2002/07/owl#> .");
		appendLine("@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .");
		appendLine("@prefix dcterms: <http://purl.org/dc/terms/> .");
		appendLine("@prefix foaf: <http://xmlns.com/foaf/0.1/> .");
		appendLine("@prefix wv: <http://vocab.org/waiver/terms/norms> .");
		appendLine("@prefix sd: <http://www.w3.org/ns/sparql-service-description#> .");
		appendLine("@prefix : # .");

		appendLine(":LinkGen ");
		appendLine(" rdf:type void:Dataset;");
		appendLine(" dcterms:title \"Synthetic Linked Data\";");
		appendLine(" dcterms:description \"Synthetic Linked Data Generated Using LinkGen\";");
		appendLine(" dcterms:modified \"" + today + "\"^^xsd:date;");
		appendLine(" dcterms:contributor \":AmitJoshi\";");
		appendLine(" dcterms:contributor \":WrightState\";");
		appendLine(" void:feature " + format + ";");

		for (Map.Entry<VOID_STAT, Long> entry : statMap.entrySet()) {
			appendLine(" " + entry.getKey().voidPropertyName + " " + entry.getValue() + " ;");
		}
		// todo: add structural metadata.

		appendLine(":AmitJoshi");
		appendLine(" rdf:type foaf:Person;");
		appendLine(" rdfs:label \"Amit Joshi\";");
		appendLine(" foaf:mbox <mailto:joshi.35@wright.edu>;");
		appendLine(":WrightState");
		appendLine(" rdf:type foaf:Organization;");
		appendLine(" rdfs:label \"Wright State University\";");
		appendLine(" foaf:homepage <http://www.wright.edu/>;");
		appendLine(" .");
	}

	private void writeToFile() {
		// ex: statMap.put(VOID_STAT.DISTINCT_OBJECTS, (long) 100);
		File f = new File(voidFile);
		byte[] bytesArray = sb.toString().getBytes();
		try {
			FileOutputStream fos = new FileOutputStream(f, false);
			fos.write(bytesArray);
			fos.flush();
			fos.close();
		} catch (IOException e) {
			// void error
			logger.error(ErrorCodes.Error.LDG_VOID_OUTPUT_FILE_WRITE_ERROR.toString());
			logger.info(sb.toString());

			e.printStackTrace();
		}
	}
}
