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

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.global.Text;
import org.gwaspi.model.ChromosomeInfo;
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.loader.AbstractNetCDFDataSetDestination;
import org.gwaspi.netCDF.loader.DataSetDestination;
import org.gwaspi.netCDF.loader.LoadingNetCDFDataSetDestination;
import org.gwaspi.netCDF.matrices.ChromosomeUtils;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayChar;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;

public abstract class AbstractMergeMarkersMatrixOperation extends AbstractMergeMatrixOperation {

	private static final Logger log = LoggerFactory.getLogger(AbstractMergeMarkersMatrixOperation.class);

	public AbstractMergeMarkersMatrixOperation(
			DataSetSource dataSetSource1,
			DataSetSource dataSetSource2,
			DataSetDestination dataSetDestination)
			throws IOException, InvalidRangeException
	{
		super(
				dataSetSource1,
				dataSetSource2,
				dataSetDestination);
	}

	/**
	 * Mingles markers and keeps samples constant.
	 */
	protected MatrixKey mergeMatrices(
			Map<SampleKey, int[]> wrSampleSetMap,
			Collection<SampleKey> sampleKeys,
			final int numSamples,
			final String humanReadableMethodName,
			final String methodDescription)
			throws IOException
	{
		MatrixKey resultMatrixKey = null;

		NetcdfFile rdNcFile1 = NetcdfFile.open(rdMatrix1Metadata.getPathToMatrix());
		NetcdfFile rdNcFile2 = NetcdfFile.open(rdMatrix2Metadata.getPathToMatrix());

		Map<MarkerKey, MarkerMetadata> wrCombinedSortedMarkersMetadata = mingleAndSortMarkerSet();

		// RETRIEVE CHROMOSOMES INFO
		Map<ChromosomeKey, ChromosomeInfo> chromosomeInfo = ChromosomeUtils.aggregateChromosomeInfo(wrCombinedSortedMarkersMetadata, 0, 1);

		LoadingNetCDFDataSetDestination netCDFSaverSamplesReceiver = null;
		try {
			// CREATE netCDF-3 FILE
			boolean hasDictionary = false;
			if (rdMatrix1Metadata.getHasDictionray() == rdMatrix2Metadata.getHasDictionray()) {
				hasDictionary = rdMatrix1Metadata.getHasDictionray();
			}
			GenotypeEncoding gtEncoding = GenotypeEncoding.UNKNOWN;
			if (rdMatrix1Metadata.getGenotypeEncoding().equals(rdMatrix2Metadata.getGenotypeEncoding())) {
				gtEncoding = rdMatrix1Metadata.getGenotypeEncoding();
			}
			ImportFormat technology = ImportFormat.UNKNOWN;
			if (rdMatrix1Metadata.getTechnology().equals(rdMatrix2Metadata.getTechnology())) {
				technology = rdMatrix1Metadata.getTechnology();
			}

			StringBuilder descSB = new StringBuilder(Text.Matrix.descriptionHeader1);
			descSB.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
			descSB.append("\n");
			descSB.append("Markers: ").append(wrCombinedSortedMarkersMetadata.size());
			descSB.append(", Samples: ").append(numSamples);
			descSB.append("\n");
			descSB.append(Text.Trafo.mergedFrom);
			descSB.append("\nMX-");
			descSB.append(rdMatrix1Metadata.getMatrixId());
			descSB.append(" - ");
			descSB.append(rdMatrix1Metadata.getMatrixFriendlyName());
			descSB.append("\nMX-");
			descSB.append(rdMatrix2Metadata.getMatrixId());
			descSB.append(" - ");
			descSB.append(rdMatrix2Metadata.getMatrixFriendlyName());
			descSB.append("\n\n");
			descSB.append("Merge Method - ");
			descSB.append(humanReadableMethodName);
			descSB.append(":\n");
			descSB.append(methodDescription);

			MatrixFactory matrixFactory = new MatrixFactory(
					technology, // technology
					wrMatrixFriendlyName,
					wrMatrixDescription + "\n\n" + descSB.toString(), // description
					gtEncoding, // GT encoding
					rdMatrix1Metadata.getStrand(),
					hasDictionary, // has dictionary?
					numSamples,
					wrCombinedSortedMarkersMetadata.size(), // Use comboed wrSortedMingledMarkerMap as MarkerSet
					chromosomeInfo.size(),
					rdMatrix1Key, // Parent matrixId 1
					rdMatrix2Key); // Parent matrixId 2

			GenotypeEncoding genotypeEncoding1 = rdMatrix1Metadata.getGenotypeEncoding();

			rdMarkerSet1.initFullMarkerIdSetMap();
			rdMarkerSet2.initFullMarkerIdSetMap();
			Map<MarkerKey, char[]> combinedMarkerRSIDs = new LinkedHashMap<MarkerKey, char[]>();
			rdMarkerSet1.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
			combinedMarkerRSIDs.putAll(rdMarkerSet1.getMarkerIdSetMapCharArray());
			rdMarkerSet2.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
			combinedMarkerRSIDs.putAll(rdMarkerSet2.getMarkerIdSetMapCharArray());

			boolean hasDictionary1 = ((Integer) rdNcFile1.findGlobalAttribute(cNetCDF.Attributes.GLOB_HAS_DICTIONARY).getNumericValue() == 1);
			boolean hasDictionary2 = ((Integer) rdNcFile2.findGlobalAttribute(cNetCDF.Attributes.GLOB_HAS_DICTIONARY).getNumericValue() == 1);
			boolean hasCombinedDictionary = (hasDictionary1 && hasDictionary2);
			Map<MarkerKey, char[]> combinedMarkerBasesDicts = null;
			if (hasCombinedDictionary) {
				combinedMarkerBasesDicts = new LinkedHashMap<MarkerKey, char[]>();
				rdMarkerSet1.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_BASES_DICT);
				combinedMarkerBasesDicts.putAll(rdMarkerSet1.getMarkerIdSetMapCharArray());
				rdMarkerSet2.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_BASES_DICT);
				combinedMarkerBasesDicts.putAll(rdMarkerSet2.getMarkerIdSetMapCharArray());

				combinedMarkerBasesDicts is used in the next function, get it there;
			}

			Map<MarkerKey, char[]> combinedMarkerGTStrands = new LinkedHashMap<MarkerKey, char[]>();
			rdMarkerSet1.fillInitMapWithVariable(cNetCDF.Variables.VAR_GT_STRAND);
			combinedMarkerGTStrands.putAll(rdMarkerSet1.getMarkerIdSetMapCharArray());
			rdMarkerSet2.fillInitMapWithVariable(cNetCDF.Variables.VAR_GT_STRAND);
			combinedMarkerGTStrands.putAll(rdMarkerSet2.getMarkerIdSetMapCharArray());

			for (Map.Entry<MarkerKey, MarkerMetadata> markerEntry : wrCombinedSortedMarkersMetadata.entrySet()) {
				MarkerKey markerKey = markerEntry.getKey();
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

			NetcdfFileWriteable wrNcFile = matrixFactory.getNetCDFHandler();
			wrNcFile.create();
			log.trace("Done creating netCDF handle: " + wrNcFile.toString());

			AbstractNetCDFDataSetDestination.saveSamplesMatadata(sampleKeys, wrNcFile);
			AbstractNetCDFDataSetDestination.saveMarkersMatadata(wrCombinedSortedMarkersMetadata, chromosomeInfo, hasCombinedDictionary, null, wrNcFile);

			writeGenotypes(wrNcFile, wrSampleSetMap, wrCombinedSortedMarkersMetadata, rdSampleSetMap1, rdSampleSetMap2);

			// CLOSE THE FILE AND BY THIS, MAKE IT READ-ONLY
			// GENOTYPE ENCODING
			ArrayChar.D2 guessedGTCodeAC = new ArrayChar.D2(1, 8);
			Index index = guessedGTCodeAC.getIndex();
			guessedGTCodeAC.setString(index.set(0, 0), rdMatrix1Metadata.getGenotypeEncoding().toString());
			int[] origin = new int[] {0, 0};
			wrNcFile.write(cNetCDF.Variables.GLOB_GTENCODING, origin, guessedGTCodeAC);

			descSB.append("\nGenotype encoding: ");
			descSB.append(genotypeEncoding1.toString());

			resultMatrixKey = matrixFactory.getResultMatrixKey();

			MatrixMetadata resultMatrixMetadata = matrixFactory.getResultMatrixMetadata();
			resultMatrixMetadata.setDescription(descSB.toString());
			MatricesList.updateMatrix(resultMatrixMetadata);

			wrNcFile.close();
			rdNcFile1.close();
			rdNcFile2.close();

			// CHECK FOR MISMATCHES
			if (genotypeEncoding1.equals(GenotypeEncoding.ACGT0)
					|| genotypeEncoding1.equals(GenotypeEncoding.O1234))
			{
				double[] mismatchState = checkForMismatches(resultMatrixKey); // mismatchCount, mismatchRatio
				if (mismatchState[1] > 0.01) {
					log.warn("");
					log.warn("Mismatch ratio is bigger than 1% ({}%)!", (mismatchState[1] * 100));
					log.warn("There might be an issue with strand positioning of your genotypes!");
					log.warn("");
					//resultMatrixId = new int[] {wrMatrixHandler.getResultMatrixId(),-4};  // The threshold of acceptable mismatching genotypes has been crossed
				}
			}

			org.gwaspi.global.Utils.sysoutCompleted("extraction to new Matrix");
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}

		return resultMatrixKey;
	}

	protected abstract void writeGenotypes(
			NetcdfFileWriteable wrNcFile,
			Map<SampleKey, int[]> wrSampleSetMap,
			Map<MarkerKey, ?> wrComboSortedMarkerSetMap,
			Map<SampleKey, byte[]> rdSampleSetMap1,
			Map<SampleKey, byte[]> rdSampleSetMap2)
			throws InvalidRangeException, IOException;
}
