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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.StudyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadGTFromBeagleFiles extends AbstractLoadGTFromFiles {

//	private final Logger log
//			= LoggerFactory.getLogger(LoadGTFromBeagleFiles.class);

	private static interface Standard {

		public static final int markerId = 1;
		public static final int genotypes = 2;
		public static final String missing = "0";
	}

	public LoadGTFromBeagleFiles() {
		super(new MetadataLoaderBeagle(), ImportFormat.BEAGLE, StrandType.UNKNOWN, false);
	}

	@Override
	public Iterator<Map.Entry<MarkerKey, byte[]>> iterator(
			StudyKey studyKey,
			SampleKey sampleKey,
			File file)
			throws IOException
	{
		return new BeagleParseIterator(studyKey, file, sampleKey);
	}

	@Override
	protected void addAdditionalBigDescriptionProperties(StringBuilder descSB, GenotypesLoadDescription loadDescription) {
		super.addAdditionalBigDescriptionProperties(descSB, loadDescription);

		descSB.append(loadDescription.getGtDirPath());
		descSB.append(" (Genotype file)\n");
		descSB.append(loadDescription.getAnnotationFilePath());
		descSB.append(" (Marker file)\n");
	}

	private static class BeagleParseIterator implements Iterator<Map.Entry<MarkerKey, byte[]>> {

		private final Logger log
				= LoggerFactory.getLogger(BeagleParseIterator.class);

		private final StudyKey studyKey;
		private final SampleKey sampleKey;
		private final File file;
		private final Map<SampleKey, Integer> sampleOrder;
		private BufferedReader inputBufferReader;
		private Map.Entry<MarkerKey, byte[]> next;

		BeagleParseIterator(
				StudyKey studyKey,
				File file,
				SampleKey sampleKey)
				throws IOException
		{
			this.studyKey = studyKey;
			this.sampleKey = sampleKey;
			this.file = file;
			this.sampleOrder = new LinkedHashMap<SampleKey, Integer>();

			BufferedReader tmpInputBufferReader;
//			try {
				FileReader inputFileReader = new FileReader(file);
				tmpInputBufferReader = new BufferedReader(inputFileReader);
//			} catch (IOException ex) {
//				tmpInputBufferReader = null;
//				log.info("Failed to open a stream to read from " + file.getAbsolutePath(), ex);
//			}
			this.inputBufferReader = tmpInputBufferReader;

//			int gtStride = cNetCDF.Strides.STRIDE_GT;
//			StringBuilder sb = new StringBuilder(gtStride);
//			for (int i = 0; i < sb.capacity(); i++) {
//				sb.append('0');
//			}

			this.next = null;
		}

		@Override
		public boolean hasNext() {

			boolean hasNext = (next != null);

			if (!hasNext) {
				// try to read, until we have a next or reach the end of the stream
				String line;
				Map.Entry<MarkerKey, byte[]> markerGT;
				do {
					line = readLine();
					if (line == null) {
						break;
					}
					markerGT = parseLine(line);
				} while  (markerGT == null);

//				wrMarkerSetMap.putAll(tempMarkerIdMap);

		//		GenotypeEncoding guessedGTCode = GenotypeEncoding.UNKNOWN;
		//		if (guessedGTCode.equals(GenotypeEncoding.UNKNOWN)) {
		//			guessedGTCode = Utils.detectGTEncoding(wrMarkerSetMap);
		//		} else if (guessedGTCode.equals(GenotypeEncoding.O12)) {
		//			guessedGTCode = Utils.detectGTEncoding(wrMarkerSetMap);
		//		}
			}

			return hasNext;
		}

		@Override
		public Map.Entry<MarkerKey, byte[]> next() {

			if (!hasNext()) {
				throw new NoSuchElementException();
			}

			Map.Entry<MarkerKey, byte[]> current = next;
			next = null;

			return current;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Removing is not allowed");
		}

		private String readLine() {

			String line = null;

			if (inputBufferReader != null) {
				try {
					line = inputBufferReader.readLine();
				} catch (IOException ex) {
					line = null;
					log.info("Failed to read a line from " + file.getAbsolutePath(), ex);
				}
				if (line == null) {
					try {
						inputBufferReader.close();
					} catch (IOException ex) {
						line = null;
						log.info("Failed to close stream to " + file.getAbsolutePath(), ex);
					}
					inputBufferReader = null;
				}
			}

			return line;
		}

		private Map.Entry<MarkerKey, byte[]> parseLine(String line) {

			Map.Entry<MarkerKey, byte[]> markerGt = null;

			if (line.startsWith("I")) { // Found first marker row!
				String sampleHeader = line;
				String[] headerFields = sampleHeader.split(cImport.Separators.separators_SpaceTab_rgxp);
				for (int i = Standard.genotypes; i < headerFields.length; i = i + 2) {
					String sampleId = headerFields[i];
					// NOTE The Beagle format does not have a family-ID
					sampleOrder.put(new SampleKey(studyKey, sampleId, SampleKey.FAMILY_ID_NONE), i);
				}
			} else if (line.startsWith("M")) { // Found first marker row!
				// GET ALLELES FROM MARKER ROWS
				String[] cVals = line.split(cImport.Separators.separators_SpaceTab_rgxp);
				MarkerKey markerKey = MarkerKey.valueOf(cVals[Standard.markerId]);

				Integer columnNb = sampleOrder.get(sampleKey);
				if (columnNb != null) {
					// XXX this seems like it could be optimized to not do concatenation and char extraction, but use the chars from the orig strings directly
					String strAlleles = cVals[columnNb] + cVals[columnNb + 1];
					byte[] tmpAlleles = new byte[] {
						(byte) strAlleles.charAt(0),
						(byte) strAlleles.charAt(1)};
					markerGt = new AbstractMap.SimpleImmutableEntry(markerKey, tmpAlleles);
				}
			}

			return markerGt;
		}
	}

//	@Override
//	public void loadIndividualFiles(
//			StudyKey studyKey,
//			File file,
//			SampleKey sampleKey,
//			Map<MarkerKey, byte[]> wrMarkerSetMap)
//			throws IOException, InvalidRangeException
//	{
//		FileReader inputFileReader = new FileReader(file);
//		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);
//
////		int gtStride = cNetCDF.Strides.STRIDE_GT;
////		StringBuilder sb = new StringBuilder(gtStride);
////		for (int i = 0; i < sb.capacity(); i++) {
////			sb.append('0');
////		}
//
//		Map<MarkerKey, byte[]> tempMarkerIdMap = new LinkedHashMap<MarkerKey, byte[]>();
//		Map<SampleKey, Integer> sampleOrderMap = new LinkedHashMap<SampleKey, Integer>();
//
//		String l;
//		while ((l = inputBufferReader.readLine()) != null) {
//			parseLine(l, studyKey, sampleKey, sampleOrderMap, tempMarkerIdMap);
//		}
//		inputBufferReader.close();
//
//		wrMarkerSetMap.putAll(tempMarkerIdMap);
//
////		GenotypeEncoding guessedGTCode = GenotypeEncoding.UNKNOWN;
////		if (guessedGTCode.equals(GenotypeEncoding.UNKNOWN)) {
////			guessedGTCode = Utils.detectGTEncoding(wrMarkerSetMap);
////		} else if (guessedGTCode.equals(GenotypeEncoding.O12)) {
////			guessedGTCode = Utils.detectGTEncoding(wrMarkerSetMap);
////		}
//	}

//	private static void parseLine(
//			String line,
//			StudyKey studyKey,
//			SampleKey sampleKey,
//			Map<SampleKey, Integer> sampleOrderMap,
//			Map<MarkerKey, byte[]> parsedMarkers)
//			throws IOException
//	{
//			if (line.startsWith("I")) { // Found first marker row!
//				String sampleHeader = line;
//				String[] headerFields = sampleHeader.split(cImport.Separators.separators_SpaceTab_rgxp);
//				for (int i = Standard.genotypes; i < headerFields.length; i = i + 2) {
//					String sampleId = headerFields[i];
//					// NOTE The Beagle format does not have a family-ID
//					sampleOrderMap.put(new SampleKey(studyKey, sampleId, SampleKey.FAMILY_ID_NONE), i);
//				}
//			} else if (line.startsWith("M")) { // Found first marker row!
//				// GET ALLELES FROM MARKER ROWS
//				String[] cVals = line.split(cImport.Separators.separators_SpaceTab_rgxp);
//				MarkerKey markerKey = MarkerKey.valueOf(cVals[Standard.markerId]);
//
//				Integer columnNb = sampleOrderMap.get(sampleKey);
//				if (columnNb != null) {
//					String strAlleles = cVals[columnNb] + cVals[columnNb + 1];
//					byte[] tmpAlleles = new byte[] {
//						(byte) strAlleles.toString().charAt(0),
//						(byte) strAlleles.toString().charAt(1)};
//					parsedMarkers.put(markerKey, tmpAlleles);
//				}
//			}
//	}
}
