/*
 * Copyright (C) 2013 Universitat Pompeu Fabra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gwaspi.netCDF.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.StudyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HapMap genotypes loader.
 * Can load a single file or multiple files, as long as they belong to a single population (CEU, YRI, JPT...)
 * Imports Hapmap genotype files as found on
 * http://hapmap.ncbi.nlm.nih.gov/downloads/genotypes/?N=D
 */
public class LoadGTFromHapmapFiles extends AbstractLoadGTFromFiles implements GenotypesLoader {

	private final Logger log
			= LoggerFactory.getLogger(LoadGTFromHapmapFiles.class);

	public static interface Standard {

		public static final int dataStartRow = 1;
		public static final int sampleId = 11;
		public static final int markerId = 0;
		public static final int alleles = 1;
		public static final int chr = 2;
		public static final int pos = 3;
		public static final int strand = 4;
		public static final String missing = "NN";
		public static final int score = 10;
	}

	public LoadGTFromHapmapFiles() {
		super(new MetadataLoaderHapmap(), ImportFormat.HAPMAP, StrandType.FWD, true);
	}

	@Override
	public Iterator<Map.Entry<MarkerKey, byte[]>> iterator(
			StudyKey studyKey,
			SampleKey sampleKey,
			File file)
			throws IOException
	{
		throw new UnsupportedOperationException("This method of this class should never be called!");
	}

	@Override
	protected void addAdditionalBigDescriptionProperties(StringBuilder descSB, GenotypesLoadDescription loadDescription) {
		super.addAdditionalBigDescriptionProperties(descSB, loadDescription);

		descSB.append(loadDescription.getGtDirPath());
		descSB.append(" (Genotype file)\n");
	}

	public static File[] extractGTFilesToImport(GenotypesLoadDescription loadDescription) {

		File[] gtFilesToImport;

		File hapmapGTFile = new File(loadDescription.getGtDirPath());
		if (hapmapGTFile.isDirectory()) {
			gtFilesToImport = org.gwaspi.global.Utils.listFiles(loadDescription.getGtDirPath());
		} else {
			gtFilesToImport = new File[]{new File(loadDescription.getGtDirPath())};
		}

		return gtFilesToImport;
	}

	//<editor-fold defaultstate="expanded" desc="PROCESS GENOTYPES">
	@Override
	protected void loadGenotypes(
			GenotypesLoadDescription loadDescription,
			Map<SampleKey, SampleInfo> sampleInfos,
			Map<MarkerKey, MarkerMetadata> markerInfos,
			DataSetDestination samplesReceiver)
			throws Exception
	{
		final Collection<SampleInfo> sampleInfos2 = new ArrayList<SampleInfo>(sampleInfos.values());
		final Set<MarkerKey> markerKeys = markerInfos.keySet();

		File[] gtFilesToImport = extractGTFilesToImport(loadDescription);

		// TODO check if real sample files coincides with sampleInfoFile
		for (File gtFileToImport : gtFilesToImport) {
			Collection<SampleInfo> tempSamplesMap = getHapmapSampleIds(loadDescription.getStudyKey(), gtFileToImport);
			sampleInfos2.addAll(tempSamplesMap);
		}

		int sampleIndex = 0;
		for (SampleInfo sampleInfo : sampleInfos2) {
			// PURGE MarkerIdMap
			Map<MarkerKey, byte[]> alleles = AbstractLoadGTFromFiles.fillMap(markerKeys, cNetCDF.Defaults.DEFAULT_GT);

			for (File gtFileToImport : gtFilesToImport) {
				loadIndividualFiles(
						loadDescription.getStudyKey(),
						gtFileToImport,
						sampleInfo.getKey(),
						alleles,
						getGuessedGTCode());
			}

			// WRITING GENOTYPE DATA INTO netCDF FILE
			samplesReceiver.addSampleGTAlleles(sampleIndex, new ArrayList<byte[]>(alleles.values()));

			sampleIndex++;
		}
	}

	/**
	 * @see AbstractLoadGTFromFiles#loadIndividualFiles
	 */
	private void loadIndividualFiles(
			StudyKey studyKey,
			File file,
			SampleKey sampleKey,
			Map<MarkerKey, byte[]> alleles,
			GenotypeEncoding guessedGTCode)
			throws Exception
	{
		int dataStartRow = Standard.dataStartRow;
		FileReader inputFileReader = new FileReader(file);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		String header = null;
		for (int i = 0; i < dataStartRow; i++) {
			header = inputBufferReader.readLine();
		}
		String[] headerFields = header.split(cImport.Separators.separators_SpaceTab_rgxp);

		Map<SampleKey, Object> sampleOrderMap = new LinkedHashMap<SampleKey, Object>();
		for (int i = Standard.sampleId; i < headerFields.length; i++) {
			sampleOrderMap.put(SampleKey.valueOf(studyKey, headerFields[i]), i); // FIXME this is only the sampleID, without familyID. does hapMap have a familyId?
		}
		Object sampleColumnNb = sampleOrderMap.get(sampleKey);

		// GET ALLELES
		String l;
		while ((l = inputBufferReader.readLine()) != null) {

			// MEMORY LEAN METHOD
			if (sampleColumnNb != null) {
				StringTokenizer st = new StringTokenizer(l, cImport.Separators.separators_SpaceTab_rgxp);
				String markerId = st.nextToken();

				//read genotypes from this point on
				int k = 1;
				byte[] tmpAlleles = cNetCDF.Defaults.DEFAULT_GT;
				while (k <= (Integer) sampleColumnNb) {
					if (k < (Integer) sampleColumnNb) {
						st.nextToken();
						k++;
					}
					if (k == (Integer) sampleColumnNb) {
						String strAlleles = st.nextToken();
						if (strAlleles.equals(Standard.missing)) {
							tmpAlleles = cNetCDF.Defaults.DEFAULT_GT;
						} else {
							tmpAlleles = new byte[] {
								(byte) strAlleles.charAt(0),
								(byte) strAlleles.charAt(1)};
						}
						k++;
					}
				}
				alleles.put(MarkerKey.valueOf(markerId), tmpAlleles);
			}
		}
		inputBufferReader.close();

		if (guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.UNKNOWN)
				|| guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.O12))
		{
			guessedGTCode = Utils.detectGTEncoding(alleles.values());
		}
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="HELPER METHODS">
	private Collection<SampleInfo> getHapmapSampleIds(StudyKey studyKey, File hapmapGTFile) throws IOException {

		Collection<SampleInfo> uniqueSamples = new LinkedList<SampleInfo>();

		FileReader fr = new FileReader(hapmapGTFile.getPath());
		BufferedReader inputAnnotationBr = new BufferedReader(fr);
		String header = inputAnnotationBr.readLine();
		inputAnnotationBr.close();

		String[] hapmapVals = header.split(cImport.Separators.separators_SpaceTab_rgxp);

		for (int i = Standard.sampleId; i < hapmapVals.length; i++) {
			uniqueSamples.add(new SampleInfo(studyKey, hapmapVals[i]));
		}

		return uniqueSamples;
	}
	//</editor-fold>
}
