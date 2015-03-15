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

package org.gwaspi.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.HelpURLs;
import org.gwaspi.gui.utils.URLInDefaultBrowser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntroPanel extends JPanel {

	private static final Logger log = LoggerFactory.getLogger(IntroPanel.class);

	public IntroPanel() {

		final GridBagLayout layout = new GridBagLayout();
		final int gapSpace = 5;
		// we use every second row and column as a spacer
		layout.columnWidths = new int[] {0, gapSpace, 0, gapSpace, 0};
		layout.rowHeights = new int[] {0, gapSpace, 0, gapSpace, 0, gapSpace, 0};
		setLayout(layout);

		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 0.0;
		gridBagConstraints.weighty = 0.0;

		setBorder(GWASpiExplorerPanel.createRegularTitledBorder(""));

		final JButton btn_logo = new JButton();
		btn_logo.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btn_logo.setAction(new BrowseHomepageAction());
		btn_logo.setMaximumSize(new Dimension(185, 150));
		btn_logo.setPreferredSize(new Dimension(185, 150));
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		add(btn_logo, gridBagConstraints);

		final JButton btn_logoIbe = new JButton();
		btn_logoIbe.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btn_logoIbe.setAction(new BrowseInbAction());
		gridBagConstraints.gridx = 4;
		gridBagConstraints.gridy = 4;
		add(btn_logoIbe, gridBagConstraints);

		final JScrollPane scroll_About = new JScrollPane();
		final JTextPane txtP_About = new JTextPane();
		txtP_About.setEditable(false);
		txtP_About.setText(Text.App.appDescription);
		// we need this, because JTextPane deals wrongly with the line breaks in the text
		// resulting in a way too big preffered width
		txtP_About.setPreferredSize(new Dimension(200, 150));
		scroll_About.setViewportView(txtP_About);
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 3;
		gridBagConstraints.weightx = 1.0;
		add(scroll_About, gridBagConstraints);

		final JScrollPane scroll_Help = new JScrollPane();
		final JList list_Help = new JList();
		list_Help.setBorder(GWASpiExplorerPanel.createRegularTitledBorder(Text.Help.aboutHelp)); // NOI18N
		list_Help.setModel(new HelpListModel());
		list_Help.addMouseListener(new HelpListMouseListener());
		scroll_Help.setViewportView(list_Help);
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.gridwidth = 5;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		add(scroll_Help, gridBagConstraints);

		final JScrollPane scroll_Contact = new JScrollPane();
		final JTextPane txtP_Contact = new JTextPane();
		txtP_Contact.setText(
				Text.App.cite + "\n"
				+ "Contact information: " + Text.App.contact + "\n"
				+ "Authors: " + Text.App.authors + "\n"
				+ "License: " + Text.App.license);
		scroll_Contact.setViewportView(txtP_Contact);
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.gridwidth = 3;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 0.0;
		add(scroll_Contact, gridBagConstraints);
		// limit the preffered width a bit
		txtP_Contact.setPreferredSize(new Dimension(400, 100));

		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.weightx = 0.0;
		gridBagConstraints.fill = GridBagConstraints.NONE;

		final JButton btn_preferences = new JButton();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 6;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		add(btn_preferences, gridBagConstraints);

		final JButton btn_exit = new JButton();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 6;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		add(btn_exit, gridBagConstraints);

		final JButton btn_start = new JButton();
		gridBagConstraints.gridx = 4;
		gridBagConstraints.gridy = 6;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		add(btn_start, gridBagConstraints);

		btn_exit.setAction(new ExitAction());
		btn_preferences.setAction(new OpenPreferencesAction());
		btn_start.setAction(new StartAction());
	}

	private static class HelpListModel extends AbstractListModel {

		@Override
		public int getSize() {
			return HelpURLs.INTRO_LINKS.size();
		}

		@Override
		public Object getElementAt(final int index) {
			return HelpURLs.INTRO_LINKS.get(index).getLabel();
		}
	}

	private static class HelpListMouseListener extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent evt) {
			// check for double click
			if (evt.getClickCount() == 2) {
				try {
					listHelpMouseReleased(evt);
				} catch (IOException ex) {
					log.error(null, ex);
				}
			}
		}

		private void listHelpMouseReleased(MouseEvent evt) throws IOException {
			URLInDefaultBrowser.browseHelpURL(HelpURLs.INTRO_LINKS.get(((JList) evt.getSource()).getSelectedIndex()).getUrl());
		}
	}

	private static class OpenPreferencesAction extends AbstractAction {

		OpenPreferencesAction() {

			putValue(NAME, Text.App.preferences);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			GWASpiExplorerPanel.getSingleton().setPnlContent(new PreferencesPanel());
			GWASpiExplorerPanel.getSingleton().getScrlContent().setViewportView(GWASpiExplorerPanel.getSingleton().getPnlContent());
		}
	}

	private static class StartAction extends AbstractAction {

		StartAction() {

			putValue(NAME, Text.App.start);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				GWASpiExplorerPanel.getSingleton().getTree().setSelectionRow(1);
				GWASpiExplorerPanel.getSingleton().setPnlContent(new StudyManagementPanel());
				GWASpiExplorerPanel.getSingleton().getScrlContent().setViewportView(GWASpiExplorerPanel.getSingleton().getPnlContent());
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}

	private static class ExitAction extends AbstractAction {

		ExitAction() {

			putValue(NAME, Text.App.exit);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			StartGWASpi.exit();
		}
	}

	private static class BrowseHomepageAction extends AbstractAction {

		BrowseHomepageAction() {

			final URL logoPath = this.getClass().getResource("/img/logo/logo_white.png");
			final Icon logo = new ImageIcon(logoPath);

			putValue(LARGE_ICON_KEY, logo);
			putValue(NAME, "");
		}

		@Override
		public void actionPerformed(ActionEvent evt) {

			try {
				URLInDefaultBrowser.browseHelpURL(HelpURLs.INTRO_LINKS.get(0).getUrl());
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}

	private static class BrowseInbAction extends AbstractAction {

		BrowseInbAction() {

			final URL logoPath = this.getClass().getResource("/img/logo/logo_INB2.png");
			final Icon logo = new ImageIcon(logoPath);

			putValue(LARGE_ICON_KEY, logo);
			putValue(NAME, "");
		}

		@Override
		public void actionPerformed(ActionEvent evt) {

			try {
				URLInDefaultBrowser.browseGenericURL("http://www.inab.org/");
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}
}
