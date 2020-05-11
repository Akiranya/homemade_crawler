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
     * @param site    the website to crawl
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
        // TODO 图片的 URLs 也要数进来
        out.printf("Total no of distinct URLs: %s%n", crawled.size());

        /*
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * Print the number of html pages and the number of non-html objects on the site (e.g. images)
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * */
        out.printf("The number of html pages on the site: %s%n",
                   crawled.stream()
                          .filter(u -> u.getStatusCode().isPresent() && u.getStatusCode().get() == StatusCode.OK)
                          .count());
        // TODO 不能只数 <img> tag 的数量，应该要检测是否真的存在图片（文件）
        out.printf("The number of non-html objects on the site: %s%n",
                   crawled.stream()
                          .mapToInt(u -> u.getInnerImageUrls().size())
                          .sum());

        /*
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * The smallest and largest html pages, and their sizes
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * */
        crawled.stream()
               .filter(u -> u.getContentLength().isPresent() && u.getStatusCode().isPresent() &&
                            u.getStatusCode().get().status20x())
               .min(Comparator.comparingInt(u -> u.getContentLength().get()))
               .ifPresent(u -> out.printf("Smallest html page: %s (%s bytes)%n",
                                          u.getURL().toString(),
                                          u.getContentLength().orElse(-1)));
        crawled.stream()
               .filter(u -> u.getStatusCode().isPresent() && u.getContentLength().isPresent() &&
                            u.getStatusCode().get().status20x())
               .max(Comparator.comparingInt(u -> u.getContentLength().get()))
               .ifPresent(u -> out.printf("Largest html page: %s (%s bytes)%n",
                                          u.getURL().toString(),
                                          u.getContentLength().orElse(-1)));

        /*
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * The oldest and the most-recently modified page, and their date/timestamps
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * */
        crawled.stream()
               // 404 html pages have null modified time headers,
               // so we have to ignore them to avoid NPE.
               .filter(u -> u.getModifiedTime().isPresent())
               .min(Comparator.comparing(u -> u.getModifiedTime().get()))
               .filter(u -> u.getModifiedTime().isPresent())
               .ifPresent(u -> out.printf("Oldest modified page: %s (Date: %s)%n",
                                          u.getURL().toString(),
                                          u.getModifiedTime().get()));
        crawled.stream()
               // Same as above
               .filter(u -> u.getModifiedTime().isPresent())
               .max(Comparator.comparing(u -> u.getModifiedTime().get()))
               .filter(u -> u.getModifiedTime().isPresent())
               .ifPresent(u -> out.printf("Most-recently modified page: %s (Date: %s)%n",
                                          u.getURL().toString(),
                                          u.getModifiedTime().get()));

        /*
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * A list of invalid URLs (not) found (404)
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * */
        // TODO To confirm: what status codes should be classified as valid URLs?
        out.println("A list of invalid URLs (not) found (404):");
        crawled.stream()
               .filter(u -> u.getStatusCode().isPresent() && u.getStatusCode().get().status40x())
               .forEach(u -> out.printf(" - %s (Reason: %s)%n",
                                        u.getURL().toString(),
                                        u.getStatusCode().get().toString()));

        /*
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * A list of on-site redirected URLs found (30x) and where they redirect to
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * */
        out.println("A list of on-site redirected URLs:");
        crawled.stream()
               .filter(u -> u.getStatusCode().isPresent() && u.getStatusCode().get().status30x() &&
                            u.getRedirectTo().isPresent() && isOnSite(u.getRedirectTo().get()))
               .forEach(u -> out.printf(" - %s -> %s%n",
                                        u.getURL().toString(),
                                        u.getRedirectTo().get().toString()));

        /*
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * A list of off-site URLs found (either 30x redirects or html references),
         * and whether those sites are valid web servers
         * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         * */
        out.println("A list of off-site URLs found:");
        crawled.stream()
               .filter(u -> !isOnSite(u.getURL()))
               .forEach(u -> out.printf(" - %s -> %s%n",
                                        u.getURL().toString(),
                                        u.isAlive()
                                        ? "web server available"
                                        : "web server unavailable"));
    }

    /**
     * Check whether a URL is on-site or not.
     *
     * @param test the URL to check for whether it is on-site or not
     *
     * @return whether the URL is on-site or not
     */
    private boolean isOnSite(SimpleURL test) {
        return this.site.getHost().equals(test.getHost()) && this.site.getPort() == test.getPort();
    }

}
