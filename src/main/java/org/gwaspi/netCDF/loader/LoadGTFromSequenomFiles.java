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
import java.util.Iterator;
import java.util.Map;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.global.Text;
import org.gwaspi.model.DataSet;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.StudyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.InvalidRangeException;

public class LoadGTFromSequenomFiles extends AbstractLoadGTFromFiles implements GenotypesLoader {

	private final Logger log
			= LoggerFactory.getLogger(LoadGTFromSequenomFiles.class);

	private static interface Standard {

		public static final int sampleId = 0;
		public static final int alleles = 1;
		public static final int markerId = 2;
		public static final int well = 3;
		public static final int qa_desc = 4;
	}

	public LoadGTFromSequenomFiles() {
		super(ImportFormat.Sequenom, StrandType.PLSMIN, false, null);
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
	protected MetadataLoader createMetaDataLoader(GenotypesLoadDescription loadDescription) {

		return new MetadataLoaderSequenom(
				loadDescription.getAnnotationFilePath(),
				loadDescription.getStudyKey());
	}

	@Override
	protected String getStrandFlag(GenotypesLoadDescription loadDescription) {
		return cNetCDF.Defaults.StrandType.FWD.toString();
	}

	@Override
	protected void loadGenotypes(
			GenotypesLoadDescription loadDescription,
			SamplesReceiver samplesReceiver)
			throws Exception
	{
		// HACK
		DataSet dataSet = ((InMemorySamplesReceiver) samplesReceiver).getDataSet();

		File[] gtFilesToImport = org.gwaspi.global.Utils.listFiles(loadDescription.getGtDirPath());
//		File gtFileToImport = new File(gtDirPath);

		// INIT AND PURGE SORTEDMARKERSET Map
		int sampleIndex = 0;
		for (SampleInfo sampleInfo : dataSet.getSampleInfos()) {
			// PURGE MarkerIdMap on current sample
			Map<MarkerKey, byte[]> alleles = AbstractLoadGTFromFiles.fillMap(dataSet.getMarkerMetadatas().keySet(), cNetCDF.Defaults.DEFAULT_GT);

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
				samplesReceiver.addSampleGTAlleles(sampleIndex, alleles.values());
			} catch (IOException ex) {
				log.error("Failed writing file", ex);
			} catch (InvalidRangeException ex) {
				log.error(null, ex);
			}

			sampleIndex++;
			if (sampleIndex == 1) {
				log.info(Text.All.processing);
			} else if (sampleIndex % 100 == 0) {
				log.info("Done processing sample Nº{}", sampleIndex);
			}
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
			throws IOException, InvalidRangeException
	{
		FileReader inputFileReader = new FileReader(file);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		// GET ALLELES
		String l;
		while ((l = inputBufferReader.readLine()) != null) {
			if (!l.contains("SAMPLE_ID")) { // SKIP ALL HEADER LINES
				String[] cVals = l.split(cImport.Separators.separators_Tab_rgxp);
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
