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

package org.gwaspi.gui.utils;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import org.gwaspi.constants.DBSamplesConstants;
import org.gwaspi.constants.ExportConstants.ExportFormat;
import org.gwaspi.constants.GlobalConstants;
import org.gwaspi.constants.ImportConstants.ImportFormat;
import org.gwaspi.constants.NetCDFConstants;
import org.gwaspi.constants.NetCDFConstants.Defaults.GenotypeEncoding;
import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;
import org.gwaspi.constants.NetCDFConstants.Defaults.StrandType;
import org.gwaspi.global.Config;
import org.gwaspi.global.Text;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.StudyKey;
import org.gwaspi.operations.GWASinOneGOParams;
import org.gwaspi.operations.markercensus.MarkerCensusOperationParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dialogs {

	private static final Logger log
			= LoggerFactory.getLogger(Dialogs.class);

	private static JFileChooser fc;

	private Dialogs() {
	}

	private static List<String> generateOperationsNames(final List<OperationMetadata> operations) {

		final List<String> operationNames = new ArrayList<String>(operations.size());

		final StringBuilder operationName = new StringBuilder(128);
		for (final OperationMetadata operation : operations) {
			operationName.setLength(0);
			operationName
					.append("OP: ")
					.append(operation.getId())
					.append(" - ")
					.append(operation.getFriendlyName());
			operationNames.add(operationName.toString());
		}

		return operationNames;
	}

	private static List<String> generateMatrixNames(final List<MatrixMetadata> matrices) {

		final List<String> matrixNames = new ArrayList<String>(matrices.size());

		final StringBuilder matrixName = new StringBuilder(128);
		for (final MatrixMetadata matrix : matrices) {
			matrixName.setLength(0);
			matrixName
					.append("SID: ")
					.append(matrix.getStudyKey().getId())
					.append(" - MX: ")
					.append(matrix.getFriendlyName());
			matrixNames.add(matrixName.toString());
		}

		return matrixNames;
	}

	//<editor-fold defaultstate="expanded" desc="DIALOG BOXES">
	public static OperationMetadata showOperationCombo(MatrixKey matrixKey, OPType filterOpType) throws IOException {

		OperationMetadata selectedOp = null;

		final List<OperationMetadata> operationsList = OperationsList.getOffspringOperationsMetadata(matrixKey);
		if (!operationsList.isEmpty()) {
			final List<OperationMetadata> selectedOperations = new ArrayList<OperationMetadata>();
			for (final OperationMetadata operation : operationsList) {
				if (operation.getOperationType().equals(filterOpType)) {
					selectedOperations.add(operation);
				}
			}

			selectedOp = showOperationCombo(selectedOperations, "Operation");
		}

		return selectedOp;
	}

	public static OperationMetadata showOperationCombo(List<OperationMetadata> operations, String title) throws IOException {

		OperationMetadata selectedOp = null;

		if (!operations.isEmpty()) {
			final List<String> operationNames = generateOperationsNames(operations);

			final String selectedRow = (String) JOptionPane.showInputDialog(
					null,
					"Choose " + title + " to use...",
					"Available Operations",
					JOptionPane.QUESTION_MESSAGE,
					null,
					operationNames.toArray(new Object[operationNames.size()]),
					0);

			if (selectedRow != null) {
				selectedOp = operations.get(operationNames.indexOf(selectedRow));
			}
		}

		return selectedOp;
	}

	public static OperationMetadata showOperationSubOperationsCombo(OperationKey parentOpKey, OPType filterOpType, String title) throws IOException {

		OperationMetadata selectedOp = null;
		List<OperationMetadata> operationsList = OperationsList.getChildrenOperationsMetadata(parentOpKey);

		if (!operationsList.isEmpty()) {
			List<OperationMetadata> selectedOperations = new ArrayList<OperationMetadata>();
			for (OperationMetadata op : operationsList) {
				if (op.getOperationType().equals(filterOpType)) {
					selectedOperations.add(op);
				}
			}

			selectedOp = showOperationCombo(selectedOperations, title);
		}

		return selectedOp;
	}

	public static ImportFormat showTechnologySelectCombo() {
		ImportFormat[] formats = ImportFormat.values();

		ImportFormat technology = (ImportFormat) JOptionPane.showInputDialog(
				null,
				"What format?",
				"Platform, Format or Technology",
				JOptionPane.QUESTION_MESSAGE,
				null,
				formats,
				formats[0]);

		return technology;
	}

	public static ExportFormat showExportFormatsSelectCombo() {
		ExportFormat[] formats = ExportFormat.values();

		ExportFormat expFormat = (ExportFormat) JOptionPane.showInputDialog(
				null,
				"What format?",
				"Export Format",
				JOptionPane.QUESTION_MESSAGE,
				null,
				formats,
				formats[0]);

		return expFormat;
	}

	public static String showPhenotypeColumnsSelectCombo() {
		final List<String> phenotype = DBSamplesConstants.F_PHENOTYPES_COLUMNS;

		String expPhenotype = (String) JOptionPane.showInputDialog(
				null,
				"What phenotype?",
				"Phenotype column to use",
				JOptionPane.QUESTION_MESSAGE,
				null,
				phenotype.toArray(),
				phenotype.get(0));

		return expPhenotype;
	}

	public static StrandType showStrandSelectCombo() {
		StrandType[] strandFlags = NetCDFConstants.Defaults.StrandType.values();

		StrandType strandType = (StrandType) JOptionPane.showInputDialog(
				null,
				"What strand are the genotypes located on?",
				"Genotypes Strand",
				JOptionPane.QUESTION_MESSAGE,
				null,
				strandFlags,
				strandFlags[0]);

		return strandType;

	}

	public static String showChromosomeSelectCombo() {
		final List<String> chroms = NetCDFConstants.Defaults.CHROMOSOMES;

		String chr = (String) JOptionPane.showInputDialog(
				null,
				"What chromosome are the genotypes placed at?",
				"Chromosome",
				JOptionPane.QUESTION_MESSAGE,
				null,
				chroms.toArray(),
				chroms.get(0));

		return chr;
	}

	public static GenotypeEncoding showGenotypeCodeSelectCombo() {
		GenotypeEncoding[] gtCode = NetCDFConstants.Defaults.GenotypeEncoding.values();

		GenotypeEncoding strandType = (GenotypeEncoding) JOptionPane.showInputDialog(
				null,
				"What code are the genotypes noted in?",
				"Genotype Encoding",
				JOptionPane.QUESTION_MESSAGE,
				null,
				gtCode,
				gtCode[0]);

		return strandType;
	}

	/**
	 * @deprecated unused
	 */
	public static int showMatrixSelectCombo(StudyKey studyKey) throws IOException {
		List<MatrixMetadata> matrices = MatricesList.getMatricesTable(studyKey);
		final List<String> matrixNames = generateMatrixNames(matrices);
		List<Integer> matrixIDs = new ArrayList<Integer>(matrices.size());
		for (MatrixMetadata matrixMetadata : matrices) {
			matrixIDs.add(matrixMetadata.getMatrixId());
		}

		String selectedRow = (String) JOptionPane.showInputDialog(
				null,
				"What code are the genotypes noted in?",
				"Genotype Encoding",
				JOptionPane.QUESTION_MESSAGE,
				null,
				matrixNames.toArray(new Object[matrixNames.size()]),
				0);

		int selectedMatrix = MatrixKey.NULL_ID;
		if (selectedRow != null) {
			selectedMatrix = matrixIDs.get(matrixNames.indexOf(selectedRow));
		}
		return selectedMatrix;
	}

	public static Integer showConfirmDialogue(String message) {
		return JOptionPane.showConfirmDialog(null, message, "Confirm?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
	}

	public static void showWarningDialogue(String message) {
		JOptionPane.showMessageDialog(null, message, "Warning!", JOptionPane.WARNING_MESSAGE);
	}

	public static void showInfoDialogue(String message) {
		JOptionPane.showMessageDialog(null, message, "Information", JOptionPane.INFORMATION_MESSAGE);
	}

	public static int showConfirmDialog(final String title, final String message) {
		return JOptionPane.showConfirmDialog(
				null,
				message,
				title,
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE);
	}

	public static int showOptionDialogue(String title, String message, String button1, String button2, String button3) {
		Object[] options = {button1,
			button2,
			button3};
		return JOptionPane.showOptionDialog(
				null,
				message,
				title,
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[2]);
	}

	public static void askUserForGTFreqAndHWFriendlyName(GWASinOneGOParams gwasParams) {

		final String friendlyName = showInputBox(Text.Operation.GTFreqAndHWFriendlyName);
		gwasParams.setProceed(friendlyName != null);
		final MarkerCensusOperationParams markerCensusParams = gwasParams.getMarkerCensusOperationParams();
		if (markerCensusParams != null) {
			markerCensusParams.setName(friendlyName);
		}
		gwasParams.setHardyWeinbergOperationName("H&W on " + friendlyName);
	}

	public static String showInputBox(String message) {
		return JOptionPane.showInputDialog(null, message, "Input text...", JOptionPane.PLAIN_MESSAGE);
	}
	//</editor-fold>

	// <editor-fold defaultstate="expanded" desc="FILE OPEN DIALOGUES">
	public static void selectAndSetFileDialog(JTextField textField, final String filter) {
		selectAndSetFileInCurrentDirDialog(GlobalConstants.HOMEDIR, textField, filter);
	}

	public static void selectAndSetFileInCurrentDirDialog(String dir, JTextField textField, final String filter) {
		selectAndSetDialog(textField, dir, filter, JFileChooser.FILES_AND_DIRECTORIES);
	}

	public static File selectAndSetDirectoryDialog(JTextField textField, String dir, final String filter) {
		return selectAndSetDialog(textField, dir, filter, JFileChooser.DIRECTORIES_ONLY);
	}

	private static File selectAndSetDialog(JTextField textField, String dir, final String filter, final int fileSelectionMode) {

		File resultFile = null;
		// Create a file chooser
		fc = new JFileChooser();
		fc.setFileSelectionMode(fileSelectionMode);

		// getting the latest opened dir
//		File tmpFile = new File(dir);
//		if(!tmpFile.exists()){
		String tmpDir = Config.getSingleton().getString(Config.PROPERTY_LAST_OPENED_DIR, GlobalConstants.HOMEDIR);
		fc.setCurrentDirectory(new File(tmpDir));
//		}

		if ((filter != null) && !filter.isEmpty()) {
			fc.setFileFilter(new FileFilter() {
				@Override
				public boolean accept(final File file) {
					return file.getName().toLowerCase().endsWith(filter) || file.isDirectory();
				}

				@Override
				public String getDescription() {

					final String filterDesc;
					if (filter.isEmpty()) {
						filterDesc = "All files";
					} else {
						filterDesc = "\"*" + filter + "\" files";
					}

					return filterDesc;
				}
			});
		}
		final Component windowAncestor = SwingUtilities.getWindowAncestor(textField);
		int returnVal = fc.showOpenDialog(windowAncestor);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			resultFile = fc.getSelectedFile();
			textField.setText(resultFile.getPath());

			// setting the directory to latest opened dir
			Config.getSingleton().putString(Config.PROPERTY_LAST_OPENED_DIR, resultFile.getParent());
		}

		return resultFile;
	}

	public static File selectDirectoryDialog(String propertyName, String dialogTitle, final Component dialogParent) {
		return selectDirectoryDialog(null, propertyName, dialogTitle, dialogParent);
	}

	public static File selectDirectoryDialog(File currentSelection, String dialogTitle, final Component dialogParent) {
		return selectDirectoryDialog(currentSelection, null, dialogTitle, dialogParent);
	}

	private static File selectDirectoryDialog(File currentSelection, String propertyName, String dialogTitle, final Component dialogParent) {

		File resultFile = null;

		// Create a file chooser
		fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		if (dialogTitle != null) {
			fc.setDialogTitle(dialogTitle);
		}

		if (propertyName == null) {
			// use the default directory
			propertyName = Config.PROPERTY_LAST_OPENED_DIR;
		}

		if (currentSelection == null) {
			// getting the latest opened dir
			final String dir = Config.getSingleton().getString(propertyName, GlobalConstants.HOMEDIR);
			fc.setCurrentDirectory(new File(dir));
		} else {
			fc.setCurrentDirectory(currentSelection);
		}

		// show the dialog
		final int returnVal = fc.showOpenDialog(dialogParent);
		// process the users choise
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			resultFile = fc.getSelectedFile();

			// setting the directory to latest opened dir
			Config.getSingleton().putString(propertyName, resultFile.getAbsolutePath());
		}

		return resultFile;
	}

	public static File selectFilesAndDirectoriesDialog(int okOption, final String title, final Component dialogParent) {

		File resultFile = null;
		// Create a file chooser
		fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setDialogTitle(title);

		// Handle open button action.
		if (okOption == JOptionPane.OK_OPTION) {
			// getting the last opened dir
			String dir = Config.getSingleton().getString(Config.PROPERTY_LAST_OPENED_DIR, GlobalConstants.HOMEDIR);
			fc.setCurrentDirectory(new File(dir));

			int returnVal = fc.showOpenDialog(dialogParent);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				resultFile = fc.getSelectedFile();

				// setting the directory to last opened dir
				Config.getSingleton().putString(Config.PROPERTY_LAST_OPENED_DIR, resultFile.getParent());
			}
		}

		return resultFile;
	}
	// </editor-fold>
}
