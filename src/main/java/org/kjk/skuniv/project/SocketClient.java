package org.kjk.skuniv.project;

/**
 * Created by SIRIUS on 2016-07-14.
 */

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketClient extends Thread{

    static private final String TAG = "SocketClient";
    private boolean isConnected;
    static final private int bytelen = 100;
    static public int getBytelen(){ return bytelen; }

    static private volatile SocketClient instance = null;
    private SocketClient(){ }
    static public SocketClient getInstance(){
        if(instance == null){
            synchronized (SocketClient.class){
                if(instance == null){
                    instance = new SocketClient();
                }
            }
        }
        return instance;
    }
    static public void getNewInstance(){
        instance = new SocketClient();
    }
    @Override
    public void run() {
        super.run();
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(dstAddress, dstPort), 2000);
            in = socket.getInputStream();
            out = socket.getOutputStream();
            isConnected = true;
        } catch (Exception e) {
            e.printStackTrace();
            isConnected = false;
        }
        if (isConnected) {
            main.NextActivity();
        } else {
            main.ServerConnectError();
        }

    }


//    static public SocketClient getNewInstance(){
//        instance = new SocketClient();
//        return instance;
//    }

    static final private int SERVERCONNECT = 0;

    private String dstAddress;
    private int dstPort;
    private static Socket socket = null;

    private MainActivity main;
    private InputStream in;
    private OutputStream out;

    public void setMainActivity(MainActivity main){ this.main = main; };

    public void setAddress(String addr, int port) {
        dstAddress = addr;
        dstPort = port;
    }

    public InputStream getInputStream(){
        synchronized (in){
            return in;
        }
    }

    public OutputStream getOutputStream(){
        synchronized (out){
            return out;
        }
    }

    public void disConnect() {
        Log.d(TAG, "disConnect called");
        if (socket != null && in != null && out != null) {
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}