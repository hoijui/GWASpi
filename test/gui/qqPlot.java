/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc. 
 * in the United States and other countries.]
 *
 * -----------------
 * ChartTiming3.java
 * -----------------
 * (C) Copyright 2002-2004, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: ChartTiming3.java,v 1.15 2004/04/26 19:11:53 taqua Exp $
 *
 * Changes
 * -------
 * 29-Oct-2002 : Version 1 (DG);
 *
 */


package gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Draws a scatter plot over and over for 10 seconds.  Reports on how many redraws were achieved.
 * <p>
 * On my PC (SuSE Linux 8.2, JDK 1.4, 256mb RAM, 2.66ghz Pentium) I get 13-14 charts per second.
 *
 */
public class qqPlot implements ActionListener {

    /** A flag that indicates when time is up. */
    private boolean finished;
    
    /**
     * Creates a new application.
     */
    public qqPlot() {
        // nothing to do
    }

    

    /**
     * Runs the test.
     */
    public void run() {
        
        this.finished = false;

        ArrayList<XYSeries> seriesArray = new ArrayList<XYSeries>();
        
        Random generator = new Random();
        int seriesNb = -1;
        XYSeriesCollection dataSeries = new XYSeriesCollection();
        XYSeries seriesData = new XYSeries("X²");
        XYSeries seriesUpper = new XYSeries("Upper bound");
        XYSeries seriesLower = new XYSeries("Lower bound");

        int N = 10000;
        ArrayList<Double> rndChiSqrDist1 = statistics.Chisquare.getChiSquareDistributionDf1AL(N, 1.0f);
        Collections.sort(rndChiSqrDist1);
        ArrayList<Double> rndChiSqrDist2 = statistics.Chisquare.getChiSquareDistributionDf1AL(N, 1.0f);
        Collections.sort(rndChiSqrDist2);
        for (int i = 0; i < rndChiSqrDist1.size(); i++) {
            double obsVal = rndChiSqrDist1.get(i)+(generator.nextDouble()*0.00001);
            double expVal = rndChiSqrDist2.get(i);

            //constant chi-square boundaries
//            double upperVal = expVal*1.05;
//            double lowerVal = expVal*0.95;
//            double upperVal = expVal+Math.pow(Math.E,(1.96*Math.sqrt(1/expVal)));
//            double lowerVal = expVal-Math.pow(Math.E,(1.96*Math.sqrt(1/expVal)));
            double upperVal = expVal+1.96*Math.sqrt(0.05*(1-0.05/N));
            double lowerVal = expVal-1.96*Math.sqrt(0.05*(1-0.05/N));

            seriesData.add(obsVal, expVal);
            seriesLower.add(expVal, lowerVal);
            seriesUpper.add(expVal, upperVal);
        }
        
        dataSeries.addSeries(seriesData);
        dataSeries.addSeries(seriesLower);
        dataSeries.addSeries(seriesUpper);
        final XYDataset data = dataSeries;

        // create a scatter chart...
        final boolean withLegend = true;
        JFreeChart chart = null;

        chart  = ChartFactory.createScatterPlot(
                "QQ-plot", "Obs X²", "Exp X²",
                data,
                PlotOrientation.VERTICAL,
                withLegend,
                false,
                false);

        final XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(false, true);
        renderer.setSeriesPaint(0, Color.blue);
        renderer.setSeriesPaint(1, Color.gray);
        renderer.setSeriesPaint(2, Color.gray);

        renderer.setBaseShapesVisible(true);
        renderer.setBaseShapesFilled(true);
        renderer.setSeriesShape(0, new Rectangle2D.Double(-1.0, -1.0, 2.0, 2.0));
        renderer.setSeriesShape(1, new Rectangle2D.Double(-2.0, 0.0, 4.0, 0.0));
        renderer.setSeriesShape(2, new Rectangle2D.Double(-2.0, 0.0, 4.0, 0.0));

        plot.setRenderer(renderer);

        try {
            ChartUtilities.saveChartAsPNG(new File(System.getProperty("user.home")+"/Desktop/QQ_plot.png"),
                                           chart,
                                           400,
                                           400);

        } catch (IOException e) {
            System.err.println("Problem occurred creating chart.");
        }
        

    }

    /**
     * Receives notification of action events (in this case, from the Timer).
     *
     * @param event  the event.
     */
    public void actionPerformed(final ActionEvent event) {
        this.finished = true;
    }

    
    
    /**
     * Starting point for the application.
     *
     * @param args  ignored.
     */
    public static void main(final String[] args) {
        
        
        final qqPlot app = new qqPlot();
        app.run();

    }

}
