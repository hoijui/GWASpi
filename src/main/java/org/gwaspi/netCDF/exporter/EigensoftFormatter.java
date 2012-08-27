package org.gwaspi.netCDF.exporter;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import org.gwaspi.model.OperationsList;
import org.gwaspi.netCDF.markers.MarkerSet_opt;
import org.gwaspi.netCDF.matrices.MatrixMetadata;
import org.gwaspi.reports.GatherQAMarkersData;
import org.gwaspi.samples.SampleSet;
import ucar.nc2.NetcdfFile;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class EigensoftFormatter implements Formatter {

	public boolean export(
			String exportPath,
			MatrixMetadata rdMatrixMetadata,
			MarkerSet_opt rdMarkerSet,
			SampleSet rdSampleSet,
			Map<String, Object> rdSampleSetMap,
			String phenotype)
			throws IOException
	{
		File exportDir = new File(exportPath);
		if (!exportDir.exists() || !exportDir.isDirectory()) {
			return false;
		}

		boolean result = false;
		NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());
		String sep = org.gwaspi.constants.cExport.separator_PLINK;


		//<editor-fold defaultstate="collapsed" desc="BIM FILE">
		String filePath = exportDir.getPath() + "/" + rdMatrixMetadata.getMatrixFriendlyName() + ".bim";
		FileWriter mapFW = new FileWriter(filePath);
		BufferedWriter mapBW = new BufferedWriter(mapFW);

		//BIM files
		//     chromosome (1-22, X(23), Y(24), XY(25), MT(26) or 0 if unplaced)
		//     rs# or snp identifier
		//     Genetic distance (morgans)
		//     Base-pair position (bp units)
		//     Allele 1
		//     Allele 2

		//PURGE MARKERSET
		rdMarkerSet.initFullMarkerIdSetLHM();
		rdMarkerSet.fillInitLHMWithMyValue("");

		//MARKERSET CHROMOSOME
		rdMarkerSet.fillInitLHMWithVariable(org.gwaspi.constants.cNetCDF.Variables.VAR_MARKERS_CHR);

		//MARKERSET RSID
		rdMarkerSet.appendVariableToMarkerSetLHMValue(org.gwaspi.constants.cNetCDF.Variables.VAR_MARKERS_RSID, sep);

		//DEFAULT GENETIC DISTANCE = 0
		for (Map.Entry<String, Object> entry : rdMarkerSet.getMarkerIdSetLHM().entrySet()) {
			StringBuilder value = new StringBuilder(entry.getValue().toString());
			value.append(sep);
			value.append("0");
			rdMarkerSet.getMarkerIdSetLHM().put(entry.getKey(), value); // FIXME use value.toString() instead?
		}

		//MARKERSET POSITION
		rdMarkerSet.appendVariableToMarkerSetLHMValue(org.gwaspi.constants.cNetCDF.Variables.VAR_MARKERS_POS, sep);

		//ALLELES
		OperationsList opList = new OperationsList(rdMatrixMetadata.getMatrixId());
		int markersQAOpId = opList.getIdOfLastOperationTypeOccurance(org.gwaspi.constants.cNetCDF.Defaults.OPType.MARKER_QA);

		Map<String, Object> minorAllelesLHM = GatherQAMarkersData.loadMarkerQAMinorAlleles(markersQAOpId);
		Map<String, Object> majorAllelesLHM = GatherQAMarkersData.loadMarkerQAMajorAlleles(markersQAOpId);
		Map<String, Object> minorAlleleFreqLHM = GatherQAMarkersData.loadMarkerQAMinorAlleleFrequency(markersQAOpId);
		for (String key : rdMarkerSet.getMarkerIdSetLHM().keySet()) {
			String minorAllele = minorAllelesLHM.get(key).toString();
			String majorAllele = majorAllelesLHM.get(key).toString();
			Double minAlleleFreq = (Double) minorAlleleFreqLHM.get(key);

			if (minAlleleFreq == 0.5) { //IF BOTH ALLELES ARE EQUALLY COMMON, USE ALPHABETICAL ORDER
				String tmpMinorAllele = majorAllele;
				majorAllele = minorAllele;
				minorAllele = tmpMinorAllele;

				majorAllelesLHM.put(key, majorAllele);
				minorAllelesLHM.put(key, minorAllele);
			}


			Object values = rdMarkerSet.getMarkerIdSetLHM().get(key);
			mapBW.append(values.toString());
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


		//<editor-fold defaultstate="collapsed/expanded" desc="BED FILE">

		//THIS SHOULD BE MULTIPLE OF SAMPLE SET LENGTH
		int nbOfSamples = rdSampleSet.getSampleSetSize();
		int bytesPerSampleSet = ((int) Math.ceil((double) nbOfSamples / 8)) * 2;
		int nbOfMarkers = rdMarkerSet.getMarkerSetSize();
		int nbRowsPerChunk = Math.round(org.gwaspi.gui.StartGWASpi.maxProcessMarkers / nbOfSamples);
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

		data_out.writeByte(108);      //magic number
		data_out.writeByte(27);        //magic number
		data_out.writeByte(1);          //mode SNP-major


		int markerNb = 0;
		int byteCount = 0;
		byte[] wrBytes = new byte[byteChunkSize];
		//ITERATE THROUGH ALL MARKERS, ONE SAMPLESET AT A TIME
		for (String markerId : rdMarkerSet.getMarkerIdSetLHM().keySet()) {
			String tmpMinorAllele = minorAllelesLHM.get(markerId).toString();
			String tmpMajorAllele = majorAllelesLHM.get(markerId).toString();

			//GET SAMPLESET FOR CURRENT MARKER
			rdSampleSetMap = rdSampleSet.readAllSamplesGTsFromCurrentMarkerToLHM(rdNcFile, rdSampleSetMap, markerNb);

			for (Iterator<String> rdSampleIds = rdSampleSetMap.keySet().iterator(); rdSampleIds.hasNext();) {
				//ONE BYTE AT A TIME (4 SAMPLES)
				StringBuilder tetraGTs = new StringBuilder("");
				for (int i = 0; i < 4; i++) {
					if (rdSampleIds.hasNext()) {
						String sampleId = rdSampleIds.next();
						byte[] tempGT = (byte[]) rdSampleSetMap.get(sampleId);
						byte[] translatedByte = translateTo00011011Byte(tempGT, tmpMinorAllele, tmpMajorAllele);
						tetraGTs.insert(0, translatedByte[0]); //REVERSE ORDER, AS PER PLINK SPECS http://pngu.mgh.harvard.edu/~purcell/plink/binary.shtml
						tetraGTs.insert(0, translatedByte[1]);
					}
				}

				int number = Integer.parseInt(tetraGTs.toString(), 2);
				byte[] tetraGT = new byte[]{(byte) number};

				System.arraycopy(tetraGT,
						0,
						wrBytes,
						byteCount,
						1);
				byteCount++;

				if (byteCount == byteChunkSize) {
					//WRITE TO FILE
					data_out.write(wrBytes, 0, byteChunkSize);

					//INIT NEW CHUNK
					wrBytes = new byte[byteChunkSize];
					byteCount = 0;
				}
			}

			markerNb++;
		}

		//WRITE LAST BITES TO FILE
		data_out.write(wrBytes, 0, byteCount);

		// Close file when finished with it..
		file_output.close();

		org.gwaspi.global.Utils.sysoutCompleted("Completed exporting BED file to " + filePath);
		//</editor-fold>


		//<editor-fold defaultstate="collapsed" desc="FAM FILE">
		filePath = exportDir.getPath() + "/" + rdMatrixMetadata.getMatrixFriendlyName() + ".fam";
		FileWriter tfamFW = new FileWriter(filePath);
		BufferedWriter tfamBW = new BufferedWriter(tfamFW);

		//Iterate through all samples
		int sampleNb = 0;
		for (String sampleId : rdSampleSetMap.keySet()) {
			Map<String, Object> sampleInfo = Utils.getCurrentSampleFormattedInfo(sampleId, rdMatrixMetadata.getStudyId());

			String familyId = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_FAMILY_ID).toString();
			String fatherId = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_FATHER_ID).toString();
			String motherId = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_MOTHER_ID).toString();
			String sex = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_SEX).toString();
			String expPhenotype = sampleInfo.get(phenotype).toString();

			//FAM files
			//Family ID
			//Individual ID
			//Paternal ID
			//Maternal ID
			//Sex (1=male; 2=female; other=unknown)
			//Phenotype

			StringBuilder line = new StringBuilder();
			line.append(familyId);
			line.append(sep);
			line.append(sampleId);
			line.append(sep);
			line.append(fatherId);
			line.append(sep);
			line.append(motherId);
			line.append(sep);
			line.append(sex);
			line.append(sep);
			line.append(expPhenotype);


			tfamBW.append(line);
			tfamBW.append("\n");
			tfamBW.flush();

			sampleNb++;
			if (sampleNb % 100 == 0) {
				System.out.println("Samples exported:" + sampleNb);
			}

		}
		tfamBW.close();
		tfamFW.close();
		org.gwaspi.global.Utils.sysoutCompleted("Completed exporting FAM file to " + filePath);
		//</editor-fold>

		return result;
	}

	protected static byte[] translateTo00011011Byte(byte[] tempGT, String tmpMinorAllele, String tmpMajorAllele) {
		byte[] result;
		if (tempGT[0] == 48
				|| tempGT[1] == 48) {
			//SOME MISSING ALLELES => SET ALL TO MISSING
			result = new byte[]{1, 0};
		} else {
			String allele1 = new String(new byte[]{tempGT[0]});
			String allele2 = new String(new byte[]{tempGT[1]});

			if (allele1.equals(tmpMinorAllele)) {
				if (allele2.equals(tmpMinorAllele)) {
					//HOMOZYGOUS FOR MINOR ALLELE
					result = new byte[]{0, 0};
				} else {
					//HETEROZYGOUS
					result = new byte[]{0, 1};
				}
			} else {
				if (allele2.equals(tmpMajorAllele)) {
					//HOMOZYGOUS FOR MAJOR ALLELE
					result = new byte[]{1, 1};
				} else {
					//HETEROZYGOUS
					result = new byte[]{0, 1};
				}
			}
		}
		return result;
	}
}
