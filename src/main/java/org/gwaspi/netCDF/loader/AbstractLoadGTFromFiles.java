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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cImport.StrandFlags;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.model.DataSet;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.StudyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractLoadGTFromFiles implements GenotypesLoader {

	private final Logger log
			= LoggerFactory.getLogger(AbstractLoadGTFromFiles.class);

	private final ImportFormat format;
	private final StrandType matrixStrand;
	private final boolean hasDictionary;
	private GenotypeEncoding guessedGTCode;

	public AbstractLoadGTFromFiles(
			ImportFormat format,
			StrandType matrixStrand,
			boolean hasDictionary
			)
	{
		this.format = format;
		this.matrixStrand = matrixStrand;
		this.hasDictionary = hasDictionary;
		this.guessedGTCode = GenotypeEncoding.UNKNOWN;
	}

	@Override
	public ImportFormat getFormat() {
		return format;
	}

	@Override
	public StrandType getMatrixStrand() {
		return matrixStrand;
	}

	@Override
	public boolean isHasDictionary() {
		return hasDictionary;
	}

	public GenotypeEncoding getGuessedGTCode() {
		return guessedGTCode;
	}

	public void setGuessedGTCode(GenotypeEncoding guessedGTCode) {
		this.guessedGTCode = guessedGTCode;
	}

	protected void addAdditionalBigDescriptionProperties(StringBuilder descSB, GenotypesLoadDescription loadDescription) {
	}

	protected abstract MetadataLoader createMetaDataLoader(GenotypesLoadDescription loadDescription);

	protected boolean isLoadAllelePerSample() {
		return true;
	}

	/**
	 * @see #isHasStrandInfo
	 */
	protected String getStrandFlag(GenotypesLoadDescription loadDescription) {

		String strandFlag;

		switch (loadDescription.getStrand()) {
			case PLUS:
				strandFlag = StrandFlags.strandPLS;
				break;
			case MINUS:
				strandFlag = StrandFlags.strandMIN;
				break;
			case FWD:
				strandFlag = StrandFlags.strandFWD;
				break;
			case REV:
				strandFlag = StrandFlags.strandREV;
				break;
			default:
				strandFlag = StrandFlags.strandUNK;
				break;
		}

		return strandFlag;
	}

	protected void loadMarkerMetadata(GenotypesLoadDescription loadDescription, DataSetDestination samplesReceiver) throws Exception {

		MetadataLoader markerSetLoader = createMetaDataLoader(loadDescription);
		samplesReceiver.startLoadingMarkerMetadatas();
		markerSetLoader.loadMarkers(samplesReceiver);
		samplesReceiver.finishedLoadingMarkerMetadatas();
	}

	/**
	 * Whether each marker loaded has a strand info.
	 * If false, then a global one has to be provided.
	 * If true, then the MarkerMetadata has to have strand info set
	 * (the ctor to set it has to be used, or a setter, if one exists).
	 * @see #getStrandFlag
	 */
	protected boolean isHasStrandInfo() {
		return false;
	}

	//<editor-fold defaultstate="expanded" desc="PROCESS GENOTYPES">
	@Override
	public void processData(GenotypesLoadDescription loadDescription, DataSetDestination samplesReceiver) throws Exception {
		loadMarkerMetadata(loadDescription, samplesReceiver);

		samplesReceiver.startLoadingAlleles(isLoadAllelePerSample());
		loadGenotypes(loadDescription, samplesReceiver);
		samplesReceiver.finishedLoadingAlleles();
	}

	protected void loadGenotypes(
			GenotypesLoadDescription loadDescription,
			DataSetDestination samplesReceiver)
			throws Exception
	{
		File gtFile = new File(loadDescription.getGtDirPath());
		File[] gtFilesToImport;
		if (gtFile.isDirectory()) {
			gtFilesToImport = org.gwaspi.global.Utils.listFiles(loadDescription.getGtDirPath());
		} else {
			gtFilesToImport = new File[]{new File(loadDescription.getGtDirPath())};
		}
		int sampleIndex = 0;
		// HACK
		DataSet dataSet = ((InMemorySamplesReceiver) samplesReceiver).getDataSet();
		for (SampleInfo sampleInfo : dataSet.getSampleInfos()) {
			// PURGE MarkerIdMap
			Map<MarkerKey, byte[]> alleles = fillMap(dataSet.getMarkerMetadatas().keySet(), cNetCDF.Defaults.DEFAULT_GT);

			for (int i = 0; i < gtFilesToImport.length; i++) {
				try {
//					loadIndividualFiles(
//							loadDescription.getStudyKey(),
//							gtFilesToImport[i],
//							sampleInfo.getKey(),
//							alleles);
					Iterator<Map.Entry<MarkerKey, byte[]>> markerGtIt = iterator(
							loadDescription.getStudyKey(),
							sampleInfo.getKey(),
							gtFilesToImport[i]);
					while (markerGtIt.hasNext()) {
						Map.Entry<MarkerKey, byte[]> markerGt = markerGtIt.next();
						alleles.put(markerGt.getKey(), markerGt.getValue());
					}

					if (Thread.interrupted()) {
						throw new InterruptedException();
					}
				} catch (IOException ex) {
					log.warn(null, ex);
				} catch (InterruptedException ex) {
					log.warn(null, ex);
					// TODO Write some cleanup code for when thread has been interrupted
				}
			}

			// WRITING GENOTYPE DATA INTO netCDF FILE
			samplesReceiver.addSampleGTAlleles(sampleIndex, alleles.values());
//					org.gwaspi.netCDF.operations.Utils.saveSingleSampleGTsToMatrix(ncfile, alleles, sampleIndex);

			sampleIndex++;
			if ((sampleIndex == 1) || (sampleIndex % 100 == 0)) {
				log.info("Done processing sample {} / {}", sampleIndex,
						dataSet.getSampleInfos().size());
			}
		}
	}

	static <K, V> Map<K, V> fillMap(Collection<K> keys, V value) {

		Map<K, V> result = new LinkedHashMap<K, V>(keys.size());

		for (K key : keys) {
			result.put(key, value);
		}

		return result;
	}

	/**
	 * Loads all the alleles from a single sample.
	 * @param file to load the sample from
	 * @param sampleKey uniquely specifies the sample to be loaded
	 *   (in the containing set)
	 * @param alleles maps marker-keys to an allele pairs;
	 *   the values are usually of type byte[2]
	 */
//	public abstract void loadIndividualFiles(
//			StudyKey studyKey,
//			File file,
//			SampleKey sampleKey,
//			Map<MarkerKey, byte[]> alleles)
//			throws IOException, InvalidRangeException;
	protected abstract Iterator<Map.Entry<MarkerKey, byte[]>> iterator(
			StudyKey studyKey,
			SampleKey sampleKey,
			File file)
			throws IOException;
	//</editor-fold>

	static void logAsWhole(String startTime, int studyId, String dirPath, ImportFormat format, String matrixName, String description) throws IOException {
		// LOG OPERATION IN STUDY HISTORY
		StringBuilder operation = new StringBuilder("\nLoaded raw " + format + " genotype data in path " + dirPath + ".\n");
		operation.append("Start Time: ").append(startTime).append("\n");
		operation.append("End Time: ").append(org.gwaspi.global.Utils.getMediumDateTimeAsString()).append(".\n");
		operation.append("Data stored in matrix ").append(matrixName).append(".\n");
		operation.append("Description: ").append(description).append(".\n");
		org.gwaspi.global.Utils.logOperationInStudyDesc(operation.toString(), studyId);
	}

	static List<SampleKey> extractKeys(Collection<SampleInfo> sampleInfos) {

		List<SampleKey> sampleKeys = new ArrayList<SampleKey>(sampleInfos.size());

		for (SampleInfo sampleInfo : sampleInfos) {
			sampleKeys.add(sampleInfo.getKey());
		}

		return sampleKeys;
	}
}
