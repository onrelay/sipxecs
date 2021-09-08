/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.site.admin.commserver;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.lang.time.DateUtils;
import org.apache.cxf.common.jaxb.JAXBUtils.S2JJAXBModel;
import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.bean.EvenOdd;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.apache.tapestry.IAsset;
import org.apache.tapestry.annotations.Asset;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.general.DefaultKeyedValuesDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.Minute;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.sipfoundry.sipxconfig.cdr.CdrGraphBean;
import org.sipfoundry.sipxconfig.commserver.imdb.TimeRegistrationStatistics;
import org.sipfoundry.sipxconfig.components.SipxBasePage;
import org.sipfoundry.sipxconfig.registrar.RegistrationContext;
import org.sipfoundry.sipxconfig.registrar.RegistrationMetrics;
import org.sipfoundry.sipxconfig.site.cdr.CdrReports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Displays active and expired registrations
 */
public abstract class Registrations extends SipxBasePage implements PageBeginRenderListener {
    private static final String EMPTY_TITLE = "";
    
    private static final String PIECHART_SECTIONLABEL_FORMAT = "{0} = {1} ({2})";
    
    public static final String PAGE = "admin/commserver/Registrations";
    
    private static final Logger LOG = LoggerFactory.getLogger(Registrations.class);

    @InjectObject(value = "spring:registrationContext")
    public abstract RegistrationContext getRegistrationContext();

    @Bean
    public abstract EvenOdd getRowClass();

    @Bean(initializer = "maximumFractionDigits=2,minimumFractionDigits=2")
    public abstract DecimalFormat getTwoDigitDecimal();

    public abstract void setMetricsProperty(RegistrationMetrics registrationMetrics);

    public abstract RegistrationMetrics getMetricsProperty();
    
    @Asset("/sipxconfig/images/saved.png")
    public abstract IAsset getChart();
    
    public void pageBeginRender(PageEvent event_) {
        getMetrics();
        getImageChart();
    }

    /**
     * Retrieves registration metrics object. Can be called multiple times during rewind/render
     * and it'll lazily initialize registrations metrics only the first time it is called.
     *
     * Workaround for Tapestry 4.0 table model problem, in some case table model is retrieved
     * before pageBenginRender gets called
     *
     * @return properly initialized registration metrics object
     */
    public RegistrationMetrics getMetrics() {
        RegistrationMetrics metrics = getMetricsProperty();
        if (metrics != null) {
            return metrics;
        }
        long startRenderingTime = System.currentTimeMillis() / DateUtils.MILLIS_PER_SECOND;
        metrics = new RegistrationMetrics();
        metrics.setRegistrations(getRegistrationContext().getRegistrations());
        metrics.setStartTime(startRenderingTime);
        setMetricsProperty(metrics);
        return metrics;
    }
    
    private XYDataset createDataset( ) {
        final TimeSeriesCollection dataset = new TimeSeriesCollection();
        List<TimeRegistrationStatistics> timeRegStatsList = getRegistrationContext().getTimeRegStats();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date pastDayDate = calendar.getTime();
        final TimeSeries s1 = new TimeSeries("Total Registrations", Minute.class);
        final TimeSeries s2 = new TimeSeries("Active Registrations", Minute.class);
        for (TimeRegistrationStatistics trStat : timeRegStatsList) {
            if(trStat.getTime().after(pastDayDate)) {
                s1.add(new Minute(trStat.getTime()), trStat.getTotal());            
                s2.add(new Minute(trStat.getTime()), trStat.getActive());
            }
        }
        dataset.addSeries(s1);
        dataset.addSeries(s2);

        return dataset;
     }     

     private JFreeChart createChart( final XYDataset dataset ) {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(             
           "Time Registration chart", 
           "Date",              
           "Value",              
           dataset,             
           true,              
           false,              
           false);
        final XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer lineRenderer = new XYLineAndShapeRenderer(true, false);
        lineRenderer.setSeriesPaint(0, Color.blue);
        lineRenderer.setSeriesPaint(1, Color.red);
        lineRenderer.setBaseStroke(new BasicStroke(3.0f));
        lineRenderer.setAutoPopulateSeriesStroke(false);
        plot.setRenderer(lineRenderer);
        DateAxis dateAxis = (DateAxis)plot.getDomainAxis();
        dateAxis.setTickUnit(new DateTickUnit(DateTickUnit.HOUR, 1, new SimpleDateFormat("dd'hr'HH")));
        return chart;
     }    
    
    public Image getImageChart() {
        BufferedImage image = null;
        try {
            // Create a chart with the dataset
            JFreeChart chart = createChart(createDataset());
            // Create and return the image
            BufferedImage chartImage = chart.createBufferedImage(1216, 256);
            File chartFile = new File("/var/sipxdata/tmp/sipxconfig/webapp/images/saved.png");
            try (OutputStream out = new FileOutputStream(chartFile)) {
              ImageIO.write(chartImage, "png", out);
            } catch (IOException e) {
                LOG.error("Cannot write registration chart image : " + e);
            }
        } catch (Exception ex) {
            LOG.error("Cannot create registration chart image : " + ex);   
        }
        return image;
    }
}
