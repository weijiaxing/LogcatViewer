package com.weijiaxing.logviewer;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorRes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogItem implements Parcelable {

    private static final String PRIORITY_VERBOSE = "V";
    private static final String PRIORITY_DEBUG = "D";
    private static final String PRIORITY_INFO = "I";
    private static final String PRIORITY_WARNING = "W";
    private static final String PRIORITY_ERROR = "E";
    private static final String PRIORITY_FATAL = "F";

    private static final Pattern sLogcatPattern = Pattern.compile(
              "([0-9^-]+-[0-9^ ]+ [0-9^:]+:[0-9^:]+\\.[0-9]+) +([0-9]+) +([0-9]+) ([VDIWEF]) ([^ ]*) *: (.*)");
    private static final SimpleDateFormat sDataFormat = new SimpleDateFormat(
            "MM-dd hh:mm:ss.SSS", Locale.getDefault());

    private static final HashMap<String, Integer> LOGCAT_COLORS = new HashMap<String, Integer>() {{
        put(PRIORITY_VERBOSE, R.color.logcat_verbose);
        put(PRIORITY_DEBUG, R.color.logcat_debug);
        put(PRIORITY_INFO, R.color.logcat_info);
        put(PRIORITY_WARNING, R.color.logcat_warning);
        put(PRIORITY_ERROR, R.color.logcat_error);
        put(PRIORITY_FATAL, R.color.logcat_fatal);
    }};

    private static final ArrayList<String> SUPPORTED_FILTERS = new ArrayList<String>() {{
        add(PRIORITY_VERBOSE);
        add(PRIORITY_DEBUG);
        add(PRIORITY_INFO);
        add(PRIORITY_WARNING);
        add(PRIORITY_ERROR);
        add(PRIORITY_FATAL);
    }};

    static final ArrayList<String> IGNORED_LOG = new ArrayList<String>() {{
        add("--------- beginning of crash");
        add("--------- beginning of main");
        add("--------- beginning of system");
    }};

    public Date time;
    public int processId;
    public int threadId;
    public String priority;
    public String tag;
    public String content;
    public String origin;

    LogItem(String line) throws IllegalStateException, ParseException {
        Matcher matcher = sLogcatPattern.matcher(line);
        if (!matcher.find()) {
            throw new IllegalStateException("logcat pattern not match: " + line);
        }

        String timeText = matcher.group(1);
        String pidText = matcher.group(2);
        String tidText = matcher.group(3);
        String tagText = matcher.group(4);
        String prefixText = matcher.group(5);
        String contentText = matcher.group(6);

        time = sDataFormat.parse(timeText);
        processId = Integer.parseInt(pidText);
        threadId = Integer.parseInt(tidText);
        priority = tagText;
        tag = prefixText;
        content = contentText;
        origin = line;
    }

    @ColorRes
    int getColorRes() {
        return LOGCAT_COLORS.get(priority);
    }

    boolean isFiltered(String filter) {
        return SUPPORTED_FILTERS.indexOf(priority) < SUPPORTED_FILTERS.indexOf(filter);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.time != null ? this.time.getTime() : -1);
        dest.writeInt(this.processId);
        dest.writeInt(this.threadId);
        dest.writeString(this.priority);
        dest.writeString(this.tag);
        dest.writeString(this.content);
        dest.writeString(this.origin);
    }

    public LogItem() {
    }

    private LogItem(Parcel in) {
        long tmpTime = in.readLong();
        this.time = tmpTime == -1 ? null : new Date(tmpTime);
        this.processId = in.readInt();
        this.threadId = in.readInt();
        this.priority = in.readString();
        this.tag = in.readString();
        this.content = in.readString();
        this.origin = in.readString();
    }

    public static final Parcelable.Creator<LogItem> CREATOR = new Parcelable.Creator<LogItem>() {
        @Override
        public LogItem createFromParcel(Parcel source) {
            return new LogItem(source);
        }

        @Override
        public LogItem[] newArray(int size) {
            return new LogItem[size];
        }
    };
}
