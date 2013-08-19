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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cExport;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.operations.AbstractOperationSet;
import org.gwaspi.netCDF.operations.MarkerOperationSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.NetcdfFile;

class BeagleFormatter implements Formatter {

	private final Logger log = LoggerFactory.getLogger(BeagleFormatter.class);

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
		String sep = cExport.separator_BEAGLE;

		BufferedWriter beagleBW = null;
		try {
			FileWriter beagleFW = new FileWriter(new File(exportDir.getPath(),
					rdMatrixMetadata.getMatrixFriendlyName() + ".beagle"));
			beagleBW = new BufferedWriter(beagleFW);

			// BEAGLE files:
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

			//<editor-fold defaultstate="expanded" desc="BEAGLE GENOTYPE FILE">

			//<editor-fold defaultstate="expanded" desc="HEADER">
			StringBuilder sampleLine = new StringBuilder("I" + sep + "Id");
			StringBuilder sexLine = new StringBuilder("#" + sep + "sex");
			StringBuilder categoryLine = new StringBuilder("#" + sep + "category");
			StringBuilder popLine = new StringBuilder("#" + sep + "population");
			//StringBuilder deseaseLine = new StringBuilder("#"+sep+"desease");
			StringBuilder ageLine = new StringBuilder("#" + sep + "age");
			StringBuilder affectionLine = new StringBuilder("A" + sep + "affection");

			for (SampleKey sampleKey : dataSetSource.getSamplesKeysSource()) {
				SampleInfo sampleInfo = Utils.getCurrentSampleFormattedInfo(sampleKey);

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

			//<editor-fold defaultstate="expanded" desc="GENOTYPES">
			// Iterate through markers
			int markerNb = 0;
			Iterator<GenotypesList> markersGenotypesIt = dataSetSource.getMarkersGenotypesSource().iterator();
			for (MarkerKey markerKey : dataSetSource.getMarkersKeysSource()) {
				beagleBW.append("M" + sep + markerKey.toString());

				// Iterate through sampleset
				for (byte[] tempGT : markersGenotypesIt.next()) {
					beagleBW.append(sep);
					beagleBW.append((char) tempGT[0]);
					beagleBW.append(sep);
					beagleBW.append((char) tempGT[1]);
				}

				beagleBW.append("\n");
				markerNb++;
				if (markerNb % 100 == 0) {
					log.info("Markers exported to Beagle file: {}", markerNb);
				}
			}
			log.info("Markers exported to Beagle file: {}", markerNb);
			//</editor-fold>
		} finally {
			if (beagleBW != null) {
				beagleBW.close();
			}
		}
		//</editor-fold>

		//<editor-fold defaultstate="expanded" desc="BEAGLE MARKER FILE">
		BufferedWriter markerBW = null;
		try {
			FileWriter markerFW = new FileWriter(new File(exportDir.getPath(),
					rdMatrixMetadata.getMatrixFriendlyName() + ".markers"));
			markerBW = new BufferedWriter(markerFW);

			// MARKER files
			//     rs# or snp identifier
			//     Base-pair position (bp units)
			//     Allele 1
			//     Allele 2

			// PURGE MARKERSET
			rdMarkerSet.fillWith(new char[0]);

			// MARKERSET RSID
			rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);

			// MARKERSET POSITION
			rdMarkerSet.appendVariableToMarkerSetMapValue(cNetCDF.Variables.VAR_MARKERS_POS, sep);

			// WRITE KNOWN ALLELES FROM QA
			// get MARKER_QA Operation
			List<OperationMetadata> operations = OperationsList.getOperationsList(MatrixKey.valueOf(rdMatrixMetadata));
			OperationKey markersQAopKey = null;
			for (int i = 0; i < operations.size(); i++) {
				OperationMetadata op = operations.get(i);
				if (op.getType().equals(OPType.MARKER_QA)) {
					markersQAopKey = OperationKey.valueOf(op);
				}
			}
			if (markersQAopKey != null) {
				OperationMetadata qaMetadata = OperationsList.getOperation(markersQAopKey);
				NetcdfFile qaNcFile = NetcdfFile.open(qaMetadata.getPathToMatrix());

				MarkerOperationSet<char[]> rdOperationSet = new MarkerOperationSet<char[]>(markersQAopKey);
				Map<MarkerKey, char[]> opMarkerSetMap = rdOperationSet.getOpSetMap();

				// MAJOR ALLELE
				opMarkerSetMap = rdOperationSet.fillOpSetMapWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES);
				for (Map.Entry<MarkerKey, char[]> entry : rdMarkerSet.getMarkerIdSetMapCharArray().entrySet()) {
					MarkerKey key = entry.getKey();
					char[] allele1Value = opMarkerSetMap.get(key);
					char[] infoValue = entry.getValue();

					StringBuilder sb = new StringBuilder();
					sb.append(infoValue); // previously stored info
					sb.append(sep);
					sb.append(allele1Value);

					entry.setValue(sb.toString().toCharArray());
				}

				// MINOR ALLELE
				AbstractOperationSet.fillMapWithDefaultValue(opMarkerSetMap, new char[0]);
				opMarkerSetMap = rdOperationSet.fillOpSetMapWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MINALLELES);
				for (Map.Entry<MarkerKey, char[]> entry : rdMarkerSet.getMarkerIdSetMapCharArray().entrySet()) {
					MarkerKey key = entry.getKey();
					char[] allele2Value = opMarkerSetMap.get(key);
					char[] infoValue = entry.getValue();

					StringBuilder sb = new StringBuilder();
					sb.append(infoValue); // previously stored info
					sb.append(sep);
					sb.append(allele2Value);

					entry.setValue(sb.toString().toCharArray());
				}
			}

			// WRITE ALL Map TO BUFFERWRITER
			markerNb = 0;
			for (char[] value : rdMarkerSet.getMarkerIdSetMapCharArray().values()) {
				markerBW.append(new String(value));
				markerBW.append("\n");
				markerNb++;
			}

			log.info("Markers exported to MARKER file: {}", markerNb);

			result = true;
		} finally {
			if (markerBW != null) {
				markerBW.close();
			}
		}
		//</editor-fold>

		return result;
	}
}
