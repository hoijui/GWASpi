/*
 * Copyright (C) 2014 Universitat Pompeu Fabra
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

package org.gwaspi.operations.combi;

import java.io.IOException;
import java.util.AbstractList;
import java.util.List;
import org.gwaspi.model.GenotypesList;

public class MarkerGenotypesEncoder extends AbstractList<Float[][]> {

	private final List<GenotypesList> markersGenotypesSource;
//	private final MarkersGenotypesSource markersGenotypesSource;
	private final GenotypeEncoder genotypeEncoder;
	/** The total number of markers. */
	private final int dSamples;
	/** The total number of markers * encodingFactor. */
	private final int dEncoded;
	/** The total number of samples. */
	private final int n;
	/**
	 * How many markers may be loaded at a time,
	 * to still full-fill the maximum memory usage limit.
	 */
	private final int maxChunkSize;
	/**
	 * How many features (== markers * encodingFactor) may be loaded at a time,
	 * to still full-fill the maximum memory usage limit.
	 * @see #maxChunkSize
	 */
	private final int maxFeaturesChunkSize;
	/**
	 * The total number of chunks in the data-set,
	 * when using {@link #maxChunkSize}.
	 * The last chunk may be smaller then {@link #maxChunkSize}.
	 */
	private final int numChunks;
	private final InMemorySamplesFeaturesStorage<Float> encodedSamplesRawStorage;

	MarkerGenotypesEncoder(
			final List<GenotypesList> markersGenotypesSource,
			final GenotypeEncoder genotypeEncoder,
			final int dSamples,
			final int n,
			final int maxChunkSize)
			throws IOException
	{
		this.markersGenotypesSource = markersGenotypesSource;
		this.genotypeEncoder = genotypeEncoder;
		this.dSamples = dSamples;
		this.dEncoded = dSamples * genotypeEncoder.getEncodingFactor();
		this.n = n;
		this.maxChunkSize = maxChunkSize;
		this.maxFeaturesChunkSize = maxChunkSize * genotypeEncoder.getEncodingFactor();
		this.numChunks = (int) Math.ceil((double) dSamples / maxChunkSize);

		try {
			// NOTE This may allocate a LOT of memory!
			//   We use float instead of double to half the memory,
			//   because we have no more then 4 or 5 distinct values anyway,
			//   so we do not need high precission.
			this.encodedSamplesRawStorage = new InMemorySamplesFeaturesStorage<Float>(Float.class, n, maxFeaturesChunkSize);
		} catch (OutOfMemoryError er) {
			throw new IOException(er);
		}
	}

	private Float[][] encodeChunk(final int chunkIndex) throws IOException {

//		// initialize the kernelMatrix
//		// this should not be required, if the aray was just created,
//		// but who knows who will call this function in what way in the future!?
//		for (float[] kernelMatrixRow : kernelMatrix) {
//			for (int ci = 0; ci < kernelMatrixRow.length; ci++) {
//				kernelMatrixRow[ci] = 0.0f;
//			}
//		}

//		// max memory usage of this function [bytes]
//		final int maxChunkMemoryUsage = 1024 * 1024;
//		// how much memory does one sample per marker use [bytes]
//		final int singleEntryMemoryUsage = 2 * 8; // FIXME two doubles.. arbitrary.. investigate
//		// how many markers may be loaded at a time, to still fullfill the max memory usage limit
//		final int maxChunkSize = Math.min(dSamples, (int) Math.floor((double) maxChunkMemoryUsage / dSamples / singleEntryMemoryUsage));
//		final int maxFeaturesChunkSize = maxChunkSize * genotypeEncoder.getEncodingFactor();
//		// ... which results in this many chunks for the whole data-set
//		final int numChunks = (int) Math.ceil((double) dSamples / maxChunkSize);

//		// This is basically a part of the (encoded) feature-map,
//		// that will be used to calculate the kernel matrix
//		InMemorySamplesFeaturesStorage<Float> encodedSamplesRawStorage;
//		try {
//			// NOTE This allocates a LOT of memory!
//			//   We use float instead of double to half the memory,
//			//   because we have no more then 4 or 5 distinct values anyway,
//			//   so we do not need high precission.
//			encodedSamplesRawStorage = new InMemorySamplesFeaturesStorage<Float>(Float.class, n, maxFeaturesChunkSize);
//		} catch (OutOfMemoryError er) {
//			throw new IOException(er);
//		}

//		for (int ci = 0; ci < numChunks; ci++) {
			final int firstMarkerIndex = chunkIndex * maxChunkSize;
			final int firstMarkerFeatureIndex = chunkIndex * maxFeaturesChunkSize;
			final int numMarkersInChunk = Math.min(dSamples - firstMarkerIndex, maxChunkSize);
//			final int lastMarkerIndex = firstMarkerIndex - 1 + numMarkersInChunk;

			SamplesFeaturesStorage<Float> encodedSamplesPart = new PartialFeaturesInMemorySamplesFeaturesStorage<Float>(n, dEncoded, encodedSamplesRawStorage.getCache(), firstMarkerFeatureIndex, encodedSamplesRawStorage);

			// encode only a chunk/part of the markers at a time
			// which gives us a part of the feature matrix
			CombiTestMatrixOperation.encodeSamples(
					markersGenotypesSource,
	//				sampleInfos.keySet(),
	//				sampleAffections,
					genotypeEncoder,
					firstMarkerIndex,
					numMarkersInChunk,
					dSamples,
					dEncoded,
					n,
					encodedSamplesPart);

			return encodedSamplesRawStorage.getStorage();
//			// calculate the part of the kernel matrix defined by
//			// the current chunk of the feature matrix
////			for (int smi = 0; smi < numMarkersInChunk; smi++) {
//			for (int smi = 0; smi < rawStorage[0].length; smi++) {
////				final int curMarkerIndex = firstMarkerIndex + smi;
//				for (int krsi = 0; krsi < rawStorage.length; krsi++) { // kernel row sample index
//					final float curRowValue = (Float) rawStorage[krsi][smi];
//					for (int krci = 0; krci < rawStorage.length; krci++) { // kernel column sample index
//						final float curColValue = (Float) rawStorage[krci][smi];
//						kernelMatrix[krsi][krci] += curRowValue * curColValue;
//					}
//				}
//			}
//		}
	}

	@Override
	/**
	 * Each call to this method invalidates the result returned by
	 * the last call.
	 */
	public Float[][] get(int index) {

		try {
			return encodeChunk(index);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public int getChunkSize(int index) {
		return Math.min(dEncoded - (index * maxFeaturesChunkSize), maxFeaturesChunkSize);
	}

	public int getMaxChunkSize() {
		return maxFeaturesChunkSize;
	}

	@Override
	public int size() {
		return numChunks;
	}

	public int getNumFeatures() {
		return dEncoded;
	}
}
