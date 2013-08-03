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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.ExtendedMarkerKey;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MarkersMetadataSource;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.SamplesKeysSource;
import org.gwaspi.netCDF.loader.DataSetDestination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMergeMatrixOperation implements MatrixOperation {

	private static final Logger log = LoggerFactory.getLogger(AbstractMergeMatrixOperation.class);

	protected final DataSetSource dataSetSource1;
	protected final DataSetSource dataSetSource2;
	protected final DataSetDestination dataSetDestination;

	protected AbstractMergeMatrixOperation(
			DataSetSource dataSetSource1,
			DataSetSource dataSetSource2,
			DataSetDestination dataSetDestination)
			throws IOException
	{
		this.dataSetSource1 = dataSetSource1;
		this.dataSetSource2 = dataSetSource2;
		this.dataSetDestination = dataSetDestination;
	}

	private static Map<MarkerKey, ExtendedMarkerKey> getMatrixMapWithChrAndPosAndMarkerId(MarkersMetadataSource markersMetadataSource) {

		Map<MarkerKey, ExtendedMarkerKey> workMap = new LinkedHashMap<MarkerKey, ExtendedMarkerKey>(markersMetadataSource.size());
		for (MarkerMetadata markerMetadata : markersMetadataSource) {
			workMap.put(MarkerKey.valueOf(markerMetadata), ExtendedMarkerKey.valueOf(markerMetadata));
		}

		return workMap;
	}

	protected SortedMap<ExtendedMarkerKey, MarkerKey> mingleAndSortMarkerSetRaw() {

		Map<MarkerKey, ExtendedMarkerKey> workMap = getMatrixMapWithChrAndPosAndMarkerId(dataSetSource1.getMarkersMetadatasSource());
		workMap.putAll(getMatrixMapWithChrAndPosAndMarkerId(dataSetSource2.getMarkersMetadatasSource()));

		// sort by extended marker key
		SortedMap<ExtendedMarkerKey, MarkerKey> sorted = new TreeMap<ExtendedMarkerKey, MarkerKey>();
		for (Map.Entry<MarkerKey, ExtendedMarkerKey> entry : workMap.entrySet()) {
			sorted.put(entry.getValue(), entry.getKey());
		}

		return sorted;
	}

	protected Map<MarkerKey, MarkerMetadata> mingleAndSortMarkerSet() {

		SortedMap<ExtendedMarkerKey, MarkerKey> sorted = mingleAndSortMarkerSetRaw();

		// repackage
		Map<MarkerKey, MarkerMetadata> result = new LinkedHashMap<MarkerKey, MarkerMetadata>();
		for (Map.Entry<ExtendedMarkerKey, MarkerKey> entry : sorted.entrySet()) {
			ExtendedMarkerKey key = entry.getKey();
			MarkerMetadata markerInfo = new MarkerMetadata(
					key.getChr(),
					key.getPos());

			MarkerKey markerKey = entry.getValue();
			result.put(markerKey, markerInfo);
		}

		return result;
	}

	/**
	 * With the help of this, we perform on-the-fly mismatch checking,
	 * while we are storing sample genotypes.
	 */
	private Collection<Set<Byte>> perMarkerAlleles;
	private int numSamples;
	private int mismatchCount;
	private double mismatchRatio;

	protected void initiateGenotypesMismatchChecking(int numMarkers) throws IOException {

		GenotypeEncoding genotypeEncoding = dataSetSource1.getMatrixMetadata().getGenotypeEncoding();
		if (genotypeEncoding.equals(GenotypeEncoding.ACGT0)
				|| genotypeEncoding.equals(GenotypeEncoding.O1234))
		{
			perMarkerAlleles = new LinkedList<Set<Byte>>();
			for (int mi = 0; mi < numMarkers; mi++) {
				perMarkerAlleles.add(new HashSet<Byte>());
			}
		} else {
			perMarkerAlleles = null;
		}
	}

	protected void addSampleGTAlleles(int sampleIndex, Collection<byte[]> sampleAlleles) throws IOException {

		if (perMarkerAlleles != null) {
			// assemble per marker alleles
			Iterator<Set<Byte>> perMarkerAllelesIt = perMarkerAlleles.iterator();
			for (byte[] bs : sampleAlleles) {
				Set<Byte> markerAllele = perMarkerAllelesIt.next();
				for (byte allele : bs) {
					markerAllele.add(allele);
				}
			}
		}

		dataSetDestination.addSampleGTAlleles(sampleIndex, sampleAlleles);
	}

	protected void finalizeGenotypesMismatchChecking() {

		mismatchCount = 0;
		mismatchRatio = 0.0;
		if (perMarkerAlleles != null) {
			for (Set<Byte> markerAllele : perMarkerAlleles) {
				// remove the invalid allele 0
				markerAllele.remove((byte) 0);

				if (markerAllele.size() > 2) {
					mismatchCount++;
				}
			}

			mismatchRatio = (double) mismatchCount / numSamples;
		}
	}

	protected void validateMissingRatio() {

		// CHECK FOR MISMATCHES
		if (getMismatchRatio() > 0.01) {
			log.warn("");
			log.warn("Mismatch ratio is bigger than 1% ({}%)!", (getMismatchRatio() * 100));
			log.warn("There might be an issue with strand positioning of your genotypes!");
			log.warn("");
			//resultMatrixId = new int[] {wrMatrixHandler.getResultMatrixId(),-4};  // The threshold of acceptable mismatching genotypes has been crossed
		}
	}

	protected int getMissingCount() {
		return mismatchCount;
	}

	protected double getMismatchRatio() {
		return mismatchRatio;
	}

//	protected void addMarkerGTAlleles(int markerIndex, Collection<byte[]> markerAlleles) throws IOException {
//		// TODO check for mismatches!
//		XXX;
//		dataSetDestination.addMarkerGTAlleles(markerIndex, markerAlleles);
//	}

	protected static Map<SampleKey, int[]> getComboSampleSetWithIndicesArray(SamplesKeysSource sampleKeys1, SamplesKeysSource sampleKeys2) {
		Map<SampleKey, int[]> resultMap = new LinkedHashMap<SampleKey, int[]>();

		int wrPos = 0;
		int rdPos = 0;
		for (SampleKey key : sampleKeys1) {
			int[] position = new int[] {1, rdPos, wrPos}; // rdMatrixNb, rdPos, wrPos
			resultMap.put(key, position);
			wrPos++;
			rdPos++;
		}

		rdPos = 0;
		for (SampleKey key : sampleKeys2) {
			int[] position;
			// IF SAMPLE ALLREADY EXISTS IN MATRIX1 SUBSTITUTE VALUES WITH MATRIX2
			if (resultMap.containsKey(key)) {
				position = resultMap.get(key);
				position[0] = 2; // rdMatrixNb
				position[1] = rdPos; // rdPos
			} else {
				position = new int[] {2, rdPos, wrPos}; // rdMatrixNb, rdPos, wrPos
			}

			resultMap.put(key, position);
			wrPos++;
			rdPos++;
		}

		return resultMap;
	}

	protected static double[] checkForMismatches(DataSetSource dataSetSource) throws IOException {

		double[] result = new double[2];

		int markerNb = 0;
		int mismatchCount = 0;
		for (GenotypesList markerGenotypes : dataSetSource.getMarkersGenotypesSource()) {
			Set<Byte> knownAlleles = new HashSet<Byte>();
			for (byte[] tempGT : markerGenotypes) {
				// Gather alleles into a list of known alleles
				// and count the number of appearences
				for (byte allele : tempGT) {
					knownAlleles.add(allele);
				}
			}
			// we are not interested in the "invalid" allele 0
			knownAlleles.remove((byte) '0');

			// if we have more then two different alleles per marker,
			// something is wrong
			if (knownAlleles.size() > 2) {
				mismatchCount++;
			}

			markerNb++;
			if (markerNb % 100000 == 0) {
				log.info("Checking markers for mismatches: {}", markerNb);
			}
		}
		final int numSamples = dataSetSource.getSamplesKeysSource().size();
		final double mismatchRatio = (double) mismatchCount / numSamples;

		result[0] = mismatchCount;
		result[1] = mismatchRatio;

		return result;
	}
}
