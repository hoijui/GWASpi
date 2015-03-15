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

package org.gwaspi.gui.reports;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import org.gwaspi.global.Config;
import org.gwaspi.global.Text;
import org.gwaspi.global.Utils;
import org.gwaspi.gui.GWASpiExplorerPanel;
import org.gwaspi.gui.utils.CursorUtils;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.SampleKey;
import org.gwaspi.reports.GenericReportGenerator;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.AbstractXYItemLabelGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SampleQAHetzygPlotZoom extends JPanel {

	private final Logger log
			= LoggerFactory.getLogger(SampleQAHetzygPlotZoom.class);

	public static final String PLOT_SAMPLEQA_HETZYG_THRESHOLD_CONFIG = "CHART_SAMPLEQA_HETZYG_THRESHOLD";
	public static final double PLOT_SAMPLEQA_HETZYG_THRESHOLD_DEFAULT = 0.5;
	public static final String PLOT_SAMPLEQA_MISSING_THRESHOLD_CONFIG = "CHART_SAMPLEQA_MISSING_THRESHOLD";
	public static final double PLOT_SAMPLEQA_MISSING_THRESHOLD_DEFAULT = 0.5;
	private static final Color PLOT_MANHATTAN_BACKGROUND = Color.getHSBColor(0.1f, 0.0f, 0.9f);
	private static final Color PLOT_MANHATTAN_DOTS = Color.red;
	/**
	 * roughly 2000MB needed per 100.000 plotted markers
	 */
	private static final int DEFAULT_NUM_MARKERS // NOTE unused
			= (int) Math.round(100000 * ((double) Config.getSingleton().getInteger(
					Config.PROPERTY_MAX_HEAP_MB, -1) / 2000));

	private final OperationKey operationKey;
	private Map<String, SampleKey> labeler;
	private Double hetzyThreshold;
	private Double missingThreshold;

	/**
	 * Creates new form ManhattanPlotZoom
	 *
	 * @param operationKey
	 */
	public SampleQAHetzygPlotZoom(OperationKey operationKey) throws IOException {

		this.operationKey = operationKey;
		this.hetzyThreshold = 0.015; // XXX Why is it different then PLOT_SAMPLEQA_HETZYG_THRESHOLD_DEFAULT?
		this.missingThreshold = PLOT_SAMPLEQA_MISSING_THRESHOLD_DEFAULT;

		initChart();

		setCursor(CursorUtils.DEFAULT_CURSOR);
	}

	public void initChart() throws IOException {

		hetzyThreshold = Config.getSingleton().getDouble(
				PLOT_SAMPLEQA_HETZYG_THRESHOLD_CONFIG,
				PLOT_SAMPLEQA_HETZYG_THRESHOLD_DEFAULT);
		missingThreshold = Config.getSingleton().getDouble(
				PLOT_SAMPLEQA_MISSING_THRESHOLD_CONFIG,
				PLOT_SAMPLEQA_MISSING_THRESHOLD_DEFAULT);

		final XYDataset initXYDataset = getSampleHetzygDataset(operationKey);
		final JFreeChart zoomChart = createChart(initXYDataset);
		final ChartPanel zoomPanel = new ChartPanel(zoomChart);

		zoomPanel.setInitialDelay(10);
		zoomPanel.setDismissDelay(8000);

		initGUI(zoomChart, zoomPanel);
	}

	private void initGUI(final JFreeChart zoomChart, final ChartPanel zoomPanel) throws IOException {

//		setCursor(CursorUtils.WAIT_CURSOR);

		final JPanel pnl_ChartNavigator = new JPanel();
		final JPanel pnl_Chart = new JPanel();
		final JScrollPane scrl_Chart = new JScrollPane();
		final JPanel pnl_Footer = new JPanel();
		final JPanel pnl_FooterGroup1 = new JPanel();
		final JPanel pnl_FooterGroup0 = new JPanel();
		final JButton btn_Save = new JButton();
		final JButton btn_Reset = new JButton();

		final JLabel lbl_thresholds = new JLabel();
		final JLabel lbl_hetzy = new JLabel();
		final JTextField txt_hetzy = new JTextField();
		final JButton btn_redraw = new JButton();
		final JLabel lbl_missing = new JLabel();
		final JTextField txt_missing = new JTextField();

		final String titlePlot = Text.Reports.smplHetzyVsMissingRat;

		pnl_ChartNavigator.setBorder(GWASpiExplorerPanel.createMainTitledBorder(titlePlot)); // NOI18N

		pnl_Chart.setBorder(GWASpiExplorerPanel.createLineBorder());

		scrl_Chart.getViewport().add(zoomPanel);
		pnl_Chart.add(scrl_Chart, BorderLayout.CENTER);

		// <editor-fold defaultstate="expanded" desc="LAYOUT1">
		GroupLayout pnl_ChartLayout = new GroupLayout(pnl_Chart);
		pnl_Chart.setLayout(pnl_ChartLayout);
		pnl_ChartLayout.setHorizontalGroup(
				pnl_ChartLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(scrl_Chart, GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE));
		pnl_ChartLayout.setVerticalGroup(
				pnl_ChartLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(scrl_Chart, GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE));

		GroupLayout pnl_ChartNavigatorLayout = new GroupLayout(pnl_ChartNavigator);
		pnl_ChartNavigator.setLayout(pnl_ChartNavigatorLayout);
		pnl_ChartNavigatorLayout.setHorizontalGroup(
				pnl_ChartNavigatorLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, pnl_ChartNavigatorLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(pnl_Chart, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addContainerGap()));
		pnl_ChartNavigatorLayout.setVerticalGroup(
				pnl_ChartNavigatorLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_ChartNavigatorLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(pnl_Chart, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addContainerGap()));
		// </editor-fold>

		lbl_thresholds.setText(Text.Reports.thresholds);

		lbl_hetzy.setText(Text.Reports.heterozygosity);
		txt_hetzy.setText(hetzyThreshold.toString());

		lbl_missing.setText(Text.Reports.missRatio);
		txt_missing.setText(missingThreshold.toString());
		btn_redraw.setAction(new RedrawAction(txt_hetzy, txt_missing));

		final MatrixMetadata rdMatrixMetadata
				= MatricesList.getMatrixMetadataById(operationKey.getParentMatrixKey());
		final String originFriendlyName = rdMatrixMetadata.getFriendlyName();
		btn_Save.setAction(new SaveAsAction(
				"SampleQA_hetzyg-missingrat_" + Utils.stripNonAlphaNumeric(originFriendlyName) + ".png",
				scrl_Chart,
				zoomChart,
				this));

		btn_Reset.setAction(new ResetAction());

		//<editor-fold defaultstate="expanded" desc="FOOTER">
		GroupLayout pnl_FooterGroup0Layout = new GroupLayout(pnl_FooterGroup0);
		pnl_FooterGroup0.setLayout(pnl_FooterGroup0Layout);
		pnl_FooterGroup0Layout.setHorizontalGroup(
				pnl_FooterGroup0Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_FooterGroup0Layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_FooterGroup0Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_FooterGroup0Layout.createSequentialGroup()
				.addGroup(pnl_FooterGroup0Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(lbl_hetzy)
				.addComponent(lbl_missing))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(pnl_FooterGroup0Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(txt_hetzy, GroupLayout.PREFERRED_SIZE, 113, GroupLayout.PREFERRED_SIZE)
				.addGroup(pnl_FooterGroup0Layout.createSequentialGroup()
				.addComponent(txt_missing, GroupLayout.PREFERRED_SIZE, 113, GroupLayout.PREFERRED_SIZE)
				.addGap(6, 6, 6)
				.addComponent(btn_redraw, GroupLayout.PREFERRED_SIZE, 103, GroupLayout.PREFERRED_SIZE))))
				.addComponent(lbl_thresholds))
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		pnl_FooterGroup0Layout.setVerticalGroup(
				pnl_FooterGroup0Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, pnl_FooterGroup0Layout.createSequentialGroup()
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(lbl_thresholds)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(pnl_FooterGroup0Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(lbl_hetzy)
				.addComponent(txt_hetzy, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(pnl_FooterGroup0Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
				.addComponent(btn_redraw, GroupLayout.Alignment.TRAILING, 0, 0, Short.MAX_VALUE)
				.addGroup(GroupLayout.Alignment.TRAILING, pnl_FooterGroup0Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(lbl_missing)
				.addComponent(txt_missing)))));

		GroupLayout pnl_FooterGroup1Layout = new GroupLayout(pnl_FooterGroup1);
		pnl_FooterGroup1.setLayout(pnl_FooterGroup1Layout);
		pnl_FooterGroup1Layout.setHorizontalGroup(
				pnl_FooterGroup1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_FooterGroup1Layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(btn_Reset, GroupLayout.PREFERRED_SIZE, 79, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(btn_Save, GroupLayout.PREFERRED_SIZE, 84, GroupLayout.PREFERRED_SIZE)
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		pnl_FooterGroup1Layout.linkSize(SwingConstants.HORIZONTAL, new Component[]{btn_Reset, btn_Save});
		pnl_FooterGroup1Layout.setVerticalGroup(
				pnl_FooterGroup1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_FooterGroup1Layout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(btn_Reset)
				.addComponent(btn_Save)));

		pnl_FooterGroup1Layout.linkSize(SwingConstants.VERTICAL, new Component[]{btn_Reset, btn_Save});

		GroupLayout pnl_FooterLayout = new GroupLayout(pnl_Footer);
		pnl_Footer.setLayout(pnl_FooterLayout);
		pnl_FooterLayout.setHorizontalGroup(
				pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, pnl_FooterLayout.createSequentialGroup()
				.addComponent(pnl_FooterGroup0, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 174, Short.MAX_VALUE)
				.addComponent(pnl_FooterGroup1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));
		pnl_FooterLayout.setVerticalGroup(
				pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, pnl_FooterLayout.createSequentialGroup()
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
				.addComponent(pnl_FooterGroup0, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(pnl_FooterGroup1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addContainerGap()));
		//</editor-fold>

		//<editor-fold defaultstate="expanded" desc="LAYOUT">
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(pnl_ChartNavigator, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(pnl_Footer, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addContainerGap()));
		layout.setVerticalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addComponent(pnl_ChartNavigator, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(pnl_Footer, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));

		//</editor-fold>

//		setCursor(CursorUtils.DEFAULT_CURSOR);
	}

	// <editor-fold defaultstate="expanded" desc="CHART GENERATOR">
	private XYDataset getSampleHetzygDataset(OperationKey operationKey) throws IOException {

		XYDataset xyd = GenericReportGenerator.getSampleHetzygDataset(this, operationKey);
		return xyd;
	}

	private JFreeChart createChart(XYDataset dataset) {
		JFreeChart chart = ChartFactory.createScatterPlot(
				"Heterozygosity vs. Missing Ratio",
				"Heterozygosity Ratio",
				"Missing Ratio",
				dataset,
				PlotOrientation.VERTICAL,
				true, false, false);

		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setNoDataMessage("NO DATA");
		plot.setDomainZeroBaselineVisible(true);
		plot.setRangeZeroBaselineVisible(true);

		// CHART BACKGROUD COLOR
		chart.setBackgroundPaint(Color.getHSBColor(0.1f, 0.1f, 1.0f)); // Hue, saturation, brightness
		plot.setBackgroundPaint(PLOT_MANHATTAN_BACKGROUND); // Hue, saturation, brightness 9

		// GRIDLINES
		plot.setDomainGridlineStroke(new BasicStroke(0.0f));
		plot.setDomainMinorGridlineStroke(new BasicStroke(0.0f));
		plot.setDomainGridlinePaint(PLOT_MANHATTAN_BACKGROUND.darker().darker()); // Hue, saturation, brightness 7
		plot.setDomainMinorGridlinePaint(PLOT_MANHATTAN_BACKGROUND); // Hue, saturation, brightness 9
		plot.setRangeGridlineStroke(new BasicStroke(0.0f));
		plot.setRangeMinorGridlineStroke(new BasicStroke(0.0f));
		plot.setRangeGridlinePaint(PLOT_MANHATTAN_BACKGROUND.darker().darker()); // Hue, saturation, brightness 7
		plot.setRangeMinorGridlinePaint(PLOT_MANHATTAN_BACKGROUND.darker());  // Hue, saturation, brightness 8

		plot.setDomainMinorGridlinesVisible(true);
		plot.setRangeMinorGridlinesVisible(true);

		// DOTS RENDERER
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
		renderer.setSeriesPaint(0, PLOT_MANHATTAN_DOTS);
//		renderer.setSeriesOutlinePaint(0, Color.DARK_GRAY);
//		renderer.setUseOutlinePaint(true);
		// Set dot shape of the currently appended Series
		renderer.setSeriesShape(0, new Rectangle2D.Double(-1, -1, 2, 2));

		renderer.setSeriesVisibleInLegend(0, false);

		// AXIS
		double maxHetzy = 0.005;
		for (int i = 0; i < dataset.getItemCount(0); i++) {
			if (maxHetzy < dataset.getXValue(0, i)) {
				maxHetzy = dataset.getXValue(0, i);
			}
		}
		NumberAxis hetzyAxis = (NumberAxis) plot.getDomainAxis();
		hetzyAxis.setAutoRangeIncludesZero(true);
		hetzyAxis.setAxisLineVisible(true);
		hetzyAxis.setTickLabelsVisible(true);
		hetzyAxis.setTickMarksVisible(true);
		hetzyAxis.setRange(0, maxHetzy * 1.1);

		double maxMissrat = 0.005;
		for (int i = 0; i < dataset.getItemCount(0); i++) {
			if (maxMissrat < dataset.getYValue(0, i)) {
				maxMissrat = dataset.getYValue(0, i);
			}
		}
		NumberAxis missratAxis = (NumberAxis) plot.getRangeAxis();
		missratAxis.setAutoRangeIncludesZero(true);
		missratAxis.setAxisLineVisible(true);
		missratAxis.setTickLabelsVisible(true);
		missratAxis.setTickMarksVisible(true);
		missratAxis.setRange(0, maxMissrat * 1.1);

		// Add significance Threshold to subplot
		final Marker missingThresholdLine = new ValueMarker(missingThreshold);
		missingThresholdLine.setPaint(Color.blue);

		final Marker hetzyThresholdLine = new ValueMarker(hetzyThreshold);
		hetzyThresholdLine.setPaint(Color.blue);

		// Add legend to hetzyThreshold
		hetzyThresholdLine.setLabel("hetzyg. threshold = " + hetzyThreshold);
		missingThresholdLine.setLabel("missing. threshold = " + missingThreshold);
		hetzyThresholdLine.setLabelAnchor(RectangleAnchor.TOP_LEFT);
		hetzyThresholdLine.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
		missingThresholdLine.setLabelAnchor(RectangleAnchor.BOTTOM_LEFT);
		missingThresholdLine.setLabelTextAnchor(TextAnchor.TOP_LEFT);
		plot.addRangeMarker(missingThresholdLine); // THIS FOR MISSING RATIO
		plot.addDomainMarker(hetzyThresholdLine); // THIS FOR HETZY RATIO

		// Marker label if below hetzyThreshold
		XYItemRenderer lblRenderer = plot.getRenderer();

		// THRESHOLD AND SELECTED LABEL GENERATOR
		MySeriesItemLabelGenerator lblGenerator = new MySeriesItemLabelGenerator(hetzyThreshold, missingThreshold);
		lblRenderer.setSeriesItemLabelGenerator(0, lblGenerator);
		lblRenderer.setSeriesItemLabelFont(0, new Font("SansSerif", Font.PLAIN, 10));
		lblRenderer.setSeriesPositiveItemLabelPosition(0, new ItemLabelPosition(ItemLabelAnchor.CENTER,
				TextAnchor.BOTTOM_LEFT,
				TextAnchor.BOTTOM_LEFT,
				2 * Math.PI));

		// TOOLTIP GENERATOR
		MyXYToolTipGenerator tooltipGenerator = new MyXYToolTipGenerator();

		lblRenderer.setBaseToolTipGenerator(tooltipGenerator);

		lblRenderer.setSeriesItemLabelsVisible(0, true);

		return chart;
	}

	private Map<String, SampleKey> getLabelerMap() {
		return labeler;
	}

	public void setLabelerMap(Map<String, SampleKey> labelerMap) {
		this.labeler = labelerMap;
	}

	private class MyXYToolTipGenerator extends StandardXYToolTipGenerator
			implements XYToolTipGenerator
	{
		MyXYToolTipGenerator() {
		}

		@Override
		public String generateToolTip(XYDataset dataset, int series, int item) {
			StringBuilder toolTip = new StringBuilder("<html>");
			double hetzygValue = dataset.getXValue(series, item);
			double missingRatValue = dataset.getYValue(series, item);

			if (!Double.isNaN(hetzygValue) && !Double.isNaN(missingRatValue)) {
				StringBuilder localizer = new StringBuilder();
				localizer.append(missingRatValue);
				localizer.append("_");
				localizer.append(hetzygValue);
				for (Map.Entry<String, SampleKey> entry : getLabelerMap().entrySet()) {
					if (entry.getKey().contains(localizer.toString())) {
						toolTip.append("Sample ID: ").append(entry.getValue().getSampleId());
						toolTip.append(" / Family ID: ").append(entry.getValue().getFamilyId());
						toolTip.append("<br>");
					}
				}
//				if(labelerMap.containsKey(localizer)){
//					toolTip.append(labelerMap.get(localizer));
//					toolTip.append("<br>");
//				}

				toolTip.append("Miss. ratio: ").append(Report_Analysis.FORMAT_SCIENTIFIC.format(missingRatValue));
				toolTip.append("<br>Hetzyg. ratio: ").append(Report_Analysis.FORMAT_SCIENTIFIC.format(hetzygValue));

			}
			toolTip.append("</html>");
			return toolTip.toString();
		}
	}

	private class MySeriesItemLabelGenerator extends AbstractXYItemLabelGenerator
			implements XYItemLabelGenerator {

		private final double hetzygThreshold;
		private final double missingThreshold;

		/**
		 * Creates a new generator that only displays labels that are greater
		 * than or equal to the hetzyThreshold value.
		 *
		 * @param hetzyThreshold the hetzyThreshold value.
		 */
		MySeriesItemLabelGenerator(double hetzygThreshold, double missingThreshold) {
			this.hetzygThreshold = hetzygThreshold;
			this.missingThreshold = missingThreshold;
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
		@Override
		public String generateLabel(XYDataset dataset, int series, int item) {
			String rsLabel = null;
			double hetzygValue = dataset.getXValue(series, item);
			double missingRatValue = dataset.getYValue(series, item);
			if (!Double.isNaN(hetzygValue) && !Double.isNaN(missingRatValue)) {
				StringBuilder localizer = new StringBuilder();
				localizer.append(missingRatValue);
				localizer.append("_");
				localizer.append(hetzygValue);
				if (hetzygValue > this.hetzygThreshold || missingRatValue > this.missingThreshold) {
					for (Map.Entry<String, SampleKey> entry : getLabelerMap().entrySet()) {
						if (entry.getKey().contains(localizer.toString())) {
							rsLabel = entry.getValue().getSampleId();
						}
					}
				}
			}
			return rsLabel;
		}
	}
	// </editor-fold>

	//<editor-fold defaultstate="expanded" desc="HELPERS">
	/**
	 * Validates the threshold and redraws
	 */
	private class RedrawAction extends AbstractAction {

		final JTextField txt_hetzy;
		final JTextField txt_missing;

		RedrawAction(final JTextField txt_hetzy, final JTextField txt_missing) {

			this.txt_hetzy = txt_hetzy;
			this.txt_missing = txt_missing;
			putValue(NAME, Text.Reports.redraw);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {

			try {
				hetzyThreshold = Double.parseDouble(txt_hetzy.getText());
				missingThreshold = Double.parseDouble(txt_missing.getText());
				Config.getSingleton().putDouble("CHART_SAMPLEQA_HETZYG_THRESHOLD", hetzyThreshold);
				Config.getSingleton().putDouble("CHART_SAMPLEQA_MISSING_THRESHOLD", missingThreshold);
				GWASpiExplorerPanel.getSingleton().setPnlContent(new SampleQAHetzygPlotZoom(operationKey));
				GWASpiExplorerPanel.getSingleton().getScrlContent().setViewportView(GWASpiExplorerPanel.getSingleton().getPnlContent());
			} catch (IOException ex) {
				log.warn(Text.App.warnMustBeNumeric, ex);
				Dialogs.showWarningDialogue(Text.App.warnMustBeNumeric);
			}
		}
	}

	private class ResetAction extends AbstractAction {

		ResetAction() {

			putValue(NAME, Text.All.reset);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {

			try {
				GWASpiExplorerPanel.getSingleton().setPnlContent(new SampleQAHetzygPlotZoom(operationKey));
				GWASpiExplorerPanel.getSingleton().getScrlContent().setViewportView(GWASpiExplorerPanel.getSingleton().getPnlContent());
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}

	static class SaveAsAction extends AbstractAction {

		private final Logger log = LoggerFactory.getLogger(SaveAsAction.class);

		private final String newReportFileName;
		private final Component dialogParent;
		private final JScrollPane scrl_Chart;
		private final JFreeChart zoomChart;

		SaveAsAction(
				final String newReportFileName,
				final JScrollPane scrl_Chart,
				final JFreeChart zoomChart,
				final Component dialogParent)
		{
			this.newReportFileName = newReportFileName;
			this.scrl_Chart = scrl_Chart;
			this.zoomChart = zoomChart;
			this.dialogParent = dialogParent;
			putValue(NAME, Text.All.save);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				final File newDir = Dialogs.selectDirectoryDialog(
						Config.PROPERTY_EXPORT_DIR,
						"Choose the new directory for " + newReportFileName,
						dialogParent);
				if (newDir == null) {
					// the user has not choosen a directory to save to
					return;
				}
				final File newFile = new File(newDir, newReportFileName);
				ChartUtilities.saveChartAsPNG(
						newFile,
						zoomChart,
						scrl_Chart.getWidth(),
						scrl_Chart.getHeight());
			} catch (final IOException ex) {
				Dialogs.showWarningDialogue("A plot saving error has occurred");
				log.error("A plot saving error has occurred", ex);
			}
		}
	}
	//</editor-fold>
}
