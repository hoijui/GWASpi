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
import org.gwaspi.netCDF.markers.MarkerSet_opt;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.gwaspi.netCDF.matrices.MatrixMetadata;
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
	private Map<String, Object> wrMarkerIdSetLHM;
	private Map<String, Object> rdSampleSetLHM;
	private Map<String, Object> wrSampleSetLHM;
	private Map<String, Object> rdChrInfoSetLHM;

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
		this.rdMatrixMetadata = new MatrixMetadata(this.rdMatrixId);
		this.studyId = rdMatrixMetadata.getStudyId();
		this.wrMatrixFriendlyName = wrMatrixFriendlyName;
		this.wrMatrixDescription = wrMatrixDescription;

		this.rdMarkerSet = new MarkerSet_opt(this.rdMatrixMetadata.getStudyId(), this.rdMatrixId);
		this.rdMarkerSet.initFullMarkerIdSetLHM();

		this.rdSampleSet = new SampleSet(this.rdMatrixMetadata.getStudyId(), this.rdMatrixId);
		this.rdSampleSetLHM = this.rdSampleSet.getSampleIdSetLHM();

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

		this.wrMarkerIdSetLHM = new LinkedHashMap<String, Object>();
		switch (markerPickCase) {
			case ALL_MARKERS:
				// Get all markers
				this.wrMarkerIdSetLHM.putAll(this.rdMarkerSet.getMarkerIdSetLHM());
				MarkerSet_opt.fillWith(wrMarkerIdSetLHM, cNetCDF.Defaults.DEFAULT_GT);
				break;
			case MARKERS_INCLUDE_BY_NETCDF_CRITERIA:
				// Pick by netCDF field value and criteria
				this.wrMarkerIdSetLHM = this.rdMarkerSet.pickValidMarkerSetItemsByValue(markerPickerVar, markerCriteria, true);
				MarkerSet_opt.fillWith(this.wrMarkerIdSetLHM, cNetCDF.Defaults.DEFAULT_GT);
				break;
			case MARKERS_EXCLUDE_BY_NETCDF_CRITERIA:
				// Exclude by netCDF field value and criteria
				this.wrMarkerIdSetLHM = this.rdMarkerSet.pickValidMarkerSetItemsByValue(markerPickerVar, markerCriteria, false);
				MarkerSet_opt.fillWith(this.wrMarkerIdSetLHM, cNetCDF.Defaults.DEFAULT_GT);
				break;
			case MARKERS_INCLUDE_BY_ID:
				this.wrMarkerIdSetLHM = this.rdMarkerSet.pickValidMarkerSetItemsByKey(markerCriteria, true);
				MarkerSet_opt.fillWith(this.wrMarkerIdSetLHM, cNetCDF.Defaults.DEFAULT_GT);
				break;
			case MARKERS_EXCLUDE_BY_ID:
				this.wrMarkerIdSetLHM = this.rdMarkerSet.pickValidMarkerSetItemsByKey(markerCriteria, false);
				MarkerSet_opt.fillWith(this.wrMarkerIdSetLHM, cNetCDF.Defaults.DEFAULT_GT);
				break;
			default:
				// Get all markers
				this.wrMarkerIdSetLHM.putAll(this.rdMarkerSet.getMarkerIdSetLHM());
				MarkerSet_opt.fillWith(this.wrMarkerIdSetLHM, cNetCDF.Defaults.DEFAULT_GT);
		}

		// RETRIEVE CHROMOSOMES INFO
		this.rdMarkerSet.fillMarkerSetLHMWithChrAndPos();
		MarkerSet_opt.replaceWithValuesFrom(this.wrMarkerIdSetLHM, this.rdMarkerSet.getMarkerIdSetLHM());
		this.rdChrInfoSetLHM = org.gwaspi.netCDF.matrices.Utils.aggregateChromosomeInfo(this.wrMarkerIdSetLHM, 0, 1);
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

		this.wrSampleSetLHM = new LinkedHashMap<String, Object>();
		switch (samplePickCase) {
			case ALL_SAMPLES:
				// Get all samples
				this.wrSampleSetLHM.putAll(this.rdSampleSetLHM);
				int i = 0;
				for (Map.Entry<String, Object> entry : this.wrSampleSetLHM.entrySet()) {
					entry.setValue(i);
					i++;
				}
				break;
			case SAMPLES_INCLUDE_BY_NETCDF_FILTER:
				// USE cNetCDF Filter Data and criteria
				this.wrSampleSetLHM = this.rdSampleSet.pickValidSampleSetItemsByNetCDFFilter(this.rdSampleSetLHM, samplePickerVar, sampleFilterPos, sampleCriteria, true);
				break;
			case SAMPLES_EXCLUDE_BY_NETCDF_FILTER:
				// USE cNetCDF Filter Data and criteria
				this.wrSampleSetLHM = this.rdSampleSet.pickValidSampleSetItemsByNetCDFFilter(this.rdSampleSetLHM, samplePickerVar, sampleFilterPos, sampleCriteria, false);
				break;
			case SAMPLES_INCLUDE_BY_NETCDF_CRITERIA:
				// USE cNetCDF Value and criteria
				this.wrSampleSetLHM = this.rdSampleSet.pickValidSampleSetItemsByNetCDFValue(this.rdSampleSetLHM, samplePickerVar, sampleCriteria, true);
				break;
			case SAMPLES_EXCLUDE_BY_NETCDF_CRITERIA:
				// USE cNetCDF Value and criteria
				this.wrSampleSetLHM = this.rdSampleSet.pickValidSampleSetItemsByNetCDFValue(this.rdSampleSetLHM, samplePickerVar, sampleCriteria, false);
				break;
			case SAMPLES_INCLUDE_BY_ID:
				this.wrSampleSetLHM = this.rdSampleSet.pickValidSampleSetItemsByNetCDFKey(this.rdSampleSetLHM, sampleCriteria, true);
				break;
			case SAMPLES_EXCLUDE_BY_ID:
				this.wrSampleSetLHM = this.rdSampleSet.pickValidSampleSetItemsByNetCDFKey(this.rdSampleSetLHM, sampleCriteria, false);
				break;
			case SAMPLES_INCLUDE_BY_DB_FIELD:
				// USE DB DATA
				this.wrSampleSetLHM = this.rdSampleSet.pickValidSampleSetItemsByDBField(studyId, this.rdSampleSetLHM, samplePickerVar, sampleCriteria, true);
				break;
			case SAMPLES_EXCLUDE_BY_DB_FIELD:
				// USE DB DATA
				this.wrSampleSetLHM = this.rdSampleSet.pickValidSampleSetItemsByDBField(studyId, this.rdSampleSetLHM, samplePickerVar, sampleCriteria, false);
				break;
			default:
				int j = 0;
				for (Map.Entry<String, Object> entry : this.wrSampleSetLHM.entrySet()) {
					entry.setValue(j);
					j++;
				}
		}
		//</editor-fold>
	}

	public int extractGenotypesToNewMatrix() throws IOException {
		int resultMatrixId = Integer.MIN_VALUE;

		if (wrSampleSetLHM.size() > 0 && wrMarkerIdSetLHM.size() > 0) {
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
				descSB.append("Markers: ").append(wrMarkerIdSetLHM.size()).append(", Samples: ").append(wrSampleSetLHM.size());

				MatrixFactory wrMatrixHandler = new MatrixFactory(studyId,
						rdMatrixMetadata.getTechnology(), // technology
						wrMatrixFriendlyName,
						descSB.toString(), // description
						rdMatrixMetadata.getStrand(),
						rdMatrixMetadata.getHasDictionray(), // has dictionary?
						wrSampleSetLHM.size(),
						wrMarkerIdSetLHM.size(),
						rdChrInfoSetLHM.size(),
						rdMatrixMetadata.getGenotypeEncoding(), // Matrix genotype encoding from orig matrix genotype encoding
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
				ArrayChar.D2 samplesD2 = Utils.writeLHMKeysToD2ArrayChar(wrSampleSetLHM, cNetCDF.Strides.STRIDE_SAMPLE_NAME);

				int[] sampleOrig = new int[]{0, 0};
				try {
					wrNcFile.write(cNetCDF.Variables.VAR_SAMPLESET, sampleOrig, samplesD2);
				} catch (IOException ex) {
					log.error("Failed writing file", ex);
				} catch (InvalidRangeException ex) {
					log.error(null, ex);
				}
				log.info("Done writing SampleSet to matrix at {}", org.gwaspi.global.Utils.getMediumDateTimeAsString()); // FIXME log system already adds timestamp


				// MARKERSET MARKERID
				ArrayChar.D2 markersD2 = Utils.writeLHMKeysToD2ArrayChar(wrMarkerIdSetLHM, cNetCDF.Strides.STRIDE_MARKER_NAME);
				int[] markersOrig = new int[]{0, 0};
				try {
					wrNcFile.write(cNetCDF.Variables.VAR_MARKERSET, markersOrig, markersD2);
				} catch (IOException ex) {
					log.error("Failed writing file", ex);
				} catch (InvalidRangeException ex) {
					log.error(null, ex);
				}

				// MARKERSET RSID
				rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
				MarkerSet_opt.replaceWithValuesFrom(wrMarkerIdSetLHM, rdMarkerSet.getMarkerIdSetLHM());
				Utils.saveCharLHMValueToWrMatrix(wrNcFile, wrMarkerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);

				// MARKERSET CHROMOSOME
				rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
				MarkerSet_opt.replaceWithValuesFrom(wrMarkerIdSetLHM, rdMarkerSet.getMarkerIdSetLHM());
				Utils.saveCharLHMValueToWrMatrix(wrNcFile, wrMarkerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_CHR, cNetCDF.Strides.STRIDE_CHR);

				// Set of chromosomes found in matrix along with number of markersinfo
				org.gwaspi.netCDF.operations.Utils.saveCharLHMKeyToWrMatrix(wrNcFile, rdChrInfoSetLHM, cNetCDF.Variables.VAR_CHR_IN_MATRIX, 8);
				// Number of marker per chromosome & max pos for each chromosome
				int[] columns = new int[]{0, 1, 2, 3};
				org.gwaspi.netCDF.operations.Utils.saveIntLHMD2ToWrMatrix(wrNcFile, rdChrInfoSetLHM, columns, cNetCDF.Variables.VAR_CHR_INFO);

				// MARKERSET POSITION
				rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
				MarkerSet_opt.replaceWithValuesFrom(wrMarkerIdSetLHM, rdMarkerSet.getMarkerIdSetLHM());
				//Utils.saveCharLHMValueToWrMatrix(wrNcFile, wrMarkerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_POS, cNetCDF.Strides.STRIDE_POS);
				Utils.saveIntLHMD1ToWrMatrix(wrNcFile, wrMarkerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_POS);

				// MARKERSET DICTIONARY ALLELES
				rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_BASES_DICT);
				MarkerSet_opt.replaceWithValuesFrom(wrMarkerIdSetLHM, rdMarkerSet.getMarkerIdSetLHM());
				Utils.saveCharLHMValueToWrMatrix(wrNcFile, wrMarkerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_BASES_DICT, cNetCDF.Strides.STRIDE_GT);

				// GENOTYPE STRAND
				rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_GT_STRAND);
				MarkerSet_opt.replaceWithValuesFrom(wrMarkerIdSetLHM, rdMarkerSet.getMarkerIdSetLHM());
				Utils.saveCharLHMValueToWrMatrix(wrNcFile, wrMarkerIdSetLHM, cNetCDF.Variables.VAR_GT_STRAND, 3);
				//</editor-fold>

				//<editor-fold defaultstate="collapsed" desc="GENOTYPES WRITER">
				// Iterate through wrSampleSetLHM, use item position to read correct sample GTs into rdMarkerIdSetLHM.
				log.info(Text.All.processing);
				int sampleWrIndex = 0;
				for (Object value : wrSampleSetLHM.values()) {
					// Iterate through wrMarkerIdSetLHM, get the correct GT from rdMarkerIdSetLHM
					Integer rdPos = (Integer) value;
					rdMarkerSet.fillGTsForCurrentSampleIntoInitLHM(rdPos);
//					rdMarkerSet.fillGTsForCurrentSampleIntoInitLHM(sampleWrPos);
					MarkerSet_opt.replaceWithValuesFrom(wrMarkerIdSetLHM, rdMarkerSet.getMarkerIdSetLHM());

					//Write wrMarkerIdSetLHM to A3 ArrayChar and save to wrMatrix
					Utils.saveSingleSampleGTsToMatrix(wrNcFile, wrMarkerIdSetLHM, sampleWrIndex);
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
					guessedGTCodeAC.setString(index.set(0, 0), rdMatrixMetadata.getGenotypeEncoding());
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
