package org.gwaspi.netCDF.exporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cExport;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.netCDF.markers.MarkerSet_opt;
import org.gwaspi.netCDF.operations.OperationSet;
import org.gwaspi.samples.SampleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.NetcdfFile;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
class BeagleFormatter implements Formatter {

	private final Logger log = LoggerFactory.getLogger(BeagleFormatter.class);

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
		String sep = cExport.separator_BEAGLE;
		NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());

		try {
			FileWriter beagleFW = new FileWriter(exportDir.getPath() + "/" + rdMatrixMetadata.getMatrixFriendlyName() + ".beagle");
			BufferedWriter beagleBW = new BufferedWriter(beagleFW);

			//BEAGLE files:
			// BEAGLE files are marker major, one line per marker, 2 columns per sample
			// # started lines contain comments and are ignored by Beagle
			// A started lines contain affection info (2 columns by sample)m 1=unaffected, 2=affected, 0=unknown
			// M started lines contain the marker genotypes
			//
			// This formatter will create the following lines:
			// # sampleID 1001 1001....
			// # sex 0=NA, 1=Male, 2=Female
			// # category [free format, no spaces]
			// # population [free format, no spaces]
			// # desease [free format, no spaces]
			// # age [integer]
			// A affection (0=NA, 1=unaffected, 2=affected)
			// M markerId & genotypes

			//<editor-fold defaultstate="collapsed" desc="BEAGLE GENOTYPE FILE">

			//<editor-fold defaultstate="collapsed" desc="HEADER">
			StringBuilder sampleLine = new StringBuilder("I" + sep + "Id");
			StringBuilder sexLine = new StringBuilder("#" + sep + "sex");
			StringBuilder categoryLine = new StringBuilder("#" + sep + "category");
			StringBuilder popLine = new StringBuilder("#" + sep + "population");
			//StringBuilder deseaseLine = new StringBuilder("#"+sep+"desease");
			StringBuilder ageLine = new StringBuilder("#" + sep + "age");
			StringBuilder affectionLine = new StringBuilder("A" + sep + "affection");

			for (String sampleId : rdSampleSetMap.keySet()) {
				SampleInfo sampleInfo = Utils.getCurrentSampleFormattedInfo(sampleId, rdMatrixMetadata.getStudyId());

				sampleLine.append(sep);
				sampleLine.append(sampleInfo.getSampleId());
				sampleLine.append(sep);
				sampleLine.append(sampleInfo.getSampleId());

				sexLine.append(sep);
				sexLine.append(sampleInfo.getSexStr());
				sexLine.append(sep);
				sexLine.append(sampleInfo.getSexStr());

				categoryLine.append(sep);
				categoryLine.append(sampleInfo.getCategory());
				categoryLine.append(sep);
				categoryLine.append(sampleInfo.getCategory());

				popLine.append(sep);
				popLine.append(sampleInfo.getPopulation());
				popLine.append(sep);
				popLine.append(sampleInfo.getPopulation());

//				diseaseLine.append(sep);
//				diseaseLine.append(sampleInfo.getDisease());
//				diseaseLine.append(sep);
//				diseaseLine.append(sampleInfo.getDisease());

				ageLine.append(sep);
				ageLine.append(sampleInfo.getAge());
				ageLine.append(sep);
				ageLine.append(sampleInfo.getAge());

				affectionLine.append(sep);
				affectionLine.append(sampleInfo.getAffectionStr());
				affectionLine.append(sep);
				affectionLine.append(sampleInfo.getAffectionStr());
			}
			beagleBW.append(sampleLine);
			beagleBW.append("\n");
			beagleBW.append(sexLine);
			beagleBW.append("\n");
			beagleBW.append(categoryLine);
			beagleBW.append("\n");
			beagleBW.append(popLine);
			beagleBW.append("\n");
//			beagleBW.append(diseaseLine);
//			beagleBW.append("\n");
			beagleBW.append(ageLine);
			beagleBW.append("\n");
			beagleBW.append(affectionLine);
			beagleBW.append("\n");
			//</editor-fold>

			//<editor-fold defaultstate="collapsed" desc="GENOTYPES">
			rdMarkerSet.initFullMarkerIdSetMap();

			//Iterate through markerset
			int markerNb = 0;
			for (String markerId : rdMarkerSet.getMarkerIdSetMap().keySet()) {
				StringBuilder markerLine = new StringBuilder("M" + sep + markerId.toString());

				// Iterate through sampleset
				StringBuilder currMarkerGTs = new StringBuilder();
				Map<String, Object> remainingSampleSet = rdSampleSet.readAllSamplesGTsFromCurrentMarkerToMap(rdNcFile, rdSampleSetMap, markerNb);
				for (Object value : remainingSampleSet.values()) {
					byte[] tempGT = (byte[]) value;
					currMarkerGTs.append(sep);
					currMarkerGTs.append(new String(new byte[]{tempGT[0]}));
					currMarkerGTs.append(sep);
					currMarkerGTs.append(new String(new byte[]{tempGT[1]}));
				}

				markerLine.append(currMarkerGTs);

				beagleBW.append(markerLine);
				beagleBW.append("\n");
				markerNb++;
				if (markerNb % 100 == 0) {
					log.info("Markers exported to Beagle file: {}", markerNb);
				}
			}
			log.info("Markers exported to Beagle file: {}", markerNb);
			//</editor-fold>
			//</editor-fold>

			//<editor-fold defaultstate="collapsed" desc="BEAGLE MARKER FILE">
			FileWriter markerFW = new FileWriter(exportDir.getPath() + "/" + rdMatrixMetadata.getMatrixFriendlyName() + ".markers");
			BufferedWriter markerBW = new BufferedWriter(markerFW);

			// MARKER files
			//     rs# or snp identifier
			//     Base-pair position (bp units)
			//     Allele 1
			//     Allele 2

			// PURGE MARKERSET
			rdMarkerSet.fillWith("");

			// MARKERSET RSID
			rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);

			// MARKERSET POSITION
			rdMarkerSet.appendVariableToMarkerSetMapValue(cNetCDF.Variables.VAR_MARKERS_POS, sep);

			// WRITE KNOWN ALLELES FROM QA
			// get MARKER_QA Operation
			List<Object[]> operationsAL = OperationsList.getMatrixOperations(rdMatrixMetadata.getMatrixId());
			int markersQAopId = Integer.MIN_VALUE;
			for (int i = 0; i < operationsAL.size(); i++) {
				Object[] element = operationsAL.get(i);
				if (element[1].toString().equals(OPType.MARKER_QA.toString())) {
					markersQAopId = (Integer) element[0];
				}
			}
			if (markersQAopId != Integer.MIN_VALUE) {
				OperationMetadata qaMetadata = OperationsList.getOperationMetadata(markersQAopId);
				NetcdfFile qaNcFile = NetcdfFile.open(qaMetadata.getPathToMatrix());

				OperationSet rdOperationSet = new OperationSet(rdMatrixMetadata.getStudyId(), markersQAopId);
				Map<String, Object> opMarkerSetMap = rdOperationSet.getOpSetMap();

				// MAJOR ALLELE
				opMarkerSetMap = rdOperationSet.fillOpSetMapWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES);
				for (Map.Entry<String, Object> entry : rdMarkerSet.getMarkerIdSetMap().entrySet()) {
					String key = entry.getKey();
					Object allele1Value = opMarkerSetMap.get(key);
					Object infoValue = entry.getValue();

					StringBuilder sb = new StringBuilder();
					sb.append(infoValue); // previously stored info
					sb.append(sep);
					sb.append(allele1Value);

					rdMarkerSet.getMarkerIdSetMap().put(key, sb.toString());
				}

				// MINOR ALLELE
				rdOperationSet.fillMapWithDefaultValue(opMarkerSetMap, "");
				opMarkerSetMap = rdOperationSet.fillOpSetMapWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MINALLELES);
				for (Map.Entry<String, Object> entry : rdMarkerSet.getMarkerIdSetMap().entrySet()) {
					String key = entry.getKey();
					Object allele2Value = opMarkerSetMap.get(key);
					Object infoValue = entry.getValue();

					StringBuilder sb = new StringBuilder();
					sb.append(infoValue); // previously stored info
					sb.append(sep);
					sb.append(allele2Value);

					rdMarkerSet.getMarkerIdSetMap().put(key, sb.toString());
				}
			}

			// WRITE ALL Map TO BUFFERWRITER
			markerNb = 0;
			for (Object value : rdMarkerSet.getMarkerIdSetMap().values()) {
				markerBW.append(value.toString());
				markerBW.append("\n");
				markerNb++;
			}

			log.info("Markers exported to MARKER file: {}", markerNb);

			markerBW.close();
			markerFW.close();
			//</editor-fold>

			log.info("Markers exported to BEAGLE: {}", markerNb);

			beagleBW.close();
			beagleFW.close();

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
