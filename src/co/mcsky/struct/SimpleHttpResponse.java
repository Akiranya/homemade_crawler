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
public class SimpleHttpResponse {

    private static final String NULL_RESPONSE = "";
    private final SimpleURL url;
    private final boolean alive;
    private final int contentLength;
    private final ContentType contentType;
    private final StatusCode statusCode;
    private final LocalDateTime modifiedTime;
    private final List<SimpleURL> innerUrls;
    private final SimpleURL location;

    /**
     * Creates a HTML object. If the URL cannot establish a connection, then
     * {@code null} should pass into {@code response} and {@code false} should
     * pass into {@code alive}.
     *
     * @param url     standard URL
     * @param message string representation of the http response from the server
     *                if present, otherwise {@code null} must pass into the
     *                constructor to indicate that the web server where the URL
     *                resides is not available
     * @param alive   whether the web server whether the URL resides is alive or
     *                not
     */
    public SimpleHttpResponse(SimpleURL url, String message, boolean alive) {
        this.url = Objects.requireNonNull(url, "URL cannot be null");
        var response = Objects.requireNonNullElse(message, NULL_RESPONSE);
        this.contentLength = ofNullable(StringUtil.extractContentLength(response))
                .flatMap(s -> of(parseInt(s)))
                .orElse(-1);
        this.contentType = ofNullable(StringUtil.extractContentType(response))
                .flatMap(s -> {
                    if (s.contains("text")) {
                        return of(ContentType.TEXT);
                    } else if (s.contains("image")) {
                        return of(ContentType.IMAGE);
                    }
                    return Optional.empty();
                })
                .orElse(null);
        this.statusCode = ofNullable(StringUtil.extractStatusCode(response))
                .flatMap(s -> of(parseInt(s)))
                .flatMap(s -> of(StatusCode.matchCode(s)))
                .orElse(null);
        this.modifiedTime = ofNullable(StringUtil.extractModifiedTime(response))
                .flatMap(timeString -> of(LocalDateTime.parse(timeString, DateTimeFormatter.RFC_1123_DATE_TIME)))
                .orElse(null);
        this.location = ofNullable(StringUtil.extractLocation(response))
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
        this.innerUrls = StringUtil.extractUrls(response)
                                   .stream()
                                   .map(this::encodeURL) // Always store URLs in full format for the purpose of comparing!
                                   .collect(Collectors.toList());
        this.alive = alive;
    }

    /**
     * @return the URL pointing to this page
     */
    public SimpleURL getURL() {
        return url;
    }

    /**
     * @return a {@link List} of http URLs inside this html page if present,
     * otherwise returns empty {@link List}
     */
    public List<SimpleURL> getInnerUrls() {
        return innerUrls;
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
     * @return the {@code Content-Length} of this html page
     */
    public Optional<Integer> getContentLength() {
        return of(contentLength);
    }

    /**
     * @return the {@code Content-Type} of this html page
     */
    public ContentType getContentType() {
        return contentType;
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
        if (!(obj instanceof SimpleHttpResponse)) {
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

    /**
     * Encodes a URL into full format as much as possible.
     *
     * @return full format of this URL
     */
    private SimpleURL encodeURL(String spec) {
        if (spec.startsWith("http://") || spec.startsWith("https://")) {
            return new SimpleURL(spec);
        } else {
            var base = url.getProtocol() + "://" + url.getHost() + ":" + url.getPort();
            if (spec.startsWith("/")) {
                return new SimpleURL(base + spec);
            } else {
                return new SimpleURL(base + url.getDirectory() + spec);
            }
        }
    }

}
