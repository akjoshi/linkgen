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
import java.util.HashSet;
import java.util.Iterator;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OntologyLoader {
	private String filename;
	static Logger logger = LoggerFactory.getLogger(Generator.class);
	
	HashSet<OntProperty> propertySet = new HashSet<OntProperty>();
	private static String contextGraph;
	

	public String getContextGraph(){
		return contextGraph;
	}
	public OntologyLoader(String filename) {
		this.filename = filename;
	}

	private OntModel loadOntology() {
		OntModel model = ModelFactory.createOntologyModel();		
		InputStream in = FileManager.get().open(filename);
		if (in == null) {
			logger.error(ErrorCodes.Error.LDG_ONTOLOGY_FILE_NOT_FOUND.toString());
		}
		model.read(in, "");
		contextGraph = model.getNsPrefixURI("");
		System.out.println("contextGrpah:"+contextGraph);
		return model;
	}

	private void readClasses(OntModel model) {
		try {
			ExtendedIterator<OntClass> classes = model.listClasses();
			while (classes.hasNext()) {
				OntClass oclass = (OntClass) classes.next();
				System.out.println(oclass.toString());
				if (oclass.isAnon()) {
					System.out.println("\t is blank node");
					continue;
				}
				String cname = oclass.getLocalName().toString();
				if (oclass.hasSubClass()) {
					System.out.println("Classe: " + cname);

					OntClass oc = model.getOntClass(cname);
					if (oc != null) {
						for (ExtendedIterator<OntClass> i = oc.listSubClasses(); i.hasNext();) {
							OntClass c = (OntClass) i.next();
							System.out.print("   " + c.getLocalName() + " " + "\n");
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			// error occurs for blank node as in schemaorg.owl file

		}
	}
	
	private void readProperty(OntModel model){
		int tprop=0;
		int invalidprop=0;
		int validprop=0;
		ExtendedIterator<OntProperty> properties = model.listOntProperties();
		while (properties.hasNext()) {
			tprop++;
			OntProperty p = (OntProperty) properties.next();
			if (p.isDatatypeProperty() && (null==p.getDomain())){
//				System.out.println(p.toString() +" is datatype  but has no domain.");
				invalidprop++;
				continue;
			}
			if (p.isAnnotationProperty()) {
				continue;			
			}
			//ignore datatypes that have no domain - why some datatypes don't have domain ?
			if (null==p.getRange()) {
				invalidprop++;
//				System.out.println(p.toString() +" has no range");
				continue;
			}
			validprop++;
			propertySet.add(p);
			//remove primitive property
			OntResource thisResource =  p.getDomain();
			System.out.println(thisResource.getURI());
			// logger.info("Found class: " + thisClass.toString());
			
//			System.out.print("\t Domain:"+p.getDomain().toString());
//			System.out.println("\t"+p.getRange().());
		}
		System.out.println(tprop);
		System.out.println(invalidprop);
		System.out.println(validprop);
	}

	@SuppressWarnings("unused")
	private void readOntology() {
		OntModel model = loadOntology();
//		readClasses(model);
		readProperty(model);
		readClasses(model);
		ExtendedIterator<OntClass> classes = model.listClasses();
		while (classes.hasNext()) {
			OntClass thisClass = (OntClass) classes.next();
			// logger.info("Found class: " + thisClass.toString());
			ExtendedIterator<?> instances = thisClass.listInstances();
			while (instances.hasNext()) {
				Individual thisInstance = (Individual) instances.next();
				// logger.info("  Found instance: " + thisInstance.toString());
			}
		}

		ExtendedIterator<?> properties = model.listOntProperties();
		while (properties.hasNext()) {
			OntProperty p = (OntProperty) properties.next();
			System.out.println(p.toString());
			// if(p.isFunctionalProperty()) {
			// p.removeProperty(RDF.type, OWL.FunctionalProperty);
			// }
		}

		while (classes.hasNext()) {
			OntClass thisClass = (OntClass) classes.next();
			// logger.info("Found class: " + thisClass.toString());
			ExtendedIterator<?> instances = thisClass.listInstances();
			while (instances.hasNext()) {
				Individual thisInstance = (Individual) instances.next();
				// logger.info("  Found instance: " + thisInstance.toString());
			}
		}

		Iterator<OntClass> anonClass = model.listClasses();
		System.out.println(anonClass.next());
		// model.listAllOntProperties();
		for (Iterator<OntClass> i = model.listClasses(); i.hasNext();) {
			OntClass c = i.next();
			System.out.println(c.getURI());
		}
	}

	public void run() {
		readOntology();
	}

}
