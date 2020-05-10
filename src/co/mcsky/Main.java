package co.mcsky;

import co.mcsky.struct.SimpleHTML;
import co.mcsky.struct.SimpleURL;
import co.mcsky.util.Report;
import co.mcsky.util.SimpleCrawler;

import java.util.HashSet;
import java.util.LinkedList;

import static java.lang.System.in;
import static java.lang.System.out;

public class Main {

    public static void main(String[] args) {
        /*
            Breadth-first search the site.
            Theoretically, it can crawl URLs of arbitrary depth on the site.
        */

        // Where to start the search?
        var targetURL = new SimpleURL("http://comp3310.ddns.net:7880");

        // Only the hosts in the whitelist will be crawled, otherwise skipping and reporting
        var whitelist = new HashSet<String>() {{
            add(targetURL.getHostPort());
        }};
        var interval = 1L; // 1 request per 2 seconds request rate
        var crawler = new SimpleCrawler(interval * 1000L, whitelist);

        // Start BFS
        var rootHTML = crawler.request(targetURL);
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
        out.println("* Generating report...\n");
        new Report(crawledAll);
    }

}
