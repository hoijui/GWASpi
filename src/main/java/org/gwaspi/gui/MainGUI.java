package org.gwaspi.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.prefs.Preferences;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gwaspi.constants.cGlobal;
import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.threadbox.SwingDeleterItemList;
import org.gwaspi.threadbox.SwingWorkerItemList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
@Component
@Service(Component.class)
public class MainGUI extends JFrame {

	private static final Logger log = LoggerFactory.getLogger(MainGUI.class);

	public JTabbedPane allTabs;;
	private Preferences prefs;
	@Reference
	private SwingWorkerItemList swingWorkerItemList;
	@Reference
	private SwingDeleterItemList swingDeleterItemList;

	protected void bindSwingWorkerItemList(SwingWorkerItemList swingWorkerItemList) {
		this.swingWorkerItemList = swingWorkerItemList;
	}

	protected void unbindSwingWorkerItemList(SwingWorkerItemList swingWorkerItemList) {

		if (this.swingWorkerItemList == swingWorkerItemList) {
			this.swingWorkerItemList = null;
		}
	}

	protected void bindSwingDeleterItemList(SwingDeleterItemList swingDeleterItemList) {
		this.swingDeleterItemList = swingDeleterItemList;
	}

	protected void unbindSwingDeleterItemList(SwingDeleterItemList swingDeleterItemList) {

		if (this.swingDeleterItemList == swingDeleterItemList) {
			this.swingDeleterItemList = null;
		}
	}

	public MainGUI() {
		super(cGlobal.APP_NAME);
		this.allTabs = new JTabbedPane();

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				int jobsPending = swingWorkerItemList.sizePending() + swingDeleterItemList.sizePending();
				if (jobsPending == 0) {
					we.getWindow().setVisible(false);
				} else {
					int decision = Dialogs.showConfirmDialogue(Text.App.jobsStillPending);
					if (decision == JOptionPane.YES_OPTION) {
						we.getWindow().setVisible(false);
					}
				}
			}
		});

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

		setExtendedState(MAXIMIZED_BOTH);

		setSize(1100, 800);
		setResizable(true);

		GWASpiExplorerPanel panel0 = GWASpiExplorerPanel.getSingleton();
		ProcessTab panel1 = ProcessTab.getSingleton();

		allTabs.addTab(Text.App.Tab0, panel0);
		allTabs.addTab(Text.App.Tab1, panel1);

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(allTabs, GroupLayout.DEFAULT_SIZE, 1000, Short.MAX_VALUE));
		layout.setVerticalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(allTabs, GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE));

		getContentPane().add(allTabs);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
