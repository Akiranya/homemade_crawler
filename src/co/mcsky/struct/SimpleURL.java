package co.mcsky.struct;

import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static java.util.Optional.ofNullable;

/**
 * Represents a {@code URL} of http scheme.
 */
public class SimpleURL {

    private final String URL;
    private final String host;
    private final int port;
    private final String protocol;
    private final String path;
    private final String directory;
    private final String file;
    private final String query;
    private final String fragment;

    /**
     * Creates a http URL from string representation.
     */
    public SimpleURL(String spec) {
        // Debugger for this regex: https://regex101.com/r/Zx74z0/16
        var regex = "^(?:(http):)(?://([^/?:#]+)(?::(\\d+))?)([^?#]+)?(?:\\?([^#]*))?(?:#(.+))?";
        var pattern = Pattern.compile(regex);
        var matcher = pattern.matcher(spec);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Cannot recognize http URL: " + spec);
        }
        this.protocol = ofNullable(matcher.group(1)).orElseThrow(() -> new NoSuchElementException("Only accepts http scheme"));
        this.host = ofNullable(matcher.group(2)).orElseThrow(() -> new NoSuchElementException("Host cannot be empty."));
        this.port = parseInt(ofNullable(matcher.group(3)).orElse("80"));
        this.path = ofNullable(matcher.group(4)).orElse("/");
        this.query = ofNullable(matcher.group(5)).orElse("");
        this.fragment = ofNullable(matcher.group(6)).orElse("");
        matcher = Pattern.compile("(.*/)(.+)?").matcher(this.path);
        this.directory = matcher.find() ? matcher.group(1) : "/";
        this.file = ofNullable(matcher.group(2)).orElse("");
        /*
         * Reconstruct this URL into very standard form.
         * This should ensure that "distinct" URLs are really distinct.
         * Say, "http://eee.com" without slash (i.e. absolute path) at the end
         * is effectively identical to "http://eee.com/" with slash at the end.
         * */
        this.URL = protocol + "://" + host + ":" + port + path +
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
    public String getPath() {
        return path;
    }

    /**
     * @return the directory ending with slash {@code /} of this URL
     */
    public String getDirectory() {
        return directory;
    }

    /**
     * @return the file name of this URL, or empty string if one does not exist
     */
    public String getFile() {
        return file;
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
