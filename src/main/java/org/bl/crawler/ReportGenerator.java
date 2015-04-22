package org.bl.crawler;

import org.apache.commons.io.FileUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.io.StringWriter;
import java.io.IOException;

import java.util.ArrayList;

/**
 * Created by blupashko on 21.04.2015.
 */
public class ReportGenerator {
    private final String TEMPLATE = "HtmlReportTemplate.vm";
    private static final String DESTINATION_FILE_NAME = "CrawlerReport";

    public void getReport(String country, int countSites, boolean isCsv) {
        if (isCsv) {
            createCsvReport(country, countSites);
        } else {
            createHtmlReport(country, countSites);
        }
    }

    private void createCsvReport(String country, int countSites) {
        ArrayList<Site> list = runCrawler(country, countSites);
        String csvHeader = "This page contains info about Top " + countSites + " sites in " + country;

        try {
            FileWriter writer = createCsvFile(country);

            writer.append(csvHeader);
            writer.append('\n');
            writer.append("Country Rank");
            writer.append(',');
            writer.append("Site URL");
            writer.append(',');
            writer.append("Global Rank");
            writer.append(',');
            writer.append("Link to Alexa's page with site stats");
            writer.append('\n');

            for (int i = 0; i < list.size(); i++) {
                writer.append(String.valueOf(list.get(i).getCurrentRank()));
                writer.append(',');
                writer.append(String.valueOf(list.get(i).getUrl()));
                writer.append(',');
                writer.append(String.valueOf(list.get(i).getGlobalRank()));
                writer.append(',');
                writer.append(String.valueOf(list.get(i).getAlexaUrl()));
                writer.append('\n');
            }
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private FileWriter createCsvFile(String country) throws Exception {
        return new FileWriter(DESTINATION_FILE_NAME + country + ".csv");
    }

    private void createHtmlReport(String country, int countSites) {

        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        velocityEngine.init();

        ArrayList list = runCrawler(country, countSites);

        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("sitesList", list);
        velocityContext.put("country", country);
        velocityContext.put("countSites", countSites);

        Template template = velocityEngine.getTemplate(TEMPLATE);

        Writer writer = new StringWriter();
        template.merge(velocityContext, writer);

        createHtmlFile(country, writer.toString());
    }

    private ArrayList<Site> runCrawler(String country, int countSites) {
        return new AlexaCrawler(countSites).getInfo(country);
    }

    private void createHtmlFile(String country, String content) {
        File file = new File(DESTINATION_FILE_NAME + country + ".html");
        try {
            FileUtils.write(file, content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
