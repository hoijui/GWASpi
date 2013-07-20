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
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.Annotation.Plink_Standard;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.DataSet;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.StudyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadGTFromPlinkFlatFiles extends AbstractLoadGTFromFiles implements GenotypesLoader {

	private final Logger log
			= LoggerFactory.getLogger(LoadGTFromPlinkFlatFiles.class);

	public LoadGTFromPlinkFlatFiles() {
		super(ImportFormat.PLINK, null, false, null);
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
		descSB.append(" (MAP file)\n");
		descSB.append(loadDescription.getAnnotationFilePath());
		descSB.append(" (PED file)\n");
		if (new File(loadDescription.getSampleFilePath()).exists()) {
			descSB.append(loadDescription.getSampleFilePath());
			descSB.append(" (Sample Info file)\n");
		}
	}

	@Override
	protected MetadataLoader createMetaDataLoader(GenotypesLoadDescription loadDescription) {

		return new MetadataLoaderPlink(
				loadDescription.getGtDirPath(),
				loadDescription.getStudyKey());
	}

	@Override
//	protected void loadGenotypes(
//			GenotypesLoadDescription loadDescription,
//			Collection<SampleInfo> sampleInfos,
//			Map<MarkerKey, MarkerMetadata> markerSetMap,
//			NetcdfFileWriteable ncfile,
//			List<SampleKey> sampleKeys,
//			GenotypeEncoding guessedGTCode)
//			throws IOException, InvalidRangeException
//	{
	protected void loadGenotypes(
			GenotypesLoadDescription loadDescription,
			SamplesReceiver samplesReceiver)
			throws Exception
	{
		Map<MarkerKey, byte[]> mapMarkerSetMap = MetadataLoaderPlink.parseOrigMapFile(loadDescription.getGtDirPath());
//		loadPedGenotypes(
//				loadDescription.getStudyKey(),
//				new File(loadDescription.getAnnotationFilePath()),
//				ncfile,
//				markerSetMap.keySet(),
//				mapMarkerSetMap,
//				sampleKeys,
//				guessedGTCode);
//	}
//
//	public void loadPedGenotypes(
//			StudyKey studyKey,
//			File file,
//			NetcdfFileWriteable ncfile,
//			Collection<MarkerKey> wrMarkerKeys,
//			Map<MarkerKey, ?> mapMarkerSetMap,
//			List<SampleKey> sampleKeys,
//			GenotypeEncoding guessedGTCode)
//			throws IOException, InvalidRangeException
//	{
		File file = new File(loadDescription.getAnnotationFilePath());
		FileReader inputFileReader = new FileReader(file);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		// HACK
		DataSet dataSet = ((InMemorySamplesReceiver) samplesReceiver).getDataSet();

		List<SampleKey> sampleKeys = AbstractLoadGTFromFiles.extractKeys(dataSet.getSampleInfos());

		// GET ALLELES
		String l;
		while ((l = inputBufferReader.readLine()) != null) {
			// PURGE WRITE MARKER SET
			Map<MarkerKey, byte[]> allelesMap = AbstractLoadGTFromFiles.fillMap(dataSet.getMarkerMetadatas().keySet(), cNetCDF.Defaults.DEFAULT_GT);

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

			// read genotypes from this point on
			for (MarkerKey markerKey : mapMarkerSetMap.keySet()) {
				byte[] alleles = new byte[] {
						(byte) (st.nextToken().charAt(0)),
						(byte) (st.nextToken().charAt(0))};
				allelesMap.put(markerKey, alleles);
			}
			st = null;

//			GenotypeEncoding guessedGTCode = getGuessedGTCode();
//			if (guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.UNKNOWN)
//					|| guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.O12))
//			{
//				guessedGTCode = Utils.detectGTEncoding(allelesMap);
//			}

			// WRITING GENOTYPE DATA INTO netCDF FILE
			int sampleIndex = sampleKeys.indexOf(new SampleKey(loadDescription.getStudyKey(), sampleId, familyId));
			if (sampleIndex != -1) {  //CHECK IF CURRENT SAMPLE IS KNOWN IN SAMPLEINFO FILE!!
				samplesReceiver.addSampleGTAlleles(allelesMap.values());
			}
		}
		inputBufferReader.close();
	}
}
