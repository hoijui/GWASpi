package org.gwaspi.netCDF.exporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import org.gwaspi.constants.cExport;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.markers.MarkerSet;
import org.gwaspi.samples.SampleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.NetcdfFile;

public class PlinkTransposedFormatter implements Formatter {

	private final Logger log = LoggerFactory.getLogger(PlinkTransposedFormatter.class);

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
		String sep = cExport.separator_PLINK;
		NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());
		rdMarkerSet.initFullMarkerIdSetMap();

		try {
			//<editor-fold defaultstate="expanded" desc="TPED FILE">
			FileWriter tpedFW = new FileWriter(exportDir.getPath() + "/" + rdMatrixMetadata.getMatrixFriendlyName() + ".tped");
			BufferedWriter tpedBW = new BufferedWriter(tpedFW);

			// TPED files:
			// chromosome (1-22, X(23), Y(24), XY(25), MT(26) or 0 if unplaced)
			// rs# or snp identifier
			// Genetic distance (morgans)
			// Base-pair position (bp units)
			// Genotypes

			// PURGE MARKERSET
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

			// Iterate through markerset
			int markerNb = 0;
			for (char[] pos : rdMarkerSet.getMarkerIdSetMapCharArray().values()) {
				StringBuilder line = new StringBuilder();

				// Iterate through sampleset
				StringBuilder genotypes = new StringBuilder();
				rdSampleSet.readAllSamplesGTsFromCurrentMarkerToMap(rdNcFile, rdSampleSetMap, markerNb);
				for (byte[] tempGT : rdSampleSetMap.values()) {
					genotypes.append(sep);
					genotypes.append(new String(tempGT, 0, 1));
					genotypes.append(sep);
					genotypes.append(new String(tempGT, 1, 1));
				}

				line.append(new String(pos));
				line.append(genotypes);

				tpedBW.append(line);
				tpedBW.append("\n");
				markerNb++;
			}

			log.info("Markers exported to tped: {}", markerNb);

			tpedBW.close();
			tpedFW.close();
			//</editor-fold>

			//<editor-fold defaultstate="expanded" desc="TFAM FILE">
			FileWriter tfamFW = new FileWriter(exportDir.getPath() + "/" + rdMatrixMetadata.getMatrixFriendlyName() + ".tfam");
			BufferedWriter tfamBW = new BufferedWriter(tfamFW);

			// Iterate through all samples
			int sampleNb = 0;
			for (SampleKey sampleKey : rdSampleSetMap.keySet()) {
				SampleInfo sampleInfo = Utils.getCurrentSampleFormattedInfo(sampleKey, rdMatrixMetadata.getStudyId());

				String familyId = sampleInfo.getFamilyId();
				String fatherId = sampleInfo.getFatherId();
				String motherId = sampleInfo.getMotherId();
				String sex = sampleInfo.getSexStr();
				String affection = sampleInfo.getAffectionStr();

				// TFAM files
				// Family ID
				// Individual ID
				// Paternal ID
				// Maternal ID
				// Sex (1=male; 2=female; other=unknown)
				// Affection

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
				line.append(affection);

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
			//</editor-fold>

			result = true;
		} catch (IOException ex) {
			log.error(null, ex);
		} finally {
			if (null != rdNcFile) {
				try {
					rdNcFile.close();
				} catch (IOException ex) {
					log.warn("Cannot close file: " + rdNcFile, ex);
				}
			}
		}

		return result;
	}
}
