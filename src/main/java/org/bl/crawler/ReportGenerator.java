package org.bl.crawler;

import org.apache.commons.io.FileUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import java.io.File;
import java.io.Writer;
import java.io.StringWriter;
import java.io.IOException;

import java.util.ArrayList;

/**
 * Created by blupashko on 21.04.2015.
 */
public class ReportGenerator {
    private final String TEMPLATE = "HtmlReportTemplate.vm";
    private final String HTML_EXTENSION = ".html";
    private final String CSV_EXTENSION = ".csv";
    private static final String DESTINATION_FILE_NAME = "CrawlerReport";
    private Writer writer = new StringWriter();

    public void getReport(String country, int countSites, boolean isCsv) {
        ArrayList<Site> crawler = new AlexaCrawler(countSites).getInfo(country);

        String content = null;
        String extension = null;

        if (isCsv) {
            content = fillCsvReport(country, crawler);
            extension = CSV_EXTENSION;
        } else {
            content = fillHtmlReport(country, crawler);
            extension = HTML_EXTENSION;
        }

        writeFile(country, content, extension);
    }

    private String fillHtmlReport(String country, ArrayList<Site> list) {
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        velocityEngine.init();

        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("sitesList", list);
        velocityContext.put("country", country);
        velocityContext.put("countSites", list.size());

        Template template = velocityEngine.getTemplate(TEMPLATE);

        template.merge(velocityContext, writer);

        return writer.toString();
    }

    private String fillCsvReport(String country, ArrayList<Site> list) {
        StringBuilder context = new StringBuilder("This page contains info about Top " + list.size() + " sites in " + country + "\n"
                + "Country Rank" + ","
                + "Site URL" + ","
                + "Global Rank" + ","
                + "Link to Alexa's page with site stats" + "\n");

        for (Site item : list) {
            context.append(item.getCurrentRank()).append(",").
                    append(item.getUrl()).append(",").
                    append(item.getGlobalRank()).append(",").
                    append(item.getAlexaUrl()).append("\n");
        }
        return context.toString();
    }

    private void writeFile(String country, String content, String extension) {
        File file = new File(DESTINATION_FILE_NAME + country + extension);
        try {
            FileUtils.write(file, content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
