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
import java.util.Iterator;
import java.util.LinkedHashMap;
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
	private HashSet markerFlipHS = new HashSet();
	private SampleSet rdSampleSet = null;
	private LinkedHashMap rdMarkerIdSetLHM = new LinkedHashMap();
	private LinkedHashMap rdSampleSetLHM = new LinkedHashMap();
	private LinkedHashMap rdChrInfoSetLHM = new LinkedHashMap();

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
			rdMarkerIdSetLHM = rdMarkerSet.fillWrLHMWithRdLHMValue(rdMarkerIdSetLHM, rdMarkerSet.getMarkerIdSetLHM());
			Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdMarkerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);

			//MARKERSET CHROMOSOME
			rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
			rdMarkerIdSetLHM = rdMarkerSet.fillWrLHMWithRdLHMValue(rdMarkerIdSetLHM, rdMarkerSet.getMarkerIdSetLHM());
			Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdMarkerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_CHR, cNetCDF.Strides.STRIDE_CHR);

			//Set of chromosomes found in matrix along with number of markersinfo
			org.gwaspi.netCDF.operations.Utils.saveCharLHMKeyToWrMatrix(wrNcFile, rdChrInfoSetLHM, cNetCDF.Variables.VAR_CHR_IN_MATRIX, 8);
			//Number of marker per chromosome & max pos for each chromosome
			int[] columns = new int[]{0, 1, 2, 3};
			org.gwaspi.netCDF.operations.Utils.saveIntLHMD2ToWrMatrix(wrNcFile, rdChrInfoSetLHM, columns, cNetCDF.Variables.VAR_CHR_INFO);


			//MARKERSET POSITION
			rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
			rdMarkerIdSetLHM = rdMarkerSet.fillWrLHMWithRdLHMValue(rdMarkerIdSetLHM, rdMarkerSet.getMarkerIdSetLHM());
			//Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdMarkerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_POS, cNetCDF.Strides.STRIDE_POS);
			Utils.saveIntLHMD1ToWrMatrix(wrNcFile, rdMarkerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_POS);


			//MARKERSET DICTIONARY ALLELES
			rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_BASES_DICT);
			rdMarkerIdSetLHM = rdMarkerSet.fillWrLHMWithRdLHMValue(rdMarkerIdSetLHM, rdMarkerSet.getMarkerIdSetLHM());
			for (Iterator it = rdMarkerIdSetLHM.keySet().iterator(); it.hasNext();) {
				Object markerId = it.next();
				if (markerFlipHS.contains(markerId)) {
					String alleles = rdMarkerIdSetLHM.get(markerId).toString();
					alleles = flipDictionaryAlleles(alleles);
					rdMarkerIdSetLHM.put(markerId, alleles);
				}
			}
			Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdMarkerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_BASES_DICT, cNetCDF.Strides.STRIDE_GT);

			//GENOTYPE STRAND
			rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_GT_STRAND);
			rdMarkerIdSetLHM = rdMarkerSet.fillWrLHMWithRdLHMValue(rdMarkerIdSetLHM, rdMarkerSet.getMarkerIdSetLHM());

			for (Iterator it = rdMarkerIdSetLHM.keySet().iterator(); it.hasNext();) {
				Object markerId = it.next();
				if (markerFlipHS.contains(markerId)) {
					String strand = rdMarkerIdSetLHM.get(markerId).toString();
					strand = flipStranding(strand);
					rdMarkerIdSetLHM.put(markerId, strand);
				}
			}
			Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdMarkerIdSetLHM, cNetCDF.Variables.VAR_GT_STRAND, 3);

			//</editor-fold>

			//<editor-fold defaultstate="collapsed" desc="GENOTYPES WRITER">

			System.out.println(org.gwaspi.global.Text.All.processing);
			int markerIndex = 0;
			for (Iterator it = rdMarkerIdSetLHM.keySet().iterator(); it.hasNext();) {
				Object markerId = it.next();
				rdSampleSetLHM = rdSampleSet.readAllSamplesGTsFromCurrentMarkerToLHM(rdNcFile, rdSampleSetLHM, markerIndex);

				if (markerFlipHS.contains(markerId)) {
					for (Iterator it2 = rdSampleSetLHM.keySet().iterator(); it2.hasNext();) {
						Object sampleId = it2.next();
						byte[] gt = (byte[]) rdSampleSetLHM.get(sampleId);
						gt = flipGenotypes(gt, gtEncoding);
						rdSampleSetLHM.put(sampleId, new byte[]{gt[0], gt[1]});
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

	/**
	 * This XXX has now been deprecated in favor of YYY
	 *
	 * @deprecated Use YYY instead
	 */
	public boolean forceAllelesToNewStrand(String newStrand) throws IOException {
		boolean result = false;

		NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());
		String rdMatrixType = rdMatrixMetadata.getGenotypeEncoding();

		if (!rdMatrixType.equals(cNetCDF.Defaults.GenotypeEncoding.ACGT0.toString())) { //Has not allready been translated
			try {
				///////////// CREATE netCDF-3 FILE ////////////
				MatrixFactory wrMatrixHandler = new MatrixFactory(studyId,
						rdMatrixMetadata.getTechnology(), //technology
						wrMatrixFriendlyName,
						wrMatrixDescription, //description
						newStrand,
						rdMatrixMetadata.getHasDictionray(), //has dictionary?
						rdSampleSet.getSampleSetSize(),
						rdMarkerSet.getMarkerSetSize(),
						888888,
						cNetCDF.Defaults.GenotypeEncoding.ACGT0.toString(), //New matrix genotype encoding
						rdMatrixId, //Orig matrixId 1
						Integer.MIN_VALUE);         //Orig matrixId 2

				NetcdfFileWriteable wrNcFile = wrMatrixHandler.getNetCDFHandler();
				try {
					wrNcFile.create();
				} catch (IOException e) {
					System.err.println("ERROR creating file " + wrNcFile.getLocation() + "\n" + e);
				}
				//System.out.println("Done creating netCDF handle in MatrixataTransform: " + org.gwaspi.global.Utils.getMediumDateTimeAsString());


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
				ArrayChar.D2 markersD2 = Utils.writeLHMKeysToD2ArrayChar(rdMarkerSet.getMarkerIdSetLHM(), cNetCDF.Strides.STRIDE_MARKER_NAME);
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
				Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdMarkerSet.getMarkerIdSetLHM(), cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);

				//MARKERSET CHROMOSOME
				rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
				Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdMarkerSet.getMarkerIdSetLHM(), cNetCDF.Variables.VAR_MARKERS_CHR, cNetCDF.Strides.STRIDE_CHR);

				//Set of chromosomes found in matrix along with number of markersinfo
				org.gwaspi.netCDF.operations.Utils.saveCharLHMKeyToWrMatrix(wrNcFile, rdChrInfoSetLHM, cNetCDF.Variables.VAR_CHR_IN_MATRIX, 8);
				//Number of marker per chromosome & max pos for each chromosome
				int[] columns = new int[]{0, 1};
				org.gwaspi.netCDF.operations.Utils.saveIntLHMD2ToWrMatrix(wrNcFile, rdChrInfoSetLHM, columns, cNetCDF.Variables.VAR_CHR_INFO);


				//MARKERSET POSITION
				rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
				//Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdMarkerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_POS, cNetCDF.Strides.STRIDE_POS);
				Utils.saveIntLHMD1ToWrMatrix(wrNcFile, rdMarkerSet.getMarkerIdSetLHM(), cNetCDF.Variables.VAR_MARKERS_POS);


				//MARKERSET DICTIONARY ALLELES
				rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_BASES_DICT);
				Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdMarkerSet.getMarkerIdSetLHM(), cNetCDF.Variables.VAR_MARKERS_BASES_DICT, cNetCDF.Strides.STRIDE_GT);

				//GENOTYPE STRAND
				for (Iterator it = rdMarkerSet.getMarkerIdSetLHM().keySet().iterator(); it.hasNext();) {
					Object key = it.next();
					rdMarkerSet.getMarkerIdSetLHM().put(key, newStrand);
				}
				Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdMarkerSet.getMarkerIdSetLHM(), cNetCDF.Variables.VAR_GT_STRAND, cNetCDF.Strides.STRIDE_STRAND);

				//</editor-fold>


				//<editor-fold defaultstate="collapsed" desc="GENOTYPES WRITER">

				//Get correct strand of each marker for newStrand translation
				LinkedHashMap markerStrandsLHM = new LinkedHashMap();
				markerStrandsLHM.putAll(rdSampleSetLHM);

				//Iterate through pmAllelesAndStrandsLHM, use Sample item position to read all Markers GTs from rdMarkerIdSetLHM.
				int sampleIndex = 0;
				for (int i = 0; i < rdSampleSetLHM.size(); i++) {
					//Get alleles from read matrix
					rdMarkerSet.fillGTsForCurrentSampleIntoInitLHM(sampleIndex);
					//Send to be flipped
					//wrMarkerIdSetLHM = forceStrandOfCurrentSampleAllelesLHM(rdMarkerSet.markerIdSetLHM, rdMatrixType, markerStrandsLHM, newStrand);

					//Write wrMarkerIdSetLHM to A3 ArrayChar and save to wrMatrix
					Utils.saveSingleSampleGTsToMatrix(wrNcFile, rdMarkerIdSetLHM, sampleIndex);

					System.out.println("Samples flipped:" + sampleIndex);

					sampleIndex++;
				}

				//</editor-fold>

				// CLOSE THE FILE AND BY THIS, MAKE IT READ-ONLY
				try {
					wrNcFile.close();
				} catch (IOException e) {
					System.err.println("ERROR creating file " + wrNcFile.getLocation() + "\n" + e);
				}

				org.gwaspi.global.Utils.sysoutCompleted("Strand Determination");

			} catch (InvalidRangeException invalidRangeException) {
			} catch (IOException iOException) {
			} finally {
				if (null != rdNcFile) {
					try {
						rdNcFile.close();
						result = true;
					} catch (IOException ioe) {
						System.out.println("Cannot close file: " + ioe);
					}
				}
			}
		}

		//If Matrix strand attribute == "+/-" && VAR_GT_STRAND != null
		//Iterate through all markers
		//If VAR_GT_STRAND field != newStrand
		//Translate A=>T, C=>T, G=>C, T=>A
		//Else if Matrix strand attribute != newStrand && VAR_GT_STRAND != null
		//Iterate through all markers
		//Translate A=>T, C=>T, G=>C, T=>A

		return result;
	}

	protected static String flipDictionaryAlleles(String alleles) {
		alleles = alleles.replaceAll("A", "t");
		alleles = alleles.replaceAll("C", "g");
		alleles = alleles.replaceAll("G", "c");
		alleles = alleles.replaceAll("T", "a");
		alleles = alleles.toUpperCase();
		return alleles;
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
