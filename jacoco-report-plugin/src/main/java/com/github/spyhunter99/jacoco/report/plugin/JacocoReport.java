/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.spyhunter99.jacoco.report.plugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.doxia.tools.SiteTool;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.util.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * This is our maven reporting plugin
 *
 * @goal jacoco-aggregate
 * @phase site
 */
public class JacocoReport extends AbstractMavenReport {

    /**
     * The vm line separator
     */
    private static final String EOL = System.getProperty("line.separator");

    /**
     * @param locales the list of locales dir to exclude
     * @param defaultLocale the default locale.
     * @return the comma separated list of default excludes and locales dir.
     * @see FileUtils#getDefaultExcludesAsString()
     * @since 1.1
     */
    private static String getDefaultExcludesWithLocales(List<Locale> locales, Locale defaultLocale) {
        String excludesLocales = FileUtils.getDefaultExcludesAsString();
        for (final Locale locale : locales) {
            if (!locale.getLanguage().equals(defaultLocale.getLanguage())) {
                excludesLocales = excludesLocales + ",**/" + locale.getLanguage() + "/*";
            }
        }

        return excludesLocales;
    }

    /**
     * The default locale.
     */
    private Locale defaultLocale;

    /**
     * The available locales list.
     */
    private List<Locale> localesList;

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * A comma separated list of locales supported by Maven. The first valid
     * token will be the default Locale for this instance of the Java Virtual
     * Machine.
     *
     * @parameter expression="${locales}"
     * @readonly
     */
    @Parameter(property = "locales")
    private String locales;

    /**
     * Site renderer.
     *
     * @component
     */
    @Component
    private Renderer siteRenderer;

    /**
     * SiteTool.
     *
     * @component
     *
     */
    @Component
    private SiteTool siteTool;
    /**
     * Directory containing source for apt, fml and xdoc docs.
     *
     * @parameter expression="${basedir}/src/site"
     */
    @Parameter(defaultValue = "${basedir}/src/site", required = true)
    private File siteDirectory;

    /**
     * Directory containing generated sources for apt, fml and xdoc docs.
     *
     * @parameter expression="${project.build.directory}/generated-site"
     * @since 1.1
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-site", required = true)
    private File generatedSiteDirectory;

    /**
     * The temp Site dir to have all site and generated-site files.
     *
     * @since 1.1
     */
    private File siteDirectoryTmp;

    /**
     * The Maven Settings.
     *
     * @parameter expression="${settings}"
     * @since 1.1
     */
    @Parameter(defaultValue = "${settings}", readonly = true, required = true)
    private Settings settings;
    /**
     * The current version of this plugin.
     *
     * @parameter expression="${plugin.version}"
     */
    @Parameter(defaultValue = "${plugin.version}", readonly = true)
    private String pluginVersion;

    @Override
    protected MavenProject getProject() {
        return project;
    }

    // Not used by Maven site plugin but required by API!
    @Override
    protected Renderer getSiteRenderer() {
        return null; // Nobody calls this!
    }

    // Not used by Maven site plugin but required by API!
    // (The site plugin is only calling getOutputName(), the output dir is fixed!)
    @Override
    protected String getOutputDirectory() {
        return null; // Nobody calls this!
    }

    // Abused by Maven site plugin, a '/' denotes a directory path!
    public String getOutputName() {

        //String path = "someDirectoryInTargetSite/canHaveSubdirectory/OrMore";
        String outputFilename = "jacoco";

        // The site plugin will make the directory (and regognize the '/') in the path,
        // it will also append '.html' onto the filename and there is nothing (yes, I tried)
        // you can do about that. Feast your eyes on the code that instantiates
        // this (good luck finding it):
        // org.apache.maven.doxia.module.xhtml.decoration.render.RenderingContext
        return //path + "/" +
                outputFilename;
    }

    public String getName(Locale locale) {
        return "Code Test Coverage";
    }

    public String getDescription(Locale locale) {
        return "An aggregated Jacoco test report";
    }

    @Override
    protected void executeReport(Locale locale) throws MavenReportException {
        if (project.hasParent()) {
            //TODO revisit this in the future, i think 1 VDD per project is sufficient
            //must there may be use cases for individual/module level docs
            return;
        }
        try {

            List<File> jacocoReports = copyResources(project);

            Sink sink = getSink();

            //simple table, module | coverage metric?
            sink.head();
            sink.title();       //html/head/title
            sink.text(getName(locale));
            sink.title_();
            sink.head_();
            sink.body();

            sink.section1();    //div
            sink.sectionTitle1();
            sink.rawText(project.getName() + " - Jacoco Aggregated Code Coverage Report");
            sink.sectionTitle1_();

            sink.section2();    //div
            sink.sectionTitle2();
            sink.rawText("Test Code Coverage");
            sink.sectionTitle2_();

            sink.paragraph();
            sink.table();
            sink.tableRow();
            sink.tableHeaderCell();
            sink.rawText("Module");
            sink.tableHeaderCell_();
            sink.tableHeaderCell();
            sink.rawText("Metric");
            sink.tableHeaderCell_();
            sink.tableRow_();

            //for reach module
            for (int i = 0; i < jacocoReports.size(); i++) {
                sink.tableRow();
                sink.tableCell();
                sink.rawText(jacocoReports.get(i).getParentFile().getName());
                sink.tableCell_();

                sink.tableCell();
                sink.link("jacoco/" + jacocoReports.get(i).getParentFile().getName() + "/" + jacocoReports.get(i).getName() + "/index.html");

                sink.rawText(getMetric(new File(jacocoReports.get(i)+ "/index.html")));
                sink.link_();
                sink.tableCell_();
                sink.tableRow_();
            }

            sink.table_();
            sink.paragraph_();
            sink.section2_();    //div
            sink.section1_();
            //recurse into all sub modules and recursively copy any located jacoco reports
            //`target/site/jacoco-ut`
            //`target/site/jacoco-it`
            //destination is (parent project)
            //'target/site/jacoco/index
            //'target/site/jacoco/${module}/
            //output a doxia document which points to a jacoco index file
            //index file contains.
            //<h1>module name</h1>
            //h2 module name
            //content
//TODO        generatedReport(getSink(), locale);
        } catch (IOException ex) {
            Logger.getLogger(JacocoReport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @return the default locale from <code>siteTool</code>.
     * @see #getAvailableLocales()
     */
    private Locale getDefaultLocale() {
        if (this.defaultLocale == null) {
            this.defaultLocale = getAvailableLocales().get(0);
        }

        return this.defaultLocale;
    }

    /**
     * @return the available locales from <code>siteTool</code>.
     * @see SiteTool#getAvailableLocales(String)
     */
    private List<Locale> getAvailableLocales() {
        if (this.localesList == null) {
            if (siteTool == null) {
                List<Locale> r = new ArrayList<>();
                r.add(Locale.US);
                this.localesList = r;
            } else {
                this.localesList = siteTool.getSiteLocales(locales);
            }
        }

        return this.localesList;
    }

    /**
     * Return the directory for a given Locale and the current default Locale.
     *
     * @param basedir the base directory
     * @param locale a Locale.
     * @return File.
     */
    private File getLocaleDirectory(File basedir, Locale locale) {
        if (locale.getLanguage().equals(getDefaultLocale().getLanguage())) {
            return basedir;
        }

        return new File(basedir, locale.getLanguage());
    }

    /**
     * Copy the from site dir to the to dir.
     *
     * @param from not null
     * @param to not null
     * @throws IOException if any
     * @since 1.1
     */
    private void copySiteDir(final File from, final File to)
            throws IOException {
        if (from == null || !from.exists()) {
            return;
        }

        // copy generated-site
        for (final Locale locale : getAvailableLocales()) {
            String excludes = getDefaultExcludesWithLocales(getAvailableLocales(), getDefaultLocale());
            List<String> siteFiles = FileUtils.getFileNames(siteDirectory, "**/*", excludes, false);
            File siteDirectoryLocale = new File(siteDirectory, locale.getLanguage());
            if (!locale.getLanguage().equals(getDefaultLocale().getLanguage()) && siteDirectoryLocale.exists()) {
                siteFiles = FileUtils.getFileNames(siteDirectoryLocale, "**/*", excludes, false);
            }

            List<String> generatedSiteFiles = FileUtils.getFileNames(from, "**/*", excludes, false);
            File fromLocale = new File(from, locale.getLanguage());
            if (!locale.getLanguage().equals(getDefaultLocale().getLanguage()) && fromLocale.exists()) {
                generatedSiteFiles = FileUtils.getFileNames(fromLocale, "**/*", excludes, false);
            }

            for (final String generatedSiteFile : generatedSiteFiles) {
                if (siteFiles.contains(generatedSiteFile)) {
                    getLog().warn("Generated-site already contains a file in site: " + generatedSiteFile
                            + ". Ignoring copying it!");
                    continue;
                }

                if (!locale.getLanguage().equals(getDefaultLocale().getLanguage())) {
                    if (fromLocale.exists()) {
                        File in = new File(fromLocale, generatedSiteFile);
                        File out = new File(new File(to, locale.getLanguage()), generatedSiteFile);
                        out.getParentFile().mkdirs();
                        FileUtils.copyFile(in, out);
                    }
                } else {
                    File in = new File(from, generatedSiteFile);
                    File out = new File(to, generatedSiteFile);
                    out.getParentFile().mkdirs();
                    FileUtils.copyFile(in, out);
                }
            }
        }
    }

    /**
     * Copy all site and generated-site files in the tmpSiteDirectory.
     * <br/>
     * <b>Note</b>: ignore copying of <code>generated-site</code> files if they
     * already exist in the <code>site</code> dir.
     *
     * @param tmpSiteDir not null
     * @throws IOException if any
     * @since 1.1
     */
    private void prepareTempSiteDirectory(final File tmpSiteDir)
            throws IOException {
        // safety
        tmpSiteDir.mkdirs();

        // copy site
        if (siteDirectory.exists()) {
            FileUtils.copyDirectoryStructure(siteDirectory, tmpSiteDir);
        }

        // Remove SCM files
        List<String> files
                = FileUtils.getFileAndDirectoryNames(tmpSiteDir, FileUtils.getDefaultExcludesAsString(), null, true,
                        true, true, true);
        for (final String fileName : files) {
            final File file = new File(fileName);

            if (file.isDirectory()) {
                FileUtils.deleteDirectory(file);
            } else {
                file.delete();
            }
        }

        copySiteDir(generatedSiteDirectory, tmpSiteDir);
    }

    private List<File> copyResources(MavenProject project) throws IOException {
        if (project == null) {
            return Collections.EMPTY_LIST;
        }
        List<File> outDirs = new ArrayList<>();
        for (int i = 0; i < project.getCollectedProjects().size(); i++) {
            MavenProject p = (MavenProject) project.getCollectedProjects().get(i);

            if ("pom".equalsIgnoreCase(p.getPackaging())) {
                for (int k = 0; k < project.getCollectedProjects().size(); k++) {
                    outDirs.addAll(copyResources((MavenProject) p.getCollectedProjects().get(k)));
                }
            } else {
                File moduleBaseDir = p.getBasedir();
                File target = new File(moduleBaseDir, "target");
                if (target.exists()) {
                    File jacocoUt = new File(moduleBaseDir, "target/site/jacoco-ut/");  //TODO properterize
                    File jacocoIt = new File(moduleBaseDir, "target/site/jacoco-it/");  //TODO properterize

                    if (jacocoIt.exists()) {
                        //since all artifacts should have unique names...this should be ok
                        File dest = new File("target/site/jacoco/" + p.getArtifactId() + "/" + jacocoIt.getName());
                        dest.mkdirs();
                      org.apache.commons.io.FileUtils.copyDirectory(jacocoIt, dest);
                        outDirs.add(dest);
                    }
                    if (jacocoUt.exists()) {
                        //since all artifacts should have unique names...this should be ok
                        File dest = new File("target/site/jacoco/" + p.getArtifactId() + "/" + jacocoUt.getName());
                        dest.mkdirs();
                        org.apache.commons.io.FileUtils.copyDirectory(jacocoUt, dest);
                        outDirs.add(dest);
                    }

                }
            }
        }
        return outDirs;
    }

    private String getMetric(File string) throws IOException {
        Document doc = Jsoup.parse(string, "UTF-8");
        Element table = doc.select("table").get(0);
        Elements rows = table.select("tr");

        double value=0;
        int rowCount=0;
        for (int i = 1; i < rows.size(); i++) { //first row is the col names so skip it.
            Element row = rows.get(i);
            Elements cols = row.select("td");

            if (cols.get(2).text().contains("%")) {
                //this is the code coverage
                try{
                value+=Double.parseDouble(cols.get(2).text().replace("%",""));
                rowCount++;
                }catch (NumberFormatException ex) {
                    
                }
            }
        }
        if (rowCount > 0) {
            return Double.toString(value/(double)rowCount) + "%";
        }
        return "N/A";
        
        
    }
}
