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

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.dao.OperationService;
import org.gwaspi.global.Config;
import org.gwaspi.gui.reports.ManhattanPlotZoom;
import org.gwaspi.gui.reports.SampleQAHetzygPlotZoom;
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MarkersMetadataSource;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.gwaspi.operations.OperationManager;
import org.gwaspi.operations.qasamples.QASamplesOperationDataSet;
import org.gwaspi.operations.trendtest.CommonTestOperationDataSet;
import org.gwaspi.operations.trendtest.TrendTestOperationEntry;
import org.gwaspi.statistics.Chisquare;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.plot.CombinedRangeXYPlot;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericReportGenerator {

	private static final Logger log
			= LoggerFactory.getLogger(GenericReportGenerator.class);

	public static final String PLOT_MANHATTAN_THRESHOLD_CONFIG = "CHART_MANHATTAN_PLOT_THRESHOLD";
	public static final double PLOT_MANHATTAN_THRESHOLD_DEFAULT = 5E-7;
	public static final String PLOT_MANHATTAN_BACKGROUND_CONFIG = "CHART_MANHATTAN_PLOT_BACKGROUND";
	public static final Color PLOT_MANHATTAN_BACKGROUND_DEFAULT = new Color(200, 200, 200);
	public static final String PLOT_MANHATTAN_BACKGROUND_ALTERNATIVE_CONFIG = "CHART_MANHATTAN_PLOT_BACKGROUND_ALTERNATIVE";
	public static final Color PLOT_MANHATTAN_BACKGROUND_ALTERNATIVE_DEFAULT = new Color(230, 230, 230);
	public static final String PLOT_MANHATTAN_MAIN_CONFIG = "CHART_MANHATTAN_PLOT_MAIN";
	public static final Color PLOT_MANHATTAN_MAIN_DEFAULT = Color.BLUE;
	public static final String PLOT_QQ_BACKGROUND_CONFIG = "CHART_QQ_PLOT_BACKGROUND";
	public static final Color PLOT_QQ_BACKGROUND_DEFAULT = new Color(230, 230, 230);
	public static final String PLOT_QQ_ACTUAL_CONFIG = "CHART_QQ_PLOT_MAIN";
	public static final Color PLOT_QQ_ACTUAL_DEFAULT = Color.BLUE;
	public static final String PLOT_QQ_MU_CONFIG = "CHART_QQ_PLOT_MU";
	public static final Color PLOT_QQ_MU_DEFAULT = Color.GRAY;
	public static final String PLOT_QQ_SIGMA_CONFIG = "CHART_QQ_PLOT_SIGMA";
	public static final Color PLOT_QQ_SIGMA_DEFAULT = Color.LIGHT_GRAY;

	public static final DecimalFormat FORMAT_P_VALUE = new DecimalFormat("0.#E0#");

	private GenericReportGenerator() {
	}

	private static OperationService getOperationService() {
		return OperationsList.getOperationService();
	}

	private static class MarkerManhattenData {

		private final String chromosome;
		private final int position;
		private final Double pValue;

		public MarkerManhattenData(final String chromosome, final int position, final Double pValue) {

			this.chromosome = chromosome;
			this.position = position;
			this.pValue = pValue;
		}

		public String getChromosome() {
			return chromosome;
		}

		public int getPosition() {
			return position;
		}

		public Double getPValue() {
			return pValue;
		}
	}

	//<editor-fold defaultstate="expanded" desc="ASSOCIATION CHARTS">
	private static Map<MarkerKey, MarkerManhattenData> assembleManhattenPlotData(OperationKey testOpKey) throws IOException {

		Map<MarkerKey, MarkerManhattenData> markerKeyChrPosPVal;

		CommonTestOperationDataSet<? extends TrendTestOperationEntry> testOpDS
				= (CommonTestOperationDataSet<? extends TrendTestOperationEntry>)
				OperationManager.generateOperationDataSet(testOpKey);
		final Iterator<MarkerKey> testOpMarkerKeys = testOpDS.getMarkersKeysSource().iterator();
		final Iterator<Double> testOpPValues = testOpDS.getPs(-1, -1).iterator();

		final DataSetSource matrixDS
				= MatrixFactory.generateMatrixDataSetSource(testOpKey.getParentMatrixKey());
		final MarkersMetadataSource matrixMarkersMS = matrixDS.getMarkersMetadatasSource();
		final Iterator<String> matrixMarkersChromosomes = matrixMarkersMS.getChromosomes().iterator();
		final Iterator<Integer> matrixMarkersPositions = matrixMarkersMS.getPositions().iterator();
		markerKeyChrPosPVal = new LinkedHashMap<MarkerKey, MarkerManhattenData>(matrixMarkersMS.size());
		MarkerKey nextTestMarkerKey = testOpMarkerKeys.hasNext() ? testOpMarkerKeys.next() : null;
		for (final MarkerKey markerKey : matrixDS.getMarkersKeysSource()) {
			final String chromosome = matrixMarkersChromosomes.next();
			final int position = matrixMarkersPositions.next();
			Double pValue;
			if (markerKey.equals(nextTestMarkerKey)) {
				pValue = testOpPValues.next();
				if (pValue.isNaN() || pValue.isInfinite()) { // Ignore invalid P-Values
					pValue = null;
				}
				nextTestMarkerKey = testOpMarkerKeys.hasNext() ? testOpMarkerKeys.next() : null;
			} else {
				// The current markerKey has no entry in the test operation,
				// thus we have no P-Value for it, so we mark it as totally insignificant.
				pValue = 1.0;
			}
			MarkerManhattenData data = new MarkerManhattenData(
					chromosome,
					position,
					pValue);
			markerKeyChrPosPVal.put(markerKey, data);
		}

//		long snpNumber = rdInfoMarkerSet.getMarkerSetSize();
//		if (snpNumber < 250000) {
//			hetzyThreshold = 0.5 / snpNumber;  // (0.05 / 10^6 SNPs => 5*10^(-7))
//		}

		return markerKeyChrPosPVal;
	}

	public static CombinedRangeXYPlot buildManhattanPlot(OperationKey testOpKey, final List<Double> thresholds) throws IOException {

		// PLOT DEFAULTS
		final Config config = Config.getSingleton();
		final double defaultThreshold = config.getDouble(
				PLOT_MANHATTAN_THRESHOLD_CONFIG,
				PLOT_MANHATTAN_THRESHOLD_DEFAULT);
		final Color background = config.getColor(
				PLOT_MANHATTAN_BACKGROUND_CONFIG,
				PLOT_MANHATTAN_BACKGROUND_DEFAULT);
		final Color backgroundAlternative = config.getColor(
				PLOT_MANHATTAN_BACKGROUND_ALTERNATIVE_CONFIG,
				PLOT_MANHATTAN_BACKGROUND_ALTERNATIVE_DEFAULT);
		final Color main = config.getColor(
				PLOT_MANHATTAN_MAIN_CONFIG,
				PLOT_MANHATTAN_MAIN_DEFAULT);

		Map<MarkerKey, MarkerManhattenData> markerKeyChrPosPVal = assembleManhattenPlotData(testOpKey);

		XYSeriesCollection currChrSC = new XYSeriesCollection();

		NumberAxis sharedAxis = new NumberAxis("-log₁₀(P)");

		CombinedRangeXYPlot combinedPlot = new CombinedRangeXYPlot(sharedAxis);
		combinedPlot.setGap(0);

		XYSeries currChrS = null;

		// Subdividing points into sub-XYSeries, per chromosome
		String currChr = "";
		Map<String, MarkerKey> labeler = new LinkedHashMap<String, MarkerKey>(); // FIXME This is unused, was a global static var before (also private though), was the data added here actually used somewhere? (i think not)
		for (Map.Entry<MarkerKey, MarkerManhattenData> entry : markerKeyChrPosPVal.entrySet()) {
			MarkerKey markerKey = entry.getKey();
			MarkerManhattenData data = entry.getValue();

			if (data.getPValue() != null) {
				final double pVal = data.getPValue(); // Is allready free of NaN and infinity
				if (pVal < 1) {
					if (data.getChromosome().equals(currChr)) {
						currChrS.add(data.getPosition(), pVal);
						labeler.put(currChr + "_" + data.getPosition(), markerKey);
					} else {
						if (!currChr.isEmpty()) { // SKIP FIRST TIME (NO DATA YET!)
							// add the last (now compleeted) chromosomes data-set,
							// before starting the new one
							currChrSC.addSeries(currChrS);
							appendToCombinedRangeManhattanPlot(combinedPlot, currChr, currChrSC, false, defaultThreshold, background, backgroundAlternative, main);
						}
						currChr = data.getChromosome();
						currChrSC = new XYSeriesCollection();
						currChrS = new XYSeries(currChr);
						labeler.put(currChr + "_" + data.getPosition(), markerKey);

						currChrS.add(data.getPosition(), pVal);
					}
				}
			}
		}
		if (currChrS != null) {
			currChrSC.addSeries(currChrS);
			// ADD LAST CHR TO PLOT
			appendToCombinedRangeManhattanPlot(combinedPlot, currChr, currChrSC, true, defaultThreshold, background, backgroundAlternative, main);
		}

		// Remove Legend from the bottom of the chart
		combinedPlot.setFixedLegendItems(new LegendItemCollection());

		return combinedPlot;
	}

	private static void appendToCombinedRangeManhattanPlot(CombinedRangeXYPlot combinedPlot, String chromosome, XYSeriesCollection currChrSC, boolean showlables, double threshold, Color background, Color backgroundAlternative, Color main) {

		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(false, true);

		// Set dot shape of the currently appended Series
		renderer.setSeriesPaint(currChrSC.getSeriesCount() - 1, main);
		renderer.setSeriesVisibleInLegend(currChrSC.getSeriesCount() - 1, showlables);
		renderer.setSeriesShape(currChrSC.getSeriesCount() - 1, new Rectangle2D.Double(-1.0, -1.0, 2.0, 2.0));

		// Set range axis
		if (combinedPlot.getSubplots().isEmpty()) {
			LogAxis rangeAxis = new LogAxis("P value");
			rangeAxis.setBase(10);
			rangeAxis.setInverted(true);
			rangeAxis.setNumberFormatOverride(FORMAT_P_VALUE);

			rangeAxis.setTickMarkOutsideLength(2.0f);
			rangeAxis.setMinorTickCount(2);
			rangeAxis.setMinorTickMarksVisible(true);
			rangeAxis.setAxisLineVisible(true);
			rangeAxis.setUpperMargin(0);

			TickUnitSource units = NumberAxis.createIntegerTickUnits();
			rangeAxis.setStandardTickUnits(units);

			combinedPlot.setRangeAxis(0, rangeAxis);
		}

		// Build subchart
		JFreeChart subchart = ChartFactory.createScatterPlot("",
				"Chr " + chromosome,
				"",
				currChrSC,
				PlotOrientation.VERTICAL,
				false,
				false,
				false);

		// Get subplot from subchart
		XYPlot subplot = (XYPlot) subchart.getPlot();
		subplot.setRenderer(renderer);
		subplot.setBackgroundPaint(null);

		// CHART BACKGROUD COLOR
		if (combinedPlot.getSubplots().size() % 2 == 0) {
			subplot.setBackgroundPaint(background); // Hue, saturation, brightness
		} else {
			subplot.setBackgroundPaint(backgroundAlternative); // Hue, saturation, brightness
		}

		// Add significance Threshold to subplot
		final Marker thresholdLine = new ValueMarker(threshold);
		thresholdLine.setPaint(Color.red);
		// Add legend to hetzyThreshold
		if (showlables) {
			thresholdLine.setLabel("P = " + FORMAT_P_VALUE.format(threshold));
		}
		thresholdLine.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
		thresholdLine.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
		subplot.addRangeMarker(thresholdLine);

		// Chromosome Axis Labels
		NumberAxis chrAxis = (NumberAxis) subplot.getDomainAxis();
		chrAxis.setLabelAngle(1.0);
		chrAxis.setAutoRangeIncludesZero(false);
		chrAxis.setAxisLineVisible(true);

		chrAxis.setTickLabelsVisible(false);
		chrAxis.setTickMarksVisible(false);
//		chrAxis.setNumberFormatOverride(Report_Analysis.FORMAT_SCIENTIFIC);
//		TickUnitSource units = NumberAxis.createIntegerTickUnits();
//		chrAxis.setStandardTickUnits(units);

		//combinedPlot.setGap(0);
		combinedPlot.add(subplot, 1);
	}

	private static List<Double> assembleQQPlotData(OperationKey testOpKey) throws IOException {

		CommonTestOperationDataSet testOpDS = (CommonTestOperationDataSet) OperationManager.generateOperationDataSet(testOpKey);
		Collection<Double> chiSqrVals = testOpDS.getTs(-1, -1);
		List<Double> obsChiSqrVals = new ArrayList<Double>(chiSqrVals.size());
		for (Double chiSqr : chiSqrVals) {
			if (!Double.isNaN(chiSqr) && !Double.isInfinite(chiSqr)) {
				obsChiSqrVals.add(chiSqr);
			}
		}
		Collections.sort(obsChiSqrVals);

		return obsChiSqrVals;
	}

	public static XYPlot buildQQPlot(OperationKey testOpKey, int df) throws IOException {

		if (df != 1 && df != 2) {
			throw new IllegalArgumentException("Only df = 1 or 2 is supported; it is " + df);
		}

		//<editor-fold defaultstate="expanded" desc="PLOT DEFAULTS">
		final Config config = Config.getSingleton();
		final Color background = config.getColor(
				PLOT_QQ_BACKGROUND_CONFIG,
				PLOT_QQ_BACKGROUND_DEFAULT);
		final Color actual = config.getColor(
				PLOT_QQ_ACTUAL_CONFIG,
				PLOT_QQ_ACTUAL_DEFAULT);
		final Color sigma = config.getColor(
				PLOT_QQ_SIGMA_CONFIG,
				PLOT_QQ_SIGMA_DEFAULT);
		final Color mu = config.getColor(
				PLOT_QQ_MU_CONFIG,
				PLOT_QQ_MU_DEFAULT);
		//</editor-fold>

		//<editor-fold defaultstate="expanded" desc="GET X^2">
		List<Double> obsChiSqrVals = assembleQQPlotData(testOpKey);

		int N = obsChiSqrVals.size();
		List<Double> expChiSqrDist;
		if (df == 1) {
			expChiSqrDist = Chisquare.getChiSquareDistributionDf1(N, 1.0f);
		} else { // df == 2
			expChiSqrDist = Chisquare.getChiSquareDistributionDf2(N, 1.0f);
		}
		Collections.sort(expChiSqrDist);
		//</editor-fold>

		//<editor-fold defaultstate="expanded" desc="GET CONFIDENCE BOUNDARY">
		InputStream boundaryStream = GenericReportGenerator.class.getResourceAsStream("/samples/chisqrboundary-df" + df + ".txt");
		InputStreamReader isr = new InputStreamReader(boundaryStream);
		BufferedReader inputBufferReader = new BufferedReader(isr);

		Double stopValue = expChiSqrDist.get(N - 1);
		Double currentValue = 0d;
		List<Double[]> boundary = new ArrayList<Double[]>();
		while (currentValue <= stopValue) {
			String l = inputBufferReader.readLine();
			if (l == null) {
				break;
			}
			String[] cVals = l.split(",");
			Double[] slice = new Double[3];
			slice[0] = Double.parseDouble(cVals[0]);
			slice[1] = Double.parseDouble(cVals[1]);
			slice[2] = Double.parseDouble(cVals[2]);
			currentValue = slice[1];
			boundary.add(slice);
		}
		inputBufferReader.close();
		//</editor-fold>

		XYSeriesCollection dataSeries = new XYSeriesCollection();
		XYSeries seriesData = new XYSeries("X²");
		XYSeries seriesRef = new XYSeries("Expected");

		for (int i = 0; i < obsChiSqrVals.size(); i++) {
			double obsVal = obsChiSqrVals.get(i);
			double expVal = expChiSqrDist.get(i);

			seriesData.add(expVal, obsVal);
			seriesRef.add(expVal, expVal);
		}

		//constant chi-square boundaries
		XYSeries seriesLower = new XYSeries("2σ boundary");
		XYSeries seriesUpper = new XYSeries("");
		for (Double[] slice : boundary) {
			seriesUpper.add(slice[1], slice[0]);
			seriesLower.add(slice[1], slice[2]);
		}

		dataSeries.addSeries(seriesData);
		dataSeries.addSeries(seriesRef);
		dataSeries.addSeries(seriesUpper);
		dataSeries.addSeries(seriesLower);
		final XYDataset data = dataSeries;

		//create QQ plot
		final boolean withLegend = true;
		JFreeChart chart = ChartFactory.createScatterPlot(
				"QQ-plot", "Exp X²", "Obs X²",
				data,
				PlotOrientation.VERTICAL,
				withLegend,
				false,
				false);

		final XYPlot plot = chart.getXYPlot();
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(false, true);
		renderer.setSeriesPaint(0, actual);
		renderer.setSeriesPaint(1, mu);
		renderer.setSeriesPaint(2, sigma);
		renderer.setSeriesPaint(3, sigma);

		renderer.setBaseShapesVisible(true);
		renderer.setBaseShapesFilled(true);
		renderer.setSeriesShape(0, new Rectangle2D.Double(-1.0, -1.0, 2.0, 2.0));
		renderer.setSeriesShape(1, new Rectangle2D.Double(-1.0, -1.0, 2.0, 2.0));
		renderer.setSeriesShape(2, new Rectangle2D.Double(-1.0, -1.0, 2.0, 2.0));
		renderer.setSeriesShape(3, new Rectangle2D.Double(-1.0, -1.0, 2.0, 2.0));

		plot.setRenderer(renderer);

		// PLOT BACKGROUND COLOR
		plot.setBackgroundPaint(background); // Hue, saturation, brightness

		return plot;
	}

	public static XYDataset getManhattanZoomByChrAndPos(
			ManhattanPlotZoom manhattanPlotZoom,
			OperationKey testOpKey,
			ChromosomeKey chr,
			MarkerKey markerKey,
			long requestedPhysPos,
			long requestedPosWindow)
	{
		XYDataset resultXYDataset = null;

		try {
			// ESTIMATE WINDOW SIZE
			Long minPosition;
			Long maxPosition;
			if (markerKey == null) {
				minPosition = requestedPhysPos;
				maxPosition = minPosition + requestedPosWindow;
			} else {
				final Long middlePosition = requestedPhysPos;
				minPosition = Math.round(middlePosition - (double) requestedPosWindow / 2);
				maxPosition = minPosition + requestedPosWindow;
			}

			Map<MarkerKey, Object[]> markerKeyChrPosPVal;

			CommonTestOperationDataSet<? extends TrendTestOperationEntry> testOpDS
					= (CommonTestOperationDataSet<? extends TrendTestOperationEntry>)
					OperationManager.generateOperationDataSet(testOpKey);
			Iterator<MarkerMetadata> markersMetadatasIt = testOpDS.getMarkersMetadatasSource().iterator();
			Iterator<Double> psIt = testOpDS.getPs(-1, -1).iterator();
			markerKeyChrPosPVal = new LinkedHashMap<MarkerKey, Object[]>(testOpDS.getNumMarkers());
			for (MarkerKey curMarkerKey : testOpDS.getMarkersKeysSource()) {
				Double pValue = psIt.next();
				MarkerMetadata markerMetadata = markersMetadatasIt.next();
				String curChr = markerMetadata.getChr();
				int curPos = markerMetadata.getPos();
				if (!curChr.equals(chr.toString())
						|| (curPos >= minPosition)
						|| (curPos <= maxPosition))
				{
					continue;
				}
				if (pValue.isNaN() || pValue.isInfinite()) { // Ignore invalid P-Values
					pValue = null;
				}
				Object[] data = new Object[] {
					markerMetadata.getChr(),
					markerMetadata.getPos(),
					pValue
				};
				markerKeyChrPosPVal.put(curMarkerKey, data);
			}

			//<editor-fold defaultstate="expanded" desc="BUILD XYDataset">
			XYSeries dataSeries = new XYSeries("");

			Map<String, MarkerKey> labeler = new LinkedHashMap<String, MarkerKey>();
			for (Map.Entry<MarkerKey, Object[]> entry : markerKeyChrPosPVal.entrySet()) {
				MarkerKey tmpMarker = entry.getKey();
				Object[] data = entry.getValue(); // CHR, POS, PVAL

				int position = (Integer) data[1];
				double pVal = 1;
				if (data[2] != null) {
					pVal = (Double) data[2]; // Is allready free of NaN
				}

				if (pVal < 1 && !Double.isInfinite(pVal)) {
					dataSeries.add(position, pVal);
					labeler.put(chr + "_" + position, tmpMarker);
					//labeler.put(key, "");
				}
			}
			manhattanPlotZoom.setLabelerMap(labeler);

			dataSeries.setDescription("Zoom chr " + chr + " from position " + minPosition + " to " + maxPosition);

			resultXYDataset = new XYSeriesCollection(dataSeries);
			//</editor-fold>
		} catch (IOException ex) {
			log.error(null, ex);
		}

		return resultXYDataset;
	}

//	/**
//	 * This getManhattanZoomByMarkerIdOrIdx has now been deprecated in favor of
//	 * getManhattanZoomByChrAndPos
//	 *
//	 * @deprecated Use getManhattanZoomByChrAndPos instead
//	 */
//	public static XYDataset getManhattanZoomByMarkerIdOrIdx(
//			ManhattanPlotZoom manhattanPlotZoom,
//			OperationKey testOpKey,
//			MarkerKey origMarkerKey,
//			int startIdxPos,
//			int requestedSetSize)
//	{
//		XYDataset resultXYDataset = null;
//
//		try {
////			OperationMetadata rdOPMetadata = OperationsList.getOperation(testOpKey);
//
//			DataSetSource parentMatrixSource = MatrixFactory.generateMatrixDataSetSource(testOpKey.getParentMatrixKey());
//			CommonTestOperationDataSet<? extends TrendTestOperationEntry> testOpDS = (CommonTestOperationDataSet<? extends TrendTestOperationEntry>) OperationManager.generateOperationDataSet(testOpKey);
//			MarkersMetadataSource markersMetadatasSource = parentMatrixSource.getMarkersMetadatasSource();
//			Map<MarkerKey, Object[]> markerKeyChrPosPVal = new LinkedHashMap<MarkerKey, Object[]>(markersMetadatasSource.size());
//			for (MarkerMetadata markerMetadata : markersMetadatasSource) {
//				MarkerKey markerKey = MarkerKey.valueOf(markerMetadata);
//				Object[] data = new Object[] {
//					markerMetadata.getChr(),
//					markerMetadata.getPos(),
//					null // P-Value, will be filled in later on (not for all markers though)
//				};
//				markerKeyChrPosPVal.put(markerKey, data);
//			}
////			Map<Integer, MarkerKey> testOpMarkers = testOpDS.getMarkers();
////			Iterator<MarkerKey> testOpMarkerKeysIt = testOpMarkers.values().iterator();
////			Collection<Double> ps = testOpDS.getPs(-1, -1);
////			for (Double pValue : ps) {
////				MarkerKey curKey = testOpMarkerKeysIt.next();
////			}
//
////			NetcdfFile assocNcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
////			MarkerOperationSet rdAssocMarkerSet = new MarkerOperationSet(testOpKey);
////			Map<MarkerKey, double[]> rdAssocMarkerSetMap = rdAssocMarkerSet.getOpSetMap();
//
//			//<editor-fold defaultstate="expanded" desc="GET POSITION DATA">
//			MarkerSet rdInfoMarkerSet = new MarkerSet(testOpKey.getParentMatrixKey());
//			rdInfoMarkerSet.initFullMarkerIdSetMap();
//
//			// ESTIMATE WINDOW SIZE
//			Integer minPosition = 0;
//			Integer middlePosition = requestedSetSize / 2;
//			Integer maxPosition = requestedSetSize;
//
//			minPosition = startIdxPos;
//			middlePosition = Math.round((float) (minPosition + requestedSetSize) / 2);
//			maxPosition = minPosition + requestedSetSize;
//
//			if (testOpMarkers.size() < maxPosition) {
//				requestedSetSize = testOpMarkers.size();
//				minPosition = 0;
//				middlePosition = Math.round((float) requestedSetSize / 2);
//				maxPosition = requestedSetSize;
//			} else {
//				Iterator<MarkerKey> keyIt = rdAssocMarkerSetMap.keySet().iterator();
//				if (startIdxPos == Integer.MIN_VALUE) { // USE MARKERID TO LOCATE CENTER
//					boolean goOn = true;
//					int i = 0;
//					while (goOn && i < testOpMarkers.size()) {
//						MarkerKey key = keyIt.next();
//						if (key.equals(origMarkerKey)) {
//							minPosition = i - Math.round((float) requestedSetSize / 2);
//							if (minPosition < 0) {
//								minPosition = 0;
//							}
//
//							middlePosition = i;
//
//							maxPosition = i + Math.round((float) requestedSetSize / 2);
//							if (maxPosition > testOpMarkers.size()) {
//								maxPosition = testOpMarkers.size();
//							}
//							goOn = false;
//						}
//						i++;
//					}
//
//				}
//			}
//
//			// CUT READ-Map TO SIZE
//			boolean goOn = true;
//			int i = 0;
//			Iterator<MarkerKey> keyIt = rdAssocMarkerSetMap.keySet().iterator();
//			while (goOn && i < testOpMarkers.size()) {
//				MarkerKey key = keyIt.next();
//				if (i >= minPosition && i <= maxPosition) {
//					markerKeyChrPosPVal.put(key, null);
//					if (i == middlePosition) { // MAKE SURE WE KNOW WHAT MARKER-ID IS IN THE MIDDLE
//						origMarkerKey = key;
//					}
//				}
//				if (i > maxPosition) {
//					goOn = false;
//				}
//				i++;
//			}
//
//			// GET MARKER CHR & POS INFO
//			rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
//			// First check for same chromosome data
//			String validateChr = new String(rdInfoMarkerSet.getMarkerIdSetMapCharArray().get(origMarkerKey));
//			manhattanPlotZoom.setCenterPhysPos((long) minPosition);
//
//			for (Map.Entry<MarkerKey, Object[]> entry : markerKeyChrPosPVal.entrySet()) {
//				MarkerKey key = entry.getKey();
//				String chr = new String(rdInfoMarkerSet.getMarkerIdSetMapCharArray().get(key));
//				Object[] data = new Object[3]; // CHR, POS, PVAL
//				data[0] = chr;
//				entry.setValue(data);
//			}
//
//			rdInfoMarkerSet.fillWith(0);
//			rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
//			for (Map.Entry<MarkerKey, Object[]> entry : markerKeyChrPosPVal.entrySet()) {
//				MarkerKey key = entry.getKey();
//				Object[] data = entry.getValue(); // CHR, POS, PVAL
//				int pos = rdInfoMarkerSet.getMarkerIdSetMapInteger().get(key);
//				data[1] = pos;
//				entry.setValue(data);
//			}
//			if (rdInfoMarkerSet.getMarkerIdSetMapInteger() != null) {
//				rdInfoMarkerSet.getMarkerIdSetMapInteger().clear();
//			}
//			//</editor-fold>
//
//			//<editor-fold defaultstate="expanded" desc="GET Pval">
//			rdAssocMarkerSetMap = rdAssocMarkerSet.fillOpSetMapWithVariable(assocNcFile, netCDFVar);
//			for (Map.Entry<MarkerKey, Object[]> entry : markerKeyChrPosPVal.entrySet()) {
//				MarkerKey key = entry.getKey();
//				Object[] data = entry.getValue(); // CHR, POS, PVAL
//				double[] value = rdAssocMarkerSetMap.get(key);
//				Double pval = (Double) value[1]; // PVAL
//				if (!Double.isNaN(pval) && !Double.isInfinite(pval)) { // Ignore NaN Pvalues
//					data[2] = pval;
//					entry.setValue(data);
//				}
//			}
//			assocNcFile.close();
//			if (rdAssocMarkerSetMap != null) {
//				rdAssocMarkerSetMap.clear();
//			}
//			//</editor-fold>
//
//			//<editor-fold defaultstate="expanded" desc="BUILD XYDataset">
//			XYSeries dataSeries = new XYSeries("");
//
//			Map<String, MarkerKey> labeler = new LinkedHashMap<String, MarkerKey>();
//			for (Map.Entry<MarkerKey, Object[]> entry : markerKeyChrPosPVal.entrySet()) {
//				MarkerKey tmpMarker = entry.getKey();
//				Object[] data = entry.getValue(); //CHR, POS, PVAL
//
//				String chr = data[0].toString();
//				int position = (Integer) data[1];
//
//				if (data[2] != null && validateChr.equals(chr)) { // Check for same chromosome data before adding
//					double pVal = (Double) data[2]; // Is allready free of NaN
//					if (pVal < 1 && pVal != Double.POSITIVE_INFINITY && pVal != Double.NEGATIVE_INFINITY) {
//						dataSeries.add(position, pVal);
//						labeler.put(chr + "_" + position, tmpMarker);
//						//labelerHM.put(key,"");
//					}
//				}
//			}
//			long snpNumber = labeler.size();
//			manhattanPlotZoom.setLabelerMap(labeler);
//
//			dataSeries.setDescription("Zoom on " + origMarkerKey + ", window size: " + snpNumber);
//
//			resultXYDataset = new XYSeriesCollection(dataSeries);
//			//</editor-fold>
//		} catch (IOException ex) {
//			log.error(null, ex);
//		}
//
//		return resultXYDataset;
//	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="SAMPLE-QA PLOTS">
	public static XYDataset getSampleHetzygDataset(SampleQAHetzygPlotZoom sampleQAHetzygPlotZoom, OperationKey operationKey) throws IOException {

		XYDataset resultXYDataset;

		QASamplesOperationDataSet qaSamplesOpDS = (QASamplesOperationDataSet) OperationManager.generateOperationDataSet(operationKey);

		OperationMetadata rdOPMetadata = getOperationService().getOperationMetadata(operationKey);

		Map<Integer, SampleKey> samples = qaSamplesOpDS.getSamplesKeysSource().getIndicesMap();
		List<Double> hetzyRatios = (List) qaSamplesOpDS.getHetzyRatios(-1, -1);
		List<Double> missingRatios = (List) qaSamplesOpDS.getMissingRatios(-1, -1);

		//<editor-fold defaultstate="expanded" desc="BUILD XYDataset">
		XYSeries dataSeries = new XYSeries("");

		int count = 0;
		Map<String, SampleKey> samplesLabeler = new LinkedHashMap<String, SampleKey>();
		for (SampleKey tmpSampleKey : samples.values()) {
			double tmpHetzyVal = hetzyRatios.get(count);
			double tmpMissratVal = missingRatios.get(count);
			if (Double.isNaN(tmpHetzyVal) || Double.isInfinite(tmpHetzyVal)) {
				tmpHetzyVal = 0;
			}
			if (Double.isNaN(tmpMissratVal) || Double.isInfinite(tmpMissratVal)) {
				tmpMissratVal = 0;
			}

			dataSeries.add(tmpHetzyVal, tmpMissratVal);
			samplesLabeler.put(count + "_" + tmpMissratVal + "_" + tmpHetzyVal, tmpSampleKey);
			count++;
		}
		sampleQAHetzygPlotZoom.setLabelerMap(samplesLabeler);

		dataSeries.setDescription(rdOPMetadata.getDescription());

		resultXYDataset = new XYSeriesCollection(dataSeries);
		//</editor-fold>

		return resultXYDataset;
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="HELPERS">
//	public static Map<MarkerKey, Object[]> getMarkerSetChrAndPos(OperationKey operationKey) throws IOException {
//		Map<MarkerKey, Object[]> dataSetMap = new LinkedHashMap<MarkerKey, Object[]>();
//
//		DataSetSource dataSetSource = MatrixFactory.generateMatrixDataSetSource(operationKey.getParentMatrixKey());
//		MarkersMetadataSource markersMetadatasSource = dataSetSource.getMarkersMetadatasSource();
////		MarkerSet rdInfoMarkerSet = new MarkerSet(operationKey.getParentMatrixKey());
////		rdInfoMarkerSet.initFullMarkerIdSetMap();
////		rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
//		if (rdInfoMarkerSet.getMarkerIdSetMapCharArray() != null) {XXX;
//			for (Map.Entry<MarkerKey, char[]> entry : rdInfoMarkerSet.getMarkerIdSetMapCharArray().entrySet()) {
//				MarkerKey key = entry.getKey();
//				String chr = new String(entry.getValue());
//				Object[] data = new Object[2]; // CHR, POS
//				data[0] = chr;
//				dataSetMap.put(key, data);
//			}
//
//			rdInfoMarkerSet.fillWith(0);
//			rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
//			for (Map.Entry<MarkerKey, Integer> entry : rdInfoMarkerSet.getMarkerIdSetMapInteger().entrySet()) {
//				MarkerKey key = entry.getKey();
//				Object[] data = dataSetMap.get(key); // CHR, POS
//				int pos = entry.getValue();
//				data[1] = pos;
//				dataSetMap.put(key, data);
//			}
//
//			rdInfoMarkerSet.getMarkerIdSetMapInteger().clear();
//		}
//
//		return dataSetMap;
//	}
	//</editor-fold>
}
