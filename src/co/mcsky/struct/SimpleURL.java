package co.mcsky.struct;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

/**
 * Represents an {@code URL}. See <a href="https://www.rfc-editor.org/rfc/rfc1945.html#section-3.2.2">RFC1945
 * 3.2.2</a>.
 */
public class SimpleURL {

    private final String URL;
    private final String host;
    private final int port;
    private final String protocol;
    private final String absPath;
    private final String query;
    private final String fragment;

    /**
     * Creates a URL from string representation. The URL has to follow the full
     * format specified in RFC 1945 3.2.2, otherwise unexpected results would
     * happen.
     */
    public SimpleURL(String spec) {
        // Debugger for this regex: https://regex101.com/r/Zx74z0/11
        var regex = "^(?:([^:/?#]+):)?(?://([^/?:#]*)(?::(\\d*))?)?([^?#]+)?(?:\\?([^#]*))?(?:#(.+))?";
        var pattern = Pattern.compile(regex);
        var matcher = pattern.matcher(spec);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Cannot recognize URL: " + spec);
        }

        /*
            We ensure that each member has some value assigned.
            Should save me from dealing with messy NPEs in other classes.
        */

        this.protocol = Optional.ofNullable(matcher.group(1)).orElseThrow(() -> new NoSuchElementException("Protocol cannot be empty"));
        this.host = Optional.ofNullable(matcher.group(2)).orElseThrow(() -> new NoSuchElementException("Host cannot be empty."));
        this.port = parseInt(Optional.ofNullable(matcher.group(3)).orElse("80"));
        this.absPath = Optional.ofNullable(matcher.group(4)).orElse("/");
        this.query = Optional.ofNullable(matcher.group(5)).orElse("");
        this.fragment = Optional.ofNullable(matcher.group(6)).orElse("");

        // Reconstruct this URL into very standard form.
        // This should ensure that "distinct" URLs are really distinct.
        // Say, "http://eee.com" without slash (i.e. absolute path) at the end
        // is effectively identical to "http://eee.com/" with slash at the end.
        this.URL = protocol + "://" + host + ":" + port + absPath +
                   (query.equals("") ? "" : "?=" + query) +
                   (fragment.equals("") ? "" : "#" + fragment);
    }

    /**
     * @return the host of the URL
     */
    public String getHost() {
        return host;
    }

    /**
     * @return the port of this URL if present in the URL, otherwise just
     * returns port {@code 80} which is the default port of HTTP
     */
    public int getPort() {
        return port;
    }

    /**
     * @return the string representation of this host and this port together,
     * like "example.com:123" where host is "example.com" and port is "123"
     */
    public String getHostPort() {
        return host + ":" + port;
    }

    /**
     * @return the protocol of this URL
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * @return the absolute path of this URL if present in this URL, otherwise
     * just returns a {@code /} by default
     */
    public String getAbsPath() {
        return absPath;
    }

    /**
     * @return the query of the URL if present in the URL, otherwise returns an
     * empty string {@code ""}
     */
    public String getQuery() {
        return query;
    }

    /**
     * @return the fragment of this URL if present in this URL, otherwise
     * returns an empty string {@code ""}
     */
    public String getFragment() {
        return fragment;
    }

    @Override
    public String toString() {
        return URL;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof SimpleURL)) {
            return false;
        }
        return obj.hashCode() == this.hashCode();
    }

    /**
     * The hashCode is based on all components of this URL.
     *
     * @return a hashCode of this URL
     */
    @Override
    public int hashCode() {
        return this.URL.hashCode();
    }

}
