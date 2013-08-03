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

package org.gwaspi.netCDF.exporter;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cExport;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;
import org.gwaspi.reports.GatherQAMarkersData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlinkBinaryFormatter implements Formatter {

	private final Logger log = LoggerFactory.getLogger(PlinkBinaryFormatter.class);

	/**
	 * If true, then we write the Eigensoft format, else PLink Binary.
	 */
	private final boolean eigensoft;

	/**
	 * Creates a new PLink Binary or Eigensoft formatter.
	 * @param eigensoft if true, then we write the Eigensoft format,
	 *   else PLink Binary.
	 */
	public PlinkBinaryFormatter(boolean eigensoft) {
		this.eigensoft = eigensoft;
	}

	/**
	 * Creates a new PLink Binary formatter.
	 */
	public PlinkBinaryFormatter() {
		this(false);
	}

	@Override
	public boolean export(
			String exportPath,
			MatrixMetadata rdMatrixMetadata,
			DataSetSource dataSetSource,
			String phenotype)
			throws IOException
	{
		File exportDir = new File(exportPath);
		if (!exportDir.exists() || !exportDir.isDirectory()) {
			return false;
		}

		boolean result = false;
		String sep = cExport.separator_PLINK;

		// ALLELES
		List<OperationMetadata> operations = OperationsList.getOperationsList(MatrixKey.valueOf(rdMatrixMetadata));
		OperationKey markersQAOpKey = OperationsList.getIdOfLastOperationTypeOccurance(operations, OPType.MARKER_QA);

		Map<MarkerKey, byte[]> minorAllelesMap = GatherQAMarkersData.loadMarkerQAMinorAlleles(markersQAOpKey);
		Map<MarkerKey, byte[]> majorAllelesMap = GatherQAMarkersData.loadMarkerQAMajorAlleles(markersQAOpKey);
		Map<MarkerKey, Double> minorAlleleFreqMap = GatherQAMarkersData.loadMarkerQAMinorAlleleFrequency(markersQAOpKey);

		//<editor-fold defaultstate="expanded" desc="BIM FILE">
		File bimFile = new File(exportDir.getPath(),
				rdMatrixMetadata.getMatrixFriendlyName() + ".bim");
		BufferedWriter bimBW = null;
		try {
			FileWriter mapFW = new FileWriter(bimFile);
			bimBW = new BufferedWriter(mapFW);

			// BIM files
			//     chromosome (1-22, X(23), Y(24), XY(25), MT(26) or 0 if unplaced)
			//     rs# or snp identifier
			//     Genetic distance (morgans)
			//     Base-pair position (bp units)
			//     Allele 1
			//     Allele 2

			for (MarkerMetadata curMarkerMetadata : dataSetSource.getMarkersMetadatasSource()) {
				bimBW.write(curMarkerMetadata.getChr());
				bimBW.write(sep);
				bimBW.write(curMarkerMetadata.getRsId()); // XXX Maybe has to be marker-id instead?
				bimBW.write(sep);
				bimBW.write('0'); // DEFAULT GENETIC DISTANCE
				bimBW.write(sep);
				bimBW.write(Integer.toString(curMarkerMetadata.getPos())); // NOTE This conversion is required, because Writer#write(int) actually writes a char, not the int value.

				// HACK from here on, make nicer! (and unuse NetCDF)
				MarkerKey key = MarkerKey.valueOf(curMarkerMetadata);
				String minorAllele = new String(minorAllelesMap.get(key));
				String majorAllele = new String(majorAllelesMap.get(key));
				Double minAlleleFreq = minorAlleleFreqMap.get(key);
				if (minAlleleFreq == 0.5 && majorAllele.compareTo(majorAllele) > 0) { // IF BOTH ALLELES ARE EQUALLY COMMON, USE ALPHABETICAL ORDER
					String tmpMinorAllele = majorAllele;
					majorAllele = minorAllele;
					minorAllele = tmpMinorAllele;

					majorAllelesMap.put(key, majorAllele.getBytes());
					minorAllelesMap.put(key, minorAllele.getBytes());
				}
				bimBW.write(sep);
				bimBW.write(minorAllele);
				bimBW.write(sep);
				bimBW.write(majorAllele);

				bimBW.write('\n');
			}

			org.gwaspi.global.Utils.sysoutCompleted("Completed exporting BIM file to " + bimFile.getAbsolutePath());
		} finally {
			if (bimBW != null) {
				bimBW.close();
			}
		}
		//</editor-fold>

		//<editor-fold defaultstate="expanded" desc="BED FILE">
		// THIS SHOULD BE MULTIPLE OF SAMPLE SET LENGTH
		int nbOfSamples = dataSetSource.getSamplesKeysSource().size();
		int bytesPerSampleSet = ((int) Math.ceil((double) nbOfSamples / 8)) * 2;
		int nbOfMarkers = dataSetSource.getMarkersKeysSource().size();
		int nbRowsPerChunk = Math.round((float) org.gwaspi.gui.StartGWASpi.maxProcessMarkers / nbOfSamples);
		if (nbRowsPerChunk > nbOfMarkers) {
			nbRowsPerChunk = nbOfMarkers;
		}
		int byteChunkSize = bytesPerSampleSet * nbRowsPerChunk;

		// Create an output stream to the file.
		File bedFile = new File(exportDir.getPath(),
				rdMatrixMetadata.getMatrixFriendlyName() + ".bed");
		DataOutputStream bedBW = null;
		try {
			FileOutputStream bedFW = new FileOutputStream(bedFile);
			bedBW = new DataOutputStream(bedFW);

//	          |----magic number---|  |---mode--|  |----------genotype data-----------|
//	          01101100 00011011   00000001   11011100 00001111 11100111
//	                                              (SNP-major)

			bedBW.writeByte(108); // TODO document magic number
			bedBW.writeByte(27); // TODO document magic number
			bedBW.writeByte(1); // mode SNP-major

			int markerNb = 0;
			int byteCount = 0;
			byte[] wrBytes = new byte[byteChunkSize];
			// ITERATE THROUGH ALL MARKERS, ONE SAMPLESET AT A TIME
			Iterator<GenotypesList> markersGenotypesIt = dataSetSource.getMarkersGenotypesSource().iterator();
			for (MarkerKey markerKey : dataSetSource.getMarkersKeysSource()) {
				String tmpMinorAllele = new String(minorAllelesMap.get(markerKey));
				String tmpMajorAllele = new String(majorAllelesMap.get(markerKey));

				for (Iterator<byte[]> rdSampleGts = markersGenotypesIt.next().iterator(); rdSampleGts.hasNext();) {
					// ONE BYTE AT A TIME (4 SAMPLES)
					StringBuilder tetraGTs = new StringBuilder("");
					for (int i = 0; i < 4; i++) {
						if (rdSampleGts.hasNext()) {
							byte[] tempGT = rdSampleGts.next();
							byte[] translatedByte = translateTo00011011Byte(tempGT, tmpMinorAllele, tmpMajorAllele);
							tetraGTs.insert(0, translatedByte[0]); // REVERSE ORDER, AS PER PLINK SPECS http://pngu.mgh.harvard.edu/~purcell/plink/binary.shtml
							tetraGTs.insert(0, translatedByte[1]);
						}
					}

					int number = Integer.parseInt(tetraGTs.toString(), 2);
					byte[] tetraGT = new byte[] {(byte) number};

					System.arraycopy(tetraGT,
							0,
							wrBytes,
							byteCount,
							1);
					byteCount++;

					if (byteCount == byteChunkSize) {
						// WRITE TO FILE
						bedBW.write(wrBytes, 0, byteChunkSize);

						// INIT NEW CHUNK
						wrBytes = new byte[byteChunkSize];
						byteCount = 0;
					}
				}

				markerNb++;
			}

			// WRITE LAST BITES TO FILE
			bedBW.write(wrBytes, 0, byteCount);

			org.gwaspi.global.Utils.sysoutCompleted("Completed exporting BED file to " + bedFile.getAbsolutePath());
		} finally {
			if (bedBW != null) {
				bedBW.close();
			}
		}
		//</editor-fold>

		//<editor-fold defaultstate="expanded" desc="FAM FILE">
		File famFile = new File(exportDir.getPath(),
				rdMatrixMetadata.getMatrixFriendlyName() + ".fam");
		BufferedWriter tfamBW = null;
		try {
			FileWriter tfamFW = new FileWriter(famFile);
			tfamBW = new BufferedWriter(tfamFW);

			// Iterate through all samples
			int sampleNb = 0;
			for (SampleKey sampleKey : dataSetSource.getSamplesKeysSource()) {
				SampleInfo sampleInfo = Utils.getCurrentSampleFormattedInfo(sampleKey);

				String familyId = sampleInfo.getFamilyId();
				String fatherId = sampleInfo.getFatherId();
				String motherId = sampleInfo.getMotherId();
				String sex = sampleInfo.getSexStr();
				String affectionOrExpPhenotype;
				if (eigensoft) {
					affectionOrExpPhenotype = sampleInfo.getField(phenotype).toString();
				} else {
					affectionOrExpPhenotype = sampleInfo.getAffectionStr();
				}

				// FAM files
				// Family ID
				// Individual ID
				// Paternal ID
				// Maternal ID
				// Sex (1=male; 2=female; other=unknown)
				// Affection (PLink Binary) / Phenotype (Eigensoft)

				tfamBW.append(familyId);
				tfamBW.append(sep);
				tfamBW.append(sampleKey.getSampleId());
				tfamBW.append(sep);
				tfamBW.append(fatherId);
				tfamBW.append(sep);
				tfamBW.append(motherId);
				tfamBW.append(sep);
				tfamBW.append(sex);
				tfamBW.append(sep);
				tfamBW.append(affectionOrExpPhenotype);

				tfamBW.append("\n");
				tfamBW.flush();

				sampleNb++;
				if (sampleNb % 100 == 0) {
					log.info("Samples exported: {}", sampleNb);
				}
			}

			org.gwaspi.global.Utils.sysoutCompleted("Completed exporting FAM file to " + famFile.getAbsolutePath());
		} finally {
			if (tfamBW != null) {
				tfamBW.close();
			}
		}
		//</editor-fold>

		return result;
	}

	private static byte[] translateTo00011011Byte(byte[] tempGT, String tmpMinorAllele, String tmpMajorAllele) {
		byte[] result;
		if (tempGT[0] == 48
				|| tempGT[1] == 48) {
			// SOME MISSING ALLELES => SET ALL TO MISSING
			result = new byte[]{1, 0};
		} else {
			String allele1 = new String(tempGT, 0, 1);
			String allele2 = new String(tempGT, 1, 1);

			if (allele1.equals(tmpMinorAllele)) {
				if (allele2.equals(tmpMinorAllele)) {
					// HOMOZYGOUS FOR MINOR ALLELE
					result = new byte[]{0, 0};
				} else {
					// HETEROZYGOUS
					result = new byte[]{0, 1};
				}
			} else {
				if (allele2.equals(tmpMajorAllele)) {
					// HOMOZYGOUS FOR MAJOR ALLELE
					result = new byte[]{1, 1};
				} else {
					// HETEROZYGOUS
					result = new byte[]{0, 1};
				}
			}
		}
		return result;
	}
}
