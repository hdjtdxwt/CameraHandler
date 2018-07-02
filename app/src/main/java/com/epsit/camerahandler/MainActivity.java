package com.epsit.camerahandler;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.camera).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        startActivity(new Intent(this,CameraActivity.class));
    }
}
