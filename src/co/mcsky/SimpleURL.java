package co.mcsky;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

/**
 * Provides convenient methods for extracting relevant information from an URL.
 */
class SimpleURL {

    private final String rawURL;
    private final String host;
    private final int port;
    private final String protocol;
    private final String absPath;
    private final String query;
    private final String fragment;

    SimpleURL(String rawURL) {
        this.rawURL = rawURL;

        // Debugger for this regex: https://regex101.com/r/Zx74z0/11
        String regex = "^(?:([^:/?#]+):)?(?://([^/?:#]*)(?::(\\d*))?)?([^?#]+)?(?:\\?([^#]*))?(?:#(.+))?";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(this.rawURL);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Cannot recognize URL: " + rawURL);
        }
        // 1st capturing group is the protocol
        this.protocol = Optional.ofNullable(matcher.group(1)).orElseThrow(() -> new NoSuchElementException("Protocol cannot be empty"));

        // 2nd capturing group is the host
        this.host = Optional.ofNullable(matcher.group(2)).orElseThrow(() -> new NoSuchElementException("Host cannot be empty."));

        // 3rd capturing group is the port
        this.port = parseInt(Optional.ofNullable(matcher.group(3)).orElse("80"));

        // 4th capturing group is the path
        this.absPath = Optional.ofNullable(matcher.group(4)).orElse("/");

        // 5th capturing group is the query
        this.query = Optional.ofNullable(matcher.group(5)).orElse("");

        // 6th capturing group is the fragment
        this.fragment = Optional.ofNullable(matcher.group(6)).orElse("");
    }

    /**
     * @return the full URL
     */
    public String getRawURL() {
        return rawURL;
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
        return this.getRawURL().hashCode();
    }

}
