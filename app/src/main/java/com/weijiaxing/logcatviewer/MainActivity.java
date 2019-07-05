package com.weijiaxing.logcatviewer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.weijiaxing.logviewer.LogcatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //use
        LogcatActivity.launch(MainActivity.this);
    }
}
