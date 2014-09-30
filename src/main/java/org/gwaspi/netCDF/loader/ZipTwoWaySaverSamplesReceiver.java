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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.SampleInfoList;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.Study;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipTwoWaySaverSamplesReceiver extends AbstractDataSetDestination {

	private final Logger log
			= LoggerFactory.getLogger(ZipTwoWaySaverSamplesReceiver.class);

	private final GenotypesLoadDescription loadDescription;
	private AbstractLoadGTFromFiles gtLoader; // HACK
	private String startTime;
	private MatrixKey resultMatrixKey;
//	private int curAlleleSampleIndex;
	private final int curAllelesMarkerIndex;
	private StringBuilder descSB;
	private MatrixFactory matrixFactory;
	private ZipOutputStream curArchive;
	private GZIPOutputStream curGArchive;
//	private ZipOutputStream curMarkersArchive;
	private Boolean alleleLoadPerSample;
	/** This is only used when alleleLoadPerSample == FALSE */
	private final List<byte[]> genotypesHyperslabs;
	private final int hyperSlabRows;

	public ZipTwoWaySaverSamplesReceiver(
			GenotypesLoadDescription loadDescription)
	{
		this.loadDescription = loadDescription;
		this.gtLoader = null;
//		this.curAlleleSampleIndex = -1;
		this.curAllelesMarkerIndex = -1;
		this.alleleLoadPerSample = null;
		this.genotypesHyperslabs = null;
		this.hyperSlabRows = -1;
	}

	public MatrixKey getResultMatrixKey() {
		return resultMatrixKey;
	}

	public void setGTLoader(AbstractLoadGTFromFiles gtLoader) {
		this.gtLoader = gtLoader;
	}

	@Override
	public void init() throws IOException {

		startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();
		resultMatrixKey = null;
	}

	@Override
	public void finishedLoadingSampleInfos() throws IOException {
		SampleInfoList.insertSampleInfos(getDataSet().getSampleInfos());
	}

	//<editor-fold defaultstate="expanded" desc="PROCESS GENOTYPES">
	@Override
	public void finishedLoadingMarkerMetadatas() throws IOException {
	}

	int numSamples;
	int numMarkers;
	int samplesPerArchive;
	int markersPerArchive;
	File pathToZippedSamples;
	File pathToZippedMarkers;
	List<SampleKey> sampleKeys;
	List<MarkerKey> markerKeys;

	@Override
	public void startLoadingAlleles(boolean perSample) throws IOException {

		alleleLoadPerSample = perSample;

		numSamples = getDataSet().getSampleInfos().size();
		numMarkers = getDataSet().getMarkerMetadatas().size();

		// ensure archives contain max 2MB (uncompressed) content size
		samplesPerArchive = Math.max(1, (2 * 1024 * 1024) / (numMarkers * 2));
		markersPerArchive = Math.max(1, (2 * 1024 * 1024) / (numSamples * 2));

		File pathToStudy = new File(Study.constructGTPath(loadDescription.getStudyKey()));
		if (!pathToStudy.exists()) {
			org.gwaspi.global.Utils.createFolder(pathToStudy);
		}

		File pathToZipped = new File(pathToStudy, "zipped");
		org.gwaspi.global.Utils.createFolder(pathToZipped);

		pathToZippedSamples = new File(pathToZipped, "samples");
		org.gwaspi.global.Utils.createFolder(pathToZippedSamples);

		pathToZippedMarkers = new File(pathToZipped, "markers");
		org.gwaspi.global.Utils.createFolder(pathToZippedMarkers);

		Map<MarkerKey, MarkerMetadata> markerMetadatas = getDataSet().getMarkerMetadatas();
		markerKeys = new ArrayList<MarkerKey>(markerMetadatas.keySet());
		sampleKeys = LoadingNetCDFDataSetDestination.extractKeys(getDataSet().getSampleInfos());

		log.info("Writing genotypes to zip files");
	}

	@Override
	public void addSampleGTAlleles(int sampleIndex, List<byte[]> sampleAlleles) throws IOException {

		if (!alleleLoadPerSample) {
			throw new IllegalStateException("You can not mix loading per sample and loading per marker");
		}

		String entryName = sampleKeys.get(sampleIndex).toString();

		writeGTsGZip(sampleIndex, samplesPerArchive, numSamples, pathToZippedSamples, entryName, sampleAlleles);
	}

	@Override
	public void addMarkerGTAlleles(int markerIndex, List<byte[]> markerAlleles) throws IOException {

		if (alleleLoadPerSample) {
			throw new IllegalStateException("You can not mix loading per sample and loading per marker");
		}

		String entryName = markerKeys.get(markerIndex).toString();

		writeGTsGZip(markerIndex, markersPerArchive, numMarkers, pathToZippedMarkers, entryName, markerAlleles);
	}

	private void writeGTsZip(int index, int perArchive, int total, File pathToZipped, String entryName, Collection<byte[]> alleles) throws IOException {

		if ((index % perArchive) == 0) {
			int lastInArchive = Math.min(total, index + perArchive);
			String archiveFileName = index + "to" + lastInArchive + ".zip";
			File curArchiveFile = new File(pathToZipped, archiveFileName);
			curArchive = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(curArchiveFile)));
		}

		ZipEntry entry = new ZipEntry(entryName);
		curArchive.putNextEntry(entry);
		writeGTsEncoded(curArchive, alleles);
		curArchive.closeEntry();

		int nextIndex = index + 1;
		if (((nextIndex % perArchive) == 0) || (nextIndex  == total)) {
			curArchive.close();
			curArchive = null;
			log.info("written: {} / {}", nextIndex, total);
		}
	}

	private void writeGTsGZip(int index, int perArchive, int total, File pathToZipped, String entryName, Collection<byte[]> alleles) throws IOException {

		if ((index % perArchive) == 0) {
			int lastInArchive = Math.min(total, index + perArchive);
			String archiveFileName = index + "to" + lastInArchive + ".zip";
			File curArchiveFile = new File(pathToZipped, archiveFileName);
			curGArchive = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(curArchiveFile)));
		}

		writeGTsEncoded(curGArchive, alleles);

		int nextIndex = index + 1;
		if (((nextIndex % perArchive) == 0) || (nextIndex  == total)) {
			curGArchive.close();
			curGArchive = null;
			log.info("written: {} / {}", nextIndex, total);
		}
	}

	private static void writeGTsEncoded(OutputStream out, Collection<byte[]> alleles) throws IOException {

		for (byte[] allele : alleles) {
			out.write(allele);
		}
	}

	@Override
	public void finishedLoadingAlleles() throws IOException {

		log.info("Done writing genotypes to zip files");
	}

	@Override
	public void done() throws IOException {
	}
}
