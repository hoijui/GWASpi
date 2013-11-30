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
import org.gwaspi.model.OperationMetadata;

public class OperationsTableModel extends DefaultTableModel {

	public OperationsTableModel(Collection<OperationMetadata> operations) {
		super(
				new String[] {
					Text.Operation.operationId,
					Text.Operation.operationName,
					Text.All.description,
					Text.All.createDate
				},
				operations.size()
				);

		int row = 0;
		for (OperationMetadata operation : operations) {
			int column = 0;
			setValueAt(operation.getOPId(), row, column++);
			setValueAt(operation.getName(), row, column++);
			setValueAt(operation.getDescription(), row, column++);
			setValueAt(operation.getCreationDate(), row, column++);
			row++;
		}
	}
}
