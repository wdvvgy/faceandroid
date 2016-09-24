package org.kjk.skuniv.project;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.ScrollBar;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.shockwave.pdfium.PdfDocument;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

public class PDFViewActivity extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener {

    private static final String TAG = PDFViewActivity.class.getSimpleName();

    private final static int REQUEST_CODE = 42;

    private PDFView pdfView;
    private ScrollBar scrollBar;

    private int maxPage = 0;
    private InputStream in;
    private OutputStream out;
    private String pdfPath;
    private String ID;
    private String workname;
    private int Done;
    private Uri uri;
    private Integer pageNumber = 1;
    private String pdfFileName;
    private File pdf = new File(Environment.getExternalStorageDirectory(), "work.pdf");

    private ProgressDialog pdLoading;
    private AlertDialog alertDialog;
    private class GetJSON extends AsyncTask<String, String, String> {
        //private ProgressDialog pdLoading = new ProgressDialog(PDFViewActivity.this);

        private final String JSON_ARRAY = "lists";
        private final String Path = "img";
        private JSONArray jsonArray = null;
        private final String Page = "page";

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "onPreExecute");
            super.onPreExecute();
//            pdLoading.setMessage("\tLoading...");
//            pdLoading.setCancelable(false);
//            pdLoading.show();
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d(TAG, "onPostExecute");
            super.onPostExecute(s);
            //pdLoading.dismiss();

            try {
                JSONObject jsonObject = new JSONObject(s);
                jsonArray = jsonObject.getJSONArray(JSON_ARRAY);
                JSONObject jsonObject2 = jsonArray.getJSONObject(0);
                pdfPath = jsonObject2.getString(Path);
                pageNumber = jsonObject2.getInt(Page);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            new sendRequestFile().execute();
        }

        protected String doInBackground(String... params) {
            Log.d(TAG, "doInBackground");
            String uri = params[0];
            Log.d("URL", uri);
            StringBuilder sb;
            BufferedReader bufferedReader = null;
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
                return null;
            }
            return sb.toString().trim();
        }
    }

    private class sendRequestFile extends AsyncTask<Void, Void, Boolean> {

        private final String TAG = sendRequestFile.class.getSimpleName();

        public boolean sendMessage(final int... args) {

            byte[] messages = new byte[100];
            int messages_index = 0;

            for (final int t : args)
                messages[messages_index++] = (byte) t;

            Log.d("File name length", new Integer(pdfPath.length()).toString());

            for(messages_index=30; messages_index<pdfPath.length()+30; messages_index++){
                messages[messages_index] = (byte) pdfPath.charAt(messages_index-30);
            }

            messages[messages_index++] = '\0';

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

        public boolean sendMessageOk(final int... args) {
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
        @Override
        protected void onPreExecute() {
            Log.d(TAG, "onPreExecute");
            super.onPreExecute();
            pdLoading = new ProgressDialog(PDFViewActivity.this);
            pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();
        }

        @Override
        protected void onPostExecute(Boolean value) {
            Log.d(TAG, "onPostExecute");
            super.onPostExecute(value);

            pdLoading.dismiss();
            //pickFile();
            pdfView.fromFile(pdf)
                    .defaultPage(pageNumber)
                    .onPageChange(PDFViewActivity.this)
                    .swipeVertical(true)
                    .showMinimap(false)
                    .enableAnnotationRendering(true)
                    .onLoad(PDFViewActivity.this)
                    .load();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.d(TAG, "doInBackground");
            try {
                in = SocketClient.getInstance().getInputStream();
                out = SocketClient.getInstance().getOutputStream();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (sendMessage(HSP.HSP_HSP_USEHSP, HSP.HSP_DEVICE_MOBILE, HSP.HSP_SERVICEID_SERVER, HSP.HSP_ID_UNKNOWN, HSP.HSP_MESSAGE_REQUEST_FILE))
                Log.d(TAG, "REQUEST FILE");

            if (getMessage() == HSP.HSP_MESSAGE_IMAGE_UPLOAD)
                Log.d(TAG, "MESSAGE OK (IMAGE UPLOAD)");

            if (sendMessageOk(HSP.HSP_HSP_USEHSP, HSP.HSP_DEVICE_MOBILE, HSP.HSP_SERVICEID_SERVER, HSP.HSP_ID_UNKNOWN, HSP.HSP_MESSAGE_OK))
                Log.d(TAG, "SEND OK");

            try{
                int size = 0;
                int start = 0;
                int Remainsize = PDFFile.length;


                if(pdf.exists())
                    pdf.delete();
                FileOutputStream fos = new FileOutputStream(pdf.getPath(), true);
                while(Remainsize > 0){
                    if(Remainsize >= 1024)
                        size = 1024;
                    else
                        size = Remainsize;

                    in.read(PDFFile, start, size);
                    Remainsize -= size;
                    start += size;

                    Thread.sleep(10);
                }
                fos.write(PDFFile);
                fos.close();
            }
            catch(Exception e){
                e.printStackTrace();
            }

            return true;
        }

        private byte[] PDFFile;
        private int[] pdfFileLen;
        private int pdfFileLen_index = 0;

        public int getMessage() {
            if (in != null) {
                byte[] arr = new byte[SocketClient.getBytelen()];
                pdfFileLen = new int[20];
                try {
                    in.read(arr);

                    for(int i=10; i<=29 && arr[i] != '\0'; i++){
                        pdfFileLen[pdfFileLen_index++] = arr[i]-48;
                    }
                    StringBuilder bd = new StringBuilder();
                    for(int i=0; i<pdfFileLen_index; i++){
                        bd.append(pdfFileLen[i]);
                    }
                    int len = Integer.parseInt(bd.toString());
                    PDFFile = new byte[len];
                    return arr[4];
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return -1;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        pdfView = (PDFView) findViewById(R.id.pdfView);
        scrollBar = (ScrollBar) findViewById(R.id.scrollBar);
        pdfView.setScrollBar(scrollBar);

        if(pdf.exists()){
            Log.d(TAG, "File is already exist.");
            pdfView.fromFile(pdf)
                    .defaultPage(pageNumber)
                    .onPageChange(PDFViewActivity.this)
                    .swipeVertical(true)
                    .showMinimap(false)
                    .enableAnnotationRendering(true)
                    .onLoad(PDFViewActivity.this)
                    .load();
            return;
        }


        Intent intent = getIntent();
        ID = intent.getStringExtra("ID");
        workname = intent.getStringExtra("WORKNAME");
        try {
            workname = URLEncoder.encode(workname, "utf-8");
        }
        catch(Exception e){
            e.printStackTrace();
        }
        String url = "http://" + AddressContainer.getInstance().getIp() + ":3000/imgpath/" + ID + "/" + workname;
        new GetJSON().execute(url);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfview);
        //displayFromAsset(SAMPLE_FILE);
        AlertDialog.Builder builder = new AlertDialog.Builder(PDFViewActivity.this);
        builder.setMessage("가장 마지막까지 본 Page를 보여줍니다.")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        alertDialog = builder.create();
        if(!alertDialog.isShowing())
            alertDialog.show();
    }

    void pickFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(intent, REQUEST_CODE);
    }

//    void afterViews() {
//        pdfView.setScrollBar(scrollBar);
//        if (uri != null) {
//            displayFromUri(uri);
//        } else {
//            displayFromAsset(SAMPLE_FILE);
//        }
//        setTitle(pdfFileName);
//    }

//    private void displayFromAsset(String assetFileName) {
//        pdfFileName = assetFileName;
//        pdfView.fromAsset(SAMPLE_FILE)
//                .defaultPage(pageNumber)
//                .onPageChange(this)
//                .swipeVertical(true)
//                .showMinimap(false)
//                .enableAnnotationRendering(true)
//                .onLoad(this)
//                .load();
//    }

    private void displayFromUri(Uri uri) {
        pdfFileName = getFileName(uri);

        pdfView.fromUri(uri)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .swipeVertical(true)
                .showMinimap(false)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .load();
    }

    public void onResult(int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            uri = intent.getData();
            displayFromUri(uri);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if(pdLoading != null && pdLoading.isShowing())
            pdLoading.dismiss();

        if(pdf.exists()) {
            pdf.delete();
            Log.d(TAG, "File was deleted.");
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        class UpdatePDFPage extends AsyncTask<Void, Void, Void> {
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
            }

            @Override
            protected Void doInBackground(Void... params) {
                StringBuilder sb;
                BufferedReader bufferedReader = null;
                String uri = "http://" + AddressContainer.getInstance().getIp() + ":3000/update/" + ID + "/" + workname + "/" + new Integer(maxPage).toString() + "/" + new Integer(Done).toString();
                Log.d("URL", uri);
                try {
                    URL url = new URL(uri);

                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    sb = new StringBuilder();
                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
                return null;
            }
        }
        new UpdatePDFPage().execute();
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        setTitle(String.format("%s %s / %s", pdfFileName, page, pageCount));

        Log.d("currentPage", new Integer(page).toString());
        Log.d("maxPage", new Integer(pageCount).toString());

        Done = 1;

        if (maxPage <= pageNumber)
            maxPage = pageNumber;

        if (maxPage == pageCount)
            Done = 2;
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    @Override
    public void loadComplete(int nbPages) {
        PdfDocument.Meta meta = pdfView.getDocumentMeta();
//        Log.e(TAG, "title = " + meta.getTitle());
//        Log.e(TAG, "author = " + meta.getAuthor());
//        Log.e(TAG, "subject = " + meta.getSubject());
//        Log.e(TAG, "keywords = " + meta.getKeywords());
//        Log.e(TAG, "creator = " + meta.getCreator());
//        Log.e(TAG, "producer = " + meta.getProducer());
//        Log.e(TAG, "creationDate = " + meta.getCreationDate());
//        Log.e(TAG, "modDate = " + meta.getModDate());

        printBookmarksTree(pdfView.getTableOfContents(), "-");

    }

    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {

            Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }

}