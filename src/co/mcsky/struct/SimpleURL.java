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

    public SimpleURL(String rawURL) {
        // Debugger for this regex: https://regex101.com/r/Zx74z0/11
        var regex = "^(?:([^:/?#]+):)?(?://([^/?:#]*)(?::(\\d*))?)?([^?#]+)?(?:\\?([^#]*))?(?:#(.+))?";
        var pattern = Pattern.compile(regex);
        var matcher = pattern.matcher(rawURL);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Cannot recognize URL: " + rawURL);
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

        // Reconstruct the URL into very standard form,
        // this should ensure that distinct URLs are really distinct
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
     * @return the port of the URL if presenting in the URL, otherwise just
     * returns port {@code 80} which is the default port of HTTP
     */
    public int getPort() {
        return port;
    }

    /**
     * @return the string representation of host and port together, like
     * "example.com:123" where host is "example.com" and port is "123"
     */
    public String getHostPort() {
        return host + ":" + port;
    }

    /**
     * @return the protocol of the URL
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * @return the absolute path of the URL if presenting in the URL, otherwise
     * just returns a {@code /} by default
     */
    public String getAbsPath() {
        return absPath;
    }

    /**
     * @return the query of the URL if presenting in the URL, otherwise returns
     * an empty string {@code ""}
     */
    public String getQuery() {
        return query;
    }

    /**
     * @return the fragment of the URL if presenting in the URL, otherwise
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

    // As we use java.utl.Set store distinct html pages,
    // it is necessary to override the hashCode() method.
    // URL should be enough to tell distinct html pages.
    @Override
    public int hashCode() {
        return this.URL.hashCode();
    }

}
