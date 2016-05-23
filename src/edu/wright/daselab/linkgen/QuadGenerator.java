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

public class QuadGenerator extends BaseRDFGenerator {
	RDFWriter rw = new RDFWriter();
	private final static String NAMESPACE = ConfigurationParams.NAMESPACE;
	private final static String GRAPH_URI_PREFIX = NAMESPACE + "/graph";
	private final static String NOISE_GRAPH = NAMESPACE + "/noisegraph/";

	private final static String SAMEAS_GRAPH = NAMESPACE + "/sameas/";
	private boolean GEN_SAMEAS = ConfigurationParams.GEN_SAMEAS;
	private boolean GEN_NOISE = ConfigurationParams.GEN_NOISE;
	private static int MAX_THREAD = ConfigurationParams.MAX_THREAD;

	private static int TOTAL_TRIPLES = (int) ConfigurationParams.NUM_DISTINCT_TRIPLES;

	public QuadGenerator() {
		super(true, GRAPH_URI_PREFIX, MAX_THREAD);
	}

	@Override
	public void run() {
		InstanceGenerator ig = new InstanceGenerator();
		ig.run();
		LiteralGenerator dg = new LiteralGenerator();
		dg.run();
		generateTriplesWithInstanceThread();
		if (GEN_SAMEAS) {
			SameAsGenerator sg = new SameAsGenerator(true, SAMEAS_GRAPH);
			sg.write();
		}
		if (GEN_NOISE) {
			NoiseGenerator ng = new NoiseGenerator(true, NOISE_GRAPH);
			ng.write();
		}
		VoidGenerator vg = new VoidGenerator();
		vg.writeVoid(TOTAL_TRIPLES);
	}
}
