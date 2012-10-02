package org.gwaspi.gui.reports;

import org.gwaspi.global.Config;
import org.gwaspi.global.Text;
import org.gwaspi.global.Utils;
import org.gwaspi.gui.GWASpiExplorerPanel;
import org.gwaspi.gui.MatrixAnalysePanel;
import org.gwaspi.gui.utils.Dialogs;
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
import org.gwaspi.model.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class ChartDefaultDisplay extends JPanel {

	private static final Logger log
			= LoggerFactory.getLogger(ChartDefaultDisplay.class);

	// Variables declaration - do not modify
	private JPanel pnl_Chart;
	private JPanel pnl_Footer;
	private JScrollPane scrl_Chart;
	private JButton btn_Save;
	private JButton btn_Back;
	private int opId;
	// End of variables declaration

	public ChartDefaultDisplay(final int studyId, final String chartPath, int _opId) {

		opId = _opId;

		scrl_Chart = new JScrollPane();
		pnl_Chart = new JPanel();
		pnl_Footer = new JPanel();
		btn_Save = new JButton();
		btn_Back = new JButton();

		//<editor-fold defaultstate="collapsed/expanded" desc="">
		btn_Save.setAction(new SaveAsAction(studyId, chartPath));

		btn_Back.setAction(new BackAction(opId));

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

		diplayChart(studyId, chartPath);
	}

	private void diplayChart(int studyId, String chartPath) {
		try {
			String reportPath = Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, "") + "/STUDY_" + studyId + "/";
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
				String reportPath = Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, "") + "/STUDY_" + studyId + "/";
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

	private static class BackAction extends AbstractAction {

		private int opId;

		BackAction(int opId) {

			this.opId = opId;
			putValue(NAME, Text.All.Back);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				Operation op = new Operation(opId);
				GWASpiExplorerPanel.getSingleton().getTree().setSelectionPath(GWASpiExplorerPanel.getSingleton().getTree().getSelectionPath().getParentPath());
				GWASpiExplorerPanel.getSingleton().setPnl_Content(new MatrixAnalysePanel(op.getParentMatrixId(), opId));
				GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}
}
