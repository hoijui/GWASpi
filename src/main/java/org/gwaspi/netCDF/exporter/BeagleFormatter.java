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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.ExportConstants;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.DataSetMetadata;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.operations.OperationManager;
import org.gwaspi.operations.qamarkers.QAMarkersOperationDataSet;
import org.gwaspi.progress.IntegerProgressHandler;
import org.gwaspi.progress.ProcessInfo;
import org.gwaspi.progress.ProcessStatus;
import org.gwaspi.progress.SubProcessInfo;
import org.gwaspi.progress.SuperProgressSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BeagleFormatter implements Formatter {

	private final Logger log = LoggerFactory.getLogger(BeagleFormatter.class);

	@Override
	public void export(
			String exportPath,
			DataSetMetadata rdDataSetMetadata,
			DataSetSource dataSetSource,
			final SuperProgressSource superProgressSource,
			String phenotype)
			throws IOException
	{
		final ProcessInfo exportPI = new SubProcessInfo(
				superProgressSource.getInfo(),
				"export",
				"export in the GWASpi internal format");
		final ProcessInfo exportSamplesPI = new SubProcessInfo(
				exportPI,
				"export samples",
				"format and export sample infos");
		final ProcessInfo exportGenotypesPI = new SubProcessInfo(
				exportPI,
				"export genotypes",
				"format and export genotypes");
		final ProcessInfo exportMarkersPI = new SubProcessInfo(
				exportPI,
				"export markers",
				"export marker infos");

		final SuperProgressSource exportPS = new SuperProgressSource(exportPI);
		superProgressSource.replaceSubProgressSource(PLACEHOLDER_PS_EXPORT, exportPS, null);
		exportPS.setNewStatus(ProcessStatus.INITIALIZING);

		final IntegerProgressHandler exportSamplesPS = new IntegerProgressHandler(exportSamplesPI, 0, dataSetSource.getNumSamples() - 1);
		exportPS.addSubProgressSource(exportSamplesPS, 0.02);

		final IntegerProgressHandler exportGenotypesPS = new IntegerProgressHandler(exportGenotypesPI, 0, dataSetSource.getNumMarkers()- 1);
		exportPS.addSubProgressSource(exportGenotypesPS, 0.9);

		final IntegerProgressHandler exportMarkersPS = new IntegerProgressHandler(exportMarkersPI, 0, dataSetSource.getNumMarkers()- 1);
		exportPS.addSubProgressSource(exportMarkersPS, 0.08);

		final File exportDir = Utils.checkDirPath(exportPath);

		String sep = ExportConstants.separator_BEAGLE;

		exportSamplesPS.setNewStatus(ProcessStatus.INITIALIZING);
		BufferedWriter beagleBW = null;
		try {
			FileWriter beagleFW = new FileWriter(new File(exportDir.getPath(),
					rdDataSetMetadata.getFriendlyName() + ".beagle"));
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

			int sampleIndex = 0;
			exportSamplesPS.setNewStatus(ProcessStatus.RUNNING);
			for (SampleInfo sampleInfo : dataSetSource.getSamplesInfosSource()) {
				sampleInfo = org.gwaspi.netCDF.exporter.Utils.formatSampleInfo(sampleInfo);

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
				exportSamplesPS.setProgress(sampleIndex);
				sampleIndex++;
			}
			exportSamplesPS.setNewStatus(ProcessStatus.FINALIZING);
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
			beagleBW.flush();
			exportSamplesPS.setNewStatus(ProcessStatus.COMPLEETED);
			//</editor-fold>

			//<editor-fold defaultstate="expanded" desc="GENOTYPES">
			// Iterate through markers
			exportGenotypesPS.setNewStatus(ProcessStatus.INITIALIZING);
			int markerNb = 0;
			Iterator<GenotypesList> markersGenotypesIt = dataSetSource.getMarkersGenotypesSource().iterator();
			exportGenotypesPS.setNewStatus(ProcessStatus.INITIALIZING);
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
				exportGenotypesPS.setProgress(markerNb);
				markerNb++;
			}
			exportGenotypesPS.setNewStatus(ProcessStatus.FINALIZING);
			log.info("Markers exported to Beagle file: {}", markerNb);
			//</editor-fold>
		} finally {
			if (beagleBW != null) {
				beagleBW.close();
			}
		}
		exportGenotypesPS.setNewStatus(ProcessStatus.COMPLEETED);
		//</editor-fold>

		//<editor-fold defaultstate="expanded" desc="BEAGLE MARKER FILE">
		exportMarkersPS.setNewStatus(ProcessStatus.INITIALIZING);
		BufferedWriter markerBW = null;
		try {
			FileWriter markerFW = new FileWriter(new File(exportDir.getPath(),
					rdDataSetMetadata.getFriendlyName() + ".markers"));
			markerBW = new BufferedWriter(markerFW);

			// get MARKER_QA Operation
			final DataSetKey dataSetKey = rdDataSetMetadata.getDataSetKey();
			final List<OperationMetadata> operations = OperationsList.getOffspringOperationsMetadata(dataSetKey);
			final OperationKey markersQAOpKey = OperationsList.getIdOfLastOperationTypeOccurance(operations, OPType.MARKER_QA, rdDataSetMetadata.getNumMarkers());

			Map<MarkerKey, Byte> opQaMarkersAllelesMaj = null;
			Map<MarkerKey, Byte> opQaMarkersAllelesMin = null;
			if (markersQAOpKey != null) {
				QAMarkersOperationDataSet qaMarkersOpDS = (QAMarkersOperationDataSet) OperationManager.generateOperationDataSet(markersQAOpKey);
				Map<Integer, MarkerKey> markers = qaMarkersOpDS.getMarkersKeysSource().getIndicesMap();
				List<Byte> knownMajorAllele = qaMarkersOpDS.getKnownMajorAllele(-1, -1);
				List<Byte> knownMinorAllele = qaMarkersOpDS.getKnownMinorAllele(-1, -1);

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
			int markerIndex = 0;
			exportMarkersPS.setNewStatus(ProcessStatus.RUNNING);
			for (MarkerMetadata curMarkerMetadata : dataSetSource.getMarkersMetadatasSource()) {
				markerBW.write(curMarkerMetadata.getRsId());
				markerBW.write(sep);
				markerBW.write(Integer.toString(curMarkerMetadata.getPos())); // NOTE This conversion is required, because Writer#write(int) actually writes a char, not the int value.

				if (markersQAOpKey != null) {
					MarkerKey markerKey = markersKeysIt.next();
					markerBW.write(sep);
					markerBW.write((char) (byte) opQaMarkersAllelesMaj.get(markerKey));
					markerBW.write(sep);
					markerBW.write((char) (byte) opQaMarkersAllelesMin.get(markerKey));
				}

				markerBW.write('\n');
				exportMarkersPS.setProgress(markerIndex);
				markerIndex++;
			}
			exportMarkersPS.setNewStatus(ProcessStatus.FINALIZING);
			log.info("Markers exported to MARKER file: {}", markerIndex);
		} finally {
			if (markerBW != null) {
				markerBW.close();
			}
		}
		exportMarkersPS.setNewStatus(ProcessStatus.COMPLEETED);
		//</editor-fold>
		exportPS.setNewStatus(ProcessStatus.COMPLEETED);
	}
}
