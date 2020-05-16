package co.mcsky.util;

import co.mcsky.struct.SimpleHttpResponse;
import co.mcsky.struct.SimpleURL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.Set;

/**
 * A simple http crawler using just low-level sockets.
 */
public class SimpleCrawler {

    private static final String CONTENT_TYPE_IMAGE = "Content-Type: image";
    private final RateLimiter throttler;
    private final Set<String> whitelist;

    /**
     * Initializes a crawler.
     *
     * @param interval  runs crawling at 1 request per {@code interval} rate
     * @param whitelist what websites should the crawler crawls for
     */
    public SimpleCrawler(long interval, Set<String> whitelist) {
        this.throttler = new RateLimiter(interval);
        this.whitelist = whitelist;
    }

    /**
     * Sends a http GET request to given URL.
     *
     * @param url standard URL
     *
     * @return a {@link SimpleHttpResponse} object obtained from the {@code URL}
     */
    public SimpleHttpResponse request(SimpleURL url) {
        // Verbose
        System.out.println("Crawler - Sec: " + LocalDateTime.now().getSecond());
        System.out.println("Crawler - URL: " + url.toString());

        var httpResponse = new SimpleHttpResponse(url, null, false);
        var httpRequest = String.format("GET %s HTTP/1.0\r\n\r\n", url.getPath());

        var host = url.getHost();
        var port = url.getPort();

        try (var socket = new Socket(host, port);
             var out = new PrintWriter(socket.getOutputStream(), true);
             var in = new Scanner(new BufferedReader(new InputStreamReader(socket.getInputStream())))
        ) {
            // Check whitelist. If the site is not in whitelist, then don't crawl and skip it
            if (!whitelist.contains(url.getHostPort())) {
                System.out.println("Crawler - " + url.getHostPort() + " not in whitelist, skipped and returning empty response");
                return new SimpleHttpResponse(url, null, true);
            }

            // Rate limiting
            throttler.await();

            // Send GET request to the http server
            out.println(httpRequest);

            var sb = new StringBuilder();
            var line = "";
            while (in.hasNextLine()) { // Read off response from the server
                line = in.nextLine();
                sb.append(line)
                  .append(System.getProperty("line.separator"));
                if (line.startsWith(CONTENT_TYPE_IMAGE)) {
                    in.close(); // Don't download the whole image files as we don't need... just get the headers
                    System.out.println("Crawler - closed image download stream early for " + url);
                    break;
                }
            }

            // Store the response message
            httpResponse = new SimpleHttpResponse(url, sb.toString(), true);
        } catch (UnknownHostException e) {
            System.err.println("Crawler - Unknown host " + host + ", returning empty response");
        } catch (IOException e) {
            System.err.println("Crawler - Couldn't get I/O for the connection to " + host + ":" + port + ", returning empty response");
        }

        // Verbose
        httpResponse.getHead().getStatusCode().ifPresent(c -> System.out.println("Crawler - Status code: " + c.toString()));
        httpResponse.getHead().getContentType().ifPresent(t -> System.out.println("Crawler - Content type: " + t.toString()));
        httpResponse.getHead().getModifiedTime().ifPresent(t -> System.out.println("Crawler - Modified time: " + t.toString()));
        httpResponse.getHead().getRedirectTo().ifPresent(l -> System.out.println("Crawler - Location: " + l.toString()));

        return httpResponse;
    }

}
