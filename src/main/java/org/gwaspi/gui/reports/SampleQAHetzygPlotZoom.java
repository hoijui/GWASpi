package org.gwaspi.gui.reports;

/**
 *
 * @author fernando
 */
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.gwaspi.model.Operation;
import org.gwaspi.netCDF.matrices.MatrixMetadata;
import org.gwaspi.netCDF.operations.OperationMetadata;

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

public final class SampleQAHetzygPlotZoom extends javax.swing.JPanel {

	private static int opId;
	private static Operation op;
	private static OperationMetadata rdOPMetadata;
	public static LinkedHashMap labelerLHM;
	private static MatrixMetadata rdMatrixMetadata;
	public static String currentMarkerId;
	public static long centerPhysPos;
	public static long startPhysPos;
	public static int defaultMarkerNb = (int) Math.round(100000 * ((double) org.gwaspi.gui.StartGWASpi.maxHeapSize / 2000)); //roughly 2000MB needed per 100.000 plotted markers
	//private int sliderSize;
	private static XYDataset initXYDataset;
	private static JFreeChart zoomChart;
	private static ChartPanel zoomPanel;
	protected static Double hetzyThreshold = 0.015;
	protected static Double missingThreshold = 0.5;
	protected static Color manhattan_back = Color.getHSBColor(0.1f, 0.0f, 0.9f);
	protected static Color manhattan_backalt = Color.getHSBColor(0.1f, 0.0f, 0.85f);
	protected static Color manhattan_dot = Color.red;
	// Variables declaration - do not modify
	private static javax.swing.JButton btn_Reset;
	private static javax.swing.JButton btn_Save;
	private static javax.swing.JPanel pnl_Chart;
	private static javax.swing.JPanel pnl_ChartNavigator;
	private static javax.swing.JPanel pnl_Footer;
	private static javax.swing.JPanel pnl_FooterGroup1;
	private static javax.swing.JPanel pnl_FooterGroup0;
	private static javax.swing.JScrollPane scrl_Chart;
	private static javax.swing.JButton btn_redraw;
	private static javax.swing.JLabel lbl_hetzy;
	private static javax.swing.JLabel lbl_missing;
	private static javax.swing.JLabel lbl_thresholds;
	private static javax.swing.JTextField txt_hetzy;
	private static javax.swing.JTextField txt_missing;
	// End of variables declaration

	/**
	 * Creates new form ManhattanPlotZoom
	 *
	 * @param _opId
	 * @param _txt_NRows
	 * @param _startIdxPos
	 * @param _requestedSetSize
	 */
	public SampleQAHetzygPlotZoom(int _opId) throws IOException {

		opId = _opId;

		try {
			op = new Operation(opId);
			rdOPMetadata = new OperationMetadata(opId);
			rdMatrixMetadata = new MatrixMetadata(rdOPMetadata.getParentMatrixId());
		} catch (IOException ex) {
			Logger.getLogger(SampleQAHetzygPlotZoom.class.getName()).log(Level.SEVERE, null, ex);
		}

		initChart();

		setCursor(org.gwaspi.gui.utils.CursorUtils.defaultCursor);

	}

	public void initChart() throws IOException {

		hetzyThreshold = Double.parseDouble(org.gwaspi.global.Config.getConfigValue("CHART_SAMPLEQA_HETZYG_THRESHOLD", "0.5"));
		missingThreshold = Double.parseDouble(org.gwaspi.global.Config.getConfigValue("CHART_SAMPLEQA_MISSING_THRESHOLD", "0.5"));

		initXYDataset = getSampleHetzygDataset(opId);

		zoomChart = createChart(initXYDataset);
		zoomPanel = new ChartPanel(zoomChart);
		zoomPanel.setInitialDelay(10);
		zoomPanel.setDismissDelay(8000);

		initGUI();
	}

	private void initGUI() {

//        setCursor(org.gwaspi.gui.utils.CursorUtils.waitCursor);

		pnl_ChartNavigator = new javax.swing.JPanel();
		pnl_Chart = new javax.swing.JPanel();
		scrl_Chart = new javax.swing.JScrollPane();
		pnl_Footer = new javax.swing.JPanel();
		pnl_FooterGroup1 = new javax.swing.JPanel();
		pnl_FooterGroup0 = new javax.swing.JPanel();
		btn_Save = new javax.swing.JButton();
		btn_Reset = new javax.swing.JButton();

		lbl_thresholds = new javax.swing.JLabel();
		lbl_hetzy = new javax.swing.JLabel();
		txt_hetzy = new javax.swing.JTextField();
		btn_redraw = new javax.swing.JButton();
		lbl_missing = new javax.swing.JLabel();
		txt_missing = new javax.swing.JTextField();


		String titlePlot = org.gwaspi.global.Text.Reports.smplHetzyVsMissingRat;

		pnl_ChartNavigator.setBorder(javax.swing.BorderFactory.createTitledBorder(null, titlePlot, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("FreeSans", 1, 18))); // NOI18N

		pnl_Chart.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

		scrl_Chart.getViewport().add(zoomPanel);
		pnl_Chart.add(scrl_Chart, BorderLayout.CENTER);


		// <editor-fold defaultstate="collapsed" desc="LAYOUT1">
		javax.swing.GroupLayout pnl_ChartLayout = new javax.swing.GroupLayout(pnl_Chart);
		pnl_Chart.setLayout(pnl_ChartLayout);
		pnl_ChartLayout.setHorizontalGroup(
				pnl_ChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(scrl_Chart, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE));
		pnl_ChartLayout.setVerticalGroup(
				pnl_ChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(scrl_Chart, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE));

		javax.swing.GroupLayout pnl_ChartNavigatorLayout = new javax.swing.GroupLayout(pnl_ChartNavigator);
		pnl_ChartNavigator.setLayout(pnl_ChartNavigatorLayout);
		pnl_ChartNavigatorLayout.setHorizontalGroup(
				pnl_ChartNavigatorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_ChartNavigatorLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(pnl_Chart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addContainerGap()));
		pnl_ChartNavigatorLayout.setVerticalGroup(
				pnl_ChartNavigatorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_ChartNavigatorLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(pnl_Chart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addContainerGap()));

		// </editor-fold>


		lbl_thresholds.setText(org.gwaspi.global.Text.Reports.thresholds);

		lbl_hetzy.setText(org.gwaspi.global.Text.Reports.heterozygosity);
		txt_hetzy.setText(hetzyThreshold.toString());

		lbl_missing.setText(org.gwaspi.global.Text.Reports.missRatio);
		txt_missing.setText(missingThreshold.toString());
		btn_redraw.setText(org.gwaspi.global.Text.Reports.redraw);
		btn_redraw.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				validateThresholdAndRedraw();
			}
		});

		btn_Save.setText("  " + org.gwaspi.global.Text.All.save + "  ");
		btn_Save.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				actionSaveAs(rdOPMetadata.getStudyId());
			}
		});

		btn_Reset.setText("  " + org.gwaspi.global.Text.All.reset + "  ");
		btn_Reset.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					actionReset(evt);
				} catch (IOException ex) {
					Logger.getLogger(SampleQAHetzygPlotZoom.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		});


		// <editor-fold defaultstate="collapsed" desc="FOOTER">
		javax.swing.GroupLayout pnl_FooterGroup0Layout = new javax.swing.GroupLayout(pnl_FooterGroup0);
		pnl_FooterGroup0.setLayout(pnl_FooterGroup0Layout);
		pnl_FooterGroup0Layout.setHorizontalGroup(
				pnl_FooterGroup0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_FooterGroup0Layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_FooterGroup0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_FooterGroup0Layout.createSequentialGroup()
				.addGroup(pnl_FooterGroup0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(lbl_hetzy)
				.addComponent(lbl_missing))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(pnl_FooterGroup0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(txt_hetzy, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addGroup(pnl_FooterGroup0Layout.createSequentialGroup()
				.addComponent(txt_missing, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addGap(6, 6, 6)
				.addComponent(btn_redraw, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE))))
				.addComponent(lbl_thresholds))
				.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		pnl_FooterGroup0Layout.setVerticalGroup(
				pnl_FooterGroup0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_FooterGroup0Layout.createSequentialGroup()
				.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(lbl_thresholds)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(pnl_FooterGroup0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
				.addComponent(lbl_hetzy)
				.addComponent(txt_hetzy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(pnl_FooterGroup0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
				.addComponent(btn_redraw, javax.swing.GroupLayout.Alignment.TRAILING, 0, 0, Short.MAX_VALUE)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_FooterGroup0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
				.addComponent(lbl_missing)
				.addComponent(txt_missing)))));

		javax.swing.GroupLayout pnl_FooterGroup1Layout = new javax.swing.GroupLayout(pnl_FooterGroup1);
		pnl_FooterGroup1.setLayout(pnl_FooterGroup1Layout);
		pnl_FooterGroup1Layout.setHorizontalGroup(
				pnl_FooterGroup1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_FooterGroup1Layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(btn_Reset, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(btn_Save, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		pnl_FooterGroup1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[]{btn_Reset, btn_Save});
		pnl_FooterGroup1Layout.setVerticalGroup(
				pnl_FooterGroup1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_FooterGroup1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
				.addComponent(btn_Reset)
				.addComponent(btn_Save)));


		pnl_FooterGroup1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[]{btn_Reset, btn_Save});


		javax.swing.GroupLayout pnl_FooterLayout = new javax.swing.GroupLayout(pnl_Footer);
		pnl_Footer.setLayout(pnl_FooterLayout);
		pnl_FooterLayout.setHorizontalGroup(
				pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_FooterLayout.createSequentialGroup()
				.addComponent(pnl_FooterGroup0, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 174, Short.MAX_VALUE)
				.addComponent(pnl_FooterGroup1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));
		pnl_FooterLayout.setVerticalGroup(
				pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_FooterLayout.createSequentialGroup()
				.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
				.addComponent(pnl_FooterGroup0, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addComponent(pnl_FooterGroup1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
				.addContainerGap()));
		// </editor-fold>


		// <editor-fold defaultstate="collapsed" desc="LAYOUT">
		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(pnl_ChartNavigator, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(pnl_Footer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addContainerGap()));
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addComponent(pnl_ChartNavigator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(pnl_Footer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));

		// </editor-fold>

//        setCursor(org.gwaspi.gui.utils.CursorUtils.defaultCursor);
	}

	// <editor-fold defaultstate="collapsed" desc="CHART GENERATOR">
	protected static XYDataset getSampleHetzygDataset(int _opId) throws IOException {

		XYDataset xyd = org.gwaspi.reports.GenericReportGenerator_opt.getSampleHetzygDataset(opId);
		return xyd;
	}

	private static JFreeChart createChart(XYDataset dataset) {
		JFreeChart chart = ChartFactory.createScatterPlot("Heterozygosity vs. Missing Ratio",
				"Heterozygosity Ratio",
				"Missing Ratio",
				dataset,
				PlotOrientation.VERTICAL,
				true, false, false);

		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setNoDataMessage("NO DATA");
		plot.setDomainZeroBaselineVisible(true);
		plot.setRangeZeroBaselineVisible(true);


		//CHART BACKGROUD COLOR
		chart.setBackgroundPaint(Color.getHSBColor(0.1f, 0.1f, 1.0f)); //Hue, saturation, brightness
		plot.setBackgroundPaint(manhattan_back); //Hue, saturation, brightness 9


		//GRIDLINES
		plot.setDomainGridlineStroke(new BasicStroke(0.0f));
		plot.setDomainMinorGridlineStroke(new BasicStroke(0.0f));
		plot.setDomainGridlinePaint(manhattan_back.darker().darker()); //Hue, saturation, brightness 7
		plot.setDomainMinorGridlinePaint(manhattan_back); //Hue, saturation, brightness 9
		plot.setRangeGridlineStroke(new BasicStroke(0.0f));
		plot.setRangeMinorGridlineStroke(new BasicStroke(0.0f));
		plot.setRangeGridlinePaint(manhattan_back.darker().darker()); //Hue, saturation, brightness 7
		plot.setRangeMinorGridlinePaint(manhattan_back.darker());  //Hue, saturation, brightness 8

		plot.setDomainMinorGridlinesVisible(true);
		plot.setRangeMinorGridlinesVisible(true);

		//DOTS RENDERER
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
		renderer.setSeriesPaint(0, manhattan_dot);
//        renderer.setSeriesOutlinePaint(0, Color.DARK_GRAY);
//        renderer.setUseOutlinePaint(true);
		//Set dot shape of the currently appended Series
		renderer.setSeriesShape(0, new Rectangle2D.Double(-1, -1, 2, 2));


		renderer.setSeriesVisibleInLegend(0, false);

		//AXIS
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


		//Add significance Threshold to subplot
		final Marker missingThresholdLine = new ValueMarker(missingThreshold);
		missingThresholdLine.setPaint(Color.blue);

		final Marker hetzyThresholdLine = new ValueMarker(hetzyThreshold);
		hetzyThresholdLine.setPaint(Color.blue);

		//Add legend to hetzyThreshold
		hetzyThresholdLine.setLabel("hetzyg. threshold = " + hetzyThreshold);
		missingThresholdLine.setLabel("missing. threshold = " + missingThreshold);
		hetzyThresholdLine.setLabelAnchor(RectangleAnchor.TOP_LEFT);
		hetzyThresholdLine.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
		missingThresholdLine.setLabelAnchor(RectangleAnchor.BOTTOM_LEFT);
		missingThresholdLine.setLabelTextAnchor(TextAnchor.TOP_LEFT);
		plot.addRangeMarker(missingThresholdLine);   //THIS FOR MISSING RATIO
		plot.addDomainMarker(hetzyThresholdLine);    //THIS FOR HETZY RATIO

		//Marker label if below hetzyThreshold
		XYItemRenderer lblRenderer = plot.getRenderer();

		//THRESHOLD AND SELECTED LABEL GENERATOR
		MySeriesItemLabelGenerator lblGenerator = new MySeriesItemLabelGenerator(hetzyThreshold, missingThreshold);
		lblRenderer.setSeriesItemLabelGenerator(0, lblGenerator);
		lblRenderer.setSeriesItemLabelFont(0, new Font("SansSerif", Font.PLAIN, 10));
		lblRenderer.setSeriesPositiveItemLabelPosition(0, new ItemLabelPosition(ItemLabelAnchor.CENTER,
				TextAnchor.BOTTOM_LEFT,
				TextAnchor.BOTTOM_LEFT,
				2 * Math.PI));

		//TOOLTIP GENERATOR
		MyXYToolTipGenerator tooltipGenerator = new MyXYToolTipGenerator();

		lblRenderer.setBaseToolTipGenerator(tooltipGenerator);

		lblRenderer.setSeriesItemLabelsVisible(0, true);

		return chart;
	}

	static class MyXYToolTipGenerator extends StandardXYToolTipGenerator
			implements XYToolTipGenerator {

		DecimalFormat dfSci = new DecimalFormat("0.##E0#");
		DecimalFormat dfInteger = new DecimalFormat("#");

		public MyXYToolTipGenerator() {
		}

		@Override
		public String generateToolTip(XYDataset dataset, int series, int item) {
			StringBuilder toolTip = new StringBuilder("<html>");
			double hetzygValue = dataset.getXValue(series, item);
			double missingRatValue = dataset.getYValue(series, item);

			if (hetzygValue != Double.NaN && missingRatValue != Double.NaN) {
				StringBuilder localizer = new StringBuilder();
				localizer.append(missingRatValue);
				localizer.append("_");
				localizer.append(hetzygValue);
				for (Iterator it = labelerLHM.keySet().iterator(); it.hasNext();) {
					Object key = it.next();
					if (key.toString().contains(localizer.toString())) {
						toolTip.append("Sample ID: ").append(labelerLHM.get(key).toString());
						toolTip.append("<br>");
					}
				}
//            if(labelerLHM.containsKey(localizer)){
//                toolTip.append(labelerLHM.get(localizer));
//                toolTip.append("<br>");
//            }

				toolTip.append("Miss. ratio: ").append(dfSci.format(missingRatValue));
				toolTip.append("<br>Hetzyg. ratio: ").append(dfSci.format(hetzygValue));

			}
			toolTip.append("</html>");
			return toolTip.toString();
		}
	}

	static class MySeriesItemLabelGenerator extends AbstractXYItemLabelGenerator
			implements XYItemLabelGenerator {

		private double hetzygThreshold;
		private double missingThreshold;

		/**
		 * Creates a new generator that only displays labels that are greater
		 * than or equal to the hetzyThreshold value.
		 *
		 * @param hetzyThreshold the hetzyThreshold value.
		 */
		public MySeriesItemLabelGenerator(double hetzygThreshold, double missingThreshold) {
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
			if (hetzygValue != Double.NaN && missingRatValue != Double.NaN) {
				StringBuilder localizer = new StringBuilder();
				localizer.append(missingRatValue);
				localizer.append("_");
				localizer.append(hetzygValue);
				if (hetzygValue > this.hetzygThreshold || missingRatValue > this.missingThreshold) {
					for (Iterator it = labelerLHM.keySet().iterator(); it.hasNext();) {
						Object key = it.next();
						if (key.toString().contains(localizer.toString())) {
							rsLabel = labelerLHM.get(key).toString();
						}
					}
				}
			}
			return rsLabel;
		}
	}

	// </editor-fold>
	//<editor-fold defaultstate="collapsed" desc="HELPERS">
	private void validateThresholdAndRedraw() {
		try {
			hetzyThreshold = Double.parseDouble(txt_hetzy.getText());
			missingThreshold = Double.parseDouble(txt_missing.getText());
			org.gwaspi.global.Config.setConfigValue("CHART_SAMPLEQA_HETZYG_THRESHOLD", hetzyThreshold.toString());
			org.gwaspi.global.Config.setConfigValue("CHART_SAMPLEQA_MISSING_THRESHOLD", missingThreshold.toString());
			org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new SampleQAHetzygPlotZoom(opId);
			org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
		} catch (Exception e) {
			org.gwaspi.gui.utils.Dialogs.showWarningDialogue(org.gwaspi.global.Text.App.warnMustBeNumeric);
		}


	}

	private static void actionReset(ActionEvent evt) throws IOException {
		org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new org.gwaspi.gui.reports.SampleQAHetzygPlotZoom(opId);

		org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
	}

	private static void actionSaveAs(int studyId) {
		try {
			File newFile = new File(org.gwaspi.gui.utils.Dialogs.selectDirectoryDialogue(JOptionPane.OK_OPTION).getPath() + "/SampleQA_hetzyg-missingrat_" + org.gwaspi.global.Utils.stripNonAlphaNumeric(rdMatrixMetadata.getMatrixFriendlyName()) + ".png");
			ChartUtilities.saveChartAsPNG(newFile, zoomChart, scrl_Chart.getWidth(), scrl_Chart.getHeight());
		} catch (IOException ex) {
			Logger.getLogger(ChartDefaultDisplay.class.getName()).log(Level.SEVERE, null, ex);
		} catch (NullPointerException ex) {
			//gui.utils.Dialogs.showWarningDialogue("A table saving error has occurred");
			//Logger.getLogger(ChartDefaultDisplay.class.getName()).log(Level.SEVERE, null, ex);
		} catch (Exception ex) {
			Logger.getLogger(ChartDefaultDisplay.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	//</editor-fold>
}
