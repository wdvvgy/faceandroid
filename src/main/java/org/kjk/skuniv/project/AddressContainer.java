package org.kjk.skuniv.project;

/**
 * Created by SIRIUS on 2016-09-24.
 */

public class AddressContainer {

    private String ip;
    public void setIp(String ip){ this.ip = ip; }
    public String getIp(){ return ip; }

    private int port;
    public void setPort(int port){ this.port = port; }
    public int getPort(){ return port; }

    private static AddressContainer instance = new AddressContainer();
    public static AddressContainer getInstance(){ return instance; }

}
