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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadGTFromHGDP1Files extends AbstractLoadGTFromFiles implements GenotypesLoader {

	private final Logger log
			= LoggerFactory.getLogger(LoadGTFromHGDP1Files.class);

	private interface Standard {

		int markerId = 0;
		int genotypes = 1;
		String missing = "--";
	}

	public LoadGTFromHGDP1Files() {
		super(new MetadataLoaderHGDP1(), ImportFormat.HGDP1, StrandType.UNKNOWN, false);
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
				.append(" (Genotype file)\n")
				.append(loadDescription.getAnnotationFilePath())
				.append(" (Marker file)\n");
	}

	@Override
	protected void loadGenotypes(
			GenotypesLoadDescription loadDescription,
			Map<SampleKey, SampleInfo> sampleInfos,
			Map<MarkerKey, MarkerMetadata> markerInfos,
			DataSetDestination samplesReceiver)
			throws IOException
	{
		final Collection<SampleInfo> sampleInfos2 = sampleInfos.values();
		final Set<MarkerKey> markerKeys = markerInfos.keySet();

		int sampleIndex = 0;
		for (SampleInfo sampleInfo : sampleInfos2) {
			// PURGE MarkerIdMap
			Map<MarkerKey, byte[]> alleles = AbstractLoadGTFromFiles.fillMap(markerKeys, NetCDFConstants.Defaults.DEFAULT_GT);

			try {
				loadIndividualFiles(
						loadDescription.getStudyKey(),
						new File(loadDescription.getGtDirPath()),
						sampleInfo.getKey(),
						alleles,
						getGuessedGTCode());

				// WRITING GENOTYPE DATA INTO netCDF FILE
				samplesReceiver.addSampleGTAlleles(sampleIndex, new ArrayList<byte[]>(alleles.values()));
			} catch (IOException ex) {
				log.warn(null, ex);
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
			Map<MarkerKey, byte[]> alleles,
			GenotypeEncoding guessedGTCode)
			throws IOException
	{
		FileReader inputFileReader = new FileReader(file);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

//		int gtStride = NetCDFConstants.Strides.STRIDE_GT;
//		StringBuilder sb = new StringBuilder(gtStride);
//		for (int i = 0; i < sb.capacity(); i++) {
//			sb.append('0');
//		}

		Map<MarkerKey, byte[]> tempMarkerIdMap = new LinkedHashMap<MarkerKey, byte[]>();
		Map<SampleKey, Integer> sampleOrderMap = new LinkedHashMap<SampleKey, Integer>();

		String sampleHeader = inputBufferReader.readLine();
		String[] headerFields = sampleHeader.split(ImportConstants.Separators.separators_SpaceTab_rgxp);
		for (int i = 0; i < headerFields.length; i++) {
			if (!headerFields[i].isEmpty()) {
				String sampleId = headerFields[i];
				// NOTE The HGDP1 format does not have a family-ID
				sampleOrderMap.put(new SampleKey(studyKey, sampleId, SampleKey.FAMILY_ID_NONE), i);
			}
		}

		String l;
		while ((l = inputBufferReader.readLine()) != null) {
			//GET ALLELES FROM MARKER ROWS
			String[] cVals = l.split(ImportConstants.Separators.separators_SpaceTab_rgxp);
			MarkerKey currMarkerId = MarkerKey.valueOf(cVals[Standard.markerId]);

			Integer columnNb = sampleOrderMap.get(sampleKey);
			if (columnNb != null) {
				String strAlleles = cVals[columnNb];
				if (strAlleles.equals(Standard.missing)) {
					tempMarkerIdMap.put(currMarkerId, NetCDFConstants.Defaults.DEFAULT_GT);
				} else {
					byte[] tmpAlleles = new byte[] {
							(byte) strAlleles.charAt(0),
							(byte) strAlleles.charAt(0)}; // FIXME this should probably be 1, not 0
					tempMarkerIdMap.put(currMarkerId, tmpAlleles);
				}
			}
		}
		inputBufferReader.close();

		alleles.putAll(tempMarkerIdMap);

		if (guessedGTCode.equals(GenotypeEncoding.UNKNOWN)
				|| guessedGTCode.equals(GenotypeEncoding.O12))
		{
			guessedGTCode = Utils.detectGTEncoding(alleles.values());
		}
	}
}
