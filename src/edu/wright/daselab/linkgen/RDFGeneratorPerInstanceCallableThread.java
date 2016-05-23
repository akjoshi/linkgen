package edu.wright.daselab.linkgen;

import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.Callable;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.wright.daselab.linkgen.DataTypes.RAND_DATATYPE;

public class RDFGeneratorPerInstanceCallableThread implements Callable<Long> {
	private static Logger logger = LoggerFactory.getLogger(BaseRDFGenerator.class);
	private String threadName;

	private RDFWriterForThreads rwf;
	private String className;
	private long instanceId;
	private long instanceFrequency;
	private boolean isQuad;
	private long NUM_TRIPLES_WROTE;

	public void setBatchIndexa() {
		this.threadName = "Batch_0" + "_" + threadName;
		// this.threadName="Batch_0";//single file per batch
		this.rwf = new RDFWriterForThreads(this.isQuad, this.threadName);
	}

	public RDFGeneratorPerInstanceCallableThread(String threadName, String className, long instanceId,
			long instanceFrequency, boolean isQuad) {
		this.threadName = threadName;
		this.isQuad = isQuad;
		this.className = className;
		this.instanceId = instanceId;
		this.instanceFrequency = instanceFrequency;
		this.rwf = new RDFWriterForThreads(this.isQuad, this.threadName);

	}

	public RDFGeneratorPerInstanceCallableThread(String threadName, String className, long instanceId,
			long instanceFrequency, boolean isQuad, Node quadGraphURI) {
		this(threadName, className, instanceId, instanceFrequency, isQuad);
		this.rwf.setQuadGraphURI(quadGraphURI);
	}

	public Long call() {
		logger.info("Thread# " + threadName + " - generating triples for class - " + className);
		logger.info("\t Number of properties for this class:" + className + " is :"
				+ SharedDataHolder.mapClassToProperty.get(className).size());
		logger.info("\t Number of triples to generate using this instanceId : " + className + "/" + instanceId
				+ " is :" + instanceFrequency);
		HashSet<OntProperty> classPropertySet = SharedDataHolder.mapClassToProperty.get(className);
		genTriplesPerInstance(className, instanceId, instanceFrequency, classPropertySet);
		return NUM_TRIPLES_WROTE;
	}

	private void genTriplesPerInstance(String className, long instanceId, long instanceIdFrequency,
			HashSet<OntProperty> classPropertySet) {
		// System.out.println("**"+ currentBatchIndex + "\t" + className +
		// " \t"+instanceId + "\t" + instanceIdFrequency);
		Node subject, predicate, object;
		Triple triple;
		// first iterate through properties for which this is a domain.
		// model.list
		genDefaultTriples(className, instanceId);

		// two conditions:
		// 1) number of properties > frequency of this id. use only few
		// properties
		// 2) number of properties < frequency of this id. create multiple
		// triples using same property but different object.
		subject = NodeFactory.createURI(className + "/" + instanceId);
		int count = 1;
		long localInstanceFrequency = instanceIdFrequency;
		while (localInstanceFrequency > 0) {
			for (OntProperty prop : classPropertySet) {
				predicate = NodeFactory.createURI(prop.getURI());
				// you can also have <Person hasOpponent Person> which will be
				// rejected at the time of writing.
				object = getRange(prop, instanceId);
				triple = new Triple(subject, predicate, object);
				logger.info("\t\t\t" + count + ") " + triple.toString());
				addTriplesToQueue(triple);
				count++;
				localInstanceFrequency = localInstanceFrequency - 1;
				if (localInstanceFrequency == 0) {
					break;
				}
			}
		}
		flushAll();
	}

	private Node getRange(OntProperty prop, long instanceId) {
		String uri;
		Node obj = null;
		Object o;
		logger.info("\t\t property:" + prop.toString());
		if (SharedDataHolder.domainOnlyPropertySet.contains(prop)) {
			// this property has noRange defined, so generate anyRandomUri
			o = SharedDataHolder.rdh.getNext(RAND_DATATYPE.ANYURI);
			obj = NodeFactory.createLiteralByValue(o, XSDDatatype.XSDanyURI);
		} else if (prop.isDatatypeProperty()) {
			uri = prop.getRange().toString();
			logger.info("\t\t\t this property has datatype:" + uri);
			if (DataTypes.dtMap.containsKey(uri)) {
				o = SharedDataHolder.rdh.getNext(DataTypes.getDataType(uri));
			} else {
				o = SharedDataHolder.rdh.getNext(RAND_DATATYPE.INT);
			}

			logger.info("\t\t obj.datatype:" + o);
			obj = NodeFactory.createLiteralByValue(o, DataTypes.mapUriToRDFType.get(uri));

		} else {
			uri = prop.getRange().getURI();
			logger.info("\t\t obj.objectType:" + prop.getRange().getURI());
			uri = prop.getRange().getURI();
			obj = NodeFactory.createURI(getRandomSubjectIdForClass(uri, instanceId));
		}
		return obj;
	}

	private String getRandomSubjectIdForClass(String classname, long instanceId) {
		// since we are fetching some randomSubjectId, it's possible that we get
		// the same Object for sameClass and hence some duplicate triples.

		String instance = "";
		if (!SharedDataHolder.domainClassFrequencyMap.containsKey(classname)) {
			logger.info("\t\t\t this objectClass:" + classname
					+ " appears only in rdfs:range and not in rdfs:domain. So using constant id of 1.");
			logger.info("\t\t\t obj.instance:" + classname + "/1");
			instance = classname + "/1";
			// genDefaultTriples(classname, 1); // this is not required- all
			// classes in domainClass is done before. also for rangeOnlyClass,
			// it's done in gendefaultTriplesForRangeOnlyClass
			return instance;
		}
		int min = SharedDataHolder.mapClassToMinID.get(classname);
		int max = SharedDataHolder.mapClassToMaxID.get(classname);
		logger.info("\t\t\t this objectClass:" + classname + " has IDs between " + min + " and " + max + " --maxminIds");
		Random generator = new Random(1);
		long val;
		if (min == max) {
			val = min;
		} else {
			val = (min + generator.nextInt(max - min));
		}
		logger.info("\t\t\t obj.instance:" + classname + "/" + val);
		return className + "/" + val;
	}

	private void genDefaultTriples(String className, long instanceId) {
		Node subject, predicate, object;
		Triple triple;
		// one for instanceName rdf:type owl:Thing
		// one for instanceName rdf:type className
		// logger.info(className + "#" + instanceId + " rdf:type " +
		// "owl:Thing");
		// logger.info(className + "#" + instanceId + " rdf:type " +
		// className);

		subject = NodeFactory.createURI(className + "/" + instanceId);
		predicate = NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		object = NodeFactory.createURI("http://www.w3.org/2002/07/owl#Thing");
		triple = new Triple(subject, predicate, object);
		addTriplesToQueue(triple);
		object = NodeFactory.createURI(className);
		triple = new Triple(subject, predicate, object);
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
