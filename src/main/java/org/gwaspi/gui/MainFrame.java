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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;
import org.gwaspi.global.Config;
import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.threadbox.TaskQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainFrame extends JFrame {

	private static final Logger log = LoggerFactory.getLogger(MainFrame.class);

	private final JTabbedPane tabs;

	public MainFrame() {
		super(StartGWASpi.constructHighlyVisibleApplicationName());

		ensureColorableProgressBars();

		try {
			// Set cross-platform Java L&F (also called "Metal")
			//UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			// Set System L&F
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException ex) {
			log.warn(null, ex);
		} catch (ClassNotFoundException ex) {
			log.warn(null, ex);
		} catch (InstantiationException ex) {
			log.warn(null, ex);
		} catch (IllegalAccessException ex) {
			log.warn(null, ex);
		}

		this.tabs = new JTabbedPane();

		initGWASpi();
	}

	private static void ensureColorableProgressBars() {

		// Since Java 6, the new Java default Look & Feel is Metal with the Nimbus theme.
		// The problem with this is, that it does not allwo to change the color of progress bars;
		// they are always orange.
		// Thus we use the previous default Metal theme, called Ocean.
		if (UIManager.getLookAndFeel().getClass().equals(MetalLookAndFeel.class)) {
			try {
				// If L&F = "Metal", set the theme
				MetalLookAndFeel.setCurrentTheme(new OceanTheme());
				UIManager.setLookAndFeel(new MetalLookAndFeel());
			} catch (UnsupportedLookAndFeelException ex) {
				log.warn("Unable to switch to the Ocean theme for the Metal Look & Feel. "
						+ "This means we will not have colored progress bars.", ex);
			}
		}
	}

	private void initGWASpi() {

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent windowEvent) {

				TaskQueue.killInstance(); // NOTE This is probably not required

				final boolean jobsPending = TaskQueue.getInstance().isActive();
				if (jobsPending) {
					int decision = Dialogs.showConfirmDialogue(Text.App.jobsStillPending);
					if (decision == JOptionPane.YES_OPTION) {
						windowEvent.getWindow().setVisible(false);
					}
				} else {
					windowEvent.getWindow().setVisible(false);
				}
			}
		});
		setExtendedState(MAXIMIZED_BOTH);
	}

	public void init() {

		setSize(1100, 800);
		setResizable(true);

		GWASpiExplorerPanel panel0 = GWASpiExplorerPanel.getSingleton();
		ProcessTab panel1 = ProcessTab.getSingleton();

		tabs.addTab(Text.App.Tab0, panel0);
		tabs.addTab(Text.App.Tab1, panel1);

		setPreferredSize(new Dimension(1000, 750));
		setLayout(new BorderLayout());
		add(tabs, BorderLayout.CENTER);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		final int maxHeapSize = Config.getSingleton().getInteger(Config.PROPERTY_MAX_HEAP_MB, -1);
		final int maxProcessMarkers =
				Config.getSingleton().getInteger(Config.PROPERTY_MAX_PROCESS_MARKERS, -1);
		if (maxHeapSize > StartGWASpi.MIN_HEAP_SIZE_MB) {
			Dialogs.showInfoDialogue(maxHeapSize + Text.App.memoryAvailable1 + "\n"
					+ Text.App.memoryAvailable2 + maxProcessMarkers + Text.App.memoryAvailable3);
		} else {
			Dialogs.showInfoDialogue(maxHeapSize + Text.App.memoryAvailable1 + "\n"
					+ Text.App.memoryAvailable2 + maxProcessMarkers + Text.App.memoryAvailable3 + "\n"
					+ Text.App.memoryAvailable4);
		}
	}

	public JTabbedPane getTabs() {
		return tabs;
	}
}
