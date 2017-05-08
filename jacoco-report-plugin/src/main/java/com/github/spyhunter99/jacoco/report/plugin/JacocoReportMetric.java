/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.spyhunter99.jacoco.report.plugin;

import java.io.File;

/**
 *
 * @author AO
 */
public class JacocoReportMetric {

    private File reportDir;
    private Double metric;

    public File getReportDir() {
        return reportDir;
    }

    public void setReportDir(File reportDir) {
        this.reportDir = reportDir;
    }

    public Double getMetric() {
        return metric;
    }

    public void setMetric(Double metric) {
        this.metric = metric;
    }
}
