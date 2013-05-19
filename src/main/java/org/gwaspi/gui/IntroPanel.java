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
import java.util.ArrayList;
import java.util.List;
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

	private static final Logger log
			= LoggerFactory.getLogger(IntroPanel.class);

	// Variables declaration - do not modify
	private JScrollPane scrl_Logo;
	private JPanel pnl_Logo;
	private JScrollPane scroll_IBE;
	private JPanel pnl_IBE;
	private JList list_Help;
	private JScrollPane scroll_About;
	private JScrollPane scroll_Contact;
	private JScrollPane scroll_Help;
	private JTextPane txtP_About;
	private JTextPane txtP_Contact;
	private JButton btn_exit;
	private JButton btn_preferences;
	private JButton btn_start;
	private List<Object[]> helpLinksAL;
	// End of variables declaration

	@SuppressWarnings("unchecked")
	public IntroPanel() {
		scrl_Logo = new JScrollPane();
		pnl_Logo = new JPanel();
		scroll_IBE = new JScrollPane();
		pnl_IBE = new JPanel();
		scroll_About = new JScrollPane();
		txtP_About = new JTextPane();
		scroll_Help = new JScrollPane();
		list_Help = new JList();
		scroll_Contact = new JScrollPane();
		txtP_Contact = new JTextPane();
		btn_exit = new JButton();
		btn_preferences = new JButton();
		btn_start = new JButton();


		//setBorder(BorderFactory.createTitledBorder(null, Text.App.appName, border.TitledBorder.DEFAULT_JUSTIFICATION, border.TitledBorder.DEFAULT_POSITION, new Font("FreeSans", 0, 24))); // NOI18N
		setBorder(BorderFactory.createTitledBorder(""));


		//<editor-fold defaultstate="expanded" desc="LOGO LAYOUT">
		scrl_Logo.setMaximumSize(new Dimension(190, 151));
		scrl_Logo.setPreferredSize(new Dimension(190, 151));

		pnl_Logo.setMaximumSize(new Dimension(185, 150));
		pnl_Logo.setPreferredSize(new Dimension(185, 150));

		GroupLayout pnl_LogoLayout = new GroupLayout(pnl_Logo);
		pnl_Logo.setLayout(pnl_LogoLayout);
		pnl_LogoLayout.setHorizontalGroup(
				pnl_LogoLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGap(0, 188, Short.MAX_VALUE));
		pnl_LogoLayout.setVerticalGroup(
				pnl_LogoLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGap(0, 157, Short.MAX_VALUE));
		//</editor-fold>

		helpLinksAL = new ArrayList<Object[]>();
		helpLinksAL.add(HelpURLs.Intro.intro);
		helpLinksAL.add(HelpURLs.Intro.quickstart);
		helpLinksAL.add(HelpURLs.Intro.tutorial);
		helpLinksAL.add(HelpURLs.Intro.loadGts);
		helpLinksAL.add(HelpURLs.Intro.fileFormats);
		helpLinksAL.add(HelpURLs.Intro.GWASinOneGo);


		txtP_About.setEditable(false);
		txtP_About.setText(Text.App.appDescription);
		scroll_About.setViewportView(txtP_About);

		list_Help.setBorder(BorderFactory.createTitledBorder(null, Text.Help.aboutHelp, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N
		list_Help.setModel(new AbstractListModel() {
			private final String[] strings = {helpLinksAL.get(0)[0].toString(),
				helpLinksAL.get(1)[0].toString(),
				helpLinksAL.get(2)[0].toString(),
				helpLinksAL.get(3)[0].toString(),
				helpLinksAL.get(4)[0].toString(),
				helpLinksAL.get(5)[0].toString()};

			public int getSize() {
				return strings.length;
			}

			public Object getElementAt(int i) {
				return strings[i];
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
				URLInDefaultBrowser.browseHelpURL(helpLinksAL.get(list_Help.getSelectedIndex())[1].toString());
			}
		});
		scroll_Help.setViewportView(list_Help);

		//<editor-fold defaultstate="expanded" desc="CONTACT + IBE">
		txtP_Contact.setText(Text.App.cite + "\n" + Text.App.contact + "\n" + Text.App.author + "\n" + Text.App.license);
		scroll_Contact.setViewportView(txtP_Contact);

		GroupLayout pnl_IBELayout = new GroupLayout(pnl_IBE);
		pnl_IBE.setLayout(pnl_IBELayout);
		pnl_IBELayout.setHorizontalGroup(
				pnl_IBELayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGap(0, 254, Short.MAX_VALUE));
		pnl_IBELayout.setVerticalGroup(
				pnl_IBELayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGap(0, 100, Short.MAX_VALUE));

		scroll_IBE.setViewportView(pnl_IBE);


		//</editor-fold>

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
				.addComponent(scrl_Logo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(scroll_About, GroupLayout.DEFAULT_SIZE, 464, Short.MAX_VALUE))
				.addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
				.addComponent(scroll_Contact, GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(scroll_IBE, GroupLayout.PREFERRED_SIZE, 256, GroupLayout.PREFERRED_SIZE)))
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
				.addComponent(scrl_Logo, GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(scroll_Help, GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)
				.addGap(11, 11, 11)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(scroll_Contact, GroupLayout.DEFAULT_SIZE, 102, GroupLayout.PREFERRED_SIZE)
				.addComponent(scroll_IBE, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(18, 18, 18)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(btn_start)
				.addComponent(btn_exit)
				.addComponent(btn_preferences))
				.addContainerGap()));
		layout.linkSize(SwingConstants.VERTICAL, new Component[]{scroll_Contact, scroll_IBE});
		// </editor-fold>

		initLogo();
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

	protected void initLogo() {
		URL logoPath = this.getClass().getResource("/img/logo/logo_white.png");
		Icon logo = new ImageIcon(logoPath);

		JButton btn_logo = new JButton(logo);
		btn_logo.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btn_logo.setBorder(null);
		btn_logo.addMouseListener(new MouseAdapter() {
			/**
			 * check for double click
			 */
			@Override
			public void mouseClicked(MouseEvent evt) {
				try {
					list_HelpMouseReleased(evt);
				} catch (IOException ex) {
					log.error(null, ex);
				}
			}

			private void list_HelpMouseReleased(MouseEvent evt) throws IOException {
				URLInDefaultBrowser.browseHelpURL(helpLinksAL.get(0)[1].toString());
			}
		});

		scrl_Logo.getViewport().add(btn_logo);
		pnl_Logo.add(scrl_Logo, BorderLayout.CENTER);


		URL ibePath = this.getClass().getResource("/img/logo/logo_INB2.png");
		Icon inbLogo = new ImageIcon(ibePath);

		JButton btn_inblogo = new JButton(inbLogo);
		btn_inblogo.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btn_inblogo.setBorder(null);
		btn_inblogo.addMouseListener(new MouseAdapter() {
			/**
			 * check for double click
			 */
			@Override
			public void mouseClicked(MouseEvent evt) {
				try {
					list_HelpMouseReleased(evt);
				} catch (IOException ex) {
					log.error(null, ex);
				}
			}

			private void list_HelpMouseReleased(MouseEvent evt) throws IOException {
				URLInDefaultBrowser.browseGenericURL("http://www.inab.org/");
			}
		});

		scroll_IBE.getViewport().add(btn_inblogo);
		pnl_IBE.add(scroll_IBE, BorderLayout.CENTER);
	}
}
