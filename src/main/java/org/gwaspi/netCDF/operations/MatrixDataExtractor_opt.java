package org.gwaspi.netCDF.operations;

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cDBMatrix;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.SetMarkerPickCase;
import org.gwaspi.constants.cNetCDF.Defaults.SetSamplePickCase;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.Dialogs;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.netCDF.markers.MarkerSet_opt;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gwaspi.samples.SampleSet;
import ucar.ma2.ArrayChar;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MatrixDataExtractor_opt {

	private final Logger log = LoggerFactory.getLogger(MatrixDataExtractor_opt.class);

	private int studyId;
	private int rdMatrixId;
	private int wrMatrixId = Integer.MIN_VALUE;
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
	private MatrixMetadata wrMatrixMetadata = null;
	private MarkerSet_opt rdMarkerSet;
	private MarkerSet_opt wrMarkerSet = null;
	private SampleSet rdSampleSet;
	private SampleSet wrSampleSet = null;
	private Map<String, Object> wrMarkerIdSetMap;
	private Map<String, Object> rdSampleSetMap;
	private Map<String, Object> wrSampleSetMap;
	private Map<String, Object> rdChrInfoSetMap;

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
	public MatrixDataExtractor_opt(
			int studyId,
			int rdMatrixId,
			String wrMatrixFriendlyName,
			String wrMatrixDescription,
			SetMarkerPickCase markerPickCase,
			SetSamplePickCase samplePickCase,
			String markerPickerVar,
			String samplePickerVar,
			Set<Object> markerCriteria,
			Set<Object> sampleCriteria,
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

		this.rdMarkerSet = new MarkerSet_opt(this.rdMatrixMetadata.getStudyId(), this.rdMatrixId);
		this.rdMarkerSet.initFullMarkerIdSetMap();

		this.rdSampleSet = new SampleSet(this.rdMatrixMetadata.getStudyId(), this.rdMatrixId);
		this.rdSampleSetMap = this.rdSampleSet.getSampleIdSetMap();

		//<editor-fold defaultstate="collapsed" desc="MARKERSET PICKING">
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
				markerCriteria.add(l);
				this.markerPickerCriteria.append(l);
				this.markerPickerCriteria.append(",");
			}
		}

		this.wrMarkerIdSetMap = new LinkedHashMap<String, Object>();
		switch (markerPickCase) {
			case ALL_MARKERS:
				// Get all markers
				this.wrMarkerIdSetMap.putAll(this.rdMarkerSet.getMarkerIdSetMap());
				MarkerSet_opt.fillWith(wrMarkerIdSetMap, cNetCDF.Defaults.DEFAULT_GT);
				break;
			case MARKERS_INCLUDE_BY_NETCDF_CRITERIA:
				// Pick by netCDF field value and criteria
				this.wrMarkerIdSetMap = this.rdMarkerSet.pickValidMarkerSetItemsByValue(markerPickerVar, markerCriteria, true);
				MarkerSet_opt.fillWith(this.wrMarkerIdSetMap, cNetCDF.Defaults.DEFAULT_GT);
				break;
			case MARKERS_EXCLUDE_BY_NETCDF_CRITERIA:
				// Exclude by netCDF field value and criteria
				this.wrMarkerIdSetMap = this.rdMarkerSet.pickValidMarkerSetItemsByValue(markerPickerVar, markerCriteria, false);
				MarkerSet_opt.fillWith(this.wrMarkerIdSetMap, cNetCDF.Defaults.DEFAULT_GT);
				break;
			case MARKERS_INCLUDE_BY_ID:
				this.wrMarkerIdSetMap = this.rdMarkerSet.pickValidMarkerSetItemsByKey(markerCriteria, true);
				MarkerSet_opt.fillWith(this.wrMarkerIdSetMap, cNetCDF.Defaults.DEFAULT_GT);
				break;
			case MARKERS_EXCLUDE_BY_ID:
				this.wrMarkerIdSetMap = this.rdMarkerSet.pickValidMarkerSetItemsByKey(markerCriteria, false);
				MarkerSet_opt.fillWith(this.wrMarkerIdSetMap, cNetCDF.Defaults.DEFAULT_GT);
				break;
			default:
				// Get all markers
				this.wrMarkerIdSetMap.putAll(this.rdMarkerSet.getMarkerIdSetMap());
				MarkerSet_opt.fillWith(this.wrMarkerIdSetMap, cNetCDF.Defaults.DEFAULT_GT);
		}

		// RETRIEVE CHROMOSOMES INFO
		this.rdMarkerSet.fillMarkerSetMapWithChrAndPos();
		MarkerSet_opt.replaceWithValuesFrom(this.wrMarkerIdSetMap, this.rdMarkerSet.getMarkerIdSetMap());
		this.rdChrInfoSetMap = org.gwaspi.netCDF.matrices.Utils.aggregateChromosomeInfo(this.wrMarkerIdSetMap, 0, 1);
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="SAMPLESET PICKING">
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
				sampleCriteria.add(l);
				this.samplePickerCriteria.append(l);
				this.samplePickerCriteria.append(",");
			}
		}

		this.wrSampleSetMap = new LinkedHashMap<String, Object>();
		switch (samplePickCase) {
			case ALL_SAMPLES:
				// Get all samples
				this.wrSampleSetMap.putAll(this.rdSampleSetMap);
				int i = 0;
				for (Map.Entry<String, Object> entry : this.wrSampleSetMap.entrySet()) {
					entry.setValue(i);
					i++;
				}
				break;
			case SAMPLES_INCLUDE_BY_NETCDF_FILTER:
				// USE cNetCDF Filter Data and criteria
				this.wrSampleSetMap = this.rdSampleSet.pickValidSampleSetItemsByNetCDFFilter(this.rdSampleSetMap, samplePickerVar, sampleFilterPos, sampleCriteria, true);
				break;
			case SAMPLES_EXCLUDE_BY_NETCDF_FILTER:
				// USE cNetCDF Filter Data and criteria
				this.wrSampleSetMap = this.rdSampleSet.pickValidSampleSetItemsByNetCDFFilter(this.rdSampleSetMap, samplePickerVar, sampleFilterPos, sampleCriteria, false);
				break;
			case SAMPLES_INCLUDE_BY_NETCDF_CRITERIA:
				// USE cNetCDF Value and criteria
				this.wrSampleSetMap = this.rdSampleSet.pickValidSampleSetItemsByNetCDFValue(this.rdSampleSetMap, samplePickerVar, sampleCriteria, true);
				break;
			case SAMPLES_EXCLUDE_BY_NETCDF_CRITERIA:
				// USE cNetCDF Value and criteria
				this.wrSampleSetMap = this.rdSampleSet.pickValidSampleSetItemsByNetCDFValue(this.rdSampleSetMap, samplePickerVar, sampleCriteria, false);
				break;
			case SAMPLES_INCLUDE_BY_ID:
				this.wrSampleSetMap = this.rdSampleSet.pickValidSampleSetItemsByNetCDFKey(this.rdSampleSetMap, sampleCriteria, true);
				break;
			case SAMPLES_EXCLUDE_BY_ID:
				this.wrSampleSetMap = this.rdSampleSet.pickValidSampleSetItemsByNetCDFKey(this.rdSampleSetMap, sampleCriteria, false);
				break;
			case SAMPLES_INCLUDE_BY_DB_FIELD:
				// USE DB DATA
				this.wrSampleSetMap = this.rdSampleSet.pickValidSampleSetItemsByDBField(studyId, this.rdSampleSetMap, samplePickerVar, sampleCriteria, true);
				break;
			case SAMPLES_EXCLUDE_BY_DB_FIELD:
				// USE DB DATA
				this.wrSampleSetMap = this.rdSampleSet.pickValidSampleSetItemsByDBField(studyId, this.rdSampleSetMap, samplePickerVar, sampleCriteria, false);
				break;
			default:
				int j = 0;
				for (Map.Entry<String, Object> entry : this.wrSampleSetMap.entrySet()) {
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

				//<editor-fold defaultstate="collapsed" desc="METADATA WRITER">
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
				log.info("Done writing SampleSet to matrix"); // FIXME log system already adds timestamp


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
				MarkerSet_opt.replaceWithValuesFrom(wrMarkerIdSetMap, rdMarkerSet.getMarkerIdSetMap());
				Utils.saveCharMapValueToWrMatrix(wrNcFile, wrMarkerIdSetMap, cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);

				// MARKERSET CHROMOSOME
				rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
				MarkerSet_opt.replaceWithValuesFrom(wrMarkerIdSetMap, rdMarkerSet.getMarkerIdSetMap());
				Utils.saveCharMapValueToWrMatrix(wrNcFile, wrMarkerIdSetMap, cNetCDF.Variables.VAR_MARKERS_CHR, cNetCDF.Strides.STRIDE_CHR);

				// Set of chromosomes found in matrix along with number of markersinfo
				org.gwaspi.netCDF.operations.Utils.saveCharMapKeyToWrMatrix(wrNcFile, rdChrInfoSetMap, cNetCDF.Variables.VAR_CHR_IN_MATRIX, 8);
				// Number of marker per chromosome & max pos for each chromosome
				int[] columns = new int[]{0, 1, 2, 3};
				org.gwaspi.netCDF.operations.Utils.saveIntMapD2ToWrMatrix(wrNcFile, rdChrInfoSetMap, columns, cNetCDF.Variables.VAR_CHR_INFO);

				// MARKERSET POSITION
				rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
				MarkerSet_opt.replaceWithValuesFrom(wrMarkerIdSetMap, rdMarkerSet.getMarkerIdSetMap());
				//Utils.saveCharMapValueToWrMatrix(wrNcFile, wrMarkerIdSetMap, cNetCDF.Variables.VAR_MARKERS_POS, cNetCDF.Strides.STRIDE_POS);
				Utils.saveIntMapD1ToWrMatrix(wrNcFile, wrMarkerIdSetMap, cNetCDF.Variables.VAR_MARKERS_POS);

				// MARKERSET DICTIONARY ALLELES
				rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_BASES_DICT);
				MarkerSet_opt.replaceWithValuesFrom(wrMarkerIdSetMap, rdMarkerSet.getMarkerIdSetMap());
				Utils.saveCharMapValueToWrMatrix(wrNcFile, wrMarkerIdSetMap, cNetCDF.Variables.VAR_MARKERS_BASES_DICT, cNetCDF.Strides.STRIDE_GT);

				// GENOTYPE STRAND
				rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_GT_STRAND);
				MarkerSet_opt.replaceWithValuesFrom(wrMarkerIdSetMap, rdMarkerSet.getMarkerIdSetMap());
				Utils.saveCharMapValueToWrMatrix(wrNcFile, wrMarkerIdSetMap, cNetCDF.Variables.VAR_GT_STRAND, 3);
				//</editor-fold>

				//<editor-fold defaultstate="collapsed" desc="GENOTYPES WRITER">
				// Iterate through wrSampleSetMap, use item position to read correct sample GTs into rdMarkerIdSetMap.
				log.info(Text.All.processing);
				int sampleWrIndex = 0;
				for (Object value : wrSampleSetMap.values()) {
					// Iterate through wrMarkerIdSetMap, get the correct GT from rdMarkerIdSetMap
					Integer rdPos = (Integer) value;
					rdMarkerSet.fillGTsForCurrentSampleIntoInitMap(rdPos);
//					rdMarkerSet.fillGTsForCurrentSampleIntoInitMap(sampleWrPos);
					MarkerSet_opt.replaceWithValuesFrom(wrMarkerIdSetMap, rdMarkerSet.getMarkerIdSetMap());

					//Write wrMarkerIdSetMap to A3 ArrayChar and save to wrMatrix
					Utils.saveSingleSampleGTsToMatrix(wrNcFile, wrMarkerIdSetMap, sampleWrIndex);
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
					DbManager db = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
					db.updateTable(cDBGWASpi.SCH_MATRICES,
							cDBMatrix.T_MATRICES,
							new String[]{cDBMatrix.f_DESCRIPTION},
							new Object[]{descSB.toString()},
							new String[]{cDBMatrix.f_ID},
							new Object[]{resultMatrixId});

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
