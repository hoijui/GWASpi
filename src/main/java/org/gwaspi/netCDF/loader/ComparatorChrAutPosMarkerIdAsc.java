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

package org.gwaspi.netCDF.loader;

import java.io.Serializable;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gwaspi.constants.NetCDFConstants;

public class ComparatorChrAutPosMarkerIdAsc
		implements Comparator<String>, Serializable
{
	private static final long serialVersionUID = 1L;

	private static final Pattern SIMPLE_INTEGER = Pattern.compile("[0-9]{1,}");

	@Override
	public int compare(final String strA, final String strB) {
		// a & b have this format: "chr;[pseudo-autosomal1;pseudo-autosomal2;]pos;markerId"

		final String[] aVals = strA.split(NetCDFConstants.Defaults.TMP_SEPARATOR);
		final String[] bVals = strB.split(NetCDFConstants.Defaults.TMP_SEPARATOR);

		final String chrA = aVals[0];
		final String chrB = bVals[0];

		final Matcher matcherA = SIMPLE_INTEGER.matcher(chrA);
		final Matcher matcherB = SIMPLE_INTEGER.matcher(chrB);

		// Check if contains pseudo-autosomal info
		if (aVals.length == 5) { // Pseudo-autosomal!  "chr;pseudo-autosomal1;pseudo-autosomal2;pos;markerId"
			// If yes compare chromosome(integer)
			if (matcherA.matches() && matcherB.matches()) { // Both chr are Int
				final int intA = Integer.parseInt(chrA);
				final int intB = Integer.parseInt(chrB);
				if (intA == intB) { // same chr, compare positions
					// if equal compare pseudo-autosomal
					final String autosomalA = aVals[1] + aVals[2];
					final String autosomalB = bVals[1] + bVals[2];
					if (autosomalA.equals(autosomalB)) { // same pseudo-autosomal status
						// if equal compare pos
						final int posA = Integer.parseInt(aVals[3]);
						final int posB = Integer.parseInt(bVals[3]);
						if (posA == posB) { // Same position!
							// if equal compare markerId
							return aVals[4].compareTo(bVals[4]); // return comparison between markerIds
						} else {
							return (posA - posB);
						}
					} else { // different pseudo-autosomal status, compare
						return autosomalA.compareTo(autosomalB);
					}
				} else { // different chr, compare chr nb
					return (intA - intB);
				}
			} else if (chrA.equals(chrB)) { // Chromosmes are Strings
				// If chromosomes are equal, compare psa
				final String autosomalA = aVals[1] + aVals[2];
				final String autosomalB = bVals[1] + bVals[2];
				if (autosomalA.equals(autosomalB)) { // same pseudo-autosomal status
					// if equal compare pos
					final int posA = Integer.parseInt(aVals[3]);
					final int posB = Integer.parseInt(bVals[3]);
					if (posA == posB) { // Same position!
						// if equal compare markerId
						return aVals[4].compareTo(bVals[4]); // return comparison between markerIds
					} else {
						return (posA - posB);
					}
				} else { // different pseudo-autosomal status, compare
					return autosomalA.compareTo(autosomalB);
				}
			} else { // different chr
				return strA.compareTo(strB); // compare strings
			}
		} else { // Normal autosomal  "chr;pos;markerId"
			// compare chromosome(integer)
			if (matcherA.matches() && matcherB.matches()) { // Both chr are Int
				final int intA = Integer.parseInt(chrA);
				final int intB = Integer.parseInt(chrB);
				if (intA == intB) { // same chr, compare positions
					// if equal compare pos
					final int posA = Integer.parseInt(aVals[1]);
					final int posB = Integer.parseInt(bVals[1]);
					if (posA == posB) { // Same position!
						// if equal compare markerId
						return aVals[2].compareTo(bVals[2]); // return comparison between markerIds
					} else {
						return (posA - posB);
					}
				} else { // different chr, compare chr nb
					return (intA - intB);
				}
			} else if (chrA.equals(chrB)) { // Chromosmes are Strings
				// if equal compare pos
				final int posA = Integer.parseInt(aVals[1]);
				final int posB = Integer.parseInt(bVals[1]);
				if (posA == posB) { // Same position!
					// if equal compare markerId
					return aVals[2].compareTo(bVals[2]); // return comparison between markerIds
				} else {
					return (posA - posB);
				}
			} else { // different chr
				return strA.compareTo(strB); // compare strings
			}
		}
	}
}
