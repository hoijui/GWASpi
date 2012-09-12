package org.gwaspi.netCDF.loader;

import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.Annotation.Plink_Standard;
import org.gwaspi.global.Text;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gwaspi.samples.SamplesParser;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class SampleInfoCollectorSwitch {

	private final static Logger log = LoggerFactory.getLogger(SampleInfoCollectorSwitch.class);

	private SampleInfoCollectorSwitch() {
	}

	public static Map<String, Object> collectSampleInfo(String format,
			boolean dummySamples,
			String sampleInfoPath,
			String altSampleInfoPath1,
			String altSampleInfoPath2) throws IOException {
		Map<String, Object> sampleInfoLHM = new LinkedHashMap<String, Object>();
		Object[] dummySampleValues = org.gwaspi.samples.DummySampleInfo.createDummySampleValues();

		switch (cImport.ImportFormat.compareTo(format)) {
			case Affymetrix_GenomeWide6:
				log.info(Text.Matrix.scanAffectionStandby);
				if (dummySamples) {
					sampleInfoLHM = SamplesParser.scanAffymetrixSampleInfo(altSampleInfoPath2);
				} else {
					Map<String, Object> dummySamplesInfoLHM = SamplesParser.scanAffymetrixSampleInfo(altSampleInfoPath2);
					sampleInfoLHM = SamplesParser.scanGwaspiSampleInfo(sampleInfoPath);

					for (String sampleId : dummySamplesInfoLHM.keySet()) {
						if (!sampleInfoLHM.containsKey(sampleId)) {
							dummySampleValues[1] = sampleId.toString();
							sampleInfoLHM.put(sampleId, dummySampleValues);
							log.warn(Text.Study.warnMissingSampleInfo);
							log.warn("SampleID: {}", sampleId.toString());
						}
					}
				}

				break;
			case PLINK:
				log.info(Text.Matrix.scanAffectionStandby);
				if (dummySamples) {
					sampleInfoLHM = SamplesParser.scanPlinkStandardSampleInfo(altSampleInfoPath2);
				} else {
					Map<String, Object> dummySamplesInfoLHM = SamplesParser.scanPlinkStandardSampleInfo(altSampleInfoPath2);
					sampleInfoLHM = SamplesParser.scanGwaspiSampleInfo(sampleInfoPath);
					for (String sampleId : dummySamplesInfoLHM.keySet()) {
						if (!sampleInfoLHM.containsKey(sampleId)) {
							dummySampleValues[1] = sampleId.toString();
							sampleInfoLHM.put(sampleId, dummySampleValues);
							log.warn(Text.Study.warnMissingSampleInfo);
							log.warn("SampleID: {}", sampleId.toString());
						}
					}
				}
				break;
			case PLINK_Binary:
				log.info(Text.Matrix.scanAffectionStandby);
				FileReader inputFileReader = new FileReader(new File(sampleInfoPath));
				BufferedReader inputBufferReader = new BufferedReader(inputFileReader);
				String header = inputBufferReader.readLine();
				String[] cVals = header.split(cImport.Separators.separators_CommaSpaceTab_rgxp);
				if (cVals.length == 6) { // It's a FAM file
					sampleInfoLHM = SamplesParser.scanPlinkFAMSampleInfo(sampleInfoPath);
				} else { // It's a SampleInfo file
					sampleInfoLHM = SamplesParser.scanGwaspiSampleInfo(sampleInfoPath);
				}
				break;
			case HAPMAP:
				log.info(Text.Matrix.scanAffectionStandby);
				if (dummySamples) {
					sampleInfoLHM = SamplesParser.scanHapmapSampleInfo(altSampleInfoPath1);
					// NO AFFECTION STATE AVAILABLE
				} else {
					Map<String, Object> dummySamplesInfoLHM = SamplesParser.scanHapmapSampleInfo(altSampleInfoPath1);
					sampleInfoLHM = SamplesParser.scanGwaspiSampleInfo(sampleInfoPath);
					for (String sampleId : dummySamplesInfoLHM.keySet()) {
						if (!sampleInfoLHM.containsKey(sampleId)) {
							dummySampleValues[1] = sampleId.toString();
							sampleInfoLHM.put(sampleId, dummySampleValues);
							log.warn(Text.Study.warnMissingSampleInfo);
							log.warn("SampleID: {}", sampleId.toString());
						}
					}
				}
				break;
			case BEAGLE:
				log.info(Text.Matrix.scanAffectionStandby);
				if (dummySamples) {
					sampleInfoLHM = SamplesParser.scanBeagleSampleInfo(altSampleInfoPath1);
				} else {
					Map<String, Object> dummySamplesInfoLHM = SamplesParser.scanBeagleSampleInfo(altSampleInfoPath1);
					sampleInfoLHM = SamplesParser.scanGwaspiSampleInfo(sampleInfoPath);
					for (String sampleId : dummySamplesInfoLHM.keySet()) {
						if (!sampleInfoLHM.containsKey(sampleId)) {
							dummySampleValues[1] = sampleId.toString();
							sampleInfoLHM.put(sampleId, dummySampleValues);
							log.warn(Text.Study.warnMissingSampleInfo);
							log.warn("SampleID: {}", sampleId.toString());
						}
					}
				}
				break;
			case HGDP1:
				log.info(Text.Matrix.scanAffectionStandby);
				if (dummySamples) {
					sampleInfoLHM = SamplesParser.scanHGDP1SampleInfo(altSampleInfoPath1);
					// NO AFFECTION STATE AVAILABLE
				} else {
					Map<String, Object> dummySamplesInfoLHM = SamplesParser.scanHGDP1SampleInfo(altSampleInfoPath1);
					sampleInfoLHM = SamplesParser.scanGwaspiSampleInfo(sampleInfoPath);
					for (String sampleId : dummySamplesInfoLHM.keySet()) {
						if (!sampleInfoLHM.containsKey(sampleId)) {
							dummySampleValues[1] = sampleId.toString();
							sampleInfoLHM.put(sampleId, dummySampleValues);
							log.warn(Text.Study.warnMissingSampleInfo);
							log.warn("SampleID: {}", sampleId.toString());
						}
					}
				}
				break;
			case GWASpi:
				sampleInfoLHM = SamplesParser.scanGwaspiSampleInfo(sampleInfoPath);
				break;
			case Illumina_LGEN:
				log.info(Text.Matrix.scanAffectionStandby);
				if (dummySamples) {
					//gwasParams = MoreLoadInfoByFormat.showMoreInfoByFormat_Modal(cmb_Format.getSelectedItem().toString());
					sampleInfoLHM = SamplesParser.scanIlluminaLGENSampleInfo(altSampleInfoPath2);
				} else {
					Map<String, Object> dummySamplesInfoLHM = SamplesParser.scanIlluminaLGENSampleInfo(altSampleInfoPath2);
					sampleInfoLHM = SamplesParser.scanGwaspiSampleInfo(sampleInfoPath);
					for (String sampleId : dummySamplesInfoLHM.keySet()) {
						if (!sampleInfoLHM.containsKey(sampleId)) {
							dummySampleValues[1] = sampleId.toString();
							sampleInfoLHM.put(sampleId, dummySampleValues);
							log.warn(Text.Study.warnMissingSampleInfo);
							log.warn("SampleID: {}", sampleId.toString());
						}
					}
				}
				break;
			case Sequenom:
				sampleInfoLHM = SamplesParser.scanGwaspiSampleInfo(sampleInfoPath);
				break;
			default:
		}

		return sampleInfoLHM;
	}

	public static Set<String> collectAffectionStates(Map<String, Object> sampleInfoLHM) {
		Set<String> affectionStates = new HashSet<String>();
		for (Object value : sampleInfoLHM.values()) {
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
