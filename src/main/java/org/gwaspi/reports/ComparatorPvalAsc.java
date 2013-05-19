package org.gwaspi.reports;

import java.util.Comparator;
import java.util.Map;

class ComparatorPvalAsc implements Comparator<Map.Entry> {

	public int compare(Map.Entry e1, Map.Entry e2) {
		int cf = ((Comparable) e1.getValue()).compareTo(e2.getValue());
		if (cf == 0) {
			cf = ((Comparable) e1.getKey()).compareTo(e2.getKey());
		}
		return cf;
	}
}
