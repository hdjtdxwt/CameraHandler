package com.epsit.camerahandler;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.epsit.camerahandler.utils.AlertError;
import com.epsit.camerahandler.utils.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CameraActivity extends AppCompatActivity {
    String TAG = "CameraActivity";
    protected String mPhotoFilePath = null;
    String mCameraId;
    CameraDevice mCameraDevice;
    CaptureRequest.Builder mPreviewBuilder;
    CaptureRequest mPreviewRequest;
    SurfaceHolder mSurfaceHolder;
    SurfaceView mSurfaceView;
    CameraCaptureSession mCaptureSession;
    CameraManager mCameraManager;
    ImageReader mImageReader;
    int mState;
    public static final int STATE_PREVIEW = 0;
    public static final int STATE_WAITING_CAPTURE = 1;
    CameraCaptureSession mSession;

    Handler mHandler;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mSurfaceView = (SurfaceView) findViewById(R.id.camera_surfaceview);
        mSurfaceHolder = mSurfaceView.getHolder();
        requestAllPermissionsIfNeed();
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                initCameraAndPreview();
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            }
        });
    }

    ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader imageReader) {

        }
    };

    private void initCameraAndPreview() {
        Log.d("linc", "init camera and preview");
        HandlerThread handlerThread = new HandlerThread("Camera2");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
        try {
            mCameraId = "" + CameraCharacteristics.LENS_FACING_FRONT;
            mImageReader = ImageReader.newInstance(mSurfaceView.getWidth(), mSurfaceView.getHeight(), ImageFormat.JPEG,/*maxImages*/7);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mHandler);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mCameraManager.openCamera(mCameraId, DeviceStateCallback, mHandler);
        } catch (CameraAccessException e) {
            Log.e("linc", "open camera failed." + e.getMessage());
        }
    }
    private CameraDevice.StateCallback DeviceStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice camera) {
            Log.d("linc","DeviceStateCallback:camera was opend.");
           // mCameraOpenCloseLock.release();
            mCameraDevice = camera;
            try {
                createCameraCaptureSession();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {

        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {

        }
    };
    private void createCameraCaptureSession() throws CameraAccessException {
        Log.d("linc","createCameraCaptureSession");

        mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        mPreviewBuilder.addTarget(mSurfaceHolder.getSurface());
        mState = STATE_PREVIEW;
        mCameraDevice.createCaptureSession(
                Arrays.asList(mSurfaceHolder.getSurface(), mImageReader.getSurface()),
                mSessionPreviewStateCallback, mHandler);
    }
    private CameraCaptureSession.StateCallback mSessionPreviewStateCallback = new CameraCaptureSession.StateCallback() {

        @Override
        public void onConfigured(CameraCaptureSession session) {
            Log.d("linc","mSessionPreviewStateCallback onConfigured");
            mSession = session;
            try {
                mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                session.setRepeatingRequest(mPreviewBuilder.build(), mSessionCaptureCallback, mHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
                Log.e("linc","set preview builder failed."+e.getMessage());
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

        }
    };
    @TargetApi(Build.VERSION_CODES.M)
    protected void requestAllPermissionsIfNeed() {
        List<String> permissionList = new ArrayList<String>();
        // 申请相机权限
        // Camera permission
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                // 用户拒绝过权限申请，下一次再进入的时候给出的解释
                AlertError.showDialog(this, getResources().getString(R.string.error_title),
                        getResources().getString(R.string.no_camera_perm_hint));
            } else {
                permissionList.add(Manifest.permission.CAMERA);
            }
        }
        // 我们需要从应用外的目录获取照片，所以需要申请读取外部存储权限
        // read external storage permission, for we need to read the photos
        // outside application-specific directories
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                AlertError.showDialog(this, getResources().getString(R.string.error_title),
                        getResources().getString(R.string.no_file_perm_hint));
            } else {
                permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
        if(permissionList.size()>0){
            requestPermissions( permissionList.toArray(new String[permissionList.size()]), 0);
        }
    }
    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback =
            new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request,
                                               TotalCaptureResult result) {
                    //            Log.d("linc","mSessionCaptureCallback, onCaptureCompleted");
                    mSession = session;
                    checkState(result);
                }

                @Override
                public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request,
                                                CaptureResult partialResult) {
                    Log.d("linc","mSessionCaptureCallback,  onCaptureProgressed");
                    mSession = session;
                    checkState(partialResult);
                }

                private void checkState(CaptureResult result) {
                    switch (mState) {
                        case STATE_PREVIEW:
                            // NOTHING
                            break;
                        case STATE_WAITING_CAPTURE:
                            int afState = result.get(CaptureResult.CONTROL_AF_STATE);

                            if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                                    CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState
                                    ||  CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED == afState
                                    || CaptureResult.CONTROL_AF_STATE_PASSIVE_UNFOCUSED == afState) {
                                //do something like save picture
                            }
                            break;
                    }
                }

            };
    public void onCapture(View view) {
        try {
            Log.i("linc", "take picture");
            mState = STATE_WAITING_CAPTURE;
            mSession.setRepeatingRequest(mPreviewBuilder.build(), mSessionCaptureCallback, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == Constants.PHOTO_REQUEST_CAREMA || requestCode == Constants.PHOTO_REQUEST_GALLERY) {
            Uri imageUri = null;
            if (requestCode == Constants.PHOTO_REQUEST_CAREMA) {
                // Android7.0开始，应用的私有目录访问权限被限制，不能通过file://uri来访问其他应用的私有目录文件，所以我们使用contentProvider
                // 来帮助我们将访问受限的file://uri转化成授权共享的content://uri
                // From Android 7.0, the application's private directory access
                // is restricted and can not be accessed via file:// uri to the
                // private directory file for other applications, so we use the
                // contentProvider to help us convert the restricted
                // uri(file://) to an authorized share uri(content://)
                /*if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                    imageUri = Uri.parse("file://" + mPhotoFilePath);
                }else{
                    File file = new File(mPhotoFilePath);
                    imageUri = FileProvider.getUriForFile(this, Constants.PROVIDER_NAME, file);
                }*/
            }
            if (requestCode == Constants.PHOTO_REQUEST_GALLERY) {
               /* if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                    imageUri = ImageFilePath.getImageContentUri(this, data);
                }else {
                    File file = new File(getImagePath(this, data.getData()));
                    imageUri = FileProvider.getUriForFile(this, Constants.PROVIDER_NAME, file);
                }*/
            }
            // 如果uri不为空，进行图片裁剪，通过裁剪可以在多人脸图中选择出想要进行搜索的人脸
            // if the uri is not null, go to crop picture, you can choose the
            // face you want to search from a picture which has multi-face
            if (imageUri != null) {
                //getCropImage(imageUri, mSearchImagePath, Constants.CROP_REQUEST_IMAGE);
            }
        } else if (requestCode == Constants.CROP_REQUEST_IMAGE) {
            // 裁剪成功，获取最新的人脸特征列表，启动异步搜索任务
            // crop success, get the lastest face feature list, start async
            // search task
            /*mFeatureList = FaceSearchDBManager.getInstance(this).getAllFeatureInfos();
            if (mFeatureList == null || mFeatureList.size() == 0) {
                AlertError.showDialog(this, null, getString(R.string.no_feature_hint));
                return;
            }
            SearchFaceAsyncTask task = new SearchFaceAsyncTask(this, mSearchImagePath, mFeatureList, this);
            task.execute();*/
        }
    }


}
