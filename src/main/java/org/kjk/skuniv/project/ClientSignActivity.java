package org.kjk.skuniv.project;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ClientSignActivity extends AppCompatActivity {

    static private final String TAG = "ClientSignActivity";

    private SignaturePad mSignaturePad;
    private Button saveButton;
    private Button clearButton;

    private SendSignImage ssi;

    private byte[] sendData;

    private String ID;
    private String workname;

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume called");
        super.onResume();

    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause called");
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate called");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.clientsignactivity);

        Intent intent = getIntent();
        ID = intent.getStringExtra("ID");
        workname = intent.getStringExtra("WORKNAME");

        saveButton = (Button) findViewById(R.id.save_button);
        clearButton = (Button) findViewById(R.id.clear_button);

        mSignaturePad = (SignaturePad) findViewById(R.id.signature_pad);
        mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onSigned() {
                saveButton.setEnabled(true);
                clearButton.setEnabled(true);
            }

            @Override
            public void onClear() {
                //Event triggered when the pad is cleared
                saveButton.setEnabled(false);
                clearButton.setEnabled(false);
            }

            @Override
            public void onStartSigning() {

            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSignaturePad.clear();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap signatureBitmap = mSignaturePad.getSignatureBitmap();
                getBytesFromBitmap(signatureBitmap);
                setSendSignImage();
                ssi.execute(ID, workname);
            }
        });
    }

    private void setSendSignImage(){ ssi = new SendSignImage(this,ID,workname); }


    public Bitmap getBitmap(){
        return mSignaturePad.getSignatureBitmap();
    }

    public void NextActivity() {
        Log.d(TAG, "NextActivity called");
        Intent intent = new Intent(this, PDFViewActivity.class);
        intent.putExtra("ID", ID);
        intent.putExtra("WORKNAME", workname);
        startActivity(intent);
    }

    public byte[] getBytesFromBitmap() {
        Log.d(TAG, "getBytesFromBitmap");

        return sendData;
    }

    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        Log.d(TAG, "getBytesFromBitmap called");

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);

        sendData = stream.toByteArray();

        Log.d(TAG, "sign image byte length : " + new Integer(getLengthByteArray()).toString());
        return sendData;
    }

    public int getLengthByteArray() {
        Log.d(TAG, "getLengthByteArray called");
        return sendData.length;
    }

    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e("SignaturePad", "Directory not created");
        }
        return file;
    }

    public void saveBitmapToJPG(Bitmap bitmap, File photo) throws IOException {
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        OutputStream stream = new FileOutputStream(photo);
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        stream.close();
    }

    public boolean addJpgSignatureToGallery(Bitmap signature) {
        boolean result = false;
        try {
            File photo = new File(getAlbumStorageDir("SignaturePad"), String.format("Signature_%d.jpg", System.currentTimeMillis()));
            saveBitmapToJPG(signature, photo);
            scanMediaFile(photo);
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void scanMediaFile(File photo) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(photo);
        mediaScanIntent.setData(contentUri);
        ClientSignActivity.this.sendBroadcast(mediaScanIntent);
    }

}