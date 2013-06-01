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

public class cNetCDF {

	public static class Attributes {

		public static final String GLOB_STUDY = "study";
		public static final String GLOB_TECHNOLOGY = "technology";
		public static final String GLOB_GWASPIDB_VERSION = "gwaspi_db_version";
		public static final String GLOB_DESCRIPTION = "description";
		public static final String GLOB_STRAND = "strand";
		public static final String LENGTH = "length";
		public static final String GLOB_HAS_DICTIONARY = "glob_has_dictionnary";

		private Attributes() {
		}
	}

	public static class Dimensions {

		public static final String DIM_SAMPLESET = "samplesDim";
		public static final String DIM_MARKERSET = "markersDim";
		public static final String DIM_CHRSET = "chrDim";
		public static final String DIM_OPSET = "opSetDim";
		public static final String DIM_IMPLICITSET = "implicitSetDim";
		public static final String DIM_GTSTRIDE = "gtStrideDim";
		public static final String DIM_MARKERSTRIDE = "markerStrideDim";
		public static final String DIM_SAMPLESTRIDE = "sampleStrideDim";
		public static final String DIM_2BOXES = "boxes2Dim";
		public static final String DIM_3BOXES = "boxes3Dim";
		public static final String DIM_4BOXES = "boxes4Dim";
		public static final String DIM_32 = "dim32";
		public static final String DIM_16 = "dim16";
		public static final String DIM_8 = "dim8";
		public static final String DIM_4 = "dim4";
		public static final String DIM_2 = "dim2";
		public static final String DIM_1 = "dim1";

		private Dimensions() {
		}
	}

	public static class Variables {
		// MARKER VARS

		public static final String VAR_MARKERSET = "marker_id";
		public static final String VAR_MARKERS_RSID = "marker_rsId";
		public static final String VAR_MARKERS_CHR = "marker_chromosome";
		public static final String VAR_CHR_IN_MATRIX = "chr_in_matrix";
		public static final String VAR_CHR_INFO = "chr_info";
		public static final String VAR_MARKERS_POS = "marker_position";
		//public static final String VAR_MARKERS_BASES_KNOWN ="marker_bases_known";
		public static final String VAR_MARKERS_BASES_DICT = "marker_bases_dict";
		//public static final String VAR_MARKERS_PLSMIN_BASES ="marker_plusminus_bases";
		//public static final String VAR_MARKERS_PLS_BASES ="marker_plus_bases";
		public static final String VAR_MARKERS_PROP1 = "marker_property_1";
		public static final String VAR_MARKERS_PROP2 = "marker_property_2";
		public static final String VAR_MARKERS_PROP8 = "marker_property_8";
		public static final String VAR_MARKERS_PROP16 = "marker_property_16";
		public static final String VAR_MARKERS_PROP32 = "marker_property_32";
		//SAMPLE VARS
		public static final String VAR_SAMPLESET = "sample_id";
		public static final String VAR_SAMPLES_SEX = "sample_sex";
		public static final String VAR_SAMPLES_AFFECTION = "sample_affection";
		public static final String VAR_SAMPLES_FILTERS = "sample_filters";
		//OPERATION VARS
		public static final String VAR_OPSET = "opset";
		public static final String VAR_IMPLICITSET = "implicitset";
		//GT VARS
		public static final String VAR_GENOTYPES = "genotypes";
		public static final String VAR_GT_STRAND = "genotype_strand";
		public static final String VAR_ALLELES = "alleles";
		public static final String GLOB_GTENCODING = "genotype_encoding";

		private Variables() {
		}
	}

	//<editor-fold defaultstate="expanded" desc="OPERATION & ANALYSIS VARS">
	public static class Census {

		public static final String VAR_OP_MARKERS_CENSUSALL = "OP_markers_censusall";
		public static final String VAR_OP_MARKERS_CENSUSCASE = "OP_markers_censuscase";
		public static final String VAR_OP_MARKERS_CENSUSCTRL = "OP_markers_censusctrl";
		public static final String VAR_OP_MARKERS_CENSUSHW = "OP_markers_censushw";
		//public static final String VAR_OP_MARKERS_KNOWNALLELES ="OP_marker_knownalleles";
		public static final String VAR_OP_MARKERS_MINALLELES = "OP_marker_minalleles";
		public static final String VAR_OP_MARKERS_MINALLELEFRQ = "OP_marker_minallelefrq";
		public static final String VAR_OP_MARKERS_MAJALLELES = "OP_marker_majalleles";
		public static final String VAR_OP_MARKERS_MAJALLELEFRQ = "OP_marker_majallelefrq";
		public static final String VAR_OP_MARKERS_MISSINGRAT = "OP_markers_missingrat";
		public static final String VAR_OP_MARKERS_MISMATCHSTATE = "OP_markers_mismatchstate";
		public static final String VAR_OP_SAMPLES_MISSINGRAT = "OP_sample_missingrat";
		public static final String VAR_OP_SAMPLES_MISSINGCOUNT = "OP_sample_missingcount";
		public static final String VAR_OP_SAMPLES_HETZYRAT = "OP_sample_hetzyrat";

		private Census() {
		}
	}

	public static class HardyWeinberg {

		public static final String VAR_OP_MARKERS_HWPval_ALL = "OP_markers_hwpval_all";
		public static final String VAR_OP_MARKERS_HWPval_CASE = "OP_markers_hwpval_case";
		public static final String VAR_OP_MARKERS_HWPval_CTRL = "OP_markers_hwpval_ctrl";
		public static final String VAR_OP_MARKERS_HWPval_ALT = "OP_markers_hwpval_alt";
		public static final String VAR_OP_MARKERS_HWHETZY_ALL = "OP_markers_hwhetzy_all"; // [OBSERVED, EXPECTED]
		public static final String VAR_OP_MARKERS_HWHETZY_CASE = "OP_markers_hwhetzy_case"; // [OBSERVED, EXPECTED]
		public static final String VAR_OP_MARKERS_HWHETZY_CTRL = "OP_markers_hwhetzy_ctrl"; // [OBSERVED, EXPECTED]
		public static final String VAR_OP_MARKERS_HWHETZY_ALT = "OP_markers_hwhetzy_alt"; // [OBSERVED, EXPECTED]

		private HardyWeinberg() {
		}
	}

	public static class Association {

		public static final String VAR_OP_MARKERS_ASTrendTestTP = "OP_markers_as_cochranarmitageTP";
		public static final String VAR_OP_MARKERS_ASGenotypicAssociationTP2OR = "OP_markers_as_gnotypTP";
		public static final String VAR_OP_MARKERS_ASAllelicAssociationTPOR = "OP_markers_as_allelicTPOR";

		private Association() {
		}
	}
	//</editor-fold>

	public static class Strides {

		public static final int STRIDE_GT = 2;
		public static final int STRIDE_MARKER_NAME = 64;
		public static final int STRIDE_CHR = 8;
		public static final int STRIDE_POS = 32;
		public static final int STRIDE_SAMPLE_NAME = 64;
		public static final int STRIDE_STRAND = 4;

		private Strides() {
		}
	}

	public static class Defaults {

		public static final byte[] DEFAULT_GT = new byte[] {48, 48};
		public static final String TMP_SEPARATOR = ";";
		public static final String DEFAULT_AFFECTION = "Affection";
		public static final String DEFAULT_EXTPHENOTYPE = "External_Phenotype";
		public static final String DEFAULT_HW = "Hardy-Weinberg";
		public static final int DEFAULT_MISMATCH_YES = 1;
		public static final int DEFAULT_MISMATCH_NO = 0;

		public static class AlleleBytes {

			public static final byte A = 65;
			public static final byte C = 67;
			public static final byte G = 71;
			public static final byte T = 84;
			public static final byte B = 66;
			public static final byte _1 = 49;
			public static final byte _2 = 50;
			public static final byte _3 = 51;
			public static final byte _4 = 52;
			public static final byte dash = 45;
			public static final byte _0 = 48;

			private AlleleBytes() {
			}
		}

		public static enum GenotypeEncoding {

			UNKNOWN,
			AB0,
			O12,
			O1234,
			ACGT0;

			public static GenotypeEncoding compareTo(String str) {
				try {
					return valueOf(str);
				} catch (Exception ex) {
					return null;
				}
			}
		}

		public static enum StrandType {

			PLUS,
			MINUS,
			PLSMIN,
			FWD,
			REV,
			FWDREV,
			UNKNOWN;

			public static StrandType compareTo(String str) {
				try {
					return valueOf(str);
				} catch (Exception ex) {
					return null;
				}
			}
		}
		public static final String[] Chromosomes = new String[]{
			"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
			"11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
			"21", "22", "X", "Y", "XY", "MT"};

		public static enum OPType {

			SAMPLE_QA,
			SAMPLE_HTZYPLOT,
			MARKER_QA,
			MARKER_CENSUS_BY_AFFECTION,
			MARKER_CENSUS_BY_PHENOTYPE,
			HARDY_WEINBERG,
			ALLELICTEST,
			GENOTYPICTEST,
			COMBI_ASSOC_TEST,
			TRENDTEST,
			MANHATTANPLOT,
			QQPLOT;

			public static OPType compareTo(String str) {
				try {
					return valueOf(str);
				} catch (Exception ex) {
					return null;
				}
			}
		}

		public static enum SetMarkerPickCase {

			ALL_MARKERS,
			MARKERS_INCLUDE_BY_NETCDF_CRITERIA,
			MARKERS_INCLUDE_BY_ID,
			MARKERS_EXCLUDE_BY_NETCDF_CRITERIA,
			MARKERS_EXCLUDE_BY_ID;

			public static SetMarkerPickCase compareTo(String str) {
				try {
					return valueOf(str);
				} catch (Exception ex) {
					return null;
				}
			}
		}

		public static enum SetSamplePickCase {

			ALL_SAMPLES,
			SAMPLES_INCLUDE_BY_NETCDF_FILTER,
			SAMPLES_INCLUDE_BY_NETCDF_CRITERIA,
			SAMPLES_INCLUDE_BY_ID,
			SAMPLES_INCLUDE_BY_DB_FIELD,
			SAMPLES_EXCLUDE_BY_NETCDF_FILTER,
			SAMPLES_EXCLUDE_BY_NETCDF_CRITERIA,
			SAMPLES_EXCLUDE_BY_ID,
			SAMPLES_EXCLUDE_BY_DB_FIELD;

			public static SetSamplePickCase compareTo(String str) {
				try {
					return valueOf(str);
				} catch (Exception ex) {
					return null;
				}
			}
		}

		private Defaults() {
		}
	}
}
