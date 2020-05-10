package co.mcsky.util;

import co.mcsky.struct.SimpleHTML;
import co.mcsky.struct.SimpleURL;
import co.mcsky.struct.StatusCode;

import java.util.Comparator;
import java.util.Set;

import static java.lang.System.out;

/**
 * Generating the report that conforms the assignment.
 */
public class Report {

    private final SimpleURL site;

    /**
     * @param crawled a {@link Set} of crawled html pages represented by {@link
     *                SimpleURL}
     */
    public Report(SimpleURL site, Set<SimpleHTML> crawled) {
        this.site = site;

        /*
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * Print the total number of distinct URLs found on the site (including any errors and redirects)
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * */
        out.printf("Total no of distinct URLs: %s%n", crawled.size());

        /*
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * Print the number of html pages and the number of non-html objects on the site (e.g. images)
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * */
        out.printf("The number of html pages on the site: %s%n",
                   crawled.stream()
                          .filter(u -> u.getStatusCode().isPresent())
                          .filter(u -> u.getStatusCode().get() == StatusCode.OK)
                          .count());
        out.printf("The number of non-html objects on the site: %s%n",
                   crawled.stream()
                          .mapToInt(u -> u.getNonHTMLObjects().size())
                          .sum());

        /*
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * The smallest and largest html pages, and their sizes
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * */
        crawled.stream()
               .filter(u -> u.getStatusCode().isPresent())
               .filter(u -> u.getContentLength().isPresent())
               .filter(u -> u.getStatusCode().get() == StatusCode.OK)
               .min(Comparator.comparingInt(u -> u.getContentLength().get()))
               .ifPresent(u -> out.printf("Smallest html page: %s (%s bytes)%n",
                                          u.getURL().toString(),
                                          u.getContentLength().orElse(-1)));
        crawled.stream()
               .filter(u -> u.getStatusCode().isPresent())
               .filter(u -> u.getContentLength().isPresent())
               .filter(u -> u.getStatusCode().get() == StatusCode.OK)
               .max(Comparator.comparingInt(u -> u.getContentLength().get()))
               .ifPresentOrElse(html -> out.printf("Largest html page: %s (%s bytes)%n",
                                                   html.getURL().toString(),
                                                   html.getContentLength().orElse(-1)),
                                () -> out.println("No largest html page found."));

        /*
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * The oldest and the most-recently modified page, and their date/timestamps
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * */
        crawled.stream()
               // 404 html pages have null modified time headers,
               // so we have to ignore them to avoid NPE.
               .filter(html -> html.getModifiedTime().isPresent())
               .min(Comparator.comparing(u -> u.getModifiedTime().get()))
               .filter(html -> html.getModifiedTime().isPresent())
               .ifPresent(html -> out.printf("Oldest modified page: %s (Date: %s)%n",
                                             html.getURL().toString(),
                                             html.getModifiedTime().get()));
        crawled.stream()
               // Same as above
               .filter(html -> html.getModifiedTime().isPresent())
               .max(Comparator.comparing(u -> u.getModifiedTime().get()))
               .filter(html -> html.getModifiedTime().isPresent())
               .ifPresent(html -> out.printf("Most-recently modified page: %s (Date: %s)%n",
                                             html.getURL().toString(),
                                             html.getModifiedTime().get()));

        /*
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * A list of invalid URLs (not) found (404)
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * */
        out.println("A list of invalid URLs (not) found (404):");
        crawled.stream()
               .filter(u -> u.getStatusCode().isPresent())
               .filter(u -> !u.getStatusCode().get().isValid())
               .forEach(html -> out.printf(" - %s (Reason: %s %s)%n",
                                           html.getURL().toString(),
                                           html.getStatusCode().get().code,
                                           html.getStatusCode().get()));

        /*
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * A list of on-site redirected URLs found (30x) and where they redirect to
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * */
        out.println("A list of on-site redirected URLs:");
        crawled.stream()
               .filter(u -> u.getStatusCode().isPresent())
               .filter(html -> html.getStatusCode().get().isRedirected())
               .filter(html -> html.getRedirectTo().isPresent())
               .filter(html -> isOnSite(html.getRedirectTo().get()))
               .forEach(html -> out.printf(" - %s -> %s%n",
                                           html.getURL().toString(),
                                           html.getRedirectTo().get().toString()));

        /*
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * A list of off-site URLs found (either 30x redirects or html references),
         * and whether those sites are valid web servers
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * */
        out.println("A list of off-site URLs found:");
        crawled.stream()
               .filter(html -> !isOnSite(html.getURL()))
               .forEach(html -> out.printf(" - %s -> %s%n",
                                           html.getURL().toString(),
                                           html.isAlive()
                                           ? "web server available"
                                           : "web server unavailable"));
    }

    private boolean isOnSite(SimpleURL test) {
        return this.site.getHost().equals(test.getHost()) && this.site.getPort() == test.getPort();
    }

}
