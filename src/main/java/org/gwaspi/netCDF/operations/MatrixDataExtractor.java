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

package org.gwaspi.netCDF.operations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.SetMarkerPickCase;
import org.gwaspi.constants.cNetCDF.Defaults.SetSamplePickCase;
import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.markers.MarkerSet;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.gwaspi.samples.SampleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayChar;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;

public class MatrixDataExtractor {

	private final Logger log = LoggerFactory.getLogger(MatrixDataExtractor.class);

	private int studyId;
	private int rdMatrixId;
	private String wrMatrixFriendlyName;
	private String wrMatrixDescription;
	private File markerCriteriaFile;
	private File sampleCriteriaFile;
	private SetMarkerPickCase markerPickCase;
	private String markerPickerVar;
	private StringBuilder markerPickerCriteria;
	private SetSamplePickCase samplePickCase;
	private String samplePickerVar;
	private StringBuilder samplePickerCriteria;
	private MatrixMetadata rdMatrixMetadata;
	private MarkerSet rdMarkerSet;
	private SampleSet rdSampleSet;
	private Map<MarkerKey, byte[]> wrMarkerIdSetMap;
	private Map<SampleKey, char[]> rdSampleSetMap;
	private Map<SampleKey, Integer> wrSampleSetMap;
	private Map<MarkerKey, int[]> rdChrInfoSetMap;

	/**
	 * This constructor to extract data from Matrix a by passing a variable and
	 * the criteria to filter items by.
	 *
	 * @param studyId
	 * @param rdMatrixId
	 * @param wrMatrixFriendlyName
	 * @param wrMatrixDescription
	 * @param markerPickCase
	 * @param samplePickCase
	 * @param markerPickerVar
	 * @param samplePickerVar
	 * @param markerCriteria
	 * @param sampleCriteria
	 * @param sampleFilterPos
	 * @param markerPickerFile
	 * @param samplePickerFile
	 * @throws IOException
	 * @throws InvalidRangeException
	 */
	public MatrixDataExtractor(
			int studyId,
			int rdMatrixId,
			String wrMatrixFriendlyName,
			String wrMatrixDescription,
			SetMarkerPickCase markerPickCase,
			SetSamplePickCase samplePickCase,
			String markerPickerVar,
			String samplePickerVar,
			Set<?> markerCriteria,
			Set<?> sampleCriteria,
			int sampleFilterPos,
			File markerPickerFile,
			File samplePickerFile)
			throws IOException, InvalidRangeException
	{
		// INIT EXTRACTOR OBJECTS
		this.markerPickCase = markerPickCase;
		this.markerPickerVar = markerPickerVar;
		this.samplePickCase = samplePickCase;
		this.samplePickerVar = samplePickerVar;
		this.markerCriteriaFile = markerPickerFile;
		this.sampleCriteriaFile = samplePickerFile;

		this.rdMatrixId = rdMatrixId;
		this.rdMatrixMetadata = MatricesList.getMatrixMetadataById(this.rdMatrixId);
		this.studyId = rdMatrixMetadata.getStudyId();
		this.wrMatrixFriendlyName = wrMatrixFriendlyName;
		this.wrMatrixDescription = wrMatrixDescription;

		this.rdMarkerSet = new MarkerSet(this.rdMatrixMetadata.getStudyId(), this.rdMatrixId);
		this.rdMarkerSet.initFullMarkerIdSetMap();

		this.rdSampleSet = new SampleSet(this.rdMatrixMetadata.getStudyId(), this.rdMatrixId);
		this.rdSampleSetMap = this.rdSampleSet.getSampleIdSetMapCharArray();

		//<editor-fold defaultstate="expanded" desc="MARKERSET PICKING">
		this.markerPickerCriteria = new StringBuilder();
		for (Object value : markerCriteria) {
			this.markerPickerCriteria.append(value.toString());
			this.markerPickerCriteria.append(",");
		}

		// Pick markerId by criteria file
		if (!markerPickerFile.toString().isEmpty() && markerPickerFile.isFile()) {
			FileReader fr = new FileReader(markerPickerFile);
			BufferedReader br = new BufferedReader(fr);
			String l;
			markerCriteria.clear();
			while ((l = br.readLine()) != null) {
				if ((markerPickCase == SetMarkerPickCase.MARKERS_INCLUDE_BY_ID)
						|| (markerPickCase == SetMarkerPickCase.MARKERS_EXCLUDE_BY_ID))
				{
					((Set<MarkerKey>) markerCriteria).add(MarkerKey.valueOf(l));
				} else {
					((Set<char[]>) markerCriteria).add(l.toCharArray());
				}
				this.markerPickerCriteria.append(l);
				this.markerPickerCriteria.append(",");
			}
			br.close();
		}

		Collection<MarkerKey> wrMarkerKeys;
		wrMarkerIdSetMap = new LinkedHashMap<MarkerKey, byte[]>();
		this.wrMarkerIdSetMap = new LinkedHashMap<MarkerKey, byte[]>();
		switch (markerPickCase) {
			case ALL_MARKERS:
				// Get all markers
				wrMarkerKeys = this.rdMarkerSet.getMarkerKeys();
				break;
			case MARKERS_INCLUDE_BY_NETCDF_CRITERIA:
				// Pick by netCDF field value and criteria
				wrMarkerKeys = this.rdMarkerSet.pickValidMarkerSetItemsByValue(markerPickerVar, (Set<byte[]>) markerCriteria, true).keySet();
				break;
			case MARKERS_EXCLUDE_BY_NETCDF_CRITERIA:
				// Exclude by netCDF field value and criteria
				wrMarkerKeys = this.rdMarkerSet.pickValidMarkerSetItemsByValue(markerPickerVar, (Set<byte[]>) markerCriteria, false).keySet();
				break;
			case MARKERS_INCLUDE_BY_ID:
				wrMarkerKeys = this.rdMarkerSet.pickValidMarkerSetItemsByKey((Set<MarkerKey>) markerCriteria, true).keySet();
				break;
			case MARKERS_EXCLUDE_BY_ID:
				wrMarkerKeys = this.rdMarkerSet.pickValidMarkerSetItemsByKey((Set<MarkerKey>) markerCriteria, false).keySet();
				break;
			default:
				// Get all markers
				wrMarkerKeys = this.rdMarkerSet.getMarkerKeys();
		}
		this.wrMarkerIdSetMap = AbstractOperationSet.fillMapWithKeyAndDefaultValue(wrMarkerKeys, cNetCDF.Defaults.DEFAULT_GT);

		// RETRIEVE CHROMOSOMES INFO
		this.rdMarkerSet.fillMarkerSetMapWithChrAndPos();
		Map<MarkerKey, MarkerMetadata> sortedChrAndPos = org.gwaspi.global.Utils.createOrderedMap(this.wrMarkerIdSetMap, this.rdMarkerSet.getMarkerMetadata());
		this.rdChrInfoSetMap = org.gwaspi.netCDF.matrices.Utils.aggregateChromosomeInfo(sortedChrAndPos, 0, 1);
		//</editor-fold>

		//<editor-fold defaultstate="expanded" desc="SAMPLESET PICKING">
		this.samplePickerCriteria = new StringBuilder();
		for (Object value : sampleCriteria) {
			this.samplePickerCriteria.append(value.toString());
			this.samplePickerCriteria.append(",");
		}

		// USE cNetCDF Key and criteria or list file
		if (!samplePickerFile.toString().isEmpty() && samplePickerFile.isFile()) {
			FileReader fr = new FileReader(samplePickerFile);
			BufferedReader br = new BufferedReader(fr);
			String l;
			sampleCriteria.clear();
			while ((l = br.readLine()) != null) {
				if ((samplePickCase == SetSamplePickCase.SAMPLES_INCLUDE_BY_ID)
						|| (samplePickCase == SetSamplePickCase.SAMPLES_EXCLUDE_BY_ID))
				{
					((Set<SampleKey>) sampleCriteria).add(SampleKey.valueOf(studyId, l));
				} else {
					((Set<char[]>) sampleCriteria).add(l.toCharArray());
				}
				this.samplePickerCriteria.append(l);
				this.samplePickerCriteria.append(",");
			}
			br.close();
		}

		this.wrSampleSetMap = new LinkedHashMap<SampleKey, Integer>(this.rdSampleSetMap.size());
		switch (samplePickCase) {
			case ALL_SAMPLES:
				// Get all samples
				int i = 0;
				for (SampleKey key : this.rdSampleSetMap.keySet()) {
					this.wrSampleSetMap.put(key, i);
					i++;
				}
				break;
			case SAMPLES_INCLUDE_BY_NETCDF_FILTER:
				// USE cNetCDF Filter Data and criteria
				this.wrSampleSetMap = this.rdSampleSet.pickValidSampleSetItemsByNetCDFFilter(this.rdSampleSetMap, samplePickerVar, sampleFilterPos, (Set<char[]>) sampleCriteria, true);
				break;
			case SAMPLES_EXCLUDE_BY_NETCDF_FILTER:
				// USE cNetCDF Filter Data and criteria
				this.wrSampleSetMap = this.rdSampleSet.pickValidSampleSetItemsByNetCDFFilter(this.rdSampleSetMap, samplePickerVar, sampleFilterPos, (Set<char[]>) sampleCriteria, false);
				break;
			case SAMPLES_INCLUDE_BY_NETCDF_CRITERIA:
				// USE cNetCDF Value and criteria
				this.wrSampleSetMap = this.rdSampleSet.pickValidSampleSetItemsByNetCDFValue(this.rdSampleSetMap, samplePickerVar, (Set<char[]>) sampleCriteria, true);
				break;
			case SAMPLES_EXCLUDE_BY_NETCDF_CRITERIA:
				// USE cNetCDF Value and criteria
				this.wrSampleSetMap = this.rdSampleSet.pickValidSampleSetItemsByNetCDFValue(this.rdSampleSetMap, samplePickerVar, (Set<char[]>) sampleCriteria, false);
				break;
			case SAMPLES_INCLUDE_BY_ID:
				this.wrSampleSetMap = this.rdSampleSet.pickValidSampleSetItemsByNetCDFKey(this.rdSampleSetMap.keySet(), (Set<SampleKey>) sampleCriteria, true);
				break;
			case SAMPLES_EXCLUDE_BY_ID:
				this.wrSampleSetMap = this.rdSampleSet.pickValidSampleSetItemsByNetCDFKey(this.rdSampleSetMap.keySet(), (Set<SampleKey>) sampleCriteria, false);
				break;
			case SAMPLES_INCLUDE_BY_DB_FIELD:
				// USE DB DATA
				this.wrSampleSetMap = this.rdSampleSet.pickValidSampleSetItemsByDBField(studyId, this.rdSampleSetMap.keySet(), samplePickerVar, sampleCriteria, true);
				break;
			case SAMPLES_EXCLUDE_BY_DB_FIELD:
				// USE DB DATA
				this.wrSampleSetMap = this.rdSampleSet.pickValidSampleSetItemsByDBField(studyId, this.rdSampleSetMap.keySet(), samplePickerVar, sampleCriteria, false);
				break;
			default:
				int j = 0;
				for (Map.Entry<SampleKey, Integer> entry : this.wrSampleSetMap.entrySet()) {
					entry.setValue(j);
					j++;
				}
		}
		//</editor-fold>
	}

	public int extractGenotypesToNewMatrix() throws IOException {
		int resultMatrixId = Integer.MIN_VALUE;

		if (wrSampleSetMap.size() > 0 && wrMarkerIdSetMap.size() > 0) {
			try {
				// CREATE netCDF-3 FILE
				StringBuilder descSB = new StringBuilder(Text.Matrix.descriptionHeader1);
				descSB.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
				descSB.append("\nThrough Matrix extraction from parent Matrix MX: ").append(rdMatrixMetadata.getMatrixId()).append(" - ").append(rdMatrixMetadata.getMatrixFriendlyName());

				descSB.append("\nMarker Filter Variable: ");
				String pickPrefix = "All Markers";
				if (markerPickCase.toString().contains("EXCLUDE")) {
					pickPrefix = "Exclude by ";
				} else if (markerPickCase.toString().contains("INCLUDE")) {
					pickPrefix = "Include by ";
				}
				descSB.append(pickPrefix).append(markerPickerVar.replaceAll("_", " ").toUpperCase());
				if (markerCriteriaFile.isFile()) {
					descSB.append("\nMarker Criteria File: ");
					descSB.append(markerCriteriaFile.getPath());
				} else if (!pickPrefix.equals("All Markers")) {
					descSB.append("\nMarker Criteria: ");
					descSB.append(markerPickerCriteria.deleteCharAt(markerPickerCriteria.length() - 1));
				}

				descSB.append("\nSample Filter Variable: ");
				pickPrefix = "All Samples";
				if (samplePickCase.toString().contains("EXCLUDE")) {
					pickPrefix = "Exclude by ";
				} else if (samplePickCase.toString().contains("INCLUDE")) {
					pickPrefix = "Include by ";
				}
				descSB.append(pickPrefix).append(samplePickerVar.replaceAll("_", " ").toUpperCase());
				if (sampleCriteriaFile.isFile()) {
					descSB.append("\nSample Criteria File: ");
					descSB.append(sampleCriteriaFile.getPath());
				} else if (!pickPrefix.equals("All Samples")) {
					descSB.append("\nSample Criteria: ");
					descSB.append(samplePickerCriteria.deleteCharAt(samplePickerCriteria.length() - 1));
				}

				if (!wrMatrixDescription.isEmpty()) {
					descSB.append("\n\nDescription: ");
					descSB.append(wrMatrixDescription);
					descSB.append("\n");
				}
//				descSB.append("\nGenotype encoding: ");
//				descSB.append(rdMatrixMetadata.getGenotypeEncoding());
				descSB.append("\n");
				descSB.append("Markers: ").append(wrMarkerIdSetMap.size()).append(", Samples: ").append(wrSampleSetMap.size());

				MatrixFactory wrMatrixHandler = new MatrixFactory(
						studyId,
						rdMatrixMetadata.getTechnology(), // technology
						wrMatrixFriendlyName,
						descSB.toString(), // description
						rdMatrixMetadata.getGenotypeEncoding(), // Matrix genotype encoding from orig matrix genotype encoding
						rdMatrixMetadata.getStrand(),
						rdMatrixMetadata.getHasDictionray(), // has dictionary?
						wrSampleSetMap.size(),
						wrMarkerIdSetMap.size(),
						rdChrInfoSetMap.size(),
						rdMatrixId, // Orig matrixId 1
						Integer.MIN_VALUE); // Orig matrixId 2

				resultMatrixId = wrMatrixHandler.getResultMatrixId();

				NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());
				NetcdfFileWriteable wrNcFile = wrMatrixHandler.getNetCDFHandler();
				try {
					wrNcFile.create();
				} catch (IOException ex) {
					log.error("Failed creating file: " + wrNcFile.getLocation(), ex);
				}
				//log.trace("Done creating netCDF handle in MatrixataExtractor: " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

				//<editor-fold defaultstate="expanded" desc="METADATA WRITER">
				// WRITING METADATA TO MATRIX

				// SAMPLESET
				ArrayChar.D2 samplesD2 = Utils.writeMapKeysToD2ArrayChar(wrSampleSetMap, cNetCDF.Strides.STRIDE_SAMPLE_NAME);

				int[] sampleOrig = new int[]{0, 0};
				try {
					wrNcFile.write(cNetCDF.Variables.VAR_SAMPLESET, sampleOrig, samplesD2);
				} catch (IOException ex) {
					log.error("Failed writing file", ex);
				} catch (InvalidRangeException ex) {
					log.error(null, ex);
				}
				log.info("Done writing SampleSet to matrix");

				// MARKERSET MARKERID
				ArrayChar.D2 markersD2 = Utils.writeMapKeysToD2ArrayChar(wrMarkerIdSetMap, cNetCDF.Strides.STRIDE_MARKER_NAME);
				int[] markersOrig = new int[]{0, 0};
				try {
					wrNcFile.write(cNetCDF.Variables.VAR_MARKERSET, markersOrig, markersD2);
				} catch (IOException ex) {
					log.error("Failed writing file", ex);
				} catch (InvalidRangeException ex) {
					log.error(null, ex);
				}

				// MARKERSET RSID
				rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
				Map<MarkerKey, char[]> sortedMarkerRSIDs = org.gwaspi.global.Utils.createOrderedMap(wrMarkerIdSetMap, rdMarkerSet.getMarkerIdSetMapCharArray());
				Utils.saveCharMapValueToWrMatrix(wrNcFile, sortedMarkerRSIDs, cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);

				// MARKERSET CHROMOSOME
				rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
				Map<MarkerKey, char[]> sortedMarkerChrs = org.gwaspi.global.Utils.createOrderedMap(wrMarkerIdSetMap, rdMarkerSet.getMarkerIdSetMapCharArray());
				Utils.saveCharMapValueToWrMatrix(wrNcFile, sortedMarkerChrs, cNetCDF.Variables.VAR_MARKERS_CHR, cNetCDF.Strides.STRIDE_CHR);

				// Set of chromosomes found in matrix along with number of markersinfo
				org.gwaspi.netCDF.operations.Utils.saveCharMapKeyToWrMatrix(wrNcFile, rdChrInfoSetMap, cNetCDF.Variables.VAR_CHR_IN_MATRIX, 8);
				// Number of marker per chromosome & max pos for each chromosome
				int[] columns = new int[] {0, 1, 2, 3};
				org.gwaspi.netCDF.operations.Utils.saveIntMapD2ToWrMatrix(wrNcFile, rdChrInfoSetMap, columns, cNetCDF.Variables.VAR_CHR_INFO);

				// MARKERSET POSITION
				rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
				Map<MarkerKey, Integer> sortedMarkerPos = org.gwaspi.global.Utils.createOrderedMap(wrMarkerIdSetMap, rdMarkerSet.getMarkerIdSetMapInteger());
				//Utils.saveCharMapValueToWrMatrix(wrNcFile, wrMarkerIdSetMap, cNetCDF.Variables.VAR_MARKERS_POS, cNetCDF.Strides.STRIDE_POS);
				Utils.saveIntMapD1ToWrMatrix(wrNcFile, sortedMarkerPos, cNetCDF.Variables.VAR_MARKERS_POS);

				// MARKERSET DICTIONARY ALLELES
				rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_BASES_DICT);
				Map<MarkerKey, char[]> sortedMarkerBasesDicts = org.gwaspi.global.Utils.createOrderedMap(wrMarkerIdSetMap, rdMarkerSet.getMarkerIdSetMapCharArray());
				Utils.saveCharMapValueToWrMatrix(wrNcFile, sortedMarkerBasesDicts, cNetCDF.Variables.VAR_MARKERS_BASES_DICT, cNetCDF.Strides.STRIDE_GT);

				// GENOTYPE STRAND
				rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_GT_STRAND);
				Map<MarkerKey, char[]> sortedMarkerGTStrands = org.gwaspi.global.Utils.createOrderedMap(wrMarkerIdSetMap, rdMarkerSet.getMarkerIdSetMapCharArray());
				Utils.saveCharMapValueToWrMatrix(wrNcFile, sortedMarkerGTStrands, cNetCDF.Variables.VAR_GT_STRAND, 3);
				//</editor-fold>

				//<editor-fold defaultstate="expanded" desc="GENOTYPES WRITER">
				// Iterate through wrSampleSetMap, use item position to read correct sample GTs into rdMarkerIdSetMap.
				log.info(Text.All.processing);
				int sampleWrIndex = 0;
				for (Integer rdPos : wrSampleSetMap.values()) {
					// Iterate through wrMarkerIdSetMap, get the correct GT from rdMarkerIdSetMap
					rdMarkerSet.fillGTsForCurrentSampleIntoInitMap(rdPos);
//					rdMarkerSet.fillGTsForCurrentSampleIntoInitMap(sampleWrPos);
					Map<MarkerKey, byte[]> sortedRdPos = org.gwaspi.global.Utils.createOrderedMap(wrMarkerIdSetMap, rdMarkerSet.getMarkerIdSetMapByteArray());

					// Write wrMarkerIdSetMap to A3 ArrayChar and save to wrMatrix
					Utils.saveSingleSampleGTsToMatrix(wrNcFile, sortedRdPos, sampleWrIndex);
					if (sampleWrIndex % 100 == 0) {
						log.info("Samples copied: {}", sampleWrIndex);
					}
					sampleWrIndex++;
				}
				//</editor-fold>

				// CLOSE THE FILE AND BY THIS, MAKE IT READ-ONLY
				try {
					// GENOTYPE ENCODING
					ArrayChar.D2 guessedGTCodeAC = new ArrayChar.D2(1, 8);
					Index index = guessedGTCodeAC.getIndex();
					guessedGTCodeAC.setString(index.set(0, 0), rdMatrixMetadata.getGenotypeEncoding().toString());
					int[] origin = new int[]{0, 0};
					wrNcFile.write(cNetCDF.Variables.GLOB_GTENCODING, origin, guessedGTCodeAC);

					descSB.append("\nGenotype encoding: ");
					descSB.append(rdMatrixMetadata.getGenotypeEncoding());

					MatrixMetadata resultMatrixMetadata = wrMatrixHandler.getResultMatrixMetadata();
					resultMatrixMetadata.setDescription(descSB.toString());
					MatricesList.updateMatrix(resultMatrixMetadata);

					wrNcFile.close();
				} catch (IOException ex) {
					log.error("Failed creating file: " + wrNcFile.getLocation(), ex);
				}

				org.gwaspi.global.Utils.sysoutCompleted("Extraction to new Matrix");
			} catch (InvalidRangeException ex) {
				log.error(null, ex);
			} catch (IOException ex) {
				log.error(null, ex);
			}
		} else {
			Dialogs.showWarningDialogue(Text.Trafo.criteriaReturnsNoResults);
		}

		return resultMatrixId;
	}
}
