package co.mcsky.struct;

import co.mcsky.util.StringUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * This class represents a html page related to a instance of {@link SimpleURL}.
 * The html page may or may not exist, depending on whether {@link SimpleURL}
 * links to a valid html page or not during the instantiation of this class.
 * </p>
 *
 * <p>
 * In other words, this class at least contains a instance of {@link SimpleURL}.
 * If the {@link SimpleURL#getRawURL()} is a valid URL (say, we can obtain a
 * HTML page from it, including 40x and 50x) then this class will contain
 * relevant information about the html page.
 * </p>
 *
 * <p>
 * This class provides convenient methods for getting relevant information about
 * its internal html page (say, {@link #getModifiedTime()}). Typically, these
 * getter methods are there for generating a good report for the assignment ;)
 * </p>
 */
public class SimpleHTML {

    private final SimpleURL url; // This URL should be given by the crawler.
    private final String raw;
    private final List<SimpleURL> innerURL;
    private final int contentLength;
    private final StatusCode statusCode;
    private final LocalDateTime modifiedTime;
    private final List<String> innerNonHTMLObjects;
    private final SimpleLocation location;

    public SimpleHTML(SimpleURL url, String raw) {
        this.url = url;

        this.raw = raw; // Raw byte messages from sockets
        this.statusCode = StringUtil.extractStatusCode(raw);

        // We parse the modified time so that we can compare it easily for the report
        this.modifiedTime = Optional.ofNullable(StringUtil.extractModifiedTime(raw))
                                    .map(timeString -> LocalDateTime.parse(timeString, DateTimeFormatter.RFC_1123_DATE_TIME))
                                    .orElse(null);

        this.contentLength = StringUtil.extractContentLength(raw); // This is the size of html page, I think?
        this.innerNonHTMLObjects = StringUtil.extractNonHTMLObjects(raw); // NonHTMLObjects only include images for now
        this.location = Optional.ofNullable(StringUtil.extractLocation(raw))
                                .map(urlString -> new SimpleLocation(this.url, new SimpleURL(urlString)))
                                .orElse(null);

        // Notes: some exceptional URL
        // http://comp3310.ddns.net/B/29.html
        // http://www.canberratimes.com.au/
        // http://comp3310.ddns.net:7880/C/307.html
        // http://www.canberratimes.com.au/
        // http://comp3310.ddns.net:7880/B/23.html (contains canberra times)

        // We try to format the rawURL into meaningful format so that the URL
        // can be recognized and crawled by SimpleCrawler.request(URL).
        this.innerURL = StringUtil.extractURL(raw)
                                  .stream()
                                  .map(urlString -> {
                                      if (urlString.startsWith("http://") || urlString.startsWith("https://")) {
                                          // Case where the URL is already in full format
                                          // e.g.1 http://comp3310.ddns.net/B/29.html
                                          // e.g.2 http://comp3310.ddns.net:7880/C/307.html
                                          return new SimpleURL(urlString);
                                      } else {
                                          // Case where the URL is not in full format
                                          // then we try to format it into full URL.
                                          // e.g. A/30.html, /B/29.html
                                          String baseURL = url.getProtocol() + "://" + url.getHost() + ":" + url.getPort();
                                          String fullURL;
                                          if (urlString.startsWith("/")) {
                                              fullURL = baseURL + urlString;
                                          } else {
                                              fullURL = baseURL + "/" + urlString;
                                          }
                                          return new SimpleURL(fullURL);
                                      }
                                  })
                                  .collect(Collectors.toList());
    }

    /**
     * @return the whole raw HTML page string (say, the raw html string starting
     * with "HTTP/1.1 200 OK ..."
     */
    public String getRaw() {
        return raw;
    }

    /**
     * @return the URL of this page (this is not the URL(s) contained in this
     * html page. For the URL(s) contained in the html page, see {@link
     * #getInnerURL()})
     */
    public SimpleURL getURL() {
        return url;
    }

    /**
     * @return a {@link List} of URLs inside the html page if presenting,
     * otherwise returns empty {@link List}
     */
    public List<SimpleURL> getInnerURL() {
        return innerURL;
    }

    /**
     * @return a {@link List} of non-html objects inside the html page if
     * presenting, otherwise returns empty {@link List}
     */
    public List<String> getNonHTMLObjects() {
        return innerNonHTMLObjects;
    }

    /**
     * @return the {@code Modified-Time} of the html page if presenting,
     * otherwise returns null
     */
    public Optional<LocalDateTime> getModifiedTime() {
        return Optional.ofNullable(modifiedTime);
    }

    /**
     * @return the status code of the html page when it is obtained in the first
     * place. That is, if this html is 30x, then we keep the 30x page instead of
     * the page which it redirects to. If we cannot access to the html page via
     * the URL, then returns {@link StatusCode#UNKNOWN} otherwise
     */
    public StatusCode getStatusCode() {
        return statusCode;
    }

    /**
     * @return the {@code Content-Length} of the html page if presenting. If the
     * html page does not exist at all (say, I/O exception), returns {@code -1}
     * otherwise
     */
    public int getContentLength() {
        return contentLength;
    }

    /**
     * @return the {@code Location} of the html page if presenting, otherwise
     * returns null (as {@code Location} only exists in 30x page)
     */
    public Optional<SimpleLocation> getLocation() {
        return Optional.ofNullable(location);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof SimpleHTML)) {
            return false;
        }
        return obj.hashCode() == this.hashCode();
    }

    // As we use java.utl.Set store distinct html pages,
    // it is necessary to override the hashCode() method.
    // URL should be enough to tell distinct html pages.
    @Override
    public int hashCode() {
        return this.getURL().getRawURL().hashCode();
    }

}
