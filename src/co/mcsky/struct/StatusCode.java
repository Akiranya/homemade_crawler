package co.mcsky.struct;

/**
 * Represents {@code Status Code} and {@code Reason Phrases}. See <a
 * href="https://www.rfc-editor.org/rfc/rfc1945.html#section-6.1.1">RFC 1945
 * 6.1.1</a>.
 */
public enum StatusCode {
    OK(200),
    CREATED(201),
    ACCEPTED(202),
    NO_CONTENT(204),
    MOVED_PERMANENTLY(301),
    MOVED_TEMPORARILY(302),
    NOT_MODIFIED(304),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    INTERNAL_SERVER_ERROR(500),
    NOT_IMPLEMENTED(501),
    BAD_GATEWAY(502),
    SERVICE_UNAVAILABLE(503),
    UNKNOWN(0);

    public final int code;

    StatusCode(int code) {
        this.code = code;
    }

    public static StatusCode matchCode(int code) {

        return switch (code) {
            case 200 -> OK;
            case 201 -> CREATED;
            case 202 -> ACCEPTED;
            case 204 -> NO_CONTENT;
            case 301 -> MOVED_PERMANENTLY;
            case 302 -> MOVED_TEMPORARILY;
            case 304 -> NOT_MODIFIED;
            case 400 -> BAD_REQUEST;
            case 401 -> UNAUTHORIZED;
            case 403 -> FORBIDDEN;
            case 404 -> NOT_FOUND;
            case 500 -> INTERNAL_SERVER_ERROR;
            case 501 -> NOT_IMPLEMENTED;
            case 502 -> BAD_GATEWAY;
            case 503 -> SERVICE_UNAVAILABLE;
            default -> UNKNOWN;
        };
    }

    // TODO To confirm: what status codes should be classified as valid URLs?
    public boolean isValid() {
        return switch (this) {
            case UNKNOWN, NOT_FOUND, FORBIDDEN, NO_CONTENT, BAD_GATEWAY,
                    BAD_REQUEST, UNAUTHORIZED, NOT_IMPLEMENTED, NOT_MODIFIED,
                    INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE -> false;
            default -> true;
        };
    }

    public boolean isRedirected() {
        return switch (this) {
            case MOVED_PERMANENTLY, MOVED_TEMPORARILY, NOT_MODIFIED -> true;
            default -> false;
        };
    }
}
