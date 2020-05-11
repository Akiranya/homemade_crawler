package co.mcsky.struct;

import co.mcsky.util.StringUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

/**
 * This class represents a html page and a related instance of {@link
 * SimpleURL}. This html page may or may not exist, depending on whether {@link
 * SimpleURL#toString()} points to a valid html page or not, at the
 * instantiation of this class.
 *
 * <p>In other words, this class at least contains a instance of {@link
 * SimpleURL}. If the {@link SimpleURL} is a valid URL (say, we can obtain a
 * HTML page from it, including 40x and 50x) then this class will contain
 * relevant information about this html page.
 *
 * <p>This class provides convenient methods for getting relevant information
 * about its internal html page. Typically, these getter methods are there for
 * generating a good report for the assignment.
 */
public class SimpleHTML {

    private static final String NULL_RESPONSE = "";
    private final SimpleURL url;
    private final String response;
    private final List<SimpleURL> innerURL;
    private final int contentLength;
    private final StatusCode statusCode;
    private final LocalDateTime modifiedTime;
    private final List<String> innerNonHTMLObjects;
    private final SimpleURL location;
    private final boolean alive;

    /**
     * Creates a HTML object. If the URL cannot establish a connection, then
     * {@code null} should pass into {@code response} and {@code false} should
     * pass into {@code alive}.
     *
     * @param url      standard URL
     * @param response string representation of the http response from the
     *                 server if present, otherwise {@code null} must pass into
     *                 the constructor to indicate that the web server where the
     *                 URL resides is not available
     * @param alive    whether the web server whether the URL resides is alive
     *                 or not
     */
    public SimpleHTML(SimpleURL url, String response, boolean alive) {
        this.url = Objects.requireNonNull(url, "URL cannot be null");
        this.response = Objects.requireNonNullElse(response, NULL_RESPONSE);
        this.alive = alive;
        this.statusCode = ofNullable(StringUtil.extractStatusCode(this.response))
                .flatMap(s -> of(parseInt(s)))
                .flatMap(s -> of(StatusCode.matchCode(s)))
                .orElse(null);
        this.modifiedTime = ofNullable(StringUtil.extractModifiedTime(this.response))
                .flatMap(timeString -> of(LocalDateTime.parse(timeString, DateTimeFormatter.RFC_1123_DATE_TIME)))
                .orElse(null);
        /*
         * Content-Length is the size of this html page (confirmed by Markus)
         * See: https://wattlecourses.anu.edu.au/mod/forum/discuss.php?d=605237
         * */
        this.contentLength = ofNullable(StringUtil.extractContentLength(this.response))
                .flatMap(s -> of(parseInt(s)))
                .orElse(-1);
        this.innerNonHTMLObjects = StringUtil.extractNonHTMLObjects(this.response);
        this.location = ofNullable(StringUtil.extractLocation(this.response))
                .flatMap(u -> {
                /*
                    The literal URL in the field of Location may not have the same port
                    as its "parent" html page. In other words, non-standard ports are not
                    explicitly referenced in http redirection.

                    Nevertheless, in the example above, our browsers will correctly redirect
                    to the correct URL even if the port is different.

                    We have to deal with such cases as it would cause the counting URLs wrong.

                    It might be OK to let the SimpleCrawler to handle such case,
                    but to keep my code simple and organized, SimpleCrawler should always
                    seek EXACTLY whatever URL (including port) passed into it.

                    So I'm going to handle such case here.

                    See: https://wattlecourses.anu.edu.au/mod/forum/discuss.php?d=603754
                */
                    var to = new SimpleURL(u);
                    var realTo = to.getProtocol() + "://" +
                                 to.getHost() + ":" +
                                 this.url.getPort() + // We modify the port to its "parent's"
                                 to.getPath();
                    return of(new SimpleURL(realTo));
                })
                .orElse(null);
        /*
            Notes: some exceptional URL
                http://comp3310.ddns.net:7880/B/25.html (contains images)
                http://comp3310.ddns.net:7880/C/307.html (contains 404)
                http://www.canberratimes.com.au/ (off-site url)
                http://comp3310.ddns.net:7880/B/23.html (contains canberra times)
        */
        /*
         * Always store URLs in full format for the purpose of comparing!
         * */
        this.innerURL = StringUtil.extractURL(this.response)
                                  .stream()
                                  .map(rawURL -> {
                                      if (rawURL.startsWith("http://") || rawURL.startsWith("https://")) {
                                          return new SimpleURL(rawURL);
                                      } else {
                                          var base = url.getProtocol() + "://" + url.getHost() + ":" + url.getPort();
                                          if (rawURL.startsWith("/")) {
                                              return new SimpleURL(base + rawURL);
                                          } else {
                                              return new SimpleURL(base + url.getDirectory() + rawURL);
                                          }
                                      }
                                  })
                                  .collect(Collectors.toList());
    }

    /**
     * @return the raw http responses from the server (say, the string starting
     * with "HTTP/1.1 200 OK ..."
     */
    public String getResponse() {
        return response;
    }

    /**
     * @return the URL pointing to this page
     */
    public SimpleURL getURL() {
        return url;
    }

    /**
     * @return a {@link List} of URLs inside this html page if present,
     * otherwise returns empty {@link List}
     */
    public List<SimpleURL> getInnerURL() {
        return innerURL;
    }

    /**
     * @return a {@link List} of non-html objects inside this html page if
     * present, otherwise returns empty {@link List}
     */
    public List<String> getNonHTMLObjects() {
        return innerNonHTMLObjects;
    }

    /**
     * @return the {@code Modified-Time} of this html page
     */
    public Optional<LocalDateTime> getModifiedTime() {
        return ofNullable(modifiedTime);
    }

    /**
     * @return the {@code Status Code} of this html page when it is obtained in
     * the first place. That is, for example, if this html page is 30x, then we
     * keep the 30x page instead of the page which it redirects to
     */
    public Optional<StatusCode> getStatusCode() {
        return ofNullable(statusCode);
    }

    /**
     * @return this {@code Content-Length} of this html page
     */
    public Optional<Integer> getContentLength() {
        return of(contentLength);
    }

    /**
     * @return the {@code Location} (the URL it redirect to) of this html page
     */
    public Optional<SimpleURL> getRedirectTo() {
        return ofNullable(location);
    }

    /**
     * @return true if this URL has a valid web server, false else wise
     */
    public boolean isAlive() {
        return alive;
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

    /**
     * Ths hashCode is just based on its URL. Though it might be naive, it
     * should be enough to handle the situation in the assessment server of the
     * assignment.
     *
     * @return a hashCode of this html page
     */
    @Override
    public int hashCode() {
        return this.url.toString().hashCode();
    }

}
