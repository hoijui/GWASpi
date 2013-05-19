package org.gwaspi.gui.reports;

import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.RowRendererDefault;
import org.gwaspi.gui.utils.RowRendererGenotypicAssocWithZoomQueryDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Report_AnalysisGenotypicTestImpl extends Report_Analysis {

	private static final Logger log
			= LoggerFactory.getLogger(Report_AnalysisGenotypicTestImpl.class);

	private static final String[] COLUMNS = new String[] {
			Text.Reports.markerId,
			Text.Reports.rsId,
			Text.Reports.chr,
			Text.Reports.pos,
			Text.Reports.minAallele,
			Text.Reports.majAallele,
			Text.Reports.chiSqr,
			Text.Reports.pVal,
			Text.Reports.ORAAaa,
			Text.Reports.ORAaaa,
			Text.Reports.zoom,
			Text.Reports.externalResource};

	public Report_AnalysisGenotypicTestImpl(final int studyId, final String analysisFileName, final int opId, Integer nRows) {
		super(studyId, opId, analysisFileName, nRows);
	}

	@Override
	protected String[] getColumns() {
		return COLUMNS;
	}

	@Override
	protected int getZoomColumnIndex() {
		return 10;
	}

	@Override
	protected RowRendererDefault createRowRenderer() {
		return new RowRendererGenotypicAssocWithZoomQueryDB();
	}

	@Override
	protected Object[] parseRow(String[] cVals) {

		Object[] row = new Object[getColumns().length];

		String markerId = cVals[0];
		String rsId = cVals[1];
		String chr = cVals[2];
		long position = Long.parseLong(cVals[3]);
		String minAllele = cVals[4];
		String majAllele = cVals[5];
		Double chiSqr = cVals[6] != null ? Double.parseDouble(cVals[6]) : Double.NaN;
		Double pVal = cVals[7] != null ? Double.parseDouble(cVals[7]) : Double.NaN;
		Double ORAAaa = cVals[8] != null ? Double.parseDouble(cVals[8]) : Double.NaN;
		Double ORAaaa = cVals[9] != null ? Double.parseDouble(cVals[9]) : Double.NaN;

		int col = 0;
		row[col++] = markerId;
		row[col++] = rsId;
		row[col++] = chr;
		row[col++] = position;
		row[col++] = minAllele;
		row[col++] = majAllele;

		Double chiSqr_f;
		Double pVal_f;
		try {
			chiSqr_f = Double.parseDouble(FORMAT_ROUND.format(chiSqr));
		} catch (NumberFormatException ex) {
			chiSqr_f = chiSqr;
			log.warn(null, ex);
		}
		try {
			pVal_f = Double.parseDouble(FORMAT_SCIENTIFIC.format(pVal));
		} catch (NumberFormatException ex) {
			pVal_f = pVal;
			log.warn(null, ex);
		}
		row[col++] = chiSqr_f;
		row[col++] = pVal_f;
		row[col++] = ORAAaa;
		row[col++] = ORAaaa;

		row[col++] = "";
		row[col++] = Text.Reports.queryDB;

		return row;
	}
}
