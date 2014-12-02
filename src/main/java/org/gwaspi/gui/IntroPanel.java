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
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.HelpURLs;
import org.gwaspi.gui.utils.URLInDefaultBrowser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntroPanel extends JPanel {

	private static final Logger log = LoggerFactory.getLogger(IntroPanel.class);

	public IntroPanel() {

		//setBorder(BorderFactory.createTitledBorder(null, Text.App.appName, border.TitledBorder.DEFAULT_JUSTIFICATION, border.TitledBorder.DEFAULT_POSITION, new Font("FreeSans", 0, 24))); // NOI18N
		setBorder(BorderFactory.createTitledBorder(""));

		//<editor-fold defaultstate="expanded" desc="LOGOS LAYOUT">
		final JButton btn_logo = new JButton();
		btn_logo.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btn_logo.setAction(new BrowseHomepageAction());
		btn_logo.setMaximumSize(new Dimension(185, 150));
		btn_logo.setPreferredSize(new Dimension(185, 150));

		final JButton btn_logoIbe = new JButton();
		btn_logoIbe.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btn_logoIbe.setAction(new BrowseInbAction());
		//</editor-fold>

		final JScrollPane scroll_About = new JScrollPane();
		final JTextPane txtP_About = new JTextPane();
		txtP_About.setEditable(false);
		txtP_About.setText(Text.App.appDescription);
		scroll_About.setViewportView(txtP_About);

		final JScrollPane scroll_Help = new JScrollPane();
		final JList list_Help = new JList();
		list_Help.setBorder(BorderFactory.createTitledBorder(null, Text.Help.aboutHelp, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N
		list_Help.setModel(new AbstractListModel() {
			@Override
			public int getSize() {
				return HelpURLs.INTRO_LINKS.size();
			}

			@Override
			public Object getElementAt(int i) {
				return HelpURLs.INTRO_LINKS.get(i).getLabel();
			}
		});
		list_Help.addMouseListener(new MouseAdapter() {
			/**
			 * check for double click
			 */
			@Override
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					try {
						list_HelpMouseReleased(evt);
					} catch (IOException ex) {
						log.error(null, ex);
					}
				}
			}

			private void list_HelpMouseReleased(MouseEvent evt) throws IOException {
				URLInDefaultBrowser.browseHelpURL(HelpURLs.INTRO_LINKS.get(list_Help.getSelectedIndex()).getUrl());
			}
		});
		scroll_Help.setViewportView(list_Help);

		//<editor-fold defaultstate="expanded" desc="CONTACT">
		final JScrollPane scroll_Contact = new JScrollPane();
		final JTextPane txtP_Contact = new JTextPane();
		txtP_Contact.setText(
				Text.App.cite + "\n"
				+ "Contact information: " + Text.App.contact + "\n"
				+ "Authors: " + Text.App.authors + "\n"
				+ "License: " + Text.App.license);
		scroll_Contact.setViewportView(txtP_Contact);
		//</editor-fold>

		final JButton btn_exit = new JButton();
		final JButton btn_preferences = new JButton();
		final JButton btn_start = new JButton();

		btn_exit.setAction(new ExitAction());
		btn_preferences.setAction(new OpenPreferencesAction());
		btn_start.setAction(new StartAction());

		// <editor-fold defaultstate="expanded" desc="LAYOUT">
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
				.addComponent(scroll_Help, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 660, Short.MAX_VALUE)
				.addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
				.addComponent(btn_logo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(scroll_About, GroupLayout.DEFAULT_SIZE, 464, Short.MAX_VALUE))
				.addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
				.addComponent(scroll_Contact, GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(btn_logoIbe, GroupLayout.PREFERRED_SIZE, 256, GroupLayout.PREFERRED_SIZE)))
				.addContainerGap())
				.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addGap(30, 30, 30)
				.addComponent(btn_preferences)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 433, Short.MAX_VALUE)
				.addComponent(btn_exit)
				.addGap(30, 30, 30)
				.addComponent(btn_start)
				.addGap(29, 29, 29)));
		layout.setVerticalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
				.addComponent(scroll_About, GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE)
				.addComponent(btn_logo, GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(scroll_Help, GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)
				.addGap(11, 11, 11)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(scroll_Contact, GroupLayout.DEFAULT_SIZE, 122, GroupLayout.PREFERRED_SIZE)
				.addComponent(btn_logoIbe, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(18, 18, 18)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(btn_start)
				.addComponent(btn_exit)
				.addComponent(btn_preferences))
				.addContainerGap()));
		layout.linkSize(SwingConstants.VERTICAL, new Component[] {scroll_Contact, btn_logoIbe});
		// </editor-fold>
	}

	private static class OpenPreferencesAction extends AbstractAction {

		OpenPreferencesAction() {

			putValue(NAME, Text.App.preferences);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			GWASpiExplorerPanel.getSingleton().setPnl_Content(new PreferencesPanel());
			GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
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
				GWASpiExplorerPanel.getSingleton().setPnl_Content(new StudyManagementPanel());
				GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
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
