package org.gwaspi.netCDF.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.global.Text;
import org.gwaspi.model.SampleInfo;
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
			Collection<SampleInfo> dummySampleInfos,
			Collection<SampleInfo> sampleInfos)
	{
		for (SampleInfo dummySampleInfo : dummySampleInfos) {
			if (!sampleInfos.contains(dummySampleInfo)) {
				SampleInfo dummySampleInfoCopy = new SampleInfo(dummySampleInfo.getSampleId());
				sampleInfos.add(dummySampleInfoCopy);
				log.warn(Text.Study.warnMissingSampleInfo);
				log.warn("SampleID: {}", dummySampleInfo.getSampleId());
			}
		}
	}

	private static boolean checkIsPlinkFAMFile(String sampleInfoPath)
			throws IOException
	{
		FileReader inputFileReader = new FileReader(new File(sampleInfoPath));
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);
		String header = inputBufferReader.readLine();
		inputBufferReader.close();
		String[] cVals = header.split(cImport.Separators.separators_CommaSpaceTab_rgxp);

		return (cVals.length == 6);
	}

	public static Collection<SampleInfo> collectSampleInfo(
			ImportFormat format,
			boolean dummySamples,
			String sampleInfoPath,
			String altSampleInfoPath1,
			String altSampleInfoPath2)
			throws IOException
	{
		Collection<SampleInfo> sampleInfos;

		switch (format) {
			case Affymetrix_GenomeWide6:
				log.info(Text.Matrix.scanAffectionStandby);
				if (dummySamples) {
					sampleInfos = SamplesParserManager.scanAffymetrixSampleInfo(altSampleInfoPath2);
				} else {
					Collection<SampleInfo> dummySamplesInfos = SamplesParserManager.scanAffymetrixSampleInfo(altSampleInfoPath2);
					sampleInfos = SamplesParserManager.scanGwaspiSampleInfo(sampleInfoPath);
					checkMissingSampleInfo(dummySamplesInfos, sampleInfos);
				}
				break;
			case PLINK:
				log.info(Text.Matrix.scanAffectionStandby);
				if (dummySamples) {
					sampleInfos = SamplesParserManager.scanPlinkStandardSampleInfo(altSampleInfoPath2);
				} else {
					Collection<SampleInfo> dummySamplesInfos = SamplesParserManager.scanPlinkStandardSampleInfo(altSampleInfoPath2);
					sampleInfos = SamplesParserManager.scanGwaspiSampleInfo(sampleInfoPath);
					checkMissingSampleInfo(dummySamplesInfos, sampleInfos);
				}
				break;
			case PLINK_Binary:
				log.info(Text.Matrix.scanAffectionStandby);
				if (checkIsPlinkFAMFile(sampleInfoPath)) {
					sampleInfos = SamplesParserManager.scanPlinkFAMSampleInfo(sampleInfoPath);
				} else {
					// It is a SampleInfo file
					sampleInfos = SamplesParserManager.scanGwaspiSampleInfo(sampleInfoPath);
				}
				break;
			case HAPMAP:
				log.info(Text.Matrix.scanAffectionStandby);
				if (dummySamples) {
					sampleInfos = SamplesParserManager.scanHapmapSampleInfo(altSampleInfoPath1);
				} else {
					Collection<SampleInfo> dummySamplesInfos = SamplesParserManager.scanHapmapSampleInfo(altSampleInfoPath1);
					sampleInfos = SamplesParserManager.scanGwaspiSampleInfo(sampleInfoPath);
					checkMissingSampleInfo(dummySamplesInfos, sampleInfos);
				}
				break;
			case BEAGLE:
				log.info(Text.Matrix.scanAffectionStandby);
				if (dummySamples) {
					sampleInfos = SamplesParserManager.scanBeagleSampleInfo(altSampleInfoPath1);
				} else {
					Collection<SampleInfo> dummySamplesInfos = SamplesParserManager.scanBeagleSampleInfo(altSampleInfoPath1);
					sampleInfos = SamplesParserManager.scanGwaspiSampleInfo(sampleInfoPath);
					checkMissingSampleInfo(dummySamplesInfos, sampleInfos);
				}
				break;
			case HGDP1:
				log.info(Text.Matrix.scanAffectionStandby);
				if (dummySamples) {
					sampleInfos = SamplesParserManager.scanHGDP1SampleInfo(altSampleInfoPath1);
				} else {
					Collection<SampleInfo> dummySamplesInfos = SamplesParserManager.scanHGDP1SampleInfo(altSampleInfoPath1);
					sampleInfos = SamplesParserManager.scanGwaspiSampleInfo(sampleInfoPath);
					checkMissingSampleInfo(dummySamplesInfos, sampleInfos);
				}
				break;
			case GWASpi:
				sampleInfos = SamplesParserManager.scanGwaspiSampleInfo(sampleInfoPath);
				break;
			case Illumina_LGEN:
				log.info(Text.Matrix.scanAffectionStandby);
				if (dummySamples) {
					sampleInfos = SamplesParserManager.scanIlluminaLGENSampleInfo(altSampleInfoPath2);
				} else {
					Collection<SampleInfo> dummySamplesInfos = SamplesParserManager.scanIlluminaLGENSampleInfo(altSampleInfoPath2);
					sampleInfos = SamplesParserManager.scanGwaspiSampleInfo(sampleInfoPath);
					checkMissingSampleInfo(dummySamplesInfos, sampleInfos);
				}
				break;
			case Sequenom:
				sampleInfos = SamplesParserManager.scanGwaspiSampleInfo(sampleInfoPath);
				break;
			default:
				sampleInfos = new ArrayList<SampleInfo>();
		}

		return sampleInfos;
	}

	public static Set<SampleInfo.Affection> collectAffectionStates(Collection<SampleInfo> sampleInfos) {

		Set<SampleInfo.Affection> affectionStates = EnumSet.noneOf(SampleInfo.Affection.class);

		for (SampleInfo sampleInfo : sampleInfos) {
			affectionStates.add(sampleInfo.getAffection());
		}

		return affectionStates;
	}
}
