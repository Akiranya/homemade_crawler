package co.mcsky;

import java.util.*;

import static java.lang.System.out;

public class Main {

    public static void main(String[] args) {
        SimpleCrawler crawler = new SimpleCrawler(2L * 1000L);

        /* Breadth-first search the site */

        Queue<HTMLWrapper> que = new LinkedList<>();
        Set<SimpleURL> crawled = new HashSet<>(); // Use URL to identify distinct pages
        Set<HTMLWrapper> crawledFull = new HashSet<>();
        HTMLWrapper rootHTML = crawler.request(new SimpleURL("http://comp3310.ddns.net:7880"));
        crawled.add(rootHTML.getURL());
        que.add(rootHTML);
        while (!que.isEmpty()) {
            HTMLWrapper html = que.remove();
            crawledFull.add(html); // Store all html pages for later analysis (the report)
            List<SimpleURL> innerURL = html.getInnerURL();
            for (SimpleURL url : innerURL) {
                if (!crawled.contains(url)) {
                    crawled.add(url);
                    que.add(crawler.request(url));
                }
            }
        }

        /* Generating report */

        out.println("* Crawling has completed...");
        out.println("* Generating report...\n");

        // Print the total number of distinct URLs found on the site (including any errors and redirects)
        out.printf("Total no of distinct URLs: %s\n", crawledFull.size());

        // Print the number of html pages and the number of non-html objects on the site (e.g. images)
        out.printf("The number of html pages on the site: %s\n",
                   crawledFull.stream()
                              .filter(html -> html.getStatusCode() == HTTPStatusCode.OK)
                              .count());
        out.printf("The number of non-html objects on the site: %s\n",
                   crawledFull.stream()
                              .mapToInt(html -> html.getNonHTMLObjects().size())
                              .sum());

        // The smallest and largest html pages, and their sizes
        crawledFull.stream()
                   .filter(html -> html.getContentLength() > 0) // This should get us all the html pages we crawled
                   .min((Comparator.comparingInt(HTMLWrapper::getContentLength)))
                   .ifPresentOrElse(html -> out.printf("Smallest html page: %s (%s bytes)\n", html.getURL().getRawURL(), html.getContentLength()),
                                    () -> out.println("No smallest html page found."));
        crawledFull.stream()
                   .filter(html -> html.getStatusCode() == HTTPStatusCode.OK)
                   .max(Comparator.comparingInt(HTMLWrapper::getContentLength))
                   .ifPresentOrElse(html -> out.printf("Largest html page: %s (%s bytes)\n", html.getURL().getRawURL(), html.getContentLength()),
                                    () -> out.println("No largest html page found."));

        // The oldest and the most-recently modified page, and their date/timestamps
        crawledFull.stream()
                   // 404 html pages have null modified time headers,
                   // so we have to ignore them to avoid NPE.
                   .filter(html -> html.getModifiedTime() != null)
                   .min(Comparator.comparing(HTMLWrapper::getModifiedTime))
                   .ifPresentOrElse(html -> out.printf("Oldest modified page: %s (Date: %s)\n", html.getURL().getRawURL(), html.getModifiedTime()),
                                    () -> out.println("No oldest modified page found."));
        crawledFull.stream()
                   // Same as above
                   .filter(html -> html.getModifiedTime() != null)
                   .max(Comparator.comparing(HTMLWrapper::getModifiedTime))
                   .ifPresentOrElse(html -> out.printf("Most-recently modified page: %s (Date: %s)\n", html.getURL().getRawURL(), html.getModifiedTime()),
                                    () -> out.println("No most-recently modified page found."));

        // A list of invalid URLs (not) found (404)
        out.println("A list of invalid URLs (not) found (404):");
        crawledFull.stream()
                   //                   .filter(html -> !html.getStatusCode().isValid())
                   .filter(html -> html.getStatusCode() == HTTPStatusCode.NOT_FOUND)
                   .forEach(html -> out.printf(" - %s (Reason: %s %s)\n", html.getURL().getRawURL(), html.getStatusCode().code, html.getStatusCode()));

        // A list of on-site redirected URLs found (30x) and where they redirect to
        out.println("A list of on-site redirected URLs:");
        crawledFull.stream()
                   // TODO filter only on-site redirected URLs
                   .filter(html -> html.getStatusCode().isRedirected())
                   .forEach(html -> out.printf(" - %s -> %s\n", html.getURL().getRawURL(), html.getLocation().getRawURL()));

        // A list of off-site URLs found (either 30x redirects or html references), and whether those sites are valid web servers
        // TODO A list of off-site URLs found
    }

}
