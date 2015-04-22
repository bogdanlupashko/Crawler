package org.bl.crawler;

/**
 * Created by blupashko on 20.04.2015.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Enter {
    public static final int THREAD_COUNT = 2;
    public static Logger LOG = LoggerFactory.getLogger(Enter.class.getName());
    private static boolean isCvs = false;
    private static int countSites = 100;
    protected static ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        for (int i = 0; i < args.length; i++) {
            if ("-format".equals(args[i])) {
                if ("csv".equals(args[i + 1])) {
                    isCvs = true;
                } else {
                    continue;
                }
            } else if ("-count".equals(args[i])) {
                countSites = Integer.valueOf(args[i + 1]);

            } else if (isCountryCodeISO(args[i])) {
                Enter.submitTask(args[i], countSites, isCvs);
            }
        }
        executorService.shutdown();
        // Wait until all threads are finish
        while (!executorService.isTerminated()){

        }
        LOG.info("Done in: {} s.", (System.currentTimeMillis() - start) / 1000f);
    }

    private static boolean isCountryCodeISO(String arg){
        for (String item : Locale.getISOCountries()){
            if (item.equals(arg)) {
                return true;
            }
        }
        return false;
    }

    private static void submitTask(final String country, final int countSites, final boolean isCvs) {

        LOG.info("Submitting country: {}", country);
        Runnable task = new Runnable() {
            @Override
            public void run() {
                new ReportGenerator().getReport(country, countSites, isCvs);
            }
        };
        try {
            executorService.submit(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
