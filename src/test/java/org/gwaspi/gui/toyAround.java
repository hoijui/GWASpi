/*
 * Copyright (C) 2013 Universitat Pompeu Fabra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gwaspi.gui;

import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class toyAround {

	private static final Logger log = LoggerFactory.getLogger(toyAround.class);

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
