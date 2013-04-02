package org.gwaspi.netCDF.operations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.global.Text;
import org.gwaspi.model.MarkerKey;
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

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MatrixGenotypesFlipper {

	private final Logger log = LoggerFactory.getLogger(MatrixGenotypesFlipper.class);

	private int studyId = Integer.MIN_VALUE;
	private int rdMatrixId = Integer.MIN_VALUE;
	private int wrMatrixId = Integer.MIN_VALUE;
	private String wrMatrixFriendlyName = "";
	private String wrMatrixDescription = "";
	private File flipperFile;
	private MatrixMetadata rdMatrixMetadata = null;
	private GenotypeEncoding gtEncoding = GenotypeEncoding.UNKNOWN;
	private MarkerSet rdMarkerSet = null;
	private Set<MarkerKey> markerFlipHS = new HashSet<MarkerKey>();
	private SampleSet rdSampleSet = null;
	private Map<MarkerKey, Object> rdMarkerIdSetMap = new LinkedHashMap<MarkerKey, Object>();
	private Map<SampleKey, Object> rdSampleSetMap = new LinkedHashMap<SampleKey, Object>();
	private Map<MarkerKey, Object> rdChrInfoSetMap = new LinkedHashMap<MarkerKey, Object>();

	/**
	 * This constructor to extract data from Matrix a by passing a variable and
	 * the criteria to filter items by.
	 *
	 * @param studyId
	 * @param rdMatrixId
	 * @param wrMatrixFriendlyName
	 * @param wrMatrixDescription
	 * @param markerVariable
	 * @param flipperFile
	 * @throws IOException
	 * @throws InvalidRangeException
	 */
	public MatrixGenotypesFlipper(
			int studyId,
			int rdMatrixId,
			String wrMatrixFriendlyName,
			String wrMatrixDescription,
			String markerVariable,
			File flipperFile)
			throws IOException, InvalidRangeException
	{
		// INIT EXTRACTOR OBJECTS
		this.rdMatrixId = rdMatrixId;
		this.rdMatrixMetadata = MatricesList.getMatrixMetadataById(this.rdMatrixId);
		this.studyId = rdMatrixMetadata.getStudyId();
		this.wrMatrixFriendlyName = wrMatrixFriendlyName;
		this.wrMatrixDescription = wrMatrixDescription;
		this.gtEncoding = this.rdMatrixMetadata.getGenotypeEncoding();
		this.flipperFile = flipperFile;

		this.rdMarkerSet = new MarkerSet(this.rdMatrixMetadata.getStudyId(), this.rdMatrixId);
		this.rdMarkerSet.initFullMarkerIdSetMap();
		this.rdMarkerIdSetMap = this.rdMarkerSet.getMarkerIdSetMap();

		this.rdChrInfoSetMap = this.rdMarkerSet.getChrInfoSetMap();

		this.rdSampleSet = new SampleSet(this.rdMatrixMetadata.getStudyId(), this.rdMatrixId);
		this.rdSampleSetMap = this.rdSampleSet.getSampleIdSetMap();

		if (this.flipperFile.isFile()) {
			FileReader fr = new FileReader(this.flipperFile);
			BufferedReader br = new BufferedReader(fr);
			String line;
			while ((line = br.readLine()) != null) {
				this.markerFlipHS.add(MarkerKey.valueOf(line));
			}
			br.close();
		}
	}

	public int flipGenotypesToNewMatrix() throws IOException {
		int resultMatrixId = Integer.MIN_VALUE;
		try {
			// CREATE netCDF-3 FILE
			StringBuilder descSB = new StringBuilder(Text.Matrix.descriptionHeader1);
			descSB.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
			descSB.append("\nThrough Matrix genotype flipping from parent Matrix MX: ").append(rdMatrixMetadata.getMatrixId()).append(" - ").append(rdMatrixMetadata.getMatrixFriendlyName());
			descSB.append("\nUsed list of markers to be flipped: ").append(flipperFile.getPath());
			if (!wrMatrixDescription.isEmpty()) {
				descSB.append("\n\nDescription: ");
				descSB.append(wrMatrixDescription);
				descSB.append("\n");
			}
			descSB.append("\nGenotype encoding: ");
			descSB.append(rdMatrixMetadata.getGenotypeEncoding());
			descSB.append("\n");
			descSB.append("Markers: ").append(rdMarkerSet.getMarkerSetSize()).append(", Samples: ").append(rdSampleSet.getSampleSetSize());

			MatrixFactory wrMatrixHandler = new MatrixFactory(
					studyId,
					rdMatrixMetadata.getTechnology(), // technology
					wrMatrixFriendlyName,
					descSB.toString(), // description
					rdMatrixMetadata.getGenotypeEncoding(), // Matrix genotype encoding from orig matrix genotype encoding
					StrandType.valueOf("FLP"), // FIXME this will fail at runtime
					rdMatrixMetadata.getHasDictionray(), // has dictionary?
					rdSampleSet.getSampleSetSize(),
					rdMarkerSet.getMarkerSetSize(),
					rdChrInfoSetMap.size(),
					rdMatrixId, // Orig matrixId 1
					Integer.MIN_VALUE); // Orig matrixId 2

			resultMatrixId = wrMatrixHandler.getResultMatrixId();

			NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());
			NetcdfFileWriteable wrNcFile = wrMatrixHandler.getNetCDFHandler();
			try {
				wrNcFile.create();
			} catch (IOException ex) {
				log.error("Failed creating file " + wrNcFile.getLocation(), ex);
			}
			//log.trace("Done creating netCDF handle in MatrixataExtractor: " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

			//<editor-fold defaultstate="expanded" desc="METADATA WRITER">
			// WRITING METADATA TO MATRIX

			// SAMPLESET
			ArrayChar.D2 samplesD2 = Utils.writeMapKeysToD2ArrayChar(rdSampleSetMap, cNetCDF.Strides.STRIDE_SAMPLE_NAME);

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
			ArrayChar.D2 markersD2 = Utils.writeMapKeysToD2ArrayChar(rdMarkerIdSetMap, cNetCDF.Strides.STRIDE_MARKER_NAME);
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
			MarkerSet.replaceWithValuesFrom(rdMarkerIdSetMap, rdMarkerSet.getMarkerIdSetMap());
			Utils.saveCharMapValueToWrMatrix(wrNcFile, rdMarkerIdSetMap, cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);

			// MARKERSET CHROMOSOME
			rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
			MarkerSet.replaceWithValuesFrom(rdMarkerIdSetMap, rdMarkerSet.getMarkerIdSetMap());
			Utils.saveCharMapValueToWrMatrix(wrNcFile, rdMarkerIdSetMap, cNetCDF.Variables.VAR_MARKERS_CHR, cNetCDF.Strides.STRIDE_CHR);

			// Set of chromosomes found in matrix along with number of markersinfo
			org.gwaspi.netCDF.operations.Utils.saveCharMapKeyToWrMatrix(wrNcFile, rdChrInfoSetMap, cNetCDF.Variables.VAR_CHR_IN_MATRIX, 8);
			// Number of marker per chromosome & max pos for each chromosome
			int[] columns = new int[]{0, 1, 2, 3};
			org.gwaspi.netCDF.operations.Utils.saveIntMapD2ToWrMatrix(wrNcFile, rdChrInfoSetMap, columns, cNetCDF.Variables.VAR_CHR_INFO);

			// MARKERSET POSITION
			rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
			MarkerSet.replaceWithValuesFrom(rdMarkerIdSetMap, rdMarkerSet.getMarkerIdSetMap());
			//Utils.saveCharMapValueToWrMatrix(wrNcFile, rdMarkerIdSetMap, cNetCDF.Variables.VAR_MARKERS_POS, cNetCDF.Strides.STRIDE_POS);
			Utils.saveIntMapD1ToWrMatrix(wrNcFile, rdMarkerIdSetMap, cNetCDF.Variables.VAR_MARKERS_POS);

			// MARKERSET DICTIONARY ALLELES
			rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_BASES_DICT);
			MarkerSet.replaceWithValuesFrom(rdMarkerIdSetMap, rdMarkerSet.getMarkerIdSetMap());
			for (Map.Entry<MarkerKey, Object> entry : rdMarkerIdSetMap.entrySet()) {
				MarkerKey markerKey = entry.getKey();
				if (markerFlipHS.contains(markerKey)) {
					String alleles = entry.getValue().toString();
					alleles = flipDictionaryAlleles(alleles);
					entry.setValue(alleles);
				}
			}
			Utils.saveCharMapValueToWrMatrix(wrNcFile, rdMarkerIdSetMap, cNetCDF.Variables.VAR_MARKERS_BASES_DICT, cNetCDF.Strides.STRIDE_GT);

			// GENOTYPE STRAND
			rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_GT_STRAND);
			MarkerSet.replaceWithValuesFrom(rdMarkerIdSetMap, rdMarkerSet.getMarkerIdSetMap());

			for (Map.Entry<MarkerKey, Object> entry : rdMarkerIdSetMap.entrySet()) {
				MarkerKey markerKey = entry.getKey();
				if (markerFlipHS.contains(markerKey)) {
					String strand = entry.getValue().toString();
					strand = flipStranding(strand);
					entry.setValue(strand);
				}
			}
			Utils.saveCharMapValueToWrMatrix(wrNcFile, rdMarkerIdSetMap, cNetCDF.Variables.VAR_GT_STRAND, 3);
			//</editor-fold>

			//<editor-fold defaultstate="expanded" desc="GENOTYPES WRITER">
			log.info(Text.All.processing);
			int markerIndex = 0;
			for (Map.Entry<MarkerKey, Object> entry : rdMarkerIdSetMap.entrySet()) {
				MarkerKey markerKey = entry.getKey();
				rdSampleSet.readAllSamplesGTsFromCurrentMarkerToMap(rdNcFile, rdSampleSetMap, markerIndex);

				if (markerFlipHS.contains(markerKey)) {
					for (Map.Entry<SampleKey, Object> sampleEntry : rdSampleSetMap.entrySet()) {
						byte[] gt = (byte[]) sampleEntry.getValue();
						gt = flipGenotypes(gt, gtEncoding);
						sampleEntry.setValue(new byte[]{gt[0], gt[1]});
					}
				}

				// Write rdMarkerIdSetMap to A3 ArrayChar and save to wrMatrix
				Utils.saveSingleMarkerGTsToMatrix(wrNcFile, rdSampleSetMap, markerIndex);
				if (markerIndex % 10000 == 0) {
					log.info("Markers processed: {}" + markerIndex);
				}
				markerIndex++;
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
				MatricesList.saveMatrixDescription(resultMatrixId, descSB.toString());

				wrNcFile.close();
			} catch (IOException ex) {
				log.error("Failed creating file " + wrNcFile.getLocation(), ex);
			}

			org.gwaspi.global.Utils.sysoutCompleted("Genotype Flipping to new Matrix");
		} catch (InvalidRangeException ex) {
			log.error(null, ex);
		} catch (IOException ex) {
			log.error(null, ex);
		}

		return resultMatrixId;
	}

	private static String flipDictionaryAlleles(String alleles) {

		String flippedAlleles = alleles;

		flippedAlleles = flippedAlleles.replaceAll("A", "t");
		flippedAlleles = flippedAlleles.replaceAll("C", "g");
		flippedAlleles = flippedAlleles.replaceAll("G", "c");
		flippedAlleles = flippedAlleles.replaceAll("T", "a");
		flippedAlleles = flippedAlleles.toUpperCase();

		return flippedAlleles;
	}

	private static String flipStranding(String strand) {
		if (strand.equals("+")) {
			return "-";
		} else if (strand.equals("-")) {
			return "+";
		} else {
			return strand;
		}
	}

	private static byte[] flipGenotypes(byte[] gt, GenotypeEncoding gtEncoding) {
		byte[] result = gt;

		if (gtEncoding.equals(cNetCDF.Defaults.GenotypeEncoding.ACGT0)) {
			for (int i = 0; i < gt.length; i++) {
				if (gt[i] == cNetCDF.Defaults.AlleleBytes.A) {
					result[i] = cNetCDF.Defaults.AlleleBytes.T;
				} else if (gt[i] == cNetCDF.Defaults.AlleleBytes.C) {
					result[i] = cNetCDF.Defaults.AlleleBytes.G;
				} else if (gt[i] == cNetCDF.Defaults.AlleleBytes.G) {
					result[i] = cNetCDF.Defaults.AlleleBytes.C;
				} else if (gt[i] == cNetCDF.Defaults.AlleleBytes.T) {
					result[i] = cNetCDF.Defaults.AlleleBytes.A;
				}
			}
		}

		if (gtEncoding.equals(cNetCDF.Defaults.GenotypeEncoding.O1234)) {
			for (int i = 0; i < gt.length; i++) {
				if (gt[i] == cNetCDF.Defaults.AlleleBytes._1) {
					result[i] = cNetCDF.Defaults.AlleleBytes._4;
				} else if (gt[i] == cNetCDF.Defaults.AlleleBytes._2) {
					result[i] = cNetCDF.Defaults.AlleleBytes._3;
				} else if (gt[i] == cNetCDF.Defaults.AlleleBytes._3) {
					result[i] = cNetCDF.Defaults.AlleleBytes._2;
				} else if (gt[i] == cNetCDF.Defaults.AlleleBytes._4) {
					result[i] = cNetCDF.Defaults.AlleleBytes._1;
				}
			}
		}

		return result;
	}
}
