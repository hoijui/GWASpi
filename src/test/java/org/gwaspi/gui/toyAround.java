package org.gwaspi.gui;

import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author u56124
 */
public class toyAround {

	private final static Logger log = LoggerFactory.getLogger(toyAround.class);

	protected static String mumbo = "This is  a,test	stringg";

	public static void main(String[] args) {
		Map<String, String> lHashMap = new LinkedHashMap<String, String>();

		lHashMap.put("1", "One");
		lHashMap.put("2", "Two");
		lHashMap.put("3", "Three");

		String obj = lHashMap.remove("2");

		for (Map.Entry<String, String> entry : lHashMap.entrySet()) {
			log.info("{} : {}", entry.getKey(), entry.getValue());
		}
	}
}
