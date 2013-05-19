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
