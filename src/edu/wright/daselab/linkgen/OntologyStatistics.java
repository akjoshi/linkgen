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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.XSD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.wright.daselab.linkgen.DataTypes.RAND_DATATYPE;
import edu.wright.daselab.linkgen.VoidGenerator.VOID_STAT;

public class OntologyStatistics {
	static Logger logger = LoggerFactory.getLogger(OntologyStatistics.class);

	public OntologyStatistics() {
	}

	private long totalTriples=0;
	static HashSet<OntProperty> propertySet = new HashSet<OntProperty>();
	static HashSet<OntProperty> objectPropertySet = new HashSet<OntProperty>();
	static HashSet<OntProperty> rangeOnlyPropertySet = new HashSet<OntProperty>();
	static HashSet<OntProperty> noDomainAndNoRangePropertySet = new HashSet<OntProperty>();
	static HashSet<OntProperty> domainOnlyPropertySet = new HashSet<OntProperty>();
	static HashSet<OntProperty> domainAndRangePropertySet = new HashSet<OntProperty>();
	static HashSet<OntClass> objectRangePropertySet = new HashSet<OntClass>();

	// storing datatypes only
	static HashSet<Resource> customTypeSet = new HashSet<Resource>();
	static HashSet<Resource> rdfTypeSet = new HashSet<Resource>();
	static HashSet<String> xsduriSet = new HashSet<String>();
	public static String contextGraph;

	private final static String inputOntology = ConfigurationParams.FILE_INPUT_ONTOLOGY;
	private final static String distributionType = ConfigurationParams.DISTRIBUTION_FUNCTION;
	private final static long TRIPLE_SIZE = ConfigurationParams.NUM_DISTINCT_TRIPLES;
	private final static long MEAN_SUBJECT_COUNT = ConfigurationParams.NUM_AVG_SUBJECT_FREQUENCY;

	private final static long MAX_SUBJECT = (long) TRIPLE_SIZE / MEAN_SUBJECT_COUNT;
	private static long MAX_SUBJECT_CREATED_FROM_DISTRIBUTION = MAX_SUBJECT;
	private static boolean isGaussian = false; // by default it is zipf.

	static HashMap<String, Integer> classFrequencyMap = new LinkedHashMap<String, Integer>();
	static HashMap<String, Integer> domainClassFrequencyMap = new LinkedHashMap<String, Integer>();

	static HashMap<Long, Long> subjectFrequencyMap = new LinkedHashMap<Long, Long>();
	static HashMap<String, Integer> datatypeFrequencyMap = new LinkedHashMap<String, Integer>();

	private static ArrayList<String> orderedClasses = new ArrayList<String>();
	private static ArrayList<Long> orderedInstancesIndex = new ArrayList<Long>();
	static HashMap<Long, Long> sortedInstanceFrequencyMap = new HashMap<Long, Long>();
	static ArrayList<Long> sortedInstanceFrequencyList = new ArrayList<Long>();
	static HashMap<String, int[]> mapClassToIDRange = new HashMap<String, int[]>();
	static HashMap<String, Integer> mapClassToMinID = new HashMap<String, Integer>();
	static HashMap<String, Integer> mapClassToMaxID = new HashMap<String, Integer>();

	static HashMap<String, HashSet<OntProperty>> mapClassToProperty = new HashMap<String, HashSet<OntProperty>>();
	

	final static int maxThread = ConfigurationParams.MAX_THREAD;
	DataTypes dt = new DataTypes();
	static OntModel model;
	Node subject, predicate, object;
	Triple triple;
	static RandomDataHolder rdh = new RandomDataHolder();
	RDFWriter rdfwriter = new RDFWriter();//

	static int threadCount = 0;

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
			}
		}
	}

	private void addObjectProperty(OntProperty p) {
		objectPropertySet.add(p);
	}

	private void addDataRange(OntProperty p) {
		OntResource r = p.getRange();
		if (null == r || xsduriSet.contains(r.getURI())) {
			rdfTypeSet.add(r);
		} else {
			customTypeSet.add(r);
		}
	}

	private void incrementMapElement(HashMap<String, Integer> map, String key, int incrementBy) {
		if (map.containsKey(key)) {
			map.put(key, map.get(key) + incrementBy);
		} else {
			map.put(key, incrementBy);
		}
	}

	private long computeCumulativeFrequency(HashMap<String, Integer> map) {
		long count = 0;
		for (String key : map.keySet()) {
			count = count + map.get(key);
		}
		return count;
	}

	private void addObjectRange(OntProperty p) {
		OntResource r = p.getRange();
		if (r.isClass()) {
			objectRangePropertySet.add(r.asClass());
		}
	}

	private void readAllClasses(OntModel model) {
		ExtendedIterator<OntClass> myclasses = model.listClasses();// OntProperties();
	}

	private void readProperty(OntModel model) {
		int tp = 1;
		int tdp = 1;
		int odp = 1;
		int ap = 1;
		int anon = 1;
		int aclass = 1;
		int afp = 1;
		ExtendedIterator<OntProperty> properties = model.listAllOntProperties();// OntProperties();
		while (properties.hasNext()) {
			tp++;
			OntProperty p = (OntProperty) properties.next();
			// System.out.println(p);
			if (p.isAnnotationProperty()) {
				ap++;
			}
			if (p.isAnon()) {
				anon++;
			}

			if (p.isClass()) {
				aclass++;
			}
			if (p.isFunctionalProperty()) {
				afp++;
			}
			if (p.isDatatypeProperty()) {
				// System.out.println(":==>"+p);
				tdp++;
			}
			if (p.isObjectProperty()) {
				odp++;
			}
			// add to all
			propertySet.add(p);

			if ((null == p.getDomain()) && (null == p.getRange())) {
				noDomainAndNoRangePropertySet.add(p);
			} else if ((null == p.getDomain())) {
				rangeOnlyPropertySet.add(p);

			} else if ((null == p.getRange())) {
				domainOnlyPropertySet.add(p);
				addClassToProperty(p.getDomain().getURI(), p);
			} else {
				// has both
				domainAndRangePropertySet.add(p);
				addClassToProperty(p.getDomain().getURI(), p);

			}
			if ((null == p.getDomain()) || (null == p.getRange())) {
				continue;
			}
			if (p.isDatatypeProperty()) {
				addDataRange(p);
				incrementMapElement(datatypeFrequencyMap, p.getRange().getURI(), 1);
			} else if (p.isObjectProperty()) {
				// System.out.println(p.getLocalName());
				// System.out.println(p.getDomain());
				// System.out.println(p.getRange());
				// all classes will be defined - owl:class or rdfs:class
				incrementMapElement(classFrequencyMap, p.getRange().getURI(), 1);
				incrementMapElement(domainClassFrequencyMap, p.getDomain().getURI(), 1);
				addObjectRange(p);
				addClassToProperty(p.getDomain().getURI(), p);

			}
		}

		// System.out.println(tp+":"+tdp+":"+odp);
		// System.out.println(ap+":"+anon+":"+aclass+":"+afp);
	}

	private void addClassToProperty(String classname, OntProperty property) {
		HashSet<OntProperty> hs;
		if (mapClassToProperty.containsKey(classname)) {
			hs = mapClassToProperty.get(classname);
		} else {
			hs = new HashSet<OntProperty>();
		}
		hs.add(property);
		mapClassToProperty.put(classname, hs);
	}

	private void initiateRandomDataHolder() {
		RandomDataHolder rd = new RandomDataHolder();
		rd.run(); // working fine

	}

	public void createModel() {
		// create an empty Model
		Model model = ModelFactory.createDefaultModel();
		// create the resource
		// Resource johnSmith = model.createResource(personURI);
	}

	public void createStatementForDatatypeProperties(Resource subjectResource, Property prop, Literal objectLiteral) {
		Statement s = ResourceFactory.createStatement(subjectResource, prop, objectLiteral);
		System.out.println(s.asTriple());

	}

	public void createStatementForObjectProperties(Resource subjectResource, Property prop, Resource objectResource) {
		Statement s = ResourceFactory.createStatement(subjectResource, prop, objectResource);
		System.out.println(s.asTriple());
	}

	public void createStatementForSubjectBlankNode(Resource subjectResource, Property prop, Literal objectLiteral) {
		Statement s = ResourceFactory.createStatement(subjectResource, prop, objectLiteral);
	}

	public void createStatementForObjectBlankNode(Resource subjectResource, Property prop, Resource objectResource) {
		Statement s = ResourceFactory.createStatement(subjectResource, prop, objectResource);
	}

	public void objectPropertiesForType(Model m, final Resource type) {
		StmtIterator i = m.listStatements();
		while (i.hasNext()) {
			Statement s = i.next();
			System.out.println("Property: " + s.getSubject().getURI());
		}
	}

	private void preprocessing() {
		getAllXSDURis();
	}

	public void readAllProperties(String filename) {
		preprocessing();
		model = loadOntology(filename);
		readProperty(model);
	}

	public void generateStat() {
		readAllProperties(inputOntology);
		// HashMap<String, Integer> hmap = (HashMap<String, Integer>)
		// Util.sortByValue(classFrequencyMap, true);
		// int scount = 1;
		// for (Entry<String, Integer> m : hmap.entrySet()) {
		// System.out.println(m.getKey() + ":" + m.getValue());
		// if (scount++ > 5)
		// break;
		//
		// }
	}

	private boolean isGaussian() {
		if (distributionType.equals("gaussian")) {
			return true;
		}
		return false;
	}

	protected void addStatToVoid() {
		VoidGenerator.addStat_DEPRECATED(VOID_STAT.CLASSES, classFrequencyMap.size());
		VoidGenerator.addStat_DEPRECATED(VOID_STAT.PROPERTIES, propertySet.size());
		VoidGenerator.addStat_DEPRECATED(VOID_STAT.DISTINCT_SUBJECTS, subjectFrequencyMap.size());
		VoidGenerator.addStat_DEPRECATED(VOID_STAT.TRIPLES, TRIPLE_SIZE);
	}

	private void incrementMapElementLong(HashMap<Long, Long> map, Long key, long incrementBy) {
		if (map.containsKey(key)) {
			map.put(key, map.get(key) + incrementBy);
		} else {
			map.put(key, incrementBy);
		}
	}

	protected void getZipfStat() {
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
		while (true) {
			val = z.nextValue();
			// System.out.println(val);
			count = count + 1;
			incrementMapElementLong(subjectFrequencyMap, val, 1);
			if (count > max_triples)
				break;
		}
		MAX_SUBJECT_CREATED_FROM_DISTRIBUTION = subjectFrequencyMap.size();

		// to speed up, disable sorting for large number of triples.
		// this has no effect in triple generation - only the way in which id
		// are allocated for each class.
		// if sorted, the first id represents the most used id.
		// zipf also sorts in the order but not guaranteed. so skipping this
		// process has no effect.
		// for 100Millioin, it takes 69 seconds without sorting and 92 seconds
		// with sorting
		// ex: Zipf distribution generated - with total INSTANCES:10738666 and
		// triples: 100000001 in 92 seconds with sorted subjectFrequencyMap
		sortedInstanceFrequencyMap = (HashMap<Long, Long>) Util.sortByValue(subjectFrequencyMap, true);
		long triple_created_from_zipf = 0;
		long tripleCount = (long) 0;
		for (Long value : sortedInstanceFrequencyMap.keySet()) {
			tripleCount = sortedInstanceFrequencyMap.get(value);
			sortedInstanceFrequencyList.add(tripleCount);
			triple_created_from_zipf = triple_created_from_zipf + tripleCount;
			// System.out.println(value +
			// ":"+sortedInstanceFrequencyMap.get(value));
		}
		Monitor.displayMessageWithTime("Zipf distribution generated - with total INSTANCES:"
				+ subjectFrequencyMap.size() + " and triples: " + triple_created_from_zipf);
	}

	protected void getGaussianStat() {
	}

	/**
	 * approximates the number of instances to be generated per class-based on
	 */
	protected void assignNumberOfInstancesPerClassBasedOnProportion() {
		// ignore null and owl:Thing
		HashMap<String, Integer> sortedDomainClassFrequencyMap = (HashMap<String, Integer>) Util.sortByValue(
				domainClassFrequencyMap, true);
		long totalClassFrequency = computeCumulativeFrequency(sortedDomainClassFrequencyMap);
		long maxInstance = 0;

		long totalInstanceCreated = 0;
		int[] minmax = new int[2];
		logger.info("Max_subject created from distribution:" + MAX_SUBJECT_CREATED_FROM_DISTRIBUTION);
		String classname;
		// sizeof subjectFrequencyMap ~ compare with classFrequencyMap
		for (Entry<String, Integer> m : sortedDomainClassFrequencyMap.entrySet()) {
			if ((null == m.getKey()) || (m.getKey().toLowerCase().endsWith("owl#thing"))) {
				continue;
			}
			classname = m.getKey();
			orderedClasses.add(classname);
			mapClassToMinID.put(classname, (int) (totalInstanceCreated + 1));
			minmax[0] = (int) (totalInstanceCreated + 1);

			maxInstance = (long) Math.ceil(MAX_SUBJECT_CREATED_FROM_DISTRIBUTION * m.getValue() / totalClassFrequency);
			if (maxInstance == 0) {
				maxInstance = 1;
			}
			orderedInstancesIndex.add(maxInstance);
			logger.info(classname + " occurred " + m.getValue()
					+ " times as domain class in ontology definition and will have :" + maxInstance + " instances");
			// genTriplesPerClass(m.getKey(), maxInstance,
			// totalInstanceCreated);
			totalInstanceCreated = totalInstanceCreated + maxInstance;
			minmax[1] = (int) totalInstanceCreated;
			mapClassToIDRange.put(classname, minmax);
			mapClassToMaxID.put(classname, (int) totalInstanceCreated);
			logger.info(classname + " has instances with ID between " + minmax[0] + " and " + minmax[1]);
		}

	}

	private void ThreadedTripleGeneration(){
		Monitor.displayMessage("Generating Triples..");
		for (int i = 0; i < orderedClasses.size(); i++) {
			String classname = orderedClasses.get(i);
			logger.info(classname + " has instances with ID between " + mapClassToMinID.get(classname) + " and "
					+ mapClassToMaxID.get(classname));
			ThreadedTripleGenerationForEachClass(classname);
		}

		System.out.println("Finished all threads");
		// independent of thread
		genDefaultTriplesForRangeOnlyClasses(mapClassToMaxID.get(orderedClasses.get(orderedClasses.size() - 1)) + 1);
		// System.out.println("total triples - " +
		// RDFWriter.NUM_TRIPLES_WROTE_BY_ALL_THREADS);
		rdfwriter.flushAll(false);
		System.out.println("total triples --"+totalTriples);

	}

	private void ThreadedTripleGenerationForEachClass(String classname)  {
		final ExecutorService pool = Executors.newFixedThreadPool(maxThread);
		long instanceFrequency;
		Set<Future<Long>> set = new HashSet<Future<Long>>();
		for (int j = mapClassToMinID.get(classname); j <= mapClassToMaxID.get(classname); j++) {
			try {
				instanceFrequency = sortedInstanceFrequencyList.get(j);
			} catch (Exception ex) {
				instanceFrequency = 1;
				sortedInstanceFrequencyList.add(instanceFrequency);
			}
			logger.info("\t Instance:" + classname + "/" + j + " occurs " + instanceFrequency
					+ " times in addition to default triples");
			if (threadCount >= maxThread) {
				threadCount = 1;
			} else {
				threadCount = threadCount + 1;
			}
			// System.out.println("thread-"+threadCount);
			Callable<Long> worker = new TripleGenerationThread(threadCount, classname, j, instanceFrequency);
			Future<Long> future = pool.submit(worker);
			set.add(future);
		}
		pool.shutdown();
		while (!pool.isTerminated()) {
		}
		
		
	    for (Future<Long> future : set) {
	      try {
			totalTriples += future.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//	      System.out.println("Total triples written:"+sum);
	    }

	}
	
	   
	    
	    
	private void genDefaultTriplesForRangeOnlyClasses(long beginIndexId) {
		classFrequencyMap.keySet().removeAll(domainClassFrequencyMap.keySet());
		// since these do not belong to domainClass, we will simply create
		// only
		// oneInstance for each of them and label with id:1
		// Long instanceIdForRangeOnlyClass = beginIndexId;
		Long instanceIdForRangeOnlyClass = (long) 1;
		for (String classname : classFrequencyMap.keySet()) {
			genDefaultTriples(classname, instanceIdForRangeOnlyClass);
			// instanceIdForRangeOnlyClass++;
		}
	}

	private void genDefaultTriples(String className, long instanceId) {
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
		rdfwriter.writeTriple(triple);

		object = NodeFactory.createURI(className);
		triple = new Triple(subject, predicate, object);
		rdfwriter.writeTriple(triple);
	}

	/*
	 * private void genTriplesPerClass(String className, long maxInstanceId,
	 * long totalInstanceCreated) { long currentInstanceId = 1; long instanceId;
	 * long instanceFrequency = 1; while (currentInstanceId <= maxInstanceId) {
	 * instanceId = currentInstanceId + totalInstanceCreated; try {
	 * instanceFrequency = sortedInstanceFrequencyList.get((int) (instanceId));
	 * } catch (Exception ex) { instanceFrequency = 1;
	 * sortedInstanceFrequencyList.add((long) 1); } logger.info(className +
	 * " hasInstanceId: " + instanceId + " and occurs " + instanceFrequency +
	 * " times in addition to default triples"); genTriplesPerSubject(className,
	 * instanceId, instanceFrequency); currentInstanceId++; } }
	 */

	private Node getRange(OntProperty prop, long instanceId) {
		String uri;
		Node obj = null;
		Object o;
		logger.info("\t\t property:" + prop.toString());
		if (domainOnlyPropertySet.contains(prop)) {
			// this property has noRange defined, so generate anyRandomUri
			o = rdh.getNext(RAND_DATATYPE.ANYURI);
			obj = NodeFactory.createLiteralByValue(o, XSDDatatype.XSDanyURI);
		} else if (prop.isDatatypeProperty()) {
			uri = prop.getRange().toString();
			logger.info("\t\t\t this property has datatype:" + uri);
			if (DataTypes.dtMap.containsKey(uri)) {
				o = rdh.getNext(DataTypes.getDataType(uri));
			} else {
				o = rdh.getNext(RAND_DATATYPE.INT);
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
		if (!domainClassFrequencyMap.containsKey(classname)) {
			logger.info("\t\t\t this objectClass:" + classname
					+ " appears only in rdfs:range and not in rdfs:domain. So using constant id of 1.");
			logger.info("\t\t\t obj.instance:" + classname + "/1");
			return classname + "/1";
		}
		int min = mapClassToMinID.get(classname);
		int max = mapClassToMaxID.get(classname);
		logger.info("\t\t\t this objectClass:" + classname + " has IDs between " + min + " and " + max + " --maxminIds");
		Random generator = new Random(1);
		long val;
		if (min == max) {
			val = min;
		} else {
			val = (min + generator.nextInt(max - min));
			// //@todo this val might not exist - in which case it's a noise.
			// if (val == instanceId) {
			// val = val + 1;
			// }
		}
		logger.info("\t\t\t obj.instance:" + classname + "/" + val);
		return classname + "/" + val;
	}

	private void genTriples(String className, long instanceId) {
		// System.out.println(className + "##" + instanceId);
	}

	public void genTriples() {
		// how many times does each instance appear is provided by gaussian or
		// zipf distribution

	}

	// @todo
	protected void assignNumberOfInstancesPerClassBasedOnZipf() {
		;
	}

	protected void generateRandomValuesForDataTypes() {
		RAND_DATATYPE rdt;
		// always store ANYURI - this is required if the property has noRange.
		// simply create randomUri.
		rdh.storeRandomData(RAND_DATATYPE.ANYURI);
		for (Resource resource : rdfTypeSet) {
			if (dt.isSupportedDataType(resource.toString())) {
				rdt = DataTypes.getDataType(resource.toString());
			} else {
				rdt = RAND_DATATYPE.INT;
			}
			rdh.storeRandomData(rdt);
			// System.out.println(rdt.toString());
		}
		rdh.readAllRandomData();
	}

	public void run() {
		generateStat();
		addStatToVoid();
		if (!isGaussian()) {
			getZipfStat();
		} else {
			getGaussianStat();
		}
		assignNumberOfInstancesPerClassBasedOnProportion();
		generateRandomValuesForDataTypes();
		// initializeTripleGeneration();
		// same but using thread per class
		ThreadedTripleGeneration();

	}

	class TripleGenerationThread implements Callable<Long> {
		private int threadNum;
		Node subject, predicate, object;
		Triple triple;
		RDFWriter rw;
		String className;
		long instanceId;
		long instanceFrequency;

		public Long call() {
			logger.info("Thread# " + threadNum + " - generating triples for class - " + className);
			genTriplesPerSubject(className, instanceId, instanceFrequency);
			return rw.NUM_TRIPLES_WROTE;
		}

		public TripleGenerationThread(int threadNum, String className, long instanceId, long instanceFrequency) {
			this.threadNum = threadNum;
			this.rw = new RDFWriter(threadNum+"");
			this.className = className;
			this.instanceId = instanceId;
			this.instanceFrequency = instanceFrequency;
		}

		private void genTriplesPerSubject(String className, long instanceId, long instanceFrequency) {
			genDefaultTriples(className, instanceId);
			// first iterate through properties for which this is a domain.
			// model.list

			int numPropertySizeForClass = mapClassToProperty.get(className).size();
			logger.info("\t Number of properties for this class:" + className + " is :"
					+ mapClassToProperty.get(className).size());
			logger.info("\t Number of triples to generate using this instanceId : " + className + "/" + instanceId
					+ " is :" + instanceFrequency);
			// two conditions:
			// 1) number of properties > frequency of this id. use only few
			// properties
			// 2) number of properties < frequency of this id. create multiple
			// triples using same property but different object.
			subject = NodeFactory.createURI(className + "/" + instanceId);
			int count = 1;
			while (instanceFrequency > 0) {
				for (OntProperty prop : mapClassToProperty.get(className)) {
					predicate = NodeFactory.createURI(prop.getURI());
					// you can also have <Person hasOpponent Person>
					object = getRange(prop, instanceId);
					triple = new Triple(subject, predicate, object);
					logger.info("\t\t\t" + count + ") " + triple.toString());
					rw.writeTriple(triple);

					count++;
					instanceFrequency = instanceFrequency - 1;
					if (instanceFrequency == 0) {
						break;
					}
				}
			}
			rw.flushAll(false);
		}

		private void genDefaultTriples(String className, long instanceId) {
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
			rw.writeTriple(triple);

			object = NodeFactory.createURI(className);
			triple = new Triple(subject, predicate, object);
			rw.writeTriple(triple);
		}

//		@Override
//		public void run() {
//			logger.info("Thread# " + threadNum + " - generating triples for class - " + className);
//			genTriplesPerSubject(className, instanceId, instanceFrequency);
//		}

	}
}
