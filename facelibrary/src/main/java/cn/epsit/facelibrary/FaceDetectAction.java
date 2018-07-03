package cn.epsit.facelibrary;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by Administrator on 2018/7/4.
 */

public class FaceDetectAction implements SurfaceHolder.Callback {
    String TAG ="FaceDetectAction";
    private Camera camera;
    private CameraManager cameraManager;
    private boolean greetingFlag;
    private boolean signWorkFlag;
    private boolean takeNumberFlag;
    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView;
    private Context mContext;
    static FaceDetectAction faceDetectAction;

    private FaceDetectAction(){

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
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

            initCamera();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, "启动摄像头失败,可能是摄像头权限问题", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        initCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }
    private void initCamera() {
        if(surfaceHolder != null && camera != null) {
            try {
                camera.setPreviewDisplay(surfaceHolder);
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
    public static class Builder{
        private boolean greetingFlag;
        private boolean signWorkFlag;
        private boolean takeNumberFlag;
        private Context mContext;
        private SurfaceView surfaceView;
        public Builder init(Context context){
            mContext = context;
            return this;
        }

        public Builder setSurfaceView(SurfaceView surfaceView) {
            this.surfaceView = surfaceView;
            return this;
        }
        public FaceDetectAction build(){
            faceDetectAction = new FaceDetectAction();
            faceDetectAction.greetingFlag = greetingFlag;
            faceDetectAction.mContext = mContext;
            faceDetectAction.signWorkFlag = signWorkFlag;
            faceDetectAction.takeNumberFlag = takeNumberFlag;
            faceDetectAction.surfaceView = surfaceView;
            return faceDetectAction;
        }
    }
    public void startTracker(){
        surfaceHolder = surfaceView.getHolder();
        surfaceView.getHolder().addCallback(this);
    }
}
