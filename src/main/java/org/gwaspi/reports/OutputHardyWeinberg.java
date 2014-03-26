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

package org.gwaspi.reports;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cExport;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.global.Extractor;
import org.gwaspi.global.Text;
import org.gwaspi.global.Utils;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MarkersMetadataSource;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.Report;
import org.gwaspi.model.ReportsList;
import org.gwaspi.model.Study;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.gwaspi.operations.OperationManager;
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperationDataSet;
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperationEntry;
import org.gwaspi.operations.qamarkers.QAMarkersOperationDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutputHardyWeinberg {

	private static final Logger log
			= LoggerFactory.getLogger(OutputHardyWeinberg.class);

	private OutputHardyWeinberg() {
	}

	public static void writeReportsForMarkersHWData(OperationKey operationKey, OperationKey qaMarkersOpKey) throws IOException {

		OperationMetadata op = OperationsList.getOperationMetadata(operationKey);

		//String hwOutName = "hw_"+op.getId()+"_"+op.getFriendlyName()+".hw";
		String prefix = ReportsList.getReportNamePrefix(op);
		String hwOutName = prefix + "hardy-weinberg.txt";

		org.gwaspi.global.Utils.createFolder(new File(Study.constructReportsPath(op.getStudyKey())));

		processSortedHardyWeinbergReport(operationKey, hwOutName, qaMarkersOpKey);
		ReportsList.insertRPMetadata(new Report(
				"Hardy Weinberg Table",
				hwOutName,
				OPType.HARDY_WEINBERG,
				operationKey,
				"Hardy Weinberg Table",
				op.getStudyKey()));
		org.gwaspi.global.Utils.sysoutCompleted("Hardy-Weinberg Report");
	}

	protected static void processSortedHardyWeinbergReport(OperationKey operationKey, String reportName, OperationKey qaMarkersOpKey) throws IOException {

		HardyWeinbergOperationDataSet hardyWeinbergOperationDataSet = (HardyWeinbergOperationDataSet) OperationManager.generateOperationDataSet(operationKey);

//		Map<MarkerKey, Double> unsortedMarkerKeyHWPval = GatherHardyWeinbergData.loadHWPval_ALT(operationKey);
//		Map<MarkerKey, Double> sortedByHWPval = org.gwaspi.global.Utils.createMapSortedByValue(unsortedMarkerKeyHWPval);
//		unsortedMarkerKeyHWPval.clear(); // "garbage collection"
//		Collection<MarkerKey> sortedMarkerKeys = sortedByHWPval.keySet();
		List<HardyWeinbergOperationEntry> hardyWeinbergEntries = hardyWeinbergOperationDataSet.getEntriesAlternate();
		Collections.sort(hardyWeinbergEntries, HardyWeinbergOperationEntry.P_VALUE_COMPARATOR);
		List<Integer> sortedMarkerOrigIndices = new ArrayList<Integer>(hardyWeinbergEntries.size());
		List<MarkerKey> sortedMarkerKeys = new ArrayList<MarkerKey>(hardyWeinbergEntries.size()); // XXX create an Extractor in AbstractOperationDataSet instead
		for (HardyWeinbergOperationEntry hardyWeinbergOperationEntry : hardyWeinbergEntries) {
			sortedMarkerOrigIndices.add(hardyWeinbergOperationEntry.getIndex());
			sortedMarkerKeys.add(hardyWeinbergOperationEntry.getKey());
		}

		// GET MARKER INFO
		String sep = cExport.separator_REPORTS;
		OperationMetadata rdOPMetadata = OperationsList.getOperationMetadata(operationKey);
		DataSetSource dataSetSource = MatrixFactory.generateMatrixDataSetSource(operationKey.getParentMatrixKey());
//		MarkerSet rdInfoMarkerSet = new MarkerSet(operationKey.getParentMatrixKey());
//		rdInfoMarkerSet.initFullMarkerIdSetMap();

		// WRITE HEADER OF FILE
		String header = "MarkerID\trsID\tChr\tPosition\tMin_Allele\tMaj_Allele\t" + Text.Reports.hwPval + Text.Reports.CTRL + "\t" + Text.Reports.hwObsHetzy + Text.Reports.CTRL + "\t" + Text.Reports.hwExpHetzy + Text.Reports.CTRL + "\n";
		String reportPath = Study.constructReportsPath(rdOPMetadata.getStudyKey());

		final MarkersMetadataSource markersMetadatas = dataSetSource.getMarkersMetadatasSource();
		final List<MarkerMetadata> orderedMarkersMetadatas = Utils.createIndicesOrderedList(sortedMarkerOrigIndices, markersMetadatas);

		// WRITE MARKERS ID & RSID
		ReportWriter.writeFirstColumnToReport(reportPath, reportName, header, orderedMarkersMetadatas, null, MarkerMetadata.TO_MARKER_ID);
		ReportWriter.appendColumnToReport(reportPath, reportName, orderedMarkersMetadatas, null, MarkerMetadata.TO_RS_ID);
//		rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
//		Map<MarkerKey, char[]> sortedMarkerRSIDs = org.gwaspi.global.Utils.createOrderedMap(sortedMarkerKeys, rdInfoMarkerSet.getMarkerIdSetMapCharArray());
//		ReportWriter.writeFirstColumnToReport(reportPath, reportName, header, sortedMarkerRSIDs, true);

		// WRITE MARKERSET CHROMOSOME
		ReportWriter.appendColumnToReport(reportPath, reportName, orderedMarkersMetadatas, null, MarkerMetadata.TO_CHR);
//		rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
//		Map<MarkerKey, char[]> sortedMarkerCHRs = org.gwaspi.global.Utils.createOrderedMap(sortedMarkerKeys, rdInfoMarkerSet.getMarkerIdSetMapCharArray());
//		ReportWriter.appendColumnToReport(reportPath, reportName, sortedMarkerCHRs, false, false);

		// WRITE MARKERSET POS
		ReportWriter.appendColumnToReport(reportPath, reportName, orderedMarkersMetadatas, null, new Extractor.ToStringMetaExtractor<MarkerMetadata, Integer>(MarkerMetadata.TO_POS));
//		rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
//		Map<MarkerKey, Integer> sortedMarkerPos = org.gwaspi.global.Utils.createOrderedMap(sortedMarkerKeys, rdInfoMarkerSet.getMarkerIdSetMapInteger());
//		ReportWriter.appendColumnToReport(reportPath, reportName, sortedMarkerPos, false, false);

		// WRITE KNOWN ALLELES FROM QA
		Map<MarkerKey, String> sortedMarkerAlleles = new LinkedHashMap<MarkerKey, String>(sortedMarkerKeys.size());

		if (qaMarkersOpKey != null) {
			QAMarkersOperationDataSet qaMarkersOpDS = (QAMarkersOperationDataSet) OperationManager.generateOperationDataSet(qaMarkersOpKey);
			Map<Integer, MarkerKey> markers = qaMarkersOpDS.getMarkersKeysSource().getIndicesMap();
			Collection<Byte> knownMajorAllele = qaMarkersOpDS.getKnownMajorAllele(-1, -1);
			Collection<Byte> knownMinorAllele = qaMarkersOpDS.getKnownMinorAllele(-1, -1);

			Map<MarkerKey, Byte> opQaMarkersAllelesMaj = new LinkedHashMap<MarkerKey, Byte>();
			Map<MarkerKey, Byte> opQaMarkersAllelesMin = new LinkedHashMap<MarkerKey, Byte>();
			Iterator<Byte> knownMajorAlleleIt = knownMajorAllele.iterator();
			Iterator<Byte> knownMinorAlleleIt = knownMinorAllele.iterator();
			for (MarkerKey markerKey : markers.values()) {
				opQaMarkersAllelesMaj.put(markerKey, knownMajorAlleleIt.next());
				opQaMarkersAllelesMin.put(markerKey, knownMinorAlleleIt.next());
			}
//		}
//
//		if (markersQAopKey != null) {
//			OperationMetadata qaMetadata = OperationsList.getOperation(markersQAopKey);
//			NetcdfFile qaNcFile = NetcdfFile.open(qaMetadata.getPathToMatrix());
//
//			MarkerOperationSet rdOperationSet = new MarkerOperationSet(markersQAopKey);
//			Map<MarkerKey, byte[]> opMarkerSetMap = rdOperationSet.getOpSetMap();

			// MINOR ALLELE
//			opMarkerSetMap = rdOperationSet.fillOpSetMapWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MINALLELES);
			for (MarkerKey key : dataSetSource.getMarkersKeysSource()) {
//				byte[] minorAllele = opMarkerSetMap.get(key);
//				sortedMarkerAlleles.put(key, new String(minorAllele));
				Byte minorAllele = opQaMarkersAllelesMin.get(key);
				sortedMarkerAlleles.put(key, "" + (char) (byte) minorAllele);
			}

			// MAJOR ALLELE
//			AbstractOperationSet.fillMapWithDefaultValue(opMarkerSetMap, new byte[0]);
//			opMarkerSetMap = rdOperationSet.fillOpSetMapWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES);
			for (Map.Entry<MarkerKey, String> entry : sortedMarkerAlleles.entrySet()) {
				String minorAllele = entry.getValue();
//				entry.setValue(minorAllele + sep + new String(opMarkerSetMap.get(entry.getKey())));
				Byte majorAllele = opQaMarkersAllelesMaj.get(entry.getKey());
				String newValue = minorAllele + sep;
				if (majorAllele != null) {
					newValue += (char) (byte) majorAllele;
				}
				entry.setValue(newValue);
			}
		}
		sortedMarkerAlleles = org.gwaspi.global.Utils.createOrderedMap(sortedMarkerKeys, sortedMarkerAlleles); // XXX probably not required?
		ReportWriter.appendColumnToReport(reportPath, reportName, sortedMarkerAlleles, false, false);

		// WRITE HW PVAL
		ReportWriter.appendColumnToReport(reportPath, reportName, hardyWeinbergEntries, null,
				new Extractor.ToStringMetaExtractor(HardyWeinbergOperationEntry.P_VALUE_EXTRACTOR));
		ReportWriter.appendColumnToReport(reportPath, reportName, hardyWeinbergEntries, null,
				new Extractor.ToStringMetaExtractor(HardyWeinbergOperationEntry.HETZY_OBSERVED_EXTRACTOR));
		ReportWriter.appendColumnToReport(reportPath, reportName, hardyWeinbergEntries, null,
				new Extractor.ToStringMetaExtractor(HardyWeinbergOperationEntry.HETZY_EXPECTED_EXTRACTOR));
//		ReportWriter.appendColumnToReport(reportPath, reportName, sortedByHWPval, false, false);
//
//		// WRITE HW HETZY ARRAY
//		Map<MarkerKey, Double> markerIdHWHETZY_CTRLMap = GatherHardyWeinbergData.loadHWHETZY_ALT(operationKey);
//		Map<MarkerKey, Double> sortedHWHETZYCTRLs = org.gwaspi.global.Utils.createOrderedMap(sortedMarkerKeys, markerIdHWHETZY_CTRLMap);
//		ReportWriter.appendColumnToReport(reportPath, reportName, sortedHWHETZYCTRLs, true, false);
	}
}
