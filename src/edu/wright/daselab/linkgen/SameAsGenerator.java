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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.vocabulary.RDF;

public class SameAsGenerator {
	//@todo: make this class independent of configurationparams.
	private final static String sameAsEntitiesFile = ConfigurationParams.FILE_ENTITY;
	private static HashMap<String, HashSet<String>> mapClassToEntitySet = new HashMap<String, HashSet<String>>();
	private static HashMap<String, HashSet<String>> mapClassToGeneratedEntitySet = new HashMap<String, HashSet<String>>();

	private final static String sameasuri = "http://www.w3.org/2002/07/owl#sameAs";

	private RDFWriterForThreads rwf;

	Node subject, predicate, object;
	Triple triple;
	Quad quad;
	Node quadGraph;

	public void write() {
		readEntityFile();
		addInstanceToList();
		generateSameAs();
	}
	
	private void addInstanceToList(){
		Set<String> classes = SharedDataHolder.domainClassFrequencyMap.keySet();
		for(String classname:classes){
			addInstance(SharedDataHolder.mapClassToMinID.get(classname), SharedDataHolder.mapClassToMaxID.get(classname), classname);
		}
	}

	public SameAsGenerator(boolean isquad) {
		String threadName="SameAs";
		this.rwf=new RDFWriterForThreads(isquad, threadName);
	}

	public SameAsGenerator(boolean isquad, String quadGraph) {
		this.quadGraph = NodeFactory.createURI(quadGraph);
	}

	private void addInstance(int startInstanceId, int endInstanceId, String classname) {
		String instance = "";
		for (int i = startInstanceId; i <= endInstanceId; i++) {
			instance = classname + "/" + i;
			addEntityToClass(instance, classname, mapClassToGeneratedEntitySet);
		}
	}

	private void readEntityFile() {
		Model model = ModelFactory.createDefaultModel();
		try {
			model.read(sameAsEntitiesFile, "NTRIPLES");
		} catch (Exception e) {
			Monitor.error(ErrorCodes.Error.LDG_INPUT_SAME_AS_FILE_NOT_FOUND.toString() +" -" + sameAsEntitiesFile);
		}
		NodeIterator classes = model.listObjectsOfProperty(RDF.type);
		while (classes.hasNext()) {
			RDFNode classname = classes.next();
			ResIterator entities = model.listSubjectsWithProperty(RDF.type, classname);
			while (entities.hasNext()) {
				Resource entity = entities.next();
				addEntityToClass(entity.toString(), classname.toString());
			}
		}
	}

	private void addEntityToClass(String entity, String classname) {
		addEntityToClass(entity, classname, mapClassToEntitySet);
	}

	private static void addEntityToClass(String entity, String classname, HashMap<String, HashSet<String>> map) {
		HashSet<String> hset = new HashSet<String>();
		if (map.containsKey(classname)) {
			hset = map.get(classname);
		}
		hset.add(entity);
		map.put(classname, hset);
	}

	private void generateSameAs() {
		Monitor.displayMessage("Generating SameAs");
		for (String classname : mapClassToEntitySet.keySet()) {
			if (!mapClassToGeneratedEntitySet.containsKey(classname)) {
				continue;
			}
			generateSameAs(classname, mapClassToEntitySet.get(classname), mapClassToGeneratedEntitySet.get(classname));
		}
		rwf.write();
		Monitor.displayMessageWithTime("Generating SameAs Complete");
	}

	private void generateSameAs(String classname, HashSet<String> userEntities, HashSet<String> generatedEntities) {

		ArrayList<String> userEntityList = new ArrayList<String>();
		ArrayList<String> generatedEntityList = new ArrayList<String>();

		userEntityList.addAll(userEntities);
		generatedEntityList.addAll(generatedEntities);
		for (int i = 0; i < userEntityList.size(); i++) {
			// the size of generatedEntityList can be small.
			if (i >= generatedEntityList.size()) {
				break;
			}
			subject = NodeFactory.createURI(userEntityList.get(i));
			predicate = NodeFactory.createURI(sameasuri);
			object = NodeFactory.createURI(generatedEntityList.get(i));
			triple = new Triple(subject, predicate, object);
				rwf.addTriple(triple);
		}
	}

}
