package org.gwaspi.gui.utils;

import org.gwaspi.global.Text;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shows a Help page URL in an external browser window.
 */
public class BrowserHelpUrlAction extends AbstractAction {

	private final static Logger log
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
