package org.gwaspi.netCDF.operations;

import org.gwaspi.constants.cNetCDF;
import org.gwaspi.global.Text;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
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
public class MatrixTranslator_opt {

	private final static Logger log = LoggerFactory.getLogger(MatrixTranslator_opt.class);

	private int studyId = Integer.MIN_VALUE;
	private int rdMatrixId = Integer.MIN_VALUE;
	private int wrMatrixId = Integer.MIN_VALUE;
	private String wrMatrixFriendlyName = "";
	private String wrMatrixDescription = "";
	private MatrixMetadata rdMatrixMetadata = null;
	private MatrixMetadata wrMatrixMetadata = null;
	private MarkerSet_opt rdMarkerSet = null;
	private MarkerSet_opt wrMarkerSet = null;
	private SampleSet rdSampleSet = null;
	private SampleSet wrSampleSet = null;
	private Map<String, Object> wrMarkerIdSetLHM = new LinkedHashMap<String, Object>();
	private Map<String, Object> rdChrInfoSetLHM = null;
	private Map<String, Object> rdSampleSetLHM = null;
	private Map<String, Object> wrSampleSetLHM = new LinkedHashMap<String, Object>();

	public MatrixTranslator_opt(int _studyId,
			int _rdMatrixId,
			String _wrMatrixFriendlyName,
			String _wrMatrixDescription)
			throws IOException, InvalidRangeException
	{
		// INIT EXTRACTOR OBJECTS

		rdMatrixId = _rdMatrixId;
		rdMatrixMetadata = new MatrixMetadata(rdMatrixId);
		studyId = rdMatrixMetadata.getStudyId();
		wrMatrixFriendlyName = _wrMatrixFriendlyName;
		wrMatrixDescription = _wrMatrixDescription;

		rdMarkerSet = new MarkerSet_opt(rdMatrixMetadata.getStudyId(), rdMatrixId);
		rdMarkerSet.initFullMarkerIdSetLHM();

		rdChrInfoSetLHM = rdMarkerSet.getChrInfoSetLHM();

		rdSampleSet = new SampleSet(rdMatrixMetadata.getStudyId(), rdMatrixId);
		rdSampleSetLHM = rdSampleSet.getSampleIdSetLHM();
	}

	public int translateAB12AllelesToACGT() throws InvalidRangeException, IOException {
		int result = Integer.MIN_VALUE;

		NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());
		String rdMatrixGtCode = rdMatrixMetadata.getGenotypeEncoding();

		if (!rdMatrixGtCode.equals(cNetCDF.Defaults.GenotypeEncoding.ACGT0.toString())) { //Has not allready been translated

			try {
				// CREATE netCDF-3 FILE
				StringBuilder descSB = new StringBuilder(Text.Matrix.descriptionHeader1);
				descSB.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
				descSB.append("\nThrough Matrix translation from parent Matrix MX: ").append(rdMatrixId).append(" - ").append(rdMatrixMetadata.getMatrixFriendlyName());
				descSB.append("\nTranslation method: AB0 or 012 to ACGT0 using the parent's dictionnary");
				if (!wrMatrixDescription.isEmpty()) {
					descSB.append("\n\nDescription: ");
					descSB.append(wrMatrixDescription);
					descSB.append("\n");
				}
				descSB.append("\n");
				descSB.append("Markers: ").append(rdMatrixMetadata.getMarkerSetSize()).append(", Samples: ").append(rdMatrixMetadata.getSampleSetSize());
				descSB.append("\nGenotype encoding: ");
				descSB.append(cNetCDF.Defaults.GenotypeEncoding.ACGT0.toString());

				MatrixFactory wrMatrixHandler = new MatrixFactory(studyId,
						rdMatrixMetadata.getTechnology(), //technology
						wrMatrixFriendlyName,
						descSB.toString(), //description
						rdMatrixMetadata.getStrand(),
						rdMatrixMetadata.getHasDictionray(), //has dictionary?
						rdSampleSet.getSampleSetSize(),
						rdMarkerSet.getMarkerSetSize(),
						rdChrInfoSetLHM.size(),
						cNetCDF.Defaults.GenotypeEncoding.ACGT0.toString(), //New matrix genotype encoding
						rdMatrixId, //Orig matrixId 1
						Integer.MIN_VALUE);         //Orig matrixId 2

				NetcdfFileWriteable wrNcFile = wrMatrixHandler.getNetCDFHandler();
				try {
					wrNcFile.create();
				} catch (IOException ex) {
					log.error("Failed creating file " + wrNcFile.getLocation(), ex);
				}
				//log.trace("Done creating netCDF handle in MatrixataTransform: " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

				//<editor-fold defaultstate="collapsed" desc="METADATA WRITER">
				// WRITING METADATA TO MATRIX

				// SAMPLESET
				ArrayChar.D2 samplesD2 = Utils.writeLHMKeysToD2ArrayChar(rdSampleSetLHM, cNetCDF.Strides.STRIDE_SAMPLE_NAME);

				int[] sampleOrig = new int[]{0, 0};
				try {
					wrNcFile.write(cNetCDF.Variables.VAR_SAMPLESET, sampleOrig, samplesD2);
				} catch (IOException ex) {
					log.error("Failed writing file", ex);
				} catch (InvalidRangeException ex) {
					log.error(null, ex);
				}
				log.info("Done writing SampleSet to matrix at {}", org.gwaspi.global.Utils.getMediumDateTimeAsString()); // FIXME log system already adds time

				// MARKERSET MARKERID
				ArrayChar.D2 markersD2 = Utils.writeLHMKeysToD2ArrayChar(rdMarkerSet.getMarkerIdSetLHM(), cNetCDF.Strides.STRIDE_MARKER_NAME);
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
				Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdMarkerSet.getMarkerIdSetLHM(), cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);

				// MARKERSET CHROMOSOME
				rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
				Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdMarkerSet.getMarkerIdSetLHM(), cNetCDF.Variables.VAR_MARKERS_CHR, cNetCDF.Strides.STRIDE_CHR);

				// Set of chromosomes found in matrix along with number of markersinfo
				org.gwaspi.netCDF.operations.Utils.saveCharLHMKeyToWrMatrix(wrNcFile, rdChrInfoSetLHM, cNetCDF.Variables.VAR_CHR_IN_MATRIX, 8);
				// Number of marker per chromosome & max pos for each chromosome
				int[] columns = new int[]{0, 1, 2, 3};
				org.gwaspi.netCDF.operations.Utils.saveIntLHMD2ToWrMatrix(wrNcFile, rdChrInfoSetLHM, columns, cNetCDF.Variables.VAR_CHR_INFO);

				// MARKERSET POSITION
				rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
				Utils.saveIntLHMD1ToWrMatrix(wrNcFile, rdMarkerSet.getMarkerIdSetLHM(), cNetCDF.Variables.VAR_MARKERS_POS);

				// MARKERSET DICTIONARY ALLELES
				rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_BASES_DICT);
				wrMarkerIdSetLHM = MarkerSet_opt.replaceWithValuesFrom(wrMarkerIdSetLHM, rdMarkerSet.getMarkerIdSetLHM());
				Utils.saveCharLHMValueToWrMatrix(wrNcFile, wrMarkerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_BASES_DICT, cNetCDF.Strides.STRIDE_GT);

				// GENOTYPE STRAND
				rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_GT_STRAND);
				Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdMarkerSet.getMarkerIdSetLHM(), cNetCDF.Variables.VAR_GT_STRAND, cNetCDF.Strides.STRIDE_STRAND);
				//</editor-fold>

				//<editor-fold defaultstate="collapsed" desc="GENOTYPES WRITER">
				// Get correct bases dictionary for translation
				Map<String, Object> dictionnaryLHM = rdMarkerSet.getDictionaryBases();

				// Iterate through Samples, use Sample item position to read all Markers GTs from rdMarkerIdSetLHM.
				int sampleIndex = 0;
				for (int i = 0; i < rdSampleSetLHM.size(); i++) {
					// Get alleles from read matrix
					rdMarkerSet.fillGTsForCurrentSampleIntoInitLHM(sampleIndex);
					// Send to be translated
					wrMarkerIdSetLHM = translateCurrentSampleAB12AllelesLHM(rdMarkerSet.getMarkerIdSetLHM(), rdMatrixGtCode, dictionnaryLHM);

					// Write wrMarkerIdSetLHM to A3 ArrayChar and save to wrMatrix
					Utils.saveSingleSampleGTsToMatrix(wrNcFile, wrMarkerIdSetLHM, sampleIndex);
					if (sampleIndex % 100 == 0) {
						log.info("Samples translated: {}", sampleIndex);
					}
					sampleIndex++;
				}
				log.info("Total Samples translated: {}", sampleIndex);
				//</editor-fold>

				// CLOSE THE FILE AND BY THIS, MAKE IT READ-ONLY
				try {
					// GUESS GENOTYPE ENCODING
					ArrayChar.D2 guessedGTCodeAC = new ArrayChar.D2(1, 8);
					Index index = guessedGTCodeAC.getIndex();
					guessedGTCodeAC.setString(index.set(0, 0), cNetCDF.Defaults.GenotypeEncoding.ACGT0.toString());
					int[] origin = new int[]{0, 0};
					wrNcFile.write(cNetCDF.Variables.GLOB_GTENCODING, origin, guessedGTCodeAC);

					wrNcFile.close();
					result = wrMatrixHandler.getResultMatrixId();
				} catch (IOException ex) {
					log.error("Failed creating file " + wrNcFile.getLocation(), ex);
				}

				org.gwaspi.global.Utils.sysoutCompleted("Translation");
			} catch (InvalidRangeException ex) {
				log.error(null, ex);
			} catch (IOException ex) {
				log.error(null, ex);
			} finally {
				if (null != rdNcFile) {
					try {
						rdNcFile.close();
					} catch (IOException ex) {
						log.error("Failed to close file " + rdNcFile.getLocation(), ex);
					}
				}
			}
		}

		return result;
	}

	// TODO Test translate1234AllelesToACGT
	public int translate1234AllelesToACGT() throws IOException, InvalidRangeException {
		int result = Integer.MIN_VALUE;

		NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());
		String rdMatrixGTCode = rdMatrixMetadata.getGenotypeEncoding();

		if (!rdMatrixGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.ACGT0.toString())) { // Has not yet been translated
			try {
				// CREATE netCDF-3 FILE
				StringBuilder descSB = new StringBuilder(Text.Matrix.descriptionHeader1);
				descSB.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
				descSB.append("\nThrough Matrix translation from parent Matrix MX: ").append(rdMatrixMetadata.getMatrixId()).append(" - ").append(rdMatrixMetadata.getMatrixFriendlyName());
				descSB.append("\nTranslation method: O1234 to ACGT0 using 0=0, 1=A, 2=C, 3=G, 4=T");
				if (!wrMatrixDescription.isEmpty()) {
					descSB.append("\n\nDescription: ");
					descSB.append(wrMatrixDescription);
					descSB.append("\n");
				}
				descSB.append("\nGenotype encoding: ");
				descSB.append(cNetCDF.Defaults.GenotypeEncoding.ACGT0.toString());
				descSB.append("\n");
				descSB.append("Markers: ").append(rdMatrixMetadata.getMarkerSetSize()).append(", Samples: ").append(rdMatrixMetadata.getSampleSetSize());

				MatrixFactory wrMatrixHandler = new MatrixFactory(studyId,
						rdMatrixMetadata.getTechnology(), // technology
						wrMatrixFriendlyName,
						descSB.toString(), // description
						rdMatrixMetadata.getStrand(),
						rdMatrixMetadata.getHasDictionray(), // has dictionary?
						rdSampleSet.getSampleSetSize(),
						rdMarkerSet.getMarkerSetSize(),
						rdChrInfoSetLHM.size(),
						cNetCDF.Defaults.GenotypeEncoding.ACGT0.toString(), // New matrix genotype encoding
						rdMatrixId, // Orig matrixId 1
						Integer.MIN_VALUE); // Orig matrixId 2

				NetcdfFileWriteable wrNcFile = wrMatrixHandler.getNetCDFHandler();
				try {
					wrNcFile.create();
				} catch (IOException ex) {
					log.error("Failed creating file " + wrNcFile.getLocation(), ex);
				}
				//log.trace("Done creating netCDF handle in MatrixataTransform: " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

				//<editor-fold defaultstate="collapsed" desc="METADATA WRITER">
				// WRITING METADATA TO MATRIX

				// SAMPLESET
				ArrayChar.D2 samplesD2 = Utils.writeLHMKeysToD2ArrayChar(rdSampleSetLHM, cNetCDF.Strides.STRIDE_SAMPLE_NAME);

				int[] sampleOrig = new int[]{0, 0};
				try {
					wrNcFile.write(cNetCDF.Variables.VAR_SAMPLESET, sampleOrig, samplesD2);
				} catch (IOException ex) {
					log.error("Failed writing file", ex);
				} catch (InvalidRangeException ex) {
					log.error(null, ex);
				}
				log.info("Done writing SampleSet to matrix at {}", org.gwaspi.global.Utils.getMediumDateTimeAsString()); // FIXME log system already adds time

				// MARKERSET MARKERID
				ArrayChar.D2 markersD2 = Utils.writeLHMKeysToD2ArrayChar(rdMarkerSet.getMarkerIdSetLHM(), cNetCDF.Strides.STRIDE_MARKER_NAME);
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
				Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdMarkerSet.getMarkerIdSetLHM(), cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);

				// MARKERSET CHROMOSOME
				rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
				Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdMarkerSet.getMarkerIdSetLHM(), cNetCDF.Variables.VAR_MARKERS_CHR, cNetCDF.Strides.STRIDE_CHR);

				// MARKERSET POSITION
				rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
				//Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdMarkerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_POS, cNetCDF.Strides.STRIDE_POS);
				Utils.saveIntLHMD1ToWrMatrix(wrNcFile, rdMarkerSet.getMarkerIdSetLHM(), cNetCDF.Variables.VAR_MARKERS_POS);

				// MARKERSET DICTIONARY ALLELES
				rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_BASES_DICT);
				Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdMarkerSet.getMarkerIdSetLHM(), cNetCDF.Variables.VAR_MARKERS_BASES_DICT, cNetCDF.Strides.STRIDE_GT);

				// GENOTYPE STRAND
				rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_GT_STRAND);
				Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdMarkerSet.getMarkerIdSetLHM(), cNetCDF.Variables.VAR_GT_STRAND, cNetCDF.Strides.STRIDE_STRAND);
				//</editor-fold>

				//<editor-fold defaultstate="collapsed" desc="GENOTYPES WRITER">
				// Get correct strand of each marker for newStrand translation
				Map<String, Object> markerStrandsLHM = new LinkedHashMap<String, Object>();
				markerStrandsLHM.putAll(rdSampleSetLHM);

				// Iterate through pmAllelesAndStrandsLHM, use Sample item position to read all Markers GTs from rdMarkerIdSetLHM.
				int sampleNb = 0;
				for (int i = 0; i < rdSampleSetLHM.size(); i++) {
					// Get alleles from read matrix
					rdMarkerSet.fillGTsForCurrentSampleIntoInitLHM(sampleNb);
					// Send to be translated
					wrMarkerIdSetLHM = translateCurrentSample1234AllelesLHM(rdMarkerSet.getMarkerIdSetLHM(), markerStrandsLHM);

					// Write wrMarkerIdSetLHM to A3 ArrayChar and save to wrMatrix
					Utils.saveSingleSampleGTsToMatrix(wrNcFile, wrMarkerIdSetLHM, sampleNb);

					if (sampleNb % 100 == 0) {
						log.info("Samples translated: {}", sampleNb);
					}
					sampleNb++;
				}
				log.info("Total Samples translated: {}", sampleNb);
				//</editor-fold>

				// CLOSE THE FILE AND BY THIS, MAKE IT READ-ONLY
				try {
					// GUESS GENOTYPE ENCODING
					ArrayChar.D2 guessedGTCodeAC = new ArrayChar.D2(1, 8);
					Index index = guessedGTCodeAC.getIndex();
					guessedGTCodeAC.setString(index.set(0, 0), cNetCDF.Defaults.GenotypeEncoding.ACGT0.toString());
					int[] origin = new int[]{0, 0};
					wrNcFile.write(cNetCDF.Variables.GLOB_GTENCODING, origin, guessedGTCodeAC);

					wrNcFile.close();
					result = wrMatrixHandler.getResultMatrixId();
				} catch (IOException ex) {
					log.error("Failed creating file " + wrNcFile.getLocation(), ex);
				}

				org.gwaspi.global.Utils.sysoutCompleted("Translation");
			} catch (InvalidRangeException ex) {
				log.error(null, ex);
			} catch (IOException ex) {
				log.error(null, ex);
			} finally {
				if (null != rdNcFile) {
					try {
						rdNcFile.close();
					} catch (IOException ex) {
						log.error("Failed close file " + rdNcFile.getLocation(), ex);
					}
				}
			}
		}

		return result;
	}

	protected Map<String, Object> translateCurrentSampleAB12AllelesLHM(Map<String, Object> codedLHM, String rdMatrixType, Map<String, Object> dictionaryLHM) {
		byte alleleA;
		byte alleleB;

		switch (cNetCDF.Defaults.GenotypeEncoding.compareTo(rdMatrixType)) {
			case AB0:
				alleleA = org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes.A;
				alleleB = org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes.B;
				// Iterate through all markers
				for (Map.Entry<String, Object> entry : codedLHM.entrySet()) {
					String markerId = entry.getKey();
					char[] basesDict = dictionaryLHM.get(markerId).toString().toCharArray();
					byte[] codedAlleles = (byte[]) entry.getValue();
					byte[] transAlleles = new byte[2];

					if (codedAlleles[0] == alleleA) {
						transAlleles[0] = (byte) basesDict[0];
					} else if (codedAlleles[0] == alleleB) {
						transAlleles[0] = (byte) basesDict[1];
					} else {
						transAlleles[0] = org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes._0;
					}

					if (codedAlleles[1] == alleleA) {
						transAlleles[1] = (byte) basesDict[0];
					} else if (codedAlleles[1] == alleleB) {
						transAlleles[1] = (byte) basesDict[1];
					} else {
						transAlleles[1] = org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes._0;
					}

					entry.setValue(transAlleles);
				}
				break;
			case O12:
				alleleA = org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes._1;
				alleleB = org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes._2;

				// Iterate through all markers
				for (Map.Entry<String, Object> entry : codedLHM.entrySet()) {
					String markerId = entry.getKey();
					char[] basesDict = dictionaryLHM.get(markerId).toString().toCharArray();
					byte[] codedAlleles = (byte[]) entry.getValue();
					byte[] transAlleles = new byte[2];

					if (codedAlleles[0] == alleleA) {
						transAlleles[0] = (byte) basesDict[0];
					} else if (codedAlleles[0] == alleleB) {
						transAlleles[0] = (byte) basesDict[1];
					} else {
						transAlleles[0] = org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes._0;
					}

					if (codedAlleles[1] == alleleA) {
						transAlleles[1] = (byte) basesDict[0];
					} else if (codedAlleles[1] == alleleB) {
						transAlleles[1] = (byte) basesDict[1];
					} else {
						transAlleles[0] = org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes._0;
					}

					entry.setValue(transAlleles);
				}
				break;
			default:
				break;
		}

		return codedLHM;
	}

	protected Map<String, Object> translateCurrentSample1234AllelesLHM(Map<String, Object> codedLHM, Map<String, Object> markerStrandsLHM) {

		Map<Byte, Byte> dictionary = new HashMap<Byte, Byte>();
		dictionary.put(org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes._0, org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes._0);
		dictionary.put(org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes._1, org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes.A);
		dictionary.put(org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes._2, org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes.C);
		dictionary.put(org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes._3, org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes.G);
		dictionary.put(org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes._4, org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes.T);

		// Iterate through all markers
		for (String markerId : markerStrandsLHM.keySet()) {
			byte[] codedAlleles = (byte[]) codedLHM.get(markerId);

			byte[] transAlleles = new byte[2];
			transAlleles[0] = dictionary.get(codedAlleles[0]);
			transAlleles[1] = dictionary.get(codedAlleles[1]);

			codedLHM.put(markerId, transAlleles);
		}
		return codedLHM;
	}

	//<editor-fold defaultstate="collapsed" desc="ACCESSORS">
	public int getRdMatrixId() {
		return rdMatrixId;
	}

	public int getStudyId() {
		return studyId;
	}

	public int getWrMatrixId() {
		return wrMatrixId;
	}
	//</editor-fold>
}
