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
package org.gwaspi.operations.combi;

import java.awt.Component;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.gwaspi.cli.CombiTestScriptCommand;
import org.gwaspi.gui.utils.MinMaxDoubleVerifier;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;

/**
 * TODO
 */
public class CombiTestParamsGUI extends JPanel {

	public static final String TITLE = "Edit Combi-Test parameters";

	private final CombiTestParams originalCombiTestParams;

	private final JLabel parentMatrixL;
	private final JTextField parentMatrixTF;
	private final JLabel hwOperationL;
	private final JComboBox hwOperationCB;
	private final JLabel hwThresholdL;
	private final JFormattedTextField hwThresholdTF;
	private final JLabel genotypeEncoderL;
	private final JComboBox genotypeEncoderCB;
	private final JLabel resultMatrixL;
	private final JTextField resultMatrixTF;

	public CombiTestParamsGUI(CombiTestParams combiTestParams) {

		this.originalCombiTestParams = combiTestParams;

		this.parentMatrixL = new JLabel();
		this.parentMatrixTF = new JTextField();
		this.hwOperationL = new JLabel();
		this.hwOperationCB = new JComboBox(getAllHWOperationKeys(combiTestParams.getMatrixKey()));
		this.hwThresholdL = new JLabel();
		this.hwThresholdTF = new JFormattedTextField(NumberFormat.getNumberInstance());
		this.genotypeEncoderL = new JLabel();
		this.genotypeEncoderCB = new JComboBox(CombiTestScriptCommand.GENOTYPE_ENCODERS.values().toArray());
		this.resultMatrixL = new JLabel();
		this.resultMatrixTF = new JTextField();

		this.parentMatrixL.setText("parent matrix");
		this.parentMatrixTF.setText(combiTestParams.getMatrixKey().toString());
		this.parentMatrixTF.setEditable(false);
		this.hwOperationL.setText("Hardy-Weinberg operation");
		this.hwOperationL.setLabelFor(this.hwOperationCB);
		this.hwOperationCB.setSelectedItem(combiTestParams.getHardyWeinbergOperationKey());
		this.hwThresholdL.setText("Hardy-Weinberg threshold");
		this.hwThresholdL.setLabelFor(this.hwThresholdTF);
		this.hwThresholdTF.setInputVerifier(new MinMaxDoubleVerifier(0.000001, 1.0));
//		this.hwThresholdTF.setColumns(10);
//		this.hwThresholdTF.addPropertyChangeListener("value", this);
		this.hwThresholdTF.setValue(combiTestParams.getHardyWeinbergThreshold());
		this.genotypeEncoderL.setText("geno-type to SVN feature encoding");
		this.genotypeEncoderL.setLabelFor(this.genotypeEncoderCB);
		this.genotypeEncoderCB.setSelectedItem(combiTestParams.getEncoder());
		this.resultMatrixL.setText("Result matrix name");
		this.resultMatrixL.setLabelFor(this.resultMatrixTF);
		this.resultMatrixTF.setText(combiTestParams.getResultMatrixName());

		this.add(this.parentMatrixL);
		this.add(this.parentMatrixTF);


        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);

		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		GroupLayout.SequentialGroup horizontalGroup = layout.createSequentialGroup();
        horizontalGroup.addGroup(layout.createParallelGroup()
				.addComponent(parentMatrixL)
				.addComponent(hwOperationL)
				.addComponent(hwThresholdL)
				.addComponent(genotypeEncoderL)
				.addComponent(resultMatrixL)
				);
		horizontalGroup.addGroup(layout.createParallelGroup()
				.addComponent(parentMatrixTF)
				.addComponent(hwOperationCB)
				.addComponent(hwThresholdTF)
				.addComponent(genotypeEncoderCB)
				.addComponent(resultMatrixTF)
				);
		layout.setHorizontalGroup(horizontalGroup);


        GroupLayout.SequentialGroup verticalGroup = layout.createSequentialGroup();
        verticalGroup.addGroup(layout.createParallelGroup()
				.addComponent(parentMatrixL)
				.addComponent(parentMatrixTF)
				);
        verticalGroup.addGroup(layout.createParallelGroup()
				.addComponent(hwOperationL)
				.addComponent(hwOperationCB)
				);
        verticalGroup.addGroup(layout.createParallelGroup()
				.addComponent(hwThresholdL)
				.addComponent(hwThresholdTF)
				);
        verticalGroup.addGroup(layout.createParallelGroup()
				.addComponent(genotypeEncoderL)
				.addComponent(genotypeEncoderCB)
				);
        verticalGroup.addGroup(layout.createParallelGroup()
				.addComponent(resultMatrixL)
				.addComponent(resultMatrixTF)
				);
		layout.setVerticalGroup(verticalGroup);
	}

	private OperationKey[] getAllHWOperationKeys(MatrixKey parentMatrixKey) {

		List<OperationMetadata> hwOperations;
		try {
			hwOperations = OperationsList.getOperationsList(parentMatrixKey);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		OperationKey[] hwOperationKeys = new OperationKey[hwOperations.size()];
		int i = 0;
		for (OperationMetadata hwOperation : hwOperations) {
			hwOperationKeys[i] = OperationKey.valueOf(hwOperation);
			i++;
		}

		 return hwOperationKeys;
	}

	public CombiTestParams getCombiTestParams() {

		CombiTestParams combiTestParams = new CombiTestParams(
				originalCombiTestParams.getMatrixKey(), // cause it is not editable
				(OperationKey) hwOperationCB.getSelectedItem(),
				(Double) hwThresholdTF.getValue(),
				(GenotypeEncoder) genotypeEncoderCB.getSelectedItem(),
				resultMatrixTF.getText()
				);

		return combiTestParams;
	}

	public static CombiTestParams chooseCombiTestParams(Component parentComponent, CombiTestParams combiTestParams) {

		CombiTestParamsGUI combiTestParamsGUI = new CombiTestParamsGUI(combiTestParams);

		int selectedValue = JOptionPane.showConfirmDialog(
				parentComponent,
				combiTestParamsGUI,
				TITLE,
				JOptionPane.OK_CANCEL_OPTION);

		CombiTestParams returnCombiTestParams;
		if (selectedValue == JOptionPane.OK_OPTION) {
			returnCombiTestParams = combiTestParamsGUI.getCombiTestParams();
		} else {
			// return the original parameters,
			// if the user clicked on the [Cancel] button
			returnCombiTestParams = combiTestParams;
		}

		return returnCombiTestParams;
	}
}
