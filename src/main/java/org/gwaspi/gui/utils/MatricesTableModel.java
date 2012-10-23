package org.gwaspi.gui.utils;

import java.util.Collection;
import javax.swing.table.DefaultTableModel;
import org.gwaspi.global.Text;
import org.gwaspi.model.MatrixMetadata;

public class MatricesTableModel extends DefaultTableModel {

	public MatricesTableModel(Collection<MatrixMetadata> matrices) {
		super(
				new String[] {
					Text.Matrix.matrixID,
					Text.Matrix.matrix,
					Text.All.description,
					Text.All.createDate
				},
				matrices.size()
				);

		int row = 0;
		for (MatrixMetadata matrix : matrices) {
			int column = 0;
			setValueAt(matrix.getMatrixId(), row, column++);
			setValueAt(matrix.getMatrixFriendlyName(), row, column++);
			setValueAt(matrix.getDescription(), row, column++);
			setValueAt(matrix.getCreationDate(), row, column++);
			row++;
		}
	}
}
