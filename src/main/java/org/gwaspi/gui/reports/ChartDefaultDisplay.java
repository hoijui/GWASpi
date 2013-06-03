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
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;
import org.gwaspi.global.Text;
import org.gwaspi.global.Utils;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.model.Study;
import org.gwaspi.model.StudyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChartDefaultDisplay extends JPanel {

	private static final Logger log
			= LoggerFactory.getLogger(ChartDefaultDisplay.class);

	// Variables declaration - do not modify
	private final JPanel pnl_Chart;
	private final JPanel pnl_Footer;
	private final JScrollPane scrl_Chart;
	private final JButton btn_Save;
	private final JButton btn_Back;
	private final int opId;
	// End of variables declaration

	public ChartDefaultDisplay(final StudyKey studyKey, final String chartPath, int _opId) {

		opId = _opId;

		scrl_Chart = new JScrollPane();
		pnl_Chart = new JPanel();
		pnl_Footer = new JPanel();
		btn_Save = new JButton();
		btn_Back = new JButton();

		//<editor-fold defaultstate="expanded" desc="">
		btn_Save.setAction(new SaveAsAction(studyKey, chartPath));

		btn_Back.setAction(new Report_Analysis.BackAction(opId));

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

		diplayChart(studyKey, chartPath);
	}

	private void diplayChart(StudyKey studyKey, String chartPath) {
		try {
			String reportPath = Study.constructReportsPath(studyKey);
			File testF = new File(reportPath + chartPath);
			if (testF.exists()) {
				Icon image = new ImageIcon(testF.getPath());
				JLabel label = new JLabel(image);
				// Creating a Scroll pane component
				scrl_Chart.getViewport().add(label);
				pnl_Chart.add(scrl_Chart, BorderLayout.CENTER);
			}
		} catch (IOException ex) {
			log.error(null, ex);
		}
	}

	private static class SaveAsAction extends AbstractAction {

		private final StudyKey studyKey;
		private final String chartPath;

		SaveAsAction(StudyKey studyKey, String chartPath) {

			this.studyKey = studyKey;
			this.chartPath = chartPath;
			putValue(NAME, Text.All.save);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				String reportPath = Study.constructReportsPath(studyKey);
				File origFile = new File(reportPath + chartPath);
				File newFile = new File(Dialogs.selectDirectoryDialog(JOptionPane.OK_OPTION).getPath() + "/" + chartPath);
				if (origFile.exists()) {
					Utils.copyFile(origFile, newFile);
				}
			} catch (IOException ex) {
				log.error(null, ex);
			} catch (Exception ex) {
				log.error(null, ex);
			}
		}
	}
}
