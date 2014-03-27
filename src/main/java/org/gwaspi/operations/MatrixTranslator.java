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

package org.gwaspi.operations;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.gwaspi.constants.cNetCDF.Defaults.AlleleByte;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.global.Text;
import org.gwaspi.model.ChromosomeInfo;
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.loader.DataSetDestination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatrixTranslator implements MatrixOperation {

	private final Logger log = LoggerFactory.getLogger(MatrixTranslator.class);

	private static final OperationTypeInfo OPERATION_TYPE_INFO
			= new DefaultOperationTypeInfo(
					true,
					Text.Trafo.translateMatrix,
					Text.Trafo.translateMatrix, // TODO We need a more elaborate description of this operation!
					null);
	static {
		// NOTE When converting to OSGi, this would be done in bundle init,
		//   or by annotations.
		OperationManager.registerOperationFactory(new MatrixOperationFactory(
				MatrixTranslator.class, OPERATION_TYPE_INFO));
	}

	private final DataSetSource dataSetSource;
	private final DataSetDestination dataSetDestination;
	private final boolean translateBySamples; // ... or markers

	public MatrixTranslator(
			DataSetSource dataSetSource,
			DataSetDestination dataSetDestination)
			throws IOException
	{
		this.dataSetSource = dataSetSource;
		this.dataSetDestination = dataSetDestination;
		this.translateBySamples = true;
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
					&& !parentMatrixMetadata.getHasDictionary()) {
				problemDescription = Text.Trafo.warnNoDictionary;
			}
		} catch (IOException ex) {
			problemDescription = ex.getMessage();
		}

		return problemDescription;
	}

	@Override
	public OperationParams getParams() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int processMatrix() throws IOException {

		int resultMatrixId = MatrixKey.NULL_ID;

		translateToACGT();

		return resultMatrixId;
	}

	private static interface GenotypeTranslator {

		Collection<byte[]> translateBySamples(SampleKey sampleKey, GenotypesList sampleGenotypes) throws IOException;
		Collection<byte[]> translateByMarkers(MarkerKey markerKey, GenotypesList markerGenotypes) throws IOException;
	}

	private static class AB12ToACGTGenotypeTranslator implements GenotypeTranslator {

		private final Map<MarkerKey, byte[]> dictionnaries;
		private final DataSetSource dataSetSource;
		private final byte alleleA;
		private final byte alleleB;

		public AB12ToACGTGenotypeTranslator(DataSetSource dataSetSource) throws IOException {

			// Get correct bases dictionary for translation
			this.dictionnaries = getDictionaryBases(dataSetSource);
			this.dataSetSource = dataSetSource;

			GenotypeEncoding sourceGenotypeEncoding = dataSetSource.getMatrixMetadata().getGenotypeEncoding();
			switch (sourceGenotypeEncoding) {
				case AB0:
					this.alleleA = AlleleByte.A.getValue();
					this.alleleB = AlleleByte.B.getValue();
					break;
				case O12:
					this.alleleA = AlleleByte._1.getValue();
					this.alleleB = AlleleByte._2.getValue();
					break;
				default:
					throw new IllegalStateException("Unsupported source genotype encoding " + sourceGenotypeEncoding.toString());
			}
		}

		private static Map<MarkerKey, byte[]> getDictionaryBases(DataSetSource dataSetSource) throws IOException {

			Map<MarkerKey, byte[]> dictionary = new HashMap<MarkerKey, byte[]>();

			Iterator<MarkerMetadata> markersMetadatasIt = dataSetSource.getMarkersMetadatasSource().iterator();
			for (MarkerKey markerKey : dataSetSource.getMarkersKeysSource()) {
				MarkerMetadata markerMetadata = markersMetadatasIt.next();
				dictionary.put(markerKey, markerMetadata.getAlleles().getBytes());
			}

			return dictionary;
		}

		@Override
		public Collection<byte[]> translateBySamples(SampleKey sampleKey, GenotypesList sampleGenotypes) throws IOException {

			// Iterate through all markers
			Iterator<byte[]> sampleGenotypesIt = sampleGenotypes.iterator();
//			Iterator<MarkerKey> markersKeysIt = dataSetSource.getMarkersKeysSource().iterator();
			int si = 0;
			for (MarkerKey markerKey : dataSetSource.getMarkersKeysSource()) {
				byte[] codedAlleles = sampleGenotypesIt.next();
//				final MarkerKey markerKey = markersKeysIt.next();
				final byte[] basesDict = dictionnaries.get(markerKey);
				byte[] transAlleles = new byte[2];

				for (int ai = 0; ai < codedAlleles.length; ai++) { // ai = {0, 1}
					if (codedAlleles[ai] == alleleA) {
						transAlleles[ai] = basesDict[0];
					} else if (codedAlleles[ai] == alleleB) {
						transAlleles[ai] = basesDict[1];
					} else {
						transAlleles[ai] = AlleleByte._0_VALUE;
					}
				}

//				codedAlleles[0] = transAlleles[0];
//				codedAlleles[1] = transAlleles[1];
				sampleGenotypes.set(si, transAlleles);
				si++;
			}

			return sampleGenotypes;
		}

		@Override
		public Collection<byte[]> translateByMarkers(MarkerKey markerKey, GenotypesList markerGenotypes) throws IOException {

			final byte[] basesDict = dictionnaries.get(markerKey);

			int mi = 0;
			for (byte[] codedAlleles : markerGenotypes) {
				byte[] transAlleles = new byte[2];

				for (int ai = 0; ai < codedAlleles.length; ai++) { // ai = {0, 1}
					if (codedAlleles[ai] == alleleA) {
						transAlleles[ai] = basesDict[0];
					} else if (codedAlleles[ai] == alleleB) {
						transAlleles[ai] = basesDict[1];
					} else {
						transAlleles[ai] = AlleleByte._0_VALUE;
					}
				}

//				codedAlleles[0] = transAlleles[0];
//				codedAlleles[1] = transAlleles[1];
				markerGenotypes.set(mi, transAlleles);
				mi++;
			}

			return markerGenotypes;
		}
	}


	private static class One234ToACGTGenotypeTranslator implements GenotypeTranslator {

		private static final Map<Byte, Byte> dictionary;
		static {
			dictionary = new HashMap<Byte, Byte>();
			dictionary.put(AlleleByte._0.getValue(), AlleleByte._0.getValue());
			dictionary.put(AlleleByte._1.getValue(), AlleleByte.A.getValue());
			dictionary.put(AlleleByte._2.getValue(), AlleleByte.C.getValue());
			dictionary.put(AlleleByte._3.getValue(), AlleleByte.G.getValue());
			dictionary.put(AlleleByte._4.getValue(), AlleleByte.T.getValue());
		}

		private final DataSetSource dataSetSource;

		public One234ToACGTGenotypeTranslator(DataSetSource dataSetSource) {
			this.dataSetSource = dataSetSource;
		}

		@Override
		public Collection<byte[]> translateBySamples(SampleKey sampleKey, GenotypesList sampleGenotypes) throws IOException {
			return translate(sampleGenotypes);
		}

		@Override
		public Collection<byte[]> translateByMarkers(MarkerKey markerKey, GenotypesList markerGenotypes) throws IOException {
			return translate(markerGenotypes);
		}

		private static Collection<byte[]> translate(GenotypesList genotypes) throws IOException {

			int li = 0;
			for (byte[] codedAlleles : genotypes) {
				byte[] transAlleles = new byte[2];
				transAlleles[0] = dictionary.get(codedAlleles[0]);
				transAlleles[1] = dictionary.get(codedAlleles[1]);

				genotypes.set(li, transAlleles);
				li++;
			}

			return genotypes;
		}
	}



	private void translateToACGT() throws IOException {

		final GenotypeEncoding gtEncoding = dataSetSource.getMatrixMetadata().getGenotypeEncoding();

		GenotypeTranslator genotypeTranslator;
		if (gtEncoding.equals(GenotypeEncoding.AB0)
				|| gtEncoding.equals(GenotypeEncoding.O12))
		{
			genotypeTranslator = new AB12ToACGTGenotypeTranslator(dataSetSource);
		} else if (gtEncoding.equals(GenotypeEncoding.O1234)) {
			genotypeTranslator = new One234ToACGTGenotypeTranslator(dataSetSource);
		} else {
			throw new IllegalStateException(
					"Can not convert genotype-encoding: "
					+ gtEncoding.toString() + " to "
					+ GenotypeEncoding.ACGT0.toString());
		}

		// METADATA WRITER
		// WRITING METADATA TO MATRIX
		for (SampleInfo sampleInfo : dataSetSource.getSamplesInfosSource()) {
			dataSetDestination.addSampleInfo(sampleInfo);
		}

		// copy & paste the marker-metadata from matrix 1
		for (MarkerMetadata markerMetadata : dataSetSource.getMarkersMetadatasSource()) {
			dataSetDestination.addMarkerMetadata(markerMetadata);
		}

		// RETRIEVE CHROMOSOMES INFO
		Iterator<ChromosomeInfo> chromosomesInfosIt = dataSetSource.getChromosomesInfosSource().iterator();
		for (ChromosomeKey chromosomeKey : dataSetSource.getChromosomesKeysSource()) {
			ChromosomeInfo chromosomeInfo = chromosomesInfosIt.next();
			dataSetDestination.addChromosomeMetadata(chromosomeKey, chromosomeInfo);
		}

		// GENOTYPES WRITER
		// Iterate through Samples, use Sample item position to read all Markers GTs from rdMarkerIdSetMap.
		dataSetDestination.startLoadingAlleles(translateBySamples);
		if (translateBySamples) {
			int sampleIndex = 0;
			Iterator<SampleKey> samplesKeysSourceIt = dataSetSource.getSamplesKeysSource().iterator();
			for (GenotypesList sampleGenotypes : dataSetSource.getSamplesGenotypesSource()) {
				SampleKey sampleKey = samplesKeysSourceIt.next();
				Collection<byte[]> translatedGTs = genotypeTranslator.translateBySamples(sampleKey, sampleGenotypes);

				dataSetDestination.addSampleGTAlleles(sampleIndex, translatedGTs);

				if (sampleIndex % 100 == 0) {
					log.info("Samples translated: {}", sampleIndex);
				}
				sampleIndex++;
			}
			log.info("Total Samples translated: {}", sampleIndex);
		} else {
			int markerIndex = 0;
			Iterator<MarkerKey> markersKeysSourceIt = dataSetSource.getMarkersKeysSource().iterator();
			for (GenotypesList markerGenotypes : dataSetSource.getMarkersGenotypesSource()) {
				MarkerKey markerKey = markersKeysSourceIt.next();
				Collection<byte[]> translatedGTs = genotypeTranslator.translateByMarkers(markerKey, markerGenotypes);

				dataSetDestination.addMarkerGTAlleles(markerIndex, translatedGTs);

				if (markerIndex % 100 == 0) {
					log.info("Samples translated: {}", markerIndex);
				}
				markerIndex++;
			}
			log.info("Total Samples translated: {}", markerIndex);
		}
		dataSetDestination.finishedLoadingAlleles();

		org.gwaspi.global.Utils.sysoutCompleted("Translation");
	}
}
