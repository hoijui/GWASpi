package org.gwaspi.netCDF.operations;


import org.gwaspi.model.SampleInfo.Sex;

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

	static CensusDecision getDecisionByChrAndSex(final String chr, final Sex sex) {

		CensusDecision decision = CensusDecision.CountAutosomally;

		if (chr.equals("X") && (sex == Sex.MALE)) {
			// Do not count to census when Chromosome is X and Sex is male
			decision = CensusDecision.CountMalesNonAutosomally;
		} else if (chr.equals("Y") && (sex == Sex.FEMALE)) {
			// Do not count to census when Chromosome is Y and Sex is female
			decision = CensusDecision.CountFemalesNonAutosomally;
		}

		return decision;
	}
}
