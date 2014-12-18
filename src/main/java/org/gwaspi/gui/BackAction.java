/*
 * Copyright (C) 2014 Universitat Pompeu Fabra
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

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.gwaspi.global.Text;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.StudyKey;

public class BackAction extends AbstractAction {

	private final Object parentElementKey;

	private BackAction(final Object parentElementKey) {

		this.parentElementKey = parentElementKey;
		putValue(NAME, Text.All.Back);
	}

	public BackAction(final StudyKey parentElementKey) {
		this((Object) parentElementKey);

		checkParent();
	}

	public BackAction(final DataSetKey parentElementKey) {
		this((Object) parentElementKey);

		checkParent();
	}

	public BackAction() {
		this((Object) null);

		// We shall not check for valid parent here,
		// because we want to select the trees root node
	}

	private void checkParent() {

		final boolean hasParent = (parentElementKey != null);
		if (!hasParent) {
			putValue(SHORT_DESCRIPTION, "There is no parent");
		}
		setEnabled(hasParent);
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		GWASpiExplorerPanel.getSingleton().selectNode(parentElementKey);
	}
}
