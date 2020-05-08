package co.mcsky.util;

import co.mcsky.HTTPStatusCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.*;

/**
 * This class simply extracts specified strings from a HTML page. Methods of
 * this class either return {@link String} or {@link List<String>}.
 */
public class StringUtil {

    /**
     * @param HTMLRawContent the whole raw HTML page string
     *
     * @return Returns a {@link List} of all the URLs in this html page if there
     * is any, otherwise returns an empty {@link List}
     */
    public static List<String> extractURL(String HTMLRawContent) {
        Pattern pattern = Pattern.compile("<a href=\"(.*?)\">.*</a>", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(HTMLRawContent);
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
        Pattern pattern = Pattern.compile("Last-Modified: (.*)");
        Matcher matcher = pattern.matcher(HTMLRawContent);
        if (matcher.find()) {
            return matcher.toMatchResult().group(1);
        }
        return null;
    }

    /**
     * @param HTMLRawContent the whole raw HTML page string
     *
     * @return Returns the status code of the html page if presenting, otherwise
     * returns {@link HTTPStatusCode#UNKNOWN}
     */
    public static HTTPStatusCode extractStatusCode(String HTMLRawContent) {
        Pattern pattern = Pattern.compile("HTTP/\\d\\.\\d (\\d\\d\\d) ");
        Matcher matcher = pattern.matcher(HTMLRawContent);
        if (matcher.find()) {
            return Optional.ofNullable(matcher.group(1))
                           .or(() -> Optional.of("0")) // If the matcher returns null, then parse 0 (UNKNOWN) instead
                           .flatMap(s -> Optional.of(parseInt(s)))
                           .flatMap(s -> Optional.of(HTTPStatusCode.matchCode(s)))
                           .get();
        }
        return HTTPStatusCode.UNKNOWN;
    }

    /**
     * @param HTMLRawContent the whole raw HTML page string
     *
     * @return Returns the {@code Content-Length} of the html page if
     * presenting, otherwise returns {@code -1}
     */
    public static int extractContentLength(String HTMLRawContent) {
        Pattern pattern = Pattern.compile("Content-Length: (\\d*)");
        Matcher matcher = pattern.matcher(HTMLRawContent);
        if (matcher.find()) {
            return parseInt(matcher.group(1));
        }
        return -1;
    }

    /**
     * @param HTMLRawContent the whole raw HTML page string
     *
     * @return Returns a {@link List} of the non-html objects in this
     * html page if presenting, otherwise returns an empty {@link List}
     */
    public static List<String> extractNonHTMLObjects(String HTMLRawContent) {
        // For now it just finds all <img> tags on a html page
        Pattern pattern = Pattern.compile("<img src=\"(.*)\" .*>");
        Matcher matcher = pattern.matcher(HTMLRawContent);
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
        Pattern pattern = Pattern.compile("Location: (.+)");
        Matcher matcher = pattern.matcher(HTMLRawContent);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

}
