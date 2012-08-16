/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gwaspi.netCDF.loader;

import org.gwaspi.constants.cNetCDF;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
class ComparatorChrAutPosAsc_old implements Comparator<String> {

	@Override
	public int compare(String a, String b) {
		String separator = cNetCDF.Defaults.TMP_SEPARATOR;

		int idxA1 = a.indexOf(separator);
		int idxA2 = a.indexOf(separator, idxA1);
		int idxB1 = b.indexOf(separator);
		int idxB2 = b.indexOf(separator, idxB1);

		String chrA = a.substring(0, idxA1);
		String chrB = b.substring(0, idxB1);

		Pattern p = Pattern.compile("[0-9]{1,}");
		Matcher mA = p.matcher(chrA);
		Matcher mB = p.matcher(chrB);

		if (mA.matches() && mB.matches()) {  //Both chr are Int
			int intA = Integer.parseInt(chrA);
			int intB = Integer.parseInt(chrB);
			if (intA == intB) { //same chr, compare positions
				int posA = Integer.parseInt(a.substring(a.lastIndexOf(separator) + 1));
				int posB = Integer.parseInt(b.substring(b.lastIndexOf(separator) + 1));
				return (posA - posB);
			} else { //different chr, compare chr nb
				return (intA - intB);
			}

		} else { //One or both chr are not Int
			if (Integer.signum(chrA.compareTo(chrB)) == 0) { //same chr
				String prfxA = a.substring(0, a.lastIndexOf(separator));
				String prfxB = b.substring(0, b.lastIndexOf(separator));
				if (Integer.signum(prfxA.compareTo(prfxB)) == 0) {   //same pseudo-autosomal status, compare positions
					int posA = Integer.parseInt(a.substring(a.lastIndexOf(separator) + 1));
					int posB = Integer.parseInt(b.substring(b.lastIndexOf(separator) + 1));
					return (posA - posB);
				} else { //different pseudo-autosomal status, compare prefix
					return prfxA.compareTo(prfxB);
				}
			} else { //different chr, compare strings
				return a.compareTo(b);
			}
		}
	}
}
