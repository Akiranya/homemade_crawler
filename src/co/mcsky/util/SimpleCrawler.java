package co.mcsky.util;

import co.mcsky.struct.SimpleHTML;
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
        var html = new SimpleHTML(url, null, false);

        var request = String.format("GET %s HTTP/1.0\r\n\r\n", url.getAbsPath());
        var host = url.getHost();
        var port = url.getPort();
        try (var socket = new Socket(host, port);
             var socketOut = new PrintWriter(socket.getOutputStream(), true);
             var socketIn = new Scanner(new BufferedReader(new InputStreamReader(socket.getInputStream())))
        ) {
            if (!whitelist.contains(url.getHostPort())) {
                err.println(url.getHostPort() + " not in whitelist, skipped and returning empty html");
                return new SimpleHTML(url, null, true);
            }

            throttler.await(); // Rate limiting should happen AFTER the whitelist checking to avoid unnecessary waiting

            socketOut.println(request); // Send http request to the server
            var httpContentBuilder = new StringBuilder();
            while (socketIn.hasNextLine()) { // Read off lines from the server
                httpContentBuilder.append(socketIn.nextLine())
                                  .append(getProperty("line.separator"));
            }
            // Store the response message from the server
            html = new SimpleHTML(url, httpContentBuilder.toString(), true);
        } catch (UnknownHostException e) {
            err.println("Unknown host " + host + ", returning empty html");
        } catch (IOException e) {
            err.println("Couldn't get I/O for the connection to " + host + ":" + port + ", returning empty html");
        }

        out.println("Crawler - Sec: " + LocalDateTime.now().getSecond());
        out.println("Crawler - URL: " + url.toString());
        html.getStatusCode().ifPresent(code -> out.println("Crawler - Status code: " + code));
        html.getModifiedTime().ifPresent(time -> out.println("Crawler - Modified time: " + time));
        html.getRedirectTo().ifPresent(location -> out.println("Crawler - Location: " + location.toString()));
        return html;
    }

}
