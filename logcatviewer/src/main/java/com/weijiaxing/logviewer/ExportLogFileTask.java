package com.weijiaxing.logviewer;


import android.os.AsyncTask;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExportLogFileTask extends AsyncTask<LogItem, Integer, File> {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
    private File mCacheDir;

    ExportLogFileTask(File cacheDir) {
        mCacheDir = cacheDir;
    }

    @Override
    protected File doInBackground(LogItem[] logs) {
        if (mCacheDir == null || mCacheDir.isFile() || logs == null || logs.length == 0) {
            return null;
        }

        File logFile = new File(mCacheDir, DATE_FORMAT.format(new Date()) + ".log");
        if (logFile.exists()) {
            if (!logFile.delete()) {
                return null;
            }
        }
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(logFile)));
            for (LogItem log : logs) {
                writer.write(log.origin + "\n");
            }
            writer.close();
            return logFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
