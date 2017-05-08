/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.spyhunter99.jacoco.report.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author AO
 */
public class JacocoItem {

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 73 * hash + Objects.hashCode(this.moduleName);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final JacocoItem other = (JacocoItem) obj;
        if (!Objects.equals(this.moduleName, other.moduleName)) {
            return false;
        }
        return true;
    }

    private String moduleName;
    private List<JacocoReportMetric> reportDirs = new ArrayList<JacocoReportMetric>();

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public List<JacocoReportMetric> getReportDirs() {
        return reportDirs;
    }

    public void setReportDirs(List<JacocoReportMetric> reportDirs) {
        this.reportDirs = reportDirs;
    }
}
