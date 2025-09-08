/*
 *
 *
 * Copyright (C) 2008 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.jasperreports;

import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.export.*;
import net.sf.jasperreports.pdf.JRPdfExporter;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.HtmlResourceHandler;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.common.SipxHibernateDaoSupport;

import java.io.File;

public class JasperReportContextImpl extends SipxHibernateDaoSupport<Object> implements JasperReportContext {
    private static final Log LOG = LogFactory.getLog(JasperReportContextImpl.class);

    private static final String ERROR_FILLING = "Error filling compiled report design ";
    private static final String ERROR_GENERATING = "Error generating report from jasper report ";
    private static final String EXCEPTION_FILLING = "&error.fillingDesignReport";
    private static final String XLS_TOO_MANY_ROWS_EXCEPTION = "&error.xlsTooManyRows";

    private String m_reportsDirectory;
    private String m_tmpDirectory;

    public JasperPrint getJasperPrint(String jasperPath, Map<String, Object> parameters, List<?> dataSource) {
        try {
            return JasperFillManager.fillReport(jasperPath, parameters,
                    new JRBeanCollectionDataSource(dataSource));
        } catch (JRException jrEx) {
            LOG.error(ERROR_FILLING + jasperPath, jrEx);
            throw new UserException(EXCEPTION_FILLING);
        }
    }

    public void generateHtmlReport(JasperPrint jasperPrint, String htmlFile) {
        HtmlExporter exporter = new HtmlExporter();

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));

        // Configure HTML output with image directory and URI mapping
        File outFile = new File(htmlFile);
        File imageDir = new File(outFile.getParentFile(), "images");

        SimpleHtmlExporterOutput exporterOutput = new SimpleHtmlExporterOutput(outFile);
        exporterOutput.setImageHandler(new HtmlResourceHandler() {
            @Override
            public String getResourcePath(String id) {
                return "images/" + id;
            }

            @Override
            public void handleResource(String id, byte[] data) {
                try {
                    File imageFile = new File(imageDir, id);
                    java.nio.file.Files.write(imageFile.toPath(), data);
                } catch (java.io.IOException e) {
                    LOG.error("Error writing image resource: " + id, e);
                }
            }
        });

        exporter.setExporterOutput(exporterOutput);

        try {
            exporter.exportReport();
        } catch (JRException e) {
            LOG.error("Error generating report for " + jasperPrint.getName(), e);
        }
    }

    public void generatePdfReport(JasperPrint jasperPrint, String pdfFile) {
        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(pdfFile));
        try {
            exporter.exportReport();
        } catch (JRException e) {
            LOG.error(ERROR_GENERATING + jasperPrint.getName(), e);
        }
    }

    public void generateCsvReport(JasperPrint jasperPrint, String csvFile) {
        jasperPrint.setProperty("net.sf.jasperreports.export.csv.exclude.origin.keep.first.band.1", "pageHeader");
        jasperPrint.setProperty("net.sf.jasperreports.export.csv.exclude.origin.band.1", "pageFooter");
        jasperPrint.setProperty("net.sf.jasperreports.export.csv.exclude.origin.keep.first.band.2", "columnHeader");
        jasperPrint.setProperty("net.sf.jasperreports.export.csv.exclude.origin.band.2", "columnFooter");
        jasperPrint.setProperty("net.sf.jasperreports.export.csv.exclude.origin.band.title", "title");

        JRCsvExporter exporter = new JRCsvExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleWriterExporterOutput(csvFile));

        SimpleCsvReportConfiguration configuration = new SimpleCsvReportConfiguration();
        exporter.setConfiguration(configuration);

        try {
            exporter.exportReport();
        } catch (JRException e) {
            LOG.error(ERROR_GENERATING + jasperPrint.getName(), e);
        }
    }

    public void generateXlsReport(JasperPrint jasperPrint, String xlsFile) {
        JRXlsxExporter exporter = new JRXlsxExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(xlsFile));

        SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
        configuration.setDetectCellType(true);
        configuration.setOnePagePerSheet(false);
        exporter.setConfiguration(configuration);

        try {
            exporter.exportReport();
        } catch (JRException e) {
            LOG.error(ERROR_GENERATING + jasperPrint.getName(), e);
            throw new UserException(XLS_TOO_MANY_ROWS_EXCEPTION);
        }
    }

    /**
     * Compiles the .jrxml report file to .jasper format in the tmp directory.
     * Returns the path to the compiled .jasper file.
     */
    public String compileReport(String jrxmlFile) {
        try {
            File jrxml = new File(jrxmlFile);
            String jasperName = jrxml.getName().replace(".jrxml", ".jasper");
            File jasperFile = new File(m_tmpDirectory, jasperName);
            JasperCompileManager.compileReportToFile(jrxmlFile, jasperFile.getAbsolutePath());
            return jasperFile.getAbsolutePath();
        } catch (JRException e) {
            LOG.error("Failed to compile Jasper report: " + jrxmlFile, e);
            throw new UserException("&error.compilingReport");
        }
    }

    public void setReportsDirectory(String reportsDirectory) {
        m_reportsDirectory = reportsDirectory;
    }

    public String getReportsDirectory() {
        return m_reportsDirectory;
    }

    public void setTmpDirectory(String tmpDirectory) {
        m_tmpDirectory = tmpDirectory;
    }

    public String getTmpDirectory() {
        return m_tmpDirectory;
    }
}
