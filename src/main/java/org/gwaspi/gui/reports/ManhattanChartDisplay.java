package org.gwaspi.gui.reports;

import org.gwaspi.global.Config;
import org.gwaspi.global.Text;
import org.gwaspi.global.Utils;
import org.gwaspi.gui.GWASpiExplorerPanel;
import org.gwaspi.gui.MatrixAnalysePanel;
import org.gwaspi.gui.utils.CursorUtils;
import org.gwaspi.gui.utils.Dialogs;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;
import org.gwaspi.model.Operation;
import org.gwaspi.netCDF.operations.OperationSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public final class ManhattanChartDisplay extends JPanel {

	private final static Logger log
			= LoggerFactory.getLogger(ManhattanChartDisplay.class);

	// Variables declaration - do not modify
	private static JPanel pnl_Chart;
	private static JPanel pnl_Footer;
	private static JScrollPane scrl_Chart;
//	private static ManhattanPlotImageLabel label = new ManhattanPlotImageLabel();
	private static JLabel label = new JLabel();
	public static boolean fired;
	private static JButton btn_Save;
	private JButton btn_Back;
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

		scrl_Chart = new JScrollPane();
		pnl_Chart = new JPanel();
		pnl_Chart.setCursor(CursorUtils.handCursor);

		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					if (e.getClickCount() == 1 && !fired) {
						fired = true;
						int mouseX = e.getX();
						if (mouseX > padLeft) {
							pnl_Chart.setCursor(CursorUtils.waitCursor);

							Object[] selectedSliceInfo = getChrSliceInfo(mouseX);
//							sliceInfo[0] = chrNb;
//							sliceInfo[1] = chr;
//							sliceInfo[2] = sliceNb;
//							sliceInfo[3] = startPhysPos;
//							sliceInfo[4] = defaultSlotsNb;

							GWASpiExplorerPanel.pnl_Content = new ManhattanPlotZoom(opId,
									selectedSliceInfo[1].toString(),
									(Long) selectedSliceInfo[3], //startPhysPos
									(Long) selectedSliceInfo[4], //physPos window
									"100");
							GWASpiExplorerPanel.scrl_Content.setViewportView(GWASpiExplorerPanel.pnl_Content);
						} else {
						}
					}
				} catch (NumberFormatException numberFormatException) {
				}
			}
		});
		label.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				int mouseX = e.getX();
				if (mouseX > padLeft) {
					Object[] sliceInfo = getChrSliceInfo(mouseX);

					label.setToolTipText("<html>Zoom on chr " + sliceInfo[1].toString()
							+ "<br>position " + sliceInfo[3] + " to " + ((Long) sliceInfo[3] + (Long) sliceInfo[4])
							+ "</html>");
				}
			}
		});

		pnl_Footer = new JPanel();
		btn_Save = new JButton();
		btn_Back = new JButton();

		//<editor-fold defaultstate="collapsed/expanded" desc="">
		btn_Save.setAction(new SaveAsAction(studyId, chartPath));

		btn_Back.setAction(new BackToTableAction());

		GroupLayout pnl_FooterLayout = new GroupLayout(pnl_Footer);
		pnl_Footer.setLayout(pnl_FooterLayout);
		pnl_FooterLayout.setHorizontalGroup(
				pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, pnl_FooterLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(btn_Back, GroupLayout.PREFERRED_SIZE, 92, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 431, Short.MAX_VALUE)
				.addComponent(btn_Save, GroupLayout.PREFERRED_SIZE, 125, GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));
		pnl_FooterLayout.setVerticalGroup(
				pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(btn_Save)
				.addComponent(btn_Back)));
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="LAYOUT">
		GroupLayout pnl_ChartLayout = new GroupLayout(pnl_Chart);
		pnl_Chart.setLayout(pnl_ChartLayout);
		pnl_ChartLayout.setHorizontalGroup(
				pnl_ChartLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGap(0, 819, Short.MAX_VALUE)
				.addGroup(pnl_ChartLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_ChartLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(scrl_Chart, GroupLayout.DEFAULT_SIZE, 795, Short.MAX_VALUE)
				.addContainerGap())));
		pnl_ChartLayout.setVerticalGroup(
				pnl_ChartLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGap(0, 544, Short.MAX_VALUE)
				.addGroup(pnl_ChartLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_ChartLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(scrl_Chart, GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE)
				.addContainerGap())));

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
				.addComponent(pnl_Footer, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(pnl_Chart, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addContainerGap()));
		layout.setVerticalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(pnl_Chart, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_Footer, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));
		//</editor-fold>
	}

	private static void initChromosmesMap(int studyId, String chartPath) {

		// LOAD MANHATTANPLOT IMAGE
		try {
			String reportPath = Config.getConfigValue("ReportsDir", "") + "/STUDY_" + studyId + "/";
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
			Dialogs.showWarningDialogue(Text.Reports.warnCantOpenFile);
		}

		try {
			OperationSet opSet = new OperationSet(studyId, opId);
			chrSetInfoLHM = opSet.getChrInfoSetLHM();

			// CHECK HOW MANY CHR HAVE PLOTS (ANY MARKERS?)
			int chrPlotNb = 0;
			for (Object value : chrSetInfoLHM.values()) {
				int[] chrInfo = (int[]) value; // Nb of markers, first physical position, last physical position, start index number in MarkerSet,
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
			log.error(null, ex);
		}
	}

	private static Object[] getChrSliceInfo(int pxXpos) {

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

		return sliceInfo;
	}

	private static int[] getChrInfo(int pxXposNoLeftPad) {

		int selectedChrMap = Math.round(pxXposNoLeftPad / chrPlotWidthPad);

		int[] chrInfo = new int[4];
		int i = 0;
		for (Map.Entry<String, Object> entry : chrSetInfoLHM.entrySet()) {
			if ((i > selectedChrMap) || (i >= chrSetInfoLHM.size())) {
				break;
			}
			chr = entry.getKey();
			chrInfo = (int[]) entry.getValue(); //Nb of markers, first physical position, last physical position, start index number in MarkerSet,
			i++;
		}

		return chrInfo;
	}

	private static class SaveAsAction extends AbstractAction {

		private int studyId;
		private String chartPath;

		SaveAsAction(int studyId, String chartPath) {

			this.studyId = studyId;
			this.chartPath = chartPath;
			putValue(NAME, Text.All.save);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				String reportPath = Config.getConfigValue("ReportsDir", "") + "/STUDY_" + studyId + "/";
				File origFile = new File(reportPath + chartPath);
				File newFile = new File(Dialogs.selectDirectoryDialog(JOptionPane.OK_OPTION).getPath() + "/" + chartPath);
				if (origFile.exists()) {
					Utils.copyFile(origFile, newFile);
				}
			} catch (IOException ex) {
				log.error(null, ex);
			} catch (Exception ex) {
				//log.error(null, ex);
			}
		}
	}

	private static class BackToTableAction extends AbstractAction {

		BackToTableAction() {

			putValue(NAME, Text.All.Back);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				Operation op = new Operation(opId);
				GWASpiExplorerPanel.tree.setSelectionPath(GWASpiExplorerPanel.tree.getSelectionPath().getParentPath());
				GWASpiExplorerPanel.pnl_Content = new MatrixAnalysePanel(op.getParentMatrixId(), opId);
				GWASpiExplorerPanel.scrl_Content.setViewportView(GWASpiExplorerPanel.pnl_Content);
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}
}
