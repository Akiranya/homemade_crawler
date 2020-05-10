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

public class SimpleCrawler {

    private final RateLimiter throttler;
    private Set<String> whitelist;

    public SimpleCrawler(long interval) {
        this.throttler = new RateLimiter(interval);
    }

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
        var wrapper = new SimpleHTML(url, null); // Initialize wrapper with concrete URL but null

        if (!whitelist.contains(url.getHostPort())) {
            System.err.println(url.getHostPort() + " not in whitelist, skipped and returning empty wrapper");
            System.err.println();
            return wrapper;
        }

        // Rate limiting should happen AFTER the whitelist checking to avoid unnecessary waiting
        throttler.limit(); // Method call rate limiting

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
                                  .append(System.getProperty("line.separator"));
            }
            // Store the message from the server
            wrapper = new SimpleHTML(url, httpContentBuilder.toString());
        } catch (UnknownHostException e) {
            System.err.println("Unknown host " + host + ". Returns empty wrapper");
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + host + ":" + port + ". Returns empty wrapper");
        }

        System.out.println("Crawler - Request: \"" + request.replaceAll("([\r\n]*)", "") + "\"");
        System.out.println("Crawler - URL: " + url.getUrl());
        System.out.println("Crawler - Status code: " + wrapper.getStatusCode());
        wrapper.getModifiedTime().ifPresent(time -> System.out.println("Crawler - Modified time: " + time));
        wrapper.getLocation().ifPresent(location -> System.out.println("Crawler - Location: " + location.getTo().getUrl()));
        System.out.println();
        return wrapper;
    }

}
