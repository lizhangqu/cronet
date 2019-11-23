package io.github.lizhangqu.cronetsample;

import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;

/**
 * @author lizhangqu
 * @version V1.0
 * @since 2018-12-29 17:33
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(getApplicationContext());
        strategy.setEnableANRCrashMonitor(true);
        strategy.setEnableNativeCrashMonitor(true);
        strategy.setBuglyLogUpload(true);
        CrashReport.initCrashReport(getApplicationContext(), "865e103a10", false, strategy);

    }
}
