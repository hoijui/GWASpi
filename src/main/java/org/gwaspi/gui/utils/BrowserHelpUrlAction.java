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

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.gwaspi.global.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shows a Help page URL in an external browser window.
 */
public class BrowserHelpUrlAction extends AbstractAction {

	private static final Logger log
			= LoggerFactory.getLogger(BrowserHelpUrlAction.class);

	private String helpUrlPostfix;

	public BrowserHelpUrlAction(String helpUrlPostfix) {

		this.helpUrlPostfix = helpUrlPostfix;
		putValue(NAME, Text.Help.help);
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		try {
			URLInDefaultBrowser.browseHelpURL(helpUrlPostfix);
		} catch (Exception ex) {
			log.error(null, ex);
		}
	}
}
