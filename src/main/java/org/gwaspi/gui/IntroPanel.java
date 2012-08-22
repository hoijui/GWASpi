package org.gwaspi.gui;

import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.HelpURLs;
import java.awt.BorderLayout;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class IntroPanel extends javax.swing.JPanel {

	// Variables declaration - do not modify
	private static javax.swing.JScrollPane scrl_Logo;
	private static javax.swing.JPanel pnl_Logo;
	private static javax.swing.JScrollPane scroll_IBE;
	private static javax.swing.JPanel pnl_IBE;
	private javax.swing.JList list_Help;
	private javax.swing.JScrollPane scroll_About;
	private javax.swing.JScrollPane scroll_Contact;
	private javax.swing.JScrollPane scroll_Help;
	private javax.swing.JTextPane txtP_About;
	private javax.swing.JTextPane txtP_Contact;
	private javax.swing.JButton btn_exit;
	private javax.swing.JButton btn_preferences;
	private javax.swing.JButton btn_start;
	private ArrayList<Object[]> helpLinksAL;
	// End of variables declaration

	@SuppressWarnings("unchecked")
	public IntroPanel() {
		scrl_Logo = new javax.swing.JScrollPane();
		pnl_Logo = new javax.swing.JPanel();
		scroll_IBE = new javax.swing.JScrollPane();
		pnl_IBE = new javax.swing.JPanel();
		scroll_About = new javax.swing.JScrollPane();
		txtP_About = new javax.swing.JTextPane();
		scroll_Help = new javax.swing.JScrollPane();
		list_Help = new javax.swing.JList();
		scroll_Contact = new javax.swing.JScrollPane();
		txtP_Contact = new javax.swing.JTextPane();
		btn_exit = new javax.swing.JButton();
		btn_preferences = new javax.swing.JButton();
		btn_start = new javax.swing.JButton();


		//setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.App.appName, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("FreeSans", 0, 24))); // NOI18N
		setBorder(javax.swing.BorderFactory.createTitledBorder(""));


		//<editor-fold defaultstate="collapsed" desc="LOGO LAYOUT">
		scrl_Logo.setMaximumSize(new java.awt.Dimension(190, 151));
		scrl_Logo.setPreferredSize(new java.awt.Dimension(190, 151));

		pnl_Logo.setMaximumSize(new java.awt.Dimension(185, 150));
		pnl_Logo.setPreferredSize(new java.awt.Dimension(185, 150));

		javax.swing.GroupLayout pnl_LogoLayout = new javax.swing.GroupLayout(pnl_Logo);
		pnl_Logo.setLayout(pnl_LogoLayout);
		pnl_LogoLayout.setHorizontalGroup(
				pnl_LogoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 188, Short.MAX_VALUE));
		pnl_LogoLayout.setVerticalGroup(
				pnl_LogoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
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

		list_Help.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.Help.aboutHelp, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N
		list_Help.setModel(new javax.swing.AbstractListModel() {
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
		list_Help.addMouseListener(new java.awt.event.MouseAdapter() {
			/*
			 ** check for double click
			 */
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					try {
						list_HelpMouseReleased(evt);
					} catch (IOException ex) {
						Logger.getLogger(IntroPanel.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			}

			private void list_HelpMouseReleased(java.awt.event.MouseEvent evt) throws IOException {
				org.gwaspi.gui.utils.URLInDefaultBrowser.browseHelpURL(helpLinksAL.get(list_Help.getSelectedIndex())[1].toString());
			}
		});
		scroll_Help.setViewportView(list_Help);

		//<editor-fold defaultstate="collapsed" desc="CONTACT + IBE">
		txtP_Contact.setText(Text.App.cite + "\n" + Text.App.contact + "\n" + Text.App.author + "\n" + Text.App.license);
		scroll_Contact.setViewportView(txtP_Contact);

		javax.swing.GroupLayout pnl_IBELayout = new javax.swing.GroupLayout(pnl_IBE);
		pnl_IBE.setLayout(pnl_IBELayout);
		pnl_IBELayout.setHorizontalGroup(
				pnl_IBELayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 254, Short.MAX_VALUE));
		pnl_IBELayout.setVerticalGroup(
				pnl_IBELayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 100, Short.MAX_VALUE));

		scroll_IBE.setViewportView(pnl_IBE);


		//</editor-fold>


		btn_exit.setText(org.gwaspi.global.Text.App.exit);
		btn_exit.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				org.gwaspi.gui.StartGWASpi.exit();
			}
		});

		btn_preferences.setText(org.gwaspi.global.Text.App.preferences);
		btn_preferences.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btn_preferencesActionPerformed(evt);
			}
		});

		btn_start.setText(org.gwaspi.global.Text.App.start);
		btn_start.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btn_startActionPerformed(evt);
			}
		});


		// <editor-fold defaultstate="collapsed" desc="LAYOUT">
		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
				.addComponent(scroll_Help, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 660, Short.MAX_VALUE)
				.addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
				.addComponent(scrl_Logo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(scroll_About, javax.swing.GroupLayout.DEFAULT_SIZE, 464, Short.MAX_VALUE))
				.addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
				.addComponent(scroll_Contact, javax.swing.GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(scroll_IBE, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)))
				.addContainerGap())
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addGap(30, 30, 30)
				.addComponent(btn_preferences)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 433, Short.MAX_VALUE)
				.addComponent(btn_exit)
				.addGap(30, 30, 30)
				.addComponent(btn_start)
				.addGap(29, 29, 29)));
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
				.addComponent(scroll_About, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE)
				.addComponent(scrl_Logo, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(scroll_Help, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)
				.addGap(11, 11, 11)
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(scroll_Contact, javax.swing.GroupLayout.DEFAULT_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addComponent(scroll_IBE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
				.addGap(18, 18, 18)
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
				.addComponent(btn_start)
				.addComponent(btn_exit)
				.addComponent(btn_preferences))
				.addContainerGap()));
		layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[]{scroll_Contact, scroll_IBE});
		// </editor-fold>

		initLogo();
	}

	private void btn_preferencesActionPerformed(java.awt.event.ActionEvent evt) {
		org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new PreferencesPanel();
		org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
	}

	private void btn_startActionPerformed(java.awt.event.ActionEvent evt) {
		try {
			GWASpiExplorerPanel.tree.setSelectionRow(1);
			org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new StudyManagementPanel();
			org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
		} catch (IOException ex) {
			Logger.getLogger(IntroPanel.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	protected void initLogo() {
		URL logoPath = this.getClass().getResource("/img/logo/logo_white.png");
		Icon logo = new ImageIcon(logoPath);

		JButton btn_logo = new JButton(logo);
		btn_logo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
		btn_logo.setBorder(null);
		btn_logo.addMouseListener(new java.awt.event.MouseAdapter() {
			/*
			 ** check for double click
			 */
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				try {
					list_HelpMouseReleased(evt);
				} catch (IOException ex) {
					Logger.getLogger(IntroPanel.class.getName()).log(Level.SEVERE, null, ex);
				}
			}

			private void list_HelpMouseReleased(java.awt.event.MouseEvent evt) throws IOException {
				org.gwaspi.gui.utils.URLInDefaultBrowser.browseHelpURL(helpLinksAL.get(0)[1].toString());
			}
		});

		scrl_Logo.getViewport().add(btn_logo);
		pnl_Logo.add(scrl_Logo, BorderLayout.CENTER);


		URL ibePath = this.getClass().getResource("/img/logo/logo_INB2.png");
		Icon inbLogo = new ImageIcon(ibePath);

		JButton btn_inblogo = new JButton(inbLogo);
		btn_inblogo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
		btn_inblogo.setBorder(null);
		btn_inblogo.addMouseListener(new java.awt.event.MouseAdapter() {
			/*
			 ** check for double click
			 */
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				try {
					list_HelpMouseReleased(evt);
				} catch (IOException ex) {
					Logger.getLogger(IntroPanel.class.getName()).log(Level.SEVERE, null, ex);
				}
			}

			private void list_HelpMouseReleased(java.awt.event.MouseEvent evt) throws IOException {
				org.gwaspi.gui.utils.URLInDefaultBrowser.browseGenericURL("http://www.inab.org/");
			}
		});

		scroll_IBE.getViewport().add(btn_inblogo);
		pnl_IBE.add(scroll_IBE, BorderLayout.CENTER);
	}
}
