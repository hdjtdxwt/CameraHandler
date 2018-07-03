package cn.epsit.ihealth.robot;


import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;


import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    String TAG ="MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.camera).setOnClickListener(this);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            Log.e(TAG,"=-----权限获取");
            requestAllPermissionsIfNeed();
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

            } else {
                permissionList.add(Manifest.permission.CAMERA);
            }
        }
        // 我们需要从应用外的目录获取照片，所以需要申请读取外部存储权限
        // read external storage permission, for we need to read the photos
        // outside application-specific directories
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {
                permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
        if(permissionList.size()>0){
            requestPermissions( permissionList.toArray(new String[permissionList.size()]), 0);
        }
    }
    @Override
    public void onClick(View v) {
        startActivity(new Intent(this, Camera1Activity.class));
    }
}
