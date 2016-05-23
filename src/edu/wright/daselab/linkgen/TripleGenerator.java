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

public class TripleGenerator extends BaseRDFGenerator {
	RDFWriter rw = new RDFWriter();
	private boolean GEN_SAMEAS = ConfigurationParams.GEN_SAMEAS;
	private boolean GEN_NOISE = ConfigurationParams.GEN_NOISE;
	private static int MAX_THREAD = ConfigurationParams.MAX_THREAD;
	public TripleGenerator() {
		super(false, MAX_THREAD);
	}

	@Override
	public void run() {
		InstanceGenerator ig = new InstanceGenerator();
		ig.run();
		// independent of thread
		genDefaultTriplesForRangeOnlyClasses();
		LiteralGenerator dg;
		ig = null;
		dg = new LiteralGenerator();
		dg.run();
		generateTriples(0);
		// generateTriples();

		// System.out.println("total triples - " +
		// RDFWriter.NUM_TRIPLES_WROTE_BY_ALL_THREADS);

		if (GEN_SAMEAS) {
			SameAsGenerator sg = new SameAsGenerator(false);
			sg.write();
		}
		if (GEN_NOISE) {
			NoiseGenerator ng = new NoiseGenerator(false);
			ng.write();
		}
		VoidGenerator vg = new VoidGenerator();
		vg.writeVoid(totalTriples);
	}
}
