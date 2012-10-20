package org.gwaspi.netCDF.exporter;

import org.gwaspi.constants.cDBSamples;
import org.gwaspi.constants.cExport;
import org.gwaspi.constants.cNetCDF;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.netCDF.markers.MarkerSet_opt;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleInfo;
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
public class MachFormatter implements Formatter {

	private final Logger log = LoggerFactory.getLogger(MachFormatter.class);
	private static final String SEP = cExport.separator_MACH;

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

		try {
			rdMarkerSet.initFullMarkerIdSetMap();

			//FIND START AND END MARKERS BY CHROMOSOME
			rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
			Map<String, Object> chrMarkerSetMap = new LinkedHashMap<String, Object>();
			chrMarkerSetMap.putAll(rdMarkerSet.getMarkerIdSetMap());
			String tmpChr = "";
			int start = 0;
			int end = 0;
			for (Object value : chrMarkerSetMap.values()) {
				String chr = value.toString();
				if (!chr.equals(tmpChr)) {
					if (start != end) {
						exportChromosomeToMped(exportDir, rdMatrixMetadata, rdMarkerSet, rdSampleSetMap, tmpChr, start, end - 1);
						exportChromosomeToDat(exportDir, rdMatrixMetadata, rdMarkerSet, tmpChr, start, end - 1);
						start = end;
					}
					tmpChr = chr;
				}
				end++;
			}
			exportChromosomeToMped(exportDir, rdMatrixMetadata, rdMarkerSet, rdSampleSetMap, tmpChr, start, end);
			exportChromosomeToDat(exportDir, rdMatrixMetadata, rdMarkerSet, tmpChr, start, end - 1);

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

	private void exportChromosomeToMped(File exportDir, MatrixMetadata rdMatrixMetadata, MarkerSet_opt rdMarkerSet, Map<String, Object> rdSampleSetMap, String chr, int startPos, int endPos) throws IOException {

		FileWriter pedFW = new FileWriter(exportDir.getPath() + "/" + rdMatrixMetadata.getMatrixFriendlyName() + "_chr" + chr + ".mped");
		BufferedWriter pedBW = new BufferedWriter(pedFW);

		// Iterate through all samples
		int sampleNb = 0;
		for (String sampleId : rdSampleSetMap.keySet()) {
			SampleInfo sampleInfo = Utils.getCurrentSampleFormattedInfo(sampleId, rdMatrixMetadata.getStudyId());
			String sexStr = "0";
			String familyId = sampleInfo.getFamilyId();
			String fatherId = sampleInfo.getFatherId();
			String motherId = sampleInfo.getMotherId();
			SampleInfo.Sex sex = sampleInfo.getSex();
			if (sex == SampleInfo.Sex.MALE) {
				sexStr = "M";
			} else if (sex == SampleInfo.Sex.FEMALE) {
				sexStr = "F";
			}

			// Iterate through current chrl markers
			rdMarkerSet.initMarkerIdSetMap(startPos, endPos);
			rdMarkerSet.fillGTsForCurrentSampleIntoInitMap(sampleNb);
			StringBuilder genotypes = new StringBuilder();
			int markerNb = 0;
			for (Object value : rdMarkerSet.getMarkerIdSetMap().values()) {
				byte[] tempGT = (byte[]) value;
				genotypes.append(SEP);
				genotypes.append(new String(new byte[]{tempGT[0]}));
				genotypes.append(SEP);
				genotypes.append(new String(new byte[]{tempGT[1]}));
				markerNb++;
			}

			// Family ID
			// Individual ID
			// Paternal ID
			// Maternal ID
			// Sex (1=male; 2=female; other=unknown)
			// Genotypes

			StringBuilder line = new StringBuilder();
			line.append(familyId);
			line.append(SEP);
			line.append(sampleId);
			line.append(SEP);
			line.append(fatherId);
			line.append(SEP);
			line.append(motherId);
			line.append(SEP);
			line.append(sexStr);
			line.append(genotypes);

			pedBW.append(line);
			pedBW.append("\n");
			pedBW.flush();

			sampleNb++;
		}
		log.info("Samples exported to chr{} MPED file: {}", chr, sampleNb);
		pedBW.close();
		pedFW.close();
	}

	public void exportChromosomeToDat(File exportDir, MatrixMetadata rdMatrixMetadata, MarkerSet_opt rdMarkerSet, String chr, int startPos, int endPos) throws IOException {

		FileWriter datFW = new FileWriter(exportDir.getPath() + "/" + rdMatrixMetadata.getMatrixFriendlyName() + "_chr" + chr + ".dat");
		BufferedWriter datBW = new BufferedWriter(datFW);

		//DAT files
		//     "M" indicates a marker
		//     rs# or marker identifier

		//MARKERSET RSID
		rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);

		//Iterate through current chr markers
		//INIT MARKERSET
		rdMarkerSet.initMarkerIdSetMap(startPos, endPos);
		int markerNb = 0;
		for (Map.Entry<String, Object> entry : rdMarkerSet.getMarkerIdSetMap().entrySet()) {
			//CHECK IF rsID available
			String markerId = entry.getKey();
			String value = entry.getValue().toString();
			if (!value.isEmpty()) {
				markerId = value;
			}

			datBW.append("M" + SEP);
			datBW.append(markerId);
			datBW.append("\n");

			markerNb++;
		}

		log.info("Markers exported to chr{} DAT file: {}", chr, (endPos + 1 - startPos));

		datBW.close();
		datFW.close();
	}
}
