package io.github.lizhangqu.sample;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

/**
 * @version V1.0
 * @author: lizhangqu
 * @date: 2017-06-11 17:17
 */
public class App extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        CronetUtils.getInstance().init(getApplicationContext());
    }
}
