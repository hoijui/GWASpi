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

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.constants.ImportConstants.ImportFormat;
import org.gwaspi.constants.NetCDFConstants;
import org.gwaspi.constants.NetCDFConstants.Defaults.GenotypeEncoding;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.StudyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadGTFromPlinkBinaryFiles extends AbstractLoadGTFromFiles implements GenotypesLoader {

	private final Logger log
			= LoggerFactory.getLogger(LoadGTFromPlinkBinaryFiles.class);

	public LoadGTFromPlinkBinaryFiles() {
		super(new MetadataLoaderPlinkBinary(), ImportFormat.PLINK_Binary, null, true);

		setGuessedGTCode(GenotypeEncoding.O12);
	}

	@Override
	public Iterator<Map.Entry<MarkerKey, byte[]>> iterator(
			StudyKey studyKey,
			SampleKey sampleKey,
			File file)
			throws IOException
	{
		throw new UnsupportedOperationException("This method of this class should never be called!");
	}

	@Override
	protected void addAdditionalBigDescriptionProperties(StringBuilder description, GenotypesLoadDescription loadDescription) {
		super.addAdditionalBigDescriptionProperties(description, loadDescription); // XXX uncomment!

		description.append(loadDescription.getAnnotationFilePath());
		description.append(" (BIM file)\n");
		description.append(loadDescription.getGtDirPath());
		description.append(" (BED file)\n");
	}

	@Override
	protected boolean isLoadAllelePerSample() {
		return false;
	}

	@Override
	protected void loadGenotypes(
			GenotypesLoadDescription loadDescription,
			Map<SampleKey, SampleInfo> sampleInfos,
			Map<MarkerKey, MarkerMetadata> markerInfos,
			DataSetDestination samplesReceiver)
			throws IOException
	{
		final Collection<SampleInfo> sampleInfos2 = sampleInfos.values();

		Map<MarkerKey, String[]> bimSamples = MetadataLoaderPlinkBinary.parseOrigBimFile(
				loadDescription.getAnnotationFilePath(),
				loadDescription.getStudyKey()
				); // key = markerId, values{allele1 (minor), allele2 (major)}

		File file = new File(loadDescription.getGtDirPath());
		FileInputStream bedFS = new FileInputStream(file);
		DataInputStream bedIS = new DataInputStream(bedFS);

		int sampleNb = sampleInfos2.size();
//		int markerNb = bimSamples.size();
		int bytesPerSNP;
		if (sampleNb % 4 == 0) { // Nb OF BYTES IN EACH ROW
			bytesPerSNP = sampleNb / 4;
		} else {
			bytesPerSNP = (sampleNb / 4) + 1;
		}

		Iterator<String[]> itMarkerSet = bimSamples.values().iterator();
		Iterator<SampleInfo> itSampleSet = sampleInfos2.iterator();

		// SKIP HEADER
		bedIS.readByte();
		bedIS.readByte();
		byte mode = bedIS.readByte();

		// INIT VARS
		String[] alleles;
		byte[] rowBytes = new byte[bytesPerSNP];
		byte byteData;
		boolean allele1;
		boolean allele2;
		if (mode == 1) {
			// GET GENOTYPES
			int rowCounter = 1;
//			List<byte[]> genotypes = new ArrayList<byte[]>();
			while (itMarkerSet.hasNext()) {
				try {
					// NEW SNP
					// Load genotyes map with defaults
					Map<SampleKey, byte[]> mappedGenotypes = new LinkedHashMap<SampleKey, byte[]>();
					while (itSampleSet.hasNext()) {
						SampleKey sampleKey = SampleKey.valueOf(itSampleSet.next());
						mappedGenotypes.put(sampleKey, NetCDFConstants.Defaults.DEFAULT_GT);
					}

					alleles = itMarkerSet.next(); // key = markerId, values{allele1 (minor), allele2 (major)}

					// READ ALL SAMPLE GTs FOR CURRENT SNP
					int check = bedIS.read(rowBytes, 0, bytesPerSNP);
					int bitsPerRow = 1;
					itSampleSet = sampleInfos2.iterator();
					for (int j = 0; j < bytesPerSNP; j++) { // ITERATE THROUGH ROWS (READING BYTES PER ROW)
						byteData = rowBytes[j];
						for (int i = 0; i < 8; i = i + 2) { // FOR EACH BYTE IN ROW, EAT 2 BITS AT A TIME
							if (bitsPerRow <= (sampleNb * 2)) { // SKIP EXCESS BITS
								allele1 = getBit(byteData, i);
								allele2 = getBit(byteData, i + 1);
								SampleKey sampleKey = SampleKey.valueOf(itSampleSet.next());
								if (!allele1 && !allele2) { // 00 Homozygote "1"/"1" - Minor allele
									mappedGenotypes.put(sampleKey, new byte[] {
											(byte) alleles[0].charAt(0),
											(byte) alleles[0].charAt(0)});
								} else if (allele1 && allele2) { // 11 Homozygote "2"/"2" - Major allele
									mappedGenotypes.put(sampleKey, new byte[] {
											(byte) alleles[1].charAt(0),
											(byte) alleles[1].charAt(0)});
								} else if (!allele1 && allele2) { // 01 Heterozygote
									mappedGenotypes.put(sampleKey, new byte[] {
											(byte) alleles[0].charAt(0),
											(byte) alleles[1].charAt(0)});
								} else if (allele1 && !allele2) { // 10 Missing genotype
									mappedGenotypes.put(sampleKey, NetCDFConstants.Defaults.DEFAULT_GT);
								}

								bitsPerRow = bitsPerRow + 2;
							}
						}
					}

					// WRITING GENOTYPE DATA INTO netCDF FILE
//					if (guessedGTCode.equals(GenotypeEncoding.UNKNOWN)
//							|| guessedGTCode.equals(GenotypeEncoding.O12))
//					{
//						guessedGTCode = Utils.detectGTEncoding(sampleSetMap); // FIXME ???
//					}

					samplesReceiver.addMarkerGTAlleles(rowCounter, new ArrayList<byte[]>(mappedGenotypes.values()));
				} catch (EOFException ex) {
					log.info("End of File", ex);
					break;
				}
				rowCounter++;
			}
		} else {
			log.warn("Binary PLINK file must be in SNP-major mode!");
		}

		bedIS.close();
		bedFS.close();
	}

	private static int translateBitSet(byte b, int bit) {
		int result;
		boolean by = (b & (1 << bit)) != 0;
		if (by) {
			result = 1;
		} else {
			result = 0;
		}
		return result;
	}

	private static boolean getBit(byte b, int pos) {
		boolean by = (b & (1 << pos)) != 0;
		return by;
	}
}
