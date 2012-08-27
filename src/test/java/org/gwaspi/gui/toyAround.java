package org.gwaspi.gui;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author u56124
 */
public class toyAround {

	protected static String mumbo = "This is  a,test	stringg";

	public static void main(String[] args) {
		Map<String, String> lHashMap = new LinkedHashMap<String, String>();

		lHashMap.put("1", "One");
		lHashMap.put("2", "Two");
		lHashMap.put("3", "Three");

		String obj = lHashMap.remove("2");

		for (Map.Entry<String, String> entry : lHashMap.entrySet()) {
			System.out.println(entry.getKey() + " : " + entry.getValue());
		}
	}
}
