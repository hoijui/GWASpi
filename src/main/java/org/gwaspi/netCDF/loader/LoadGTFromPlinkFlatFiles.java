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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.Annotation.Plink_Standard;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cImport.StrandFlags;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.global.Text;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.StudyKey;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFileWriteable;

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
	protected void loadGenotypes(
			GenotypesLoadDescription loadDescription,
			Collection<SampleInfo> sampleInfos,
			Map<MarkerKey, MarkerMetadata> markerSetMap,
			NetcdfFileWriteable ncfile,
			List<SampleKey> sampleKeys,
			GenotypeEncoding guessedGTCode)
			throws IOException, InvalidRangeException
	{
		Map<MarkerKey, byte[]> mapMarkerSetMap = MetadataLoaderPlink.parseOrigMapFile(loadDescription.getGtDirPath());
		loadPedGenotypes(
				loadDescription.getStudyKey(),
				new File(loadDescription.getAnnotationFilePath()),
				ncfile,
				markerSetMap.keySet(),
				mapMarkerSetMap,
				sampleKeys,
				guessedGTCode);
	}

	public void loadPedGenotypes(
			StudyKey studyKey,
			File file,
			NetcdfFileWriteable ncfile,
			Collection<MarkerKey> wrMarkerKeys,
			Map<MarkerKey, ?> mapMarkerSetMap,
			List<SampleKey> sampleKeys,
			GenotypeEncoding guessedGTCode)
			throws IOException, InvalidRangeException
	{
		FileReader inputFileReader = new FileReader(file);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		// GET ALLELES
		String l;
		while ((l = inputBufferReader.readLine()) != null) {
			// PURGE WRITE MARKER SET
			Map<MarkerKey, byte[]> allelesMap = AbstractLoadGTFromFiles.fillMap(wrMarkerKeys, cNetCDF.Defaults.DEFAULT_GT);

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

			if (guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.UNKNOWN)) {
				guessedGTCode = Utils.detectGTEncoding(allelesMap);
			} else if (guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.O12)) {
				guessedGTCode = Utils.detectGTEncoding(allelesMap);
			}

			// WRITING GENOTYPE DATA INTO netCDF FILE
			int sampleIndex = sampleKeys.indexOf(new SampleKey(studyKey, sampleId, familyId));
			if (sampleIndex != -1) {  //CHECK IF CURRENT SAMPLE IS KNOWN IN SAMPLEINFO FILE!!
				org.gwaspi.netCDF.operations.Utils.saveSingleSampleGTsToMatrix(ncfile, allelesMap, sampleIndex);
			}
		}
		inputBufferReader.close();
	}
}
