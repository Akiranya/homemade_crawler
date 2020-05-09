package co.mcsky.struct;

/**
 * Represents a {@code Location}. See <a href="https://www.rfc-editor.org/rfc/rfc1945.html#section-10.11">RFC
 * 1945 10.11</a>.
 */
public class SimpleLocation {

    private final SimpleURL from;
    private final SimpleURL to;
    private final boolean onSite;

    public SimpleLocation(SimpleURL from, SimpleURL to) {
        this.from = from;
        this.to = to;
        this.onSite = from.getHost().equals(to.getHost()) && from.getPort() == to.getPort();
    }

    public SimpleURL getFrom() {
        return from;
    }

    public SimpleURL getTo() {
        return to;
    }

    public boolean isOnSite() {
        return onSite;
    }

}
