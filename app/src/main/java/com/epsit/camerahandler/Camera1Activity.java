package com.epsit.camerahandler;

import android.hardware.Camera;
import android.hardware.camera2.CameraDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;

public class Camera1Activity extends AppCompatActivity  implements SurfaceHolder.Callback {
    String TAG ="Camera1Activity";
    private SurfaceView surfaceView;//预览摄像头
    private SurfaceHolder surfaceHolder;
    private Button button;//拍照按钮
    private Camera camera;//摄像头

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera1);
        initView();
        initData();
        initListener();
    }

    //初始化View的方法,其实少的话都放到
    private void initView() {
        surfaceView = (SurfaceView) findViewById(R.id.main_surface_view);
        button = (Button) findViewById(R.id.main_button);
    }

    private void initData() {
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
    }

    private void initListener() {
        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Camera1Activity.this, "surfaceView", Toast.LENGTH_SHORT).show();
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Camera1Activity.this, "button", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initCamera() {
        if(camera!=null) {
            camera.startPreview();
        }
    }
    private int getDefaultCameraId()
    {
        int defaultId = -1;

        // Find the total number of cameras available
        int mNumberOfCameras = Camera.getNumberOfCameras();

        // Find the ID of the default camera
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < mNumberOfCameras; i++)
        {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK)
            {
                defaultId = i;
            }
        }
        if (-1 == defaultId)
        {
            if (mNumberOfCameras > 0)
            {
                // 如果没有后向摄像头
                defaultId = 0;
            }
            else
            {
                // 没有摄像头
                Toast.makeText(getApplicationContext(), "没有摄像头", Toast.LENGTH_LONG).show();
            }
        }
        return defaultId;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            /*int cameraId = getDefaultCameraId();
            Log.e("surfaceCreated","surfaceCreated-->cameraid="+cameraId);
            camera = Camera.open(cameraId);*/
            boolean cameraCount = false;
            release();
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            int var10 = Camera.getNumberOfCameras();

            for(int parameters = 0; parameters < var10; ++parameters) {
                Camera.getCameraInfo(parameters, cameraInfo);
                if(cameraInfo.facing == 1) {
                    try {
                        camera = Camera.open(parameters);
                        Log.e(TAG,"[开启摄像头] camera：" + camera);
                    } catch (RuntimeException var9) {
                        var9.printStackTrace();
                        Log.e(TAG,"[开启摄像头失败] ：" + var9.toString());
                        Log.e("FaceDetectAction", "startDetect:开启摄像头失败 ：" + var9.toString());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(Camera1Activity.this, "启动摄像头失败,可能是摄像头权限问题", Toast.LENGTH_SHORT).show();
        }
        if(holder != null && camera != null) {
            try {
                camera.setPreviewDisplay(holder);
                Log.e(TAG,"[设置Holder成功]");
            } catch (IOException var8) {
                var8.printStackTrace();
                Log.e(TAG, "[设置Holder失败] ：" + var8.toString());
            }
        }

        if(camera != null) {
            Log.d("FaceDetectAction", "startDetect:开启摄像头成功");
            Log.e(TAG, "[开启摄像头成功]");
            Camera.Parameters var11 = null;
            try {
                var11 = camera.getParameters();
            } catch (Exception var7) {
                var7.printStackTrace();
            }

            if (var11 != null) {
                var11.setPreviewFormat(17);
                camera.setParameters(var11);
            }
            camera.startPreview();
            camera.setPreviewCallback(new Camera.PreviewCallback() {
                public void onPreviewFrame(byte[] data, Camera camera) {
                    /*Camera.Size previewSize = camera.getParameters().getPreviewSize();
                    if (System.currentTimeMillis() - FaceDetectAction.lastSearchTime > FaceDetectAction.mTime) {
                        FaceDetectAction.lastSearchTime = System.currentTimeMillis();
                        FaceDetectAction.mFaceDetector.detect(data, previewSize.width, previewSize.height);
                    }*/

                }
            });
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        initCamera();
    }
    public void release() {
        try {
            if(camera != null) {
               Log.e(TAG,"[释放摄像头资源]");
                camera.stopPreview();
                camera.setPreviewCallback((Camera.PreviewCallback)null);
                camera.release();
                camera = null;
                Log.e(TAG,"[释放摄像头资源成功]");
            }
        } catch (Exception var1) {
            Log.e(TAG,"[释放摄像头资源Error]" + var1.toString());
        }

    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (null != camera) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }
}
