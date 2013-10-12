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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
import org.gwaspi.netCDF.operations.OperationFactory;
import org.gwaspi.operations.qamarkers.QAMarkersOperationDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

			// get MARKER_QA Operation
			List<OperationMetadata> operations = OperationsList.getOperationsList(MatrixKey.valueOf(rdMatrixMetadata));
			OperationKey markersQAopKey = null;
			for (int i = 0; i < operations.size(); i++) {
				OperationMetadata op = operations.get(i);
				if (op.getType().equals(OPType.MARKER_QA)) {
					markersQAopKey = OperationKey.valueOf(op);
				}
			}

			Map<MarkerKey, Byte> opQaMarkersAllelesMaj = null;
			Map<MarkerKey, Byte> opQaMarkersAllelesMin = null;
			if (markersQAopKey != null) {
				QAMarkersOperationDataSet qaMarkersOpDS = (QAMarkersOperationDataSet) OperationFactory.generateOperationDataSet(markersQAopKey);
				Map<Integer, MarkerKey> markers = qaMarkersOpDS.getMarkers();
				Collection<Byte> knownMajorAllele = qaMarkersOpDS.getKnownMajorAllele(-1, -1);
				Collection<Byte> knownMinorAllele = qaMarkersOpDS.getKnownMinorAllele(-1, -1);

				opQaMarkersAllelesMaj = new LinkedHashMap<MarkerKey, Byte>();
				opQaMarkersAllelesMin = new LinkedHashMap<MarkerKey, Byte>();
				Iterator<Byte> knownMajorAlleleIt = knownMajorAllele.iterator();
				Iterator<Byte> knownMinorAlleleIt = knownMinorAllele.iterator();
				for (MarkerKey markerKey : markers.values()) {
					opQaMarkersAllelesMaj.put(markerKey, knownMajorAlleleIt.next());
					opQaMarkersAllelesMin.put(markerKey, knownMinorAlleleIt.next());
				}
			}

			// MARKER files
			//     rs# or snp identifier
			//     Base-pair position (bp units)
			//     Allele 1
			//     Allele 2

			Iterator<MarkerKey> markersKeysIt = dataSetSource.getMarkersKeysSource().iterator();
			for (MarkerMetadata curMarkerMetadata : dataSetSource.getMarkersMetadatasSource()) {
				markerBW.write(curMarkerMetadata.getRsId());
				markerBW.write(sep);
				markerBW.write(Integer.toString(curMarkerMetadata.getPos())); // NOTE This conversion is required, because Writer#write(int) actually writes a char, not the int value.

				if (markersQAopKey != null) {
					MarkerKey markerKey = markersKeysIt.next();
					markerBW.write(sep);
					markerBW.write((char) (byte) opQaMarkersAllelesMaj.get(markerKey));
					markerBW.write(sep);
					markerBW.write((char) (byte) opQaMarkersAllelesMin.get(markerKey));
				}

				markerBW.write('\n');
			}

			log.info("Markers exported to MARKER file: {}", dataSetSource.getMarkersMetadatasSource().size());

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
