package org.kjk.skuniv.project;

import android.os.AsyncTask;
import android.util.Log;

import org.opencv.core.Mat;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by SIRIUS on 2016-07-28.
 */
public class SendFaceImage extends AsyncTask<Void, Void, Boolean> {

    private int id;
    static final private String TAG = "SendFaceImage";
    private Mat faceMat;
    private byte[] ByteArrayFaceMat;
    private InputStream in;
    private OutputStream out;
    private boolean isRunning;
    private boolean isPredicted;

    private CameraActivity ca;

    @Override
    protected void onPostExecute(Boolean value) {
        Log.d(TAG, "onPostExecute");
        super.onPostExecute(value);

        if(value){
            if(id != 1){
                ca.RunClientActivity();
            }
            else{
                ca.RunAdministratorActivity();
            }
        }
    }

    @Override
    protected void onPreExecute() {
        Log.d(TAG, "onPreExecute");
        super.onPreExecute();
    }

    public void setCameraActivity(CameraActivity ca){
        this.ca = ca;
    }
    @Override
    protected Boolean doInBackground(Void... params) {
        Log.d(TAG, "doInBackground");

        try{
            in = SocketClient.getInstance().getInputStream();
            out = SocketClient.getInstance().getOutputStream();
        }
        catch(Exception e){
            e.printStackTrace();
        }

        if (sendMessage(HSP.HSP_HSP_USEHSP, HSP.HSP_DEVICE_MOBILE, HSP.HSP_SERVICEID_ADMIN, HSP.HSP_ID_UNKNOWN, HSP.HSP_MESSAGE_PROGRAM_START))
            Log.d(TAG, "PROGRAM START");

        if (sendMessage(HSP.HSP_HSP_USEHSP, HSP.HSP_DEVICE_MOBILE, HSP.HSP_SERVICEID_ADMIN, HSP.HSP_ID_UNKNOWN, HSP.HSP_MESSAGE_FACEDETECTING_START))
            Log.d(TAG, "FACEDETECTING START");

        if (getMessage() == HSP.HSP_MESSAGE_OK)
            Log.d(TAG, "MESSAGE OK");

        if (sendMessage(HSP.HSP_HSP_USEHSP, HSP.HSP_DEVICE_MOBILE, HSP.HSP_SERVICEID_ADMIN, HSP.HSP_ID_UNKNOWN, HSP.HSP_MESSAGE_IMAGE_UPLOAD))
            Log.d(TAG, "IMAGE UPLOAD");

        if (getMessage() == HSP.HSP_MESSAGE_OK)
            Log.d(TAG, "OK");

        while( !isPredicted && isRunning) {
            try {
                if(faceMat != null) {
                    if (sendImage(ByteArrayFaceMat))
                        Log.d(TAG, "Face image sended");
                    if (getLabel())
                        return true;
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return false;
    }

    public void setisRunning(boolean value){ isRunning = value; }

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

    public void setFaceMat(Mat face) {
        faceMat = face;

        if(faceMat != null)
            ByteArrayFaceMat = new byte[(int) (faceMat.total() * faceMat.channels()) + 1];
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

    public boolean sendImage(byte[] datas) {
        if (out != null) {
            byte[] sendData = datas;
            faceMat.get(0, 0, sendData);
            try {
                out.write(sendData);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    public boolean getLabel() {
        if (in != null) {
            byte[] arr = new byte[SocketClient.getBytelen()];
            try {
                in.read(arr);

                Log.d("PREDICTED", new Integer(arr[3]).toString());

                if (arr[3] > 0 && arr[4] == HSP.HSP_MESSAGE_LOGIN_START) {
                    id = arr[3];
                    isPredicted = true;
                    ca.setID(id);
                    return true;
                }

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
}