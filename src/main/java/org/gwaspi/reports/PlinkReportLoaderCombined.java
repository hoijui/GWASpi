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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedRangeXYPlot;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlinkReportLoaderCombined {

	private static final Logger log = LoggerFactory.getLogger(PlinkReportLoaderCombined.class);

	private PlinkReportLoaderCombined() {
	}

	public static CombinedRangeXYPlot loadAssocUnadjLogPvsPos(File plinkReport, Set<String> redMarkers) throws IOException {

		NumberAxis sharedAxis = new NumberAxis("-log₁₀(P)");
		sharedAxis.setTickMarkInsideLength(3.0f);
		CombinedRangeXYPlot combinedPlot = new CombinedRangeXYPlot(sharedAxis);
		combinedPlot.setGap(0);

		XYSeries series1 = null;
		XYSeries series2 = null;
		FileReader inputFileReader = null;
		BufferedReader inputBufferReader = null;
		try {
			inputFileReader = new FileReader(plinkReport);
			inputBufferReader = new BufferedReader(inputFileReader);

			// Getting data from file and subdividing to series all points by chromosome
			String l;
			String tempChr = "";
			// read but ignore the header
			/*String header = */inputBufferReader.readLine();
			int count = 0;
			while ((l = inputBufferReader.readLine()) != null) {
				if (count % 10000 == 0) {
					log.info("loadAssocUnadjLogPvsPos -> reader count: {}", count);
				}
				count++;

				l = l.trim().replaceAll("\\s+", ",");
				String[] cVals = l.split(",");
				String markerId = cVals[1];
				int position = Integer.parseInt(cVals[2]);
				String s_pVal = cVals[8];

				if (!s_pVal.equals("NA")) {
					double pValue = Double.parseDouble(s_pVal); // P value

					if (cVals[0].toString().equals(tempChr)) {
						if (redMarkers.contains(markerId)) { // Insert in alternate color series
							series2.add(position, pValue);
						} else {
							series1.add(position, pValue);
						}

//						series1.add(position, logPValue);
					} else {
						if (!tempChr.isEmpty()) { // Not the first time round!
							XYSeriesCollection tempChrData = new XYSeriesCollection();
							tempChrData.addSeries(series1);
							tempChrData.addSeries(series2);
							appendToCombinedRangePlot(combinedPlot, tempChr, tempChrData, false);
						}

						tempChr = cVals[0];
						series1 = new XYSeries("Imputed");
						series2 = new XYSeries("Observed"); // Alternate color series
						if (redMarkers.contains(markerId)) { // Insert inlternate color series
							series2.add(position, pValue);
						} else {
							series1.add(position, pValue);
						}

//						series1 = new XYSeries(cVals[0]);
//						series1.add(position, logPValue);
					}
				}
			}
			// Append last chromosome to combined plot
			XYSeriesCollection tempChrData = new XYSeriesCollection();
			tempChrData.addSeries(series1);
			tempChrData.addSeries(series2);
			appendToCombinedRangePlot(combinedPlot, tempChr, tempChrData, true);
		} finally {
			try {
				if (inputBufferReader != null) {
					inputBufferReader.close();
				} else if (inputFileReader != null) {
					inputFileReader.close();
				}
			} catch (Exception ex) {
				log.warn(null, ex);
			}
		}

		return combinedPlot;
	}

	private static void appendToCombinedRangePlot(CombinedRangeXYPlot combinedPlot, String chromosome, XYSeriesCollection tempChrData, boolean showlables) {
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(false, true);
		renderer.setSeriesPaint(0, Color.blue);
		renderer.setSeriesPaint(1, Color.red);
		renderer.setSeriesVisibleInLegend(0, showlables);
		renderer.setSeriesVisibleInLegend(1, showlables);
		//renderer.setBaseShape(new Ellipse2D.Float(0, 0, 2,2), false);

		if (combinedPlot.getSubplots().isEmpty()) {
			LogAxis rangeAxis = new LogAxis("P value");
			rangeAxis.setBase(10);
			rangeAxis.setInverted(true);
			rangeAxis.setNumberFormatOverride(GenericReportGenerator.FORMAT_P_VALUE);

			rangeAxis.setTickMarkOutsideLength(2.0f);
			rangeAxis.setMinorTickCount(2);
			rangeAxis.setMinorTickMarksVisible(true);
			rangeAxis.setAxisLineVisible(true);
			rangeAxis.setAutoRangeMinimumSize(0.0000005);
			rangeAxis.setLowerBound(1d);
			//rangeAxis.setAutoRangeIncludesZero(false);

			combinedPlot.setRangeAxis(0, rangeAxis);
		}

		JFreeChart subchart = ChartFactory.createScatterPlot("",
				"Chr " + chromosome,
				"",
				tempChrData,
				PlotOrientation.VERTICAL,
				true,
				false,
				false);

		XYPlot subplot = (XYPlot) subchart.getPlot();
		subplot.setRenderer(renderer);
		subplot.setBackgroundPaint(null);

		final Marker thresholdLine = new ValueMarker(0.0000005);
		thresholdLine.setPaint(Color.red);
		if (showlables) {
			thresholdLine.setLabel("P = 5·10⁻⁷");
		}
		thresholdLine.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
		thresholdLine.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
		subplot.addRangeMarker(thresholdLine);

		NumberAxis chrAxis = (NumberAxis) subplot.getDomainAxis();
		chrAxis.setAxisLineVisible(false);
		chrAxis.setTickLabelsVisible(false);
		chrAxis.setTickMarksVisible(false);
		chrAxis.setAutoRangeIncludesZero(false);
		//combinedPlot.setGap(0);
		combinedPlot.add(subplot, 1);
	}
//<editor-fold defaultstate="expanded" desc="Deprecated">
//	public static List<IdChrPosValuePoint> loadUnadjAssoReportFromFile(File reportFile, int valueColumn) throws FileNotFoundException, IOException {
//		int idColumn=1;
//		int chrColumn=0;
//		int posColumn=2;
//
//		FileReader inputFileReader = new FileReader(reportFile);
//		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);
//
//		List<IdChrPosValuePoint> pointsList = new ArrayList<IdChrPosValuePoint>();
//
//		String l;
//		while ((l = inputBufferReader.readLine()) != null) {
//			String[] cVals = l.split(graphics.Constants.plinkReportSpaceSeparator_regexp);
//			IdChrPosValuePoint temp_genotype = new IdChrPosValuePoint(
//					cVals[idColumn],
//					cVals[chrColumn],
//					Long.parseLong(cVals[posColumn]),
//					Long.parseLong(cVals[valueColumn]));
//			pointsList.add(temp_genotype);
//		}
//
//		//Create comparators for sorting and grouping genotypeList
//		Collections.sort(pointsList, new graphics.IdChrPosValuePoint.AbsolutePositionComparator());
//		return pointsList;
//	}
//</editor-fold>
}
