package io.github.lizhangqu.sample;

import android.content.Context;
import android.util.Log;

import org.chromium.net.CronetEngine;
import org.chromium.net.HostResolver;
import org.chromium.net.UploadDataProviders;
import org.chromium.net.UrlRequest;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @version V1.0
 * @author: lizhangqu
 * @date: 2017-06-11 17:24
 */
public class CronetUtils {
    private static CronetUtils sInstance;
    private CronetEngine mCronetEngine;
    private Executor mExecutor = Executors.newFixedThreadPool(4);

    private CronetUtils() {
    }

    public static synchronized CronetUtils getInstance() {
        if (sInstance == null) {
            sInstance = new CronetUtils();
        }
        return sInstance;
    }

    public synchronized void init(Context context) {
        if (mCronetEngine == null) {
            CronetEngine.Builder builder = new CronetEngine.Builder(context);
            builder.
                    enableHttpCache(CronetEngine.Builder.HTTP_CACHE_IN_MEMORY,
                            100 * 1024)
                    .enableHttp2(true)
                    .enableQuic(true)
                    .setHostResolver(new HostResolver() {
                        @Override
                        public List<InetAddress> resolve(String hostname) throws UnknownHostException {
                            Log.e("TAG", "HostResolver resolve");
                            if (hostname == null)
                                throw new UnknownHostException("hostname == null");
                            return Arrays.asList(InetAddress.getAllByName(hostname));
                        }
                    })
                    .enableSDCH(true)
                    .setLibraryName("cronet");
            mCronetEngine = builder.build();
        }
    }

    public void getHtml(String url, UrlRequest.Callback callback) {
        startWithURL(url, callback);
    }

    public void startWithURL(String url, UrlRequest.Callback callback) {
        startWithURL(url, callback, null);
    }

    public void startWithURL(String url, UrlRequest.Callback callback, String postData) {
        UrlRequest.Builder builder = new UrlRequest.Builder(url, callback, mExecutor, mCronetEngine);
        applyPostDataToUrlRequestBuilder(builder, mExecutor, postData);
        builder.build().start();
    }

    private void applyPostDataToUrlRequestBuilder(
            UrlRequest.Builder builder, Executor executor, String postData) {
        if (postData != null && postData.length() > 0) {
            builder.setHttpMethod("POST");
            builder.addHeader("Content-Type", "application/x-www-form-urlencoded");
            builder.setUploadDataProvider(
                    UploadDataProviders.create(postData.getBytes()), executor);
        }
    }
}
