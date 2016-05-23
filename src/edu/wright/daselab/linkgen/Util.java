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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Util {
	/**
	 * sort a map by value
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, final boolean isDescending) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				if (isDescending) {
					return (o2.getValue()).compareTo(o1.getValue());
				} else {
					return (o1.getValue()).compareTo(o2.getValue());
				}
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	public static Random random() {
		final Random rand = new Random(500);
		final ThreadLocal<Random> rng = new ThreadLocal<Random>();
		Random ret = rng.get();
		if (ret == null) {
			ret = new Random(rand.nextLong());
			rng.set(ret);
		}
		return ret;
	}
	
	public static long getUnsignedInt(int x) {
	    return x & 0x00000000ffffffffL;
	}

	public static int getUnsignedShort(short x){
		return  x & 0xffff;
	}
	
	public static long computeCumulativeFrequency(HashMap<String, Integer> map) {
		long count = 0;
		for (String key : map.keySet()) {
			count = count + map.get(key);
		}
		return count;
	}

}
