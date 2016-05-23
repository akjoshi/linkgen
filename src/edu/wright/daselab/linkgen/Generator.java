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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.configuration.Configuration;

import edu.wright.daselab.linkgen.ErrorCodes.Error;

public class Generator {

	public void usage(Options options) {
		HelpFormatter f = new HelpFormatter();
		f.printHelp("java -jar linkgen.jar Generator", options);
	}

	private void gensynth() {		
//		OntologyStatistics os = new OntologyStatistics();
//		os.run();
		boolean IS_QUAD_FORMAT = ConfigurationParams.MODE_QUAD_FORMAT;
		if (IS_QUAD_FORMAT) {
			QuadGenerator qg = new QuadGenerator();
			qg.run();
		} else {
			TripleGenerator tg = new TripleGenerator();
			tg.run();
		}
	}

	void run(String[] args) {
		CommandLineParser parser = new DefaultParser();
		Options optionList = new Options();
		Option configFile = Option.builder("c").longOpt("config").hasArg().required(false)
				.desc("Config File location. \n Optional - if config file is located in same location as that of jar.")
				.build();
		optionList.addOption(configFile);

		try {
			CommandLine line = parser.parse(optionList, args);
			if (line.hasOption("c")) {
				System.out.println("\t Custom config:" + line.getOptionValue("c"));
				ConfigurationLoader.CONFIG_FILE_LOCATION = line.getOptionValue("c");
			}
			Configuration config = ConfigurationLoader.getInstance().getConfig();
			ConfigurationParams.config = config;
			ConfigurationParams.checkStatusOnLoad();
		} catch (Exception exp) {
			usage(optionList);
			Monitor.safeExit(Error.CONFIG_FILE_LOAD_ERROR);
		}
		gensynth();
		Monitor.displayMessage("Generation Complete");
	}

	public static void main(String[] args) {
		Monitor.start("Starting Generator");
		Generator gen = new Generator();
		gen.run(args);
		Monitor.stop("");
	}
}
