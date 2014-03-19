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

package org.gwaspi.gui;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.gwaspi.global.Config;
import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.gui.utils.RowRendererDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreferencesPanel extends JPanel {

	private static final Logger log
			= LoggerFactory.getLogger(PreferencesPanel.class);

	private final Preferences prefs;
	// Variables declaration - do not modify
	private JButton btn_Back;
	private JButton btn_Reset;
	private JButton btn_Save;
	private JButton btn_ChangeDataDir;
	private JPanel pnl_Footer;
	private JScrollPane scrl_PreferencesTable;
	private JTable tbl_PreferencesTable;
	private final List<String[]> prefBackup;
	private ResetPreferencesAction resetPreferencesAction;
	// End of variables declaration

	private static class PreferencesTableModel extends DefaultTableModel {

		private static final String[] COLUMN_NAMES
				= new String[] {Text.App.propertyName, Text.App.propertyValue};
		private static final boolean[] CAN_EDIT = new boolean[] {false, true};
		private static final Object[][] DATA
				= new Object[][] {
					{null, null},
					{null, null},
					{null, null},
					{null, null}
				};

		PreferencesTableModel() {
			super(DATA, COLUMN_NAMES);
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return CAN_EDIT[columnIndex];
		}
	}
	/**
	 * Creates new form IntroPanel
	 */
	public PreferencesPanel() {

		prefs = Preferences.userNodeForPackage(Config.class);
		prefBackup = new LinkedList<String[]>();
		initGui();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	private void initGui() {

		scrl_PreferencesTable = new JScrollPane();
		tbl_PreferencesTable = new JTable() {
			@Override
			public boolean isCellEditable(int row, int col) {
				return (col != 0); // mRenders column 0 uneditable.
			}
		};
		tbl_PreferencesTable.setDefaultRenderer(Object.class, new RowRendererDefault());
		pnl_Footer = new JPanel();
		btn_Back = new JButton();
		btn_Save = new JButton();
		btn_Reset = new JButton();
		btn_ChangeDataDir = new JButton();

		setBorder(BorderFactory.createTitledBorder(null, Text.App.propertiesPaths, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("FreeSans", 0, 24))); // NOI18N

		tbl_PreferencesTable.setModel(new PreferencesTableModel());
		scrl_PreferencesTable.setViewportView(tbl_PreferencesTable);

		btn_Back.setAction(new StudyManagementPanel.BackAction());

		resetPreferencesAction = new ResetPreferencesAction(prefBackup, tbl_PreferencesTable, prefs);
		btn_Reset.setAction(resetPreferencesAction);
		resetPreferencesAction.loadPrefs();

		btn_Save.setAction(new SavePreferencesAction(tbl_PreferencesTable, resetPreferencesAction));

		btn_ChangeDataDir.setAction(new ChangeDataDirAction(tbl_PreferencesTable, resetPreferencesAction));

		//<editor-fold defaultstate="expanded" desc="LAYOUT FOOTER">
		GroupLayout pnl_FooterLayout = new GroupLayout(pnl_Footer);
		pnl_Footer.setLayout(pnl_FooterLayout);
		pnl_FooterLayout.setHorizontalGroup(
				pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, pnl_FooterLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(btn_Back)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 403, Short.MAX_VALUE)
				.addGroup(pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
				.addComponent(btn_ChangeDataDir, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(GroupLayout.Alignment.TRAILING, pnl_FooterLayout.createSequentialGroup()
				.addComponent(btn_Reset, GroupLayout.PREFERRED_SIZE, 126, GroupLayout.PREFERRED_SIZE)
				.addGap(18, 18, 18)
				.addComponent(btn_Save, GroupLayout.PREFERRED_SIZE, 134, GroupLayout.PREFERRED_SIZE)))
				.addContainerGap()));


		pnl_FooterLayout.linkSize(SwingConstants.HORIZONTAL, new Component[]{btn_Back, btn_Reset, btn_Save});

		pnl_FooterLayout.setVerticalGroup(
				pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, pnl_FooterLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(btn_ChangeDataDir, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
				.addGroup(pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(btn_Save)
				.addComponent(btn_Reset)
				.addComponent(btn_Back))));


		pnl_FooterLayout.linkSize(SwingConstants.VERTICAL, new Component[]{btn_Back, btn_ChangeDataDir, btn_Reset, btn_Save});
		//</editor-fold>

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
				.addComponent(pnl_Footer, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(scrl_PreferencesTable, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 744, Short.MAX_VALUE))
				.addContainerGap()));
		layout.setVerticalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(scrl_PreferencesTable, GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_Footer, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(99, 99, 99)));
	}

	private static class SavePreferencesAction extends AbstractAction {

		private final JTable preferencesTable;
		private final Action resetPreferencesAction;

		SavePreferencesAction(JTable preferencesTable, Action resetPreferencesAction) {

			this.preferencesTable = preferencesTable;
			this.resetPreferencesAction = resetPreferencesAction;
			putValue(NAME, Text.All.save);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {

			boolean proceed = true;
			preferencesTable.editCellAt(0, 0); //commit edited changes by chnaging the focus of table

			//<editor-fold defaultstate="expanded" desc="VALIDATION">
			for (int i = 0; i < preferencesTable.getRowCount(); i++) {
				final String selectedPropertyName = preferencesTable.getValueAt(i, 0).toString();
				final String selectedPropertyValue = preferencesTable.getValueAt(i, 1).toString();
				if (selectedPropertyName.toLowerCase().contains("dir")) {
					File path = new File(selectedPropertyValue);
					if (!path.exists()) {
						proceed = false;
						Dialogs.showInfoDialogue("Warning! Path provided does not exists!\nThis may cause " + Text.App.appName + " to fail!\nPlease fix broken path: " + selectedPropertyValue);
						resetPreferencesAction.actionPerformed(null);
					}
				} else if (selectedPropertyName.equals("CHART_MANHATTAN_PLOT_BCKG")
						|| selectedPropertyName.equals("CHART_MANHATTAN_PLOT_BCKG_ALT")
						|| selectedPropertyName.equals("CHART_MANHATTAN_PLOT_DOT")
						|| selectedPropertyName.equals("CHART_QQ_PLOT_BCKG")
						|| selectedPropertyName.equals("CHART_QQ_PLOT_DOT")
						|| selectedPropertyName.equals("CHART_QQ_PLOT_2SIGMA")
						|| selectedPropertyName.equals("CHART_SAMPLEQA_HETZYG_THRESHOLD")
						|| selectedPropertyName.equals("CHART_SAMPLEQA_MISSING_THRESHOLD"))
				{
					// Check if it is a valid color setting,
					// and issue a warning if not.
					String[] tmp = selectedPropertyValue.split(",");
					if (tmp.length == 3) {
						try {
							int redInt = Integer.parseInt(tmp[0]);
							if (redInt < 0 || redInt > 255) {
								redInt = redInt % 255;
							}
							int greenInt = Integer.parseInt(tmp[1]);
							if (greenInt < 0 || greenInt > 255) {
								greenInt = greenInt % 255;
							}
							int blueInt = Integer.parseInt(tmp[2]);
							if (blueInt < 0 || blueInt > 255) {
								blueInt = blueInt % 255;
							}
						} catch (Exception ex) {
							String warningText = Text.App.warnPropertyRGB + "\nField: " + selectedPropertyName;
							log.warn(warningText, ex);
							Dialogs.showWarningDialogue(warningText);
							proceed = false;
						}
					} else {
						Dialogs.showWarningDialogue(Text.App.warnPropertyRGB + "\nField: " + selectedPropertyName);
						proceed = false;
					}
				} else if (selectedPropertyName.equals("CHART_MANHATTAN_PLOT_THRESHOLD")) {    //Check if it's a number
					try {
						double tmpNb = Double.parseDouble(selectedPropertyValue);
					} catch (Exception ex) {
						String warningText = Text.App.warnMustBeNumeric + "\nField: " + selectedPropertyName;
						log.warn(warningText, ex);
						Dialogs.showWarningDialogue(warningText);
						proceed = false;
					}
				} else if (selectedPropertyName.equals("CHART_SAMPLEQA_HETZYG_THRESHOLD")) {    //Check if it's a number
					try {
						double tmpNb = Double.parseDouble(selectedPropertyValue);
					} catch (Exception ex) {
						String warningText = Text.App.warnMustBeNumeric + "\nField: " + selectedPropertyName;
						log.warn(warningText, ex);
						Dialogs.showWarningDialogue(warningText);
						proceed = false;
					}
				} else if (selectedPropertyName.equals("CHART_SAMPLEQA_MISSING_THRESHOLD")) {    //Check if it's a number
					try {
						double tmpNb = Double.parseDouble(selectedPropertyValue);
					} catch (Exception ex) {
						String warningText = Text.App.warnMustBeNumeric + "\nField: " + selectedPropertyName;
						log.warn(warningText, ex);
						Dialogs.showWarningDialogue(warningText);
						proceed = false;
					}
				}
			}
			//</editor-fold>
			if (proceed) {
				Integer decision = Dialogs.showConfirmDialogue("Do you really want to change the current preference values?\nDoing so may cause breakage if the data expected (databases, genotypes...) is not available at the new paths.");
				if ((decision == JOptionPane.YES_OPTION) && proceed) {
					for (int i = 0; i < preferencesTable.getRowCount(); i++) {
						final String selectedPropertyName = preferencesTable.getValueAt(i, 0).toString();
						final String selectedPropertyValue = preferencesTable.getValueAt(i, 1).toString();
						try {
							Config.setConfigValue(selectedPropertyName, selectedPropertyValue);
						} catch (IOException ex) {
							log.error(null, ex);
						}
					}
					Dialogs.showInfoDialogue("Preferences & Paths Saved");
				}
			}
		}
	}

	private static class ResetPreferencesAction extends AbstractAction {

		private final List<String[]> prefBackup;
		private final JTable preferencesTable;
		private final Preferences prefs;

		ResetPreferencesAction(List<String[]> prefBackup, JTable preferencesTable, Preferences prefs) {

			this.prefBackup = prefBackup;
			this.preferencesTable = preferencesTable;
			this.prefs = prefs;
			putValue(NAME, Text.All.reset);
		}

		public void loadPrefs() {
			try {
				String[] preferences = prefs.keys();
				prefBackup.clear();

				Object[][] tableMatrix = new Object[preferences.length][2];
				for (int i = 0; i < preferences.length; i++) {
					tableMatrix[i][0] = preferences[i];
					tableMatrix[i][1] = prefs.get(preferences[i], "");

					String[] pref = new String[2];
					pref[0] = tableMatrix[i][0].toString();
					pref[1] = tableMatrix[i][1].toString();
					prefBackup.add(pref);
				}
				String[] columns = new String[]{Text.App.propertyName, Text.App.propertyValue};
				TableModel model = new DefaultTableModel(tableMatrix, columns);
				preferencesTable.setModel(model);
			} catch (BackingStoreException ex) {
				log.error(null, ex);
			}
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			boolean result = true;

			for (String[] pref : prefBackup) {
				try {
					Config.setConfigValue(pref[0], pref[1]);
				} catch (IOException ex) {
					result = false;
					log.error(null, ex);
				}
			}

			if (result) {
				loadPrefs();
				Dialogs.showInfoDialogue("Preferences & Paths reset to previous values.");
			}
		}
	}

	private static class ChangeDataDirAction extends AbstractAction {

		private final JTable preferencesTable;
		private final Action resetPreferencesAction;

		ChangeDataDirAction(JTable preferencesTable, Action resetPreferencesAction) {

			this.preferencesTable = preferencesTable;
			this.resetPreferencesAction = resetPreferencesAction;
			putValue(NAME, Text.App.changeDataDir);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			int decision = Dialogs.showConfirmDialogue(Text.App.confirmCopyDataDir);
			if (decision == JOptionPane.YES_OPTION) {
				TableModel tm = preferencesTable.getModel();
				String currentDataDirPath = null;
				for (int i = 0; i < tm.getRowCount(); i++) {
					String key = tm.getValueAt(i, 0).toString();
					if (key.equals(Config.PROPERTY_DATA_DIR)) {
						currentDataDirPath = tm.getValueAt(i, 1).toString();
					}
				}

				if (currentDataDirPath != null) {
					File newDataDir = Dialogs.selectDirectoryDialog(JOptionPane.OK_OPTION);

					try {
						File origFile = new File(currentDataDirPath + "/datacenter");
						File newFile = new File(newDataDir.getPath() + "/datacenter");
						if (origFile.exists()) {
							org.gwaspi.global.Utils.copyFileRecursive(origFile, newFile);
						}
						Config.setDBSystemDir(newFile.getPath());
						Config.setDBSystemDir(newFile.getPath());

						origFile = new File(currentDataDirPath + "/genotypes");
						newFile = new File(newDataDir.getPath() + "/genotypes");
						if (origFile.exists()) {
							org.gwaspi.global.Utils.copyFileRecursive(origFile, newFile);
						}
						Config.setConfigValue(Config.PROPERTY_GENOTYPES_DIR, newFile.getPath());

//						origFile = new File(currentDataDirPath + "/help");
//						newFile = new File(newDataDir.getPath() + "/help");
//						if (origFile.exists()) {
//							org.gwaspi.global.Utils.copyFileRecursive(origFile, newFile);
//						}
//						Config.setConfigValue("OfflineHelpDir", newFile.getPath());

						origFile = new File(currentDataDirPath + "/export");
						newFile = new File(newDataDir.getPath() + "/export");
						if (origFile.exists()) {
							org.gwaspi.global.Utils.copyFileRecursive(origFile, newFile);
						}
						Config.setConfigValue(Config.PROPERTY_EXPORT_DIR, newFile.getPath());

						origFile = new File(currentDataDirPath + "/reports");
						newFile = new File(newDataDir.getPath() + "/reports");
						if (origFile.exists()) {
							org.gwaspi.global.Utils.copyFileRecursive(origFile, newFile);
						}
						Config.setConfigValue(Config.PROPERTY_REPORTS_DIR, newFile.getPath());
						Config.setConfigValue(Config.PROPERTY_LOG_DIR, newFile.getPath() + "/log");
						Config.setConfigValue(Config.PROPERTY_DATA_DIR, newDataDir.getPath());

						GWASpiExplorerPanel.getSingleton().setPnl_Content(new PreferencesPanel());
						GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
						Dialogs.showInfoDialogue(Text.App.infoDataDirCopyOK);
					} catch (IOException ex) {
						log.warn(Text.App.warnErrorCopyData, ex);
						Dialogs.showWarningDialogue(Text.App.warnErrorCopyData);
						resetPreferencesAction.actionPerformed(null);
					}
				}
			}
		}
	}
}
