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

package org.gwaspi.constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class cImport {

	private static final Logger log = LoggerFactory.getLogger(cImport.class);

	private cImport() {
	}

	public static enum ImportFormat {
		Affymetrix_GenomeWide6,
		BEAGLE,
		GWASpi,
		HAPMAP,
		HGDP1,
		Illumina_LGEN,
		PLINK,
		PLINK_Binary,
		Sequenom,
		UNKNOWN;

		public static ImportFormat compareTo(String str) {
			try {
				return valueOf(str);
			} catch (Exception ex) {
				return GWASpi; // XXX maybe should be UNKNOWN?
			}
		}
	}

	public static class Annotation {

		public static class Affymetrix_GenomeWide6 {

			/* Standard annotation since GenomeWideSNP_6.na30.annot
			 0 Probe Set ID
			 1 dbSNP RS ID
			 2 Chromosome
			 3 Physical Position
			 4 Strand
			 5 ChrX pseudo-autosomal region 1
			 6 Cytoband
			 7 Flank
			 8 Allele A
			 9 Allele B
			 10 Associated Gene
			 11 Genetic Map
			 12 Microsatellite
			 13 Fragment Enzyme Type Length Start Stop
			 14 Allele Frequencies
			 15 Heterozygous Allele Frequencies
			 16 Number of individuals/Number of chromosomes
			 17 In Hapmap
			 18 Strand Versus dbSNP
			 19 Copy Number Variation
			 20 Probe Count
			 21 ChrX pseudo-autosomal region 2
			 22 In Final List
			 23 Minor Allele
			 24 Minor Allele Frequency
			 25 % GC
			 26 OMIM
			 */
			public static int markerId = 0;
			public static int rsId = 1;
			public static int chr = 2;
			public static int pos = 3;
			public static int strand = 4;
			public static int pseudo_a1 = 5;
			//public static int cytoband=6;
			//public static int flank=7;
			public static int alleleA = 8;
			public static int alleleB = 9;
			//public static int assoc_gene=10;
			//public static int gen_map=11;
			//public static int micro_sat=12;
			//public static int frag_enzyme_type_length_start_stop=13;
			public static int alleles_freq = 14;
			//public static int het_alleles_freq=15;
			//public static int nb_hapmap_indiv_chr=16;
			//public static int in_hapmap=17;
			//public static int strand_vs_dbsnp=18;
			//public static int cnv=19;
			//public static int probe_count=20;
			public static int pseudo_a2 = 21;
			public static int in_final_list = 22;
			//public static int minor_allele=23;
			//public static int minor_allele_freq=24;
			//public static int pcGC=25;
			//public static int OMIM = 26;

			public static void init(String currentAnnotation) {

				int versionStart = currentAnnotation.indexOf(".na");
				int versionEnd = currentAnnotation.indexOf('.', versionStart + 1);
				int annotationVersion = 30; // Assuming newest annotation version
				try { // Find out the real annotation version
					annotationVersion = Integer.parseInt(currentAnnotation.substring(versionStart + 3, versionEnd));
				} catch (Exception ex) {
					log.warn(null, ex);
				}

				if (annotationVersion < 30) {
					// Format for Annotation releases 26, 27, 28
					markerId = 0;
					rsId = 2;
					chr = 3;
					pos = 4;
					strand = 5;
					pseudo_a1 = 6;
					//cytoband=7;
					//flank=8;
					alleleA = 9;
					alleleB = 10;
					//assoc_gene=11;
					//gen_map=12;
					//micro_sat=13;
					//frag_enzyme_type_length_start_stop=14;
					alleles_freq = 15;
					//het_alleles_freq=16;
					//nb_hapmap_indiv_chr=17;
					//in_hapmap=18;
					//strand_vs_dbsnp=19;
					//cnv=20;
					//probe_count=21;
					pseudo_a2 = 22;
					in_final_list = 23;
					//minor_allele=24;
					//minor_allele_freq=25;
					//pcGC=26;
				}
			}
		}

		public static interface GWASpi {

			public static final int sampleId = 1;
			public static final int familyId = 0;
			public static final int fatherId = 2;
			public static final int motherId = 3;
			public static final int sex = 4;
			public static final int affection = 5;
			public static final int category = 6;
			public static final int disease = 7;
			public static final int population = 8;
			public static final int age = 9;
		}

		public static interface Plink_Standard {

			public static final int map_chr = 0;
			public static final int map_markerId = 1;
			public static final int map_gendist = 2;
			public static final int map_pos = 3;
			public static final int ped_familyId = 0;
			public static final int ped_sampleId = 1;
			public static final int ped_fatherId = 2;
			public static final int ped_motherId = 3;
			public static final int ped_sex = 4;
			public static final int ped_affection = 5;
			public static final int ped_genotypes = 6;
			public static final int[] ped_key_parts
					= { ped_familyId, ped_sampleId };
		}

		public static interface Plink_LGEN {

			public static final int map_chr = 0;
			public static final int map_markerId = 1;
			public static final int map_gendist = 2;
			public static final int map_pos = 3;
			public static final int lgen_familyId = 0;
			public static final int lgen_sampleId = 1;
			public static final int lgen_markerId = 2;
			public static final int lgen_allele1_fwd = 3;
			public static final int lgen_allele2_fwd = 4;
		}

		public static interface Plink_Binary {

			public static final int bim_chr = 0;
			public static final int bim_markerId = 1;
			public static final int bim_gendist = 2;
			public static final int bim_pos = 3;
			public static final int bim_allele1 = 4;
			public static final int bim_allele2 = 5;
			public static final int ped_familyId = Plink_Standard.ped_familyId;
			public static final int ped_sampleId = Plink_Standard.ped_sampleId;
			public static final int ped_fatherId = Plink_Standard.ped_fatherId;
			public static final int ped_motherId = Plink_Standard.ped_motherId;
			public static final int ped_sex = Plink_Standard.ped_sex;
			public static final int ped_affection = Plink_Standard.ped_affection;
		}

		public static interface HapmapGT_Standard {

			public static final int rsId = 0;
			public static final int alleles = 1;
			public static final int chr = 2;
			public static final int pos = 3;
			public static final int strand = 4;
			public static final int build = 5;
			public static final int gt_center = 6;
			public static final int qc_code = 10;
		}

		public static interface Beagle_Standard {

			public static final int rsId = 0;
			public static final int pos = 1;
			public static final int allele1 = 2;
			public static final int allele2 = 3;
		}

		public static interface HGDP1_Standard {

			public static final int rsId = 0;
			public static final int chr = 1;
			public static final int pos = 2;
		}

		public static interface Sequenom {

			public static final int sampleId = 0;
			public static final int alleles = 1;
			public static final int markerId = 2;
			public static final int well = 3;
			public static final int qa_desc = 4;
			// Annotation same as PLINK MAP file
			public static final int annot_chr = 0;
			public static final int annot_markerId = 1;
			public static final int annot_pos = 3;
		}
	}

	public static interface SampleInfo {

		public static final int familyId = 0;
		public static final int sampleId = 1;
		public static final int fatherId = 2;
		public static final int motherId = 3;
		public static final int sex = 4;
		public static final int affection = 5;
		public static final int category = 6;
		public static final int disease = 7;
		public static final int population = 8;
		public static final int age = 9;
	}

	public static interface StrandFlags {

		public static final String strandPLS = "+";
		public static final String strandMIN = "-";
		public static final String strandPLSMIN = "+/-";
		public static final String strandFWD = "fwd";
		public static final String strandREV = "rev";
		public static final String strandUNK = "unk";
	}

	public static interface Separators {

		public static final String separators_Spaces_rgxp = "[ +]";
		public static final String separators_CommaSpaceTab_rgxp = "[, \t]+";
		public static final String separators_CommaSpaceTabLf_rgxp = "[\n, \t]+";
		public static final String separators_CommaTab_rgxp = "[,\t]+";
		public static final String separators_SpaceTab_rgxp = "[ \t]+";
		public static final String separators_Tab_rgxp = "[\t]+";
		public static final String ops = " "; // separator used in output file
		public static final String separator_PLINK = " ";
		public static final String separator_BEAGLE = " ";
		public static final String separator_REPORTS = "\t";
	}
}
