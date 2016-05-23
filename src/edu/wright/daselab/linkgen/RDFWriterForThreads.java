package edu.wright.daselab.linkgen;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.Quad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDFWriterForThreads {
	private final static Logger logger = LoggerFactory.getLogger(RDFWriterForThreads.class);
	private final static boolean isStream = ConfigurationParams.MODE_STREAM;
	private final static String filePrefix = ConfigurationParams.FILE_OUTPUT_DATA_PREFIX;

	private Queue<Quad> quadQueue = new LinkedList<Quad>();
	private Queue<Triple> tripleQueue = new LinkedList<Triple>();

	private boolean isQuad = false;

	private OutputStream os; // one output per thread
	private String threadName;

	private Node quadGraphNode;

	private static long CHUNK_SIZE_TRIPLE_PER_FILE = ConfigurationParams.NUM_TRIPLES_PER_OUTPUT;
	private static long CHUNK_SIZE_TRIPLE_PER_STREAM = ConfigurationParams.NUM_TRIPLES_PER_STREAM;

	public void setQuadGraphURI(Node quadGraphURI) {
		this.quadGraphNode = quadGraphURI;
	}

	public RDFWriterForThreads(boolean isQuad, String threadName) {
		this.isQuad = isQuad;
		this.threadName = threadName;
	}

	public void addTriple(Triple triple) {

		int queueSize;
		if (isQuad) {
			Quad quad = new Quad(quadGraphNode, triple);
			quadQueue.add(quad);
			queueSize = quadQueue.size();
		} else {
			tripleQueue.add(triple);
			queueSize = tripleQueue.size();
		}
		long chunksize = isStream ? CHUNK_SIZE_TRIPLE_PER_STREAM : CHUNK_SIZE_TRIPLE_PER_FILE;
		if (queueSize >= chunksize) {
			write();
		}
	}

public void write() {
		String filename = filePrefix + "T" + threadName + "_1";
		if (isQuad) {
			synchronized (this) {
				try {
					os = (isStream) ? System.out : new FileOutputStream(filename, true);
					RDFDataMgr.writeQuads(os, quadQueue.iterator());
					os.flush();
					os.close();
					quadQueue.clear();
				} catch (Exception e) {
					logger.error(ErrorCodes.Error.LDG_OUTPUT_FILE_WRITE_ERROR.toString() + ":\t" + filename);
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			
		} else {
			synchronized (this) {
				try {
					os = (isStream) ? System.out : new FileOutputStream(filename, true);
					RDFDataMgr.writeTriples(os, tripleQueue.iterator());
					os.flush();
					os.close();
					tripleQueue.clear();
				} catch (Exception e) {
					logger.error(ErrorCodes.Error.LDG_OUTPUT_FILE_WRITE_ERROR.toString() + ":\t" + filename);
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
		}

	}
}
