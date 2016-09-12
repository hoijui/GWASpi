/*
 * Copyright (C) 2016 Universitat Pompeu Fabra
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

package org.gwaspi.global;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class RuntimeAnalyzer {

	private final Logger log = LoggerFactory.getLogger(RuntimeAnalyzer.class);

	private static RuntimeAnalyzer SINGLETON = null;

	private final Marker marker;
	private final Map<String, Long> keyStartTime;
	private String descriptor;
	private boolean discard;

	private RuntimeAnalyzer() {

		this.marker = MarkerFactory.getMarker("runtime-analzation");
		this.keyStartTime = new HashMap<String, Long>();
		this.descriptor = null;
		this.discard = false;
	}

	public static RuntimeAnalyzer getInstance() {
		return getSingleton();
	}

	public static void createSingleton() {

		if (SINGLETON != null) {
			throw new IllegalStateException(RuntimeAnalyzer.class.getSimpleName() + " singleton can be created only once");
		}
		SINGLETON = new RuntimeAnalyzer();
	}

	public static void destroySingleton() {
		SINGLETON = null;
	}

	public static RuntimeAnalyzer getSingleton() {
		return SINGLETON;
	}

	public void setDescriptor(final String descriptor) {
		this.descriptor = descriptor;
	}

	public void setDiscard(final boolean discard) {
		this.discard = discard;
	}

	public void log(final String key, final boolean start) {

		/*if (!discard)*/ {
			final long curTime = System.currentTimeMillis();
			final String extra;
			if (start) {
				keyStartTime.put(key, curTime);
				extra = "";
			} else {
				final long startTime = keyStartTime.remove(key);
				final long runtime = curTime - startTime;
				final String runtimeStr = Utils.toHumanReadableTime(runtime);
				extra = runtime + " = " + runtimeStr;
			}
			if (discard) {
				log.debug(marker, "{} - {} - {} - {} - {}", descriptor, key, start, curTime, extra);
			} else {
				log.info(marker, "{} - {} - {} - {} - {}", descriptor, key, start, curTime, extra);
			}
		}
	}
}
