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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.Quad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDFWriter {

	private final static Logger logger = LoggerFactory.getLogger(RDFWriter.class);
	private final static boolean isStream = ConfigurationParams.MODE_STREAM;
	private final static String filePrefix = ConfigurationParams.FILE_OUTPUT_DATA_PREFIX;

	private static long CHUNK_SIZE_TRIPLE_PER_FILE = ConfigurationParams.NUM_TRIPLES_PER_OUTPUT;
	private static long CHUNK_SIZE_TRIPLE_PER_STREAM = ConfigurationParams.NUM_TRIPLES_PER_STREAM;

	private Queue<Quad> quadQueue = new LinkedList<Quad>();
	private Queue<Triple> tripleQueue = new LinkedList<Triple>();

	public long NUM_TRIPLES_WROTE = 0;
	private int fileCounter = 1;
	private OutputStream os; // one output per thread

	private String threadName;
	private String fileNamePrefixForThread;

	public RDFWriter() {
		RDFWriteStart("0");
	}

	public RDFWriter(String threadName) {
		RDFWriteStart(threadName);
	}

	private synchronized void RDFWriteStart(String threadName) {
		// System.out.println("thread from writer#" + threadNumber);
		this.threadName = threadName;
		this.fileNamePrefixForThread = filePrefix + "T" + threadName + "_";

		if (CHUNK_SIZE_TRIPLE_PER_STREAM < 2)
			CHUNK_SIZE_TRIPLE_PER_STREAM = 2;
		if (CHUNK_SIZE_TRIPLE_PER_FILE < 2)
			CHUNK_SIZE_TRIPLE_PER_FILE = 2;
		// don't create outputs here. it will create a chain effect - the files
		// will be created without ensuring a write is required.
	}

	private void updateFileCounter() {
		fileCounter++;
	}

	private synchronized void updateOutputStream() {
		if (isStream) {
			os = System.out;
		} else {
			String filename = fileNamePrefixForThread + fileCounter;
			try {
				os = new FileOutputStream(filename, true);

			} catch (FileNotFoundException e) {
				logger.error(ErrorCodes.Error.LDG_OUTPUT_FILE_WRITE_ERROR.toString() + ":\t" + filename);
				e.printStackTrace();
			}
			updateFileCounter();
		}
	}

	public enum STATEMENT_TYPE {
		WITH_DATATYPE_PROPERTY, WITH_OBJECTPROPERTY, WITH_BLANKNODE_OBJECT, WITH_BLANKNODE_SUBJECT
	}

	public void writeTriple(Triple triple) {
		// skip triples with same subject and object ex:Person1 opponentOf
		// Person1
		// remove this instancedId from object.
		// http://dbpedia.org/ontology/Person/1
		// http://dbpedia.org/ontology/opponent
		// http://dbpedia.org/ontology/Person/1

		if (triple.getSubject().toString().equals(triple.getObject().toString())) {
			logger.info("\t\t\t" + triple.toString() + " - -same subject and object - removing this triple");
			return;
		}
		NUM_TRIPLES_WROTE++;
		addToQueue(triple);

	}

	public void addToQueue(Triple triple) {
		tripleQueue.add(triple);
		long chunksize = isStream ? CHUNK_SIZE_TRIPLE_PER_STREAM : CHUNK_SIZE_TRIPLE_PER_FILE;
		if (tripleQueue.size() == chunksize) {
			updateOutputStream();
			RDFDataMgr.writeTriples(os, tripleQueue.iterator());
			tripleQueue.clear();
		}
	}

	public void flushAll(boolean isQuad) {
		if (null == os)
			updateOutputStream();
		if (isQuad) {
			RDFDataMgr.writeQuads(os, quadQueue.iterator());
		} else {
			RDFDataMgr.writeTriples(os, tripleQueue.iterator());
		}
		logger.info("Thread#" + threadName + " wrote " + NUM_TRIPLES_WROTE + " triples.");
	}

	public void writeQuad(Quad quad) {
		quadQueue.add(quad);
		long chunksize = isStream ? CHUNK_SIZE_TRIPLE_PER_STREAM : CHUNK_SIZE_TRIPLE_PER_FILE;
		if (quadQueue.size() == chunksize) {
			updateOutputStream();
			RDFDataMgr.writeQuads(os, quadQueue.iterator());
			quadQueue.clear();
		}
	}
}
