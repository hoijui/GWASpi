package org.gwaspi.gui;

import org.gwaspi.global.Config;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class PreferencesPanel extends javax.swing.JPanel {

	protected static Preferences prefs = Preferences.userNodeForPackage(Config.class.getClass());
	// Variables declaration - do not modify
	private javax.swing.JButton btn_Back;
	private javax.swing.JButton btn_Reset;
	private javax.swing.JButton btn_Save;
	private javax.swing.JButton btn_ChangeDataDir;
	private javax.swing.JPanel pnl_Footer;
	private javax.swing.JScrollPane scrl_PreferencesTable;
	private javax.swing.JTable tbl_PreferencesTable;
	private String[][] prefBackup;
	// End of variables declaration

	/**
	 * Creates new form IntroPanel
	 */
	public PreferencesPanel() {
		initGui();
		loadPrefs();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	private void initGui() {

		scrl_PreferencesTable = new javax.swing.JScrollPane();
		tbl_PreferencesTable = new javax.swing.JTable() {
			@Override
			public boolean isCellEditable(int row, int col) {
				if (col == 0) {
					return false; //Renders column 0 uneditable.
				}
				return true;
			}
		};
		tbl_PreferencesTable.setDefaultRenderer(Object.class, new org.gwaspi.gui.utils.RowRendererDefault());
		pnl_Footer = new javax.swing.JPanel();
		btn_Back = new javax.swing.JButton();
		btn_Save = new javax.swing.JButton();
		btn_Reset = new javax.swing.JButton();
		btn_ChangeDataDir = new javax.swing.JButton();

		setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.gwaspi.global.Text.App.propertiesPaths, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("FreeSans", 0, 24))); // NOI18N

		tbl_PreferencesTable.setModel(new javax.swing.table.DefaultTableModel(
				new Object[][]{
					{null, null},
					{null, null},
					{null, null},
					{null, null}
				},
				new String[]{global.Text.App.propertyName, org.gwaspi.global.Text.App.propertyValue}) {
			boolean[] canEdit = new boolean[]{
				false, true
			};

					@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return canEdit[columnIndex];
			}
		});
		scrl_PreferencesTable.setViewportView(tbl_PreferencesTable);

		btn_Back.setText(org.gwaspi.global.Text.All.Back);
		btn_Back.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btn_BackActionPerformed(evt);
			}
		});


		btn_Reset.setText(org.gwaspi.global.Text.All.reset);
		btn_Reset.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				actionResetPreferences();
			}
		});


		btn_Save.setText(org.gwaspi.global.Text.All.save);
		btn_Save.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				actionSavePreferences(evt);
			}
		});

		btn_ChangeDataDir.setText(org.gwaspi.global.Text.App.changeDataDir);
		btn_ChangeDataDir.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				actionChangeDataDir();
			}
		});


		//<editor-fold defaultstate="collapsed" desc="LAYOUT FOOTER">
		javax.swing.GroupLayout pnl_FooterLayout = new javax.swing.GroupLayout(pnl_Footer);
		pnl_Footer.setLayout(pnl_FooterLayout);
		pnl_FooterLayout.setHorizontalGroup(
				pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_FooterLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(btn_Back)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 403, Short.MAX_VALUE)
				.addGroup(pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
				.addComponent(btn_ChangeDataDir, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_FooterLayout.createSequentialGroup()
				.addComponent(btn_Reset, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addGap(18, 18, 18)
				.addComponent(btn_Save, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)))
				.addContainerGap()));


		pnl_FooterLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[]{btn_Back, btn_Reset, btn_Save});

		pnl_FooterLayout.setVerticalGroup(
				pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_FooterLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(btn_ChangeDataDir, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
				.addGroup(pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
				.addComponent(btn_Save)
				.addComponent(btn_Reset)
				.addComponent(btn_Back))));


		pnl_FooterLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[]{btn_Back, btn_ChangeDataDir, btn_Reset, btn_Save});
		//</editor-fold>


		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
				.addComponent(pnl_Footer, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(scrl_PreferencesTable, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 744, Short.MAX_VALUE))
				.addContainerGap()));
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(scrl_PreferencesTable, javax.swing.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_Footer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addGap(99, 99, 99)));
	}

	private void loadPrefs() {
		try {
			String[] preferences = prefs.keys();
			prefBackup = new String[preferences.length][2];

			Object[][] tableMatrix = new Object[preferences.length][2];
			for (int i = 0; i < preferences.length; i++) {
				tableMatrix[i][0] = preferences[i];
				tableMatrix[i][1] = prefs.get(preferences[i], "");

				prefBackup[i][0] = tableMatrix[i][0].toString();
				prefBackup[i][1] = tableMatrix[i][1].toString();
			}
			String[] columns = new String[]{global.Text.App.propertyName, org.gwaspi.global.Text.App.propertyValue};
			TableModel model = new DefaultTableModel(tableMatrix, columns);
			tbl_PreferencesTable.setModel(model);
		} catch (BackingStoreException ex) {
			Logger.getLogger(PreferencesPanel.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	private void actionSavePreferences(ActionEvent evt) {
		boolean proceed = true;
		tbl_PreferencesTable.editCellAt(0, 0); //commit edited changes by chnaging the focus of table

		//<editor-fold defaultstate="collapsed" desc="VALIDATION">
		for (int i = 0; i < tbl_PreferencesTable.getRowCount(); i++) {
			if (tbl_PreferencesTable.getValueAt(i, 0).toString().toLowerCase().contains("dir")) {
				File path = new File(tbl_PreferencesTable.getValueAt(i, 1).toString());
				if (!path.exists()) {
					proceed = false;
					org.gwaspi.gui.utils.Dialogs.showInfoDialogue("Warning! Path provided does not exists!\nThis may cause " + org.gwaspi.global.Text.App.appName + " to fail!\nPlease fix broken path: " + tbl_PreferencesTable.getValueAt(i, 1).toString());
					actionResetPreferences();
				}
			} else if (tbl_PreferencesTable.getValueAt(i, 0).toString().equals("CHART_MANHATTAN_PLOT_BCKG") || //Check if it's a color setting
					tbl_PreferencesTable.getValueAt(i, 0).toString().equals("CHART_MANHATTAN_PLOT_BCKG_ALT")
					|| tbl_PreferencesTable.getValueAt(i, 0).toString().equals("CHART_MANHATTAN_PLOT_DOT")
					|| tbl_PreferencesTable.getValueAt(i, 0).toString().equals("CHART_QQ_PLOT_BCKG")
					|| tbl_PreferencesTable.getValueAt(i, 0).toString().equals("CHART_QQ_PLOT_DOT")
					|| tbl_PreferencesTable.getValueAt(i, 0).toString().equals("CHART_QQ_PLOT_2SIGMA")
					|| tbl_PreferencesTable.getValueAt(i, 0).toString().equals("CHART_SAMPLEQA_HETZYG_THRESHOLD")
					|| tbl_PreferencesTable.getValueAt(i, 0).toString().equals("CHART_SAMPLEQA_MISSING_THRESHOLD")) {

				String[] tmp = tbl_PreferencesTable.getValueAt(i, 1).toString().split(",");
				if (tmp.length == 3) {
					try {
						// FIXME This code does nothing... logic error?
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
					} catch (Exception e) {
						org.gwaspi.gui.utils.Dialogs.showWarningDialogue(org.gwaspi.global.Text.App.warnPropertyRGB + "\nField: " + tbl_PreferencesTable.getValueAt(i, 0).toString());
						proceed = false;
					}
				} else {
					org.gwaspi.gui.utils.Dialogs.showWarningDialogue(org.gwaspi.global.Text.App.warnPropertyRGB + "\nField: " + tbl_PreferencesTable.getValueAt(i, 0).toString());
					proceed = false;
				}
			} else if (tbl_PreferencesTable.getValueAt(i, 0).toString().equals("CHART_MANHATTAN_PLOT_THRESHOLD")) {    //Check if it's a number
				try {
					double tmpNb = Double.parseDouble(tbl_PreferencesTable.getValueAt(i, 1).toString());
				} catch (Exception e) {
					org.gwaspi.gui.utils.Dialogs.showWarningDialogue(org.gwaspi.global.Text.App.warnMustBeNumeric + "\nField: " + tbl_PreferencesTable.getValueAt(i, 0).toString());
					proceed = false;
				}
			} else if (tbl_PreferencesTable.getValueAt(i, 0).toString().equals("CHART_SAMPLEQA_HETZYG_THRESHOLD")) {    //Check if it's a number
				try {
					double tmpNb = Double.parseDouble(tbl_PreferencesTable.getValueAt(i, 1).toString());
				} catch (Exception e) {
					org.gwaspi.gui.utils.Dialogs.showWarningDialogue(org.gwaspi.global.Text.App.warnMustBeNumeric + "\nField: " + tbl_PreferencesTable.getValueAt(i, 0).toString());
					proceed = false;
				}
			} else if (tbl_PreferencesTable.getValueAt(i, 0).toString().equals("CHART_SAMPLEQA_MISSING_THRESHOLD")) {    //Check if it's a number
				try {
					double tmpNb = Double.parseDouble(tbl_PreferencesTable.getValueAt(i, 1).toString());
				} catch (Exception e) {
					org.gwaspi.gui.utils.Dialogs.showWarningDialogue(org.gwaspi.global.Text.App.warnMustBeNumeric + "\nField: " + tbl_PreferencesTable.getValueAt(i, 0).toString());
					proceed = false;
				}
			}
		}
		//</editor-fold>
		if (proceed) {
			Integer decision = org.gwaspi.gui.utils.Dialogs.showConfirmDialogue("Do you really want to change the current preference values?\nDoing so may cause breakage if the data expected (databases, genotypes...) is not available at the new paths.");
			if (decision == JOptionPane.YES_OPTION) {
				if (proceed) {
					for (int i = 0; i < tbl_PreferencesTable.getRowCount(); i++) {
						try {
							org.gwaspi.global.Config.setConfigValue(tbl_PreferencesTable.getValueAt(i, 0).toString(), tbl_PreferencesTable.getValueAt(i, 1).toString());
						} catch (IOException ex) {
							Logger.getLogger(PreferencesPanel.class.getName()).log(Level.SEVERE, null, ex);
						}
					}
					org.gwaspi.gui.utils.Dialogs.showInfoDialogue("Preferences & Paths Saved");
				}
			}
		}
	}

	private void actionResetPreferences() {
		boolean result = true;

		for (int i = 0; i < prefBackup.length; i++) {
			try {
				org.gwaspi.global.Config.setConfigValue(prefBackup[i][0], prefBackup[i][1]);
			} catch (IOException ex) {
				Logger.getLogger(PreferencesPanel.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		if (result) {
			loadPrefs();
			org.gwaspi.gui.utils.Dialogs.showInfoDialogue("Preferences & Paths reset to previous values.");
		}

	}

	private void actionChangeDataDir() {
		int decision = org.gwaspi.gui.utils.Dialogs.showConfirmDialogue(org.gwaspi.global.Text.App.confirmCopyDataDir);
		if (decision == JOptionPane.YES_OPTION) {

			TableModel tm = tbl_PreferencesTable.getModel();
			String currentDataDirPath = null;
			for (int i = 0; i < tm.getRowCount(); i++) {
				String key = tm.getValueAt(i, 0).toString();
				if (key.equals("DataDir")) {
					currentDataDirPath = tm.getValueAt(i, 1).toString();
				}
			}

			if (currentDataDirPath != null) {
				File newDataDir = org.gwaspi.gui.utils.Dialogs.selectDirectoryDialogue(JOptionPane.OK_OPTION);

				try {
					File origFile = new File(currentDataDirPath + "/datacenter");
					File newFile = new File(newDataDir.getPath() + "/datacenter");
					if (origFile.exists()) {
						org.gwaspi.global.Utils.copyFileRecursive(origFile, newFile);
					}
					org.gwaspi.global.Config.setDBSystemDir(newFile.getPath());
					org.gwaspi.global.Config.setDBSystemDir(newFile.getPath());

					origFile = new File(currentDataDirPath + "/genotypes");
					newFile = new File(newDataDir.getPath() + "/genotypes");
					if (origFile.exists()) {
						org.gwaspi.global.Utils.copyFileRecursive(origFile, newFile);
					}
					org.gwaspi.global.Config.setConfigValue("GTdir", newFile.getPath());

//					origFile = new File(currentDataDirPath + "/help");
//					newFile = new File(newDataDir.getPath() + "/help");
//					if (origFile.exists()) {
//						org.gwaspi.global.Utils.copyFileRecursive(origFile, newFile);
//					}
//					org.gwaspi.global.Config.setConfigValue("OfflineHelpDir", newFile.getPath());

					origFile = new File(currentDataDirPath + "/export");
					newFile = new File(newDataDir.getPath() + "/export");
					if (origFile.exists()) {
						org.gwaspi.global.Utils.copyFileRecursive(origFile, newFile);
					}
					org.gwaspi.global.Config.setConfigValue("ExportDir", newFile.getPath());

					origFile = new File(currentDataDirPath + "/reports");
					newFile = new File(newDataDir.getPath() + "/reports");
					if (origFile.exists()) {
						org.gwaspi.global.Utils.copyFileRecursive(origFile, newFile);
					}
					org.gwaspi.global.Config.setConfigValue("ReportsDir", newFile.getPath());
					org.gwaspi.global.Config.setConfigValue("LogDir", newFile.getPath() + "/log");
					org.gwaspi.global.Config.setConfigValue("DataDir", newDataDir.getPath());

					org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new PreferencesPanel();
					org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
					org.gwaspi.gui.utils.Dialogs.showInfoDialogue(org.gwaspi.global.Text.App.infoDataDirCopyOK);
				} catch (IOException iOException) {
					org.gwaspi.gui.utils.Dialogs.showWarningDialogue(org.gwaspi.global.Text.App.warnErrorCopyData);
					actionResetPreferences();
				}
			}
		}
	}

	private void btn_BackActionPerformed(ActionEvent evt) {
		org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new IntroPanel();
		org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
	}
}
