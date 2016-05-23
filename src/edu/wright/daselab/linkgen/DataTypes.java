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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.vocabulary.XSD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataTypes {
	static Logger logger = LoggerFactory.getLogger(DataTypes.class);
	static HashSet<String> xsduriSet = new HashSet<String>();
	
	static HashMap<String, RAND_DATATYPE> dtMap = new HashMap<String, RAND_DATATYPE>();
	static HashMap<String, RDFDatatype> mapUriToRDFType = new HashMap<String, RDFDatatype>();

	public DataTypes() {
		getAllXSDURis();
		mapToRanDdataType();
		mapToXsdDataType();
	}

	// @todo: improve this functionality to support all XSD datatypes
	public static enum RAND_DATATYPE {
		BOOLEAN, STRING, INT, DOUBLE, LONG, FLOAT, INTEGER, DECIMAL, POSITIVE_INTEGER, NON_POSITIVE_INTEGER, NEGATIVE_INTEGER, NON_NEGATIVE_INTEGER, UNSIGNED_INT, DATE, DATETIME, TIME, GYEAR, ANYURI, NOT_SUPPORTED_XSD, CUSTOM_DATATYPE
		// for unsupported datatype, we will simply use integer
	}

	private  void mapToXsdDataType() {
		// mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#",
		// XSDDataType.NOT_SUPPORTED);

		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#float", XSDDatatype.XSDfloat);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#double", XSDDatatype.XSDdouble);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#int", XSDDatatype.XSDint);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#long", XSDDatatype.XSDlong);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#short", XSDDatatype.XSDshort);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#byte", XSDDatatype.XSDbyte);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#boolean", XSDDatatype.XSDboolean);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#string", XSDDatatype.XSDstring);

		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#unsignedByte", XSDDatatype.XSDunsignedByte);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#unsignedShort", XSDDatatype.XSDunsignedShort);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#unsignedInt", XSDDatatype.XSDunsignedInt);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#unsignedLong", XSDDatatype.XSDunsignedLong);

		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#decimal", XSDDatatype.XSDdecimal);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#integer", XSDDatatype.XSDinteger);

		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#nonPositiveInteger", XSDDatatype.XSDnonPositiveInteger);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#nonNegativeInteger", XSDDatatype.XSDnonNegativeInteger);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#positiveInteger", XSDDatatype.XSDpositiveInteger);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#negativeInteger", XSDDatatype.XSDnegativeInteger);

		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#normalizedString", XSDDatatype.XSDnormalizedString);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#anyURI", XSDDatatype.XSDanyURI);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#token", XSDDatatype.XSDtoken);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#Name", XSDDatatype.XSDName);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#QName", XSDDatatype.XSDQName);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#language", XSDDatatype.XSDlanguage);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#NMTOKEN", XSDDatatype.XSDNMTOKEN);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#ENTITY", XSDDatatype.XSDENTITY);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#ID", XSDDatatype.XSDID);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#NCName", XSDDatatype.XSDNCName);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#IDREF", XSDDatatype.XSDIDREF);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#NOTATION", XSDDatatype.XSDNOTATION);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#hexBinary", XSDDatatype.XSDhexBinary);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#base64Binary", XSDDatatype.XSDbase64Binary);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#date", XSDDatatype.XSDdate);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#time", XSDDatatype.XSDtime);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#dateTime", XSDDatatype.XSDdateTime);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#dateTimeStamp", XSDDatatype.XSDdateTimeStamp);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#duration", XSDDatatype.XSDduration);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#yearMonthDuration", XSDDatatype.XSDyearMonthDuration);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#dayTimeDuration", XSDDatatype.XSDdayTimeDuration);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#gDay", XSDDatatype.XSDgDay);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#gMonth", XSDDatatype.XSDgMonth);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#gYear", XSDDatatype.XSDgYear);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#gYearMonth", XSDDatatype.XSDgYearMonth);
		mapUriToRDFType.put("http://www.w3.org/2001/XMLSchema#gMonthDay", XSDDatatype.XSDgMonthDay);
	}

	private  void mapToRanDdataType() {
		// dtMap.put("http://www.w3.org/2001/XMLSchema#",
		// RAND_DATATYPE.NOT_SUPPORTED);
		
		dtMap.put("http://www.w3.org/2001/XMLSchema#float", RAND_DATATYPE.FLOAT);
		dtMap.put("http://www.w3.org/2001/XMLSchema#double", RAND_DATATYPE.DOUBLE);
		dtMap.put("http://www.w3.org/2001/XMLSchema#int", RAND_DATATYPE.INT);
		dtMap.put("http://www.w3.org/2001/XMLSchema#long", RAND_DATATYPE.LONG);
		dtMap.put("http://www.w3.org/2001/XMLSchema#short", RAND_DATATYPE.NOT_SUPPORTED_XSD);
		dtMap.put("http://www.w3.org/2001/XMLSchema#byte", RAND_DATATYPE.NOT_SUPPORTED_XSD);
		dtMap.put("http://www.w3.org/2001/XMLSchema#boolean", RAND_DATATYPE.BOOLEAN);
		dtMap.put("http://www.w3.org/2001/XMLSchema#string", RAND_DATATYPE.STRING);

		dtMap.put("http://www.w3.org/2001/XMLSchema#unsignedByte", RAND_DATATYPE.NOT_SUPPORTED_XSD);
		dtMap.put("http://www.w3.org/2001/XMLSchema#unsignedShort", RAND_DATATYPE.NOT_SUPPORTED_XSD);
		dtMap.put("http://www.w3.org/2001/XMLSchema#unsignedInt", RAND_DATATYPE.UNSIGNED_INT);
		dtMap.put("http://www.w3.org/2001/XMLSchema#unsignedLong", RAND_DATATYPE.NOT_SUPPORTED_XSD);

		dtMap.put("http://www.w3.org/2001/XMLSchema#decimal", RAND_DATATYPE.DECIMAL);
		dtMap.put("http://www.w3.org/2001/XMLSchema#integer", RAND_DATATYPE.INTEGER);

		dtMap.put("http://www.w3.org/2001/XMLSchema#nonPositiveInteger", RAND_DATATYPE.NON_POSITIVE_INTEGER);
		dtMap.put("http://www.w3.org/2001/XMLSchema#nonNegativeInteger", RAND_DATATYPE.NON_NEGATIVE_INTEGER);
		dtMap.put("http://www.w3.org/2001/XMLSchema#positiveInteger", RAND_DATATYPE.POSITIVE_INTEGER);
		dtMap.put("http://www.w3.org/2001/XMLSchema#negativeInteger", RAND_DATATYPE.NEGATIVE_INTEGER);

		dtMap.put("http://www.w3.org/2001/XMLSchema#normalizedString", RAND_DATATYPE.NOT_SUPPORTED_XSD);
		dtMap.put("http://www.w3.org/2001/XMLSchema#anyURI", RAND_DATATYPE.ANYURI);
		dtMap.put("http://www.w3.org/2001/XMLSchema#token", RAND_DATATYPE.NOT_SUPPORTED_XSD);
		dtMap.put("http://www.w3.org/2001/XMLSchema#Name", RAND_DATATYPE.NOT_SUPPORTED_XSD);
		dtMap.put("http://www.w3.org/2001/XMLSchema#QName", RAND_DATATYPE.NOT_SUPPORTED_XSD);
		dtMap.put("http://www.w3.org/2001/XMLSchema#language", RAND_DATATYPE.NOT_SUPPORTED_XSD);
		dtMap.put("http://www.w3.org/2001/XMLSchema#NMTOKEN", RAND_DATATYPE.NOT_SUPPORTED_XSD);
		dtMap.put("http://www.w3.org/2001/XMLSchema#ENTITY", RAND_DATATYPE.NOT_SUPPORTED_XSD);
		dtMap.put("http://www.w3.org/2001/XMLSchema#ID", RAND_DATATYPE.NOT_SUPPORTED_XSD);
		dtMap.put("http://www.w3.org/2001/XMLSchema#NCName", RAND_DATATYPE.NOT_SUPPORTED_XSD);
		dtMap.put("http://www.w3.org/2001/XMLSchema#IDREF", RAND_DATATYPE.NOT_SUPPORTED_XSD);
		dtMap.put("http://www.w3.org/2001/XMLSchema#NOTATION", RAND_DATATYPE.NOT_SUPPORTED_XSD);
		dtMap.put("http://www.w3.org/2001/XMLSchema#hexBinary", RAND_DATATYPE.NOT_SUPPORTED_XSD);
		dtMap.put("http://www.w3.org/2001/XMLSchema#base64Binary", RAND_DATATYPE.NOT_SUPPORTED_XSD);
		dtMap.put("http://www.w3.org/2001/XMLSchema#date", RAND_DATATYPE.DATE);
		dtMap.put("http://www.w3.org/2001/XMLSchema#time", RAND_DATATYPE.TIME);
		dtMap.put("http://www.w3.org/2001/XMLSchema#dateTime", RAND_DATATYPE.DATETIME);
		dtMap.put("http://www.w3.org/2001/XMLSchema#dateTimeStamp", RAND_DATATYPE.NOT_SUPPORTED_XSD);
		dtMap.put("http://www.w3.org/2001/XMLSchema#duration", RAND_DATATYPE.NOT_SUPPORTED_XSD);
		dtMap.put("http://www.w3.org/2001/XMLSchema#yearMonthDuration", RAND_DATATYPE.NOT_SUPPORTED_XSD);
		dtMap.put("http://www.w3.org/2001/XMLSchema#dayTimeDuration", RAND_DATATYPE.NOT_SUPPORTED_XSD);
		dtMap.put("http://www.w3.org/2001/XMLSchema#gDay", RAND_DATATYPE.NOT_SUPPORTED_XSD);
		dtMap.put("http://www.w3.org/2001/XMLSchema#gMonth", RAND_DATATYPE.NOT_SUPPORTED_XSD);
		dtMap.put("http://www.w3.org/2001/XMLSchema#gYear", RAND_DATATYPE.GYEAR);
		dtMap.put("http://www.w3.org/2001/XMLSchema#gYearMonth", RAND_DATATYPE.NOT_SUPPORTED_XSD);
		dtMap.put("http://www.w3.org/2001/XMLSchema#gMonthDay", RAND_DATATYPE.NOT_SUPPORTED_XSD);
	}

	protected void getAllXSDURis() {
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
				logger.trace(uri);
				logger.trace("Added:" + uri);
			} catch (Exception e) {
				logger.trace("Skipping. Can't get XSD uri for " + field.getName());
			}
		}
	}

	public  boolean isSupportedDataType(String uri) {
		RAND_DATATYPE dt ;
		try {
			dt=getDataType(uri);
		} catch (Exception e) {
			return false;
		}
		if (dt == RAND_DATATYPE.NOT_SUPPORTED_XSD){
		return false;
		}
		return true;
	}

	public static RAND_DATATYPE getDataType(String uri) {
		if (!xsduriSet.contains(uri)) {
			throw new UnsupportedOperationException(" Not valid XSD DataTypes");
		}
		if (dtMap.containsKey(uri)) {
			return dtMap.get(uri);
		} else {
			throw new UnsupportedOperationException(" Custom DataTypes not supported");
		}
	}

	public static void main(String[] args) {
		DataTypes dt = new DataTypes();
		dt.getAllXSDURis();

	}
}
