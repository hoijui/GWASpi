/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gwaspi.gui.utils;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class LinkEnsemblUrl {

	public static String HOMOSAPIENS_BY_CHRPOS = "http://www.ensembl.org/Homo_sapiens/Location/View?r=";

	public static String getHomoSapiensLink(String chr, int position) {
		String baseUrl = HOMOSAPIENS_BY_CHRPOS;
		Integer startPos = (position - 50000);
		Integer endPos = (position + 50000);
		if (Integer.signum(startPos) == -1) {
			startPos = 0;
		}

		String querry = chr + ":" + startPos + "-" + endPos;

		return (baseUrl + querry);
	}
}
