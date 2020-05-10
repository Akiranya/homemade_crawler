package co.mcsky.struct;

import co.mcsky.util.StringUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;
import static java.util.Optional.ofNullable;

/**
 * This class represents a html page related to a instance of {@link SimpleURL}.
 * The html page may or may not exist, depending on whether {@link SimpleURL}
 * links to a valid html page or not during the instantiation of this class.
 *
 * <p>In other words, this class at least contains a instance of {@link
 * SimpleURL}. If the {@link SimpleURL#getUrl()} is a valid URL (say, we can
 * obtain a HTML page from it, including 40x and 50x) then this class will
 * contain relevant information about the html page.
 *
 * <p>This class provides convenient methods for getting relevant information
 * about its internal html page (say, {@link #getModifiedTime()}). Typically,
 * these getter methods are there for generating a good report for the
 * assignment ;)
 *
 * <p>It is {@code final} so that there is no need to take care of states.
 */
public final class SimpleHTML {

    private static final String NULL_RESPONSE = "";
    private final SimpleURL url;
    private final String response;
    private final List<SimpleURL> innerURL;
    private final int contentLength;
    private final StatusCode statusCode;
    private final LocalDateTime modifiedTime;
    private final List<String> innerNonHTMLObjects;
    private final SimpleLocation location;

    /**
     * @param url      standard URL
     * @param response string representation of the http response from the
     *                 server if presenting, otherwise {@code null} should pass
     *                 into the constructor
     */
    public SimpleHTML(SimpleURL url, String response) {
        this.url = Objects.requireNonNull(url, "URL cannot be null");

        // NULL_RESPONSE means that the URL does not have a valid web server,
        // and this value is determined and given by the crawler
        this.response = Objects.requireNonNullElse(response, NULL_RESPONSE);

        this.statusCode = ofNullable(StringUtil.extractStatusCode(this.response))
                .or(() -> Optional.of("0")) // If the matcher returns null, then parse 0 (UNKNOWN) instead
                .flatMap(s -> Optional.of(parseInt(s)))
                .flatMap(s -> Optional.of(StatusCode.matchCode(s)))
                .get();

        // We parse the modified time so that we can compare it easily for the report
        this.modifiedTime = ofNullable(StringUtil.extractModifiedTime(this.response))
                .map(timeString -> LocalDateTime.parse(timeString, DateTimeFormatter.RFC_1123_DATE_TIME))
                .orElse(null);

        // This is the size of html page (confirmed by Markus)
        // See: https://wattlecourses.anu.edu.au/mod/forum/discuss.php?d=605237
        this.contentLength = ofNullable(StringUtil.extractContentLength(this.response))
                .or((() -> Optional.of("-1")))
                .flatMap(s -> Optional.of(parseInt(s)))
                .get();

        this.innerNonHTMLObjects = StringUtil.extractNonHTMLObjects(this.response);

        this.location = ofNullable(StringUtil.extractLocation(this.response))
                .map(urlString -> new SimpleLocation(this.url, new SimpleURL(urlString)))
                .orElse(null);

        /*
        Notes: some exceptional URL
            http://comp3310.ddns.net/B/29.html
            http://www.canberratimes.com.au/
            http://comp3310.ddns.net:7880/C/307.html
            http://www.canberratimes.com.au/
            http://comp3310.ddns.net:7880/B/23.html (contains canberra times)
        */

        this.innerURL = StringUtil.extractURL(this.response)
                                  .stream()
                                  .map(rawURL -> {
                                      if (rawURL.startsWith("http://") || rawURL.startsWith("https://")) {
                                          // Case where the URL is already in full format
                                          // e.g.1 http://comp3310.ddns.net/B/29.html
                                          // e.g.2 http://comp3310.ddns.net:7880/C/307.html
                                          return new SimpleURL(rawURL);
                                      } else {
                                          // Case where the URL is not in full format
                                          // then we try to format it into full URL.
                                          // e.g. A/30.html, /B/29.html
                                          var baseURL = url.getProtocol() + "://" + url.getHost() + ":" + url.getPort();
                                          if (rawURL.startsWith("/")) {
                                              return new SimpleURL(baseURL + rawURL);
                                          } else {
                                              return new SimpleURL(baseURL + "/" + rawURL);
                                          }
                                      }
                                  })
                                  .collect(Collectors.toList());
    }

    /**
     * @return the raw HTML page strings from the server (say, the one starting
     * with "HTTP/1.1 200 OK ..."
     */
    public String getResponse() {
        return response;
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
     * @return the {@code Modified-Time} of the html page wrapped in Optional
     */
    public Optional<LocalDateTime> getModifiedTime() {
        return ofNullable(modifiedTime);
    }

    /**
     * @return the {@code Status Code} of the html page when it is obtained in
     * the first place. That is, for example, if this html page is 30x, then we
     * keep the 30x page instead of the page which it redirects to. Note that it
     * returns {@link StatusCode#UNKNOWN} if we cannot access to the html page
     * via the URL,
     */
    public StatusCode getStatusCode() {
        return statusCode;
    }

    /**
     * @return the {@code Content-Length} of the html page if presenting,
     * returns {@code -1} otherwise if the html page does not exist at all (say,
     * I/O exception)
     */
    public int getContentLength() {
        return contentLength;
    }

    /**
     * @return the {@code Location} of the html page wrapped in Optional
     */
    public Optional<SimpleLocation> getLocation() {
        return ofNullable(location);
    }

    /**
     * @return true if the URL has a valid web server, false else wise
     */
    public boolean isAlive() {
        return !response.equals(NULL_RESPONSE);
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
        return this.getURL().getUrl().hashCode();
    }

}
