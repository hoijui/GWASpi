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
import java.util.Iterator;
import java.util.Map;
import org.gwaspi.constants.ImportConstants;
import org.gwaspi.constants.ImportConstants.ImportFormat;
import org.gwaspi.constants.NetCDFConstants.Defaults;
import org.gwaspi.constants.NetCDFConstants.Defaults.StrandType;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.StudyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadGTFromSequenomFiles extends AbstractLoadGTFromFiles implements GenotypesLoader {

	private final Logger log
			= LoggerFactory.getLogger(LoadGTFromSequenomFiles.class);

	private interface Standard {

		static final int sampleId = 0;
		static final int alleles = 1;
		static final int markerId = 2;
		static final int well = 3;
		static final int qa_desc = 4;
	}

	public LoadGTFromSequenomFiles() {
		super(new MetadataLoaderSequenom(), ImportFormat.Sequenom, StrandType.PLSMIN, false);
	}

	@Override
	public Iterator<Map.Entry<MarkerKey, byte[]>> iterator(
			StudyKey studyKey,
			SampleKey sampleKey,
			File file)
			throws IOException
	{
//		return new PlinkFlatParseIterator(studyKey, file, sampleKey);
		throw new UnsupportedOperationException("This method of this class should never be called!");
	}

	@Override
	protected void addAdditionalBigDescriptionProperties(StringBuilder descSB, GenotypesLoadDescription loadDescription) {
		super.addAdditionalBigDescriptionProperties(descSB, loadDescription);

		descSB.append(loadDescription.getGtDirPath());
		descSB.append(" (Genotype files)\n");
		descSB.append("\n");
		descSB.append(loadDescription.getAnnotationFilePath());
		descSB.append(" (Annotation file)\n");
	}

	@Override
	protected void loadGenotypes(
			GenotypesLoadDescription loadDescription,
			Map<SampleKey, SampleInfo> sampleInfos,
			Map<MarkerKey, MarkerMetadata> markerInfos,
			DataSetDestination samplesReceiver)
			throws IOException
	{
		File[] gtFilesToImport = org.gwaspi.global.Utils.listFiles(loadDescription.getGtDirPath());
//		File gtFileToImport = new File(gtDirPath);

		// INIT AND PURGE SORTEDMARKERSET Map
		int sampleIndex = 0;
		for (SampleInfo sampleInfo : sampleInfos.values()) {
			// PURGE MarkerIdMap on current sample
			Map<MarkerKey, byte[]> alleles = AbstractLoadGTFromFiles.fillMap(markerInfos.keySet(), Defaults.DEFAULT_GT);

			// PARSE ALL FILES FOR ANY DATA ON CURRENT SAMPLE
			for (int i = 0; i < gtFilesToImport.length; i++) {
				//log.info("Input file: "+i);
				loadIndividualFiles(
						loadDescription.getStudyKey(),
						gtFilesToImport[i],
						sampleInfo.getKey(),
						alleles);
			}

//			GenotypeEncoding guessedGTCode = getGuessedGTCode();
//			if (guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.UNKNOWN)
//					|| guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.O12))
//			{
//				guessedGTCode = Utils.detectGTEncoding(alleles);
//			}

			// SAVING GENOTYPE DATA
			try {
				samplesReceiver.addSampleGTAlleles(sampleIndex, new ArrayList<byte[]>(alleles.values()));
			} catch (IOException ex) {
				log.error("Failed writing file", ex);
			}

			sampleIndex++;
		}
	}

	/**
	 * @see AbstractLoadGTFromFiles#loadIndividualFiles
	 */
	public void loadIndividualFiles(
			StudyKey studyKey,
			File file,
			SampleKey sampleKey,
			Map<MarkerKey, byte[]> alleles)
			throws IOException
	{
		FileReader inputFileReader = new FileReader(file);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		// GET ALLELES
		String l;
		while ((l = inputBufferReader.readLine()) != null) {
			if (!l.contains("SAMPLE_ID")) { // SKIP ALL HEADER LINES
				String[] cVals = l.split(ImportConstants.Separators.separators_Tab_rgxp);
				String currSampleId = cVals[Standard.sampleId];
				// NOTE The Sequenom format does not have a family-ID
				SampleKey currSampleKey = new SampleKey(studyKey, currSampleId, SampleKey.FAMILY_ID_NONE);
				if (currSampleKey.equals(sampleKey)) {
					// ONLY PROCESS CURRENT SAMPLEID DATA
					String markerId = cVals[Standard.markerId].trim();
					try {
						Long.parseLong(markerId);
						markerId = "rs" + markerId;
					} catch (Exception ex) {
						log.warn(null, ex); // XXX maybe this is not a problem, but an OK thing?
					}

					String sAlleles = cVals[Standard.alleles];
					if (sAlleles.length() == 0) {
						sAlleles = "00";
					} else if (sAlleles.length() == 1) {
						sAlleles = sAlleles + sAlleles;
					}
					byte[] tmpAlleles = new byte[]{(byte) sAlleles.charAt(0), (byte) sAlleles.charAt(1)};
					alleles.put(MarkerKey.valueOf(markerId), tmpAlleles);
				}
			}
		}
		inputBufferReader.close();
	}
}
