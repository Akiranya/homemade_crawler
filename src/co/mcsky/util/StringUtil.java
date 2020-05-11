package co.mcsky.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This class simply extracts strings with certain pattern from a HTML page.
 * Methods of this class either return {@link String}, {@link List<String>}, or
 * just {@link null} if necessary. Further processing of the extracted strings
 * should be done in other classes to keep code organized.
 */
public class StringUtil {

    /**
     * @param response the whole http response string from a server
     *
     * @return a {@link List} of all the URLs in this html page if there is any,
     * otherwise returns an empty {@link List}
     */
    public static List<String> extractURL(String response) {
        var pattern = Pattern.compile("<a href=\"(.*?)\">.*?</a>", Pattern.MULTILINE);
        var matcher = pattern.matcher(response);
        List<String> urls = new ArrayList<>();
        matcher.results().forEach(r -> urls.add(r.group(1)));
        return urls;
    }

    /**
     * @param response the whole http response string from a server
     *
     * @return the {@code Modified-Time} of this html page if present, otherwise
     * returns {@code null}
     */
    public static String extractModifiedTime(String response) {
        var pattern = Pattern.compile("Last-Modified: (.+)");
        var matcher = pattern.matcher(response);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * @param response the whole http response string from a server
     *
     * @return the status code of this html page if present, otherwise returns
     * {@code null}
     */
    public static String extractStatusCode(String response) {
        var pattern = Pattern.compile("HTTP/\\d\\.\\d (\\d{3}) ");
        var matcher = pattern.matcher(response);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * @param response the whole http response string from a server
     *
     * @return the {@code Content-Length} of this html page if present,
     * otherwise returns {@code null}
     */
    public static String extractContentLength(String response) {
        var pattern = Pattern.compile("Content-Length: (\\d+)");
        var matcher = pattern.matcher(response);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * @param response the whole http response string from a server
     *
     * @return a {@link List} of the non-html objects in this html page if
     * present, otherwise returns an empty {@link List}
     */
    public static List<String> extractNonHTMLObjects(String response) {
        // For now it just finds all <img> tags on a html page
        var pattern = Pattern.compile("<img src=\"(.*)\" .*?>", Pattern.MULTILINE);
        var matcher = pattern.matcher(response);
        List<String> images = new ArrayList<>();
        matcher.results().forEach(img -> images.add(img.group(1)));
        return images;
    }

    /**
     * @param response the whole http response string from a server
     *
     * @return the {@code Location} of this html page if present, otherwise
     * returns {@code null}
     */
    public static String extractLocation(String response) {
        var pattern = Pattern.compile("Location: (.+)");
        var matcher = pattern.matcher(response);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

}
