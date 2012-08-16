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
public class ComparatorChrAutPosMarkerIdAsc implements Comparator<String> {

	@Override
	public int compare(String a, String b) {
		//a & b have this format: "chr;[pseudo-autosomal1;pseudo-autosomal2;]pos;markerId"

		String[] aVals = a.split(cNetCDF.Defaults.TMP_SEPARATOR);
		String[] bVals = b.split(cNetCDF.Defaults.TMP_SEPARATOR);

		String chrA = aVals[0];
		String chrB = bVals[0];

		Pattern p = Pattern.compile("[0-9]{1,}");
		Matcher mA = p.matcher(chrA);
		Matcher mB = p.matcher(chrB);


		//Check if contains pseudo-autosomal info
		if (aVals.length == 5) { //Pseudo-autosomal!  "chr;pseudo-autosomal1;pseudo-autosomal2;pos;markerId"
			//If yes compare chromosome(integer)
			if (mA.matches() && mB.matches()) {  //Both chr are Int
				int intA = Integer.parseInt(chrA);
				int intB = Integer.parseInt(chrB);
				if (intA == intB) { //same chr, compare positions
					//if equal compare psa
					String autosomalA = aVals[1] + aVals[2];
					String autosomalB = bVals[1] + bVals[2];
					if (autosomalA.equals(autosomalB)) {   //same pseudo-autosomal status
						//if equal compare pos
						int posA = Integer.parseInt(aVals[3]);
						int posB = Integer.parseInt(bVals[3]);
						if (posA == posB) { //Same position!
							//if equal compare markerId
							return aVals[4].compareTo(bVals[4]);    //return comparison between markerIds
						} else {
							return (posA - posB);
						}
					} else { //different pseudo-autosomal status, compare
						return autosomalA.compareTo(autosomalB);
					}
				} else { //different chr, compare chr nb
					return (intA - intB);
				}
			} else if (chrA.equals(chrB)) { //Chromosmes are Strings
				//If chromosomes are equal, compare psa
				String autosomalA = aVals[1] + aVals[2];
				String autosomalB = bVals[1] + bVals[2];
				if (autosomalA.equals(autosomalB)) {   //same pseudo-autosomal status
					//if equal compare pos
					int posA = Integer.parseInt(aVals[3]);
					int posB = Integer.parseInt(bVals[3]);
					if (posA == posB) { //Same position!
						//if equal compare markerId
						return aVals[4].compareTo(bVals[4]);    //return comparison between markerIds
					} else {
						return (posA - posB);
					}
				} else { //different pseudo-autosomal status, compare
					return autosomalA.compareTo(autosomalB);
				}
			} else { //different chr
				return a.compareTo(b); //compare strings
			}
		} else {    //Normal autosomal  "chr;pos;markerId"
			//compare chromosome(integer)
			if (mA.matches() && mB.matches()) {  //Both chr are Int
				int intA = Integer.parseInt(chrA);
				int intB = Integer.parseInt(chrB);
				if (intA == intB) { //same chr, compare positions
					//if equal compare pos
					int posA = Integer.parseInt(aVals[1]);
					int posB = Integer.parseInt(bVals[1]);
					if (posA == posB) { //Same position!
						//if equal compare markerId
						return aVals[2].compareTo(bVals[2]);    //return comparison between markerIds
					} else {
						return (posA - posB);
					}
				} else { //different chr, compare chr nb
					return (intA - intB);
				}
			} else if (chrA.equals(chrB)) { //Chromosmes are Strings
				//if equal compare pos
				int posA = Integer.parseInt(aVals[1]);
				int posB = Integer.parseInt(bVals[1]);
				if (posA == posB) { //Same position!
					//if equal compare markerId
					return aVals[2].compareTo(bVals[2]);    //return comparison between markerIds
				} else {
					return (posA - posB);
				}
			} else { //different chr
				return a.compareTo(b); //compare strings
			}
		}



	}
}
