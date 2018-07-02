package com.epsit.camerahandler;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

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
    CaptureRequest.Builder mPreviewRequestBuilder;
    CaptureRequest mPreviewRequest;
    SurfaceHolder mSurfaceHolder;
    SurfaceView surfaceView;
    CameraCaptureSession mCaptureSession;

    Handler mBackgroundHandler = new Handler(){

    };
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        surfaceView = (SurfaceView) findViewById(R.id.camera_surfaceview);
        mSurfaceHolder = surfaceView.getHolder();

        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            //获取可用摄像头列表
            for (String cameraId : manager.getCameraIdList()) {
                //获取相机的相关参数
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                // 不使用前置摄像头。
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }
                // 检查闪光灯是否支持。
                Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mCameraId = cameraId;
                Log.e(TAG, " 相机可用 ");
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            //不支持Camera2API
        }

        try {
            //打开相机预览
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
            manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


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
                if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                    imageUri = Uri.parse("file://" + mPhotoFilePath);
                }else{
                    File file = new File(mPhotoFilePath);
                    imageUri = FileProvider.getUriForFile(this, Constants.PROVIDER_NAME, file);
                }
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

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            //创建CameraPreviewSession
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            cameraDevice.close();
            mCameraDevice = null;
        }

    };
    /**
     * 为相机预览创建新的CameraCaptureSession
     */
    private void createCameraPreviewSession() {
        try {
            //设置了一个具有输出Surface的CaptureRequest.Builder。
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(mSurfaceHolder.getSurface());
            //创建一个CameraCaptureSession来进行相机预览。
            mCameraDevice.createCaptureSession(Arrays.asList(mSurfaceHolder.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // 相机已经关闭
                            if (null == mCameraDevice) {
                                return;
                            }
                            // 会话准备好后，我们开始显示预览
                            mCaptureSession = cameraCaptureSession;
                            try {
                                // 自动对焦应
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                // 闪光灯
                                //setAutoFlash(mPreviewRequestBuilder);
                                // 开启相机预览并添加事件
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                //发送请求
                                mCaptureSession.setRepeatingRequest(mPreviewRequest, null, mBackgroundHandler);
                                Log.e(TAG," 开启相机预览并添加事件");
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed( @NonNull CameraCaptureSession cameraCaptureSession) {
                            Log.e(TAG," onConfigureFailed 开启预览失败");
                        }
                    }, null);
        } catch (CameraAccessException e) {
            Log.e(TAG," CameraAccessException 开启预览失败");
            e.printStackTrace();
        }
    }

}
