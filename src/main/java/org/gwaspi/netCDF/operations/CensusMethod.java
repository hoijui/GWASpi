/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gwaspi.netCDF.operations;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class CensusMethod {

	public static enum CensusDecision {

		CountAutosomally, CountMalesNonAutosomally, CountFemalesNonAutosomally;

		public static CensusDecision compareTo(String str) {
			try {
				return valueOf(str);
			} catch (Exception ex) {
				return CountAutosomally;
			}
		}

		static CensusDecision getDecisionByChrAndSex(String chr, String sex) {
			CensusDecision decision = CensusDecision.CountAutosomally;
			if (chr.equals("X")) {
				if (sex.equals("1")) {    //Male
					decision = CensusDecision.CountMalesNonAutosomally;    //Don't count to census when Chromosome is X and Sex is male
				}
			}
			if (chr.equals("Y")) {
				if (sex.equals("2")) {    //Female
					decision = CensusDecision.CountFemalesNonAutosomally;    //Don't count to census when Chromosome is X and Sex is male
				}
			}
			return decision;
		}
	}
}
