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
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.Operation;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.markers.MarkerSet;
import org.gwaspi.reports.GatherQAMarkersData;
import org.gwaspi.samples.SampleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.NetcdfFile;

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
			MarkerSet rdMarkerSet,
			SampleSet rdSampleSet,
			Map<SampleKey, byte[]> rdSampleSetMap,
			String phenotype)
			throws IOException
	{
		File exportDir = new File(exportPath);
		if (!exportDir.exists() || !exportDir.isDirectory()) {
			return false;
		}

		boolean result = false;
		NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());
		String sep = cExport.separator_PLINK;

		//<editor-fold defaultstate="expanded" desc="BIM FILE">
		String filePath = exportDir.getPath() + "/" + rdMatrixMetadata.getMatrixFriendlyName() + ".bim";
		FileWriter mapFW = new FileWriter(filePath);
		BufferedWriter mapBW = new BufferedWriter(mapFW);

		// BIM files
		//     chromosome (1-22, X(23), Y(24), XY(25), MT(26) or 0 if unplaced)
		//     rs# or snp identifier
		//     Genetic distance (morgans)
		//     Base-pair position (bp units)
		//     Allele 1
		//     Allele 2

		// PURGE MARKERSET
		rdMarkerSet.initFullMarkerIdSetMap();
		rdMarkerSet.fillWith(new char[0]);

		// MARKERSET CHROMOSOME
		rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);

		// MARKERSET RSID
		rdMarkerSet.appendVariableToMarkerSetMapValue(cNetCDF.Variables.VAR_MARKERS_RSID, sep);

		// DEFAULT GENETIC DISTANCE = 0
		for (Map.Entry<?, char[]> entry : rdMarkerSet.getMarkerIdSetMapCharArray().entrySet()) {
			StringBuilder value = new StringBuilder(new String(entry.getValue()));
			value.append(sep);
			value.append("0");
			entry.setValue(value.toString().toCharArray());
		}

		// MARKERSET POSITION
		rdMarkerSet.appendVariableToMarkerSetMapValue(cNetCDF.Variables.VAR_MARKERS_POS, sep);

		// ALLELES
		List<Operation> operations = OperationsList.getOperationsList(rdMatrixMetadata.getMatrixId());
		int markersQAOpId = OperationsList.getIdOfLastOperationTypeOccurance(operations, OPType.MARKER_QA);

		Map<MarkerKey, char[]> minorAllelesMap = GatherQAMarkersData.loadMarkerQAMinorAlleles(markersQAOpId);
		Map<MarkerKey, char[]> majorAllelesMap = GatherQAMarkersData.loadMarkerQAMajorAlleles(markersQAOpId);
		Map<MarkerKey, Double> minorAlleleFreqMap = GatherQAMarkersData.loadMarkerQAMinorAlleleFrequency(markersQAOpId);
		for (Map.Entry<MarkerKey, char[]> entry : rdMarkerSet.getMarkerIdSetMapCharArray().entrySet()) {
			MarkerKey key = entry.getKey();
			String minorAllele = new String(minorAllelesMap.get(key));
			String majorAllele = new String(majorAllelesMap.get(key));
			Double minAlleleFreq = minorAlleleFreqMap.get(key);

			if (minAlleleFreq == 0.5) { // IF BOTH ALLELES ARE EQUALLY COMMON, USE ALPHABETICAL ORDER
				String tmpMinorAllele = majorAllele;
				majorAllele = minorAllele;
				minorAllele = tmpMinorAllele;

				majorAllelesMap.put(key, majorAllele.toCharArray());
				minorAllelesMap.put(key, minorAllele.toCharArray());
			}

			char[] values = entry.getValue();
			mapBW.append(new String(values));
			mapBW.append(sep);
			mapBW.append(minorAllele);
			mapBW.append(sep);
			mapBW.append(majorAllele);
			mapBW.append("\n");
		}

		mapBW.close();
		mapFW.close();
		org.gwaspi.global.Utils.sysoutCompleted("Completed exporting BIM file to " + filePath);
		//</editor-fold>

		//<editor-fold defaultstate="expanded" desc="BED FILE">
		// THIS SHOULD BE MULTIPLE OF SAMPLE SET LENGTH
		int nbOfSamples = rdSampleSet.getSampleSetSize();
		int bytesPerSampleSet = ((int) Math.ceil((double) nbOfSamples / 8)) * 2;
		int nbOfMarkers = rdMarkerSet.getMarkerSetSize();
		int nbRowsPerChunk = Math.round((float) org.gwaspi.gui.StartGWASpi.maxProcessMarkers / nbOfSamples);
		if (nbRowsPerChunk > nbOfMarkers) {
			nbRowsPerChunk = nbOfMarkers;
		}
		int byteChunkSize = bytesPerSampleSet * nbRowsPerChunk;

		// Create an output stream to the file.
		filePath = exportDir.getPath() + "/" + rdMatrixMetadata.getMatrixFriendlyName() + ".bed";
		File bedFW = new File(filePath);
		FileOutputStream file_output = new FileOutputStream(bedFW);
		DataOutputStream data_out = new DataOutputStream(file_output);

//          |----magic number---|  |---mode--|  |----------genotype data-----------|
//          01101100 00011011   00000001   11011100 00001111 11100111
//                                              (SNP-major)

		data_out.writeByte(108); // TODO document magic number
		data_out.writeByte(27); // TODO document magic number
		data_out.writeByte(1); // mode SNP-major

		int markerNb = 0;
		int byteCount = 0;
		byte[] wrBytes = new byte[byteChunkSize];
		// ITERATE THROUGH ALL MARKERS, ONE SAMPLESET AT A TIME
		for (MarkerKey markerKey : rdMarkerSet.getMarkerKeys()) {
			String tmpMinorAllele = new String(minorAllelesMap.get(markerKey));
			String tmpMajorAllele = new String(majorAllelesMap.get(markerKey));

			// GET SAMPLESET FOR CURRENT MARKER
			rdSampleSet.readAllSamplesGTsFromCurrentMarkerToMap(rdNcFile, rdSampleSetMap, markerNb);

			for (Iterator<byte[]> rdSampleGts = rdSampleSetMap.values().iterator(); rdSampleGts.hasNext();) {
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
					data_out.write(wrBytes, 0, byteChunkSize);

					// INIT NEW CHUNK
					wrBytes = new byte[byteChunkSize];
					byteCount = 0;
				}
			}

			markerNb++;
		}

		// WRITE LAST BITES TO FILE
		data_out.write(wrBytes, 0, byteCount);

		// Close file when finished with it..
		data_out.close();

		org.gwaspi.global.Utils.sysoutCompleted("Completed exporting BED file to " + filePath);
		//</editor-fold>

		//<editor-fold defaultstate="expanded" desc="FAM FILE">
		filePath = exportDir.getPath() + "/" + rdMatrixMetadata.getMatrixFriendlyName() + ".fam";
		FileWriter tfamFW = new FileWriter(filePath);
		BufferedWriter tfamBW = new BufferedWriter(tfamFW);

		// Iterate through all samples
		int sampleNb = 0;
		for (SampleKey sampleKey : rdSampleSetMap.keySet()) {
			SampleInfo sampleInfo = Utils.getCurrentSampleFormattedInfo(sampleKey, rdMatrixMetadata.getStudyId());

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

			StringBuilder line = new StringBuilder();
			line.append(familyId);
			line.append(sep);
			line.append(sampleKey.getSampleId());
			line.append(sep);
			line.append(fatherId);
			line.append(sep);
			line.append(motherId);
			line.append(sep);
			line.append(sex);
			line.append(sep);
			line.append(affectionOrExpPhenotype);

			tfamBW.append(line);
			tfamBW.append("\n");
			tfamBW.flush();

			sampleNb++;
			if (sampleNb % 100 == 0) {
				log.info("Samples exported: {}", sampleNb);
			}
		}
		tfamBW.close();
		tfamFW.close();
		org.gwaspi.global.Utils.sysoutCompleted("Completed exporting FAM file to " + filePath);
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
