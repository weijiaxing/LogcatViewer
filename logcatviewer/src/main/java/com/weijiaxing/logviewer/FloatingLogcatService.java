package com.weijiaxing.logviewer;


import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
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

public class FloatingLogcatService extends Service implements View.OnClickListener {


    private WindowManager wm;

    public static void launch(Context context) {
        context.startService(new Intent(context, FloatingLogcatService.class));
    }

    private View mRoot;
    private Toolbar mToolbar;
    private Spinner mSpinner;
    private ListView mList;
    private ImageView mIvClean;
    private ImageView mIvShare;
    private ImageView mIvZoomOut;

    private LogcatAdapter mAdapter = new LogcatAdapter();
    private volatile boolean mReading = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mReading) {
            return super.onStartCommand(intent, flags, startId);
        }

        ContextThemeWrapper context = new ContextThemeWrapper(this, R.style.Theme_AppCompat_NoActionBar);
        mRoot = View.inflate(context, R.layout.service_floating_logcat, null);
        mToolbar = mRoot.findViewById(R.id.toolbar);
        mSpinner = mRoot.findViewById(R.id.spinner);

        mIvClean = mRoot.findViewById(R.id.iv_clean);
        mIvShare = mRoot.findViewById(R.id.iv_share);
        mIvZoomOut = mRoot.findViewById(R.id.iv_zoomout);

        mIvClean.setOnClickListener(this);
        mIvShare.setOnClickListener(this);
        mIvZoomOut.setOnClickListener(this);

        mList = mRoot.findViewById(R.id.list);

        initViews();
        startReadLogcat();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (wm != null) {
            wm.removeView(mRoot);
        }

        stopReadLogcat();
        super.onDestroy();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initViews() {
        final WindowManager.LayoutParams params;
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (wm == null) {
            return;
        } else {
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;

            params = new WindowManager.LayoutParams(
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,

                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,

                    PixelFormat.TRANSLUCENT);
            params.alpha = 1.0f;
            params.dimAmount = 0f;
            params.gravity = Gravity.CENTER;
            params.windowAnimations = android.R.style.Animation_Dialog;
            params.setTitle("Logcat Viewer");

            if (height > width) {
                params.width = (int) (width * .7);
                params.height = (int) (height * .5);
            } else {
                params.width = (int) (width * .7);
                params.height = (int) (height * .8);
            }

            wm.addView(mRoot, params);
        }

        mToolbar.setNavigationIcon(R.drawable.ic_action_close);
        mList.setBackgroundResource(R.color.logcat_floating_bg);
        mToolbar.getLayoutParams().height = getResources().getDimensionPixelSize(
                R.dimen.floating_toolbar_height);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSelf();
            }
        });

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.logcat_spinner, R.layout.item_float_logcat_dropdown);
        spinnerAdapter.setDropDownViewResource(R.layout.item_float_logcat_dropdown);
        mSpinner.setAdapter(spinnerAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

                LogcatDetailActivity.launch(getApplicationContext(), mAdapter.getItem(position));
            }
        });

        mToolbar.setOnTouchListener(new View.OnTouchListener() {

            boolean mIntercepted = false;
            int mLastX;
            int mLastY;
            int mFirstX;
            int mFirstY;
            int mTouchSlop = ViewConfiguration.get(getApplicationContext()).getScaledTouchSlop();

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int totalDeltaX = mLastX - mFirstX;
                int totalDeltaY = mLastY - mFirstY;

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        mLastX = (int) event.getRawX();
                        mLastY = (int) event.getRawY();
                        mFirstX = mLastX;
                        mFirstY = mLastY;
                        break;
                    case MotionEvent.ACTION_UP:
                        if (!mIntercepted) {
                            v.performClick();
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int deltaX = (int) event.getRawX() - mLastX;
                        int deltaY = (int) event.getRawY() - mLastY;
                        mLastX = (int) event.getRawX();
                        mLastY = (int) event.getRawY();

                        if (Math.abs(totalDeltaX) >= mTouchSlop || Math.abs(totalDeltaY) >= mTouchSlop) {
                            if (event.getPointerCount() == 1) {
                                params.x += deltaX;
                                params.y += deltaY;
                                mIntercepted = true;
                                wm.updateViewLayout(mRoot, params);
                            } else {
                                mIntercepted = false;
                            }
                        } else {
                            mIntercepted = false;
                        }
                        break;
                    default:
                        break;
                }
                return mIntercepted;
            }
        });
    }

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

                        Snackbar.make(mRoot, R.string.create_log_file_failed, Snackbar.LENGTH_SHORT).show();

                    } else {

                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        shareIntent.setType("text/plain");

                        Uri uri = LogcatFileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".logcat_fileprovider", file);
                        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);

                        if (getPackageManager().queryIntentActivities(shareIntent, 0).isEmpty()) {
                            Snackbar.make(mRoot, R.string.not_support_on_this_device, Snackbar.LENGTH_SHORT).show();

                        } else {
                            startActivity(shareIntent);
                        }
                    }
                }
            };

            task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, mAdapter.getData());


        } else if (v.getId() == R.id.iv_zoomout) {

            wm.removeView(mRoot);
            stopReadLogcat();

            LogcatActivity.launchNewTask(this);
        }

    }
}
