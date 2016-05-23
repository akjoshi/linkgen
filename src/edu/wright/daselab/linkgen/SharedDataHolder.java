package edu.wright.daselab.linkgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Resource;

public class SharedDataHolder {

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

	static HashMap<String, Integer> classFrequencyMap = new LinkedHashMap<String, Integer>();
	static HashMap<String, Integer> domainClassFrequencyMap = new LinkedHashMap<String, Integer>();

	static HashMap<Long, Long> subjectFrequencyMap = new LinkedHashMap<Long, Long>();
	static HashMap<String, Integer> datatypeFrequencyMap = new LinkedHashMap<String, Integer>();

	static ArrayList<String> orderedClasses = new ArrayList<String>();
	static ArrayList<Long> orderedInstancesIndex = new ArrayList<Long>();
	static HashMap<Long, Long> sortedInstanceFrequencyMap = new HashMap<Long, Long>();
	static ArrayList<Long> sortedInstanceFrequencyList = new ArrayList<Long>();
	static HashMap<String, int[]> mapClassToIDRange = new HashMap<String, int[]>();
	static HashMap<String, Integer> mapClassToMinID = new HashMap<String, Integer>();
	static HashMap<String, Integer> mapClassToMaxID = new HashMap<String, Integer>();

	static HashMap<String, HashSet<OntProperty>> mapClassToProperty = new HashMap<String, HashSet<OntProperty>>();
	static RandomDataHolder rdh = new RandomDataHolder();

	static long numTripleWrotePerBatch ;

	public SharedDataHolder() {
	}
}
