package co.mcsky;

import co.mcsky.util.StringUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>This is a wrapper class for {@link SimpleURL} which may or may not
 * give back a HTML page. This means that this class must contain a instance of
 * {@link SimpleURL}, and if the {@link SimpleURL#getRawURL()} is a valid URL
 * (say, we can obtain a HTML page from the URL, including 40x and 50x) then
 * this class must contain relevant information about the HTML page.</p>
 *
 * <p>This class provides convenient methods for getting relevant information
 * about its internal HTML page (say, {@code modifiedTime}). Typically, the
 * relevant information is for generating a good report for the assignment
 * ;)</p>
 */
public class HTMLWrapper {

    private final SimpleURL url; // This URL should be given by the crawler.
    private final List<SimpleURL> innerURL;

    private final String rawContent;
    private final int contentLength;
    private final HTTPStatusCode statusCode;
    private final LocalDateTime modifiedTime;
    private final List<String> innerNonHTMLObjects;
    private final SimpleURL location;

    public HTMLWrapper(SimpleURL url, String rawContent) {
        this.url = url;
        this.rawContent = rawContent; // Raw byte messages from sockets
        this.statusCode = StringUtil.extractStatusCode(this.rawContent);

        // We parse the modified time so that we can compare it easily for reporting
        this.modifiedTime = Optional.ofNullable(StringUtil.extractModifiedTime(this.rawContent))
                                    .map(s -> LocalDateTime.parse(s, DateTimeFormatter.RFC_1123_DATE_TIME))
                                    .orElse(null);

        this.contentLength = StringUtil.extractContentLength(this.rawContent); // This is the size of html page, I think?
        this.innerNonHTMLObjects = StringUtil.extractNonHTMLObjects(this.rawContent); // NonHTMLObjects only include images for now
        this.location = Optional.ofNullable(StringUtil.extractLocation(this.rawContent))
                                .map(SimpleURL::new)
                                .orElse(null);

        // Exceptional URL
        // http://comp3310.ddns.net/B/29.html
        // http://www.canberratimes.com.au/
        // http://comp3310.ddns.net:7880/C/307.html
        // http://www.canberratimes.com.au/
        // http://comp3310.ddns.net:7880/B/23.html (contains canberra times)

        // We always store the inner URLs in full URL format
        // innerURl 必须可以直接 pass 到 ICrawler.request(URL) 让其成功爬虫
        this.innerURL = StringUtil.extractURL(this.rawContent)
                                  .stream()
                                  .map(rawURL -> {
                                      if (rawURL.startsWith("http://") || rawURL.startsWith("https://")) {
                                          // Case where the URL is in full format
                                          // e.g.1 http://comp3310.ddns.net/B/29.html
                                          // e.g.2 http://comp3310.ddns.net:7880/C/307.html
                                          return new SimpleURL(rawURL);
                                      } else {
                                          // Case where the URL is not in full format
                                          // e.g. A/30.html, /B/29.html
                                          String baseURL = url.getProtocol() + "://" + url.getHost() + ":" + url.getPort();
                                          String fullURL;
                                          if (rawURL.startsWith("/")) {
                                              fullURL = baseURL + rawURL;
                                          } else {
                                              fullURL = baseURL + "/" + rawURL;
                                          }
                                          return new SimpleURL(fullURL);
                                      }
                                  })
                                  .collect(Collectors.toList());
    }

    /**
     * @return the whole raw HTML page string
     */
    public String getRawContent() {
        return rawContent;
    }

    /**
     * @return the URL of this page
     */
    public SimpleURL getURL() {
        return url;
    }

    /**
     * @return a {@link List} of URLs inside the html page
     */
    public List<SimpleURL> getInnerURL() {
        return innerURL;
    }

    /**
     * @return a {@link List} of non-html objects inside the html page
     */
    public List<String> getNonHTMLObjects() {
        return innerNonHTMLObjects;
    }

    /**
     * @return the {@code Modified-Time} of the html page
     */
    public LocalDateTime getModifiedTime() {
        return modifiedTime;
    }

    /**
     * @return the status code of the html page. If we cannot access to the html
     * page via the URL, then returns {@link HTTPStatusCode#UNKNOWN} otherwise
     */
    public HTTPStatusCode getStatusCode() {
        return statusCode;
    }

    /**
     * @return the {@code Content-Length} of the html page
     */
    public int getContentLength() {
        return contentLength;
    }

    /**
     * @return the {@code Location} of the html page returns {@code null}
     */
    public SimpleURL getLocation() {
        return location;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HTMLWrapper)) {
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
