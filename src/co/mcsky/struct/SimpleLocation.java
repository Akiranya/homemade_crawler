package co.mcsky.struct;

/**
 * Represents a {@code Location}. See <a href="https://www.rfc-editor.org/rfc/rfc1945.html#section-10.11">RFC
 * 1945 10.11</a>. It is {@code final} so that there is no need to take care of
 * states.
 */
public final class SimpleLocation {

    private final SimpleURL from;
    private final SimpleURL to;

    public SimpleLocation(SimpleURL from, SimpleURL to) {
        this.from = from;

        /*
            The literal URL in the field of Location may not have the same port
            as its "parent" html page. Say, "http://comp3310.ddns.net:7880/A/1A.html" with port 7880
            is a valid html page with status code 301, and the value in the Location field
            is, however, "http://comp3310.ddns.net/B/29.html" which indicates port 80.

            In other words, non-standard ports are not explicitly referenced in http redirection.

            Nevertheless, in the example above, our browsers will correctly redirect
            to "http://comp3310.ddns.net:7880/B/29.html" instead of "http://comp3310.ddns.net/B/29.html".

            It might be OK to let the SimpleCrawler to handle such case,
            but to keep my code organized, SimpleCrawler should always seek exactly
            whatever URL (including port) I pass into it.

            So I'm going to handle such case here.

            See: https://wattlecourses.anu.edu.au/mod/forum/discuss.php?d=603754
        */
        if (from.getHost().equalsIgnoreCase(to.getHost())) {
            // Fix the "wrong" port by explicitly setting the port of redirection to its parent's.
            var trueURL = to.getProtocol() + "://" + to.getHost() + ":" + from.getPort() + to.getAbsPath();
            this.to = new SimpleURL(trueURL);
        } else {
            this.to = to;
        }
    }

    public SimpleURL getFrom() {
        return from;
    }

    public SimpleURL getTo() {
        return to;
    }

    /**
     * @return an URL is said to be on-site if its {@code host} and {@code port}
     * are identical to the URL of the html page it resides
     */
    public boolean isOnSite() {
        return from.getHost().equals(to.getHost()) && from.getPort() == to.getPort();
    }

}
