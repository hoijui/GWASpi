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
