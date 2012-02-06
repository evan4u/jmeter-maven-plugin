package com.lazerycode.jmeter.reporting;

import com.lazerycode.jmeter.JMeterMojo;
import com.lazerycode.jmeter.ReportConfig;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;

import javax.xml.transform.TransformerException;
import java.io.*;
import java.util.List;

/**
 * ReportGenerator encapsules functions for generating reports
 */
//TODO: should ReportGenerator really extend JMeterMojo just for using getLog()?
public class ReportGenerator extends JMeterMojo {

    private ReportConfig reportConfig;

    public ReportGenerator(ReportConfig reportConfig) {
        this.reportConfig = reportConfig;
    }

    /**
     * Create a report for every resultfile in the given list
     *
     * @param resultFiles list of resultfiles
     * @throws MojoExecutionException
     */
    public void makeReport(List<String> resultFiles) throws MojoExecutionException {
        if (reportConfig.isEnable()) {
            try {
                ReportTransformer transformer;
                transformer = new ReportTransformer(getXslt());
                getLog().info(" ");
                getLog().info("Building JMeter Report(s)...");
                for (String resultFile : resultFiles) {
                    final String outputFile = toOutputFileName(resultFile);
                    transformer.transform(resultFile, outputFile);
                    getLog().info(" ");
                    getLog().info("Raw results: " + resultFile);
                    getLog().info("Test report: " + outputFile);
                }
            } catch (FileNotFoundException e) {
                throw new MojoExecutionException("Error writing report file jmeter file.", e);
            } catch (TransformerException e) {
                throw new MojoExecutionException("Error transforming jmeter results", e);
            } catch (IOException e) {
                throw new MojoExecutionException("Error copying resources to jmeter results", e);
            }
        } else {
            getLog().info("Report generation disabled.");
        }
    }

    //=======================================================================================================

    private InputStream getXslt() throws IOException {
        if (reportConfig.getXsltFile() == null) {
            //if we are using the default report, also copy the images out.
            IOUtils.copy(Thread.currentThread().getContextClassLoader().getResourceAsStream("reports/collapse.jpg"), new FileOutputStream(reportConfig.getOutputDirectory().getPath() + File.separator + "collapse.jpg"));
            IOUtils.copy(Thread.currentThread().getContextClassLoader().getResourceAsStream("reports/expand.jpg"), new FileOutputStream(reportConfig.getOutputDirectory().getPath() + File.separator + "expand.jpg"));
            return Thread.currentThread().getContextClassLoader().getResourceAsStream("reports/jmeter-results-detail-report_21.xsl");
        } else {
            return new FileInputStream(reportConfig.getXsltFile());
        }
    }

    /**
     * returns the fileName with the configured reportPostfix
     *
     * @param fileName the String to modify
     * @return modified fileName
     */
    private String toOutputFileName(String fileName) {
        if (fileName.endsWith(".xml")) {
            return fileName.replace(".xml", reportConfig.getPostfix());
        } else {
            return fileName + reportConfig.getPostfix();
        }
    }
}
