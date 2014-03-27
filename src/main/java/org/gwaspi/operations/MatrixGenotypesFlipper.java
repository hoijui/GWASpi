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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
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
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.loader.DataSetDestination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatrixGenotypesFlipper extends AbstractOperation {

	private final Logger log = LoggerFactory.getLogger(MatrixGenotypesFlipper.class);

	private static final OperationTypeInfo OPERATION_TYPE_INFO
			= new DefaultOperationTypeInfo(
					true,
					Text.Trafo.flipStrand,
					Text.Trafo.flipStrand, // TODO We need a more elaborate description of this operation!
					null);
	static {
		// NOTE When converting to OSGi, this would be done in bundle init,
		//   or by annotations.
		OperationManager.registerOperationFactory(new MatrixOperationFactory(
				MatrixGenotypesFlipper.class, OPERATION_TYPE_INFO));
	}

	private final DataSetSource dataSetSource;
	private final File flipperFile;
	private final Set<MarkerKey> markersToFlip;

	/**
	 * Use this constructor to extract data from a matrix
	 * by passing a variable and the criteria to filter items by.
	 */
	public MatrixGenotypesFlipper(
			DataSetSource dataSetSource,
			DataSetDestination dataSetDestination,
			File flipperFile)
			throws IOException
	{
		super(dataSetDestination);

		this.dataSetSource = dataSetSource;
		this.flipperFile = flipperFile;
		this.markersToFlip = loadMarkerKeys(this.flipperFile);
	}

	@Override
	public OperationParams getParams() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * Loads a number of marker keys from a plain text file.
	 * @return the loaded marker keys
	 * @throws IOException
	 */
	private static Set<MarkerKey> loadMarkerKeys(File flipperFile) throws IOException {

		Set<MarkerKey> markerKeys = new HashSet<MarkerKey>();

		if (flipperFile.isFile()) {
			FileReader fr = new FileReader(flipperFile);
			BufferedReader br = new BufferedReader(fr);
			String line;
			while ((line = br.readLine()) != null) {
				markerKeys.add(MarkerKey.valueOf(line));
			}
			br.close();
		}

		return markerKeys;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String getProblemDescription() {

		String problemDescription = null;

		try {
			GenotypeEncoding genotypeEncoding = dataSetSource.getMatrixMetadata().getGenotypeEncoding();
			if (!genotypeEncoding.equals(GenotypeEncoding.O1234)
					&& !genotypeEncoding.equals(GenotypeEncoding.ACGT0))
			{
				problemDescription = Text.Trafo.warnNotACGTor1234;
			}
		} catch (IOException ex) {
			problemDescription = ex.getMessage();
		}

		return problemDescription;
	}

	@Override
	public int processMatrix() throws IOException {

		int resultMatrixId = MatrixKey.NULL_ID;

		final DataSetDestination dataSetDestination = getDataSetDestination();

		// simply copy&paste the sample infos
		dataSetDestination.startLoadingSampleInfos(true);
//		for (SampleInfo sampleInfo : dataSetSource.getSamplesInfosSource()) {
//			dataSetDestination.addSampleInfo(sampleInfo);
//		}
		for (SampleKey sampleKey : dataSetSource.getSamplesKeysSource()) {
			dataSetDestination.addSampleKey(sampleKey);
		}
		dataSetDestination.finishedLoadingSampleInfos();

		// copy&paste the marker metadata aswell,
		// but flipp dictionary-alleles and strand
		// of the ones that are selected for flipping
		dataSetDestination.startLoadingMarkerMetadatas(false);
		Iterator<MarkerKey> markerKeysIt = dataSetSource.getMarkersKeysSource().iterator();
		for (MarkerMetadata origMarkerMetadata : dataSetSource.getMarkersMetadatasSource()) {
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

		// simply copy&paste the chromosomes infos
		dataSetDestination.startLoadingChromosomeMetadatas();
		Iterator<ChromosomeInfo> chromosomesInfosIt = dataSetSource.getChromosomesInfosSource().iterator();
		for (ChromosomeKey chromosomeKey : dataSetSource.getChromosomesKeysSource()) {
			ChromosomeInfo chromosomeInfo = chromosomesInfosIt.next();
			dataSetDestination.addChromosomeMetadata(chromosomeKey, chromosomeInfo);
		}
		dataSetDestination.finishedLoadingChromosomeMetadatas();

		// WRITE GENOTYPES
		dataSetDestination.startLoadingAlleles(false);
		log.info(Text.All.processing);
		int markerIndex = 0;
		final GenotypeEncoding gtEncoding = dataSetSource.getMatrixMetadata().getGenotypeEncoding();
		markerKeysIt = dataSetSource.getMarkersKeysSource().iterator();
		for (GenotypesList markerGenotypes : dataSetSource.getMarkersGenotypesSource()) {
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
				log.info("Markers processed: {} / {}", markerIndex, dataSetSource.getMarkersGenotypesSource().size());
			}
		}
		dataSetDestination.finishedLoadingAlleles();

		org.gwaspi.global.Utils.sysoutCompleted("Genotype Flipping to new Matrix");

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

		if (gtEncoding.equals(GenotypeEncoding.ACGT0)) {
			for (int i = 0; i < gt.length; i++) {
				if (gt[i] == AlleleByte.A.getValue()) {
					result[i] = AlleleByte.T.getValue();
				} else if (gt[i] == AlleleByte.C.getValue()) {
					result[i] = AlleleByte.G.getValue();
				} else if (gt[i] == AlleleByte.G.getValue()) {
					result[i] = AlleleByte.C.getValue();
				} else if (gt[i] == AlleleByte.T.getValue()) {
					result[i] = AlleleByte.A.getValue();
				}
			}
		}

		if (gtEncoding.equals(GenotypeEncoding.O1234)) {
			for (int i = 0; i < gt.length; i++) {
				if (gt[i] == AlleleByte._1.getValue()) {
					result[i] = AlleleByte._4.getValue();
				} else if (gt[i] == AlleleByte._2.getValue()) {
					result[i] = AlleleByte._3.getValue();
				} else if (gt[i] == AlleleByte._3.getValue()) {
					result[i] = AlleleByte._2.getValue();
				} else if (gt[i] == AlleleByte._4.getValue()) {
					result[i] = AlleleByte._1.getValue();
				}
			}
		}
	}
}
