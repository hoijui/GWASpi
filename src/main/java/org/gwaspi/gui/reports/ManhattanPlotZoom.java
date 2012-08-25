package org.gwaspi.gui.reports;

import org.gwaspi.constants.cNetCDF;
import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.LinksExternalResouces;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.gwaspi.model.Operation;
import org.gwaspi.model.ReportsList;
import org.gwaspi.netCDF.matrices.MatrixMetadata;
import org.gwaspi.netCDF.operations.OperationMetadata;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.axis.ValueAxis;
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
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

/**
 *
 * @author fernando
 */
public final class ManhattanPlotZoom extends javax.swing.JPanel {

	private static int opId;
	private static Operation op;
	private static OperationMetadata rdOPMetadata;
	public static Map<String, Object> labelerLHM;
	private static MatrixMetadata rdMatrixMetadata;
	public static String origMarkerId;
	public static String origChr;
	public static String currentMarkerId;
	public static String currentChr;
	public static String txt_NRows;
	public static long centerPhysPos;
	public static long startPhysPos;
	public static int defaultMarkerNb = (int) Math.round(100000 * ((double) org.gwaspi.gui.StartGWASpi.maxHeapSize / 2000)); //roughly 2000MB needed per 100.000 plotted markers
	private static long requestedSetSize;
	private static long requestedPosWindow;
	//private int sliderSize;
	private static XYDataset initXYDataset;
	private static JFreeChart zoomChart;
	private static ChartPanel zoomPanel;
	protected static double threshold = 5E-7;
	protected static Color manhattan_back = Color.getHSBColor(0.1f, 0.0f, 0.9f);
	protected static Color manhattan_backalt = Color.getHSBColor(0.1f, 0.0f, 0.85f);
	protected static Color manhattan_dot = Color.blue;
	// Variables declaration - do not modify
	private static javax.swing.JButton btn_Back;
	private static javax.swing.JButton btn_Back2;
	private static javax.swing.JButton btn_Reset;
	private static javax.swing.JButton btn_Save;
	private static javax.swing.JPanel pnl_Chart;
	private static javax.swing.JPanel pnl_ChartNavigator;
	private javax.swing.JPanel pnl_SearchDB;
	private javax.swing.JComboBox cmb_SearchDB;
	private static javax.swing.JPanel pnl_Footer;
	private static javax.swing.JPanel pnl_FooterGroup1;
	//private javax.swing.JPanel pnl_Tracker;
	private static javax.swing.JScrollPane scrl_Chart;
	//private javax.swing.JSlider slid_Tracker;
	// End of variables declaration

	/**
	 * Creates new form ManhattanPlotZoom
	 *
	 * @param _opId
	 * @param _txt_NRows
	 * @param _startIdxPos
	 * @param _requestedSetSize
	 */
	public ManhattanPlotZoom(int _opId,
			String _chr,
			long _startPhysPos,
			long _requestedPosWindow,
			String _txt_NRows) {



//        long start = new Date().getTime();

		opId = _opId;
		currentChr = _chr;
		origChr = _chr;
		txt_NRows = _txt_NRows;
		startPhysPos = _startPhysPos;
		requestedPosWindow = _requestedPosWindow;

		//<editor-fold defaultstate="collapsed" desc="PLOT DEFAULTS">
		try {
			threshold = Double.parseDouble(org.gwaspi.global.Config.getConfigValue("CHART_MANHATTAN_PLOT_THRESHOLD", "5E-7"));

			String[] tmp = org.gwaspi.global.Config.getConfigValue("CHART_MANHATTAN_PLOT_BCKG", "200,200,200").split(",");
			float[] hsbTmp = Color.RGBtoHSB(Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2]), null);
			manhattan_back = Color.getHSBColor(hsbTmp[0], hsbTmp[1], hsbTmp[2]);

			tmp = org.gwaspi.global.Config.getConfigValue("CHART_MANHATTAN_PLOT_DOT", "0,0,255").split(",");
			hsbTmp = Color.RGBtoHSB(Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2]), null);
			manhattan_dot = Color.getHSBColor(hsbTmp[0], hsbTmp[1], hsbTmp[2]);
		} catch (IOException ex) {
			Logger.getLogger(ManhattanPlotZoom.class.getName()).log(Level.SEVERE, null, ex);
		}
		//</editor-fold>

		try {
			op = new Operation(opId);
			rdOPMetadata = new OperationMetadata(opId);
			rdMatrixMetadata = new MatrixMetadata(rdOPMetadata.getParentMatrixId());

//            OperationSet rdAssocMarkerSet = new OperationSet(rdOPMetadata.getStudyId(), opId);
//            labelerLHM = rdAssocMarkerSet.getOpSetLHM();
		} catch (IOException ex) {
			Logger.getLogger(ManhattanPlotZoom.class.getName()).log(Level.SEVERE, null, ex);
		}

		initChart(true);

		setCursor(org.gwaspi.gui.utils.CursorUtils.defaultCursor);

//        long end = new Date().getTime();
//        System.out.println("Time spent zooming: "+(end-start));
//        Runtime runtime = Runtime.getRuntime();
//        long heapMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576;
//        System.out.println("Used Heap Memory:"+ heapMB);
	}

	/**
	 * Creates new form ManhattanPlotZoom
	 *
	 * @param _opId
	 * @param _markerId
	 * @param _centerPhysPos
	 * @param _requestedSetSize
	 */
	public ManhattanPlotZoom(int _opId,
			String _chr,
			String _markerId,
			long _centerPhysPos,
			long _requestedSetSize,
			String _txt_NRows) {

		opId = _opId;
		currentMarkerId = _markerId;
		origMarkerId = _markerId;
		currentChr = _chr;
		origChr = _chr;
		txt_NRows = _txt_NRows;
		centerPhysPos = _centerPhysPos;
		requestedSetSize = _requestedSetSize;

		try {
			op = new Operation(opId);
			rdOPMetadata = new OperationMetadata(opId);
			rdMatrixMetadata = new MatrixMetadata(rdOPMetadata.getParentMatrixId());

//            OperationSet rdAssocMarkerSet = new OperationSet(rdOPMetadata.getStudyId(), opId);
//            labelerLHM = rdAssocMarkerSet.getOpSetLHM();
		} catch (IOException ex) {
			Logger.getLogger(ManhattanPlotZoom.class.getName()).log(Level.SEVERE, null, ex);
		}

		initChart(false);

	}

	public void initChart(boolean usePhysicalPosition) {

		if (usePhysicalPosition) {
			initXYDataset = getXYDataSetByPhysPos(opId,
					origChr,
					startPhysPos,
					requestedPosWindow);

		} else {
			initXYDataset = getXYDataSetByMarkerIdAndPhysPos(opId,
					origChr,
					currentMarkerId,
					centerPhysPos,
					requestedSetSize);
		}

//        slid_Tracker = new javax.swing.JSlider();

		zoomChart = createChart(initXYDataset, currentChr);
		zoomPanel = new ChartPanel(zoomChart);
		zoomPanel.setInitialDelay(10);
		zoomPanel.setDismissDelay(5000);
		zoomPanel.addChartMouseListener(new ChartMouseListener() {
			@Override
			public void chartMouseClicked(ChartMouseEvent event) {
				int mouseX = event.getTrigger().getX();
				int mouseY = event.getTrigger().getY();
				Point2D p = zoomPanel.translateScreenToJava2D(new Point(mouseX, mouseY));
				XYPlot plot = (XYPlot) zoomChart.getPlot();
				ChartRenderingInfo info = zoomPanel.getChartRenderingInfo();
				Rectangle2D dataArea = info.getPlotInfo().getDataArea();

				ValueAxis domainAxis = plot.getDomainAxis();
				RectangleEdge domainAxisEdge = plot.getDomainAxisEdge();
				long chartX = (long) domainAxis.java2DToValue(p.getX(), dataArea, domainAxisEdge);
//				ValueAxis rangeAxis = plot.getRangeAxis();
//				RectangleEdge rangeAxisEdge = plot.getRangeAxisEdge();
//				double chartY = rangeAxis.java2DToValue(p.getY(), dataArea,
//						rangeAxisEdge);
				try {
					if (LinksExternalResouces.checkIfRsNecessary(cmb_SearchDB.getSelectedIndex())) { //THE SELECTED EXTERNAL RESOURCE NEEDS RSID INFO
						String tooltip = zoomPanel.getToolTipText(event.getTrigger());
						if (tooltip == null || tooltip.isEmpty()) { //CHECK IF THERE IS AN RSID
							org.gwaspi.gui.utils.Dialogs.showWarningDialogue(Text.Reports.warnExternalResource);
						} else {
							String rsId = tooltip.substring(6, tooltip.indexOf('<', 6));
							org.gwaspi.gui.utils.URLInDefaultBrowser.browseGenericURL(org.gwaspi.gui.utils.LinksExternalResouces.getResourceLink(cmb_SearchDB.getSelectedIndex(),
									currentChr, // chr
									rsId, // rsId
									chartX) // pos
									);
						}
					} else { // THE SELECTED EXTERNAL RESOURCE ONLY NEEDS CHR+POS INFO
						org.gwaspi.gui.utils.URLInDefaultBrowser.browseGenericURL(org.gwaspi.gui.utils.LinksExternalResouces.getResourceLink(cmb_SearchDB.getSelectedIndex(),
								currentChr, // chr
								"", // rsId
								chartX) // pos
								);
					}
//					org.gwaspi.gui.utils.URLInDefaultBrowser.browseGenericURL(org.gwaspi.gui.utils.LinkEnsemblUrl.getHomoSapiensLink(currentChr, (int) chartX));
				} catch (IOException ex) {
					System.out.println(Text.Reports.cannotOpenEnsembl);
				}
			}

			/**
			 * Receives chart mouse moved events.
			 *
			 * @param event the event.
			 */
			@Override
			public void chartMouseMoved(ChartMouseEvent event) {
				// ignore
			}
		});

		initGUI();
	}

	private void initGUI() {

		setCursor(org.gwaspi.gui.utils.CursorUtils.waitCursor);

		pnl_ChartNavigator = new javax.swing.JPanel();
		pnl_Chart = new javax.swing.JPanel();
		pnl_SearchDB = new javax.swing.JPanel();
		cmb_SearchDB = new javax.swing.JComboBox();
		scrl_Chart = new javax.swing.JScrollPane();
		pnl_Footer = new javax.swing.JPanel();
		pnl_FooterGroup1 = new javax.swing.JPanel();
		btn_Save = new javax.swing.JButton();
		btn_Reset = new javax.swing.JButton();
		btn_Back = new javax.swing.JButton();
		btn_Back2 = new javax.swing.JButton();
//		pnl_Tracker = new javax.swing.JPanel();
//		slid_Tracker = new javax.swing.JSlider();

		String titlePlot = ": " + origMarkerId + " - Chr" + currentChr;
		if (origMarkerId == null) {
			titlePlot = ": Chr" + currentChr + " - Pos: " + startPhysPos + " to " + (startPhysPos + requestedPosWindow);
		}


		pnl_ChartNavigator.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Manhattan Plot Navigator" + titlePlot, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("FreeSans", 1, 18))); // NOI18N

		pnl_Chart.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

		scrl_Chart.getViewport().add(zoomPanel);
		pnl_Chart.add(scrl_Chart, BorderLayout.CENTER);


		// <editor-fold defaultstate="collapsed" desc="LAYOUT1">
		javax.swing.GroupLayout pnl_ChartLayout = new javax.swing.GroupLayout(pnl_Chart);
		pnl_Chart.setLayout(pnl_ChartLayout);
		pnl_ChartLayout.setHorizontalGroup(
				pnl_ChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(scrl_Chart, javax.swing.GroupLayout.DEFAULT_SIZE, 812, Short.MAX_VALUE));
		pnl_ChartLayout.setVerticalGroup(
				pnl_ChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(scrl_Chart, javax.swing.GroupLayout.DEFAULT_SIZE, 437, Short.MAX_VALUE));

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

		String lblChr = "Chr ";
		Long currPos = 0L;
		lblChr += currentChr;
		if (centerPhysPos != Integer.MIN_VALUE) {
			currPos = centerPhysPos;
		} else {
			currPos = (long) Math.round(defaultMarkerNb / 2);
		}

		//<editor-fold defaultstate="collapsed" desc="TRACKER">
//		pnl_Tracker.setBorder(javax.swing.BorderFactory.createTitledBorder("Marker Nº on "+lblChr));
//
//
//		slid_Tracker.setMaximum(max);
//		slid_Tracker.setValue(currPos);
//		slid_Tracker.addMouseListener(new java.awt.event.MouseAdapter() {
//			public void mouseReleased(java.awt.event.MouseEvent evt) {
//				actionSlide();
//			}
//		});
//
//		javax.swing.GroupLayout pnl_TrackerLayout = new javax.swing.GroupLayout(pnl_Tracker);
//		pnl_Tracker.setLayout(pnl_TrackerLayout);
//		pnl_TrackerLayout.setHorizontalGroup(
//				pnl_TrackerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_TrackerLayout.createSequentialGroup()
//				.addGap(18, 18, 18)
//				.addComponent(slid_Tracker, javax.swing.GroupLayout.DEFAULT_SIZE, 814, Short.MAX_VALUE)
//				.addContainerGap())
//				);
//		pnl_TrackerLayout.setVerticalGroup(
//				pnl_TrackerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//				.addGroup(pnl_TrackerLayout.createSequentialGroup()
//				.addContainerGap()
//				.addComponent(slid_Tracker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
//				.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//				);
		//</editor-fold>


		//<editor-fold defaultstate="collapsed" desc="EXTERNAL RESOURCE DBs">
		pnl_SearchDB = new javax.swing.JPanel();
		pnl_SearchDB.setBorder(javax.swing.BorderFactory.createTitledBorder(Text.Reports.externalResourceDB));
		cmb_SearchDB = new javax.swing.JComboBox();
		cmb_SearchDB.setModel(new javax.swing.DefaultComboBoxModel(org.gwaspi.gui.utils.LinksExternalResouces.getLinkNames()));

		javax.swing.GroupLayout pnl_SearchDBLayout = new javax.swing.GroupLayout(pnl_SearchDB);
		pnl_SearchDB.setLayout(pnl_SearchDBLayout);
		pnl_SearchDBLayout.setHorizontalGroup(
				pnl_SearchDBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_SearchDBLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(cmb_SearchDB, 0, 614, Short.MAX_VALUE)
				.addContainerGap()));
		pnl_SearchDBLayout.setVerticalGroup(
				pnl_SearchDBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_SearchDBLayout.createSequentialGroup()
				.addComponent(cmb_SearchDB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addContainerGap(14, Short.MAX_VALUE)));
		//</editor-fold>


		btn_Save.setText("  " + org.gwaspi.global.Text.All.save + "  ");
		btn_Save.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				actionSaveAs(rdOPMetadata.getStudyId());
			}
		});

		btn_Reset.setText("  " + org.gwaspi.global.Text.All.reset + "  ");
		btn_Reset.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				actionReset(evt);
			}
		});

		btn_Back.setText("  " + org.gwaspi.global.Text.Reports.backToTable + "  ");
		btn_Back.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					actionBackToTable(evt);
				} catch (IOException ex) {
				}
			}
		});

		btn_Back2.setText("  " + org.gwaspi.global.Text.Reports.backToManhattanPlot + "  ");
		btn_Back2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					actionBackToManhattanPlot(evt);
				} catch (IOException ex) {
				}
			}
		});

		// <editor-fold defaultstate="collapsed" desc="FOOTER">
		javax.swing.GroupLayout pnl_FooterGroup1Layout = new javax.swing.GroupLayout(pnl_FooterGroup1);
		pnl_FooterGroup1.setLayout(pnl_FooterGroup1Layout);
		pnl_FooterGroup1Layout.setHorizontalGroup(
				pnl_FooterGroup1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_FooterGroup1Layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(btn_Reset, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
				.addGap(18, 18, 18)
				.addComponent(btn_Save, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));


		pnl_FooterGroup1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[]{btn_Reset, btn_Save});

		pnl_FooterGroup1Layout.setVerticalGroup(
				pnl_FooterGroup1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_FooterGroup1Layout.createSequentialGroup()
				.addGroup(pnl_FooterGroup1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
				.addComponent(btn_Save)
				.addComponent(btn_Reset))
				.addContainerGap(16, Short.MAX_VALUE)));


		pnl_FooterGroup1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[]{btn_Reset, btn_Save});


		javax.swing.GroupLayout pnl_FooterLayout = new javax.swing.GroupLayout(pnl_Footer);
		pnl_Footer.setLayout(pnl_FooterLayout);
		pnl_FooterLayout.setHorizontalGroup(
				pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_FooterLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(btn_Back)
				.addGap(18, 18, 18)
				.addComponent(btn_Back2)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 368, Short.MAX_VALUE)
				.addComponent(pnl_FooterGroup1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));
		pnl_FooterLayout.setVerticalGroup(
				pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_FooterLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_FooterLayout.createSequentialGroup()
				.addGroup(pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
				.addComponent(btn_Back)
				.addComponent(btn_Back2))
				.addGap(71, 71, 71))
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_FooterLayout.createSequentialGroup()
				.addComponent(pnl_FooterGroup1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addGap(55, 55, 55)))));
		// </editor-fold>


		// <editor-fold defaultstate="collapsed" desc="LAYOUT">
//		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
//		this.setLayout(layout);
//		layout.setHorizontalGroup(
//				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
//				.addContainerGap()
//				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
//				.addComponent(pnl_Tracker, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//				.addComponent(pnl_ChartNavigator, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//				.addComponent(pnl_Footer, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//				.addContainerGap())
//				);
//		layout.setVerticalGroup(
//				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//				.addGroup(layout.createSequentialGroup()
//				.addComponent(pnl_ChartNavigator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
//				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
//				.addComponent(pnl_Tracker, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
//				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
//				.addComponent(pnl_Footer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
//				.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//				);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(pnl_SearchDB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addComponent(pnl_ChartNavigator, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(pnl_Footer, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addContainerGap()));
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addComponent(pnl_ChartNavigator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_SearchDB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(pnl_Footer, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));

		// </editor-fold>

		setCursor(org.gwaspi.gui.utils.CursorUtils.defaultCursor);
	}

	// <editor-fold defaultstate="collapsed" desc="CHART GENERATOR">
	static XYDataset getXYDataSetByPhysPos(int _opId,
			String _origChr,
			long _startPhysPos,
			long _requestedPosWindow) {

//        System.out.println("getXYDataSetByPhysPos");
//        System.out.println("opId: "+_opId);
//        System.out.println("chr: "+_origChr);
//        System.out.println("startPhysPos: "+_startPhysPos);
//        System.out.println("requestedPosWindow: "+_requestedPosWindow);
//        System.out.println("\n");


		XYDataset xyd = null;
		if (op.getOperationType().equals(cNetCDF.Defaults.OPType.ALLELICTEST.toString())) {
			xyd = org.gwaspi.reports.GenericReportGenerator_opt.getManhattanZoomByChrAndPos(_opId,
					org.gwaspi.constants.cNetCDF.Association.VAR_OP_MARKERS_ASAllelicAssociationTPOR,
					_origChr,
					null,
					_startPhysPos,
					_requestedPosWindow);
		} else if (op.getOperationType().equals(cNetCDF.Defaults.OPType.GENOTYPICTEST.toString())) {
			xyd = org.gwaspi.reports.GenericReportGenerator_opt.getManhattanZoomByChrAndPos(_opId,
					org.gwaspi.constants.cNetCDF.Association.VAR_OP_MARKERS_ASGenotypicAssociationTP2OR,
					_origChr,
					null,
					_startPhysPos,
					_requestedPosWindow);
		} else if (op.getOperationType().equals(cNetCDF.Defaults.OPType.TRENDTEST.toString())) {
			xyd = org.gwaspi.reports.GenericReportGenerator_opt.getManhattanZoomByChrAndPos(_opId,
					org.gwaspi.constants.cNetCDF.Association.VAR_OP_MARKERS_ASTrendTestTP,
					_origChr,
					null,
					_startPhysPos,
					_requestedPosWindow);
		}
		return xyd;
	}

	static XYDataset getXYDataSetByMarkerIdAndPhysPos(int _opId,
			String _origChr,
			String _markerId,
			long _centerPhysPos,
			long _requestedPosWindow) {

//		System.out.println("getXYDataSetByMarkerIdOrIdx");
//		System.out.println("opId: "+_opId);
//		System.out.println("markerId: "+_markerId);
//		System.out.println("indexPosition: "+_startIdxPos);
//		System.out.println("requestedSetSize: "+_requestedSetSize);
//		System.out.println("\n");


		XYDataset xyd = null;
		if (op.getOperationType().equals(cNetCDF.Defaults.OPType.ALLELICTEST.toString())) {
			xyd = org.gwaspi.reports.GenericReportGenerator_opt.getManhattanZoomByChrAndPos(_opId,
					org.gwaspi.constants.cNetCDF.Association.VAR_OP_MARKERS_ASAllelicAssociationTPOR,
					_origChr,
					_markerId,
					_centerPhysPos,
					_requestedPosWindow);
		} else if (op.getOperationType().equals(cNetCDF.Defaults.OPType.GENOTYPICTEST.toString())) {
			xyd = org.gwaspi.reports.GenericReportGenerator_opt.getManhattanZoomByChrAndPos(_opId,
					org.gwaspi.constants.cNetCDF.Association.VAR_OP_MARKERS_ASGenotypicAssociationTP2OR,
					_origChr,
					_markerId,
					_centerPhysPos,
					_requestedPosWindow);
		} else if (op.getOperationType().equals(cNetCDF.Defaults.OPType.TRENDTEST.toString())) {
			xyd = org.gwaspi.reports.GenericReportGenerator_opt.getManhattanZoomByChrAndPos(_opId,
					org.gwaspi.constants.cNetCDF.Association.VAR_OP_MARKERS_ASTrendTestTP,
					_origChr,
					_markerId,
					_centerPhysPos,
					_requestedPosWindow);
		}
		return xyd;
	}

	/**
	 * This getXYDataSetByMarkerIdAndIdx has now been deprecated in favor of
	 * getXYDataSetByMarkerIdAndPhysPos
	 *
	 * @deprecated Use getXYDataSetByMarkerIdAndPhysPos instead
	 */
	static XYDataset getXYDataSetByMarkerIdAndIdx(int _opId,
			String _origChr,
			String _markerId,
			int _centerPhysPos,
			int _requestedSetSize) {

//		System.out.println("getXYDataSetByMarkerIdOrIdx");
//		System.out.println("opId: "+_opId);
//		System.out.println("markerId: "+_markerId);
//		System.out.println("indexPosition: "+_startIdxPos);
//		System.out.println("requestedSetSize: "+_requestedSetSize);
//		System.out.println("\n");


		XYDataset xyd = null;
		if (op.getOperationType().equals(cNetCDF.Defaults.OPType.ALLELICTEST.toString())) {
			xyd = org.gwaspi.reports.GenericReportGenerator_opt.getManhattanZoomByMarkerIdOrIdx(_opId,
					org.gwaspi.constants.cNetCDF.Association.VAR_OP_MARKERS_ASAllelicAssociationTPOR,
					_markerId,
					_centerPhysPos,
					_requestedSetSize);
		} else if (op.getOperationType().equals(cNetCDF.Defaults.OPType.GENOTYPICTEST.toString())) {
			xyd = org.gwaspi.reports.GenericReportGenerator_opt.getManhattanZoomByMarkerIdOrIdx(_opId,
					org.gwaspi.constants.cNetCDF.Association.VAR_OP_MARKERS_ASGenotypicAssociationTP2OR,
					_markerId,
					_centerPhysPos,
					_requestedSetSize);
		} else if (op.getOperationType().equals(cNetCDF.Defaults.OPType.TRENDTEST.toString())) {
			xyd = org.gwaspi.reports.GenericReportGenerator_opt.getManhattanZoomByMarkerIdOrIdx(_opId,
					org.gwaspi.constants.cNetCDF.Association.VAR_OP_MARKERS_ASTrendTestTP,
					_markerId,
					_centerPhysPos,
					_requestedSetSize);
		}
		return xyd;
	}

	private static JFreeChart createChart(XYDataset dataset, String chr) {
		JFreeChart chart = ChartFactory.createScatterPlot(null,
				"",
				"P value",
				dataset,
				PlotOrientation.VERTICAL,
				true, false, false);



		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setNoDataMessage("NO DATA");
		plot.setDomainZeroBaselineVisible(true);
		plot.setRangeZeroBaselineVisible(true);


		// CHART BACKGROUD COLOR
		chart.setBackgroundPaint(Color.getHSBColor(0.1f, 0.1f, 1.0f)); //Hue, saturation, brightness
		plot.setBackgroundPaint(manhattan_back); //Hue, saturation, brightness 9


		// GRIDLINES
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

		// DOTS RENDERER
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
		renderer.setSeriesPaint(0, manhattan_dot);
//		renderer.setSeriesOutlinePaint(0, Color.DARK_GRAY);
//		renderer.setUseOutlinePaint(true);
		// Set dot shape of the currently appended Series
		renderer.setSeriesShape(0, new Rectangle2D.Double(0.0, 0.0, 2, 2));


		renderer.setSeriesVisibleInLegend(0, false);

		NumberAxis positionAxis = (NumberAxis) plot.getDomainAxis();
//		domainAxis.setAutoRangeIncludesZero(false);
//		domainAxis.setTickMarkInsideLength(2.0f);
//		domainAxis.setTickMarkOutsideLength(2.0f);
//		domainAxis.setMinorTickCount(2);
//		domainAxis.setMinorTickMarksVisible(true);
		positionAxis.setLabelAngle(1.0);
		positionAxis.setAutoRangeIncludesZero(false);
		positionAxis.setAxisLineVisible(true);
		positionAxis.setTickLabelsVisible(true);
		positionAxis.setTickMarksVisible(true);


		// ADD INVERSE LOG(10) Y AXIS
		LogAxis logPAxis = new LogAxis("P value");
		logPAxis.setBase(10);
		logPAxis.setInverted(true);
		logPAxis.setNumberFormatOverride(new DecimalFormat("0.#E0#"));

		logPAxis.setTickMarkOutsideLength(2.0f);
		logPAxis.setMinorTickCount(2);
		logPAxis.setMinorTickMarksVisible(true);
		logPAxis.setAxisLineVisible(true);
		logPAxis.setUpperMargin(0);

		TickUnitSource units = NumberAxis.createIntegerTickUnits();
		logPAxis.setStandardTickUnits(units);
		plot.setRangeAxis(0, logPAxis);

		// Add significance Threshold to subplot
		//threshold = 0.5/rdMatrixMetadata.getMarkerSetSize();  //(0.05/10⁶ SNPs => 5*10-⁷)
		final Marker thresholdLine = new ValueMarker(threshold);
		thresholdLine.setPaint(Color.red);
		DecimalFormat df1 = new DecimalFormat("0.#E0#");
		// Add legend to threshold
		thresholdLine.setLabel("P = " + df1.format(threshold));
		thresholdLine.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
		thresholdLine.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
		plot.addRangeMarker(thresholdLine);

		// Marker label if below threshold
		XYItemRenderer lblRenderer = plot.getRenderer();

		// THRESHOLD AND SELECTED LABEL GENERATOR
		MySeriesItemLabelGenerator lblGenerator = new MySeriesItemLabelGenerator(threshold, chr);
		lblRenderer.setSeriesItemLabelGenerator(0, lblGenerator);
		lblRenderer.setSeriesItemLabelFont(0, new Font("SansSerif", Font.PLAIN, 12));
		lblRenderer.setSeriesPositiveItemLabelPosition(0, new ItemLabelPosition(ItemLabelAnchor.CENTER,
				TextAnchor.TOP_LEFT,
				TextAnchor.BOTTOM_LEFT,
				Math.PI / 4.0));

		// TOOLTIP GENERATOR
		MyXYToolTipGenerator tooltipGenerator = new MyXYToolTipGenerator(chr);

		lblRenderer.setBaseToolTipGenerator(tooltipGenerator);

		lblRenderer.setSeriesItemLabelsVisible(0, true);

		return chart;
	}

	private static class MyXYToolTipGenerator extends StandardXYToolTipGenerator
			implements XYToolTipGenerator {

		private DecimalFormat dfSci = new DecimalFormat("0.##E0#");
		private DecimalFormat dfInteger = new DecimalFormat("#");
		private String chr;

		MyXYToolTipGenerator(String _chr) {
			this.chr = _chr;
		}

		@Override
		public String generateToolTip(XYDataset dataset, int series, int item) {
			StringBuilder toolTip = new StringBuilder("<html>");
			double position = dataset.getXValue(series, item);
			double pValue = dataset.getYValue(series, item);

			String chrPos = chr + "_" + dfInteger.format(position);
			if (labelerLHM.containsKey(chrPos)) {
				toolTip.append(labelerLHM.get(chrPos));
				toolTip.append("<br>");
			}

			toolTip.append("pVal: ").append(dfSci.format(pValue));
			toolTip.append("<br>pos: ").append(dfInteger.format(position));
			toolTip.append("</html>");
			return toolTip.toString();
		}
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
		MySeriesItemLabelGenerator(double threshold, String _chr) {
			this.threshold = threshold;
			this.chr = _chr;
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
			Number pValue = dataset.getYValue(series, item);
			int position = (int) dataset.getXValue(series, item);
			if (pValue != null) {
				double pV = pValue.doubleValue();
				StringBuilder chrPos = new StringBuilder(chr);
				chrPos.append("_");
				chrPos.append(position);
				if (pV < this.threshold) {

					rsLabel = labelerLHM.get(chrPos.toString()).toString();

					//result = value.toString().substring(0, 4);  // could apply formatting here
				}
				if (labelerLHM.get(chrPos.toString()).toString().equals(origMarkerId)) {
					rsLabel = labelerLHM.get(chrPos.toString()).toString();
					rsLabel = "◄ " + rsLabel + "";
				}
			}
			return rsLabel;
		}
//		@Override
//		protected Object[] createItemArray(XYDataset dataset, int series, int item){
//			Object[] returnObj = null; //series name, x data value, y data value
//			dataset.get
//
//
//			return returnObj;
//		}
	}

	// </editor-fold>
	//<editor-fold defaultstate="collapsed" desc="HELPERS">
	private static void actionSlide() {
//		if(slid_Tracker.getValue()>=(rdOPMetadata.getOpSetSize()-defaultMarkerNb)){
//			indexPosition=sliderSize-defaultMarkerNb;
//		} else {
//			indexPosition=slid_Tracker.getValue();
//		}
//		org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new ManhattanPlotZoom(opId,
//				 origChr,
//				 origMarkerId,
//				 indexPosition,
//				 defaultMarkerNb,
//				 txt_NRows);
//		org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
	}

	private static void actionReset(ActionEvent evt) {
		org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new org.gwaspi.gui.reports.ManhattanPlotZoom(opId,
				origChr,
				startPhysPos, // startPhysPos
				requestedPosWindow, // physPos window
				txt_NRows);

		org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
	}

	private static void actionSaveAs(int studyId) {
		try {
			File newFile = new File(org.gwaspi.gui.utils.Dialogs.selectDirectoryDialogue(JOptionPane.OK_OPTION).getPath() + "/zoom_" + origMarkerId + ".png");
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

	private static void actionBackToTable(java.awt.event.ActionEvent evt) throws IOException {
		ReportsList rpList = new ReportsList(rdOPMetadata.getOPId(), rdOPMetadata.getParentMatrixId());
		org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new Report_AnalysisPanel(rdOPMetadata.getStudyId(), rdOPMetadata.getParentMatrixId(), rdOPMetadata.getOPId(), txt_NRows);
		org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
	}

	private static void actionBackToManhattanPlot(java.awt.event.ActionEvent evt) throws IOException {
		ManhattanChartDisplay.fired = false;
		ReportsList rpList = new ReportsList(rdOPMetadata.getOPId(), rdOPMetadata.getParentMatrixId());
		String reportFile = "";
		for (int i = 0; i < rpList.reportsListAL.size(); i++) {
			String reportType = rpList.reportsListAL.get(i).getReportType();
			if (reportType.equals(org.gwaspi.constants.cNetCDF.Defaults.OPType.MANHATTANPLOT.toString())) {
				reportFile = rpList.reportsListAL.get(i).getReportFileName();
			}
		}
		org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new ManhattanChartDisplay(rdOPMetadata.getStudyId(), reportFile, rdOPMetadata.getOPId());
		org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
	}
	//</editor-fold>
}
