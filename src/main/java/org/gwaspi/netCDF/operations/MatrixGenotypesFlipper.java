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

package org.gwaspi.netCDF.operations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.global.Text;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.netCDF.loader.DataSetDestination;
import org.gwaspi.netCDF.markers.NetCDFDataSetSource;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayChar;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;

public class MatrixGenotypesFlipper implements MatrixOperation {

	private final Logger log = LoggerFactory.getLogger(MatrixGenotypesFlipper.class);

	private final MatrixKey rdMatrixKey;
	private final String wrMatrixFriendlyName;
	private final String wrMatrixDescription;
	private final File flipperFile;
	private final Set<MarkerKey> markersToFlip;
	private final DataSetSource rdDataSetSource;
	private final DataSetDestination dataSetDestination;

	/**
	 * Use this constructor to extract data from a matrix
	 * by passing a variable and the criteria to filter items by.
	 *
	 * @param rdMatrixKey
	 * @param wrMatrixFriendlyName
	 * @param wrMatrixDescription
	 * @param markerVariable
	 * @param flipperFile
	 * @throws IOException
	 */
	public MatrixGenotypesFlipper(
			MatrixKey rdMatrixKey,
			String wrMatrixFriendlyName,
			String wrMatrixDescription,
			String markerVariable,
			File flipperFile)
			throws IOException
	{
		// INIT EXTRACTOR OBJECTS
		this.rdMatrixKey = rdMatrixKey;
		this.wrMatrixFriendlyName = wrMatrixFriendlyName;
		this.wrMatrixDescription = wrMatrixDescription;
		this.flipperFile = flipperFile;

		this.rdDataSetSource = new NetCDFDataSetSource(this.rdMatrixKey);

		this.markersToFlip = new HashSet<MarkerKey>();
		if (this.flipperFile.isFile()) {
			FileReader fr = new FileReader(this.flipperFile);
			BufferedReader br = new BufferedReader(fr);
			String line;
			while ((line = br.readLine()) != null) {
				this.markersToFlip.add(MarkerKey.valueOf(line));
			}
			br.close();
		}
	}

	private MatrixFactory createMatrixFactory() throws IOException {

			StringBuilder description = new StringBuilder();
			description.append(Text.Matrix.descriptionHeader1);
			description.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
			description.append("\nThrough Matrix genotype flipping from parent Matrix MX: ").append(rdDataSetSource.getMatrixMetadata().getMatrixId());
			description.append(" - ").append(rdDataSetSource.getMatrixMetadata().getMatrixFriendlyName());
			description.append("\nUsed list of markers to be flipped: ").append(flipperFile.getPath());
			if (!wrMatrixDescription.isEmpty()) {
				description.append("\n\nDescription: ");
				description.append(wrMatrixDescription);
				description.append("\n");
			}
			description.append("\nGenotype encoding: ");
			description.append(rdDataSetSource.getMatrixMetadata().getGenotypeEncoding());
			description.append("\n");
			description.append("Markers: ").append(rdDataSetSource.getMarkersKeysSource().size());
			description.append(", Samples: ").append(rdDataSetSource.getSamplesKeysSource().size());

		try {
			return new MatrixFactory(
					rdDataSetSource.getMatrixMetadata().getTechnology(), // technology
					wrMatrixFriendlyName,
					description.toString(), // description
					rdDataSetSource.getMatrixMetadata().getGenotypeEncoding(), // Matrix genotype encoding from orig matrix genotype encoding
					StrandType.valueOf("FLP"), // FIXME this will fail at runtime
					rdDataSetSource.getMatrixMetadata().getHasDictionray(), // has dictionary?
					rdDataSetSource.getSamplesKeysSource().size(),
					rdDataSetSource.getMarkersKeysSource().size(),
					rdDataSetSource.getMarkersChromosomeInfosSource().size(),
					rdMatrixKey, // Orig matrixId 1
					null); // Orig matrixId 2
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}
	}

	@Override
	public int processMatrix() throws IOException {
		int resultMatrixId = Integer.MIN_VALUE;

		MatrixFactory wrMatrixHandler = createMatrixFactory();
		try {
			resultMatrixId = wrMatrixHandler.getResultMatrixId();

			NetcdfFile rdNcFile = NetcdfFile.open(rdDataSetSource.getMatrixMetadata().getPathToMatrix());
			NetcdfFileWriteable wrNcFile = wrMatrixHandler.getNetCDFHandler();
			wrNcFile.create();
			log.trace("Done creating netCDF handle: " + wrNcFile.toString());

			// simply copy&paste the sample infos
			dataSetDestination.startLoadingSampleInfos();
			for (SampleInfo sampleInfo : rdDataSetSource.getSamplesInfosSource()) {
				dataSetDestination.addSampleInfo(sampleInfo);
			}
			dataSetDestination.finishedLoadingSampleInfos();

			// copy&paste the marker metadata aswell,
			// but flipp dictionary-alleles and strand
			// of the ones that are selected for flipping
			dataSetDestination.startLoadingMarkerMetadatas();
			Iterator<MarkerKey> markerKeysIt = rdDataSetSource.getMarkersKeysSource().iterator();
			for (MarkerMetadata origMarkerMetadata : rdDataSetSource.getMarkersMetadatasSource()) {
				MarkerKey markerKey = markerKeysIt.next();

				MarkerMetadata newMarkerMetadata;
				if (markersToFlip.contains(markerKey)) {
					String alleles = flipDictionaryAlleles(origMarkerMetadata.getAlleles());
					String strand = flipStranding(origMarkerMetadata.getStrand());

					newMarkerMetadata = new MarkerMetadata(
							origMarkerMetadata.getMarkerId(),
							origMarkerMetadata.getRsId(),
							origMarkerMetadata.getChr(),
							origMarkerMetadata.getPos(),
							alleles,
							strand);
				} else {
					newMarkerMetadata = origMarkerMetadata;
				}
				dataSetDestination.addMarkerMetadata(newMarkerMetadata);
			}
			dataSetDestination.finishedLoadingMarkerMetadatas();


			// WRITE GENOTYPES
			dataSetDestination.startLoadingAlleles(false);
			log.info(Text.All.processing);
			int markerIndex = 0;
			final GenotypeEncoding gtEncoding = rdDataSetSource.getMatrixMetadata().getGenotypeEncoding();
			markerKeysIt = rdDataSetSource.getMarkersKeysSource().iterator();
			for (GenotypesList markerGenotypes : rdDataSetSource.getMarkersGenotypesSource()) {
				MarkerKey markerKey = markerKeysIt.next();
				if (markersToFlip.contains(markerKey)) {
					for (byte[] gt : markerGenotypes) {
						// we deal with references here, so we change the value
						// in the map. no need to explicitly write it back.
						flipGenotypes(gt, gtEncoding);
					}
				}

				// Write rdMarkerIdSetMap to A3 ArrayChar and save to wrMatrix
				dataSetDestination.addMarkerGTAlleles(markerIndex, markerGenotypes);
				markerIndex++;
				if ((markerIndex == 1) || ((markerIndex % 10000) == 0)) {
					log.info("Markers processed: {} / {}", markerIndex, rdMarkerOrder.size());
				}
			}
			dataSetDestination.finishedLoadingAlleles();

			// TODO this should be done in the NetCDFDataSetDestination. it should already have the gtEncoding through the matrixFactory, or matrixMetadata
			GenotypeEncoding genotypeEncoding = rdMatrixMetadata.getGenotypeEncoding();
			// CLOSE THE FILE AND BY THIS, MAKE IT READ-ONLY
			// GENOTYPE ENCODING
			ArrayChar.D2 guessedGTCodeAC = new ArrayChar.D2(1, 8);
			Index index = guessedGTCodeAC.getIndex();
			guessedGTCodeAC.setString(index.set(0, 0), genotypeEncoding.toString());
			int[] origin = new int[] {0, 0};
			wrNcFile.write(cNetCDF.Variables.GLOB_GTENCODING, origin, guessedGTCodeAC);
			wrNcFile.close();

			org.gwaspi.global.Utils.sysoutCompleted("Genotype Flipping to new Matrix");
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}

		return resultMatrixId;
	}

	private static String flipDictionaryAlleles(String alleles) {

		String flippedAlleles = alleles;

		flippedAlleles = flippedAlleles.replaceAll("A", "t");
		flippedAlleles = flippedAlleles.replaceAll("C", "g");
		flippedAlleles = flippedAlleles.replaceAll("G", "c");
		flippedAlleles = flippedAlleles.replaceAll("T", "a");
		flippedAlleles = flippedAlleles.toUpperCase();

		return flippedAlleles;
	}

	private static String flipStranding(String strand) {
		if (strand.equals("+")) {
			return "-";
		} else if (strand.equals("-")) {
			return "+";
		} else {
			return strand;
		}
	}

	private static void flipGenotypes(byte[] gt, GenotypeEncoding gtEncoding) {
		byte[] result = gt;

		if (gtEncoding.equals(cNetCDF.Defaults.GenotypeEncoding.ACGT0)) {
			for (int i = 0; i < gt.length; i++) {
				if (gt[i] == cNetCDF.Defaults.AlleleBytes.A) {
					result[i] = cNetCDF.Defaults.AlleleBytes.T;
				} else if (gt[i] == cNetCDF.Defaults.AlleleBytes.C) {
					result[i] = cNetCDF.Defaults.AlleleBytes.G;
				} else if (gt[i] == cNetCDF.Defaults.AlleleBytes.G) {
					result[i] = cNetCDF.Defaults.AlleleBytes.C;
				} else if (gt[i] == cNetCDF.Defaults.AlleleBytes.T) {
					result[i] = cNetCDF.Defaults.AlleleBytes.A;
				}
			}
		}

		if (gtEncoding.equals(cNetCDF.Defaults.GenotypeEncoding.O1234)) {
			for (int i = 0; i < gt.length; i++) {
				if (gt[i] == cNetCDF.Defaults.AlleleBytes._1) {
					result[i] = cNetCDF.Defaults.AlleleBytes._4;
				} else if (gt[i] == cNetCDF.Defaults.AlleleBytes._2) {
					result[i] = cNetCDF.Defaults.AlleleBytes._3;
				} else if (gt[i] == cNetCDF.Defaults.AlleleBytes._3) {
					result[i] = cNetCDF.Defaults.AlleleBytes._2;
				} else if (gt[i] == cNetCDF.Defaults.AlleleBytes._4) {
					result[i] = cNetCDF.Defaults.AlleleBytes._1;
				}
			}
		}
	}
}
