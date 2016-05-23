package edu.wright.daselab.linkgen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseRDFGenerator {
	private static Logger logger = LoggerFactory.getLogger(BaseRDFGenerator.class);
	private static long NUM_TRIPLES = ConfigurationParams.NUM_DISTINCT_TRIPLES;
	protected static long totalTriples;
	private static int maxThread;
	private int threadCount = 0;
	// private static ThreadPoolExecutor poolExecutor;

	private static RDFWriterForThreads rdfwriter;
	private static boolean isQuad;
	private static Node quadGraphURI;


	public BaseRDFGenerator(boolean _isQuad, int _maxThread) {
		isQuad = _isQuad;
		maxThread = _maxThread;
		// initializeThreadPool();
		rdfwriter = new RDFWriterForThreads(isQuad, "0");
	}

	public BaseRDFGenerator(boolean _isQuad, String quadGraphURIPrefix, int _maxThread) {
		isQuad = _isQuad;
		maxThread = _maxThread;
		quadGraphURI = NodeFactory.createURI(quadGraphURIPrefix + "/0");

		// initializeThreadPool();
	}

	/*
	 * private void initializeThreadPool() { // final ExecutorService pool =
	 * Executors.newFixedThreadPool(maxThread); int corePoolSize = 4; int
	 * maximumPoolSize = 8; int keepAliveTime = 5000;
	 * 
	 * if (maxThread == 1) { // run sequentially corePoolSize = 1;
	 * maximumPoolSize = 1000; } poolExecutor = new
	 * ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime,
	 * TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>()); }
	 */

	public abstract void run();

	protected void generateTriples(int indexOfSplittedSubjectMap) {
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
		SharedDataHolder.sortedInstanceFrequencyMap = (HashMap<Long, Long>) Util.sortByValue(
				SharedDataHolder.subjectFrequencyMap, true);
		long triple_created_from_zipf = 0;
		long tripleCount = (long) 0;
		for (Long value : SharedDataHolder.sortedInstanceFrequencyMap.keySet()) {
			tripleCount = SharedDataHolder.sortedInstanceFrequencyMap.get(value);
			SharedDataHolder.sortedInstanceFrequencyList.add(tripleCount);
			triple_created_from_zipf = triple_created_from_zipf + tripleCount;
			// System.out.println(value +
			// ":"+sortedInstanceFrequencyMap.get(value));
		}
		assignNumberOfInstancesPerClassBasedOnProportion();
		// generateTriplesWithInstanceThread();
		generateTriplesWithClassThread();
	}

	/**
	 * approximates the number of instances to be generated per class-based on
	 */
	protected void assignNumberOfInstancesPerClassBasedOnProportion() {
		// ignore null and owl:Thing
		HashMap<String, Integer> sortedDomainClassFrequencyMap = (HashMap<String, Integer>) Util.sortByValue(
				SharedDataHolder.domainClassFrequencyMap, true);
		long totalClassFrequency = Util.computeCumulativeFrequency(sortedDomainClassFrequencyMap);
		long maxInstance = 0;

		long totalInstanceCreated = 0;
		int[] minmax = new int[2];

		long Subjectcount = SharedDataHolder.subjectFrequencyMap.size();
		String classname;
		// sizeof subjectFrequencyMap ~ compare with classFrequencyMap
		for (Entry<String, Integer> m : sortedDomainClassFrequencyMap.entrySet()) {
			if ((null == m.getKey()) || (m.getKey().toLowerCase().endsWith("owl#thing"))) {
				continue;
			}
			classname = m.getKey();
			SharedDataHolder.orderedClasses.add(classname);
			SharedDataHolder.mapClassToMinID.put(classname, (int) (totalInstanceCreated + 1));
			minmax[0] = (int) (totalInstanceCreated + 1);

			maxInstance = (long) Math.ceil(Subjectcount * m.getValue() / totalClassFrequency);
			if (maxInstance == 0) {
				maxInstance = 1;
			}
			SharedDataHolder.orderedInstancesIndex.add(maxInstance);
			logger.info(classname + " occurred " + m.getValue()
					+ " times as domain class in ontology definition and will have :" + maxInstance + " instances");
			// genTriplesPerClass(m.getKey(), maxInstance,
			// totalInstanceCreated);
			totalInstanceCreated = totalInstanceCreated + maxInstance;
			minmax[1] = (int) totalInstanceCreated;
			SharedDataHolder.mapClassToIDRange.put(classname, minmax);
			SharedDataHolder.mapClassToMaxID.put(classname, (int) totalInstanceCreated);
			logger.info(classname + " has instances with ID between " + minmax[0] + " and " + minmax[1]);
		}
		sortedDomainClassFrequencyMap = null;
	}

	protected void generateTriplesWithInstanceThread() {
		Monitor.displayMessage("Generating Triples..");
		int size = SharedDataHolder.orderedClasses.size();
		// @todo - why reverse order? It really doesn't matter but we wanted to
		// capture every single subjects created in zipf/gaussian
		// if we do in descending order of occurrence, the least frequent
		// subject may not appear in output
		// so, reversing the loop order. the most frequent subject is guaranteed
		// to appear always in every batch.
		for (int i = size - 1; i >= 0; i--) {
			String classname = SharedDataHolder.orderedClasses.get(i);
			logger.info(classname + " has instances with ID between " + SharedDataHolder.mapClassToMinID.get(classname)
					+ " and " + SharedDataHolder.mapClassToMaxID.get(classname));
			genTriplesForEachClassWithInstanceThread(classname);
			Monitor.displayMessageWithTime("\tFinished all instance threads for the class: " + classname
					+ ". \n\tTotal triples generated in this batch :" + totalTriples);
			
		}

		flushAll();
	}

	private void genTriplesForEachClassWithInstanceThread(String classname) {
		// @todo - check
		if (totalTriples >= NUM_TRIPLES) {
			return;
		}

		ExecutorService pool = Executors.newFixedThreadPool(maxThread);
		String threadName;
		long instanceFrequency;
		int minId = SharedDataHolder.mapClassToMinID.get(classname);
		int maxId = SharedDataHolder.mapClassToMaxID.get(classname);
		Set<Future<Long>> set = new HashSet<Future<Long>>();
		Callable<Long> worker;
		for (int j = minId; j <= maxId; j++) {
			try {
				instanceFrequency = SharedDataHolder.sortedInstanceFrequencyList.get(j);
			} catch (Exception ex) {
				instanceFrequency = 1;
				SharedDataHolder.sortedInstanceFrequencyList.add(instanceFrequency);
			}
			logger.info("\t Instance:" + classname + "/" + j + " occurs " + instanceFrequency
					+ " times in addition to default triples");
			if (threadCount >= maxThread) {
				threadCount = 1;
			} else {
				threadCount = threadCount + 1;
			}
			// threadName = poolid + j + "_";
			threadName = threadCount + "";
			// System.out.println("thread-"+threadCount);
			if (isQuad) {
				worker = new RDFGeneratorPerInstanceCallableThread(threadName, classname, j,
						instanceFrequency, true, quadGraphURI);

			} else {
				worker = new RDFGeneratorPerInstanceCallableThread(threadName, classname, j,
						instanceFrequency, false);
			}
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
			// System.out.println("Total triples written:"+sum);
		}

		pool = null;
		worker = null;
		set = null;
	}

	protected void generateTriplesWithClassThread() {
		Monitor.displayMessage("Generating Triples..");
		int size = SharedDataHolder.orderedClasses.size();
		// @todo - why reverse order? It really doesn't matter but we wanted to
		// capture every single subjects created in zipf/gaussian
		// if we do in descending order of occurrence, the least frequent
		// subject may not appear in output
		// so, reversing the loop order. the most frequent subject is guaranteed
		// to appear always in every batch.

		ExecutorService pool = Executors.newFixedThreadPool(maxThread);
		String threadName;
		Set<Future<Long>> set = new HashSet<Future<Long>>();
		Callable<Long> worker;
		for (int i = size - 1; i >= 0; i--) {
			String classname = SharedDataHolder.orderedClasses.get(i);
			logger.info(classname + " has instances with ID between " + SharedDataHolder.mapClassToMinID.get(classname)
					+ " and " + SharedDataHolder.mapClassToMaxID.get(classname));
			if (threadCount >= maxThread) {
				threadCount = 1;
			} else {
				threadCount = threadCount + 1;
			}
			// threadName = poolid + j + "_";
			threadName = threadCount + "";
			// System.out.println("thread-"+threadCount);
			if (isQuad) {
				worker = new RDFGeneratorPerClassCallableThread(threadName, classname, true,quadGraphURI);

			} else {
				worker = new RDFGeneratorPerClassCallableThread(threadName, classname, false);
			}
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
			// System.out.println("Total triples written:"+sum);
		}

		pool = null;
		worker = null;
		set = null;
		flushAll();
		Monitor.displayMessageWithTime("\tFinished all threads:" 
				+ ". \n\tTotal triples generated in this batch :" + totalTriples);

	}

	protected void genDefaultTriplesForRangeOnlyClasses() {
		HashSet<String> allClass = new HashSet<String>();
		allClass.addAll(SharedDataHolder.classFrequencyMap.keySet());
		HashSet<String> domainClassOnly = new HashSet<String>();
		domainClassOnly.addAll(SharedDataHolder.domainClassFrequencyMap.keySet());

		allClass.removeAll(domainClassOnly); // this now becomes rangeOnlyClass
		Monitor.displayMessage(" Size of rangeOnly Classes is: " + allClass.size());
		// since these do not belong to domainClass, we will simply create
		// only
		// oneInstance for each of them and label with id:1
		// Long instanceIdForRangeOnlyClass = beginIndexId;
		Long instanceIdForRangeOnlyClass = (long) 1;
		for (String classname : allClass) {
			genDefaultTriples(classname, instanceIdForRangeOnlyClass);
		}
		allClass = null;
		domainClassOnly = null;
		flushAll();
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
		
		NoiseGenerator.addTripleForNoise(triple);
		rdfwriter.addTriple(triple);
	}

	private void flushAll() {
		rdfwriter.write();
	}
}
