package co.mcsky;

import co.mcsky.struct.SimpleHTML;
import co.mcsky.struct.SimpleURL;
import co.mcsky.util.Report;
import co.mcsky.util.SimpleCrawler;

import java.util.HashSet;
import java.util.LinkedList;

import static java.lang.System.out;

/**
 * A very primitive HTTP crawler.
 */
public class Crawler {

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java Crawler <host name> <port number> <interval>");
            System.exit(1);
        }
        var host = args[0];
        var port = args[1];
        var interval = Integer.parseInt(args[2]); // 1 request per X seconds
        var site = new SimpleURL("http://" + host + ":" + port);

        /*
            Breadth-first search the site.
            Theoretically, it can crawl URLs of arbitrary depth on the site.
        */

        // Only the hosts in the whitelist will be crawled, otherwise skipping and reporting
        var whitelist = new HashSet<String>() {{
            add(site.getHostPort());
        }};
        var crawler = new SimpleCrawler(interval * 1000L, whitelist);

        // Start BFS
        var rootHTML = crawler.request(site);
        var que = new LinkedList<SimpleHTML>();
        var crawled = new HashSet<SimpleURL>();
        var crawledAll = new HashSet<SimpleHTML>(); // Store all what we crawl
        crawled.add(rootHTML.getURL());
        que.add(rootHTML);
        while (!que.isEmpty()) {
            var html = que.remove();
            crawledAll.add(html); // Store all html pages for later analysis (the report)
            var innerURL = html.getInnerURL();
            for (SimpleURL url : innerURL) {
                if (!crawled.contains(url)) {
                    crawled.add(url);
                    que.add(crawler.request(url));
                }
            }
        }

        /*
            Generating report
        */

        out.println("* Crawling has completed...");
        out.println("* Generating report...");
        new Report(site, crawledAll);
    }

}
