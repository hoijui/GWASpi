package org.gwaspi.netCDF.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.Annotation.Plink_Standard;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.global.Text;
import org.gwaspi.samples.DummySampleInfo;
import org.gwaspi.samples.SamplesParserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class SampleInfoCollectorSwitch {

	private static final Logger log = LoggerFactory.getLogger(SampleInfoCollectorSwitch.class);

	private SampleInfoCollectorSwitch() {
	}

	private static void checkMissingSampleInfo(
			Map<String, Object> dummySampleInfo,
			Map<String, Object> sampleInfo)
	{
		for (String sampleId : dummySampleInfo.keySet()) {
			if (!sampleInfo.containsKey(sampleId)) {
				Object[] dummySampleValues = DummySampleInfo.createDummySampleValues();
				dummySampleValues[1] = sampleId.toString();
				sampleInfo.put(sampleId, dummySampleValues);
				log.warn(Text.Study.warnMissingSampleInfo);
				log.warn("SampleID: {}", sampleId.toString());
			}
		}
	}

	private static boolean checkIsPlinkFAMFile(String sampleInfoPath)
			throws IOException
	{
		FileReader inputFileReader = new FileReader(new File(sampleInfoPath));
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);
		String header = inputBufferReader.readLine();
		String[] cVals = header.split(cImport.Separators.separators_CommaSpaceTab_rgxp);

		return (cVals.length == 6);
	}

	public static Map<String, Object> collectSampleInfo(
			ImportFormat format,
			boolean dummySamples,
			String sampleInfoPath,
			String altSampleInfoPath1,
			String altSampleInfoPath2)
			throws IOException
	{
		Map<String, Object> sampleInfoMap;

		switch (format) {
			case Affymetrix_GenomeWide6:
				log.info(Text.Matrix.scanAffectionStandby);
				if (dummySamples) {
					sampleInfoMap = SamplesParserManager.scanAffymetrixSampleInfo(altSampleInfoPath2);
				} else {
					Map<String, Object> dummySamplesInfoMap = SamplesParserManager.scanAffymetrixSampleInfo(altSampleInfoPath2);
					sampleInfoMap = SamplesParserManager.scanGwaspiSampleInfo(sampleInfoPath);
					checkMissingSampleInfo(dummySamplesInfoMap, sampleInfoMap);
				}
				break;
			case PLINK:
				log.info(Text.Matrix.scanAffectionStandby);
				if (dummySamples) {
					sampleInfoMap = SamplesParserManager.scanPlinkStandardSampleInfo(altSampleInfoPath2);
				} else {
					Map<String, Object> dummySamplesInfoMap = SamplesParserManager.scanPlinkStandardSampleInfo(altSampleInfoPath2);
					sampleInfoMap = SamplesParserManager.scanGwaspiSampleInfo(sampleInfoPath);
					checkMissingSampleInfo(dummySamplesInfoMap, sampleInfoMap);
				}
				break;
			case PLINK_Binary:
				log.info(Text.Matrix.scanAffectionStandby);
				if (checkIsPlinkFAMFile(sampleInfoPath)) {
					sampleInfoMap = SamplesParserManager.scanPlinkFAMSampleInfo(sampleInfoPath);
				} else {
					// It is a SampleInfo file
					sampleInfoMap = SamplesParserManager.scanGwaspiSampleInfo(sampleInfoPath);
				}
				break;
			case HAPMAP:
				log.info(Text.Matrix.scanAffectionStandby);
				if (dummySamples) {
					sampleInfoMap = SamplesParserManager.scanHapmapSampleInfo(altSampleInfoPath1);
				} else {
					Map<String, Object> dummySamplesInfoMap = SamplesParserManager.scanHapmapSampleInfo(altSampleInfoPath1);
					sampleInfoMap = SamplesParserManager.scanGwaspiSampleInfo(sampleInfoPath);
					checkMissingSampleInfo(dummySamplesInfoMap, sampleInfoMap);
				}
				break;
			case BEAGLE:
				log.info(Text.Matrix.scanAffectionStandby);
				if (dummySamples) {
					sampleInfoMap = SamplesParserManager.scanBeagleSampleInfo(altSampleInfoPath1);
				} else {
					Map<String, Object> dummySamplesInfoMap = SamplesParserManager.scanBeagleSampleInfo(altSampleInfoPath1);
					sampleInfoMap = SamplesParserManager.scanGwaspiSampleInfo(sampleInfoPath);
					checkMissingSampleInfo(dummySamplesInfoMap, sampleInfoMap);
				}
				break;
			case HGDP1:
				log.info(Text.Matrix.scanAffectionStandby);
				if (dummySamples) {
					sampleInfoMap = SamplesParserManager.scanHGDP1SampleInfo(altSampleInfoPath1);
				} else {
					Map<String, Object> dummySamplesInfoMap = SamplesParserManager.scanHGDP1SampleInfo(altSampleInfoPath1);
					sampleInfoMap = SamplesParserManager.scanGwaspiSampleInfo(sampleInfoPath);
					checkMissingSampleInfo(dummySamplesInfoMap, sampleInfoMap);
				}
				break;
			case GWASpi:
				sampleInfoMap = SamplesParserManager.scanGwaspiSampleInfo(sampleInfoPath);
				break;
			case Illumina_LGEN:
				log.info(Text.Matrix.scanAffectionStandby);
				if (dummySamples) {
					sampleInfoMap = SamplesParserManager.scanIlluminaLGENSampleInfo(altSampleInfoPath2);
				} else {
					Map<String, Object> dummySamplesInfoMap = SamplesParserManager.scanIlluminaLGENSampleInfo(altSampleInfoPath2);
					sampleInfoMap = SamplesParserManager.scanGwaspiSampleInfo(sampleInfoPath);
					checkMissingSampleInfo(dummySamplesInfoMap, sampleInfoMap);
				}
				break;
			case Sequenom:
				sampleInfoMap = SamplesParserManager.scanGwaspiSampleInfo(sampleInfoPath);
				break;
			default:
				sampleInfoMap = new LinkedHashMap<String, Object>();
		}

		return sampleInfoMap;
	}

	public static Set<String> collectAffectionStates(Map<String, Object> sampleInfoMap) {
		Set<String> affectionStates = new HashSet<String>();
		for (Object value : sampleInfoMap.values()) {
			Object[] values = (Object[]) value;
			String affection = values[Plink_Standard.ped_affection].toString();
			if (!affection.equals("1") && !affection.equals("2")) {
				affection = "0";
			}
			affectionStates.add(affection);
		}
		return affectionStates;
	}
}
