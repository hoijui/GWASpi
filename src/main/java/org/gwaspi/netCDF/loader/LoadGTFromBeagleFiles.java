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
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.StudyKey;
import ucar.ma2.InvalidRangeException;

public class LoadGTFromBeagleFiles extends AbstractLoadGTFromFiles {

	private static interface Standard {

		public static final int markerId = 1;
		public static final int genotypes = 2;
		public static final String missing = "0";
	}

	//<editor-fold defaultstate="expanded" desc="CONSTRUCTORS">
	public LoadGTFromBeagleFiles()
	{
		super(
				ImportFormat.BEAGLE,
				StrandType.UNKNOWN,
				false,
				null); // disabled, else: cNetCDF.Variables.VAR_MARKERS_BASES_KNOWN
	}

	@Override
	protected void addAdditionalBigDescriptionProperties(StringBuilder descSB, GenotypesLoadDescription loadDescription) {
		super.addAdditionalBigDescriptionProperties(descSB, loadDescription);

		descSB.append(loadDescription.getGtDirPath());
		descSB.append(" (Genotype file)\n");
		descSB.append(loadDescription.getAnnotationFilePath());
		descSB.append(" (Marker file)\n");
	}

	@Override
	protected MetadataLoader createMetaDataLoader(GenotypesLoadDescription loadDescription) {

		return new MetadataLoaderBeagle(
				loadDescription.getAnnotationFilePath(),
				loadDescription.getChromosome(),
				loadDescription.getStrand(),
				loadDescription.getStudyKey());
	}

	@Override
	public void loadIndividualFiles(
			StudyKey studyKey,
			File file,
			SampleKey sampleKey,
			Map<MarkerKey, byte[]> wrMarkerSetMap)
			throws IOException, InvalidRangeException
	{
		FileReader inputFileReader = new FileReader(file);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		int gtStride = cNetCDF.Strides.STRIDE_GT;
		StringBuilder sb = new StringBuilder(gtStride);
		for (int i = 0; i < sb.capacity(); i++) {
			sb.append('0');
		}

		Map<MarkerKey, byte[]> tempMarkerIdMap = new LinkedHashMap<MarkerKey, byte[]>();
		Map<SampleKey, Integer> sampleOrderMap = new LinkedHashMap<SampleKey, Integer>();

		String l;
		while ((l = inputBufferReader.readLine()) != null) {
			if (l.startsWith("I")) { // Found first marker row!
				String sampleHeader = l;
				String[] headerFields = sampleHeader.split(cImport.Separators.separators_SpaceTab_rgxp);
				for (int i = Standard.genotypes; i < headerFields.length; i = i + 2) {
					String sampleId = headerFields[i];
					// NOTE The Beagle format does not have a family-ID
					sampleOrderMap.put(new SampleKey(studyKey, sampleId, SampleKey.FAMILY_ID_NONE), i);
				}
			}
			if (l.startsWith("M")) { // Found first marker row!
				// GET ALLELES FROM MARKER ROWS
				String[] cVals = l.split(cImport.Separators.separators_SpaceTab_rgxp);
				MarkerKey markerKey = MarkerKey.valueOf(cVals[Standard.markerId]);

				Integer columnNb = sampleOrderMap.get(sampleKey);
				if (columnNb != null) {
					String strAlleles = cVals[columnNb] + cVals[columnNb + 1];
					byte[] tmpAlleles = new byte[] {
						(byte) strAlleles.toString().charAt(0),
						(byte) strAlleles.toString().charAt(1)};
					tempMarkerIdMap.put(markerKey, tmpAlleles);
				}
			}
		}
		inputBufferReader.close();

		wrMarkerSetMap.putAll(tempMarkerIdMap);

		GenotypeEncoding guessedGTCode = GenotypeEncoding.UNKNOWN;
		if (guessedGTCode.equals(GenotypeEncoding.UNKNOWN)) {
			guessedGTCode = Utils.detectGTEncoding(wrMarkerSetMap);
		} else if (guessedGTCode.equals(GenotypeEncoding.O12)) {
			guessedGTCode = Utils.detectGTEncoding(wrMarkerSetMap);
		}
	}
	//</editor-fold>
}
