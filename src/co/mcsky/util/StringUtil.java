package co.mcsky.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

/**
 * This class simply extracts strings with certain pattern from a http response.
 * Methods of this class should only either return {@code List<String>} (can be
 * size of 0) or {@code Optional<String>}. Further processing of the extracted
 * strings should be done in other classes to keep code organized.
 */
public class StringUtil {

    /**
     * @param response the whole http response string from a server
     *
     * @return a {@link List} of all the URLs in this html page if there is any,
     * otherwise returns an empty {@link List}
     */
    public static List<String> extractUrls(String response) {
        var pattern = Pattern.compile("<.*?(?:href|src)=\"(.*?)\">?.*?(?:</a>|>)", Pattern.MULTILINE);
        var matcher = pattern.matcher(response);
        List<String> urls = new ArrayList<>();
        matcher.results().forEach(r -> urls.add(r.group(1)));
        return urls;
    }

    /**
     * @param response the whole http response string from a server
     *
     * @return the {@code Modified-Time} of the resource of this http response
     * if present, otherwise returns {@code null}
     */
    public static Optional<String> extractModifiedTime(String response) {
        var pattern = Pattern.compile("Last-Modified: (.+)");
        var matcher = pattern.matcher(response);
        if (matcher.find()) {
            return ofNullable(matcher.group(1));
        }
        return empty();
    }

    /**
     * @param response the whole http response string from a server
     *
     * @return the status code of this http response if present, otherwise
     * returns {@code null}
     */
    public static Optional<String> extractStatusCode(String response) {
        var pattern = Pattern.compile("HTTP/\\d\\.\\d (\\d{3}) ");
        var matcher = pattern.matcher(response);
        if (matcher.find()) {
            return ofNullable(matcher.group(1));
        }
        return empty();
    }

    /**
     * @param response the whole http response string from a server
     *
     * @return the {@code Location} of this http response if present, otherwise
     * returns {@code null}
     */
    public static Optional<String> extractLocation(String response) {
        var pattern = Pattern.compile("Location: (.+)");
        var matcher = pattern.matcher(response);
        if (matcher.find()) {
            return ofNullable(matcher.group(1));
        }
        return empty();
    }

    /**
     * @param response the whole http response string from a server
     *
     * @return the {@code Content-Length} of this http response if present,
     * otherwise returns {@code null}
     */
    public static Optional<String> extractContentLength(String response) {
        var pattern = Pattern.compile("Content-Length: (\\d+)");
        var matcher = pattern.matcher(response);
        if (matcher.find()) {
            return ofNullable(matcher.group(1));
        }
        return empty();
    }

    /**
     * @param response the whole http response string from a server
     *
     * @return the {@code Content-Type} of this http response if present,
     * otherwise returns {@code null}
     */
    public static Optional<String> extractContentType(String response) {
        var pattern = Pattern.compile("Content-Type: (.+)");
        var matcher = pattern.matcher(response);
        if (matcher.find()) {
            return ofNullable(matcher.group(1));
        }
        return empty();
    }

}
