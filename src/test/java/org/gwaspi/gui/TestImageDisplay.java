package org.gwaspi.gui;

/**
 *
 * @author u56124
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class TestImageDisplay extends JFrame {

	JScrollPane scrl_Image = new javax.swing.JScrollPane();
	JPanel pnl_Image = new javax.swing.JPanel();

	public static void main(String[] args) {
		TestImageDisplay t = new TestImageDisplay();
		t.setSize(810, 810);
		t.setVisible(true);

	}

	TestImageDisplay() {
		setTitle(" Demonstration of Scrolling Pane ");
		setSize(250, 150);
		setBackground(Color.gray);

		JPanel tPanel = new JPanel();
		tPanel.setLayout(new BorderLayout());
		getContentPane().add(tPanel);

		Icon image = new ImageIcon("/media/data/work/moapi/reports/STUDY_1/qq_Comabella Imputed Chr13_Association-Tests_Genotype freq_Default-Affection.png");
		JLabel label = new JLabel(image);

		// Creating a Scroll pane component
		scrl_Image = new JScrollPane();
		scrl_Image.getViewport().add(label);
		tPanel.add(scrl_Image, BorderLayout.CENTER);
	}

//    {
//        addWindowListener(new WindowAdapter(){
//            public void windowClosing(WindowEvent e)
//            {
//                System.exit(1);
//            }
//        });
//        Container contentPane=getContentPane();
//        contentPane.setLayout(null);
//
//        ImageIcon ii = new ImageIcon(Toolkit.getDefaultToolkit().getImage("/media/data/work/moapi/reports/STUDY_1/qq_Comabella Imputed Chr13_Association-Tests_Genotype freq_Default-Affection.png"));
//        ImageViewerSwing imv = new ImageViewerSwing(ii.getImage());
//
//        pnl_Image.add(imv);
//        scrl_Image.add(pnl_Image);
//
//        contentPane.add(pnl_Image);
//        pnl_Image.setBounds(10, 10, 600, 600);
//        scrl_Image.setBounds(0, 0, 800, 800);
//        //lb.setBounds(100,50,600,600);
//    }
	public BufferedImage zoomIn(BufferedImage bi, int scale) {
		int width = scale * bi.getWidth();
		int height = scale * bi.getHeight();

		BufferedImage biScale = new BufferedImage(width, height, bi.getType());

		// Cicla dando un valore medio al pixel corrispondente
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				biScale.setRGB(i, j, bi.getRGB(i / scale, j / scale));
			}
		}

		return biScale;
	}
}
