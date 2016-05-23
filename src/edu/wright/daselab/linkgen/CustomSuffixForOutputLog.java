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

import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.log4j.FileAppender;

public final class CustomSuffixForOutputLog extends FileAppender {
	@Override
	public void setFile(String fileName) {
		if (fileName.indexOf("%timestamp") >= 0) {
			Date d = new Date();
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSS");
			format = new SimpleDateFormat("yyyyMMdd");
			fileName = fileName.replaceAll("%timestamp", format.format(d));
		}
		super.setFile(fileName);
	}
}
