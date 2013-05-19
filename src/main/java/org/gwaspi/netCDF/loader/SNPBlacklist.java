package org.gwaspi.netCDF.loader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Duplicate SNPs to be removed
 */
public class SNPBlacklist {

	private final List<String> affyIDBlackList;
	private final List<String> rsIDBlackList;

	public SNPBlacklist() {

		List<String> tmpAffyIDBlackList = new ArrayList<String>();
		tmpAffyIDBlackList.add("SNP_A-8523508"); //  => rs7528356
		tmpAffyIDBlackList.add("SNP_A-8387337"); //  => rs1462062
		tmpAffyIDBlackList.add("SNP_A-8419535"); //  => rs7592868
		tmpAffyIDBlackList.add("SNP_A-2144818"); //  => rs12635398
		tmpAffyIDBlackList.add("SNP_A-8430502"); //  => rs6848327
		tmpAffyIDBlackList.add("SNP_A-8544027"); //  => rs1992707
		tmpAffyIDBlackList.add("SNP_A-8499914"); //  => rs16895710
		tmpAffyIDBlackList.add("SNP_A-8515633"); //  => rs17061987
		tmpAffyIDBlackList.add("SNP_A-8655683"); //  => rs876888
		tmpAffyIDBlackList.add("SNP_A-8470383"); //  => rs2394832
		tmpAffyIDBlackList.add("SNP_A-8434242"); //  => rs10850459
		tmpAffyIDBlackList.add("SNP_A-8436658"); //  => rs2359181
		tmpAffyIDBlackList.add("SNP_A-8497683"); //  => rs4028931
		tmpAffyIDBlackList.add("SNP_A-8628813");
		tmpAffyIDBlackList.add("SNP_A-8713315");
		affyIDBlackList = Collections.unmodifiableList(tmpAffyIDBlackList);

		List<String> tmpRsIDBlackList = new ArrayList<String>();
		tmpRsIDBlackList.add("rs7528356"); //  => SNP_A-8523508
		tmpRsIDBlackList.add("rs1462062"); //  => SNP_A-8387337
		tmpRsIDBlackList.add("rs7592868"); //  => SNP_A-8419535
		tmpRsIDBlackList.add("rs12635398"); //  => SNP_A-2144818
		tmpRsIDBlackList.add("rs6848327"); //  => SNP_A-8430502
		tmpRsIDBlackList.add("rs1992707"); //  => SNP_A-8544027
		tmpRsIDBlackList.add("rs16895710"); //  => SNP_A-8499914
		tmpRsIDBlackList.add("rs17061987"); //  => SNP_A-8515633
		tmpRsIDBlackList.add("rs876888"); //  => SNP_A-8655683
		tmpRsIDBlackList.add("rs2394832"); //  => SNP_A-8470383
		tmpRsIDBlackList.add("rs10850459"); //  => SNP_A-8434242
		tmpRsIDBlackList.add("rs2359181"); //  => SNP_A-8436658
		tmpRsIDBlackList.add("rs4028931"); //  => SNP_A-8497683
		rsIDBlackList = Collections.unmodifiableList(tmpRsIDBlackList);
	}

	public List<String> getAffyIdBlacklist() {
		return affyIDBlackList;
	}

	public List<String> getRsIdBlacklist() {
		return rsIDBlackList;
	}
}
