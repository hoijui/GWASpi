package org.gwaspi.samples;

import org.gwaspi.constants.cDBSamples;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.Annotation.GWASpi;
import org.gwaspi.constants.cImport.Annotation.Plink_LGEN;
import org.gwaspi.constants.cImport.Annotation.Sequenom;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gwaspi.netCDF.loader.LoadGTFromHapmapFiles;
import org.gwaspi.netCDF.matrices.MatrixMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.InvalidRangeException;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class SamplesParserManager {

	private final static Logger log
			= LoggerFactory.getLogger(SamplesParserManager.class);

	private SamplesParserManager() {
	}

	//<editor-fold defaultstate="collapsed" desc="DB SAMPLE INFO PROVIDERS">
	public static Set<Object> getDBAffectionStates(int matrixId) {
		Set<Object> resultHS = new HashSet<Object>();
		try {
			MatrixMetadata rdMatrixMetadata = new MatrixMetadata(matrixId);
			log.info("Getting Sample Affection info for: {} at {}",
					rdMatrixMetadata.getMatrixFriendlyName(),
					org.gwaspi.global.Utils.getMediumDateTimeAsString());
//			NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());
			SampleSet rdSampleSet = new SampleSet(rdMatrixMetadata.getStudyId(), matrixId);
			Map<String, Object> rdSampleSetMap = rdSampleSet.getSampleIdSetMap();
			for (String key : rdSampleSetMap.keySet()) {
				List<Map<String, Object>> rs = SampleManager.getCurrentSampleInfoFromDB(key.toString(), rdMatrixMetadata.getStudyId());
				if (rs != null) {
					// PREVENT PHANTOM-DB READS EXCEPTIONS
					if (!rs.isEmpty() && rs.get(0).size() == cDBSamples.T_CREATE_SAMPLES_INFO.length) {
						resultHS.add(rs.get(0).get(cDBSamples.f_AFFECTION));
					}
				}
			}
		} catch (InvalidRangeException ex) {
			log.error(null, ex);
		} catch (IOException ex) {
			log.error(null, ex);
		}
		return resultHS;
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="FILE SAMPLE INFO SCANNERS">
	public static Map<String, Object> scanGwaspiSampleInfo(String sampleInfoPath) throws IOException {
		Map<String, Object> sampleInfoMap = new LinkedHashMap<String, Object>();
		FileReader inputFileReader;
		BufferedReader inputBufferReader;
		File sampleFile = new File(sampleInfoPath);
		inputFileReader = new FileReader(sampleFile);
		inputBufferReader = new BufferedReader(inputFileReader);

		int count = 0;
		while (inputBufferReader.ready()) {
			String[] cVals = new String[10];
			if (count == 0) {
				inputBufferReader.readLine(); // Skip header
			} else {
				int i = 0;
				for (String field : inputBufferReader.readLine().split(cImport.Separators.separators_CommaSpaceTab_rgxp, 10)) {
					cVals[i] = field;
					i++;
				}
				sampleInfoMap.put(cVals[GWASpi.sampleId], cVals);
			}

			count++;
			if (count % 100 == 0) {
				log.info("Parsed {} Samples for info...", count);
			}
		}

		inputBufferReader.close();
		inputFileReader.close();

		return sampleInfoMap;
	}

	public static Map<String, Object> scanPlinkStandardSampleInfo(String pedPath) throws IOException {
		Map<String, Object> sampleInfoMap = new LinkedHashMap<String, Object>();
		FileReader inputFileReader;
		BufferedReader inputBufferReader;

		File sampleFile = new File(pedPath);
		inputFileReader = new FileReader(sampleFile);
		inputBufferReader = new BufferedReader(inputFileReader);

		char[] chunker = new char[300];
		inputBufferReader.read(chunker, 0, 300);
		if (String.valueOf(chunker).contains("\n")) { // SHORT PED FILE
			inputBufferReader.close();
			inputFileReader.close();
			inputFileReader = new FileReader(sampleFile);
			inputBufferReader = new BufferedReader(inputFileReader);

			int count = 0;
			while (inputBufferReader.ready()) {
				String l = inputBufferReader.readLine();
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
					cVals[9] = "0"; // AGE
				}

				sampleInfoMap.put(cVals[GWASpi.sampleId], cVals);

				count++;
				if (count % 100 == 0) {
					log.info("Parsed {} Samples for info...", count);
				}
			}
		} else { // LONG PED FILE
			// This has sucked out 1 week of my life and caused many grey hairs!
			int count = 0;
			while (inputBufferReader.ready()) {
				if (count != 0) {
					chunker = new char[300];
					inputBufferReader.read(chunker, 0, 300); // Read a sizable but conrolled chunk of data into memory
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
					cVals[9] = "0"; // AGE
				}
				inputBufferReader.readLine(); // Read rest of line and discard it...

				sampleInfoMap.put(cVals[GWASpi.sampleId], cVals);

				count++;
				if (count % 100 == 0) {
					log.info("Parsed {} Samples for info...", count);
				}
			}
		}

		inputBufferReader.close();
		inputFileReader.close();

		return sampleInfoMap;
	}

	public static Map<String, Object> scanPlinkLGENSampleInfo(String lgenPath) throws IOException {
		Map<String, Object> sampleInfoMap = new LinkedHashMap<String, Object>();

		File sampleFile = new File(lgenPath);
		FileReader inputFileReader = new FileReader(sampleFile);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		while (inputBufferReader.ready()) {
			String l = inputBufferReader.readLine();
			String[] cVals = l.split(cImport.Separators.separators_CommaSpaceTab_rgxp);
			String[] infoVals = new String[]{cVals[Plink_LGEN.lgen_familyId],
				cVals[Plink_LGEN.lgen_sampleId],
				"0", "0", "0", "0", "0", "0", "0", "0"};

			sampleInfoMap.put(cVals[Plink_LGEN.lgen_sampleId], infoVals);
		}
		log.info("Parsed {} Samples in LGEN file {}...",
				sampleInfoMap.size(), sampleFile.getName());

		inputBufferReader.close();
		inputFileReader.close();

		return sampleInfoMap;
	}

	public static Map<String, Object> scanPlinkFAMSampleInfo(String famPath) throws IOException {
		Map<String, Object> sampleInfoMap = new LinkedHashMap<String, Object>();
		FileReader inputFileReader = new FileReader(new File(famPath));
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		int count = 0;
		while (inputBufferReader.ready()) {
			String l = inputBufferReader.readLine();
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
			cVals[9] = "0"; // AGE

			sampleInfoMap.put(cVals[GWASpi.sampleId], cVals);

			count++;
			if (count % 100 == 0) {
				log.info("Parsed {} Samples for info...", count);
			}
		}

		inputBufferReader.close();
		inputFileReader.close();

		return sampleInfoMap;
	}

	public static Map<String, Object> scanIlluminaLGENSampleInfo(String lgenDir) throws IOException {
		Map<String, Object> sampleInfoMap = new LinkedHashMap<String, Object>();

		File[] gtFilesToImport = org.gwaspi.global.Utils.listFiles(lgenDir, false);

		for (int i = 0; i < gtFilesToImport.length; i++) {
			FileReader inputFileReader = new FileReader(gtFilesToImport[i]);
			BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

			boolean gotHeader = false;
			while (!gotHeader && inputBufferReader.ready()) {
				String header = inputBufferReader.readLine();
				if (header.startsWith("[Data]")) {
					/*header = */inputBufferReader.readLine(); // Get next line which is real header
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

				sampleInfoMap.put(cVals[Plink_LGEN.lgen_sampleId], infoVals);

				if (sampleInfoMap.size() % 100 == 0) {
					log.info("Parsed {} Samples...", sampleInfoMap.size());
				}
			}
			log.info("Parsed {} Samples in LGEN file {}...",
					sampleInfoMap.size(), gtFilesToImport[i].getName());

			inputBufferReader.close();
			inputFileReader.close();
		}

		return sampleInfoMap;
	}

	public static Map<String, Object> scanMultipleIlluminaLGENSampleInfo(String lgenDir) throws IOException {
		Map<String, Object> sampleInfoMap = new LinkedHashMap<String, Object>();
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

				sampleInfoMap.put(cVals[Plink_LGEN.lgen_sampleId], infoVals);
			}
			log.info("Parsed {} Samples in LGEN file {}...",
					sampleInfoMap.size(), currentLGENFile.getName());

			inputBufferReader.close();
			inputFileReader.close();
		}

		return sampleInfoMap;
	}

	public static Map<String, Object> scanBeagleSampleInfo(String beaglePath) throws IOException {
		Map<String, Object> sampleInfoMap = new LinkedHashMap<String, Object>();
		FileReader inputFileReader;
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
			sampleInfoMap.put(sampleIds[i], infoVals);
		}

		inputFileReader.close();

		return sampleInfoMap;
	}

	public static Map<String, Object> scanHapmapSampleInfo(String hapmapPath) throws IOException {
		Map<String, Object> sampleInfoMap = new LinkedHashMap<String, Object>();
		FileReader fr = null;
		BufferedReader inputAnnotationBr = null;
		File hapmapGTFile = new File(hapmapPath);
		if (hapmapGTFile.isDirectory()) {
			File[] gtFilesToImport = org.gwaspi.global.Utils.listFiles(hapmapPath, false);
			for (int i = 0; i < gtFilesToImport.length; i++) {
				fr = new FileReader(gtFilesToImport[i]);
				inputAnnotationBr = new BufferedReader(fr);

				String header = inputAnnotationBr.readLine();

				String[] hapmapVals = header.split(cImport.Separators.separators_SpaceTab_rgxp);
				for (int j = LoadGTFromHapmapFiles.Standard.sampleId; j < hapmapVals.length; j++) {
					String[] infoVals = new String[]{"0", "0", "0", "0", "0", "0", "0", "0", "0", "0"};
					infoVals[GWASpi.sampleId] = hapmapVals[j];
					sampleInfoMap.put(hapmapVals[j], infoVals);
				}
			}
		} else {
			fr = new FileReader(hapmapPath);
			inputAnnotationBr = new BufferedReader(fr);

			String header = inputAnnotationBr.readLine();

			String[] hapmapVals = header.split(cImport.Separators.separators_SpaceTab_rgxp);
			for (int i = LoadGTFromHapmapFiles.Standard.sampleId; i < hapmapVals.length; i++) {
				String[] infoVals = new String[]{"0", "0", "0", "0", "0", "0", "0", "0", "0", "0"};
				infoVals[GWASpi.sampleId] = hapmapVals[i];
				sampleInfoMap.put(hapmapVals[i], infoVals);
			}
		}

		inputAnnotationBr.close();
		fr.close();

		return sampleInfoMap;
	}

	public static Map<String, Object> scanHGDP1SampleInfo(String hgdpPath) throws IOException {
		Map<String, Object> sampleInfoMap = new LinkedHashMap<String, Object>();
		File sampleFile = new File(hgdpPath);
		FileReader inputFileReader = new FileReader(sampleFile);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		String sampleIdHeader = inputBufferReader.readLine();

		String[] sampleIds = sampleIdHeader.split(cImport.Separators.separators_SpaceTab_rgxp);
		for (int i = 1; i < sampleIds.length; i++) {
			String sampleId = sampleIds[i];
			String[] infoVals = new String[]{"0", "0", "0", "0", "0", "0", "0", "0", "0", "0"};
			infoVals[GWASpi.sampleId] = sampleId;
			sampleInfoMap.put(sampleId, infoVals);
		}

		inputFileReader.close();

		return sampleInfoMap;
	}

	public static Map<String, Object> scanAffymetrixSampleInfo(String genotypesPath) throws IOException {
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		File[] gtFilesToImport = org.gwaspi.global.Utils.listFiles(genotypesPath, false);

		for (int i = 0; i < gtFilesToImport.length; i++) {
			String l = gtFilesToImport[i].getName();
			String sampleId;
			int end = l.lastIndexOf(".birdseed-v2");
			if (end != -1) {
				sampleId = l.substring(0, end);
			} else {
				sampleId = l.substring(0, l.lastIndexOf('.'));
			}
			String[] infoVals = new String[]{"0", "0", "0", "0", "0", "0", "0", "0", "0", "0"};
			infoVals[GWASpi.sampleId] = sampleId;
			resultMap.put(sampleId, infoVals);
		}
		return resultMap;
	}

	public static Map<String, Object> scanSequenomSampleInfo(String genotypePath) throws IOException {
		Map<String, Object> sampleInfoMap = new LinkedHashMap<String, Object>();

		File gtFileToImport = new File(genotypePath);
		FileReader inputFileReader = new FileReader(gtFileToImport);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		String l;
		while (inputBufferReader.ready()) {
			l = inputBufferReader.readLine();
			if (!l.contains("SAMPLE_ID")) { //SKIP ALL HEADER LINES
				String[] cVals = l.split(cImport.Separators.separators_CommaSpaceTab_rgxp);
				if (!sampleInfoMap.containsKey(cVals[Sequenom.sampleId])) {
					String[] infoVals = new String[]{"0",
						cVals[Sequenom.sampleId],
						"0", "0", "0", "0", "0", "0", "0", "0"};
					sampleInfoMap.put(cVals[Sequenom.sampleId], infoVals);
				}

				if (sampleInfoMap.size() % 100 == 0) {
					log.info("Parsed {} lines...", sampleInfoMap.size());
				}
			}

		}
		log.info("Parsed {} Samples in Sequenom file {}...",
				sampleInfoMap.size(), gtFileToImport);

		inputBufferReader.close();
		inputFileReader.close();

		return sampleInfoMap;
	}

	public static Set<String> scanSampleInfoAffectionStates(String sampleInfoPath) throws IOException {
		Set<String> resultHS = new HashSet<String>();

		File sampleFile = new File(sampleInfoPath);
		FileReader inputFileReader = new FileReader(sampleFile);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		String header = inputBufferReader.readLine(); // ignore header block
		String l;
		while ((l = inputBufferReader.readLine()) != null) {
			String[] cVals = l.split(cImport.Separators.separators_CommaSpaceTab_rgxp);
			resultHS.add(cVals[GWASpi.affection]);
		}

		inputFileReader.close();

		return resultHS;
	}
	//</editor-fold>
}
