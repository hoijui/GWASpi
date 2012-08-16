package org.gwaspi.netCDF.loader;

import java.util.ArrayList;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class SNPBlacklist {

	// Duplicate SNPs to be removed
	public ArrayList affyIDBlackList = new ArrayList();
	public ArrayList rsIDBlackList = new ArrayList();

	public SNPBlacklist() {

		affyIDBlackList.add("SNP_A-8523508"); //  => rs7528356
		affyIDBlackList.add("SNP_A-8387337"); //  => rs1462062
		affyIDBlackList.add("SNP_A-8419535"); //  => rs7592868
		affyIDBlackList.add("SNP_A-2144818"); //  => rs12635398
		affyIDBlackList.add("SNP_A-8430502"); //  => rs6848327
		affyIDBlackList.add("SNP_A-8544027"); //  => rs1992707
		affyIDBlackList.add("SNP_A-8499914"); //  => rs16895710
		affyIDBlackList.add("SNP_A-8515633"); //  => rs17061987
		affyIDBlackList.add("SNP_A-8655683"); //  => rs876888
		affyIDBlackList.add("SNP_A-8470383"); //  => rs2394832
		affyIDBlackList.add("SNP_A-8434242"); //  => rs10850459
		affyIDBlackList.add("SNP_A-8436658"); //  => rs2359181
		affyIDBlackList.add("SNP_A-8497683"); //  => rs4028931
		affyIDBlackList.add("SNP_A-8628813");
		affyIDBlackList.add("SNP_A-8713315");

		rsIDBlackList.add("rs7528356"); //  => SNP_A-8523508
		rsIDBlackList.add("rs1462062"); //  => SNP_A-8387337
		rsIDBlackList.add("rs7592868"); //  => SNP_A-8419535
		rsIDBlackList.add("rs12635398"); //  => SNP_A-2144818
		rsIDBlackList.add("rs6848327"); //  => SNP_A-8430502
		rsIDBlackList.add("rs1992707"); //  => SNP_A-8544027
		rsIDBlackList.add("rs16895710"); //  => SNP_A-8499914
		rsIDBlackList.add("rs17061987"); //  => SNP_A-8515633
		rsIDBlackList.add("rs876888"); //  => SNP_A-8655683
		rsIDBlackList.add("rs2394832"); //  => SNP_A-8470383
		rsIDBlackList.add("rs10850459"); //  => SNP_A-8434242
		rsIDBlackList.add("rs2359181"); //  => SNP_A-8436658
		rsIDBlackList.add("rs4028931"); //  => SNP_A-8497683

	}

	public ArrayList getAffyIdBlacklist() {
		return affyIDBlackList;
	}

	public ArrayList getRsIdBlacklist() {
		return rsIDBlackList;
	}
}
