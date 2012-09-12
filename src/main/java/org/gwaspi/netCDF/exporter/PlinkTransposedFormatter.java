package org.gwaspi.netCDF.exporter;

import org.gwaspi.constants.cDBSamples;
import org.gwaspi.constants.cExport;
import org.gwaspi.constants.cNetCDF;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import org.gwaspi.netCDF.markers.MarkerSet_opt;
import org.gwaspi.netCDF.matrices.MatrixMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gwaspi.samples.SampleSet;
import ucar.nc2.NetcdfFile;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class PlinkTransposedFormatter implements Formatter {

	private final Logger log = LoggerFactory.getLogger(PlinkTransposedFormatter.class);

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
		String sep = cExport.separator_PLINK;
		NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());
		rdMarkerSet.initFullMarkerIdSetLHM();

		try {
			//<editor-fold defaultstate="collapsed" desc="TPED FILE">
			FileWriter tpedFW = new FileWriter(exportDir.getPath() + "/" + rdMatrixMetadata.getMatrixFriendlyName() + ".tped");
			BufferedWriter tpedBW = new BufferedWriter(tpedFW);

			// TPED files:
			// chromosome (1-22, X(23), Y(24), XY(25), MT(26) or 0 if unplaced)
			// rs# or snp identifier
			// Genetic distance (morgans)
			// Base-pair position (bp units)
			// Genotypes

			// PURGE MARKERSET
			rdMarkerSet.fillWith("");

			// MARKERSET CHROMOSOME
			rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);

			// MARKERSET RSID
			rdMarkerSet.appendVariableToMarkerSetLHMValue(cNetCDF.Variables.VAR_MARKERS_RSID, sep);

			// DEFAULT GENETIC DISTANCE = 0
			for (Map.Entry<String, Object> entry : rdMarkerSet.getMarkerIdSetLHM().entrySet()) {
				StringBuilder value = new StringBuilder(entry.getValue().toString());
				value.append(sep);
				value.append("0");
				entry.setValue(value.toString());
			}

			// MARKERSET POSITION
			rdMarkerSet.appendVariableToMarkerSetLHMValue(cNetCDF.Variables.VAR_MARKERS_POS, sep);

			// Iterate through markerset
			int markerNb = 0;
			for (Object pos : rdMarkerSet.getMarkerIdSetLHM().values()) {
				StringBuilder line = new StringBuilder();

				// Iterate through sampleset
				StringBuilder genotypes = new StringBuilder();
				Map<String, Object> remainingSampleSet = rdSampleSet.readAllSamplesGTsFromCurrentMarkerToLHM(rdNcFile, rdSampleSetMap, markerNb);
				rdSampleSetMap = remainingSampleSet; // FIXME This line should most likely be removed, because further down this is used again ... check out!
				for (Object value : remainingSampleSet.values()) {
					byte[] tempGT = (byte[]) value;
					genotypes.append(sep);
					genotypes.append(new String(new byte[]{tempGT[0]}));
					genotypes.append(sep);
					genotypes.append(new String(new byte[]{tempGT[1]}));
				}

				line.append(pos.toString());
				line.append(genotypes);

				tpedBW.append(line);
				tpedBW.append("\n");
				markerNb++;
			}

			log.info("Markers exported to tped: {}", markerNb);

			tpedBW.close();
			tpedFW.close();
			//</editor-fold>

			//<editor-fold defaultstate="collapsed" desc="TFAM FILE">
			FileWriter tfamFW = new FileWriter(exportDir.getPath() + "/" + rdMatrixMetadata.getMatrixFriendlyName() + ".tfam");
			BufferedWriter tfamBW = new BufferedWriter(tfamFW);

			// Iterate through all samples
			int sampleNb = 0;
			for (String sampleId : rdSampleSetMap.keySet()) {
				Map<String, Object> sampleInfo = Utils.getCurrentSampleFormattedInfo(sampleId, rdMatrixMetadata.getStudyId());

				String familyId = sampleInfo.get(cDBSamples.f_FAMILY_ID).toString();
				String fatherId = sampleInfo.get(cDBSamples.f_FATHER_ID).toString();
				String motherId = sampleInfo.get(cDBSamples.f_MOTHER_ID).toString();
				String sex = sampleInfo.get(cDBSamples.f_SEX).toString();
				String affection = sampleInfo.get(cDBSamples.f_AFFECTION).toString();

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
				line.append(sampleId);
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
					log.error("Cannot close file: " + rdNcFile, ex);
				}
			}
		}

		return result;
	}
}
