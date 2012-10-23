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
			setValueAt(operation.getOPName(), row, column++);
			setValueAt(operation.getDescription(), row, column++);
			setValueAt(operation.getCreationDate(), row, column++);
			row++;
		}
	}
}
