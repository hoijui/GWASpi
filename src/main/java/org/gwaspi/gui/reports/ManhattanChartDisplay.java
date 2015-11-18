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

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;
import org.gwaspi.global.Text;
import org.gwaspi.gui.BackAction;
import org.gwaspi.gui.GWASpiExplorerPanel;
import org.gwaspi.gui.utils.CursorUtils;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.model.ChromosomeInfo;
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.Study;
import org.gwaspi.model.StudyKey;
import org.gwaspi.operations.OperationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ManhattanChartDisplay extends JPanel {

	private static final Logger log
			= LoggerFactory.getLogger(ManhattanChartDisplay.class);

	// Variables declaration - do not modify
	private final JPanel pnl_Chart;
	private final JPanel pnl_Footer;
	private final JScrollPane scrl_Chart;
	private final JLabel label;
	private final JButton btn_Save;
	private final JButton btn_Back;
	private boolean fired;
	private List<ChromosomeKey> chromosomeKeys;
	private List<ChromosomeInfo> chromosomeInfos;
	private int chartWidth = 0;
	private int chrPlotWidth = 0;
	private int chrPlotWidthPad = 0;
	private static final int padLeft = 64; // Pixel padding to the left of graph
	private static final int padGap = 9; // Pixel padding between chromosome plots
	// End of variables declaration

	public ManhattanChartDisplay(final String chartPath, final OperationKey operationKey) {

		this.label = new JLabel();
		this.chromosomeKeys = null;
		this.chromosomeInfos = null;
		this.fired = false;

		final StudyKey studyKey;
		if ((operationKey != null) && (operationKey.getId() != OperationKey.NULL_ID)) {
			studyKey = operationKey.getParentMatrixKey().getStudyKey();
		} else {
			studyKey = new StudyKey(1);
		}

		scrl_Chart = new JScrollPane();
		pnl_Chart = new JPanel();
		pnl_Chart.setCursor(CursorUtils.HAND_CURSOR);

		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent evt) {
				try {
					if (evt.getClickCount() == 1 && !isFired()) {
						setFired(true);
						int mouseX = evt.getX();
						if (mouseX > padLeft) {
							pnl_Chart.setCursor(CursorUtils.WAIT_CURSOR);

							Object[] selectedSliceInfo = getChrSliceInfo(mouseX);

							GWASpiExplorerPanel.getSingleton().setPnlContent(new ManhattanPlotZoom(
									ManhattanChartDisplay.this,
									operationKey,
									(ChromosomeKey) selectedSliceInfo[1],
									(Long) selectedSliceInfo[3], // startPhysPos
									(Long) selectedSliceInfo[4], // physPos window
									100));
							GWASpiExplorerPanel.getSingleton().getScrlContent().setViewportView(GWASpiExplorerPanel.getSingleton().getPnlContent());
						}
					}
				} catch (NumberFormatException ex) {
					log.warn(null, ex);
				}
			}
		});
		label.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(final MouseEvent evt) {
				int mouseX = evt.getX();
				if (mouseX > padLeft) {
					Object[] sliceInfo = getChrSliceInfo(mouseX);

					label.setToolTipText("<html>Zoom on chr " + ((ChromosomeKey) sliceInfo[1]).getChromosome()
							+ "<br>position " + sliceInfo[3] + " to " + ((Long) sliceInfo[3] + (Long) sliceInfo[4])
							+ "</html>");
				}
			}
		});

		pnl_Footer = new JPanel();
		btn_Save = new JButton();
		btn_Back = new JButton();

		//<editor-fold defaultstate="expanded" desc="">
		btn_Save.setAction(new ChartDefaultDisplay.SaveAsAction(studyKey, chartPath, this));

		btn_Back.setAction(new BackAction(new DataSetKey(operationKey)));

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

		//<editor-fold defaultstate="expanded" desc="LAYOUT">
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

		initChromosmesMap(studyKey, chartPath, operationKey);
	}

	private void initChromosmesMap(StudyKey studyKey, String chartPath, final OperationKey operationKey) {

		// LOAD MANHATTANPLOT IMAGE
		try {
			String reportPath = Study.constructReportsPath(studyKey);
			File testF = new File(reportPath + chartPath);
			if (testF.exists()) {
				Icon image = new ImageIcon(testF.getPath());
				label.setIcon(image);
				chartWidth = image.getIconWidth() - padLeft; // Get width of loaded manhattan plot and remove 62 pixels for Y axis labels
				// Creating a Scroll pane component
				scrl_Chart.getViewport().add(label);
				pnl_Chart.add(scrl_Chart, BorderLayout.CENTER);
				pnl_Chart.doLayout();
			}
		} catch (IOException ex) {
			log.warn(Text.Reports.warnCantOpenFile, ex);
			Dialogs.showWarningDialogue(Text.Reports.warnCantOpenFile);
		}

		try {
			Map<ChromosomeKey, ChromosomeInfo> chromosomes
					= OperationManager.extractChromosomeKeysAndInfos(operationKey);
			chromosomeKeys = new ArrayList<ChromosomeKey>(chromosomes.keySet());
			chromosomeInfos = new ArrayList<ChromosomeInfo>(chromosomes.values());

			// CHECK HOW MANY CHR HAVE PLOTS (ANY MARKERS?)
			int chrPlotNb = 0;
			for (ChromosomeInfo chrInfo : chromosomeInfos) {
				if (chrInfo.getMarkerCount() > 0) {
					chrPlotNb++;
				}
			}

			// CALCULATE THE WIDTH OF 1 CHR PLOT
			chrPlotWidth = Math.round(
					(float) (chartWidth - ((chrPlotNb - 1) * padGap))
					/ chrPlotNb);

			chrPlotWidthPad = chrPlotWidth + padGap;
		} catch (IOException ex) {
			log.error(null, ex);
		}
	}

	private Object[] getChrSliceInfo(int pxXpos) {

		int pxXposNoLeftPad = pxXpos - padLeft;

		Object[] chromosomeAtPos = getChrInfo(pxXposNoLeftPad);
		ChromosomeKey chr = (ChromosomeKey) chromosomeAtPos[0];
		ChromosomeInfo chrInfo = (ChromosomeInfo) chromosomeAtPos[1];

		int nbMarkers = chrInfo.getMarkerCount();
		int startPhysPos = chrInfo.getFirstPos();
		int maxPhysPos = chrInfo.getPos();
		int startIdx = chrInfo.getIndex();

		double avgMarkersPerPx = (double) nbMarkers / chrPlotWidth;
		double avgSlotsPerPx = (double) (maxPhysPos - startPhysPos) / chrPlotWidth;

		int defaultSliceWidth;
		// FIXME the following if statement does not make sense (two times the same code)
		if (avgMarkersPerPx < 0) {
			defaultSliceWidth = (int) Math.round(ManhattanPlotZoom.MARKERS_NUM_DEFAULT / avgMarkersPerPx); // width of a slice inside current chr
		} else {
			defaultSliceWidth = (int) Math.round(ManhattanPlotZoom.MARKERS_NUM_DEFAULT / avgMarkersPerPx); // width of a slice inside current chr
		}
		long defaultSlotsNb = Math.round(defaultSliceWidth * avgSlotsPerPx); // Nb of physical slots per slice

		int chrNb = Math.round((float) pxXposNoLeftPad / chrPlotWidthPad);
		int pxXRest = pxXposNoLeftPad - (chrNb * chrPlotWidthPad);
		int sliceNb = Math.round((float) pxXRest / defaultSliceWidth);
		long longStartPhysPos = startPhysPos + (sliceNb * defaultSlotsNb);

		Object[] sliceInfo = new Object[5];
		sliceInfo[0] = chrNb;
		sliceInfo[1] = chr;
		sliceInfo[2] = sliceNb;
		sliceInfo[3] = longStartPhysPos;
		sliceInfo[4] = defaultSlotsNb;

		return sliceInfo;
	}

	private Object[] getChrInfo(int pxXposNoLeftPad) {

		final int selectedChrMap = Math.round((float) pxXposNoLeftPad / chrPlotWidthPad);

		ChromosomeKey chrKey;
		ChromosomeInfo chrInfo;
		if (selectedChrMap < chromosomeKeys.size()) {
			chrKey = chromosomeKeys.get(selectedChrMap);
			chrInfo = chromosomeInfos.get(selectedChrMap);
		} else {
			chrKey = new ChromosomeKey("");
			chrInfo = new ChromosomeInfo();
		}

		return new Object[] {chrKey, chrInfo};
	}

	public boolean isFired() {
		return fired;
	}

	public void setFired(boolean fired) {
		this.fired = fired;
	}
}
