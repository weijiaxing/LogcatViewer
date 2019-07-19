package com.weijiaxing.logcatviewer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.weijiaxing.logcatviewer.ui.UseFloatingLogcatServiceActivity;
import com.weijiaxing.logcatviewer.ui.UseLogcatActivity;
import com.weijiaxing.logcatviewer.ui.UseLogcatControlActivity;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }


    public void Logcat(View view) {

        Log.e(TAG, "Logcat");
        startActivity(new Intent(this, UseLogcatActivity.class));

    }

    public void FloatingLogcat(View view) {

        Log.e(TAG, "FloatingLogcat");
        startActivity(new Intent(this, UseFloatingLogcatServiceActivity.class));
    }

    public void LogcatControl(View view) {

        Log.e(TAG, "LogcatControl");
        startActivity(new Intent(this, UseLogcatControlActivity.class));

    }
}
