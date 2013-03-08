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
			case PLINK:
			case Illumina_LGEN:
				log.info(Text.Matrix.scanAffectionStandby);
				if (dummySamples) {
					sampleInfos = SamplesParserManager.scanSampleInfo(format, altSampleInfoPath2);
				} else {
					Collection<SampleInfo> dummySamplesInfos = SamplesParserManager.scanSampleInfo(format, altSampleInfoPath2);
					sampleInfos = SamplesParserManager.scanSampleInfo(ImportFormat.GWASpi, sampleInfoPath);
					checkMissingSampleInfo(dummySamplesInfos, sampleInfos);
				}
				break;
			case PLINK_Binary:
				log.info(Text.Matrix.scanAffectionStandby);
				if (checkIsPlinkFAMFile(sampleInfoPath)) {
					sampleInfos = SamplesParserManager.scanSampleInfo(format, sampleInfoPath);
				} else {
					// It is a SampleInfo file
					sampleInfos = SamplesParserManager.scanSampleInfo(ImportFormat.GWASpi, sampleInfoPath);
				}
				break;
			case HAPMAP:
			case BEAGLE:
			case HGDP1:
				log.info(Text.Matrix.scanAffectionStandby);
				if (dummySamples) {
					sampleInfos = SamplesParserManager.scanSampleInfo(format, altSampleInfoPath1);
				} else {
					Collection<SampleInfo> dummySamplesInfos = SamplesParserManager.scanSampleInfo(format, altSampleInfoPath1);
					sampleInfos = SamplesParserManager.scanSampleInfo(ImportFormat.GWASpi, sampleInfoPath);
					checkMissingSampleInfo(dummySamplesInfos, sampleInfos);
				}
				break;
			case GWASpi:
				sampleInfos = SamplesParserManager.scanSampleInfo(format, sampleInfoPath);
				break;
			case Sequenom:
				sampleInfos = SamplesParserManager.scanSampleInfo(ImportFormat.GWASpi, sampleInfoPath); // FIXME why not format instead of ImportFormat.GWASpi?
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
