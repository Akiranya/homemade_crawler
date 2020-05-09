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

public class SimpleCrawler {

    private final RateLimiter throttler;

    public SimpleCrawler(long interval) {
        throttler = new RateLimiter(interval);
    }

    /**
     * @param url standard URL
     *
     * @return a html page object ({@link SimpleHTML}) obtained from the {@code
     * url}
     */
    public SimpleHTML request(SimpleURL url) {
        throttler.limit(); // Method call rate limiting
        var request = String.format("GET %s HTTP/1.0\r\n\r\n", url.getAbsPath());
        var wrapper = new SimpleHTML(url, ""); // New wrapper with concrete URL but empty raw html content
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
            System.err.println("Unknown host " + host + ". Returns empty content instead.");
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + host + ":" + port + ". Returns empty content instead.");
        }

        System.out.println("Crawler - Request: \"" + request.replaceAll("([\r\n]*)", "") + "\"");
        System.out.println("Crawler - URL: " + url.getRawURL());
        System.out.println("Crawler - Status code: " + wrapper.getStatusCode());
        System.out.println("Crawler - Modified time: " + wrapper.getModifiedTime());
        System.out.println();
        return wrapper;
    }

}
