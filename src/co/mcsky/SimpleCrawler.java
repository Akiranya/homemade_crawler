package co.mcsky;

import co.mcsky.util.Throttler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.Scanner;

public class SimpleCrawler {

    private final Throttler throttler;

    public SimpleCrawler(long interval) {
        throttler = new Throttler(interval);
    }

    /**
     * @param url standard URL
     *
     * @return a html page object ({@link HTMLWrapper}) obtained from the {@code
     * url}
     */
    public HTMLWrapper request(SimpleURL url) {
        throttler.limit();

        /*
            1. Open a socket.
            2. Open an input stream and output stream to the socket.
            3. Read from and write to the stream according to the server's protocol.
            4. Close the streams.
            5. Close the socket.
        */

        String request = String.format("GET %s HTTP/1.0\r\n\r\n", url.getAbsPath());
        HTMLWrapper wrapper = null;

        String host = url.getHost();
        int port = url.getPort();
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner in = new Scanner(new BufferedReader(new InputStreamReader(socket.getInputStream())))
        ) {
            out.println(request); // 向服务器发送HTTP请求字节

            StringBuilder httpContentBuilder = new StringBuilder();
            while (in.hasNextLine()) { // 读取从服务器发回来的字节
                httpContentBuilder.append(in.nextLine())
                                  .append(System.getProperty("line.separator"));
            }

            // 把服务器发回来的字节存进 wrapper
            wrapper = new HTMLWrapper(url, httpContentBuilder.toString());
        } catch (UnknownHostException e) {
            System.err.println("Unknown host " + host + ". Returns empty rawContent instead.");
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + host + ":" + port + ". Returns empty rawContent instead.");
        }

        System.out.println("Crawler - Request: \"" + request.replaceAll("([\r\n]*)", "") + "\"");
        System.out.println("Crawler - URL: " + url.getRawURL());
        System.out.println("Crawler - Status: " + Optional.ofNullable(wrapper).orElse(new HTMLWrapper(url, "")).getStatusCode());
        System.out.println("Crawler - Modified time: " + Optional.ofNullable(wrapper).orElse(new HTMLWrapper(url, "")).getModifiedTime());
        System.out.println();
        return Optional.ofNullable(wrapper).orElse(new HTMLWrapper(url, ""));
    }

}
