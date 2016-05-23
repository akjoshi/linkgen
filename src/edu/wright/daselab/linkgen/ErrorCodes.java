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

public final class ErrorCodes {	
	private static ErrorCodes _instance;

	// make sure that no more than one instance is instantiated.
	public synchronized static ErrorCodes getInstance() {
		if (_instance == null)
			_instance = new ErrorCodes();
		return _instance;
	}

	public enum Error {
			//third party error/exception begins with 9 and prefixed with TP
		    //local error begins with 1
			TP_ONTOLOGY_LOAD_ERROR(9001, "Cannot load ontology"),
			TP_ONTOLOGY_CLASS_READ_ERROR(9002, "Cannot read ontology classes"),
			
			LDG_ONTOLOGY_FILE_NOT_FOUND(1001, "Input ontology file not found"),
			LDG_OUTPUT_FILE_WRITE_ERROR(1002, "Cannot write to ouptut file"),
			LDG_VOID_OUTPUT_FILE_WRITE_ERROR(1003, "Cannot write to VOID ouptut file"),
			LDG_INPUT_SAME_AS_FILE_NOT_FOUND(1004, "Cannot read input SAME_AS file"),
			
			INVALID_COMMAND_LINE_PARAMS(0000, "Check command line parameters"),
			INVALID_CONFIG_PARAMS(1980, "Not all the configuration parameters are valid."),
			CONFIG_FILE_NOT_EXISTS(1981, "A config file does not exist."),
			CONFIG_FILE_EMPTY(1982, "A config file exists but is empty"),
			CONFIG_FILE_LOAD_ERROR(1983, "A config file load error"),
			CONFIG_FILE_INPUT_NT_FILE(1984, "Input path does not exist")
			;

		  private final int code;
		  private final String description;

		  private Error(int code, String description) {
		    this.code = code;
		    this.description = description;
		  }

		  public String getDescription() {
		     return description;
		  }

		  public int getCode() {
		     return code;
		  }

		  @Override
		  public String toString() {
		    return code + ": " + description;
		  }
		}
}
