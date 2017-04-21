package com.don.democatchhandler;

import android.Manifest.permission;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    int write = ActivityCompat.checkSelfPermission(this, permission.WRITE_EXTERNAL_STORAGE);
    int read = ActivityCompat.checkSelfPermission(this, permission.READ_EXTERNAL_STORAGE);
    if (write != PackageManager.PERMISSION_GRANTED || read != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]{
          permission.WRITE_EXTERNAL_STORAGE,
          permission.READ_EXTERNAL_STORAGE
      }, 0);
    }

    findViewById(R.id.btn_crash)
        .setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
                        throw new RuntimeException("test crash");

//            int[] myIntArray = new int[3];
//            Log.i(TAG, "" + myIntArray[3]);
          }
        });
  }
}
