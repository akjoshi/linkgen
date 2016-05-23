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

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.XSD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstanceGenerator {
	private final static Logger logger = LoggerFactory.getLogger(InstanceGenerator.class);
	private final static String inputOntology = ConfigurationParams.FILE_INPUT_ONTOLOGY;
	private final static String distributionType = ConfigurationParams.DISTRIBUTION_FUNCTION;
	private final static long TRIPLE_SIZE = ConfigurationParams.NUM_DISTINCT_TRIPLES;
	private final static long MEAN_SUBJECT_COUNT = ConfigurationParams.NUM_AVG_SUBJECT_FREQUENCY;
	private final static long MAX_SUBJECT = (long) TRIPLE_SIZE / MEAN_SUBJECT_COUNT;

	public static String contextGraph;
	static HashSet<String> xsduriSet = new HashSet<String>();
	RDFWriter rdfwriter = new RDFWriter();
	static OntModel model;
	Node subject, predicate, object;
	Triple triple;
	static int threadCount = 0;

	public InstanceGenerator() {
	}

	protected OntModel loadOntology(String filename) {
		OntModel model = ModelFactory.createOntologyModel();
		InputStream in = FileManager.get().open(filename);
		if (in == null) {
			logger.error(ErrorCodes.Error.LDG_ONTOLOGY_FILE_NOT_FOUND.toString());
		}
		model.read(in, "");
		contextGraph = model.getNsPrefixURI("");
		if (null == contextGraph) {
			logger.error("Ontology does not contain xmlns value. ex: xmlns=\"http://topbraid.org/schema/\"");
			logger.error("using default xmlns=\"" + ConfigurationParams.NAMESPACE + "\"");
			contextGraph = ConfigurationParams.NAMESPACE;
		}
		logger.info("Base Namespace/contextGraph:" + contextGraph);
		return model;
	}

	private void getAllXSDURis() {
		Field[] fields = XSD.class.getDeclaredFields();
		String uri;
		for (Field field : fields) {
			// check only public static fields
			if ((!Modifier.isPublic(field.getModifiers()))) {
				continue;
			}
			try {
				uri = (String) field.get(null).toString();
				xsduriSet.add(uri);
				logger.info("Added:" + uri);
			} catch (Exception e) {
				logger.info("Skipping. Can't get XSD uri for " + field.getName());
				//e.printStackTrace();
			}
		}
		 fields = null;
	}

	private void addDataRange(OntProperty p) {
		OntResource r = p.getRange();
		if (null == r || xsduriSet.contains(r.getURI())) {
			SharedDataHolder.rdfTypeSet.add(r);
		} else {
			SharedDataHolder.customTypeSet.add(r);
		}
	}

	private void addObjectRange(OntProperty p) {
		OntResource r = p.getRange();
		if (r.isClass()) {
			SharedDataHolder.objectRangePropertySet.add(r.asClass());
		}
	}

	private void incrementMapElement(HashMap<String, Integer> map, String key, int incrementBy) {
		if (map.containsKey(key)) {
			map.put(key, map.get(key) + incrementBy);
		} else {
			map.put(key, incrementBy);
		}
	}


	private void readProperty(OntModel model) {
		ExtendedIterator<OntProperty> properties = model.listAllOntProperties();// OntProperties();
		while (properties.hasNext()) {
			OntProperty p = (OntProperty) properties.next();
			SharedDataHolder.propertySet.add(p);
			if ((null == p.getDomain()) && (null == p.getRange())) {
				SharedDataHolder.noDomainAndNoRangePropertySet.add(p);
			} else if ((null == p.getDomain())) {
				SharedDataHolder.rangeOnlyPropertySet.add(p);

			} else if ((null == p.getRange())) {
				SharedDataHolder.domainOnlyPropertySet.add(p);
				addClassToProperty(p.getDomain().getURI(), p);
			} else {
				// has both
				SharedDataHolder.domainAndRangePropertySet.add(p);
				addClassToProperty(p.getDomain().getURI(), p);

			}
			if ((null == p.getDomain()) || (null == p.getRange())) {
				continue;
			}
			if (p.isDatatypeProperty()) {
				addDataRange(p);
				incrementMapElement(SharedDataHolder.datatypeFrequencyMap, p.getRange().getURI(), 1);
			} else if (p.isObjectProperty()) {
				// System.out.println(p.getLocalName());
				// System.out.println(p.getDomain());
				// System.out.println(p.getRange());
				// all classes will be defined - owl:class or rdfs:class
				incrementMapElement(SharedDataHolder.classFrequencyMap, p.getRange().getURI(), 1);
				incrementMapElement(SharedDataHolder.domainClassFrequencyMap, p.getDomain().getURI(), 1);
				addObjectRange(p);
				addClassToProperty(p.getDomain().getURI(), p);
			}
		}
		properties = null;
	}

	private void addClassToProperty(String classname, OntProperty property) {
		HashSet<OntProperty> hs;
		if (SharedDataHolder.mapClassToProperty.containsKey(classname)) {
			hs = SharedDataHolder.mapClassToProperty.get(classname);
		} else {
			hs = new HashSet<OntProperty>();
		}
		hs.add(property);
		SharedDataHolder.mapClassToProperty.put(classname, hs);
		hs = null;
	}

	public void objectPropertiesForType(Model m, final Resource type) {
		StmtIterator i = m.listStatements();
		while (i.hasNext()) {
			Statement s = i.next();
			System.out.println("Property: " + s.getSubject().getURI());
		}
		
		i =null;
	}

	public void readAllProperties(String filename) {
		getAllXSDURis();
		model = loadOntology(filename);
		readProperty(model);
	}

	private boolean isGaussian() {
		if (distributionType.equals("gaussian")) {
			return true;
		}
		return false;
	}

	private void incrementMapElementLong(HashMap<Long, Long> map, Long key, long incrementBy) {
		if (map.containsKey(key)) {
			map.put(key, map.get(key) + incrementBy);
		} else {
			map.put(key, incrementBy);
		}
	}

	protected void getZipfStat(int numMaxInstancesPerBatch) {
		Monitor.displayMessage("Generating ZipfDistribution of Instances");
		long max_subjects = MAX_SUBJECT;
		long max_triples = TRIPLE_SIZE;
		logger.info("max-subjects:" + max_subjects);
		logger.info("max-triples:" + max_triples);
		ZipfianGenerator z = new ZipfianGenerator(1, max_subjects);
		// generates 1,2,1,4,5,1,7,8,1
		// output is the id of subject
		long count = 0;
		long val = 0;
        HashSet<Long> subjectSet = new HashSet<Long>();
		long countPerBatch = 0;
		long countIncludingDefaultTriples=0; 
		
		
		while (true) {
			val = z.nextValue();
			subjectSet.add(val);
			// System.out.println(val);
			count = count + 1;
			countPerBatch = countPerBatch + 1;
			// by default we create two triples for each subject, rdf:type (owl:thing,classname)
			// however, we may need to delete triples that have same subject and object ex: subject opponentOf subject.
			// @todo so experiment it from 1 o 2 ex: 1.25, 1.5, 1.75 and 2 to 1.5 instead of 2.
			countIncludingDefaultTriples = (long) (count + subjectSet.size()*1.25);
			incrementMapElementLong(SharedDataHolder.subjectFrequencyMap, val, 1);
			if (countPerBatch == numMaxInstancesPerBatch) {
				countPerBatch = 0;
			}
			if (countIncludingDefaultTriples > max_triples)
				break;
		}
		Monitor.displayMessageWithTime("Zipf distribution generated - with total INSTANCES:" + subjectSet.size()
				+ " and triples: " + countIncludingDefaultTriples);
		z =null;
		subjectSet = null;
	}

	protected void getZipfStat() {
	getZipfStat(-1);//do for all. no splitting
//		getZipfStat(1000000);//create new batch after this triples' have been created.
	}

	protected void getGaussianStat() {
		Monitor.displayMessage("Generating Gaussian Distribution of Instances");
		long max_subjects = MAX_SUBJECT;
		long max_triples = TRIPLE_SIZE;
		logger.info("max-subjects:" + max_subjects);
		logger.info("max-triples:" + max_triples);
		GaussianGenerator z = new GaussianGenerator(1, max_subjects);
		// generates 1,2,1,4,5,1,7,8,1
		// output is the id of subject
		long count = 0;
		long val = 0;
        HashSet<Long> subjectSet = new HashSet<Long>();
		long countPerBatch = 0;
		long countIncludingDefaultTriples=0; 	
		
		while (true) {
			val = z.nextValue();
			subjectSet.add(val);
			// System.out.println(val);
			count = count + 1;
			countPerBatch = countPerBatch + 1;
			// by default we create two triples for each subject, rdf:type (owl:thing,classname)
			// however, we may need to delete triples that have same subject and object ex: subject opponentOf subject.
			// @todo so experiment it from 1 o 2 ex: 1.25, 1.5, 1.75 and 2 to 1.5 instead of 2.
			countIncludingDefaultTriples = (long) (count + subjectSet.size()*1.25);
			incrementMapElementLong(SharedDataHolder.subjectFrequencyMap, val, 1);

			if (countIncludingDefaultTriples > max_triples)
				break;
		}
		Monitor.displayMessageWithTime("Zipf distribution generated - with total INSTANCES:" + subjectSet.size()
				+ " and triples: " + countIncludingDefaultTriples);
		z =null;
		subjectSet = null;
	}

	public void run() {
		readAllProperties(inputOntology);
		if (!isGaussian()) {
			getZipfStat();
		} else {
			getGaussianStat();
		}
	}

}
