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

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import org.gwaspi.constants.cImport.ImportFormat;

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

	public static void dispatchLoadByFormat(
			GenotypesLoadDescription loadDescription,
			DataSetDestination samplesReceiver)
			throws Exception
	{
		GenotypesLoader genotypesLoader = genotypesLoaders.get(loadDescription.getFormat());

		if (samplesReceiver instanceof LoadingNetCDFDataSetDestination
				&& genotypesLoader instanceof AbstractLoadGTFromFiles)
		{
			((LoadingNetCDFDataSetDestination) samplesReceiver).setGTLoader((AbstractLoadGTFromFiles) genotypesLoader); // HACK
		}

		if (genotypesLoader == null) {
			throw new IOException("No Genotypes-Loader found for format " + loadDescription.getFormat());
		} else {
			genotypesLoader.processData(loadDescription, samplesReceiver);
		}
	}
}
