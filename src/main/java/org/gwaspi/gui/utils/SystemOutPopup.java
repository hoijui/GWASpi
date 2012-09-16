package org.gwaspi.gui.utils;

import org.gwaspi.global.Text;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

/**
 * http://tanksoftware.com/juk/developer/src/com/tanksoftware/util/RedirectedFrame.java
 * A Java Swing class that captures
 * output from the command line * (eg, System.out.println) RedirectedFrame
 * <p/>
 * This class was downloaded from: Java CodeGuru
 * (http://codeguru.earthweb.com/java/articles/382.shtml) <br/>
 *
 * The original author was Real Gagnon (real.gagnon@tactika.com);
 * William Denniss has edited the code, improving its customizability
 *
 * In brief, this class captures all output to the system and prints it in a
 * frame. You can choose whether or not you want to catch errors, log them to a
 * file and more. For more details, read the constructor method description
 */
public class SystemOutPopup extends JFrame {

	// Class information
	private boolean catchErrors;
	private boolean logFile;
	private String matrixId;
	private int width;
	private int height;
	private int closeOperation;
	private TextArea aTextArea = new TextArea();
	private PrintStream aPrintStream = new PrintStream(new FilteredStream(new ByteArrayOutputStream()));

	/**
	 * Creates a new RedirectFrame. From the moment it is created, all
	 * System.out messages and error messages (if requested) are diverted to
	 * this frame and appended to the log file (if requested)
	 *
	 * for example:
	 * <code>
	 * RedirectedFrame outputFrame = new RedirectedFrame (false, false, null, 700, 600, JFrame.DO_NOTHING_ON_CLOSE);
	 * </code> this will create a new RedirectedFrame that doesn't catch errors,
	 * nor logs to the file, with the dimensions 700x600 and it doesn't close
	 * this frame can be toggled to visible, hidden by a controlling class
	 * by(using the example) outputFrame.setVisible(true|false)
	 *
	 * @param catchErrors set this to true if you want the errors to also be
	 * caught
	 * @param logFile set this to true if you want the output logged
	 * @param fileName the name of the file it is to be logged to
	 * @param width the width of the frame
	 * @param height the height of the frame
	 * @param closeOperation the default close operation (this must be one of
	 * the WindowConstants)
	 */
	public SystemOutPopup(boolean catchErrors,
			boolean logFile,
			String fileName,
			int width,
			int height,
			int closeOperation)
	{
		this.catchErrors = catchErrors;
		this.logFile = logFile;
		this.matrixId = fileName;
		this.width = width;
		this.height = height;
		this.closeOperation = closeOperation;

		Container c = getContentPane();

		setTitle(Text.App.processOutput);
		setSize(width, height);
		c.setLayout(new BorderLayout());
		c.add("Center", aTextArea);
		c.repaint();
		displayLog();

		this.logFile = logFile;

		System.setOut(aPrintStream); // catches System.out messages
		if (catchErrors) {
			System.setErr(aPrintStream); // catches error messages
		}
		// set the default closing operation to the one given
		setDefaultCloseOperation(closeOperation);

		Toolkit tk = Toolkit.getDefaultToolkit();
		Image im = tk.getImage("myicon.gif");
		setIconImage(im);
	}

	private class FilteredStream extends FilterOutputStream {

		FilteredStream(OutputStream aStream) {
			super(aStream);
		}

		@Override
		public void write(byte[] b) throws IOException {
			String aString = new String(b);
			aTextArea.append(aString);
			aPrintStream.flush();
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			String aString = new String(b, off, len);
			aTextArea.append(aString);
			aPrintStream.flush();
			if (logFile) {
				FileWriter aWriter = new FileWriter(matrixId, true);
				aWriter.write(aString);
				aWriter.close();
			}
		}
	}

	private void displayLog() {
		Dimension dim = getToolkit().getScreenSize();
		Rectangle abounds = getBounds();
		Dimension dd = getSize();
		setLocation((dim.width - abounds.width) / 2,
				(dim.height - abounds.height) / 2);
		setVisible(true);
		requestFocus();
	}

	public static void showDefaultProcessOutput() {
		try {
			if (org.gwaspi.constants.cGlobal.OSNAME.toLowerCase().contains("windows")) {
				String logName = org.gwaspi.global.Utils.getURIDate();
				SystemOutPopup sysout = new SystemOutPopup(false, // Catch errors?
						true, // Log to file?
						org.gwaspi.global.Config.getConfigValue("ReportsDir", "") + "/" + logName + ".log", // Log save path
						600, // Width
						400, // Height
						WindowConstants.DISPOSE_ON_CLOSE); // CloseOperation
			} else if (org.gwaspi.constants.cGlobal.OSNAME.toLowerCase().contains("mac")) {
				String logName = org.gwaspi.global.Utils.getURIDate();
				SystemOutPopup sysout = new SystemOutPopup(false, // Catch errors?
						true, // Log to file?
						org.gwaspi.global.Config.getConfigValue("ReportsDir", "") + "/" + logName + ".log", // Log save path
						600, // Width
						400, // Height
						WindowConstants.DISPOSE_ON_CLOSE);   // CloseOperation
			} else {
//				String logName = org.gwaspi.global.Utils.getURIDate();
//				SystemOutPopup_old sysout = new SystemOutPopup_old(false, // Catch errors?
//						true, // Log to file?
//						org.gwaspi.global.Config.getConfigValue("ReportsDir", "") + "/"+logName+".log", // Log save path
//						600, // Width
//						400, // Height
//						WindowConstants.DISPOSE_ON_CLOSE);   // CloseOperation
			}
		} catch (IOException iOException) {
		}
	}

	private void startRunning() {
		URL logoPath = this.getClass().getResource("/img/logo/bigrotation2.gif"); // FIXME this file does not exist!
		Icon logo = new ImageIcon(logoPath);

		JLabel lbl_Running = new JLabel(logo);
		lbl_Running.setBorder(null);
		if (!org.gwaspi.constants.cGlobal.OSNAME.toLowerCase().contains("linux")) {
//			scrl_Gif.getViewport().add(lbl_Running);
//			pnl_GifCenter.add(scrl_Gif, BorderLayout.CENTER);
//
//			scrl_Gif.setVisible(true);
		}
	}
}
