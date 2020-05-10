package co.mcsky.util;

import co.mcsky.struct.SimpleHTML;
import co.mcsky.struct.SimpleURL;
import co.mcsky.struct.StatusCode;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

import static java.lang.System.out;

/**
 * Generating the report that conforms the assignment.
 */
public class Report {

    /**
     * @param crawled a {@link Set} of crawled html pages represented by {@link
     *                SimpleURL}
     */
    public Report(Set<SimpleHTML> crawled) {
        /*
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * Print the total number of distinct URLs found on the site (including any errors and redirects)
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * */
        out.printf("Total no of distinct URLs: %s\n", crawled.size());

        /*
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * Print the number of html pages and the number of non-html objects on the site (e.g. images)
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * */
        out.printf("The number of html pages on the site: %s\n",
                   crawled.stream()
                          .filter(html -> html.getStatusCode() == StatusCode.OK)
                          .count());
        out.printf("The number of non-html objects on the site: %s\n",
                   crawled.stream()
                          .mapToInt(html -> html.getNonHTMLObjects().size())
                          .sum());

        /*
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * The smallest and largest html pages, and their sizes
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * */
        crawled.stream()
               .filter(html -> html.getContentLength() > 0) // This should get us all the html pages we crawled
               .min((Comparator.comparingInt(SimpleHTML::getContentLength)))
               .ifPresentOrElse(html -> out.printf("Smallest html page: %s (%s bytes)\n",
                                                   html.getURL().getUrl(),
                                                   html.getContentLength()),
                                () -> out.println("No smallest html page found."));
        crawled.stream()
               .filter(html -> html.getStatusCode() == StatusCode.OK)
               .max(Comparator.comparingInt(SimpleHTML::getContentLength))
               .ifPresentOrElse(html -> out.printf("Largest html page: %s (%s bytes)\n",
                                                   html.getURL().getUrl(),
                                                   html.getContentLength()),
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
               .min(Comparator.comparing(SimpleHTML::getModifiedTime, Comparator.comparing(Optional::get)))
               .ifPresent(html -> out.printf("Oldest modified page: %s (Date: %s)\n",
                                             html.getURL().getUrl(),
                                             html.getModifiedTime().get()));
        crawled.stream()
               // Same as above
               .filter(html -> html.getModifiedTime().isPresent())
               .max(Comparator.comparing(SimpleHTML::getModifiedTime, Comparator.comparing(Optional::get)))
               .ifPresent(html -> out.printf("Most-recently modified page: %s (Date: %s)\n",
                                             html.getURL().getUrl(),
                                             html.getModifiedTime().get()));

        /*
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * A list of invalid URLs (not) found (404)
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * */
        out.println("A list of invalid URLs (not) found (404):");
        crawled.stream()
               .filter(html -> !html.getStatusCode().isValid())
               .forEach(html -> out.printf(" - %s (Reason: %s %s)\n",
                                           html.getURL().getUrl(),
                                           html.getStatusCode().code,
                                           html.getStatusCode()));

        /*
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * A list of on-site redirected URLs found (30x) and where they redirect to
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * */
        out.println("A list of on-site redirected URLs:");
        crawled.stream()
               .filter(html -> html.getStatusCode().isRedirected())
               .filter(html -> html.getLocation().isPresent())
               .filter(html -> html.getLocation().get().isOnSite())
               .forEach(html -> out.printf(" - %s -> %s\n",
                                           html.getLocation().get().getFrom().getUrl(),
                                           html.getLocation().get().getTo().getUrl()));

        /*
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * A list of off-site URLs found (either 30x redirects or html references),
         * and whether those sites are valid web servers
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * */
        // TODO A list of off-site URLs found
        out.println("A list of off-site URLs found:");
    }

}
