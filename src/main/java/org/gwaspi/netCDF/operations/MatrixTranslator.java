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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.global.Text;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.netCDF.loader.AbstractNetCDFDataSetDestination;
import org.gwaspi.netCDF.loader.DataSetDestination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatrixTranslator implements MatrixOperation {

	private final Logger log = LoggerFactory.getLogger(MatrixTranslator.class);

//	private final MatrixKey rdMatrixKey;
//	private final int wrMatrixId;
//	private final String wrMatrixFriendlyName;
//	private final String wrMatrixDescription;
//	private final MatrixMetadata rdMatrixMetadata;
//	private final MarkerSet rdMarkerSet;
//	private final SampleSet rdSampleSet;
//	private Map<MarkerKey, byte[]> wrMarkerIdSetMap;
//	private final Map<ChromosomeKey, ChromosomeInfo> rdChrInfoSetMap;
//	private final Map<SampleKey, ?> rdSampleSetMap;

	private final DataSetSource dataSetSource;
	private final DataSetDestination dataSetDestination;

	public MatrixTranslator(
			DataSetSource dataSetSource,
			DataSetDestination dataSetDestination)
			throws IOException
	{
		this.dataSetSource = dataSetSource;
		this.dataSetDestination = dataSetDestination;

//		// INIT EXTRACTOR OBJECTS
//		this.rdMatrixKey = rdMatrixKey;
//		this.rdMatrixMetadata = MatricesList.getMatrixMetadataById(this.rdMatrixKey);
//		this.wrMatrixId = Integer.MIN_VALUE;
//		this.wrMatrixFriendlyName = wrMatrixFriendlyName;
//		this.wrMatrixDescription = wrMatrixDescription;
//
//		this.rdMarkerSet = new MarkerSet(this.rdMatrixKey);
//		this.rdMarkerSet.initFullMarkerIdSetMap();
//
//		this.wrMarkerIdSetMap = new LinkedHashMap<MarkerKey, byte[]>();
//		this.rdChrInfoSetMap = this.rdMarkerSet.getChrInfoSetMap();
//
//		this.rdSampleSet = new SampleSet(this.rdMatrixKey);
//		this.rdSampleSetMap = this.rdSampleSet.getSampleIdSetMapByteArray();
	}

	@Override
	public boolean isValid() {
		return (getProblemDescription() == null);
	}

	@Override
	public String getProblemDescription() {

		String problemDescription = null;

		try {
			MatrixMetadata parentMatrixMetadata = dataSetSource.getMatrixMetadata();
			GenotypeEncoding genotypeEncoding = parentMatrixMetadata.getGenotypeEncoding();
			if (!genotypeEncoding.equals(GenotypeEncoding.AB0)
					&& !genotypeEncoding.equals(GenotypeEncoding.O12)
					&& !genotypeEncoding.equals(GenotypeEncoding.O1234))
			{
				problemDescription = Text.Trafo.warnNotAB12 +  " & " + Text.Trafo.warnNot1234;
			} else if (!genotypeEncoding.equals(GenotypeEncoding.O1234)
					&& !parentMatrixMetadata.getHasDictionray()) {
				problemDescription = Text.Trafo.warnNoDictionary;
			}
		} catch (IOException ex) {
			problemDescription = ex.getMessage();
		}

		return problemDescription;
	}

	@Override
	public int processMatrix() throws IOException {

		int resultMatrixId;

		GenotypeEncoding gtEncoding = dataSetSource.getMatrixMetadata().getGenotypeEncoding();
		if (gtEncoding.equals(GenotypeEncoding.AB0)
				|| gtEncoding.equals(GenotypeEncoding.O12))
		{
			resultMatrixId = translateAB12AllelesToACGT();
		} else if (gtEncoding.equals(GenotypeEncoding.O1234)) {
			resultMatrixId = translate1234AllelesToACGT();
		} else {
			throw new IllegalStateException(
					"Can not convert genotype-encoding: "
					+ gtEncoding.toString() + " to "
					+ GenotypeEncoding.ACGT0.toString());
		}

		return resultMatrixId;
	}

	private int translateAB12AllelesToACGT() throws IOException {
		int result = Integer.MIN_VALUE;

		GenotypeEncoding rdMatrixGTCode = dataSetSource.getMatrixMetadata().getGenotypeEncoding();
		if (!rdMatrixGTCode.equals(GenotypeEncoding.ACGT0)) { // Has not yet been translated
			// METADATA WRITER
			// WRITING METADATA TO MATRIX
			AbstractNetCDFDataSetDestination.saveSamplesMetadata(rdSampleSetMap.keySet(), wrNcFile);
			AbstractNetCDFDataSetDestination.saveMarkersMatadata(dataSetSource.getMarkersMetadatasSource(), dataSetSource.getMarkersChromosomeInfosSource(), true, null, wrNcFile);

			// GENOTYPES WRITER
			// Get correct bases dictionary for translation
			Map<MarkerKey, byte[]> dictionnaries = rdMarkerSet.getDictionaryBases();

			// Iterate through Samples, use Sample item position to read all Markers GTs from rdMarkerIdSetMap.
			dataSetDestination.startLoadingAlleles(false);
//			int sampleIndex = 0;
//			for (int i = 0; i < rdSampleSetMap.size(); i++) {
//				// Get alleles from read matrix
//				rdMarkerSet.fillGTsForCurrentSampleIntoInitMap(sampleIndex);
//				// Send to be translated
//				Map<MarkerKey, byte[]> markerGTs = rdMarkerSet.getMarkerIdSetMapByteArray();
//				dataSetSource.getMarkersKeysSource();
//				translateCurrentSampleAB12AllelesMap(markerGTs, rdMatrixGTCode, dictionnaryMap);
//
//				// Write wrMarkerIdSetMap to A3 ArrayChar and save to wrMatrix
//				dataSetDestination.addSampleGTAlleles(sampleIndex, markerGTs.values());
//
//				if (sampleIndex % 100 == 0) {
//					log.info("Samples translated: {}", sampleIndex);
//				}
//				sampleIndex++;
//			}
//			log.info("Total Samples translated: {}", sampleIndex);
			int markerIndex = 0;
			Iterator<MarkerKey> markersKeysSourceIt = dataSetSource.getMarkersKeysSource().iterator();
			for (GenotypesList markerGenotypes : dataSetSource.getMarkersGenotypesSource()) {
				MarkerKey markerKey = markersKeysSourceIt.next();
				byte[] dictionary = dictionnaries.get(markerKey);
				Collection<byte[]> translatedGTs = translateCurrentMarkerAB12AllelesMap(markerGenotypes, rdMatrixGTCode, dictionary);

				// Write wrMarkerIdSetMap to A3 ArrayChar and save to wrMatrix
				dataSetDestination.addMarkerGTAlleles(markerIndex, translatedGTs);

				if (markerIndex % 100 == 0) {
					log.info("Samples translated: {}", markerIndex);
				}
				markerIndex++;
			}
			log.info("Total Samples translated: {}", markerIndex);
			dataSetDestination.finishedLoadingAlleles();

			org.gwaspi.global.Utils.sysoutCompleted("Translation");
		}

		return result;
	}

	// TODO Test this method
	private int translate1234AllelesToACGT() throws IOException {
		int result = Integer.MIN_VALUE;

		GenotypeEncoding rdMatrixGTCode = rdMatrixMetadata.getGenotypeEncoding();
		if (!rdMatrixGTCode.equals(GenotypeEncoding.ACGT0)) { // Has not yet been translated
			// METADATA WRITER
			// WRITING METADATA TO MATRIX
			AbstractNetCDFDataSetDestination.saveSamplesMetadata(rdSampleSetMap.keySet(), wrNcFile);
			AbstractNetCDFDataSetDestination.saveMarkersMatadata(dataSetSource.getMarkersMetadatasSource(), rdChrInfoSetMap, true, null, wrNcFile);

			//<editor-fold defaultstate="expanded" desc="GENOTYPES WRITER">
			// Get correct strand of each marker for newStrand translation
			// Iterate through Samples, use Sample item position to read all Markers GTs from rdMarkerIdSetMap.
			dataSetDestination.startLoadingAlleles(false);
			int sampleIndex = 0;
			for (int i = 0; i < rdSampleSetMap.size(); i++) {
				// Get alleles from read matrix
				rdMarkerSet.fillGTsForCurrentSampleIntoInitMap(sampleIndex);
				// Send to be translated
				Map<MarkerKey, byte[]> markerGTs = rdMarkerSet.getMarkerIdSetMapByteArray();
				translateCurrentSample1234AllelesMap(markerGTs, rdMarkerSet.getMarkerKeys());

				// Write wrMarkerIdSetMap to A3 ArrayChar and save to wrMatrix
				dataSetDestination.addSampleGTAlleles(sampleIndex, markerGTs.values());

				if (sampleIndex % 100 == 0) {
					log.info("Samples translated: {}", sampleIndex);
				}
				sampleIndex++;
			}
			log.info("Total Samples translated: {}", sampleIndex);
			dataSetDestination.finishedLoadingAlleles();

			org.gwaspi.global.Utils.sysoutCompleted("Translation");
		}

		return result;
	}

	private void translateCurrentSampleAB12AllelesMap(Map<MarkerKey, byte[]> codedMap, GenotypeEncoding sourceGenotypeEncoding, Map<MarkerKey, byte[]> dictionaryMap) {

		final byte alleleA;
		final byte alleleB;
		switch (sourceGenotypeEncoding) {
			case AB0:
				alleleA = AlleleBytes.A;
				alleleB = AlleleBytes.B;
				break;
			case O12:
				alleleA = AlleleBytes._1;
				alleleB = AlleleBytes._2;
				break;
			default:
				throw new IllegalStateException("Unsupported source genotype encoding " + sourceGenotypeEncoding.toString());
		}

		// Iterate through all markers
		for (Map.Entry<MarkerKey, byte[]> entry : codedMap.entrySet()) {
			MarkerKey markerKey = entry.getKey();
			byte[] basesDict = dictionaryMap.get(markerKey);
			byte[] codedAlleles = entry.getValue();
			byte[] transAlleles = new byte[2];

			for (int ai = 0; ai < codedAlleles.length; ai++) {
				if (codedAlleles[ai] == alleleA) {
					transAlleles[ai] = basesDict[0];
				} else if (codedAlleles[ai] == alleleB) {
					transAlleles[ai] = basesDict[1];
				} else {
					transAlleles[ai] = AlleleBytes._0;
				}
			}

			entry.setValue(transAlleles);
		}
	}

	private static void translateCurrentSample1234AllelesMap(Map<MarkerKey, byte[]> codedMap, Collection<MarkerKey> markerStrands) {

		Map<Byte, Byte> dictionary = new HashMap<Byte, Byte>();
		dictionary.put(AlleleBytes._0, AlleleBytes._0);
		dictionary.put(AlleleBytes._1, AlleleBytes.A);
		dictionary.put(AlleleBytes._2, AlleleBytes.C);
		dictionary.put(AlleleBytes._3, AlleleBytes.G);
		dictionary.put(AlleleBytes._4, AlleleBytes.T);

		// Iterate through all markers
		for (MarkerKey markerKey : markerStrands) {
			byte[] codedAlleles = codedMap.get(markerKey);

			byte[] transAlleles = new byte[2];
			transAlleles[0] = dictionary.get(codedAlleles[0]);
			transAlleles[1] = dictionary.get(codedAlleles[1]);

			codedMap.put(markerKey, transAlleles);
		}
	}
}
