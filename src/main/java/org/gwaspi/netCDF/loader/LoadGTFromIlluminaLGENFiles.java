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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.model.DataSet;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.StudyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadGTFromIlluminaLGENFiles extends AbstractLoadGTFromFiles implements GenotypesLoader {

	private final Logger log
			= LoggerFactory.getLogger(LoadGTFromIlluminaLGENFiles.class);

	private static interface Standard {

		public static final int familyId = 0;
		public static final int sampleId = 1;
		public static final int markerId = 2;
		public static final int allele1 = 3;
		public static final int allele2 = 4;
		public static final String missing = "-";
	}

	public LoadGTFromIlluminaLGENFiles() {
		super(ImportFormat.Illumina_LGEN, StrandType.PLSMIN, false, null);
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

		return new MetadataLoaderIlluminaLGEN(
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
			DataSetDestination samplesReceiver)
			throws Exception
	{
		File[] gtFilesToImport = org.gwaspi.global.Utils.listFiles(loadDescription.getGtDirPath());

		for (int i = 0; i < gtFilesToImport.length; i++) {
			//log.info("Input file: "+i);
			loadIndividualFiles(
					loadDescription,
					samplesReceiver,
					gtFilesToImport[i]);
//					ncfile,
//					markerSetMap,
//					sampleIds,
//					guessedGTCode);

			if (i % 10 == 0) {
				log.info("Done processing file " + i);
			}
		}
	}

	/**
	 * @see AbstractLoadGTFromFiles#loadIndividualFiles
	 */
	private void loadIndividualFiles(
			GenotypesLoadDescription loadDescription,
			DataSetDestination samplesReceiver,
			File file)
//			NetcdfFileWriteable ncfile,
//			Map<MarkerKey, ?> sortedMetadata,
//			List<String> samplesAL,
//			GenotypeEncoding guessedGTCode)
			throws Exception
	{
		// HACK
		DataSet dataSet = ((InMemorySamplesReceiver) samplesReceiver).getDataSet();

		List<SampleKey> sampleKeys = AbstractLoadGTFromFiles.extractKeys(dataSet.getSampleInfos());

		// LOAD INPUT FILE
		FileReader inputFileReader = new FileReader(file);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		//Skip header rows
		String header;
		boolean gotHeader = false;
		while (!gotHeader && inputBufferReader.ready()) {
			header = inputBufferReader.readLine();
			if (header.startsWith("[Data]")) {
				header = inputBufferReader.readLine(); // Get next line which is real header
				gotHeader = true;
			}
		}

		int gtStride = cNetCDF.Strides.STRIDE_GT;
		StringBuilder sb = new StringBuilder(gtStride);
		for (int i = 0; i < sb.capacity(); i++) {
			sb.append('0');
		}

		//GET ALLELES
		String l;
		Map<MarkerKey, byte[]> tempMarkerSet = new LinkedHashMap<MarkerKey, byte[]>();
		SampleKey currentSampleKey = new SampleKey(loadDescription.getStudyKey(), "", SampleKey.FAMILY_ID_NONE);
		while ((l = inputBufferReader.readLine()) != null) {
			String[] cVals = l.split(cImport.Separators.separators_CommaTab_rgxp);
			String tmpSampleId = cVals[1];

			if (tmpSampleId.equals(currentSampleKey.getSampleId())) {
				byte[] tmpAlleles = new byte[] {
						(byte) cVals[Standard.allele1].charAt(0),
						(byte) cVals[Standard.allele2].charAt(0)};
				tempMarkerSet.put(MarkerKey.valueOf(cVals[Standard.markerId]), tmpAlleles);
			} else {
				if (!currentSampleKey.getSampleId().equals("")) { // EXCEPT FIRST TIME ROUND
					// INIT AND PURGE SORTEDMARKERSET Map
					Map<MarkerKey, byte[]> sortedAlleles = AbstractLoadGTFromFiles.fillMap(dataSet.getMarkerMetadatas().keySet(), cNetCDF.Defaults.DEFAULT_GT);

					// WRITE Map TO MATRIX
					for (Map.Entry<MarkerKey, byte[]> entry : sortedAlleles.entrySet()) {
						MarkerKey markerKey = entry.getKey();
						byte[] value = (tempMarkerSet.get(markerKey) != null) ? tempMarkerSet.get(markerKey) : cNetCDF.Defaults.DEFAULT_GT;
						entry.setValue(value);
					}
					tempMarkerSet.clear();

					// WRITING GENOTYPE DATA INTO netCDF FILE
					int sampleIndex = sampleKeys.indexOf(currentSampleKey);
					if (sampleIndex != -1) {  //CHECK IF CURRENT FILE IS NOT PRESENT IN SAMPLEINFO FILE!!
						samplesReceiver.addSampleGTAlleles(sampleIndex, sortedAlleles.values());
					}
				}

				currentSampleKey = new SampleKey(loadDescription.getStudyKey(), tmpSampleId, SampleKey.FAMILY_ID_NONE);

				byte[] tmpAlleles;
				if (cVals[Standard.allele1].equals(Standard.missing)
						&& cVals[Standard.allele2].equals(Standard.missing)) {
					tmpAlleles = cNetCDF.Defaults.DEFAULT_GT;
				} else {
					tmpAlleles = new byte[] {
							(byte) (cVals[Standard.allele1].charAt(0)),
							(byte) (cVals[Standard.allele2].charAt(0))};
				}
				tempMarkerSet.put(MarkerKey.valueOf(cVals[Standard.markerId]), tmpAlleles);
			}
		}
		inputBufferReader.close();

		// WRITE LAST SAMPLE Map TO MATRIX
		// INIT AND PURGE SORTEDMARKERSET Map
		Map<MarkerKey, byte[]> sortedAlleles = AbstractLoadGTFromFiles.fillMap(dataSet.getMarkerMetadatas().keySet(), cNetCDF.Defaults.DEFAULT_GT);
		for (Map.Entry<MarkerKey, byte[]> entry : sortedAlleles.entrySet()) {
			MarkerKey markerKey = entry.getKey();
			byte[] value = (tempMarkerSet.get(markerKey) != null) ? tempMarkerSet.get(markerKey) : cNetCDF.Defaults.DEFAULT_GT;
			entry.setValue(value);
		}
		tempMarkerSet.clear();

		GenotypeEncoding guessedGTCode = getGuessedGTCode();
		if (guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.UNKNOWN)
				|| guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.O12))
		{
			guessedGTCode = Utils.detectGTEncoding(sortedAlleles.values());
		}

		// WRITING GENOTYPE DATA INTO netCDF FILE
		int sampleIndex = sampleKeys.indexOf(currentSampleKey);
		if (sampleIndex != -1) {  //CHECK IF CURRENT FILE IS NOT PRESENT IN SAMPLEINFO FILE!!
			samplesReceiver.addSampleGTAlleles(sampleIndex, sortedAlleles.values());
		}
	}

	private static String getAffySampleId(File fileToScan) throws IOException {

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

		return sampleId;
	}
}
