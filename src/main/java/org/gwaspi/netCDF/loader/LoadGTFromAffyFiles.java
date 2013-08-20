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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.global.Text;
import org.gwaspi.global.TypeConverter;
import org.gwaspi.model.DataSet;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.StudyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadGTFromAffyFiles extends AbstractLoadGTFromFiles implements GenotypesLoader {

	private final Logger log
			= LoggerFactory.getLogger(LoadGTFromAffyFiles.class);

	static interface Standard {
		// ProbesetID, Call, Confidence, Signal A, Signal B, Forced Call

		public static final int markerId = 0;
		public static final int alleles = 1; // Caution, using normal Call, not Forced Call!
		public static final String missing = "NoCall";
		public static final int score = 2;
		public static final int intensity_A = 3;
		public static final int intensity_B = 4;
	}

	public LoadGTFromAffyFiles() {
		super(ImportFormat.Affymetrix_GenomeWide6, StrandType.PLSMIN, true, cNetCDF.Variables.VAR_MARKERS_BASES_DICT);
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
		descSB.append(" (Genotype files)\n");
		descSB.append("\n");
		descSB.append(loadDescription.getAnnotationFilePath());
		descSB.append(" (Annotation file)\n");
	}

	@Override
	protected MetadataLoader createMetaDataLoader(GenotypesLoadDescription loadDescription) {

		return new MetadataLoaderAffy(
				loadDescription.getAnnotationFilePath(),
				loadDescription.getFormat(),
				loadDescription.getStudyKey());
	}

	@Override
	protected TypeConverter<MarkerMetadata, String> getBaseDictPropertyExtractor() {
		return MarkerMetadata.TO_ALLELES;
	}

	@Override
	protected boolean isHasStrandInfo() {
		return true;
	}

	@Override
	protected void loadGenotypes(
			GenotypesLoadDescription loadDescription,
			DataSetDestination samplesReceiver)
			throws Exception
	{
		// HACK
		DataSet dataSet = ((InMemorySamplesReceiver) samplesReceiver).getDataSet();

		// PURGE alleles
		Map<MarkerKey, byte[]> alleles = AbstractLoadGTFromFiles.fillMap(dataSet.getMarkerMetadatas().keySet(), cNetCDF.Defaults.DEFAULT_GT);

		File[] gtFilesToImport = org.gwaspi.global.Utils.listFiles(loadDescription.getGtDirPath());

		//<editor-fold defaultstate="expanded" desc="SAMPLES GATHERING">
		// GET SAMPLES FROM FILES
		List<SampleKey> sampleKeys = new ArrayList<SampleKey>();
		for (int i = 0; i < gtFilesToImport.length; i++) {
			SampleKey sampleKey;
			switch (loadDescription.getFormat()) {
				case Affymetrix_GenomeWide6:
					sampleKey = getAffySampleId(loadDescription.getStudyKey(), gtFilesToImport[i]);
					break;
				default:
					sampleKey = new SampleKey(loadDescription.getStudyKey(), "", SampleKey.FAMILY_ID_NONE);
					break;
			}
			// NOTE The Beagle format does not have a family-ID
			sampleKeys.add(sampleKey);
		}

		// COMPARE SAMPLE INFO LIST TO AVAILABLE FILES
//		samples.containsAll(loadDescription.getSampleInfo().keySet());
		//</editor-fold>

		// START PROCESS OF LOADING GENOTYPES
		for (int i = 0; i < gtFilesToImport.length; i++) {
			//log.info("Input file: "+i);
			loadIndividualFiles(
					loadDescription,
					samplesReceiver,
					gtFilesToImport[i],
					alleles,
					sampleKeys);

			if ((i == 1) || ((i+1) % 100 == 0)) {
				log.info("Done processing sample {} / {}", i,
						sampleKeys.size());
			}
		}
	}

	/**
	 * @see AbstractLoadGTFromFiles#loadIndividualFiles
	 */
	private void loadIndividualFiles(
			GenotypesLoadDescription loadDescription,
			DataSetDestination samplesReceiver,
			File file,
			Map<MarkerKey, byte[]> sortedAlleles,
			List<SampleKey> samples)
			throws Exception
	{
		// LOAD INPUT FILE
		// GET SAMPLEID
		SampleKey sampleKey = getAffySampleId(loadDescription.getStudyKey(), file);

		FileReader inputFileReader = new FileReader(file);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		//Skip header rows
		String header = null;
		while (header == null) {
			header = inputBufferReader.readLine();
			if (header.startsWith("#")) {
				header = null;
			}
		}


		// GET ALLELES
		Map<MarkerKey, byte[]> tempMarkerSet = new HashMap<MarkerKey, byte[]>();
		String l;
		while ((l = inputBufferReader.readLine()) != null) {
			String[] cVals = l.split(cImport.Separators.separators_CommaTab_rgxp);

			byte[] alleles;
			switch (loadDescription.getFormat()) {
				case Affymetrix_GenomeWide6:
					if (cVals[Standard.alleles].equals(Standard.missing)) {
						alleles = cNetCDF.Defaults.DEFAULT_GT;
					} else {
						alleles = new byte[] {
							(byte) (cVals[Standard.alleles].charAt(0)),
							(byte) (cVals[Standard.alleles].charAt(1))};
					}
					break;
				default:
					alleles = new byte[cNetCDF.Strides.STRIDE_GT];
					break;
			}

			tempMarkerSet.put(MarkerKey.valueOf(cVals[Standard.markerId]), alleles);
		}
		inputBufferReader.close();

		for (MarkerKey key : sortedAlleles.keySet()) {
			byte[] value = tempMarkerSet.containsKey(key) ? tempMarkerSet.get(key) : cNetCDF.Defaults.DEFAULT_GT;
			sortedAlleles.put(key, value);
		}
		if (tempMarkerSet != null) {
			tempMarkerSet.clear();
		}

		GenotypeEncoding guessedGTCode = getGuessedGTCode();
		if (guessedGTCode.equals(GenotypeEncoding.UNKNOWN)
				|| guessedGTCode.equals(GenotypeEncoding.O12))
		{
			guessedGTCode = Utils.detectGTEncoding(sortedAlleles.values());
		}

		// WRITING GENOTYPE DATA INTO netCDF FILE
		int sampleIndex = samples.indexOf(sampleKey);
		if (sampleIndex != -1) { // CHECK IF CURRENT FILE IS NOT PRESENT IN SAMPLEINFO FILE!!
			samplesReceiver.addSampleGTAlleles(sampleIndex, sortedAlleles.values());
		}
	}

	private static SampleKey getAffySampleId(StudyKey studyKey, File fileToScan) throws IOException {
//		FileReader inputFileReader = new FileReader(fileToScan);
//		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);
		String l = fileToScan.getName();
		String sampleId;
		int end = l.lastIndexOf(".birdseed-v2");
		if (end != -1) {
			sampleId = l.substring(0, end);
		} else {
			sampleId = l.substring(0, l.indexOf('.'));
		}

//		String[] cVals = l.split("_");
//		String sampleId = cVals[preprocessing.cFormats.sampleId];

		return new SampleKey(studyKey, sampleId, SampleKey.FAMILY_ID_NONE);
	}
}
