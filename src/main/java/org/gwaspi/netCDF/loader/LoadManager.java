package org.gwaspi.netCDF.loader;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfoList;
import ucar.ma2.InvalidRangeException;

public class LoadManager {

	private static final Map<ImportFormat, GenotypesLoader> genotypesLoaders;

	static {
		genotypesLoaders = new EnumMap<ImportFormat, GenotypesLoader>(ImportFormat.class);
		genotypesLoaders.put(ImportFormat.Affymetrix_GenomeWide6, new LoadGTFromAffyFiles());
		genotypesLoaders.put(ImportFormat.PLINK, new LoadGTFromPlinkFlatFiles());
		genotypesLoaders.put(ImportFormat.PLINK_Binary, new LoadGTFromPlinkBinaryFiles());
		genotypesLoaders.put(ImportFormat.HAPMAP, new LoadGTFromHapmapFiles());
		genotypesLoaders.put(ImportFormat.BEAGLE, new LoadGTFromBeagleFiles());
		genotypesLoaders.put(ImportFormat.HGDP1, new LoadGTFromHGDP1Files());
		genotypesLoaders.put(ImportFormat.Illumina_LGEN, new LoadGTFromIlluminaLGENFiles());
		genotypesLoaders.put(ImportFormat.GWASpi, new LoadGTFromGWASpiFiles());
		genotypesLoaders.put(ImportFormat.Sequenom, new LoadGTFromSequenomFiles());
	}

	private LoadManager() {
	}

	public static int dispatchLoadByFormat(
			GenotypesLoadDescription loadDescription,
			Collection<SampleInfo> sampleInfos)
			throws IOException, InvalidRangeException, InterruptedException
	{
		int newMatrixId = Integer.MIN_VALUE;

		GenotypesLoader genotypesLoader = genotypesLoaders.get(loadDescription.getFormat());
		if (genotypesLoader == null) {
			throw new IOException("No Genotypes-Loader found for format " + loadDescription.getFormat());
		} else {
			SampleInfoList.insertSampleInfos(loadDescription.getStudyId(), sampleInfos);
			newMatrixId = genotypesLoader.processData(loadDescription, sampleInfos);
		}

		return newMatrixId;
	}
}
