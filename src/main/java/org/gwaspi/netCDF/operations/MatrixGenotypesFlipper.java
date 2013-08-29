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
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.global.Text;
import org.gwaspi.model.ChromosomeInfo;
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MarkersKeysSource;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.loader.AbstractNetCDFDataSetDestination;
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
	private final Set<MarkerKey> markerFlipHS;
	private final DataSetSource rdDataSetSource;

	/**
	 * This constructor to extract data from Matrix a by passing a variable and
	 * the criteria to filter items by.
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

		this.markerFlipHS = new HashSet<MarkerKey>();
		if (this.flipperFile.isFile()) {
			FileReader fr = new FileReader(this.flipperFile);
			BufferedReader br = new BufferedReader(fr);
			String line;
			while ((line = br.readLine()) != null) {
				this.markerFlipHS.add(MarkerKey.valueOf(line));
			}
			br.close();
		}
	}

	@Override
	public int processMatrix() throws IOException {
		int resultMatrixId = Integer.MIN_VALUE;
		try {
			// CREATE netCDF-3 FILE
			StringBuilder descSB = new StringBuilder(Text.Matrix.descriptionHeader1);
			descSB.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
			descSB.append("\nThrough Matrix genotype flipping from parent Matrix MX: ").append(rdDataSetSource.getMatrixMetadata().getMatrixId()).append(" - ").append(rdDataSetSource.getMatrixMetadata().getMatrixFriendlyName());
			descSB.append("\nUsed list of markers to be flipped: ").append(flipperFile.getPath());
			if (!wrMatrixDescription.isEmpty()) {
				descSB.append("\n\nDescription: ");
				descSB.append(wrMatrixDescription);
				descSB.append("\n");
			}
			descSB.append("\nGenotype encoding: ");
			descSB.append(rdDataSetSource.getMatrixMetadata().getGenotypeEncoding());
			descSB.append("\n");
			descSB.append("Markers: ").append(rdDataSetSource.getMarkersKeysSource().size());
			descSB.append(", Samples: ").append(rdDataSetSource.getSamplesKeysSource().size());

			MatrixFactory wrMatrixHandler = new MatrixFactory(
					rdDataSetSource.getMatrixMetadata().getTechnology(), // technology
					wrMatrixFriendlyName,
					descSB.toString(), // description
					rdDataSetSource.getMatrixMetadata().getGenotypeEncoding(), // Matrix genotype encoding from orig matrix genotype encoding
					StrandType.valueOf("FLP"), // FIXME this will fail at runtime
					rdDataSetSource.getMatrixMetadata().getHasDictionray(), // has dictionary?
					rdDataSetSource.getSamplesKeysSource().size(),
					rdDataSetSource.getMarkersKeysSource().size(),
					rdDataSetSource.getMarkersChromosomeInfosSource().size(),
					rdMatrixKey, // Orig matrixId 1
					null); // Orig matrixId 2

			resultMatrixId = wrMatrixHandler.getResultMatrixId();

			final MarkersKeysSource rdMarkerOrder = rdDataSetSource.getMarkersKeysSource();

			NetcdfFile rdNcFile = NetcdfFile.open(rdDataSetSource.getMatrixMetadata().getPathToMatrix());
			NetcdfFileWriteable wrNcFile = wrMatrixHandler.getNetCDFHandler();
			wrNcFile.create();
			log.trace("Done creating netCDF handle: " + wrNcFile.toString());

			// WRITING METADATA TO MATRIX
			AbstractNetCDFDataSetDestination.saveSamplesMatadata(rdDataSetSource.getSamplesKeysSource(), wrNcFile);
			saveMarkersMatadata(rdMarkerOrder, rdChrInfoSetMap, hasDictionary, strandFlag, wrNcFile);

			// WRITE GENOTYPES
			log.info(Text.All.processing);
			int markerIndex = 0;
			final GenotypeEncoding gtEncoding = rdDataSetSource.getMatrixMetadata().getGenotypeEncoding();
			for (MarkerKey markerKey : rdMarkerOrder) {
				rdSampleSet.readAllSamplesGTsFromCurrentMarkerToMap(rdNcFile, rdSampleSetMap, markerIndex);

				if (markerFlipHS.contains(markerKey)) {
					for (Map.Entry<SampleKey, byte[]> sampleEntry : rdSampleSetMap.entrySet()) {
						// we deal with references here, so we change the value
						// in the map. no need to explicitly write it back.
						byte[] gt = sampleEntry.getValue();
						flipGenotypes(gt, gtEncoding);
					}
				}

				// Write rdMarkerIdSetMap to A3 ArrayChar and save to wrMatrix
				NetCdfUtils.saveSingleMarkerGTsToMatrix(wrNcFile, rdSampleSetMap.values(), markerIndex);
				markerIndex++;
				if ((markerIndex == 1) || ((markerIndex % 10000) == 0)) {
					log.info("Markers processed: {} / {}", markerIndex, rdMarkerOrder.size());
				}
			}

			// CLOSE THE FILE AND BY THIS, MAKE IT READ-ONLY
			// GENOTYPE ENCODING
			ArrayChar.D2 guessedGTCodeAC = new ArrayChar.D2(1, 8);
			Index index = guessedGTCodeAC.getIndex();
			guessedGTCodeAC.setString(index.set(0, 0), rdMatrixMetadata.getGenotypeEncoding().toString());
			int[] origin = new int[] {0, 0};
			wrNcFile.write(cNetCDF.Variables.GLOB_GTENCODING, origin, guessedGTCodeAC);

			descSB.append("\nGenotype encoding: ");
			descSB.append(rdMatrixMetadata.getGenotypeEncoding());

			MatrixMetadata resultMatrixMetadata = wrMatrixHandler.getResultMatrixMetadata();
			resultMatrixMetadata.setDescription(descSB.toString());
			MatricesList.updateMatrix(resultMatrixMetadata);

			wrNcFile.close();

			org.gwaspi.global.Utils.sysoutCompleted("Genotype Flipping to new Matrix");
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}

		return resultMatrixId;
	}

	public static void saveMarkersMatadata(Map<MarkerKey, MarkerMetadata> markerMetadatas, Map<ChromosomeKey, ChromosomeInfo> chromosomeInfo, boolean hasDictionary, String strandFlag, NetcdfFileWriteable wrNcFile) throws IOException, InvalidRangeException {

		ArrayChar.D2 markersD2;
		final int[] markersOrig = new int[] {0, 0};

		XXX;
		for (MarkerKey markerKey : rdMarkerOrder) {
			MarkerMetadata origMarkerMetadata = markerEntry.getValue();
			MarkerMetadata newMarkerMetadata = new MarkerMetadata(
					origMarkerMetadata.getMarkerId(),
					new String (combinedMarkerRSIDs.get(markerKey)),
					origMarkerMetadata.getChr(),
					origMarkerMetadata.getPos(),
					hasCombinedDictionary ? new String (combinedMarkerBasesDicts.get(markerKey)) : origMarkerMetadata.getAlleles(),
					new String (combinedMarkerGTStrands.get(markerKey)));
			markerEntry.setValue(newMarkerMetadata);
		}
		XXX;

		// WRITE RSID
		rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
		Map<MarkerKey, char[]> sortedMarkerRSIDs = org.gwaspi.global.Utils.createOrderedMap(markerMetadatas.keySet(), rdMarkerSet.getMarkerIdSetMapCharArray());
		NetCdfUtils.saveCharMapValueToWrMatrix(wrNcFile, sortedMarkerRSIDs.values(), cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);

		// WRITE MARKERID
		markersD2 = NetCdfUtils.writeCollectionToD2ArrayChar(markerMetadatas.keySet(), cNetCDF.Strides.STRIDE_MARKER_NAME);
		wrNcFile.write(cNetCDF.Variables.VAR_MARKERSET, markersOrig, markersD2);
		log.info("Done writing MarkerId to matrix");

		// WRITE CHROMOSOME
		rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
		Map<MarkerKey, char[]> sortedMarkerCHRs = org.gwaspi.global.Utils.createOrderedMap(markerMetadatas.keySet(), rdMarkerSet.getMarkerIdSetMapCharArray());
		NetCdfUtils.saveCharMapValueToWrMatrix(wrNcFile, sortedMarkerCHRs.values(), cNetCDF.Variables.VAR_MARKERS_CHR, cNetCDF.Strides.STRIDE_CHR);

		// Set of chromosomes found in matrix along with number of markersinfo
		NetCdfUtils.saveObjectsToStringToMatrix(wrNcFile, chromosomeInfo.keySet(), cNetCDF.Variables.VAR_CHR_IN_MATRIX, 8);
		// Number of marker per chromosome & max pos for each chromosome
		int[] columns = new int[] {0, 1, 2, 3};
		NetCdfUtils.saveChromosomeInfosD2ToWrMatrix(wrNcFile, chromosomeInfo.values(), columns, cNetCDF.Variables.VAR_CHR_INFO);

		// WRITE POSITION
		rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
		Map<MarkerKey, Integer> sortedMarkerPos = org.gwaspi.global.Utils.createOrderedMap(markerMetadatas.keySet(), rdMarkerSet.getMarkerIdSetMapInteger());
		//Utils.saveCharMapValueToWrMatrix(wrNcFile, sortedMarkerPos, cNetCDF.Variables.VAR_MARKERS_POS, cNetCDF.Strides.STRIDE_POS);
		NetCdfUtils.saveIntMapD1ToWrMatrix(wrNcFile, sortedMarkerPos.values(), cNetCDF.Variables.VAR_MARKERS_POS);

		// WRITE DICTIONARY ALLELES
		rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_BASES_DICT);
		Map<MarkerKey, char[]> sortedMarkerBasesDicts = org.gwaspi.global.Utils.createOrderedMap(markerMetadatas.keySet(), rdMarkerSet.getMarkerIdSetMapCharArray());
		for (Map.Entry<MarkerKey, char[]> entry : sortedMarkerBasesDicts.entrySet()) {
			MarkerKey markerKey = entry.getKey();
			if (markerFlipHS.contains(markerKey)) {
				String alleles = new String(entry.getValue());
				alleles = flipDictionaryAlleles(alleles);
				entry.setValue(alleles.toCharArray());
			}
		}
		NetCdfUtils.saveCharMapValueToWrMatrix(wrNcFile, sortedMarkerBasesDicts.values(), cNetCDF.Variables.VAR_MARKERS_BASES_DICT, cNetCDF.Strides.STRIDE_GT);

		// WRITE GENOTYPE STRAND
		rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_GT_STRAND);
		Map<MarkerKey, char[]> sortedMarkerGTStrands = org.gwaspi.global.Utils.createOrderedMap(markerMetadatas.keySet(), rdMarkerSet.getMarkerIdSetMapCharArray());

		for (Map.Entry<MarkerKey, char[]> entry : sortedMarkerGTStrands.entrySet()) {
			MarkerKey markerKey = entry.getKey();
			if (markerFlipHS.contains(markerKey)) {
				String strand = new String(entry.getValue());
				strand = flipStranding(strand);
				entry.setValue(strand.toCharArray());
			}
		}
		NetCdfUtils.saveCharMapValueToWrMatrix(wrNcFile, sortedMarkerGTStrands.values(), cNetCDF.Variables.VAR_GT_STRAND, 3);
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
