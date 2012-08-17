package org.gwaspi.reports;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.labels.AbstractXYItemLabelGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.CombinedRangeXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.TextAnchor;

/**
 * ASSOC File columns
 *   CHR     Chromosome
 *   SNP     SNP ID
 *   BP      Physical position (base-pair)
 *   A1      Minor allele name (based on whole sample)
 *   F_A     Frequency of this allele in cases
 *   F_U     Frequency of this allele in controls
 *   A2      Major allele name
 *   CHISQ   Basic allelic test chi-square (1df)
 *   P       Asymptotic p-value for this test
 *   OR      Estimated odds ratio (for A1, i.e. A2 is reference)
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class PlinkReportLoader {

	private static HashMap labelerHM = new HashMap();

	private PlinkReportLoader() {
	}

	public static CombinedRangeXYPlot loadAssocUnadjLogPvsPos(File plinkReport, HashSet redMarkersHS) throws FileNotFoundException, IOException {


		XYSeriesCollection chrData = new XYSeriesCollection();

		NumberAxis sharedAxis = new NumberAxis("-log₁₀(P)");

		CombinedRangeXYPlot combinedPlot = new CombinedRangeXYPlot(sharedAxis);
		combinedPlot.setGap(0);

		XYSeries series1 = null;
		XYSeries series2 = null;

		FileReader inputFileReader = new FileReader(plinkReport);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);


		//Getting data from file and subdividing to series all points by chromosome
		String l;
		String tempChr = "";
		String header = inputBufferReader.readLine();
		int count = 0;
		while ((l = inputBufferReader.readLine()) != null) {

			if (count % 10000 == 0) {
				System.out.println("loadAssocUnadjLogPvsPos -> reader count: " + count);
			}
			count++;

			l = l.trim().replaceAll("\\s+", ",");
			String[] cVals = l.split(",");

			String markerId = cVals[1];
			int position = Integer.parseInt(cVals[2]);
			String s_pVal = cVals[8];

			if (!s_pVal.equals("NA")) {
				double logPValue = Math.abs(Math.log(Double.parseDouble(s_pVal)) / Math.log(10));

				if (cVals[0].toString().equals(tempChr)) {
					if (redMarkersHS.contains(markerId)) {
						series2.add(position, logPValue);
					} else {
						series1.add(position, logPValue);
					}
					labelerHM.put(tempChr + "_" + position, markerId);
				} else {
					if (!tempChr.equals("")) { //SKIP FIRST TIME (NO DATA YET!)
						chrData.addSeries(series1);
						chrData.addSeries(series2);
						appendToCombinedRangePlot(combinedPlot, tempChr, chrData);
					}
					tempChr = cVals[0];
					series1 = new XYSeries("Imputed");
					series2 = new XYSeries("Observed");
					labelerHM.put(tempChr + "_" + position, markerId);

					if (redMarkersHS.contains(markerId)) {
						series2.add(position, logPValue);
					} else {
						series1.add(position, logPValue);
					}
				}
			}
		}
		chrData.addSeries(series1);
		chrData.addSeries(series2);
		appendToCombinedRangePlot(combinedPlot, tempChr, chrData); //ADD LAST CHR TO PLOT
		return combinedPlot;

	}

	private static void appendToCombinedRangePlot(CombinedRangeXYPlot combinedPlot, String chromosome, XYSeriesCollection seriesCol) {
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(false, true);
		renderer.setSeriesPaint(0, Color.blue);

		JFreeChart subchart = ChartFactory.createScatterPlot("",
				"Chr " + chromosome, "", seriesCol, PlotOrientation.VERTICAL, true, false, false);

		XYPlot subplot = (XYPlot) subchart.getPlot();
		subplot.setRenderer(renderer);
		subplot.setBackgroundPaint(null);

		subplot.setDomainGridlineStroke(new BasicStroke(0.0f));
		subplot.setDomainMinorGridlineStroke(new BasicStroke(0.0f));
		subplot.setDomainGridlinePaint(Color.blue);
		subplot.setRangeGridlineStroke(new BasicStroke(0.0f));
		subplot.setRangeMinorGridlineStroke(new BasicStroke(0.0f));
		subplot.setRangeGridlinePaint(Color.blue);


		NumberAxis chrAxis = (NumberAxis) subplot.getDomainAxis();
		chrAxis.setAxisLineVisible(true);
		chrAxis.setTickLabelsVisible(true);
		chrAxis.setTickMarksVisible(true);
		chrAxis.setTickUnit(new NumberTickUnit(10000));
		chrAxis.setAutoRangeIncludesZero(false);

		NumberAxis log10Axis = (NumberAxis) subplot.getRangeAxis();
		log10Axis.setTickMarkInsideLength(2.0f);
		log10Axis.setTickMarkOutsideLength(2.0f);
		log10Axis.setMinorTickCount(2);
		log10Axis.setMinorTickMarksVisible(true);
		log10Axis.setAxisLineVisible(true);
		log10Axis.setAutoRangeIncludesZero(false);

		XYItemRenderer lblRenderer = subplot.getRenderer();
		MySeriesItemLabelGenerator lblGenerator = new MySeriesItemLabelGenerator(4.0d, chromosome);

		lblRenderer.setSeriesItemLabelGenerator(0, lblGenerator);
		lblRenderer.setSeriesItemLabelGenerator(1, lblGenerator);
		lblRenderer.setSeriesItemLabelFont(0, new Font("Serif", Font.PLAIN, 12));
		lblRenderer.setSeriesItemLabelFont(1, new Font("Serif", Font.PLAIN, 12));
		lblRenderer.setSeriesPositiveItemLabelPosition(0, new ItemLabelPosition(ItemLabelAnchor.CENTER,
				TextAnchor.BOTTOM_LEFT,
				TextAnchor.TOP_LEFT,
				-Math.PI / 4.0));
		lblRenderer.setSeriesPositiveItemLabelPosition(1, new ItemLabelPosition(ItemLabelAnchor.CENTER,
				TextAnchor.BOTTOM_LEFT,
				TextAnchor.TOP_LEFT,
				-Math.PI / 4.0));
		lblRenderer.setSeriesItemLabelsVisible(0, true);
		lblRenderer.setSeriesItemLabelsVisible(1, true);


		combinedPlot.add(subplot, 1);
	}

	private static class MySeriesItemLabelGenerator extends AbstractXYItemLabelGenerator
			implements XYItemLabelGenerator {

		private double threshold;
		private String chr;

		/**
		 * Creates a new generator that only displays labels that are greater
		 * than or equal to the threshold value.
		 *
		 * @param threshold the threshold value.
		 */
		MySeriesItemLabelGenerator(double threshold, String chr) {
			this.threshold = threshold;
			this.chr = chr;
		}

		/**
		 * Generates a label for the specified item. The label is typically a
		 * formatted version of the data value, but any text can be used.
		 *
		 * @param dataset the dataset (<code>null</code> not permitted).
		 * @param series the series index (zero-based).
		 * @param category the category index (zero-based).
		 *
		 * @return the label (possibly <code>null</code>).
		 */
		public String generateLabel(XYDataset dataset, int series, int item) {
			String result = null;
			Number value = dataset.getYValue(series, item);
			int position = (int) dataset.getXValue(series, item);
			if (value != null) {
				double v = value.doubleValue();
				if (v > this.threshold) {
					StringBuilder chrPos = new StringBuilder(chr);
					chrPos.append("_");
					chrPos.append(position);
					result = labelerHM.get(chrPos.toString()).toString();

					//result = value.toString().substring(0, 4);  // could apply formatting here
				}
			}
			return result;
		}
	}
}
