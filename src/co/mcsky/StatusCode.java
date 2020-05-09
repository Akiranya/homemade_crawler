package co.mcsky;

/**
 * Contains status code and reason phrases. See <a href="https://www.rfc-editor.org/rfc/rfc1945.html#section-6.1.1">rfc1945
 * section 6.1.1</a>.
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

    final int code;

    StatusCode(int code) {
        this.code = code;
    }

    public static StatusCode matchCode(int code) {

        switch (code) {
            case 200:
                return OK;
            case 201:
                return CREATED;
            case 202:
                return ACCEPTED;
            case 204:
                return NO_CONTENT;
            case 301:
                return MOVED_PERMANENTLY;
            case 302:
                return MOVED_TEMPORARILY;
            case 304:
                return NOT_MODIFIED;
            case 400:
                return BAD_REQUEST;
            case 401:
                return UNAUTHORIZED;
            case 403:
                return FORBIDDEN;
            case 404:
                return NOT_FOUND;
            case 500:
                return INTERNAL_SERVER_ERROR;
            case 501:
                return NOT_IMPLEMENTED;
            case 502:
                return BAD_GATEWAY;
            case 503:
                return SERVICE_UNAVAILABLE;
            default:
                return UNKNOWN;
        }
    }

    // TODO What status codes should be classified as valid URLs?
    public boolean isValid() {
        switch (this) {
            case UNKNOWN:
            case NOT_FOUND:
            case FORBIDDEN:
            case NO_CONTENT:
            case BAD_GATEWAY:
            case BAD_REQUEST:
            case UNAUTHORIZED:
            case NOT_IMPLEMENTED:
            case NOT_MODIFIED:
            case INTERNAL_SERVER_ERROR:
            case SERVICE_UNAVAILABLE:
                return false;
        }
        return true;
    }

    public boolean isRedirected() {
        switch (this) {
            case MOVED_PERMANENTLY:
            case MOVED_TEMPORARILY:
            case NOT_MODIFIED:
                return true;
        }
        return false;
    }
}
