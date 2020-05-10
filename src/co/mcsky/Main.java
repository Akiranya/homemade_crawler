package co.mcsky;

import co.mcsky.struct.SimpleHTML;
import co.mcsky.struct.SimpleURL;
import co.mcsky.util.Report;
import co.mcsky.util.SimpleCrawler;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import static java.lang.System.out;

public class Main {

    public static void main(String[] args) {
        /*
            Breadth-first search the site.
            Theoretically, it can crawl URLs of arbitrary depth on the site.
        */

        Queue<SimpleHTML> que = new LinkedList<>();
        Set<SimpleURL> crawled = new HashSet<>(); // Use URL to identify distinct pages
        Set<SimpleHTML> crawledAll = new HashSet<>();
        var crawler = new SimpleCrawler(2L * 1000L);
        var rootHTML = crawler.request(new SimpleURL("http://comp3310.ddns.net:7880"));
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
