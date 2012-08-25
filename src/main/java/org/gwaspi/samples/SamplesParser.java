package org.gwaspi.samples;

import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.Annotation.GWASpi;
import org.gwaspi.constants.cImport.Annotation.Plink_LGEN;
import org.gwaspi.constants.cImport.Annotation.Sequenom;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gwaspi.netCDF.matrices.MatrixMetadata;
import ucar.ma2.InvalidRangeException;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class SamplesParser {

	private SamplesParser() {
	}

	//<editor-fold defaultstate="collapsed" desc="DB SAMPLE INFO PROVIDERS">
	public static HashSet getDBAffectionStates(int matrixId) {
		HashSet resultHS = new HashSet();
		try {
			MatrixMetadata rdMatrixMetadata = new MatrixMetadata(matrixId);
			System.out.println("Getting Sample Affection info for: " + rdMatrixMetadata.getMatrixFriendlyName() + " at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
//            NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());
			SampleSet rdSampleSet = new SampleSet(rdMatrixMetadata.getStudyId(), matrixId);
			Map<String, Object> rdSampleSetLHM = rdSampleSet.getSampleIdSetLHM();
			for (Iterator it = rdSampleSetLHM.keySet().iterator(); it.hasNext();) {
				Object key = it.next();
				List<Map<String, Object>> rs = SampleManager.getCurrentSampleInfoFromDB(key.toString(), rdMatrixMetadata.getStudyId());
				if (rs != null) {
					//PREVENT PHANTOM-DB READS EXCEPTIONS
					if (!rs.isEmpty() && rs.get(0).size() == org.gwaspi.constants.cDBSamples.T_CREATE_SAMPLES_INFO.length) {
						resultHS.add(rs.get(0).get(org.gwaspi.constants.cDBSamples.f_AFFECTION));
					}
				}
			}
		} catch (InvalidRangeException ex) {
			Logger.getLogger(SamplesParser.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(SamplesParser.class.getName()).log(Level.SEVERE, null, ex);
		}
		return resultHS;
	}

	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="FILE SAMPLE INFO SCANNERS">
	public static LinkedHashMap scanGwaspiSampleInfo(String sampleInfoPath) throws IOException {
		LinkedHashMap sampleInfoLHM = new LinkedHashMap();
		FileReader inputFileReader = null;
		BufferedReader inputBufferReader = null;
//        try {
		File sampleFile = new File(sampleInfoPath);
		inputFileReader = new FileReader(sampleFile);
		inputBufferReader = new BufferedReader(inputFileReader);

		int count = 0;
		while (inputBufferReader.ready()) {
			String[] cVals = new String[10];
			if (count == 0) {
				inputBufferReader.readLine(); //Skip header
			} else {
				int i = 0;
				for (String field : inputBufferReader.readLine().split(cImport.Separators.separators_CommaSpaceTab_rgxp, 10)) {
					cVals[i] = field;
					i++;
				}
				sampleInfoLHM.put(cVals[GWASpi.sampleId], cVals);
			}

			count++;
			if (count % 100 == 0) {
				System.out.println("Parsed " + count + " Samples for info...");
			}
		}

		inputBufferReader.close();
		inputFileReader.close();

//        } catch (IOException ex) {
//            Logger.getLogger(SamplesParser.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            try {
//                inputBufferReader.close();
//                inputFileReader.close();
//            } catch (IOException ex) {
//                Logger.getLogger(SamplesParser.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }

		return sampleInfoLHM;
	}

	public static LinkedHashMap scanPlinkStandardSampleInfo(String pedPath) throws IOException {
		LinkedHashMap sampleInfoLHM = new LinkedHashMap();
		FileReader inputFileReader = null;
		BufferedReader inputBufferReader = null;
//        try {

		File sampleFile = new File(pedPath);
		inputFileReader = new FileReader(sampleFile);
		inputBufferReader = new BufferedReader(inputFileReader);

		char[] chunker = new char[300];
		inputBufferReader.read(chunker, 0, 300);
		if (String.valueOf(chunker).contains("\n")) { //SHORT PED FILE
			inputBufferReader.close();
			inputFileReader.close();
			inputFileReader = new FileReader(sampleFile);
			inputBufferReader = new BufferedReader(inputFileReader);

			int count = 0;
			String l;
			while (inputBufferReader.ready()) {
				l = inputBufferReader.readLine();
				String[] cVals = new String[10];
				if (chunker.length > 0) {
					cVals = l.split(cImport.Separators.separators_CommaSpaceTab_rgxp, 10);
					String sex = (cVals[4].equals("-9")) ? "0" : cVals[4];
					String affection = (cVals[5].equals("-9")) ? "0" : cVals[5];
					cVals[4] = sex;
					cVals[5] = affection;
					cVals[6] = "0"; //
					cVals[7] = "0";
					cVals[8] = "0";
					cVals[9] = "0"; //AGE
				}

				sampleInfoLHM.put(cVals[GWASpi.sampleId], cVals);

				count++;
				if (count % 100 == 0) {
					System.out.println("Parsed " + count + " Samples for info...");
				}
			}
		} else { //LONG PED FILE
			//This has sucked out 1 week of my life and caused many grey hairs!
			int count = 0;
			while (inputBufferReader.ready()) {
				if (count != 0) {
					chunker = new char[300];
					inputBufferReader.read(chunker, 0, 300); //Read a sizable but conrolled chunk of data into memory
				}

				String[] cVals = new String[10];
				if (chunker.length > 0) {
					cVals = String.valueOf(chunker).split(cImport.Separators.separators_CommaSpaceTab_rgxp, 10);
					String sex = (cVals[4].equals("-9")) ? "0" : cVals[4];
					String affection = (cVals[5].equals("-9")) ? "0" : cVals[5];
					cVals[4] = sex;
					cVals[5] = affection;
					cVals[6] = "0"; //
					cVals[7] = "0";
					cVals[8] = "0";
					cVals[9] = "0"; //AGE
				}
				inputBufferReader.readLine(); //Read rest of line and discard it...

				sampleInfoLHM.put(cVals[GWASpi.sampleId], cVals);

				count++;
				if (count % 100 == 0) {
					System.out.println("Parsed " + count + " Samples for info...");
				}
			}
		}

		inputBufferReader.close();
		inputFileReader.close();

//        } catch (IOException ex) {
//            Logger.getLogger(SamplesParser.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            try {
//                inputBufferReader.close();
//                inputFileReader.close();
//            } catch (IOException ex) {
//                Logger.getLogger(SamplesParser.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }

		return sampleInfoLHM;
	}

	public static LinkedHashMap scanPlinkLGENSampleInfo(String lgenPath) throws IOException {
		LinkedHashMap sampleInfoLHM = new LinkedHashMap();

		File sampleFile = new File(lgenPath);
		FileReader inputFileReader = new FileReader(sampleFile);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		String l;
		while (inputBufferReader.ready()) {
			l = inputBufferReader.readLine();
			String[] cVals = l.split(cImport.Separators.separators_CommaSpaceTab_rgxp);
			String[] infoVals = new String[]{cVals[Plink_LGEN.lgen_familyId],
				cVals[Plink_LGEN.lgen_sampleId],
				"0", "0", "0", "0", "0", "0", "0", "0"};

			sampleInfoLHM.put(cVals[Plink_LGEN.lgen_sampleId], infoVals);
		}
		System.out.println("Parsed " + sampleInfoLHM.size() + " Samples in LGEN file " + sampleFile.getName() + "...");

		inputBufferReader.close();
		inputFileReader.close();

		return sampleInfoLHM;
	}

	public static LinkedHashMap scanPlinkFAMSampleInfo(String famPath) throws IOException {
		LinkedHashMap sampleInfoLHM = new LinkedHashMap();
		FileReader inputFileReader = new FileReader(new File(famPath));
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		int count = 0;
		String l;
		while (inputBufferReader.ready()) {
			l = inputBufferReader.readLine();
			String[] cVals = new String[10];
			String[] sampleInfoVals = l.split(cImport.Separators.separators_CommaSpaceTab_rgxp);

			String sex = (sampleInfoVals[4].equals("-9")) ? "0" : sampleInfoVals[4];
			String affection = (sampleInfoVals[5].equals("-9")) ? "0" : sampleInfoVals[5];

			cVals[0] = sampleInfoVals[0]; //
			cVals[1] = sampleInfoVals[1]; //
			cVals[2] = sampleInfoVals[2]; //
			cVals[3] = sampleInfoVals[3]; //
			cVals[4] = sex; //
			cVals[5] = affection; //
			cVals[6] = "0"; //
			cVals[7] = "0";
			cVals[8] = "0";
			cVals[9] = "0"; //AGE

			sampleInfoLHM.put(cVals[GWASpi.sampleId], cVals);

			count++;
			if (count % 100 == 0) {
				System.out.println("Parsed " + count + " Samples for info...");
			}
		}


		inputBufferReader.close();
		inputFileReader.close();

		return sampleInfoLHM;
	}

	public static LinkedHashMap scanIlluminaLGENSampleInfo(String lgenDir) throws IOException {
		LinkedHashMap sampleInfoLHM = new LinkedHashMap();

		File[] gtFilesToImport = org.gwaspi.global.Utils.listFiles(lgenDir, false);

		for (int i = 0; i < gtFilesToImport.length; i++) {
			FileReader inputFileReader = new FileReader(gtFilesToImport[i]);
			BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

			String header = "";
			boolean gotHeader = false;
			while (!gotHeader && inputBufferReader.ready()) {
				header = inputBufferReader.readLine();
				if (header.startsWith("[Data]")) {
					header = inputBufferReader.readLine(); //Get next line which is real header
					gotHeader = true;
				}
			}

			String l;
			while (inputBufferReader.ready()) {
				l = inputBufferReader.readLine();
				String[] cVals = l.split(cImport.Separators.separators_CommaSpaceTab_rgxp);
				String[] infoVals = new String[]{cVals[Plink_LGEN.lgen_familyId],
					cVals[Plink_LGEN.lgen_sampleId],
					"0", "0", "0", "0", "0", "0", "0", "0"};

				sampleInfoLHM.put(cVals[Plink_LGEN.lgen_sampleId], infoVals);

				if (sampleInfoLHM.size() % 100 == 0) {
					System.out.println("Parsed " + sampleInfoLHM.size() + " Samples...");
				}
			}
			System.out.println("Parsed " + sampleInfoLHM.size() + " Samples in LGEN file " + gtFilesToImport[i].getName() + "...");

			inputBufferReader.close();
			inputFileReader.close();
		}

		return sampleInfoLHM;
	}

	public static LinkedHashMap scanMultipleIlluminaLGENSampleInfo(String lgenDir) throws IOException {
		LinkedHashMap sampleInfoLHM = new LinkedHashMap();
		File[] lgenFilesToScan = org.gwaspi.global.Utils.listFiles(lgenDir, false);

		for (File currentLGENFile : lgenFilesToScan) {
			FileReader inputFileReader = new FileReader(currentLGENFile);
			BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

			boolean gotHeader = false;
			while (!gotHeader) {
				String header = inputBufferReader.readLine();
				if (header == null) {
					break;
				}
				if (header.startsWith("[Data]")) {
					/*header = */inputBufferReader.readLine(); // get the next line, which is the real header
					gotHeader = true;
				}
			}

			String l;
			while (inputBufferReader.ready()) {
				l = inputBufferReader.readLine();
				String[] cVals = l.split(cImport.Separators.separators_CommaSpaceTab_rgxp);
				String[] infoVals = new String[]{cVals[Plink_LGEN.lgen_familyId],
					cVals[Plink_LGEN.lgen_sampleId],
					"0", "0", "0", "0", "0", "0", "0", "0"};

				sampleInfoLHM.put(cVals[Plink_LGEN.lgen_sampleId], infoVals);
			}
			System.out.println("Parsed " + sampleInfoLHM.size() + " Samples in LGEN file " + currentLGENFile.getName() + "...");

			inputBufferReader.close();
			inputFileReader.close();
		}

		return sampleInfoLHM;
	}

	public static LinkedHashMap scanBeagleSampleInfo(String beaglePath) throws IOException {
		LinkedHashMap sampleInfoLHM = new LinkedHashMap();
		FileReader inputFileReader;
//        try {
		File sampleFile = new File(beaglePath);
		inputFileReader = new FileReader(sampleFile);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		String sampleIdHeader = "";
		String affectionHeader = "";
		boolean gotAffection = false;
		while (!gotAffection) {
			String l = inputBufferReader.readLine();
			if (l == null) {
				break;
			}
			if (l.startsWith("I")) {
				sampleIdHeader = l;
			}
			if (l.startsWith("A")) {
				affectionHeader = l;
				gotAffection = true;
			}
		}

		String[] sampleIds = sampleIdHeader.split(cImport.Separators.separators_SpaceTab_rgxp);
		String[] beagleAffections = affectionHeader.split(cImport.Separators.separators_SpaceTab_rgxp);

		for (int i = 2; i < beagleAffections.length; i++) {
			String[] infoVals = new String[]{"0", "0", "0", "0", "0", "0", "0", "0", "0", "0"};
			infoVals[GWASpi.sampleId] = sampleIds[i];
			infoVals[GWASpi.affection] = beagleAffections[i];
			sampleInfoLHM.put(sampleIds[i], infoVals);
		}

		inputFileReader.close();

//        } catch (IOException ex) {
//            Logger.getLogger(SamplesParser.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            try {
//                inputFileReader.close();
//            } catch (IOException ex) {
//                Logger.getLogger(SamplesParser.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }

		return sampleInfoLHM;
	}

	public static LinkedHashMap scanHapmapSampleInfo(String hapmapPath) throws IOException {
		LinkedHashMap sampleInfoLHM = new LinkedHashMap();
		FileReader fr = null;
		BufferedReader inputAnnotationBr = null;
		//        try {

		File hapmapGTFile = new File(hapmapPath);
		if (hapmapGTFile.isDirectory()) {
			File[] gtFilesToImport = org.gwaspi.global.Utils.listFiles(hapmapPath, false);
			for (int i = 0; i < gtFilesToImport.length; i++) {
				fr = new FileReader(gtFilesToImport[i]);
				inputAnnotationBr = new BufferedReader(fr);

				String header = inputAnnotationBr.readLine();

				String[] hapmapVals = header.split(cImport.Separators.separators_SpaceTab_rgxp);
				for (int j = cImport.Genotypes.Hapmap_Standard.sampleId; j < hapmapVals.length; j++) {
					String[] infoVals = new String[]{"0", "0", "0", "0", "0", "0", "0", "0", "0", "0"};
					infoVals[GWASpi.sampleId] = hapmapVals[j];
					sampleInfoLHM.put(hapmapVals[j], infoVals);
				}
			}
		} else {
			fr = new FileReader(hapmapPath);
			inputAnnotationBr = new BufferedReader(fr);

			String header = inputAnnotationBr.readLine();

			String[] hapmapVals = header.split(cImport.Separators.separators_SpaceTab_rgxp);
			for (int i = cImport.Genotypes.Hapmap_Standard.sampleId; i < hapmapVals.length; i++) {
				String[] infoVals = new String[]{"0", "0", "0", "0", "0", "0", "0", "0", "0", "0"};
				infoVals[GWASpi.sampleId] = hapmapVals[i];
				sampleInfoLHM.put(hapmapVals[i], infoVals);
			}
		}

		inputAnnotationBr.close();
		fr.close();

//        } catch (IOException ex) {
//            Logger.getLogger(SamplesParser.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            try {
//                inputAnnotationBr.close();
//                fr.close();
//            } catch (IOException ex) {
//                Logger.getLogger(SamplesParser.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }

		return sampleInfoLHM;
	}

	public static LinkedHashMap scanHGDP1SampleInfo(String hgdpPath) throws IOException {
		LinkedHashMap sampleInfoLHM = new LinkedHashMap();
		FileReader inputFileReader = null;
//        try {
		File sampleFile = new File(hgdpPath);
		inputFileReader = new FileReader(sampleFile);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		String l;
		String sampleIdHeader = inputBufferReader.readLine();

		String[] sampleIds = sampleIdHeader.split(cImport.Separators.separators_SpaceTab_rgxp);
		for (int i = 1; i < sampleIds.length; i++) {
			String sampleId = sampleIds[i];
			String[] infoVals = new String[]{"0", "0", "0", "0", "0", "0", "0", "0", "0", "0"};
			infoVals[GWASpi.sampleId] = sampleId;
			sampleInfoLHM.put(sampleId, infoVals);
		}

		inputFileReader.close();

//        } catch (IOException ex) {
//            Logger.getLogger(SamplesParser.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            try {
//                inputFileReader.close();
//            } catch (IOException ex) {
//                Logger.getLogger(SamplesParser.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }

		return sampleInfoLHM;
	}

	public static LinkedHashMap scanAffymetrixSampleInfo(String genotypesPath) throws IOException {
		LinkedHashMap resultLHM = new LinkedHashMap();
		File[] gtFilesToImport = org.gwaspi.global.Utils.listFiles(genotypesPath, false);

		for (int i = 0; i < gtFilesToImport.length; i++) {
			String l = gtFilesToImport[i].getName();
			String sampleId = l;
			int end = l.lastIndexOf(".birdseed-v2");
			if (end != -1) {
				sampleId = l.substring(0, end);
			} else {
				sampleId = l.substring(0, l.lastIndexOf("."));
			}
			String[] infoVals = new String[]{"0", "0", "0", "0", "0", "0", "0", "0", "0", "0"};
			infoVals[GWASpi.sampleId] = sampleId;
			resultLHM.put(sampleId, infoVals);
		}
		return resultLHM;
	}

	public static LinkedHashMap scanSequenomSampleInfo(String genotypePath) throws IOException {
		LinkedHashMap sampleInfoLHM = new LinkedHashMap();

		File gtFileToImport = new File(genotypePath);
		FileReader inputFileReader = new FileReader(gtFileToImport);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		String l;
		while (inputBufferReader.ready()) {
			l = inputBufferReader.readLine();
			if (!l.contains("SAMPLE_ID")) { //SKIP ALL HEADER LINES
				String[] cVals = l.split(cImport.Separators.separators_CommaSpaceTab_rgxp);
				if (!sampleInfoLHM.containsKey(cVals[Sequenom.sampleId])) {
					String[] infoVals = new String[]{"0",
						cVals[Sequenom.sampleId],
						"0", "0", "0", "0", "0", "0", "0", "0"};
					sampleInfoLHM.put(cVals[Sequenom.sampleId], infoVals);
				}

				if (sampleInfoLHM.size() % 100 == 0) {
					System.out.println("Parsed " + sampleInfoLHM.size() + " lines...");
				}
			}

		}
		System.out.println("Parsed " + sampleInfoLHM.size() + " Samples in Sequenom file " + gtFileToImport + "...");

		inputBufferReader.close();
		inputFileReader.close();

		return sampleInfoLHM;
	}

	public static HashSet scanSampleInfoAffectionStates(String sampleInfoPath) throws IOException {
		HashSet resultHS = new HashSet();
		FileReader inputFileReader = null;

		File sampleFile = new File(sampleInfoPath);
		inputFileReader = new FileReader(sampleFile);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		String header = inputBufferReader.readLine(); //ignore header block
		String l;
		while ((l = inputBufferReader.readLine()) != null) {
			String[] cVals = l.split(org.gwaspi.constants.cImport.Separators.separators_CommaSpaceTab_rgxp);
			resultHS.add(cVals[GWASpi.affection]);
		}

		inputFileReader.close();

		return resultHS;
	}
	//</editor-fold>
}
