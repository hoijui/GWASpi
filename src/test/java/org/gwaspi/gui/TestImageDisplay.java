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

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class TestImageDisplay extends JFrame {

	private JScrollPane scrl_Image = new javax.swing.JScrollPane();
	private JPanel pnl_Image = new javax.swing.JPanel();

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

//	{
//		addWindowListener(new WindowAdapter() {
//			public void windowClosing(WindowEvent e) {
//				System.exit(1);
//			}
//		});
//		Container contentPane = getContentPane();
//		contentPane.setLayout(null);
//
//		ImageIcon ii = new ImageIcon(Toolkit.getDefaultToolkit().getImage("/media/data/work/moapi/reports/STUDY_1/qq_Comabella Imputed Chr13_Association-Tests_Genotype freq_Default-Affection.png"));
//		ImageViewerSwing imv = new ImageViewerSwing(ii.getImage());
//
//		pnl_Image.add(imv);
//		scrl_Image.add(pnl_Image);
//
//		contentPane.add(pnl_Image);
//		pnl_Image.setBounds(10, 10, 600, 600);
//		scrl_Image.setBounds(0, 0, 800, 800);
////		lb.setBounds(100, 50, 600, 600);
//	}
	public BufferedImage zoomIn(BufferedImage bi, int scale) {
		int width = scale * bi.getWidth();
		int height = scale * bi.getHeight();

		BufferedImage biScale = new BufferedImage(width, height, bi.getType());

		// Cicla dando un valore medio al pixel corrispondente TODO convert comment to english
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				biScale.setRGB(i, j, bi.getRGB(i / scale, j / scale));
			}
		}

		return biScale;
	}
}
