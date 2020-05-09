package co.mcsky.util;

import co.mcsky.struct.StatusCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

/**
 * This class simply extracts specified strings from a HTML page. Methods of
 * this class either return {@link String}, {@link List<String>}, {@link int} or
 * just {@link null} if necessary. Further processing of the extracted strings
 * should be done in other classes.
 */
public class StringUtil {

    /**
     * @param HTMLRawContent the whole raw HTML page string
     *
     * @return Returns a {@link List} of all the URLs in this html page if there
     * is any, otherwise returns an empty {@link List}
     */
    public static List<String> extractURL(String HTMLRawContent) {
        var pattern = Pattern.compile("<a href=\"(.*?)\">.*</a>", Pattern.MULTILINE);
        var matcher = pattern.matcher(HTMLRawContent);
        List<String> urls = new ArrayList<>();
        matcher.results().forEach(r -> urls.add(r.group(1)));
        return urls;
    }

    /**
     * @param HTMLRawContent the whole raw HTML page string
     *
     * @return Returns the {@code Modified-Time} of this html page if
     * presenting, otherwise returns {@code null}
     */
    public static String extractModifiedTime(String HTMLRawContent) {
        var pattern = Pattern.compile("Last-Modified: (.+)");
        var matcher = pattern.matcher(HTMLRawContent);
        if (matcher.find()) {
            return matcher.toMatchResult().group(1);
        }
        return null;
    }

    /**
     * @param HTMLRawContent the whole raw HTML page string
     *
     * @return Returns the status code of the html page if presenting, otherwise
     * returns {@link StatusCode#UNKNOWN}
     */
    public static StatusCode extractStatusCode(String HTMLRawContent) {
        var pattern = Pattern.compile("HTTP/\\d\\.\\d (\\d{3}) ");
        var matcher = pattern.matcher(HTMLRawContent);
        if (matcher.find()) {
            return Optional.ofNullable(matcher.group(1))
                           .or(() -> Optional.of("0")) // If the matcher returns null, then parse 0 (UNKNOWN) instead
                           .flatMap(s -> Optional.of(parseInt(s)))
                           .flatMap(s -> Optional.of(StatusCode.matchCode(s)))
                           .get();
        }
        return StatusCode.UNKNOWN;
    }

    /**
     * @param HTMLRawContent the whole raw HTML page string
     *
     * @return Returns the {@code Content-Length} of the html page if
     * presenting, otherwise returns {@code -1}
     */
    public static int extractContentLength(String HTMLRawContent) {
        var pattern = Pattern.compile("Content-Length: (\\d+)");
        var matcher = pattern.matcher(HTMLRawContent);
        if (matcher.find()) {
            return parseInt(matcher.group(1));
        }
        return -1;
    }

    /**
     * @param HTMLRawContent the whole raw HTML page string
     *
     * @return Returns a {@link List} of the non-html objects in this html page
     * if presenting, otherwise returns an empty {@link List}
     */
    public static List<String> extractNonHTMLObjects(String HTMLRawContent) {
        // For now it just finds all <img> tags on a html page
        var pattern = Pattern.compile("<img src=\"(.*)\" .*>");
        var matcher = pattern.matcher(HTMLRawContent);
        List<String> images = new ArrayList<>();
        matcher.results().forEach(img -> images.add(img.group(1)));
        return images;
    }

    /**
     * @param HTMLRawContent the whole raw HTML page string
     *
     * @return Returns the {@code Location} of this html page if presenting,
     * otherwise returns {@code null}
     */
    public static String extractLocation(String HTMLRawContent) {
        var pattern = Pattern.compile("Location: (.+)");
        var matcher = pattern.matcher(HTMLRawContent);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

}
