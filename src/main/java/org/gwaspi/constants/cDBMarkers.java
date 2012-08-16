package org.gwaspi.constants;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
/**
 * This cDBMarkers has now been deprecated
 *
 * @deprecated
 */
public class cDBMarkers {

	// <editor-fold defaultstate="collapsed" desc="MARKER-SET METADATA">
	///////////////////////////////////////////////////
	////////////////* MARKER-SET METADATA */////////////////
	///////////////////////////////////////////////////
	// This table contains info about Common marker Arrays or Subset Arrays
	public static String T_MARKERSET_METADATA = "MARKERSET_METADATA";
	public static String[] T_CREATE_MARKERSET_METADATA = new String[]{"id INTEGER generated by default as identity",
		"markerset_name VARCHAR(64)",
		"type VARCHAR(32)",
		"platform VARCHAR(32)",
		"probe_nb INTEGER",
		"description LONG VARCHAR",
		"last_update DATE"
	};
	public static final String[] F_INSERT_MARKERSET_METADATA = new String[]{"markerset_name",
		"type",
		"platform",
		"probe_nb",
		"description",
		"last_update"
	};
	private static final String F_CONCAT_INSERT_MARKERSET_METADATA = org.gwaspi.database.Utils.arrayToString(F_INSERT_MARKERSET_METADATA, ",");
	public static final String[] F_SELECT_MARKERSET_METADATA = new String[]{"id",
		"markerset_name",
		"type",
		"platform",
		"probe_nb",
		"description",
		"last_update"
	};
	public static String IE_MARKERSET_METADATA_INIT = "INSERT INTO " + org.gwaspi.constants.cDBGWASpi.SCH_MARKERS + "." + T_MARKERSET_METADATA
			+ " (" + F_CONCAT_INSERT_MARKERSET_METADATA + ") "
			+ "VALUES "
			+ "(%arrayinitSQL%)";
	public static String IE_MARKERSET_METADATA_INIT_MARKER_DATA = "MARKER_DATA,ANNOTATION,ALL,2000000,Annotation of all standard array markers,%lastupdate%";
	public static String IE_MARKERSET_METADATA_INIT_AFFY500K = "CHIP_AFFY500K,CHIP,AFFYMETRIX,500568,GeneChip Human Mapping 500K Array Set,%lastupdate%";
	public static String IE_MARKERSET_METADATA_INIT_AFFY5 = "CHIP_AFFY5,CHIP,AFFYMETRIX,443816,Genome-Wide Human SNP Array 5.0,%lastupdate%";
	public static String IE_MARKERSET_METADATA_INIT_AFFY6 = "CHIP_AFFY6,CHIP,AFFYMETRIX,934968,Genome-Wide Human SNP Array 6.0,%lastupdate%";
	public static String IE_MARKERSET_METADATA_INIT_ILLU1MDUO = "CHIP_ILLU1MDUO,CHIP,ILLUMINA,1199187,InfiniumHD Human1M-Duo Array,%lastupdate%";
	public static String IE_MARKERSET_METADATA_INIT_ILLU660W = "CHIP_ILLU660W,CHIP,ILLUMINA,657366,InfiniumHD Human660W-Quad Array,%lastupdate%";
	public static String IE_MARKERSET_METADATA_INIT_HAPMAP2 = "MARKERSET_HAPMAP2,HAPMAP,MULTIPLE,4099891,Hapmap Phase 2 with 4.099.891 markers,%lastupdate%";
	public static String[] IE_MARKERSETS_METADATA_ROWS = {IE_MARKERSET_METADATA_INIT_MARKER_DATA,
		IE_MARKERSET_METADATA_INIT_AFFY500K,
		IE_MARKERSET_METADATA_INIT_AFFY5,
		IE_MARKERSET_METADATA_INIT_AFFY6,
		IE_MARKERSET_METADATA_INIT_ILLU1MDUO,
		IE_MARKERSET_METADATA_INIT_ILLU660W,
		IE_MARKERSET_METADATA_INIT_HAPMAP2};
	// </editor-fold>
	// <editor-fold defaultstate="collapsed" desc=" DEPRECATED GLOBAL MARKER DATA TABLE">
	public static String DEPRECATED_MARKER_DATA = "MARKER_DATA";
	public static String[] DEPRECATED_CREATE_MARKERS = new String[]{"order_id INTEGER generated by default as identity",
		"marker_id VARCHAR(32) NOT NULL",
		"affy_id VARCHAR(32)",
		"illu_id VARCHAR(32)",
		"ab_id VARCHAR(32)",
		"chromosome VARCHAR(8)",
		"position INTEGER",
		"genetic_dist INTEGER DEFAULT 0",
		"allele_1 Varchar(32)",
		"allele_2 Varchar(32)",
		"strand CHAR(1)",
		"copy_nb INTEGER",
		"gene VARCHAR(255)",
		"build VARCHAR(32)",
		"PRIMARY KEY (standard_id,affy_id,illu_id,ab_id)"
	};
	// </editor-fold>
}
