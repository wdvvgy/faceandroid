package org.kjk.skuniv.project;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import lib.PortraitCameraBridgeViewBase;

public class CameraActivity extends AppCompatActivity implements PortraitCameraBridgeViewBase.CvCameraViewListener2 {

    private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            String TAG = new StringBuilder(_TAG).append("onManagerConnected").toString();

            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.d(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("detection_based_tracker");

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(org.kjk.skuniv.project.R.raw.haarcascade_frontalface_alt2);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_alt2.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.d(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int deviceWidth = displayMetrics.widthPixels;
                    int deviceHeight = displayMetrics.heightPixels;

                    mOpenCvCameraView.setMaxFrameSize(deviceWidth, deviceHeight);
                    mOpenCvCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
            }
        }
    };

    private static final String TAG = "CameraActivity";
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);

    private Mat mRgba;
    private Mat mGray;
    private Mat face_Mat;
    private Rect face_Rect;

    private boolean isdetected = false;
    private File mCascadeFile;
    private CascadeClassifier mJavaDetector;

    private final String _TAG = "ProcessedCameraActivity:";
    private float mRelativeFaceSize = 0.2f;
    private int mAbsoluteFaceSize = 0;
    private PortraitCameraBridgeViewBase mOpenCvCameraView;

    private SendFaceImage sfi;
    private AlertDialog alertDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.face_detect_surface_view);

        mOpenCvCameraView = (PortraitCameraBridgeViewBase) findViewById(R.id.cameraView);
        mOpenCvCameraView.setCvCameraViewListener(this);

        //loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(CameraActivity.this);
                builder.setMessage("얼굴 검출을 시도합니다. 카메라 정면을 바라보세요.")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String TAG = new StringBuilder(_TAG).append("onResume").toString();
                                if (!OpenCVLoader.initDebug()) {
                                    Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initiation");
                                    OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, CameraActivity.this, loaderCallback);
                                } else {
                                    Log.d(TAG, "OpenCV library found inside package. Using it");
                                    loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
                                }
                                if(sfi == null){
                                    setSendFaceImage();
                                    sfi.setisRunning(true);
                                    sfi.execute();
                                }
                                dialog.dismiss();
                            }
                        });

                alertDialog = builder.create();
                if(!alertDialog.isShowing())
                    alertDialog.show();
            }
        });
    }

    private int id;
    public void setID(int value){ id = value; }


    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }

    private void setSendFaceImage() {
        sfi = new SendFaceImage();
        sfi.setCameraActivity(this);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause called");
        super.onPause();
        String TAG = new StringBuilder(_TAG).append("onPause").toString();
        Log.d(TAG, "Disabling a camera view");

        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }

        sfi.cancel(true);
        sfi.setisRunning(false);

    }

    @Override
    protected void onDestroy() {
        String TAG = new StringBuilder(_TAG).append("onDestroy").toString();
        Log.d(TAG, "Disabling a camera view");
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
        super.onDestroy();
        //SocketClient.getInstance().disConnect();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        setMinFaceSize(0.2f);
        Log.d(TAG, "OpenCV CameraView Started");
    }

    @Override
    public void onCameraViewStopped() {
        Log.d(TAG, "OpenCV CameraView Stopped");
    }

    @Override
    public Mat onCameraFrame(PortraitCameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        MatOfRect faces = new MatOfRect();
        Core.flip(mRgba.t(), mRgba, -1);
        Core.flip(mGray.t(), mGray, -1);
        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }
        mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());

        Rect[] facesArray = faces.toArray();
        //Imgproc.rectangle(mRgba, facesArray[0].tl(), facesArray[0].br(), FACE_RECT_COLOR, 1);

        if (facesArray.length > 0) {
            isdetected = true;
            face_Rect = facesArray[0];
        } else {
            isdetected = false;
        }
        Core.flip(mRgba, mRgba, 1);
        Core.transpose(mRgba, mRgba);

        try{
            Thread.sleep(50);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        setfaceMat();

        return mRgba;
    }

    private void setfaceMat() {
        if (isdetected) {
            face_Mat = new Mat(mGray.clone(), face_Rect);
            Imgproc.resize(face_Mat, face_Mat, new Size(92, 112));
            sfi.setFaceMat(face_Mat);
        }
        else {
            face_Mat = new Mat(new Size(100, 100), CvType.CV_8UC1, new Scalar(255, 255, 255));
            sfi.setFaceMat(null);
        }
    }

    public void RunClientActivity(){
        Log.d(TAG, "RunClientActivity called");
        Log.d("RunClientActivity", new Integer(id).toString());
        Intent intent = new Intent(this, ClientMainActivity.class);
        intent.putExtra("ID", id);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void RunAdministratorActivity(){
        Intent intent = new Intent(this, Administrator.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

}