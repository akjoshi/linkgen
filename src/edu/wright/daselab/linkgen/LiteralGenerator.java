package edu.wright.daselab.linkgen;

import org.apache.jena.rdf.model.Resource;

import edu.wright.daselab.linkgen.DataTypes.RAND_DATATYPE;

public class LiteralGenerator {
	DataTypes dt = new DataTypes();

	public LiteralGenerator() {
	}

	protected void run() {
		//this needs to run after InstanceGenerator
		generateRandomValuesForDataTypes();
	}

	private void generateRandomValuesForDataTypes() {
		RAND_DATATYPE rdt;
		// always store ANYURI - this is required if the property has noRange.
		// simply create randomUri.
		SharedDataHolder.rdh.storeRandomData(RAND_DATATYPE.ANYURI);
		for (Resource resource : SharedDataHolder.rdfTypeSet) {
			if (dt.isSupportedDataType(resource.toString())) {
				rdt = DataTypes.getDataType(resource.toString());
			} else {
				rdt = RAND_DATATYPE.INT;
			}
			SharedDataHolder.rdh.storeRandomData(rdt);
		}
		// SharedDataHolder.rdh.readAllRandomData();
	}

}
