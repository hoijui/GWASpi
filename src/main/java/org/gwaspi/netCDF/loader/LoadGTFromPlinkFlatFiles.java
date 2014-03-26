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
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.Annotation.Plink_Standard;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.model.DataSet;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.StudyKey;

public class LoadGTFromPlinkFlatFiles extends AbstractLoadGTFromFiles implements GenotypesLoader {

	public LoadGTFromPlinkFlatFiles() {
		super(new MetadataLoaderPlink(), ImportFormat.PLINK, null, false);
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
	protected void addAdditionalBigDescriptionProperties(StringBuilder description, GenotypesLoadDescription loadDescription) {
		super.addAdditionalBigDescriptionProperties(description, loadDescription);

		description.append(loadDescription.getGtDirPath());
		description.append(" (MAP file)\n");
		description.append(loadDescription.getAnnotationFilePath());
		description.append(" (PED file)\n");
		if (new File(loadDescription.getSampleFilePath()).exists()) {
			description.append(loadDescription.getSampleFilePath());
			description.append(" (Sample Info file)\n");
		}
	}

	@Override
	protected void loadGenotypes(
			GenotypesLoadDescription loadDescription,
			DataSetDestination samplesReceiver)
			throws Exception
	{
		File file = new File(loadDescription.getAnnotationFilePath());
		FileReader inputFileReader = new FileReader(file);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		DataSet dataSet = ((AbstractDataSetDestination) samplesReceiver).getDataSet(); // HACK

		List<SampleKey> sampleKeys = AbstractLoadGTFromFiles.extractKeys(dataSet.getSampleInfos());

		final int numMarkers = dataSet.getMarkerMetadatas().size();

		// GET ALLELES
		String l;
		while ((l = inputBufferReader.readLine()) != null) {
			// PURGE WRITE MARKER SET
			List<byte[]> markerAlleles = new ArrayList<byte[]>(numMarkers);

			StringTokenizer st = new StringTokenizer(l, cImport.Separators.separators_CommaSpaceTab_rgxp);

			// skip to genotype data
			String familyId = "";
			String sampleId = "";
			int i = 0;
			while (i < Plink_Standard.ped_genotypes) {
				if (i == Plink_Standard.ped_sampleId) {
					sampleId = st.nextToken();
				} else if (i == Plink_Standard.ped_familyId) {
					familyId = st.nextToken();
				} else {
					st.nextToken();
				}
				i++;
			}

			// Parse genotypes from this point on

			// This would require to parse the line two times,
			// and if things are in order,
			// it should be the same like numMarkers.
//			final int numAvailableMarkers = st.countTokens() / 2;
			for (int mi = 0; mi < numMarkers; mi++) {
				byte[] alleles = new byte[] {
						(byte) (st.nextToken().charAt(0)),
						(byte) (st.nextToken().charAt(0))};
				markerAlleles.add(alleles);
			}
			// This would only possibly happen if we woudl allow an incompleete
			// list of genotypes in the step above.
//			if (markerAlleles.size() < numMarkers) {
//				markerAlleles.addAll(Collections.nCopies(numMarkers - markerAlleles.size(), cNetCDF.Defaults.DEFAULT_GT));
//			}

//			GenotypeEncoding guessedGTCode = getGuessedGTCode();
//			if (guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.UNKNOWN)
//					|| guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.O12))
//			{
//				guessedGTCode = Utils.detectGTEncoding(allelesMap);
//			}

			// WRITING GENOTYPE DATA INTO netCDF FILE
			int sampleIndex = sampleKeys.indexOf(new SampleKey(loadDescription.getStudyKey(), sampleId, familyId));
			if (sampleIndex != -1) { // CHECK IF CURRENT SAMPLE IS KNOWN IN SAMPLEINFO FILE!!
				samplesReceiver.addSampleGTAlleles(sampleIndex, markerAlleles);
			}
		}
		inputBufferReader.close();
	}
}
