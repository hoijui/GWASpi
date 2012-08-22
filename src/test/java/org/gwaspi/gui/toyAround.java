package org.gwaspi.gui;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 *
 * @author u56124
 */
public class toyAround {

	protected static String mumbo = "This is  a,test	stringg";

	public static void main(String[] args) {
		LinkedHashMap lHashMap = new LinkedHashMap();

		lHashMap.put("1", "One");
		lHashMap.put("2", "Two");
		lHashMap.put("3", "Three");

		Object obj = lHashMap.remove("2");

		for (Iterator it = lHashMap.keySet().iterator(); it.hasNext();) {
			Object key = it.next();
			Object value = lHashMap.get(key);
			System.out.println(key + " : " + value);
		}
	}
}
