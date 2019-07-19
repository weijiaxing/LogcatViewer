package com.weijiaxing.logcatviewer.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.gson.Gson;
import com.weijiaxing.logcatviewer.R;
import com.weijiaxing.logcatviewer.been.LogviewControlBeen;
import com.weijiaxing.logcatviewer.common.Constant;
import com.weijiaxing.logviewer.LogcatActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UseLogcatControlActivity extends AppCompatActivity {

    private static String TAG = "UseLogcatControlActivity";
    private String loginAccountId;
    private String isOpenLogcatViewerCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_use_logcat_control);

        /*
         * 用户登录后得到的账号ID（loginAccountId） 示例中账号ID为20190701123456  是否开启浮动日志窗口码（isOpenLogcatViewerCode）可以自定定义参数
         * Account ID (loginAccountId) obtained after the user logs in. The account ID in the example is 20190701123456. Whether to enable the floating log window code (isOpenLogcatViewerCode) can customize the parameters.
         */
        loginAccountId = "20190701123456";
        isOpenLogcatViewerCode = "0";

        /*
         * 远程开启指定用户或玩家的logviewer日志输出窗口
         * (Remotely open the logviewer log output window of the specified user or player)
         */
        remoteControlLogviewer();
    }


    private void remoteControlLogviewer() {

        OkHttpClient okHttpClient = new OkHttpClient();

        Request.Builder builder = new Request.Builder();
        Request request = builder.get().url(Constant.CONTROL_LOGCATVIEWER_API).build();

        Call call = okHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                Log.e(TAG, "onFailure: ");
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                //获取远程控制接口返回的json
                assert response.body() != null;
                String string = response.body().string();

                Gson gson = new Gson();
                LogviewControlBeen logviewControlBeen = gson.fromJson(string, LogviewControlBeen.class);
                String isOpenLogcatViewer = logviewControlBeen.getData().getIsOpenLogcatViewer();
                String accountId = logviewControlBeen.getData().getAccountId();


                /*
                 * 是否打开LogcatViewer 可以自定义对应参数（isOpenLogcatViewerCode）
                 * 用户登录得到的账号ID(loginAccountId) 与 后台动态控制接口得到账号ID(accountId)
                 *
                 * Whether to open LogcatViewer can customize the corresponding parameters (isOpenLogcatViewerCode)
                 * The account ID (loginAccountId) obtained by the user login and the background dynamic control interface get the account ID (accountId)
                 *
                 */

                if (isOpenLogcatViewer.equals(isOpenLogcatViewerCode) && accountId.equals(loginAccountId)) {
                    Log.e(TAG, "指定账号ID：20190701123456 用户或玩家 Logviewer窗口开启 \n" +
                            "(Specify account ID: 20190701123456 User or player Logviewer window opens)");
                    LogcatActivity.launch(UseLogcatControlActivity.this);

                } else {
                    Log.e(TAG, "指定账号ID：20190701123456 用户或玩家 Logviewer窗口没有开启 \n" +
                            "(Specify account ID: 20190701123456 User or player Logviewer window is not open)");
                }
            }
        });
    }
}
