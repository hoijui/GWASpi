/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gwaspi.gui;

/**
 *
 * @author u56124
 */
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

public class InfoPanes extends JFrame {

	public InfoPanes() {
		super("LayeredPane Demonstration");
		setSize(200, 150);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		JLayeredPane lp = getLayeredPane();

		// Create 3 buttons
		JPanel top = new JPanel();
		top.setBackground(Color.getHSBColor(0, 5, 50)); //(?,hue[5=yellow, 1=red],lightness [0=0%,100=100%] )
		top.setBounds(20, 20, 50, 50);
		JPanel middle = new JPanel();
		middle.setBackground(Color.getHSBColor(0, 3, 50));
		middle.setBounds(40, 40, 50, 50);

		// Place the buttons in different layers
		lp.add(middle, new Integer(2));
		lp.add(top, new Integer(3));
	}

	public static void main(String[] args) {
		InfoPanes sl = new InfoPanes();
		sl.setVisible(true);
	}
}
