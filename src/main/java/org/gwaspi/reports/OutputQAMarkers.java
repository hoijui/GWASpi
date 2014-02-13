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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cExport;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.global.Extractor;
import org.gwaspi.global.Utils;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MarkersMetadataSource;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.Report;
import org.gwaspi.model.ReportsList;
import org.gwaspi.model.Study;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.gwaspi.netCDF.operations.OperationFactory;
import org.gwaspi.operations.qamarkers.QAMarkersOperationDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutputQAMarkers {

	private static final Logger log
			= LoggerFactory.getLogger(OutputQAMarkers.class);

	private OutputQAMarkers() {
	}

	public static void writeReportsForQAMarkersData(OperationKey operationKey) throws IOException {

		OperationMetadata op = OperationsList.getOperationMetadata(operationKey);

		String prefix = ReportsList.getReportNamePrefix(op);
		String markMissOutName = prefix + "markmissing.txt";

		org.gwaspi.global.Utils.createFolder(new File(Study.constructReportsPath(op.getStudyKey())));

		createSortedMarkerMissingnessReport(operationKey, markMissOutName);
		ReportsList.insertRPMetadata(new Report(
				"Marker Missingness Table",
				markMissOutName,
				OPType.MARKER_QA,
				operationKey,
				"Marker Missingness Table",
				op.getStudyKey()));
		org.gwaspi.global.Utils.sysoutCompleted("Marker Missingness QA Report");

		String markMismatchOutName = prefix + "markmismatch.txt";
		createMarkerMismatchReport(operationKey, markMismatchOutName);
		ReportsList.insertRPMetadata(new Report(
				"Marker Mismatch State Table",
				markMismatchOutName,
				OPType.MARKER_QA,
				operationKey,
				"Marker Mismatch State Table",
				op.getStudyKey()));
		org.gwaspi.global.Utils.sysoutCompleted("Marker Mismatch QA Report");
	}

	private static void createSortedMarkerMissingnessReport(OperationKey markersQAopKey, String reportName) throws IOException {

		QAMarkersOperationDataSet qaMarkersOperationDataSet = (QAMarkersOperationDataSet) OperationFactory.generateOperationDataSet(markersQAopKey);
		Collection<Double> missingRatios = qaMarkersOperationDataSet.getMissingRatio();
		Map<Integer, Double> unsortedOrigIndexMissingRatios = new LinkedHashMap<Integer, Double>(missingRatios.size());
		Iterator<Double> missingRatiosIt = missingRatios.iterator();
		for (int origIndex = 0; origIndex < missingRatios.size(); origIndex++) {
			unsortedOrigIndexMissingRatios.put(origIndex, missingRatiosIt.next());
		}
		Map<Integer, Double> sortedOrigIndexMissingRatios = org.gwaspi.global.Utils.createMapSortedByValueDescending(unsortedOrigIndexMissingRatios);
		unsortedOrigIndexMissingRatios.clear(); // "garbage collection"


//		Map<MarkerKey, Double> unsortedMarkerMissingRatios = GatherQAMarkersData.loadMarkerQAMissingRatio(operationKey);
//		Map<MarkerKey, Double> sortedMarkerKeyMissingRatio = org.gwaspi.global.Utils.createMapSortedByValueDescending(unsortedMarkerMissingRatios);
//		unsortedMarkerMissingRatios.clear(); // "garbage collection"

		// FILTER THE SORTED MAP
		Iterator<Map.Entry<Integer, Double>> sortedOrigIndexMissingRatioIt = sortedOrigIndexMissingRatios.entrySet().iterator();
		while (sortedOrigIndexMissingRatioIt.hasNext()) {
			if (sortedOrigIndexMissingRatioIt.next().getValue() <= 0.0) {
				sortedOrigIndexMissingRatioIt.remove();
			}
		}
		Collection<Integer> sortedMarkerOrigIndices = sortedOrigIndexMissingRatios.keySet();

		DataSetSource matrixDataSetSource = MatrixFactory.generateMatrixDataSetSource(markersQAopKey.getParentMatrixKey());

		String sep = cExport.separator_REPORTS;
		OperationMetadata rdOPMetadata = OperationsList.getOperationMetadata(markersQAopKey);
//		MarkerSet rdInfoMarkerSet = new MarkerSet(operationKey.getParentMatrixKey());
//		rdInfoMarkerSet.initFullMarkerIdSetMap();
		MarkersMetadataSource markersMetadatas = matrixDataSetSource.getMarkersMetadatasSource();
		List<MarkerMetadata> orderedMarkersMetadatas = Utils.createIndicesOrderedList(sortedMarkerOrigIndices, markersMetadatas);

		// WRITE HEADER OF FILE
		String header = "MarkerID\trsID\tChr\tPosition\tMin. Allele\tMaj. Allele\tMissing Ratio\n";
		String reportPath = Study.constructReportsPath(rdOPMetadata.getStudyKey());

		// WRITE MARKERS ID & RSID
//		rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
//		Map<MarkerKey, char[]> sortedMarkerRSIDs = org.gwaspi.global.Utils.createOrderedMap(sortedMarkerKeys, rdInfoMarkerSet.getMarkerIdSetMapCharArray());
//		ReportWriter.writeFirstColumnToReport(reportPath, reportName, header, sortedMarkerRSIDs, true);
		ReportWriter.writeFirstColumnToReport(reportPath, reportName, header, orderedMarkersMetadatas, null, MarkerMetadata.TO_MARKER_ID);
		ReportWriter.appendColumnToReport(reportPath, reportName, orderedMarkersMetadatas, null, MarkerMetadata.TO_RS_ID);

		// WRITE MARKERSET CHROMOSOME
//		rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
//		Map<MarkerKey, char[]> sortedMarkerCHRs = org.gwaspi.global.Utils.createOrderedMap(sortedMarkerKeys, rdInfoMarkerSet.getMarkerIdSetMapCharArray());
//		ReportWriter.appendColumnToReport(reportPath, reportName, sortedMarkerCHRs, false, false);
		ReportWriter.appendColumnToReport(reportPath, reportName, orderedMarkersMetadatas, null, MarkerMetadata.TO_CHR);

		// WRITE MARKERSET POS
//		rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
//		Map<MarkerKey, Integer> sortedMarkerPos = org.gwaspi.global.Utils.createOrderedMap(sortedMarkerKeys, rdInfoMarkerSet.getMarkerIdSetMapInteger());
//		ReportWriter.appendColumnToReport(reportPath, reportName, sortedMarkerPos, false, false);
		ReportWriter.appendColumnToReport(reportPath, reportName, orderedMarkersMetadatas, null, new Extractor.ToStringMetaExtractor(MarkerMetadata.TO_POS));

		// WRITE KNOWN ALLELES FROM QA
		// get MARKER_QA Operation
//		List<OperationMetadata> operations = OperationsList.getOperationsList(rdOPMetadata.getParentMatrixKey());
//		OperationKey markersQAopKey = null;
//		for (int i = 0; i < operations.size(); i++) {
//			OperationMetadata op = operations.get(i);
//			if (op.getType().equals(OPType.MARKER_QA)) {
//				markersQAopKey = OperationKey.valueOf(op);
//			}
//		}
//		Map<MarkerKey, String> sortedMarkerAlleles = new LinkedHashMap<MarkerKey, String>(sortedMarkerOrigIndices.size());
//		if (markersQAopKey != null) {
//			OperationMetadata qaMetadata = OperationsList.getOperation(markersQAopKey);
//			NetcdfFile qaNcFile = NetcdfFile.open(qaMetadata.getPathToMatrix());
//
//			MarkerOperationSet rdOperationSet = new MarkerOperationSet(markersQAopKey);
//			Map<MarkerKey, byte[]> opMarkerSetMap = rdOperationSet.getOpSetMap();
//
//			// MINOR ALLELE
//			opMarkerSetMap = rdOperationSet.fillOpSetMapWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MINALLELES);
//			for (MarkerKey key : rdInfoMarkerSet.getMarkerKeys()) {
//				byte[] minorAllele = opMarkerSetMap.get(key);
//				sortedMarkerAlleles.put(key, new String(minorAllele));
//			}
//
//			// MAJOR ALLELE
//			AbstractOperationSet.fillMapWithDefaultValue(opMarkerSetMap, new byte[0]);
//			opMarkerSetMap = rdOperationSet.fillOpSetMapWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES);
//			for (Map.Entry<MarkerKey, String> entry : sortedMarkerAlleles.entrySet()) {
//				String minorAllele = entry.getValue();
//				entry.setValue(minorAllele + sep + new String(opMarkerSetMap.get(entry.getKey())));
//			}
//		}
//		sortedMarkerAlleles = org.gwaspi.global.Utils.createOrderedMap(sortedMarkerKeys, sortedMarkerAlleles); // XXX probably not required?
//		ReportWriter.appendColumnToReport(reportPath, reportName, sortedMarkerAlleles, false, false);
		List<Byte> knownMinorAlleles = (List) qaMarkersOperationDataSet.getKnownMinorAllele(-1, -1); // HACK might not be a List!
		List<Byte> knownMajorAlleles = (List) qaMarkersOperationDataSet.getKnownMajorAllele(-1, -1); // HACK might not be a List!
		List<String> sortedMarkerAlleles = new ArrayList<String>(sortedMarkerOrigIndices.size());
		for (Integer origIndices : sortedMarkerOrigIndices) {
			final char knownMinorAllele = (char) (byte) knownMinorAlleles.get(origIndices);
			final char knownMajorAllele = (char) (byte) knownMajorAlleles.get(origIndices);
			String concatenatedValue = knownMinorAllele + sep + knownMajorAllele;
			sortedMarkerAlleles.add(concatenatedValue);
		}
		ReportWriter.appendColumnToReport(reportPath, reportName, sortedMarkerAlleles, null, new Extractor.ToStringExtractor());

		// WRITE QA MISSINGNESS RATIO
		ReportWriter.appendColumnToReport(reportPath, reportName, sortedOrigIndexMissingRatios, false, false);
	}

	private static void createMarkerMismatchReport(OperationKey markersQAopKey, String reportName) throws IOException {

		QAMarkersOperationDataSet qaMarkersOperationDataSet = (QAMarkersOperationDataSet) OperationFactory.generateOperationDataSet(markersQAopKey);
		Collection<Boolean> mismatchStates = qaMarkersOperationDataSet.getMismatchStates();
		Map<Integer, Boolean> unsortedOrigIndexMismatchStates = new LinkedHashMap<Integer, Boolean>(mismatchStates.size());
		Iterator<Boolean> mismatchStatesIt = mismatchStates.iterator();
		for (int origIndex = 0; origIndex < mismatchStates.size(); origIndex++) {
			unsortedOrigIndexMismatchStates.put(origIndex, mismatchStatesIt.next());
		}

		// FILTER THE UNSORTED MAP
		Iterator<Map.Entry<Integer, Boolean>> unsortedOrigIndexMismatchStatesIt = unsortedOrigIndexMismatchStates.entrySet().iterator();
		while (unsortedOrigIndexMismatchStatesIt.hasNext()) {
			Boolean mismatching = unsortedOrigIndexMismatchStatesIt.next().getValue();
			if (!mismatching) {
				unsortedOrigIndexMismatchStatesIt.remove();
			}
		}
		Collection<Integer> unsortedOrigIndices = unsortedOrigIndexMismatchStates.keySet();

		DataSetSource matrixDataSetSource = MatrixFactory.generateMatrixDataSetSource(markersQAopKey.getParentMatrixKey());

		String sep = cExport.separator_REPORTS;
		OperationMetadata rdOPMetadata = OperationsList.getOperationMetadata(markersQAopKey);
		MarkersMetadataSource markersMetadatas = matrixDataSetSource.getMarkersMetadatasSource();
		List<MarkerMetadata> orderedMarkersMetadatas = Utils.createIndicesOrderedList(unsortedOrigIndices, markersMetadatas);

		// WRITE HEADER OF FILE
		String header = "MarkerID\trsID\tChr\tPosition\tMin. Allele\tMaj. Allele\tMismatching\n";
		String reportPath = Study.constructReportsPath(rdOPMetadata.getStudyKey());

		// WRITE MARKERSET RSID
		ReportWriter.writeFirstColumnToReport(reportPath, reportName, header, orderedMarkersMetadatas, null, MarkerMetadata.TO_RS_ID);

		// WRITE MARKERSET CHROMOSOME
		ReportWriter.appendColumnToReport(reportPath, reportName, orderedMarkersMetadatas, null, MarkerMetadata.TO_CHR);

		// WRITE MARKERSET POS
		ReportWriter.appendColumnToReport(reportPath, reportName, orderedMarkersMetadatas, null, new Extractor.ToStringMetaExtractor(MarkerMetadata.TO_POS));

		// WRITE KNOWN ALLELES FROM QA
		List<Byte> knownMinorAlleles = (List) qaMarkersOperationDataSet.getKnownMinorAllele(-1, -1); // HACK might not be a List!
		List<Byte> knownMajorAlleles = (List) qaMarkersOperationDataSet.getKnownMajorAllele(-1, -1); // HACK might not be a List!
		List<String> sortedMarkerAlleles = new ArrayList<String>(unsortedOrigIndices.size());
		for (Integer origIndices : unsortedOrigIndices) {
			final char knownMinorAllele = (char) (byte) knownMinorAlleles.get(origIndices);
			final char knownMajorAllele = (char) (byte) knownMajorAlleles.get(origIndices);
			String concatenatedValue = knownMinorAllele + sep + knownMajorAllele;
			sortedMarkerAlleles.add(concatenatedValue);
		}
		ReportWriter.appendColumnToReport(reportPath, reportName, sortedMarkerAlleles, null, new Extractor.ToStringExtractor());

		// WRITE QA MISMATCH STATE
		ReportWriter.appendColumnToReport(reportPath, reportName, unsortedOrigIndexMismatchStates, false, false);
	}
}
