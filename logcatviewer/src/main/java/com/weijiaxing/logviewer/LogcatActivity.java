package com.weijiaxing.logviewer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;

public class LogcatActivity extends AppCompatActivity implements View.OnClickListener {

    public static void launch(Context context) {
        context.startActivity(new Intent(context, LogcatActivity.class));
    }


    public static void launchNewTask(Context context) {

        Intent intent = new Intent(context, LogcatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private static final int REQUEST_SCREEN_OVERLAY = 23453;

    private View mRoot;
    private ListView mList;

    private LogcatAdapter mAdapter = new LogcatAdapter();
    private boolean mReading = false;

    private ImageView mIvClean;
    private ImageView mIvShare;
    private ImageView mIvZoomOut;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(Color.parseColor("#1a1a1a"));
        }
        setContentView(R.layout.activity_logcat);
        mRoot = findViewById(R.id.root);
        Toolbar toolbar = findViewById(R.id.toolbar);
        Spinner spinner = findViewById(R.id.spinner);

        mIvClean = mRoot.findViewById(R.id.iv_clean);
        mIvShare = mRoot.findViewById(R.id.iv_share);
        mIvZoomOut = mRoot.findViewById(R.id.iv_zoomout);

        mIvClean.setOnClickListener(this);
        mIvShare.setOnClickListener(this);
        mIvZoomOut.setOnClickListener(this);

        mList = findViewById(R.id.list);

        toolbar.setNavigationIcon(R.drawable.ic_action_close);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.logcat_spinner, R.layout.item_logcat_dropdown);
        spinnerAdapter.setDropDownViewResource(R.layout.item_logcat_dropdown);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String filter = getResources().getStringArray(R.array.logcat_spinner)[position];
                mAdapter.getFilter().filter(filter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        mList.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        mList.setStackFromBottom(true);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                LogItem item = mAdapter.getItem(position);

                LogcatDetailActivity.launch(LogcatActivity.this, item);
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {

            return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SCREEN_OVERLAY && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && Settings.canDrawOverlays(getApplicationContext())) {
            FloatingLogcatService.launch(getApplicationContext());
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startReadLogcat();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopReadLogcat();
    }

    // sample:
    // 10-21 16:01:46.539  1949  2233 I NetworkController.MobileSignalController(2):  showDisableIcon:false
    private void startReadLogcat() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                mReading = true;
                BufferedReader reader = null;
                try {
                    Process process = new ProcessBuilder("logcat", "-v", "threadtime").start();
                    reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while (mReading && (line = reader.readLine()) != null) {
                        if (LogItem.IGNORED_LOG.contains(line)) {
                            continue;
                        }
                        try {
                            final LogItem item = new LogItem(line);
                            mList.post(new Runnable() {
                                @Override
                                public void run() {
                                    mAdapter.append(item);
                                }
                            });
                        } catch (ParseException | NumberFormatException | IllegalStateException e) {
                            e.printStackTrace();
                        }
                    }
                    stopReadLogcat();
                } catch (IOException e) {
                    e.printStackTrace();
                    stopReadLogcat();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private void stopReadLogcat() {
        mReading = false;
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.iv_clean) {
            mAdapter.clear();

        } else if (v.getId() == R.id.iv_share) {

            @SuppressLint("StaticFieldLeak")
            ExportLogFileTask task = new ExportLogFileTask(getExternalCacheDir()) {
                @Override
                protected void onPostExecute(File file) {
                    if (file == null) {
                        Snackbar.make(mRoot, R.string.create_log_file_failed, Snackbar.LENGTH_SHORT)
                                .show();
                    } else {
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);

                        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        shareIntent.setType("text/plain");
                        Uri uri = LogcatFileProvider.getUriForFile(getApplicationContext(),
                                getPackageName() + ".logcat_fileprovider", file);
                        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                        if (getPackageManager().queryIntentActivities(
                                shareIntent, 0).isEmpty()) {
                            Snackbar.make(mRoot, R.string.not_support_on_this_device,
                                    Snackbar.LENGTH_SHORT).show();
                        } else {
                            startActivity(shareIntent);
                        }
                    }
                }
            };
            task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, mAdapter.getData());
        }

        //设置悬浮窗
        else if (v.getId() == R.id.iv_zoomout) {

            Context context = getApplicationContext();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                if (getPackageManager().queryIntentActivities(intent, 0).isEmpty()) {
                    Snackbar.make(mRoot, R.string.not_support_on_this_device,
                            Snackbar.LENGTH_SHORT).show();
                } else {
                    startActivityForResult(intent, REQUEST_SCREEN_OVERLAY);
                }
            } else {
                FloatingLogcatService.launch(context);
                finish();
            }

        }

    }

}
