# LogcatViewer [![jitpack](https://jitpack.io/v/weijiaxing/LogcatViewer.svg)](https://jitpack.io/#weijiaxing/LogcatViewer) [![](https://img.shields.io/badge/License-Apache--2.0-brightgreen.svg)](https://github.com/weijiaxing/LogcatViewer/blob/master/LICENSE) 

### [README of English](https://github.com/weijiaxing/LogcatViewer/blob/master/README_EN.md)

LogcatViewer是一个Android浮动窗口日志输出库！获取应用程序的logcat输出日志并以窗口的形式悬浮在应用上层，支持悬浮窗口缩放，拖动，支持日志文件过滤，清除，保存分享。可以自定义接口远程动态控制LogcatViewer浮动日志窗口显示与关闭，测试阶段（方便测试人员和开发者定位异常），发布线上阶段(动态控制指定账号用户LogcatViewer浮动日志窗口显示与关闭，方便异常机型的用户反馈问题，开发人员针对指定机型进行适配)。说明：动态控制指定账号用户LogcatViewer浮动日志窗口显示与关闭，集成该库并参考示例代码UseLogcatControlActivity类中的remoteControlLogviewer()方法，isOpenLogcatViewer.equals(isOpenLogcatViewerCode)&& accountId.equals(loginAccountId)，后台动态控制LogcatViewer控制接口网络请求返回的参数isOpenLogcatViewer，accountId 和 isOpenLogcatViewerCode，loginAccountId 比较，确定用户或指定账号用户的LogcatViewer浮动日志窗口显示与关闭。

### LogcatViewer功能介绍

 * 日志输出过滤
 * 悬浮窗口拖动
 * 日志输出清除
 * 分享保存log日志文本
 * log日志页面放大缩小
 
### GooglePlay下载或二维码扫码下载Apk
<a href="https://play.google.com/store/apps/details?id=com.zytmcq.zy" target="_blank"> 
<img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png" alt="Get it on Google Play" height="90" /> <img src="https://www.pgyer.com/app/qrcode/P7Tt" height="90" /> </a>

   
### Usage
##### Step 1. Add a JitPack repertory
```
allprojects {
    repositories {
        ...
        maven { url "https://jitpack.io" }
    }
}
```

##### Step 2. Add a gradle dependency
```
dependencies {
	implementation 'com.github.weijiaxing:LogcatViewer:1.0.3'
}
```

##### Step 3. Add following provider code to your AndroidManifest.xml
```
<provider
    android:name="com.weijiaxing.logviewer.LogcatFileProvider"
    android:authorities="${applicationId}.logcat_fileprovider"
    android:grantUriPermissions="true"
    android:exported="false">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/logcat_filepaths" />
</provider>
```

##### Step 4. Use the class in the need to print the log
```
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Use this method
        LogcatActivity.launch(MainActivity.this);
    }
}
```



## Screenshots

### 样例示例应用
<img src="screenshots/LogcatViewer01.png" width="220" hspace="20">  <img src="screenshots/LogcatViewer02.png" width="220" hspace="20">  <img src="screenshots/LogcatViewer03.png" width="220" hspace="20">


### 项目实战应用
<img src="screenshots/FloatLog.gif" width="220" hspace="20">  <img src="screenshots/FullScreenLog.gif" width="220" hspace="20">  <img src="screenshots/LogTextShare.gif" width="220" hspace="20">



### Thanks
- [fatangare：LogcatViewer](https://github.com/fatangare/LogcatViewer) 
- [kyze8439690：logcatviewer](https://github.com/kyze8439690/logcatviewer)


## License

```
Copyright (C) weijiaxing  Inc. Open source codes for study only.
Do not use for commercial purpose.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
