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

package org.gwaspi.netCDF;

import java.io.File;
import java.util.List;
import org.gwaspi.constants.NetCDFConstants;
import org.gwaspi.datasource.netcdf.NetCdfMarkersGenotypesSource;
import org.gwaspi.model.ArrayGenotypesList;
import org.gwaspi.model.CompactGenotypesList;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.GenotypesListFactory;
import org.gwaspi.operations.NetCdfUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class BenchmarkReadGTs {

	private static final Logger log = LoggerFactory.getLogger(BenchmarkReadGTs.class);

	public static void main(String[] args) throws Exception {

		final File netCdfGTs;
		if (args.length >= 1) {
			netCdfGTs = new File(args[0]);
		} else {
			netCdfGTs = new File(System.getProperty("user.home")
				+ "/Projects/GWASpi/var/dataStore/testing/genotypes/STUDY_1/GT_32814153120CET.nc");
		}

		if (!netCdfGTs.exists() || !netCdfGTs.isFile()) {
			log.error("Please make sure to supply the path to an existing, valid, GWASpi NetCDF GTs file. This is not so: " + netCdfGTs.getAbsolutePath());
		}

		final GenotypesListFactory[] genotypesListFactories
				= new GenotypesListFactory[] {
					ArrayGenotypesList.FACTORY,
					CompactGenotypesList.FACTORY};
		final boolean[] readInBulkValues = new boolean[] {true, false};
		final int[] chunkSizes = new int[] {1, 10, 100, 1000};
		final boolean[] arrayCopyStates = new boolean[] {true, false};

		log.info("WCT(ms)\tGT-fac\trd-bulk\tchunk-s\tarray-c");
		for (GenotypesListFactory genotypesListFactory : genotypesListFactories) {
			final String genotypesListFactoryName = genotypesListFactory.getClass().getSimpleName().replace("GenotypesListFactory", "");
			for (boolean readInBulk : readInBulkValues) {
				for (int chunkSize : chunkSizes) {
					for (boolean arrayCopy : arrayCopyStates) {
						NetCdfUtils.ARRAY_COPY = arrayCopy;
						final long wallClockTime = readAllGTs(netCdfGTs, genotypesListFactory, readInBulk, chunkSize);
						log.info(
								"{}\t{}\t{}\t{}\t{}",
								wallClockTime,
								genotypesListFactoryName,
								readInBulk ? "yes" : "no",
								chunkSize,
								arrayCopy ? "yes" : "no"
						);
					}
				}
			}
		}
	}

	private static long readAllGTs(final File netCdfGTs, final GenotypesListFactory genotypesListFactory, final boolean readInBulk, final int chunkSize) throws Exception {

		final NetcdfFile rdNetCdf = NetcdfFile.open(netCdfGTs.getAbsolutePath());

		final Variable var = rdNetCdf.findVariable(NetCDFConstants.Variables.VAR_GENOTYPES);
		final int[] shape = var.getShape();
		final int numMarkers = shape[0];
		final int numChunks = (int) Math.ceil((double) numMarkers / chunkSize);
		final long startTime = System.currentTimeMillis();
		for (int ci = 0; ci < numChunks; ci++) {
			final int chunkFirstMarker = ci * chunkSize;
			final int chunkLastMarker = Math.min(chunkFirstMarker + chunkSize, numMarkers);
			final List<GenotypesList> throwAway
					= NetCdfMarkersGenotypesSource.readMarkerGTs(rdNetCdf,
							NetCDFConstants.Variables.VAR_GENOTYPES,
							chunkFirstMarker,
							chunkLastMarker,
							genotypesListFactory,
							readInBulk);
		}
		rdNetCdf.close();
		final long endTime = System.currentTimeMillis();

		return endTime - startTime;
	}
}
