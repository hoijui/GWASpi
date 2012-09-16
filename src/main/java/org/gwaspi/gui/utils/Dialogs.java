package org.gwaspi.gui.utils;

import org.gwaspi.constants.cExport.ExportFormat;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.global.Config;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.Matrix;
import org.gwaspi.model.Operation;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Dialogs {

	public static String currentAppPath = "";
	private static JFileChooser fc;

	private Dialogs() {
	}

	//<editor-fold defaultstate="collapsed" desc="DIALOG BOXES">
	public static Operation showOperationCombo(int matrixId, OPType filterOpType) throws IOException {
		Operation selectedOP = null;
		org.gwaspi.model.OperationsList operations = new org.gwaspi.model.OperationsList(matrixId);

		if (!operations.operationsListAL.isEmpty()) {
			List<String> operationsNames = new ArrayList<String>();
			List<Operation> operationAL = new ArrayList<Operation>();
			for (int i = 0; i < operations.operationsListAL.size(); i++) {
				Operation op = operations.operationsListAL.get(i);
				if (op.getOperationType().equals(filterOpType.toString())) {
					StringBuilder sb = new StringBuilder();
					sb.append("OP: ");
					sb.append(op.getOperationId());
					sb.append(" - ");
					sb.append(op.getOperationFriendlyName());
					operationsNames.add(sb.toString());
					operationAL.add(op);
				}
			}

			JFrame frame = new JFrame("Census Combo Dialog");
			String selectedRow = (String) JOptionPane.showInputDialog(frame,
					"Choose Operation to use...",
					"Available Census",
					JOptionPane.QUESTION_MESSAGE,
					null,
					operationsNames.toArray(),
					0);

			if (selectedRow != null) {
				selectedOP = operationAL.get(operationsNames.indexOf(selectedRow));
			}
		}

		return selectedOP;
	}

	public static Operation showOperationCombo(int matrixId, List<String> filterOpTypeAL, String title) throws IOException {
		Operation selectedOP = null;
		org.gwaspi.model.OperationsList operations = new org.gwaspi.model.OperationsList(matrixId);

		if (!operations.operationsListAL.isEmpty()) {
			List<String> operationsNames = new ArrayList<String>();
			List<Operation> operationAL = new ArrayList<Operation>();
			for (int i = 0; i < operations.operationsListAL.size(); i++) {
				Operation op = operations.operationsListAL.get(i);
				if (filterOpTypeAL.contains(op.getOperationType())) {
					StringBuilder sb = new StringBuilder();
					sb.append("OP: ");
					sb.append(op.getOperationId());
					sb.append(" - ");
					sb.append(op.getOperationFriendlyName());
					operationsNames.add(sb.toString());
					operationAL.add(op);
				}
			}

			if (!operationAL.isEmpty()) {
				JFrame frame = new JFrame(title);
				String selectedRow = (String) JOptionPane.showInputDialog(frame,
						"Choose " + title + " to use...",
						"Available Operations",
						JOptionPane.QUESTION_MESSAGE,
						null,
						operationsNames.toArray(),
						0);

				if (selectedRow != null) {
					selectedOP = operationAL.get(operationsNames.indexOf(selectedRow));
				}
			}
		}

		return selectedOP;
	}

	public static Operation showOperationSubOperationsCombo(int matrixId, int parentOpId, OPType filterOpType, String title) throws IOException {
		Operation selectedSubOp = null;
		org.gwaspi.model.OperationsList operations = new org.gwaspi.model.OperationsList(matrixId, parentOpId);

		if (!operations.operationsListAL.isEmpty()) {
			List<String> operationsNames = new ArrayList<String>();
			List<Operation> operationAL = new ArrayList<Operation>();
			for (int i = 0; i < operations.operationsListAL.size(); i++) {
				Operation op = operations.operationsListAL.get(i);
				if (op.getOperationType().equals(filterOpType.toString())) {
					StringBuilder sb = new StringBuilder();
					sb.append("OP: ");
					sb.append(op.getOperationId());
					sb.append(" - ");
					sb.append(op.getOperationFriendlyName());
					operationsNames.add(sb.toString());
					operationAL.add(op);
				}
			}

			JFrame frame = new JFrame(title);
			String selectedRow = (String) JOptionPane.showInputDialog(frame,
					"Choose " + title + " to use...",
					"Available Operations",
					JOptionPane.QUESTION_MESSAGE,
					null,
					operationsNames.toArray(),
					0);

			if (selectedRow != null) {
				selectedSubOp = operationAL.get(operationsNames.indexOf(selectedRow));
			}
		}

		return selectedSubOp;
	}

	public static ImportFormat showTechnologySelectCombo() {
		ImportFormat[] formats = org.gwaspi.constants.cImport.ImportFormat.values();

		JFrame frame = new JFrame("Format Combo Dialog");
		ImportFormat technology = (ImportFormat) JOptionPane.showInputDialog(frame,
				"What format?",
				"Platform, Format or Technology",
				JOptionPane.QUESTION_MESSAGE,
				null,
				formats,
				formats[0]);

		return technology;
	}

	public static ExportFormat showExportFormatsSelectCombo() {
		ExportFormat[] formats = org.gwaspi.constants.cExport.ExportFormat.values();

		JFrame frame = new JFrame("Format Combo Dialog");
		ExportFormat expFormat = (ExportFormat) JOptionPane.showInputDialog(frame,
				"What format?",
				"Export Format",
				JOptionPane.QUESTION_MESSAGE,
				null,
				formats,
				formats[0]);

		return expFormat;
	}

	public static String showPhenotypeColumnsSelectCombo() {
		String[] phenotype = org.gwaspi.constants.cDBSamples.f_PHENOTYPES_COLUMNS;

		JFrame frame = new JFrame("Phenotype Combo Dialog");
		String expPhenotype = (String) JOptionPane.showInputDialog(frame,
				"What phenotype?",
				"Phenotype column to use",
				JOptionPane.QUESTION_MESSAGE,
				null,
				phenotype,
				phenotype[0]);

		return expPhenotype;
	}

	public static StrandType showStrandSelectCombo() {
		StrandType[] strandFlags = org.gwaspi.constants.cNetCDF.Defaults.StrandType.values();

		JFrame frame = new JFrame("Strand Combo Dialog");
		StrandType strandType = (StrandType) JOptionPane.showInputDialog(frame,
				"What strand are the genotypes located on?",
				"Genotypes Strand",
				JOptionPane.QUESTION_MESSAGE,
				null,
				strandFlags,
				strandFlags[0]);

		return strandType;

	}

	public static String showChromosomeSelectCombo() {
		String[] chroms = org.gwaspi.constants.cNetCDF.Defaults.Chromosomes;

		JFrame frame = new JFrame("Chromosomes Combo Dialog");
		String chr = (String) JOptionPane.showInputDialog(frame,
				"What chromosome are the genotypes placed at?",
				"Chromosome",
				JOptionPane.QUESTION_MESSAGE,
				null,
				chroms,
				chroms[0]);

		return chr;
	}

	public static GenotypeEncoding showGenotypeCodeSelectCombo() {
		GenotypeEncoding[] gtCode = org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding.values();

		JFrame frame = new JFrame("Genotype Encoding Combo Dialog");
		GenotypeEncoding strandType = (GenotypeEncoding) JOptionPane.showInputDialog(frame,
				"What code are the genotypes noted in?",
				"Genotype Encoding",
				JOptionPane.QUESTION_MESSAGE,
				null,
				gtCode,
				gtCode[0]);

		return strandType;
	}

	public static int showMatrixSelectCombo() throws IOException {
		MatricesList matrices = new MatricesList();
		//String[] matrixNames = new String[matrices.matrixList.size()];
		List<String> matrixNames = new ArrayList<String>();
		List<Integer> matrixIDs = new ArrayList<Integer>();
		for (int i = 0; i < matrices.matrixList.size(); i++) {
			Matrix mx = matrices.matrixList.get(i);
			StringBuilder mn = new StringBuilder();
			mn.append("SID: ");
			mn.append(mx.getStudyId());
			mn.append(" - MX: ");
			mn.append(mx.matrixMetadata.getMatrixFriendlyName());
			//matrixNames[i]=mn.toString();
			matrixNames.add(mn.toString());
			matrixIDs.add(mx.getMatrixId());
		}

		JFrame frame = new JFrame("Genotype Encoding Combo Dialog");
		String selectedRow = (String) JOptionPane.showInputDialog(frame,
				"What code are the genotypes noted in?",
				"Genotype Encoding",
				JOptionPane.QUESTION_MESSAGE,
				null,
				matrixNames.toArray(),
				0);

		int selectedMatrix = Integer.MIN_VALUE;
		if (selectedRow != null) {
			selectedMatrix = matrixIDs.get(matrixNames.indexOf(selectedRow));
		}
		return selectedMatrix;
	}

	public static Integer showConfirmDialogue(String message) {
		JFrame frame = new JFrame("Confirm?");
		return JOptionPane.showConfirmDialog(frame, message);
	}

	public static void showWarningDialogue(String message) {
		JFrame frame = new JFrame("Warning!");
		JOptionPane.showMessageDialog(frame, message);
	}

	public static void showInfoDialogue(String message) {
		JFrame frame = new JFrame("Information"); // FIXME would we not rather use this string in a title, isnted of in an invisible parent frame? :/ same above
		JOptionPane.showMessageDialog(frame, message);

		frame.setVisible(false);
	}

	public static int showOptionDialogue(String title, String message, String button1, String button2, String button3) {
		JFrame frame = new JFrame("Proceed with...");
		Object[] options = {button1,
			button2,
			button3};
		return JOptionPane.showOptionDialog(frame,
				message,
				title,
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[2]);
	}

	public static String showInputBox(String message) {
		JFrame frame = new JFrame("Input text...");
		return JOptionPane.showInputDialog(frame, message);
	}
	//</editor-fold>

	// <editor-fold defaultstate="expanded" desc="FILE OPEN DIALOGUES">
	public static void selectAndSetFileDialog(ActionEvent evt, JButton openButton, JTextField textField, final String filter) {
		selectAndSetFileInCurrentDirDialog(evt, openButton, org.gwaspi.constants.cGlobal.HOMEDIR, textField, filter);
	}

	public static void selectAndSetFileInCurrentDirDialog(ActionEvent evt, JButton openButton, String dir, JTextField textField, final String filter) {
		// Create a file chooser
		fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

		// getting the latest opened dir
		try {
//			File tmpFile = new File(dir);
//			if(!tmpFile.exists()){
			dir = Config.getConfigValue("LAST_OPENED_DIR", org.gwaspi.constants.cGlobal.HOMEDIR);
			fc.setCurrentDirectory(new java.io.File(dir));
//			}
		} catch (IOException ex) {
			Logger.getLogger(Dialogs.class.getName()).log(Level.SEVERE, null, ex);
		}

		// displaying only necessary files as requested by "filter"
		fc.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				return f.getName().toLowerCase().endsWith(filter) || f.isDirectory();
			}

			public String getDescription() {
				String filterDesc;
				if (filter.equals("")) {
					filterDesc = "All files";
				} else {
					filterDesc = filter + " files";
				}
				return filterDesc;
			}
		});

		int returnVal = fc.showOpenDialog(org.gwaspi.gui.StartGWASpi.mainGUIFrame);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			textField.setText(file.getAbsolutePath());

			// setting the directory to latest opened dir
			try {
				Config.setConfigValue("LAST_OPENED_DIR", file.getParent());
			} catch (IOException ex) {
				Logger.getLogger(Dialogs.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	public static File selectAndSetDirectoryDialog(ActionEvent evt, JButton openButton, JTextField textField, String dir, final String filter) {

		File resultFile = null;
		// Create a file chooser
		fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		// Handle open button action.
		if (evt.getSource() == openButton) {

			// getting the latest opened dir
			try {
//				File tmpFile = new File(dir);
//				if(!tmpFile.exists()){
				dir = Config.getConfigValue("LAST_OPENED_DIR", org.gwaspi.constants.cGlobal.HOMEDIR);
				fc.setCurrentDirectory(new java.io.File(dir));
//				}
			} catch (IOException ex) {
				Logger.getLogger(Dialogs.class.getName()).log(Level.SEVERE, null, ex);
			}

			fc.setFileFilter(new FileFilter() {
				public boolean accept(File f) {
					return f.getName().toLowerCase().endsWith(filter) || f.isDirectory();
				}

				public String getDescription() {
					String filterDesc;
					if (filter.isEmpty()) {
						filterDesc = "All files";
					} else {
						filterDesc = filter + " files";
					}
					return filterDesc;
				}
			});
			int returnVal = fc.showOpenDialog(org.gwaspi.gui.StartGWASpi.mainGUIFrame);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				resultFile = fc.getSelectedFile();
				textField.setText(resultFile.getPath());

				// setting the directory to latest opened dir
				try {
					Config.setConfigValue("LAST_OPENED_DIR", resultFile.getParent());
				} catch (IOException ex) {
					Logger.getLogger(Dialogs.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}

		return resultFile;
	}

	public static File selectDirectoryDialog(int okOption) {

		File resultFile = null;
		// Create a file chooser
		fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		// Handle open button action.
		if (okOption == JOptionPane.OK_OPTION) {

			// getting the latest opened dir
			try {
				String dir = Config.getConfigValue("LAST_OPENED_DIR", org.gwaspi.constants.cGlobal.HOMEDIR);
				fc.setCurrentDirectory(new java.io.File(dir));
			} catch (IOException ex) {
				Logger.getLogger(Dialogs.class.getName()).log(Level.SEVERE, null, ex);
			}

			int returnVal = fc.showOpenDialog(org.gwaspi.gui.StartGWASpi.mainGUIFrame);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				resultFile = fc.getSelectedFile();

				// setting the directory to latest opened dir
				try {
					Config.setConfigValue("LAST_OPENED_DIR", resultFile.getParent());
				} catch (IOException ex) {
					Logger.getLogger(Dialogs.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
		return resultFile;
	}

	public static File selectFilesAndDirectoriesDialog(int okOption) {

		File resultFile = null;
		// Create a file chooser
		fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

		// Handle open button action.
		if (okOption == JOptionPane.OK_OPTION) {

			// getting the last opened dir
			try {
				String dir = Config.getConfigValue("LAST_OPENED_DIR", org.gwaspi.constants.cGlobal.HOMEDIR);
				fc.setCurrentDirectory(new java.io.File(dir));
			} catch (IOException ex) {
				Logger.getLogger(Dialogs.class.getName()).log(Level.SEVERE, null, ex);
			}

			int returnVal = fc.showOpenDialog(org.gwaspi.gui.StartGWASpi.mainGUIFrame);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				resultFile = fc.getSelectedFile();

				// setting the directory to last opened dir
				try {
					Config.setConfigValue("LAST_OPENED_DIR", resultFile.getParent());
				} catch (IOException ex) {
					Logger.getLogger(Dialogs.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
		return resultFile;
	}
	// </editor-fold>
}
