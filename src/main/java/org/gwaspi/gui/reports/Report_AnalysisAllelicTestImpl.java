package org.gwaspi.gui.reports;

import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.RowRendererAllelicAssocWithZoomQueryDB;
import org.gwaspi.gui.utils.RowRendererDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public final class Report_AnalysisAllelicTestImpl extends Report_Analysis {

	private static final Logger log
			= LoggerFactory.getLogger(Report_AnalysisAllelicTestImpl.class);

	private static final String[] COLUMNS = new String[] {
			Text.Reports.markerId,
			Text.Reports.rsId,
			Text.Reports.chr,
			Text.Reports.pos,
			Text.Reports.minAallele,
			Text.Reports.majAallele,
			Text.Reports.chiSqr,
			Text.Reports.pVal,
			Text.Reports.oddsRatio,
			Text.Reports.zoom,
			Text.Reports.externalResource};

	public Report_AnalysisAllelicTestImpl(final int studyId, final String analysisFileName, final int opId, final Integer nRows) {
		super(studyId, opId, analysisFileName, nRows);
	}

	@Override
	protected String[] getColumns() {
		return COLUMNS;
	}

	@Override
	protected int getZoomColumnIndex() {
		return 9;
	}

	@Override
	protected RowRendererDefault createRowRenderer() {
		return new RowRendererAllelicAssocWithZoomQueryDB();
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
		Double or = cVals[8] != null ? Double.parseDouble(cVals[8]) : Double.NaN;

		int col = 0;
		row[col++] = markerId;
		row[col++] = rsId;
		row[col++] = chr;
		row[col++] = position;
		row[col++] = minAllele;
		row[col++] = majAllele;

//		if (!cGlobal.OSNAME.contains("Windows")) {
		Double chiSqr_f;
		Double pVal_f;
		Double or_f;
		try {
			chiSqr_f = Double.parseDouble(FORMAT_ROUND.format(chiSqr));
		} catch (NumberFormatException ex) {
			chiSqr_f = chiSqr;
		}
		try {
			pVal_f = Double.parseDouble(FORMAT_SCIENTIFIC.format(pVal));
		} catch (NumberFormatException ex) {
			pVal_f = pVal;
		}
		try {
			or_f = Double.parseDouble(FORMAT_ROUND.format(or));
		} catch (NumberFormatException ex) {
			or_f = or;
		}
		row[col++] = chiSqr_f;
		row[col++] = pVal_f;
		row[col++] = or_f;
//		} else {
//			row[c++] = dfRound.format(chiSqr);
//			row[c++] = dfSci.format(pVal);
//			row[c++] = dfRound.format(or);
//		}

		row[col++] = "";
		row[col++] = Text.Reports.queryDB;

		return row;
	}
}
