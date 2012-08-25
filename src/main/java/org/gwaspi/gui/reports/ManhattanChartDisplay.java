package org.gwaspi.gui.reports;

import org.gwaspi.global.Text;
import org.gwaspi.gui.MatrixAnalysePanel;
import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import org.gwaspi.model.Operation;
import org.gwaspi.netCDF.operations.OperationSet;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public final class ManhattanChartDisplay extends javax.swing.JPanel {

	// Variables declaration - do not modify
	private static javax.swing.JPanel pnl_Chart;
	private static javax.swing.JPanel pnl_Footer;
	private static javax.swing.JScrollPane scrl_Chart;
//	private static ManhattanPlotImageLabel label = new ManhattanPlotImageLabel();
	private static JLabel label = new JLabel();
	public static boolean fired;
	private static javax.swing.JButton btn_Save;
	private javax.swing.JButton btn_Back;
	private static int opId;
	private static Map<String, Object> chrSetInfoLHM = new LinkedHashMap<String, Object>();
	private static String chr = "";
	private static int chartWidth = 0;
	private static int chrPlotWidth = 0;
	private static int chrPlotWidthPad = 0;
	private static int padLeft = 64; // Pixel padding to the left of graph
	private static int padGap = 9; // Pixel padding between chromosome plots
	// End of variables declaration

	public ManhattanChartDisplay(final int studyId, final String chartPath, int _opId) {
		fired = false;
		InitManhattanChartDisplay(studyId, chartPath, _opId);
		initChromosmesMap(studyId, chartPath);
	}

	public void InitManhattanChartDisplay(final int studyId, final String chartPath, int _opId) {

		opId = _opId;

		scrl_Chart = new javax.swing.JScrollPane();
		pnl_Chart = new javax.swing.JPanel();
		pnl_Chart.setCursor(org.gwaspi.gui.utils.CursorUtils.handCursor);

		label.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					if (e.getClickCount() == 1 && !fired) {
						fired = true;
						int mouseX = e.getX();
						if (mouseX > padLeft) {
							pnl_Chart.setCursor(org.gwaspi.gui.utils.CursorUtils.waitCursor);

							Object[] selectedSliceInfo = getChrSliceInfo(mouseX);
//							sliceInfo[0] = chrNb;
//							sliceInfo[1] = chr;
//							sliceInfo[2] = sliceNb;
//							sliceInfo[3] = startPhysPos;
//							sliceInfo[4] = defaultSlotsNb;

							org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new org.gwaspi.gui.reports.ManhattanPlotZoom(opId,
									selectedSliceInfo[1].toString(),
									(Long) selectedSliceInfo[3], //startPhysPos
									(Long) selectedSliceInfo[4], //physPos window
									"100");
							org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
						} else {
						}
					}
				} catch (NumberFormatException numberFormatException) {
				}


			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}
		});
		label.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent e) {
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				int mouseX = e.getX();
				if (mouseX > padLeft) {
					Object[] sliceInfo = getChrSliceInfo(mouseX);
//					System.out.println("chrNb: "+sliceInfo[0]);
//					System.out.println("chr: "+sliceInfo[1]);
//					System.out.println("sliceNb: "+sliceInfo[2]);
//					System.out.println("startPhysPos: "+sliceInfo[3]);
//					System.out.println("defaultSlotsNb: "+sliceInfo[4]);

					label.setToolTipText("<html>Zoom on chr " + sliceInfo[1].toString()
							+ "<br>position " + sliceInfo[3] + " to " + ((Long) sliceInfo[3] + (Long) sliceInfo[4])
							+ "</html>");



				} else {
				}
			}
		});

		pnl_Footer = new javax.swing.JPanel();
		btn_Save = new javax.swing.JButton();
		btn_Back = new javax.swing.JButton();

		//<editor-fold defaultstate="collapsed/expanded" desc="">

		btn_Save.setText(org.gwaspi.global.Text.All.save);
		btn_Save.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				actionSaveAs(studyId, chartPath);
			}
		});

		btn_Back.setText(Text.All.Back);
		btn_Back.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					actionBackToTable(evt);
				} catch (IOException ex) {
					Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
				}
			}
		});

		javax.swing.GroupLayout pnl_FooterLayout = new javax.swing.GroupLayout(pnl_Footer);
		pnl_Footer.setLayout(pnl_FooterLayout);
		pnl_FooterLayout.setHorizontalGroup(
				pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_FooterLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(btn_Back, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 431, Short.MAX_VALUE)
				.addComponent(btn_Save, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));
		pnl_FooterLayout.setVerticalGroup(
				pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
				.addComponent(btn_Save)
				.addComponent(btn_Back)));

		//</editor-fold>


		//<editor-fold defaultstate="collapsed" desc="LAYOUT">
		javax.swing.GroupLayout pnl_ChartLayout = new javax.swing.GroupLayout(pnl_Chart);
		pnl_Chart.setLayout(pnl_ChartLayout);
		pnl_ChartLayout.setHorizontalGroup(
				pnl_ChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 819, Short.MAX_VALUE)
				.addGroup(pnl_ChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_ChartLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(scrl_Chart, javax.swing.GroupLayout.DEFAULT_SIZE, 795, Short.MAX_VALUE)
				.addContainerGap())));
		pnl_ChartLayout.setVerticalGroup(
				pnl_ChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 544, Short.MAX_VALUE)
				.addGroup(pnl_ChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_ChartLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(scrl_Chart, javax.swing.GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE)
				.addContainerGap())));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
				.addComponent(pnl_Footer, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(pnl_Chart, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addContainerGap()));
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(pnl_Chart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_Footer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));
		//</editor-fold>

	}

	private static void initChromosmesMap(int studyId, String chartPath) {

		// LOAD MANHATTANPLOT IMAGE
		try {
			String reportPath = org.gwaspi.global.Config.getConfigValue("ReportsDir", "") + "/STUDY_" + studyId + "/";
			File testF = new File(reportPath + chartPath);
			if (testF.exists()) {
				ImageIcon image = new ImageIcon(testF.getPath());

				label.setIcon(image);
				//label.setHorizontalAlignment(SwingConstants.CENTER);

				chartWidth = image.getIconWidth() - padLeft; //Get width of loaded manhattan plot and remove 62 pixels for Y axis labels

				// Creating a Scroll pane component
				scrl_Chart.getViewport().add(label);
				pnl_Chart.add(scrl_Chart, BorderLayout.CENTER);
				pnl_Chart.doLayout();

			}
		} catch (IOException iOException) {
			org.gwaspi.gui.utils.Dialogs.showWarningDialogue(Text.Reports.warnCantOpenFile);
		}

		try {
			OperationSet opSet = new OperationSet(studyId, opId);
			chrSetInfoLHM = opSet.getChrInfoSetLHM();

			// CHECK HOW MANY CHR HAVE PLOTS (ANY MARKERS?)
			int chrPlotNb = 0;
			for (Iterator it = chrSetInfoLHM.keySet().iterator(); it.hasNext();) {
				Object key = it.next();
				int[] chrInfo = (int[]) chrSetInfoLHM.get(key); // Nb of markers, first physical position, last physical position, start index number in MarkerSet,
				if (chrInfo[0] > 0) {
					chrPlotNb++;
				}
			}


			chrPlotWidth = Math.round(
					(chartWidth
					- ((chrPlotNb - 1) * padGap))
					/ chrPlotNb); // CALCULATE THE WIDTH OF 1 CHR PLOT

			chrPlotWidthPad = chrPlotWidth + padGap;

		} catch (IOException ex) {
			Logger.getLogger(ManhattanChartDisplay.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	static Object[] getChrSliceInfo(int pxXpos) {

		int pxXposNoLeftPad = pxXpos - padLeft;

		int[] chrInfo = getChrInfo(pxXposNoLeftPad);  // Nb of markers, first physical position, last physical position, start index number in MarkerSet, placeholder

		int nbMarkers = (Integer) chrInfo[0];
		int startPhysPos = (Integer) chrInfo[1];
		int maxPhysPos = (Integer) chrInfo[2];
		int startIdx = (Integer) chrInfo[3];

		double avgMarkersPerPx = (double) nbMarkers / chrPlotWidth;
		double avgSlotsPerPx = (double) (maxPhysPos - startPhysPos) / chrPlotWidth;

		int defaultSliceWidth;
		if (avgMarkersPerPx < 0) {
			defaultSliceWidth = (int) Math.round(ManhattanPlotZoom.defaultMarkerNb / avgMarkersPerPx); //width of a slice inside current chr
		} else {
			defaultSliceWidth = (int) Math.round(ManhattanPlotZoom.defaultMarkerNb / avgMarkersPerPx); //width of a slice inside current chr
		}
		long defaultSlotsNb = Math.round(defaultSliceWidth * avgSlotsPerPx); //Nb of physical slots per slice

		int chrNb = Math.round(pxXposNoLeftPad / chrPlotWidthPad);
		int pxXRest = pxXposNoLeftPad - (chrNb * chrPlotWidthPad);
		int sliceNb = Math.round(pxXRest / defaultSliceWidth);
		long longStartPhysPos = startPhysPos + (sliceNb * defaultSlotsNb);

		Object[] sliceInfo = new Object[5];
		sliceInfo[0] = chrNb;
		sliceInfo[1] = chr;
		sliceInfo[2] = sliceNb;
		sliceInfo[3] = longStartPhysPos;
		sliceInfo[4] = defaultSlotsNb;

//		System.out.println("getChrSliceInfo");
//		System.out.println("chr: "+chr);
//		System.out.println("sliceInfo[0] - chrNb: "+sliceInfo[0]);
//		System.out.println("sliceInfo[1] - chr: "+sliceInfo[1]);
//		System.out.println("sliceInfo[2] - sliceNb: "+sliceInfo[2]);
//		System.out.println("sliceInfo[3] - startPhysPos: "+sliceInfo[3]);
//		System.out.println("sliceInfo[4] - defaultSlotsNb: "+sliceInfo[4]);
//		System.out.println("\n");

		return sliceInfo;
	}

	static int[] getChrInfo(int pxXposNoLeftPad) {

		int selectedChrMap = Math.round(pxXposNoLeftPad / chrPlotWidthPad);

		int[] chrInfo = new int[4];
		Iterator it = chrSetInfoLHM.keySet().iterator();
		for (int i = 0; i <= selectedChrMap && i < chrSetInfoLHM.size(); i++) {
			Object key = it.next();
			chr = key.toString();
			chrInfo = (int[]) chrSetInfoLHM.get(key); //Nb of markers, first physical position, last physical position, start index number in MarkerSet,
		}

//		System.out.println("getChrInfo");
//		System.out.println("selectedMap: "+selectedChrMap);
//		System.out.println("info[0]: "+chrInfo[0]);
//		System.out.println("info[1]: "+chrInfo[1]);
//		System.out.println("info[2]: "+chrInfo[2]);
//		System.out.println("info[3]: "+chrInfo[3]);
//		System.out.println("\n");

		return chrInfo;
	}

	private void actionSaveAs(int studyId, String chartPath) {
		try {
			String reportPath = org.gwaspi.global.Config.getConfigValue("ReportsDir", "") + "/STUDY_" + studyId + "/";
			File origFile = new File(reportPath + chartPath);
			File newFile = new File(org.gwaspi.gui.utils.Dialogs.selectDirectoryDialogue(JOptionPane.OK_OPTION).getPath() + "/" + chartPath);
			if (origFile.exists()) {
				org.gwaspi.global.Utils.copyFile(origFile, newFile);
			}
		} catch (IOException ex) {
			Logger.getLogger(ManhattanChartDisplay.class.getName()).log(Level.SEVERE, null, ex);
		} catch (Exception ex) {
			//Logger.getLogger(ChartDefaultDisplay.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void actionBackToTable(java.awt.event.ActionEvent evt) throws IOException {
		Operation op = new Operation(opId);
		org.gwaspi.gui.GWASpiExplorerPanel.tree.setSelectionPath(org.gwaspi.gui.GWASpiExplorerPanel.tree.getSelectionPath().getParentPath());
		org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new MatrixAnalysePanel(op.getParentMatrixId(), opId);
		org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
	}
}
