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

package org.gwaspi.progress;

import com.sun.java.swing.plaf.gtk.GTKLookAndFeel;
import com.sun.java.swing.plaf.motif.MotifLookAndFeel;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;
import javax.swing.plaf.metal.OceanTheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A minimal GUI application for human based, interactive testing of the Swing
 * based GUI for progress display.
 */
public class TestSwing extends javax.swing.JFrame {

	private static final Logger log = LoggerFactory.getLogger(TestSwing.class);

	private static class DummyTask extends AbstractProgressSource<Integer> implements Runnable {

		private static final int NUM_SUB_TASKS_MIN = 2;
		private static final int NUM_SUB_TASKS_MAX = 6;
		private static final int INTERVAL_SLEEP_MS_MIN = 500;
		private static final int INTERVAL_SLEEP_MS_MAX = 3000;
		private static final int SLEEP_TIME_INITIALIZATION = 500;
		private static final int SLEEP_TIME_FINALIZATION = 500;
		private static int lastTaskIndex = 0;
		private final List<Integer> intervallSleepTimes;
		private int intervallSleepTimeTotal;

		public DummyTask(final ProcessInfo superProcessInfo) {
			super(new SubProcessInfo(superProcessInfo, "child with index " + lastTaskIndex++, ""),
					NUM_SUB_TASKS_MIN + new Random().nextInt(NUM_SUB_TASKS_MAX - NUM_SUB_TASKS_MIN + 1));

			this.intervallSleepTimes = new ArrayList<Integer>(getNumIntervals());
			final Random sleepTimeDefiner = new Random();
			this.intervallSleepTimeTotal = 0;
			for (int ii = 0; ii < getNumIntervals(); ii++) {
				final int curIntervalSleepTime
						= INTERVAL_SLEEP_MS_MIN + sleepTimeDefiner.nextInt(INTERVAL_SLEEP_MS_MAX - INTERVAL_SLEEP_MS_MIN + 1);
				this.intervallSleepTimes.add(curIntervalSleepTime);
				this.intervallSleepTimeTotal += curIntervalSleepTime;
			}
		}

//		@Override
//		public void addProgressListener(ProgressListener lst) {
//			System.err.println("");
//			super.addProgressListener(lst);
//		}
//
//		@Override
//		public void removeProgressListener(ProgressListener lst) {
//			System.err.println("");
//			super.removeProgressListener(lst);
//		}

		public List<Integer> getIntervalSleepTimes() {
			return intervallSleepTimes;
		}

		public int getTotalTime() {

			return SLEEP_TIME_INITIALIZATION + intervallSleepTimeTotal + SLEEP_TIME_FINALIZATION;
		}

		static void quietSleep(long milliSeconds) {

			try {
				Thread.sleep(milliSeconds);
			} catch (InterruptedException ex) {
				// did not sleep enough, can't do nothing
			}
		}

		@Override
		public void run() {

			fireStatusChanged(ProcessStatus.INITIALIZING);
			int curIntervalIndex = 0;
			quietSleep(SLEEP_TIME_INITIALIZATION);
			fireStatusChanged(ProcessStatus.RUNNING);
			int intervallSleepTimeSoFar = 0;
			while (curIntervalIndex < getNumIntervals()) {
				final long curIntervalSleepTime = intervallSleepTimes.get(curIntervalIndex);
				quietSleep(curIntervalSleepTime);
				intervallSleepTimeSoFar += curIntervalSleepTime;
				fireProgressHappened((double) intervallSleepTimeSoFar / intervallSleepTimeTotal, curIntervalIndex);
				curIntervalIndex++;
			}
			fireStatusChanged(ProcessStatus.FINALIZING);
			quietSleep(SLEEP_TIME_FINALIZATION);
			fireStatusChanged(ProcessStatus.COMPLEETED);
		}
	}

	private static class SuperDummyTask extends SuperProgressSource implements Runnable {

		private static final int NUM_SUB_TASKS_MIN = 1;
		private static final int NUM_SUB_TASKS_MAX = 4;
		private static final int SLEEP_TIME_INITIALIZATION = 1000;
		private static final int SLEEP_TIME_FINALIZATION = 1000;
		private static int lastTaskIndex = 0;
		private final List<DummyTask> subTasks;
		private final SubTaskEndNotifier subTaskEndNotifier;
		private final int totalSubTime;
		private int curSubTask;

		public SuperDummyTask() {
			super(new DefaultProcessInfo("parent with index " + lastTaskIndex++, ""));

			final int numSubTasks = NUM_SUB_TASKS_MIN + new Random().nextInt(NUM_SUB_TASKS_MAX - NUM_SUB_TASKS_MIN + 1);

			this.subTaskEndNotifier = new SubTaskEndNotifier();
			this.subTasks = new ArrayList<DummyTask>(numSubTasks);
			int tmpTotalTime = 0;
			for (int sti = 0; sti < numSubTasks; sti++) {
				final DummyTask subTask = new DummyTask(getInfo());
				subTasks.add(subTask);
				tmpTotalTime += subTask.getTotalTime();
			}
			this.totalSubTime = tmpTotalTime;

			for (DummyTask subTask : subTasks) {
				addSubProgressSource(subTask, (double) subTask.getTotalTime() / totalSubTime);
			}
		}

		public int getTotalTime() {

			return SLEEP_TIME_INITIALIZATION + totalSubTime + SLEEP_TIME_FINALIZATION;
		}

		public List<DummyTask> getSubTasks() {
			return subTasks;
		}

		private class SubTaskEndNotifier extends AbstractProgressListener<Integer> {

			@Override
			public void statusChanged(ProcessStatusChangeEvent evt) {

				if (evt.getNewStatus().isEnd()) {
					currentSubTaskEnded();
				}
			}
		}

		private void currentSubTaskEnded() {


			if (curSubTask < (subTasks.size() - 1)) {
				curSubTask++;
				scheduleSubTask();
			} else {
				processFinalize();
			}
		}

		private void scheduleSubTask() {

			final DummyTask subTask = subTasks.get(curSubTask);
			subTask.addProgressListener(subTaskEndNotifier);
			final Thread subTaskThread = new Thread(subTask);
			subTaskThread.start();
		}

		private void processFinalize() {

			fireStatusChanged(ProcessStatus.FINALIZING);
			DummyTask.quietSleep(SLEEP_TIME_FINALIZATION);
			fireStatusChanged(ProcessStatus.COMPLEETED);
		}

		@Override
		public void run() {

			fireStatusChanged(ProcessStatus.INITIALIZING);
			DummyTask.quietSleep(SLEEP_TIME_INITIALIZATION);
			fireStatusChanged(ProcessStatus.RUNNING);
			curSubTask = 0;
			scheduleSubTask();
		}
	}

	/**
	 * Creates new form TestSwing
	 */
	public TestSwing() {
		initComponents();

		cbLookAndFeel.removeAllItems();
		cbLookAndFeel.addItem(new MetalLookAndFeel());
		cbLookAndFeel.addItem(new GTKLookAndFeel());
		cbLookAndFeel.addItem(new WindowsLookAndFeel());
		cbLookAndFeel.addItem(new MotifLookAndFeel());

		cbTheme.removeAllItems();
		cbTheme.addItem(new DefaultMetalTheme());
		cbTheme.addItem(new OceanTheme());
//		cbTheme.addItem(new TestTheme());

		setSize(640, 480);
	}

	private void addTaskProgressGui(ProgressSource progressSource) {

		final SwingProgressListener taskProgressDisplay
				= SuperSwingProgressListener.newDisplay(progressSource);
		progressSource.addProgressListener(taskProgressDisplay); // XXX is this required, or even bad?
		pTasks.add(taskProgressDisplay.getMainComponent());
		validate();
	}

	private void initSelectedLookAndFeel() {

		final LookAndFeel selectedLAndF = (LookAndFeel) cbLookAndFeel.getSelectedItem();

		try {
			UIManager.setLookAndFeel(selectedLAndF);

			// If L&F = "Metal", set the theme
			if (selectedLAndF.getClass().equals(MetalLookAndFeel.class)) {
				final MetalTheme selectedMetalTheme = (MetalTheme) cbTheme.getSelectedItem();
				MetalLookAndFeel.setCurrentTheme(selectedMetalTheme);
				UIManager.setLookAndFeel(new MetalLookAndFeel());
			}
		} catch (UnsupportedLookAndFeelException ex) {
			ex.printStackTrace();
		}

		// update the L&F of the already present components
		SwingUtilities.updateComponentTreeUI(this);
		this.pack();
	}

	private void addTaskProgressCmdLine(SuperDummyTask superDummyTask) {

		final Slf4jProgressListener<Integer> slf4jProgressListener
				= new Slf4jProgressListener<Integer>(log, superDummyTask.getInfo());
		superDummyTask.addProgressListener(slf4jProgressListener);
	}

	private void logInfo(SuperDummyTask superDummyTask) {

		log.info("");
		log.info("adding new task ...");
		log.info("name - description: " + superDummyTask.getInfo().getShortName() + " - " + superDummyTask.getInfo().getDescription());
		log.info("sub-tasks: " + superDummyTask.getSubTasks().size());
		log.info("total time (ms): " + superDummyTask.getTotalTime());
		for (DummyTask subTask : superDummyTask.getSubTasks()) {
			log.info("\tsub total time (ms): " + subTask.getTotalTime());
			final List<Integer> intervalSleepTimes = subTask.getIntervalSleepTimes();
			log.info("\tsub intervals: " + intervalSleepTimes.size());
			for (Integer intervalSleepTime : intervalSleepTimes) {
				log.info("\t\tsub interval sleep time (ms): " + intervalSleepTime);
			}
		}
		log.info("");
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pCenter = new javax.swing.JPanel();
        pTasks = new javax.swing.JPanel();
        pButtons = new javax.swing.JPanel();
        bAddTask = new javax.swing.JButton();
        cbLookAndFeel = new javax.swing.JComboBox();
        cbTheme = new javax.swing.JComboBox();
        bSetLookAndFeel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Swing Progress Listener Test");

        pCenter.setLayout(new java.awt.BorderLayout());
        pCenter.add(pTasks, java.awt.BorderLayout.CENTER);

        getContentPane().add(pCenter, java.awt.BorderLayout.CENTER);

        pButtons.setLayout(new java.awt.BorderLayout());

        bAddTask.setText("Add Task");
        bAddTask.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bAddTaskActionPerformed(evt);
            }
        });
        pButtons.add(bAddTask, java.awt.BorderLayout.PAGE_START);

        cbLookAndFeel.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbLookAndFeel.setMaximumSize(new java.awt.Dimension(250, 32767));
        cbLookAndFeel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbLookAndFeelActionPerformed(evt);
            }
        });
        pButtons.add(cbLookAndFeel, java.awt.BorderLayout.PAGE_END);

        cbTheme.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbTheme.setEnabled(false);
        pButtons.add(cbTheme, java.awt.BorderLayout.CENTER);

        bSetLookAndFeel.setText("Set L&F");
        bSetLookAndFeel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bSetLookAndFeelActionPerformed(evt);
            }
        });
        pButtons.add(bSetLookAndFeel, java.awt.BorderLayout.LINE_END);

        getContentPane().add(pButtons, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void bAddTaskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bAddTaskActionPerformed

		final SuperDummyTask superDummyTask = new SuperDummyTask();
		addTaskProgressCmdLine(superDummyTask);
		addTaskProgressGui(superDummyTask);
		logInfo(superDummyTask);
		final Thread superDummyTaskThread = new Thread(superDummyTask);
		DummyTask.quietSleep(1000);
		superDummyTaskThread.start();
    }//GEN-LAST:event_bAddTaskActionPerformed

    private void bSetLookAndFeelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bSetLookAndFeelActionPerformed
        initSelectedLookAndFeel();
    }//GEN-LAST:event_bSetLookAndFeelActionPerformed

    private void cbLookAndFeelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbLookAndFeelActionPerformed

		final LookAndFeel selected = (LookAndFeel) cbLookAndFeel.getSelectedItem();
		final boolean metalSelected;
		if (selected == null) {
			metalSelected = false;
		} else {
			metalSelected = selected.getClass().equals(MetalLookAndFeel.class);
		}
		cbTheme.setEnabled(metalSelected);
    }//GEN-LAST:event_cbLookAndFeelActionPerformed

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		/* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
		 * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
		 */
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(TestSwing.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(TestSwing.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(TestSwing.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(TestSwing.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
        //</editor-fold>

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				new TestSwing().setVisible(true);
			}
		});
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bAddTask;
    private javax.swing.JButton bSetLookAndFeel;
    private javax.swing.JComboBox cbLookAndFeel;
    private javax.swing.JComboBox cbTheme;
    private javax.swing.JPanel pButtons;
    private javax.swing.JPanel pCenter;
    private javax.swing.JPanel pTasks;
    // End of variables declaration//GEN-END:variables
}
