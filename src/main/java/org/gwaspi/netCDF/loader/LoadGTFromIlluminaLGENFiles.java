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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gwaspi.constants.ImportConstants;
import org.gwaspi.constants.ImportConstants.ImportFormat;
import org.gwaspi.constants.NetCDFConstants;
import org.gwaspi.constants.NetCDFConstants.Defaults.GenotypeEncoding;
import org.gwaspi.constants.NetCDFConstants.Defaults.StrandType;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.StudyKey;

public class LoadGTFromIlluminaLGENFiles extends AbstractLoadGTFromFiles implements GenotypesLoader {

	private interface Standard {

		int familyId = 0;
		int sampleId = 1;
		int markerId = 2;
		int allele1 = 3;
		int allele2 = 4;
		String missing = "-";
	}

	public LoadGTFromIlluminaLGENFiles() {
		super(new MetadataLoaderIlluminaLGEN(), ImportFormat.Illumina_LGEN, StrandType.PLSMIN, false);
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
	protected void addAdditionalBigDescriptionProperties(StringBuilder description, GenotypesLoadDescription loadDescription) {
		super.addAdditionalBigDescriptionProperties(description, loadDescription);

		description
				.append(loadDescription.getGtDirPath())
				.append(" (Genotype files)\n")
				.append('\n')
				.append(loadDescription.getAnnotationFilePath())
				.append(" (Annotation file)\n");
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

		for (File gtFileToImport : gtFilesToImport) {
			loadIndividualFiles(
					loadDescription,
					sampleInfos,
					markerInfos,
					samplesReceiver,
					gtFileToImport);
		}
	}

	/**
	 * @see AbstractLoadGTFromFiles#loadIndividualFiles
	 */
	private void loadIndividualFiles(
			GenotypesLoadDescription loadDescription,
			Map<SampleKey, SampleInfo> sampleInfos,
			Map<MarkerKey, MarkerMetadata> markerInfos,
			DataSetDestination samplesReceiver,
			File file)
//			NetcdfFileWriteable ncfile,
//			Map<MarkerKey, ?> sortedMetadata,
//			List<String> samplesAL,
//			GenotypeEncoding guessedGTCode)
			throws IOException
	{
		final List<SampleKey> sampleKeys = new ArrayList<SampleKey>(sampleInfos.keySet());
		final Set<MarkerKey> markerKeys = markerInfos.keySet();

		// LOAD INPUT FILE
		FileReader inputFileReader = new FileReader(file);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		//Skip header rows
		String header = null;
		while ((header == null) && inputBufferReader.ready()) {
			final String line = inputBufferReader.readLine();
			if (line.startsWith("[Data]")) {
				header = inputBufferReader.readLine(); // get the real header
			}
		}

//		int gtStride = NetCDFConstants.Strides.STRIDE_GT;
//		StringBuilder sb = new StringBuilder(gtStride);
//		for (int i = 0; i < sb.capacity(); i++) {
//			sb.append('0');
//		}

		//GET ALLELES
		String l;
		Map<MarkerKey, byte[]> tempMarkerSet = new LinkedHashMap<MarkerKey, byte[]>();
		SampleKey currentSampleKey = new SampleKey(loadDescription.getStudyKey(), "", SampleKey.FAMILY_ID_NONE);
		while ((l = inputBufferReader.readLine()) != null) {
			String[] cVals = l.split(ImportConstants.Separators.separators_CommaTab_rgxp);
			String tmpSampleId = cVals[1];

			if (tmpSampleId.equals(currentSampleKey.getSampleId())) {
				byte[] tmpAlleles = new byte[] {
						(byte) cVals[Standard.allele1].charAt(0),
						(byte) cVals[Standard.allele2].charAt(0)};
				tempMarkerSet.put(MarkerKey.valueOf(cVals[Standard.markerId]), tmpAlleles);
			} else {
				if (!currentSampleKey.getSampleId().isEmpty()) { // EXCEPT FIRST TIME ROUND
					// INIT AND PURGE SORTEDMARKERSET Map
					Map<MarkerKey, byte[]> sortedAlleles = AbstractLoadGTFromFiles.fillMap(markerKeys, NetCDFConstants.Defaults.DEFAULT_GT);

					// WRITE Map TO MATRIX
					for (Map.Entry<MarkerKey, byte[]> entry : sortedAlleles.entrySet()) {
						MarkerKey markerKey = entry.getKey();
						byte[] value = (tempMarkerSet.get(markerKey) != null) ? tempMarkerSet.get(markerKey) : NetCDFConstants.Defaults.DEFAULT_GT;
						entry.setValue(value);
					}
					tempMarkerSet.clear();

					// WRITING GENOTYPE DATA INTO netCDF FILE
					int sampleIndex = sampleKeys.indexOf(currentSampleKey);
					if (sampleIndex != -1) {  //CHECK IF CURRENT FILE IS NOT PRESENT IN SAMPLEINFO FILE!!
						samplesReceiver.addSampleGTAlleles(sampleIndex, new ArrayList<byte[]>(sortedAlleles.values()));
					}
				}

				currentSampleKey = new SampleKey(loadDescription.getStudyKey(), tmpSampleId, SampleKey.FAMILY_ID_NONE);

				byte[] tmpAlleles;
				if (cVals[Standard.allele1].equals(Standard.missing)
						&& cVals[Standard.allele2].equals(Standard.missing)) {
					tmpAlleles = NetCDFConstants.Defaults.DEFAULT_GT;
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
		Map<MarkerKey, byte[]> sortedAlleles = AbstractLoadGTFromFiles.fillMap(markerKeys, NetCDFConstants.Defaults.DEFAULT_GT);
		for (Map.Entry<MarkerKey, byte[]> entry : sortedAlleles.entrySet()) {
			MarkerKey markerKey = entry.getKey();
			byte[] value = (tempMarkerSet.get(markerKey) != null) ? tempMarkerSet.get(markerKey) : NetCDFConstants.Defaults.DEFAULT_GT;
			entry.setValue(value);
		}
		tempMarkerSet.clear();

		GenotypeEncoding guessedGTCode = getGuessedGTCode();
		if (guessedGTCode.equals(NetCDFConstants.Defaults.GenotypeEncoding.UNKNOWN)
				|| guessedGTCode.equals(NetCDFConstants.Defaults.GenotypeEncoding.O12))
		{
			guessedGTCode = Utils.detectGTEncoding(sortedAlleles.values());
		}

		// WRITING GENOTYPE DATA INTO netCDF FILE
		int sampleIndex = sampleKeys.indexOf(currentSampleKey);
		if (sampleIndex != -1) {  //CHECK IF CURRENT FILE IS NOT PRESENT IN SAMPLEINFO FILE!!
			samplesReceiver.addSampleGTAlleles(sampleIndex, new ArrayList<byte[]>(sortedAlleles.values()));
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
