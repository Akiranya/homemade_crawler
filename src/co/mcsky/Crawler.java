package co.mcsky;

import co.mcsky.struct.SimpleHttpResponse;
import co.mcsky.struct.SimpleURL;
import co.mcsky.util.ReportAss2;
import co.mcsky.util.SimpleCrawler;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * A very primitive HTTP crawler.
 */
public class Crawler {

    public static void main(String[] args) {
        /*
         * Just some CLI stuff to get input host and port from users...
         * */
        if (args.length != 3) {
            System.err.println("Usage: java Crawler <host name> <port number> <interval>");
            System.exit(1);
        }
        var host = args[0];
        var port = args[1];
        var interval = Integer.parseInt(args[2]); // 1 request per X seconds
        var site = new SimpleURL("http://" + host + ":" + port);

        /*
         * Now we initialize our crawler with a given rate limit (1 request per 2 seconds)
         * and a whitelist which contains a list of sites which the crawler should crawl on.
         * The crawler should be able to skip any sites that's not in the whitelist.
         * */
        var whitelist = new HashSet<String>() {{
            add(site.getHostPort()); // Only the hosts in the whitelist will be crawled, otherwise skipping and reporting
        }};
        var crawler = new SimpleCrawler(interval * 1000L, whitelist);

        /*
         * Since a site usually contains lots of URLs that locate in arbitrary depth,
         * one way to model how the crawler searches for all the URLs is to think of
         * the index of files on the site as a graph, where we can therefore apply
         * graph search algorithms.
         *
         * Here is a Breadth-first search algorithm. Honestly, I choose BFS for no
         * particular reason..., it is just easy to implement, and happens to work
         * perfectly. I believe that other search algorithms could work as well.
         *
         * Theoretically, it can crawl URLs of arbitrary depth on a site.
         * */
        // This set is used to mark what we have crawled (URL should be enough to tell distinct responses)
        var crawledUrls = new HashSet<SimpleURL>();
        // This set is where we store all responses we have crawled for later analysis (the report)
        var crawledResponse = new HashSet<SimpleHttpResponse>();
        // The queue is necessary for BFS to work
        var que = new LinkedList<SimpleHttpResponse>();

        var initialResponse = crawler.request(site);
        crawledUrls.add(initialResponse.getHead().getURL()); // mark the initial response as crawled
        que.add(initialResponse);
        while (!que.isEmpty()) {
            var currentResponse = que.remove();
            crawledResponse.add(currentResponse);
            var innerUrls = currentResponse.getInnerUrls(); // get all the inner URLs of this http response
            for (SimpleURL url : innerUrls) { // and try to crawl all the inner URLs
                if (!crawledUrls.contains(url)) {
                    crawledUrls.add(url); // mark it as crawled
                    que.add(crawler.request(url));
                }
            }
        }

        /*
            Generating report
        */

        System.out.println("* Crawling has completed...");
        System.out.println("* Generating report...");
        new ReportAss2(site, crawledResponse);
    }

}
