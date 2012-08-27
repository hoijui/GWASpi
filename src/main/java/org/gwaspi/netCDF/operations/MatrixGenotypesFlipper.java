package org.gwaspi.netCDF.operations;

import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import org.gwaspi.global.Text;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.gwaspi.netCDF.markers.MarkerSet_opt;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.gwaspi.netCDF.matrices.MatrixMetadata;
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
public class MatrixGenotypesFlipper {

	private int studyId = Integer.MIN_VALUE;
	private int rdMatrixId = Integer.MIN_VALUE;
	private int wrMatrixId = Integer.MIN_VALUE;
	private String wrMatrixFriendlyName = "";
	private String wrMatrixDescription = "";
	private File flipperFile;
	private MatrixMetadata rdMatrixMetadata = null;
	private GenotypeEncoding gtEncoding = GenotypeEncoding.UNKNOWN;
	private MarkerSet_opt rdMarkerSet = null;
	private Set<String> markerFlipHS = new HashSet<String>();
	private SampleSet rdSampleSet = null;
	private Map<String, Object> rdMarkerIdSetLHM = new LinkedHashMap<String, Object>();
	private Map<String, Object> rdSampleSetLHM = new LinkedHashMap<String, Object>();
	private Map<String, Object> rdChrInfoSetLHM = new LinkedHashMap<String, Object>();

	/**
	 * This constructor to extract data from Matrix a by passing a variable and
	 * the criteria to filter items by.
	 *
	 * @param _studyId
	 * @param _rdMatrixId
	 * @param _wrMatrixFriendlyName
	 * @param _wrMatrixDescription
	 * @param _markerPickCase
	 * @param _samplePickCase
	 * @param _markerPickerVar
	 * @param _samplePickerVar
	 * @param _markerCriteria
	 * @param _sampleCriteria
	 * @param _sampleFilterPos
	 * @param _markerPickerFile
	 * @param flipperFile
	 * @throws IOException
	 * @throws InvalidRangeException
	 */
	public MatrixGenotypesFlipper(int _studyId,
			int _rdMatrixId,
			String _wrMatrixFriendlyName,
			String _wrMatrixDescription,
			String _markerVariable,
			File _flipperFile) throws IOException, InvalidRangeException {

		/////////// INIT EXTRACTOR OBJECTS //////////


		rdMatrixId = _rdMatrixId;
		rdMatrixMetadata = new MatrixMetadata(rdMatrixId);
		studyId = rdMatrixMetadata.getStudyId();
		wrMatrixFriendlyName = _wrMatrixFriendlyName;
		wrMatrixDescription = _wrMatrixDescription;
		gtEncoding = GenotypeEncoding.compareTo(rdMatrixMetadata.getGenotypeEncoding());
		flipperFile = _flipperFile;

		rdMarkerSet = new MarkerSet_opt(rdMatrixMetadata.getStudyId(), rdMatrixId);
		rdMarkerSet.initFullMarkerIdSetLHM();
		rdMarkerIdSetLHM = rdMarkerSet.getMarkerIdSetLHM();

		rdChrInfoSetLHM = rdMarkerSet.getChrInfoSetLHM();

		rdSampleSet = new SampleSet(rdMatrixMetadata.getStudyId(), rdMatrixId);
		rdSampleSetLHM = rdSampleSet.getSampleIdSetLHM();

		if (flipperFile.isFile()) {
			FileReader fr = new FileReader(flipperFile);
			BufferedReader br = new BufferedReader(fr);
			String l;
			while ((l = br.readLine()) != null) {
				markerFlipHS.add(l);
			}
		}

	}

	public int flipGenotypesToNewMatrix() throws IOException {
		int resultMatrixId = Integer.MIN_VALUE;
		try {
			///////////// CREATE netCDF-3 FILE ////////////
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


			MatrixFactory wrMatrixHandler = new MatrixFactory(studyId,
					rdMatrixMetadata.getTechnology(), //technology
					wrMatrixFriendlyName,
					descSB.toString(), //description
					"FLP",
					rdMatrixMetadata.getHasDictionray(), //has dictionary?
					rdSampleSet.getSampleSetSize(),
					rdMarkerSet.getMarkerSetSize(),
					rdChrInfoSetLHM.size(),
					rdMatrixMetadata.getGenotypeEncoding(), //Matrix genotype encoding from orig matrix genotype encoding
					rdMatrixId, //Orig matrixId 1
					Integer.MIN_VALUE);         //Orig matrixId 2

			resultMatrixId = wrMatrixHandler.getResultMatrixId();

			NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());
			NetcdfFileWriteable wrNcFile = wrMatrixHandler.getNetCDFHandler();
			try {
				wrNcFile.create();
			} catch (IOException e) {
				System.err.println("ERROR creating file " + wrNcFile.getLocation() + "\n" + e);
			}
			//System.out.println("Done creating netCDF handle in MatrixataExtractor: " + org.gwaspi.global.Utils.getMediumDateTimeAsString());


			//<editor-fold defaultstate="collapsed" desc="METADATA WRITER">

			//////// WRITING METADATA TO MATRIX /////////

			//SAMPLESET
			ArrayChar.D2 samplesD2 = Utils.writeLHMKeysToD2ArrayChar(rdSampleSetLHM, cNetCDF.Strides.STRIDE_SAMPLE_NAME);

			int[] sampleOrig = new int[]{0, 0};
			try {
				wrNcFile.write(cNetCDF.Variables.VAR_SAMPLESET, sampleOrig, samplesD2);
			} catch (IOException e) {
				System.err.println("ERROR writing file");
			} catch (InvalidRangeException e) {
				e.printStackTrace();
			}
			samplesD2 = null;
			System.out.println("Done writing SampleSet to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());


			//MARKERSET MARKERID
			ArrayChar.D2 markersD2 = Utils.writeLHMKeysToD2ArrayChar(rdMarkerIdSetLHM, cNetCDF.Strides.STRIDE_MARKER_NAME);
			int[] markersOrig = new int[]{0, 0};
			try {
				wrNcFile.write(cNetCDF.Variables.VAR_MARKERSET, markersOrig, markersD2);
			} catch (IOException e) {
				System.err.println("ERROR writing file");
			} catch (InvalidRangeException e) {
				e.printStackTrace();
			}


			//MARKERSET RSID
			rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
			rdMarkerIdSetLHM = rdMarkerSet.replaceWithValuesFrom(rdMarkerIdSetLHM, rdMarkerSet.getMarkerIdSetLHM());
			Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdMarkerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);

			//MARKERSET CHROMOSOME
			rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
			rdMarkerIdSetLHM = rdMarkerSet.replaceWithValuesFrom(rdMarkerIdSetLHM, rdMarkerSet.getMarkerIdSetLHM());
			Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdMarkerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_CHR, cNetCDF.Strides.STRIDE_CHR);

			//Set of chromosomes found in matrix along with number of markersinfo
			org.gwaspi.netCDF.operations.Utils.saveCharLHMKeyToWrMatrix(wrNcFile, rdChrInfoSetLHM, cNetCDF.Variables.VAR_CHR_IN_MATRIX, 8);
			//Number of marker per chromosome & max pos for each chromosome
			int[] columns = new int[]{0, 1, 2, 3};
			org.gwaspi.netCDF.operations.Utils.saveIntLHMD2ToWrMatrix(wrNcFile, rdChrInfoSetLHM, columns, cNetCDF.Variables.VAR_CHR_INFO);


			//MARKERSET POSITION
			rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
			rdMarkerIdSetLHM = rdMarkerSet.replaceWithValuesFrom(rdMarkerIdSetLHM, rdMarkerSet.getMarkerIdSetLHM());
			//Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdMarkerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_POS, cNetCDF.Strides.STRIDE_POS);
			Utils.saveIntLHMD1ToWrMatrix(wrNcFile, rdMarkerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_POS);


			//MARKERSET DICTIONARY ALLELES
			rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_BASES_DICT);
			rdMarkerIdSetLHM = rdMarkerSet.replaceWithValuesFrom(rdMarkerIdSetLHM, rdMarkerSet.getMarkerIdSetLHM());
			for (Map.Entry<String, Object> entry : rdMarkerIdSetLHM.entrySet()) {
				String markerId = entry.getKey();
				if (markerFlipHS.contains(markerId)) {
					String alleles = entry.getValue().toString();
					alleles = flipDictionaryAlleles(alleles);
					entry.setValue(alleles);
				}
			}
			Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdMarkerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_BASES_DICT, cNetCDF.Strides.STRIDE_GT);

			//GENOTYPE STRAND
			rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_GT_STRAND);
			rdMarkerIdSetLHM = rdMarkerSet.replaceWithValuesFrom(rdMarkerIdSetLHM, rdMarkerSet.getMarkerIdSetLHM());

			for (Map.Entry<String, Object> entry : rdMarkerIdSetLHM.entrySet()) {
				String markerId = entry.getKey();
				if (markerFlipHS.contains(markerId)) {
					String strand = entry.getValue().toString();
					strand = flipStranding(strand);
					entry.setValue(strand);
				}
			}
			Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdMarkerIdSetLHM, cNetCDF.Variables.VAR_GT_STRAND, 3);

			//</editor-fold>

			//<editor-fold defaultstate="collapsed" desc="GENOTYPES WRITER">

			System.out.println(org.gwaspi.global.Text.All.processing);
			int markerIndex = 0;
			for (Map.Entry<String, Object> entry : rdMarkerIdSetLHM.entrySet()) {
				String markerId = entry.getKey();
				rdSampleSetLHM = rdSampleSet.readAllSamplesGTsFromCurrentMarkerToLHM(rdNcFile, rdSampleSetLHM, markerIndex);

				if (markerFlipHS.contains(markerId)) {
					for (Map.Entry<String, Object> sampleEntry : rdSampleSetLHM.entrySet()) {
						byte[] gt = (byte[]) sampleEntry.getValue();
						gt = flipGenotypes(gt, gtEncoding);
						sampleEntry.setValue(new byte[]{gt[0], gt[1]});
					}
				}

				//Write rdMarkerIdSetLHM to A3 ArrayChar and save to wrMatrix
				Utils.saveSingleMarkerGTsToMatrix(wrNcFile, rdSampleSetLHM, markerIndex);
				if (markerIndex % 10000 == 0) {
					System.out.println("Markers processed: " + markerIndex);
				}
				markerIndex++;
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
				DbManager db = ServiceLocator.getDbManager(org.gwaspi.constants.cDBGWASpi.DB_DATACENTER);
				db.updateTable(org.gwaspi.constants.cDBGWASpi.SCH_MATRICES,
						org.gwaspi.constants.cDBMatrix.T_MATRICES,
						new String[]{constants.cDBMatrix.f_DESCRIPTION},
						new Object[]{descSB.toString()},
						new String[]{constants.cDBMatrix.f_ID},
						new Object[]{resultMatrixId});

				wrNcFile.close();
			} catch (IOException e) {
				System.err.println("ERROR creating file " + wrNcFile.getLocation() + "\n" + e);
			}

			org.gwaspi.global.Utils.sysoutCompleted("Genotype Flipping to new Matrix");

		} catch (InvalidRangeException invalidRangeException) {
		} catch (IOException iOException) {
		}

		return resultMatrixId;
	}

	protected static String flipDictionaryAlleles(String alleles) {

		String flippedAlleles = alleles;
		
		flippedAlleles = flippedAlleles.replaceAll("A", "t");
		flippedAlleles = flippedAlleles.replaceAll("C", "g");
		flippedAlleles = flippedAlleles.replaceAll("G", "c");
		flippedAlleles = flippedAlleles.replaceAll("T", "a");
		flippedAlleles = flippedAlleles.toUpperCase();

		return flippedAlleles;
	}

	protected static String flipStranding(String strand) {
		if (strand.equals("+")) {
			return "-";
		} else if (strand.equals("-")) {
			return "+";
		} else {
			return strand;
		}
	}

	protected static byte[] flipGenotypes(byte[] gt, GenotypeEncoding gtEncoding) {
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
