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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;
import org.gwaspi.global.Extractor;
import org.gwaspi.global.Filter;
import org.gwaspi.global.Text;
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
import org.gwaspi.operations.DefaultOperationTypeInfo;
import org.gwaspi.operations.OperationManager;
import org.gwaspi.operations.OperationTypeInfo;
import org.gwaspi.operations.qamarkers.QAMarkersOperationDataSet;
import org.gwaspi.progress.DefaultProcessInfo;
import org.gwaspi.progress.IndeterminateProgressHandler;
import org.gwaspi.progress.ProcessInfo;
import org.gwaspi.progress.ProcessStatus;
import org.gwaspi.progress.ProgressHandler;
import org.gwaspi.progress.ProgressSource;
import org.gwaspi.progress.SuperProgressSource;

/**
 * Write reports for QA Markers data.
 */
public class OutputQAMarkers extends AbstractOutputOperation<QAMarkersOutputParams> {

	private static final ProcessInfo qaMarkersOutputProcessInfo = new DefaultProcessInfo("Write QA Markers output to files", ""); // TODO

	static final OperationTypeInfo OPERATION_TYPE_INFO
			= new DefaultOperationTypeInfo(
					false,
					"Output QA Markers data",
					"Output QA Markers data", // TODO We need a more elaborate description of this operation!
					null,
					false,
					false);

	public static final String[] COLUMNS_MISSING = createColumnHeaders(true);
	public static final String[] COLUMNS_MISMATCH = createColumnHeaders(false);

	private ProgressHandler operationPH;
	private ProgressHandler creatingMissingnessTablePH;
	private ProgressHandler creatingMismatchTablePH;

	public OutputQAMarkers(QAMarkersOutputParams params) {
		super(params);
	}

	private static String[] createColumnHeaders(final boolean missingness) {

		return new String[] {
				Text.Reports.markerId,
				Text.Reports.rsId,
				Text.Reports.chr,
				Text.Reports.pos,
				Text.Reports.minAallele,
				Text.Reports.majAallele,
				missingness ? Text.Reports.missRatio : Text.Reports.mismatchState};
	}

	static String createReportHeaderLine(final String[] columnHeaders) {

		final StringBuilder headerLine = new StringBuilder();
		for (final String columnHeader : columnHeaders) {
			headerLine.append(columnHeader).append('\t');
		}
		headerLine.deleteCharAt(headerLine.length() - 1);
		headerLine.append('\n');
		return headerLine.toString();
	}

	@Override
	public OperationTypeInfo getTypeInfo() {
		// XXX For this class, we should use a different return type on this method (ialso for the othe Output* classes)
		return OPERATION_TYPE_INFO;
	}

	@Override
	public Object call() throws IOException {

		operationPH.setNewStatus(ProcessStatus.INITIALIZING);
		final OperationMetadata qaMarkersOperation = OperationsList.getOperationMetadata(getParams().getMarkersQAOpKey());

		String prefix = ReportsList.getReportNamePrefix(qaMarkersOperation);
		Utils.createFolder(new File(Study.constructReportsPath(qaMarkersOperation.getStudyKey())));

		creatingMissingnessTablePH.setNewStatus(ProcessStatus.INITIALIZING);
		String markMissOutName = prefix + "markmissing.txt";
		operationPH.setNewStatus(ProcessStatus.RUNNING);
		creatingMissingnessTablePH.setNewStatus(ProcessStatus.RUNNING);
		createSortedMarkerMissingnessReport(getParams().getMarkersQAOpKey(), markMissOutName);
		creatingMissingnessTablePH.setNewStatus(ProcessStatus.FINALIZING);
		ReportsList.insertRPMetadata(new Report(
				"Marker Missingness Table",
				markMissOutName,
				OPType.MARKER_QA,
				getParams().getMarkersQAOpKey(),
				"Marker Missingness Table",
				qaMarkersOperation.getStudyKey()));
		Utils.sysoutCompleted("Marker Missingness QA Report");
		creatingMissingnessTablePH.setNewStatus(ProcessStatus.COMPLEETED);

		creatingMismatchTablePH.setNewStatus(ProcessStatus.INITIALIZING);
		String markMismatchOutName = prefix + "markmismatch.txt";
		creatingMismatchTablePH.setNewStatus(ProcessStatus.RUNNING);
		createMarkerMismatchReport(getParams().getMarkersQAOpKey(), markMismatchOutName);
		creatingMismatchTablePH.setNewStatus(ProcessStatus.FINALIZING);
		ReportsList.insertRPMetadata(new Report(
				"Marker Mismatch State Table",
				markMismatchOutName,
				OPType.MARKER_QA,
				getParams().getMarkersQAOpKey(),
				"Marker Mismatch State Table",
				qaMarkersOperation.getStudyKey()));
		Utils.sysoutCompleted("Marker Mismatch QA Report");
		creatingMismatchTablePH.setNewStatus(ProcessStatus.COMPLEETED);
		operationPH.setNewStatus(ProcessStatus.COMPLEETED);

		return null;
	}

	@Override
	public ProcessInfo getProcessInfo() {
		return qaMarkersOutputProcessInfo;
	}

	@Override
	public ProgressSource getProgressSource() throws IOException {

		if (operationPH == null) {
			final ProcessInfo creatingMissingnessTablePI = new DefaultProcessInfo(
					"writing the QA Markers missingness table to a text file", null);
			creatingMissingnessTablePH
					= new IndeterminateProgressHandler(creatingMissingnessTablePI);

			final ProcessInfo creatingMismatchTablePI = new DefaultProcessInfo(
					"creating & writing the QA Markers mismatch table to a text file", null);
			creatingMismatchTablePH
					= new IndeterminateProgressHandler(creatingMismatchTablePI);

			Map<ProgressSource, Double> subProgressSourcesAndWeights
					= new LinkedHashMap<ProgressSource, Double>();

			subProgressSourcesAndWeights.put(creatingMissingnessTablePH, 0.5); // TODO adjust these weights!
			subProgressSourcesAndWeights.put(creatingMismatchTablePH, 0.5);

			operationPH = new SuperProgressSource(qaMarkersOutputProcessInfo, subProgressSourcesAndWeights);
		}

		return operationPH;
	}

	private static <V> Map<Integer, V> createOrigIndicesMap(final DataSetSource dataSet, final List<V> values) throws IOException {

		final Map<Integer, V> origIndicesValues = new LinkedHashMap<Integer, V>(
				values.size());
		final Iterator<V> valuesIt = values.iterator();
		for (final Integer origIndex : dataSet.getMarkersKeysSource().getIndices()) {
			origIndicesValues.put(origIndex, valuesIt.next());
		}

		return origIndicesValues;
	}

	private static <V> Map<Integer, V> createValueSortedMarkersOrigIndicesMap(final DataSetSource dataSet, final List<V> values) throws IOException {

		final Map<Integer, V> unsortedOrigIndicesValues = createOrigIndicesMap(dataSet, values);
		final Map<Integer, V> sortedOrigIndicesValues
				= Utils.createMapSortedByValueDescending(unsortedOrigIndicesValues);

		return sortedOrigIndicesValues;
	}

	/**
	 * Removes all entries from the map where the value was not accepted by the filter.
	 * @param <K>
	 * @param <V>
	 * @param map
	 * @param filter
	 * @throws IOException
	 */
	private static <K, V> void filterMap(final Map<K, V> map, final Filter<V> filter) throws IOException {

		final Iterator<Map.Entry<K, V>> mapIt = map.entrySet().iterator();
		while (mapIt.hasNext()) {
			final V mismatching = mapIt.next().getValue();
			if (!filter.accept(mismatching)) {
				mapIt.remove();
			}
		}
	}

	private static void createSortedMarkerMissingnessReport(OperationKey markersQAopKey, String reportName) throws IOException {

		final QAMarkersOperationDataSet qaMarkersOperationDataSet = (QAMarkersOperationDataSet)
				OperationManager.generateOperationDataSet(markersQAopKey);
		final List<Double> missingRatios = qaMarkersOperationDataSet.getMissingRatio();
		final Map<Integer, Double> sortedOrigIndexMissingRatios
				= createValueSortedMarkersOrigIndicesMap(qaMarkersOperationDataSet, missingRatios);

		// FILTER THE SORTED MAP
		filterMap(sortedOrigIndexMissingRatios, new Filter.DoubleGreaterThenFilter(0.0));
		final Collection<Integer> sortedMarkerOrigIndices = sortedOrigIndexMissingRatios.keySet(); // XXX maybe put into a List? (cause it needs to be ordered anyway)
		final Collection<Integer> orderedMarkerOrigIndices = sortedMarkerOrigIndices;
		final String[] columns = COLUMNS_MISSING;
		final Map<Integer, ?> orderedOrigIndexMissingnessRatioOrMismatchStates
				= sortedOrigIndexMissingRatios;

		createQAMarkerReport(
				qaMarkersOperationDataSet,
				orderedMarkerOrigIndices,
				columns,
				orderedOrigIndexMissingnessRatioOrMismatchStates,
				markersQAopKey,
				reportName);
	}

	private static void createMarkerMismatchReport(OperationKey markersQAopKey, String reportName) throws IOException {

		final QAMarkersOperationDataSet qaMarkersOperationDataSet = (QAMarkersOperationDataSet)
				OperationManager.generateOperationDataSet(markersQAopKey);
		final List<Boolean> mismatchStates = qaMarkersOperationDataSet.getMismatchStates();
		final Map<Integer, Boolean> unsortedOrigIndexMismatchStates
				= createOrigIndicesMap(qaMarkersOperationDataSet, mismatchStates);

		// FILTER THE UNSORTED MAP
		filterMap(unsortedOrigIndexMismatchStates, new Filter.BooleanFilter());
		final Collection<Integer> unsortedOrigIndices = unsortedOrigIndexMismatchStates.keySet();
		final Collection<Integer> orderedMarkerOrigIndices = unsortedOrigIndices;
		final String[] columns = COLUMNS_MISMATCH;
		final Map<Integer, ?> orderedOrigIndexMissingnessRatioOrMismatchStates
				= unsortedOrigIndexMismatchStates;

		createQAMarkerReport(
				qaMarkersOperationDataSet,
				orderedMarkerOrigIndices,
				columns,
				orderedOrigIndexMissingnessRatioOrMismatchStates,
				markersQAopKey,
				reportName);
	}

	private static void createQAMarkerReport(
			final QAMarkersOperationDataSet qaMarkersOperationDataSet,
			final Collection<Integer> orderedMarkerOrigIndices, // XXX maybe put into a List? (cause it needs to be ordered anyway)
			final String[] columns,
			final Map<Integer, ?> orderedOrigIndexMissingnessRatioOrMismatchStates,
			OperationKey markersQAopKey,
			String reportName)
			throws IOException
	{
		final DataSetSource matrixDataSetSource = MatrixFactory.generateMatrixDataSetSource(markersQAopKey.getParentMatrixKey());

		final OperationMetadata rdOPMetadata = OperationsList.getOperationMetadata(markersQAopKey);
		final MarkersMetadataSource markersMetadatas = matrixDataSetSource.getMarkersMetadatasSource();
		final List<MarkerMetadata> orderedMarkersMetadatas = Utils.createIndicesOrderedList(orderedMarkerOrigIndices, markersMetadatas);

		// WRITE HEADER OF FILE
		final String header = createReportHeaderLine(columns);
		final String reportPath = Study.constructReportsPath(rdOPMetadata.getStudyKey());

		// WRITE MARKERS ID & RSID
		ReportWriter.writeFirstColumnToReport(reportPath, reportName, header, orderedMarkersMetadatas, null, MarkerMetadata.TO_MARKER_ID);
		ReportWriter.appendColumnToReport(reportPath, reportName, orderedMarkersMetadatas, null, MarkerMetadata.TO_RS_ID);

		// WRITE MARKERSET CHROMOSOME
		ReportWriter.appendColumnToReport(reportPath, reportName, orderedMarkersMetadatas, null, MarkerMetadata.TO_CHR);

		// WRITE MARKERSET POS
		ReportWriter.appendColumnToReport(reportPath, reportName, orderedMarkersMetadatas, null, new Extractor.ToStringMetaExtractor(MarkerMetadata.TO_POS));

		// WRITE KNOWN ALLELES FROM QA
		final List<Byte> unsortedKnownMinorAlleles = qaMarkersOperationDataSet.getKnownMinorAllele(-1, -1);
		final List<Byte> sortedKnownMinorAlleles = Utils.createIndicesOrderedList(orderedMarkerOrigIndices, unsortedKnownMinorAlleles);
		ReportWriter.appendColumnToReport(reportPath, reportName, sortedKnownMinorAlleles, null, new Extractor.ByteToStringExtractor());

		final List<Byte> unsortedKnownMajorAlleles = qaMarkersOperationDataSet.getKnownMajorAllele(-1, -1);
		final List<Byte> sortedKnownMajorAlleles = Utils.createIndicesOrderedList(orderedMarkerOrigIndices, unsortedKnownMajorAlleles);
		ReportWriter.appendColumnToReport(reportPath, reportName, sortedKnownMajorAlleles, null, new Extractor.ByteToStringExtractor());

		// WRITE QA MISSINGNESS RATIO OR MISMATCH STATE
		ReportWriter.appendColumnToReport(reportPath, reportName, orderedOrigIndexMissingnessRatioOrMismatchStates, false, false);
	}

	public static class QAMarkersReportParser implements ReportParser {

		private final boolean missingness;

		public QAMarkersReportParser(final boolean missingness) {
			this.missingness = missingness;
		}

		@Override
		public String[] getColumnHeaders() {
			return missingness ? COLUMNS_MISSING : COLUMNS_MISMATCH;
		}

		@Override
		public List<Object[]> parseReport(
				final File reportFile,
				final int numRowsToFetch,
				final boolean exactValues)
				throws IOException
		{
			return ReportWriter.parseReport(
					reportFile,
					new QAMarkersReportLineParser(exactValues, missingness),
					numRowsToFetch);
		}
	}

	private static class QAMarkersReportLineParser extends AbstractReportLineParser {

		private final boolean missingness;

		public QAMarkersReportLineParser(final boolean exactValues, final boolean missingness) {
			super(exactValues);

			this.missingness = missingness;
		}

		@Override
		public Object[] extract(final String[] cVals) {

			final String[] columns = missingness ? COLUMNS_MISSING : COLUMNS_MISMATCH;
			final Object[] row = new Object[columns.length];

			final String markerId = cVals[0];
			final String rsId = cVals[1];
			final String chr = cVals[2];
			final int position = Integer.parseInt(cVals[3]);
			final String minAllele = cVals[4];
			final String majAllele = cVals[5];
			final Object missingnessOrMismatch;
			if (missingness) {
				final Double missRat_f = maybeTryToRoundNicely(tryToParseDouble(cVals[6]));
				missingnessOrMismatch = missRat_f;
			} else {
				final Boolean mismatchState = cVals[6] == null ? null : Boolean.valueOf(cVals[6]);
				missingnessOrMismatch = mismatchState;
			}

			row[0] = markerId;
			row[1] = rsId;
			row[2] = chr;
			row[3] = position;
			row[4] = minAllele;
			row[5] = majAllele;
			row[6] = missingnessOrMismatch;

			return row;
		}
	}
}
