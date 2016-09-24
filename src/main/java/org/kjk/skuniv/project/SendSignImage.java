package org.kjk.skuniv.project;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by SIRIUS on 2016-07-28.
 */
public class SendSignImage extends AsyncTask<String, Void, Boolean> {

    private static final String TAG = "SendSignImage";

    public SendSignImage(ClientSignActivity csa, String id, String wn){
        this.csa = csa;
        this.ID = id;
        this.workname = wn;

        try {
            workname = URLEncoder.encode(workname, "utf-8");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private ClientSignActivity csa;

    private InputStream in;
    private OutputStream out;

    private byte[] sendData;
    private byte[] bytelen;

    private String ID;
    private String workname;

    @Override
    protected void onPreExecute() {
        Log.d(TAG, "onPreExecute");
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Boolean value) {
        Log.d(TAG, "onPostExecute");
        super.onPostExecute(value);

        if(value) {
            csa.NextActivity();
        }
    }

    @Override
    protected Boolean doInBackground(String... params) {
        Log.d(TAG, "doInBackground");

        try{
            in = SocketClient.getInstance().getInputStream();
            out = SocketClient.getInstance().getOutputStream();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        if(sendMessage(HSP.HSP_HSP_USEHSP, HSP.HSP_DEVICE_MOBILE, HSP.HSP_SERVICEID_USER, HSP.HSP_ID_SERVER, HSP.HSP_MESSAGE_SIGN_UPLOAD))
            Log.d("MESSAGE SEND", "SIGN UPLOAD");

        if(getMessage() == HSP.HSP_MESSAGE_OK)
            Log.d("MESSAGE RECEIVE", "OK");

        sendData = csa.getBytesFromBitmap();

        if (sendMessageLen(HSP.HSP_HSP_USEHSP, HSP.HSP_DEVICE_MOBILE, HSP.HSP_SERVICEID_USER, HSP.HSP_ID_SERVER, HSP.HSP_MESSAGE_IMAGE_UPLOAD))
            Log.d("MESSAGE SEND", "IMAGE UPLOAD");

        if(getMessage() == HSP.HSP_MESSAGE_OK)
            Log.d("MESSAGE RECEIVE", "OK");


        if (sendImage())
            Log.d("MESSAGE SEND", "SUCCESS SEND SIGN IMAGE");

        if(getMessage() == HSP.HSP_MESSAGE_OK) {
            Log.d("MESSAGE RECEIVE", "OK");

            StringBuilder sb;
            BufferedReader bufferedReader = null;
            String uri = "http://" + AddressContainer.getInstance().getIp() + ":3000/update/" + ID + "/" + workname + "/1";
            Log.d("URL", uri);
            try {
                URL url = new URL(uri);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                sb = new StringBuilder();
                bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String json;
                while((json = bufferedReader.readLine())!= null){
                    sb.append(json+"\n");
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            return true;
        }

        return false;
    }

    public boolean sendMessage(final int... args) {

        byte[] messages = new byte[SocketClient.getBytelen()];
        int messages_index = 0;

        for (final int t : args)
            messages[messages_index++] = (byte) t;

        if (out != null) {
            try {
                out.write(messages);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        return false;
    }

    public int getDataLength(){
        Log.d(TAG, "getDataLength");

        String slen = new Integer(sendData.length).toString();
        bytelen = new byte[slen.length()];

        for(int i=0; i<slen.length(); i++){
            bytelen[i] = (byte) (slen.charAt(i) - '0');
        }
        Log.d(TAG, new Integer(bytelen.length).toString());
        return bytelen.length;
    }

    public boolean sendMessageLen(final int... args){
        byte[] messages = new byte[SocketClient.getBytelen()];
        int messages_index = 0;

        for (final int t : args)
            messages[messages_index++] = (byte) t;

        for(messages_index=10; messages_index<getDataLength()+10; messages_index++){
            messages[messages_index] = (byte)(bytelen[messages_index-10]+48);
        }

        messages[messages_index++] = '\0';

        String fName = ID;
        Log.d("fname", fName);
        for(messages_index=30; messages_index<fName.length()+30; messages_index++){
            messages[messages_index] = (byte) fName.charAt(messages_index-30);
        }

        messages[messages_index] = '\0';

        if (out != null) {
            try {
                out.write(messages);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    public boolean sendImage() {
        if (out != null) {
            try {
                Log.d("sendImage", new Integer(sendData.length).toString());
                csa.addJpgSignatureToGallery(csa.getBitmap());
                out.write(sendData);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public int getMessage() {
        if (in != null) {
            byte[] arr = new byte[SocketClient.getBytelen()];
            try {
                in.read(arr);
                return arr[4];
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

}
