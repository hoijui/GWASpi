package org.gwaspi.netCDF.operations;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public enum CensusDecision {

	CountAutosomally,
	CountMalesNonAutosomally,
	CountFemalesNonAutosomally;

	static CensusDecision getDecisionByChrAndSex(String chr, String sex) {
		CensusDecision decision = CensusDecision.CountAutosomally;
		if (chr.equals("X")) {
			if (sex.equals("1")) { // Male
				// Do not count to census when Chromosome is X and Sex is male
				decision = CensusDecision.CountMalesNonAutosomally;
			}
		}
		if (chr.equals("Y")) {
			if (sex.equals("2")) { // Female
				// Do not count to census when Chromosome is Y and Sex is female
				decision = CensusDecision.CountFemalesNonAutosomally;
			}
		}
		return decision;
	}
}
