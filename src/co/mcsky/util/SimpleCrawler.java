package co.mcsky.util;

import co.mcsky.struct.SimpleHTML;
import co.mcsky.struct.SimpleURL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Set;

import static java.lang.System.*;

public class SimpleCrawler {

    private final RateLimiter throttler;
    private final Set<String> whitelist;

    public SimpleCrawler(long interval, Set<String> whitelist) {
        this.throttler = new RateLimiter(interval);
        this.whitelist = whitelist;
    }

    /**
     * @param url standard URL
     *
     * @return a html page object ({@link SimpleHTML}) obtained from the {@code
     * url}
     */
    public SimpleHTML request(SimpleURL url) {
        var wrapper = new SimpleHTML(url, null); // Initialize wrapper with concrete URL but null response

        if (!whitelist.contains(url.getHostPort())) {
            out.println(url.getHostPort() + " not in whitelist, skipped and returning empty wrapper");
            out.println();
            return wrapper;
        }

        throttler.await(); // Rate limiting should happen AFTER the whitelist checking to avoid unnecessary waiting

        var request = String.format("GET %s HTTP/1.0\r\n\r\n", url.getAbsPath());
        var host = url.getHost();
        var port = url.getPort();
        try (var socket = new Socket(host, port);
             var out = new PrintWriter(socket.getOutputStream(), true);
             var in = new Scanner(new BufferedReader(new InputStreamReader(socket.getInputStream())))
        ) {
            out.println(request); // Send http request to the server
            var httpContentBuilder = new StringBuilder();
            while (in.hasNextLine()) { // Read off lines from the server
                httpContentBuilder.append(in.nextLine())
                                  .append(getProperty("line.separator"));
            }
            // Store the message from the server
            wrapper = new SimpleHTML(url, httpContentBuilder.toString());
        } catch (UnknownHostException e) {
            err.println("Unknown host " + host + ". Returns empty wrapper");
            // UnknownHost means that we actually haven't requested the server,
            // so we have to reset the rate limiting to avoid unnecessary waiting
            throttler.reset();
        } catch (IOException e) {
            err.println("Couldn't get I/O for the connection to " + host + ":" + port + ". Returns empty wrapper");
            // Same as above
            throttler.reset();
        }

        out.println("Crawler - URL: " + url.toString());
        wrapper.getStatusCode().ifPresent(code -> out.println("Crawler - Status code: " + code));
        wrapper.getModifiedTime().ifPresent(time -> out.println("Crawler - Modified time: " + time));
        wrapper.getRedirectTo().ifPresent(location -> out.println("Crawler - Location: " + location.toString()));
        out.println();
        return wrapper;
    }

}
