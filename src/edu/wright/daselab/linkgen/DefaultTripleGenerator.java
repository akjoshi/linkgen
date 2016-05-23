package edu.wright.daselab.linkgen;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultTripleGenerator {
	RDFWriterForThreads rwf;
	boolean isQuad;
	int NUM_TRIPLES_WROTE;
	int NumTypeToHide;
	Node quadGraph;

	private final static Logger logger = LoggerFactory.getLogger(DefaultTripleGenerator.class);

	public DefaultTripleGenerator() {

	}

	public DefaultTripleGenerator(boolean isQuad, int totalNoType) {
		this.NumTypeToHide = totalNoType;
		this.isQuad = isQuad;
		this.rwf = new RDFWriterForThreads(isQuad, "");
	}

	public DefaultTripleGenerator(boolean isQuad, Node quadgraph, int totalNoType) {
		this(true, totalNoType);
		this.quadGraph = quadgraph;
	}

	private void addDefaultTriples(String className, String instance) {
		this.NumTypeToHide = this.NumTypeToHide - 2;
		if (this.NumTypeToHide > 0) {
			return;
		}
		Node subject, predicate, object;
		Triple triple;
		// one for instanceName rdf:type owl:Thing
		// one for instanceName rdf:type className
		subject = NodeFactory.createURI(instance);
		predicate = NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		object = NodeFactory.createURI("http://www.w3.org/2002/07/owl#Thing");
		triple = new Triple(subject, predicate, object);

		addTriplesToQueue(triple);
		object = NodeFactory.createURI(className);
		triple = new Triple(subject, predicate, object);
		// skip until we have removed type information
		addTriplesToQueue(triple);
	}

	private void addTriplesToQueue(Triple triple) {
		if (triple.getSubject().toString().equals(triple.getObject().toString())) {
			logger.info("\t\t\t" + triple.toString() + " - -same subject and object - removing this triple");
			return;
		}
		rwf.addTriple(triple);
		NUM_TRIPLES_WROTE++;
	}

	private void flushAll() {
		rwf.write();
	}

}
