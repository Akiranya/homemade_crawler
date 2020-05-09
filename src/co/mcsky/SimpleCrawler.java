package co.mcsky;

import co.mcsky.util.RateLimiter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.Scanner;

public class SimpleCrawler {

    private final RateLimiter throttler;

    public SimpleCrawler(long interval) {
        throttler = new RateLimiter(interval);
    }

    /**
     * @param url standard URL
     *
     * @return a html page object ({@link HTMLWrapper}) obtained from the {@code
     * url}
     */
    public HTMLWrapper request(SimpleURL url) {
        throttler.limit(); // Method call rate limiting
        String request = String.format("GET %s HTTP/1.0\r\n\r\n", url.getAbsPath());
        HTMLWrapper wrapper = null;
        String host = url.getHost();
        int port = url.getPort();
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner in = new Scanner(new BufferedReader(new InputStreamReader(socket.getInputStream())))
        ) {
            out.println(request); // Send http request to the server
            StringBuilder httpContentBuilder = new StringBuilder();
            while (in.hasNextLine()) { // 读取从服务器发回来的字节
                httpContentBuilder.append(in.nextLine())
                                  .append(System.getProperty("line.separator"));
            }
            // Store the message from the server
            wrapper = new HTMLWrapper(url, httpContentBuilder.toString());
        } catch (UnknownHostException e) {
            System.err.println("Unknown host " + host + ". Returns empty content instead.");
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + host + ":" + port + ". Returns empty content instead.");
        }

        System.out.println("Crawler - Request: \"" + request.replaceAll("([\r\n]*)", "") + "\"");
        System.out.println("Crawler - URL: " + url.getRawURL());
        System.out.println("Crawler - Status code: " + Optional.ofNullable(wrapper).orElse(new HTMLWrapper(url, "")).getStatusCode());
        System.out.println("Crawler - Modified time: " + Optional.ofNullable(wrapper).orElse(new HTMLWrapper(url, "")).getModifiedTime());
        System.out.println();
        return Optional.ofNullable(wrapper).orElse(new HTMLWrapper(url, ""));
    }

}
