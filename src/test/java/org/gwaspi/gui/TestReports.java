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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CombinedRangeXYPlot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestReports extends javax.swing.JPanel {

	private static final Logger log = LoggerFactory.getLogger(TestReports.class);

	public static void main(final String[] args) throws InterruptedException, FileNotFoundException, IOException {

		File plinkFile = new File("/home/fernando/work/Moapi/input/plink.assoc");
		File outputFile = new File("/home/fernando/work/Moapi/input/plink.png");
		Set<String> redMarkers = new HashSet<String>();
		createCombinedPNGFromAssocUnadjLogPvsPos(plinkFile, outputFile, redMarkers, 4048, 700);

		log.info("This is a main test");
	}

	public static File createCombinedPNGFromAssocUnadjLogPvsPos(File plinkReport, File outputFile, Set<String> redMarkers, int width, int height) throws FileNotFoundException, IOException {
		// Generating XY scatter plot with loaded data
		CombinedRangeXYPlot combinedPlot = org.gwaspi.reports.PlinkReportLoaderCombined.loadAssocUnadjLogPvsPos(plinkReport, redMarkers);

		JFreeChart chart = new JFreeChart("P value x Chr position", JFreeChart.DEFAULT_TITLE_FONT, combinedPlot, true);

		try {
			ChartUtilities.saveChartAsPNG(outputFile,
					chart,
					width,
					height);
		} catch (IOException ex) {
			log.error("Problem occurred creating chart", ex);
		}

		return null;
	}
}
