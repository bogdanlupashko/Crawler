package org.bl.crawler;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by blupashko on 20.04.2015.
 */
public class AlexaCrawler {
    public static final int THREAD_COUNT = 7;
    public static Logger LOG = LoggerFactory.getLogger(AlexaCrawler.class.getName());
    private static final String xPathTopSites = "//*[@class='desc-paragraph']/a";
    private static final String xPathTopSitesOrder = "//*[@class='count']";
    private static final String xPathCurrentSite = "//*[@class='metrics-data align-vmiddle']";
    public static final String targetMainUrl = "http://www.alexa.com";
    private static final String targetCountryUrl = "/topsites/countries";
    private static final String currentSiteUrl = "/siteinfo/";
    private int countSites;
    private ArrayList<Site> sites;
    private ExecutorService executorSitesRange;

    public AlexaCrawler(int countSites) {
        this.countSites = countSites;
    }

    private WebClient getClient() {
        WebClient client = new WebClient();
        client.getOptions().setCssEnabled(false);
        client.getOptions().setUseInsecureSSL(true);
        client.getOptions().setThrowExceptionOnFailingStatusCode(false);
        client.getOptions().setThrowExceptionOnScriptError(false);
        client.getOptions().setRedirectEnabled(false);
        client.getOptions().setAppletEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);
        client.getOptions().setPopupBlockerEnabled(true);
        client.setCssErrorHandler(getCssErrorHandler());
        return new WebClient();
    }

    private ErrorHandler getCssErrorHandler() {
        return new ErrorHandler() {
            @Override
            public void warning(CSSParseException exception) throws CSSException {
            }

            @Override
            public void error(CSSParseException exception) throws CSSException {
            }

            @Override
            public void fatalError(CSSParseException exception) throws CSSException {
            }
        };
    }

    private void init() {
        sites = new ArrayList<Site>();
        urls = new HashSet<String>();
        sites = new ArrayList<Site>();
        executorSitesRange = Executors.newFixedThreadPool(THREAD_COUNT);
    }

    public ArrayList<Site> getInfo(final String country) {
        init();
        getSites(country);
        executorSitesRange.shutdown();
        // Wait until all threads are finish
        while (!executorSitesRange.isTerminated()) {

        }
        Collections.sort(sites);
        while (sites.size() > countSites) {
            sites.remove(sites.size()-1);
        }
        return sites;
    }

    private void submitTask(final String href, final int order) {

        LOG.info("Submitting url: {}", href);
        Runnable task = new Runnable() {
            @Override
            public void run() {
                getSiteRanks(href, order);
            }
        };
        try {
            executorSitesRange.submit(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Set<String> urls;

    private void add(String href, int order) {
        if (urls.add(href)) {
            submitTask(href, order);
        }
    }

    private boolean isAllAdded() {
        return sites.size() >= countSites;
    }

    private void getSites(String country) {
        int j = 0;
        WebClient client = getClient();
        while (urls.size() < countSites) {
            HtmlPage page = null;
            try {
                String href = targetMainUrl + targetCountryUrl + ";" + j++ + "/" + country;
                LOG.debug("Href: {}", href);
                page = client.getPage(href);
            } catch (IOException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
                continue;
            }
            ArrayList<DomElement> list = (ArrayList<DomElement>) page.getByXPath(xPathTopSites);
            ArrayList<DomElement> listOrder = (ArrayList<DomElement>) page.getByXPath(xPathTopSitesOrder);
            for (int i = 0; i < list.size(); i ++) {
                add(list.get(i).getTextContent().toLowerCase(), Integer.valueOf(listOrder.get(i).getTextContent()));
            }
        }
        client.close();
    }

    private void getSiteRanks(String siteUrl, int order) {
        if (isAllAdded()) {
            return;
        }
        LOG.info("Processing url: {}", siteUrl);
        HtmlPage page = null;
        Site site = new Site();
        sites.add(site);

        try {
            WebClient client = getClient();
            page = client.getPage(targetMainUrl + currentSiteUrl + siteUrl);
            client.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            return;
        }
        site.setOrder(order);
        List<DomElement> list = (List<DomElement>) page.getByXPath(xPathCurrentSite);
        site.setAlexaUrl(targetMainUrl + currentSiteUrl + siteUrl);
        site.setGlobalRank(Integer.valueOf(list.get(0).getTextContent().replace(",", "")));
        site.setCurrentRank(Integer.valueOf(list.get(1).getTextContent().replace(",", "")));
        site.setUrl("http://" + siteUrl);
    }
}
