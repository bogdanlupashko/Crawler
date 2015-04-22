package org.bl.crawler;

/**
 * Created by blupashko on 20.04.2015.
 */
public class Site implements Comparable<Site> {
    private String url;
    private String alexaUrl;
    private int globalRank;
    private int currentRank;
    private int order;

    public int getOrder() {
        return order;
    }

    protected void setOrder(int order) {
        this.order = order;
    }

    public String getUrl() {
        return url;
    }

    protected void setUrl(String url) {
        this.url = url;
    }

    public String getAlexaUrl() {
        return alexaUrl;
    }

    protected void setAlexaUrl(String alexaUrl) {
        this.alexaUrl = alexaUrl;
    }

    public int getGlobalRank() {
        return globalRank;
    }

    protected void setGlobalRank(int globalRank) {
        this.globalRank = globalRank;
    }

    public int getCurrentRank() {
        return currentRank;
    }

    protected void setCurrentRank(int currentRank) {
        this.currentRank = currentRank;
    }

    @Override
    public int compareTo(Site site) {
        if (site == null) {
            return -1;
        }
        return Integer.valueOf(this.order).compareTo(Integer.valueOf(site.order));
    }
}
