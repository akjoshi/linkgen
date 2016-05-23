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

public class RDFGeneratorPerClassCallableThread implements Callable<Long> {
	private static Logger logger = LoggerFactory.getLogger(BaseRDFGenerator.class);
	private String threadName;

	private RDFWriterForThreads rwf;
	private String className;
	private boolean isQuad;

	private long NUM_TRIPLES_WROTE;

	public RDFGeneratorPerClassCallableThread(String threadName, String className, boolean isQuad) {
		this.threadName = threadName;
		this.isQuad = isQuad;
		this.className = className;
		this.rwf = new RDFWriterForThreads(this.isQuad, this.threadName);
	}

	public RDFGeneratorPerClassCallableThread(String threadName, String className, boolean isQuad,
			Node quadGraphURI) {
		this (threadName,className,isQuad);
		this.rwf.setQuadGraphURI(quadGraphURI);

	}

	public Long call() {
		logger.info("Thread# " + threadName + " - generating triples for class - " + className);
		HashSet<OntProperty> classPropertySet = SharedDataHolder.mapClassToProperty.get(className);
		logger.info("\t Number of properties for this class:" + className + " is :"
				+  classPropertySet.size());
		genTriplesForEachClass(className);
		return NUM_TRIPLES_WROTE;
	}

	private void genTriplesForEachClass(String classname) {
		long instanceFrequency;
		int minId = SharedDataHolder.mapClassToMinID.get(classname);
		int maxId = SharedDataHolder.mapClassToMaxID.get(classname);
		HashSet<OntProperty> classPropertySet = SharedDataHolder.mapClassToProperty.get(className);
		for (int j = minId; j <= maxId; j++) {
			try {
				instanceFrequency = SharedDataHolder.sortedInstanceFrequencyList.get(j);
			} catch (Exception ex) {
				instanceFrequency = 1;
				SharedDataHolder.sortedInstanceFrequencyList.add(instanceFrequency);
				ex.printStackTrace();
			}
			genTriplesPerInstance(className, j, instanceFrequency,classPropertySet);
			logger.info("\t Instance:" + classname + "/" + j + " occurs " + instanceFrequency
					+ " times in addition to default triples");
			
		}
		flushAll();
	}

	private void genTriplesPerInstance(String className, long instanceId, long instanceIdFrequency,HashSet<OntProperty> classPropertySet) {
		genDefaultTriples(className,instanceId);
		
//		System.out.println("**" + currentBatchIndex + "\t" + className + " \t" + instanceId + "\t"		+ instanceIdFrequency);
		Node subject, predicate, object;
		Triple triple;
		// first iterate through properties for which this is a domain.
		// model.list

		// two conditions:
		// 1) number of properties > frequency of this id. use only few
		// properties
		// 2) number of properties < frequency of this id. create multiple
		// triples using same property but different object.
		subject = NodeFactory.createURI(className + "/" + instanceId);
		int count = 1;
		long localInstanceFrequency = instanceIdFrequency;
		
		//at least one property - 
		int numPerProperty = (int) ((instanceIdFrequency/classPropertySet.size()) + 1);
		int numWrittenFromRanges=0;
		boolean domultipleRange = true;
		while (localInstanceFrequency > 0) {
			for (OntProperty prop : classPropertySet) {
				predicate = NodeFactory.createURI(prop.getURI());
				// you can also have <Person hasOpponent Person> which will be
				// rejected at the time of writing.

				//getRange will pick up one of the existing instances, and it can return same too. 
				//@todo  in the futre, make it a set.
				if (domultipleRange){
				numWrittenFromRanges=writeMultipleRanges(subject,predicate,instanceId,prop,numPerProperty);
				count  = count +numWrittenFromRanges;
				localInstanceFrequency = localInstanceFrequency - numWrittenFromRanges;
				}
				else {				
				object = getRange(prop, instanceId);
				triple = new Triple(subject, predicate, object);
				logger.info("\t\t\t" + count + ") " + triple.toString());
				addTriplesToQueue(triple);
				count++;
				localInstanceFrequency = localInstanceFrequency - 1;
				}
				if (localInstanceFrequency <= 0) {
					break;
				}
			}
		}
	}
	
	
	private int writeMultipleRanges(Node subject, Node predicate,long instanceId, OntProperty prop, int numTriplesToGenerate){	
		//prevents duplicate triples.
		HashSet<Node> rangeSet = new HashSet<Node>();
		Node object;
		Triple triple;
		int totalWritten=0;
		for (int i=0;i<numTriplesToGenerate;i++){
			object = getRange(prop,instanceId);
//			if (rangeSet.contains(object)) { continue;}
			rangeSet.add(object);
			triple = new Triple(subject, predicate, object);			
			addTriplesToQueue(triple);
			totalWritten++;
		}
		return totalWritten;		
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
