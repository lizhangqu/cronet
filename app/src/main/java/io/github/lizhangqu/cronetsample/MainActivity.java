package io.github.lizhangqu.cronetsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


import org.chromium.net.CronetEngine;
import org.chromium.net.CronetException;
import org.chromium.net.ExperimentalUrlRequest;
import org.chromium.net.RequestFinishedInfo;
import org.chromium.net.UploadDataProviders;
import org.chromium.net.UrlRequest;
import org.chromium.net.UrlResponseInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private CronetEngine mCronetEngine;

    private String mUrl;
    private EditText mUrlEditorText;
    private TextView mResultText;
    private TextView mReceiveDataText;

    class SimpleUrlRequestCallback extends UrlRequest.Callback {
        private ByteArrayOutputStream mBytesReceived = new ByteArrayOutputStream();
        private WritableByteChannel mReceiveChannel = Channels.newChannel(mBytesReceived);

        @Override
        public void onRedirectReceived(
                UrlRequest request, UrlResponseInfo info, String newLocationUrl) {
            Log.i(TAG, "****** onRedirectReceived ******");
            request.followRedirect();
        }

        @Override
        public void onResponseStarted(UrlRequest request, UrlResponseInfo info) {
            Log.i(TAG, "****** Response Started ******");
            Log.i(TAG, "*** Headers Are *** " + info.getAllHeaders());

            request.read(ByteBuffer.allocateDirect(32 * 1024));
        }

        @Override
        public void onReadCompleted(
                UrlRequest request, UrlResponseInfo info, ByteBuffer byteBuffer) {
            byteBuffer.flip();
            Log.i(TAG, "****** onReadCompleted ******" + byteBuffer);

            try {
                mReceiveChannel.write(byteBuffer);
            } catch (IOException e) {
                Log.i(TAG, "IOException during ByteBuffer read. Details: ", e);
            }
            byteBuffer.clear();
            request.read(byteBuffer);
        }

        @Override
        public void onSucceeded(UrlRequest request, UrlResponseInfo info) {
            Log.i(TAG, "****** Request Completed, status code is " + info.getHttpStatusCode()
                    + ", total received bytes is " + info.getReceivedByteCount());

            final String receivedData = mBytesReceived.toString();
            final String url = info.getUrl();
            final String text = "Completed " + url + " (" + info.getHttpStatusCode() + ")";
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mResultText.setText(text);
                    mReceiveDataText.setText(receivedData);
                }
            });
        }

        @Override
        public void onFailed(UrlRequest request, UrlResponseInfo info, CronetException error) {
            Log.i(TAG, "****** onFailed, error is: " + error.getMessage());
            final String url = mUrl;
            final String text = "Failed " + mUrl + " (" + error.getMessage() + ")";
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mResultText.setText(text);
                }
            });
        }
    }

    private static void readInputStream(InputStream inputStream) {
        if (inputStream != null) {
            try {
                byte[] buffer = new byte[1024];
                int len = -1;
                while ((len = inputStream.read(buffer)) != -1) {
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUrlEditorText = (EditText) findViewById(R.id.url);
        mResultText = (TextView) findViewById(R.id.resultView);
        mReceiveDataText = (TextView) findViewById(R.id.dataView);


        findViewById(R.id.head).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < 10; i++) {
                            InputStream inputStream = null;
                            try {
                                HttpURLConnection urlConnection = (HttpURLConnection) mCronetEngine.openConnection(new URL("https://si.geilicdn.com"));
                                urlConnection.setDoInput(true);
                                urlConnection.setDoOutput(true);
                                urlConnection.setRequestMethod("HEAD");
                                urlConnection.getOutputStream().write("a=b&b=c".getBytes());


                                Log.e(TAG, "getResponseCode:" + urlConnection.getResponseCode());
                                Log.e(TAG, "getRequestMethod:" + urlConnection.getRequestMethod());
                                Map<String, List<String>> headerFields = urlConnection.getHeaderFields();

                                if (urlConnection.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST) {
                                    InputStream errorStream = urlConnection.getErrorStream();
                                    readInputStream(errorStream);
                                    Log.e(TAG, "errorStream:" + errorStream);
                                } else {
                                    inputStream = urlConnection.getInputStream();
                                    readInputStream(inputStream);
                                    Log.e(TAG, "inputStream:" + inputStream);
                                }

                                Set<String> keys = headerFields.keySet();
                                for (String key : keys) {
                                    Log.e(TAG, key + "->" + headerFields.get(key));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                if (inputStream != null) {
                                    try {
                                        //必须close，否则容易阻塞
                                        inputStream.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }

                    }
                }).start();

            }
        });


        findViewById(R.id.start_request).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startWithURL(mUrlEditorText.getText().toString(), "param={}");
            }
        });

        findViewById(R.id.start_log).

                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startNetLog();
                    }
                });

        findViewById(R.id.stop_log).

                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        stopNetLog();


                    }
                });

        CronetEngine.Builder myBuilder = new CronetEngine.Builder(this);
        myBuilder.enableHttpCache(CronetEngine.Builder.HTTP_CACHE_IN_MEMORY, 100 * 1024)
//                .setHostResolver(hostname -> {
//                    Log.e("HostResolver", "resolve:" + hostname);
//                    List<InetAddress> inetAddresses = Arrays.asList(InetAddress.getAllByName(hostname));
//                    Log.e("HostResolver", "inetAddresses:" + inetAddresses);
//                    return inetAddresses;
//                })
                .enableHttp2(true)
                .enableQuic(false);
        Log.i(TAG, "setup");
        mCronetEngine = myBuilder.build();


        startWithURL(mUrlEditorText.getText().toString(), "param={}");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCronetEngine.shutdown();
        Log.i(TAG, "shutdown");
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

    private void startWithURL(String url) {
        startWithURL(url, null);
    }

    private void startWithURL(String url, String postData) {
        Log.i(TAG, "Cronet started: " + url);
        mUrl = url;

        Executor executor = Executors.newSingleThreadExecutor();
        UrlRequest.Callback callback = new SimpleUrlRequestCallback();
        ExperimentalUrlRequest.Builder builder = (ExperimentalUrlRequest.Builder) mCronetEngine.newUrlRequestBuilder(url, callback, executor);
        applyPostDataToUrlRequestBuilder(builder, executor, postData);
        builder.setRequestFinishedListener(new RequestFinishedInfo.Listener(executor) {
            @Override
            public void onRequestFinished(RequestFinishedInfo requestInfo) {
                Log.e(TAG, "url:" + requestInfo.getUrl());
                Log.e(TAG, "annotations:" + requestInfo.getAnnotations());
                Log.e(TAG, "exception:" + requestInfo.getException());
                Log.e(TAG, "finishedReason:" + requestInfo.getFinishedReason());
            }
        });
        builder.build().start();
    }

    // Starts writing NetLog to disk. startNetLog() should be called afterwards.
    private void startNetLog() {
        mCronetEngine.startNetLogToFile(getCacheDir().getPath() + "/netlog.json", false);
    }

    // Stops writing NetLog to disk. Should be called after calling startNetLog().
    // NetLog can be downloaded afterwards via:
    //   adb root
    //   adb pull /data/data/org.chromium.cronet_sample_apk/cache/netlog.json
    // netlog.json can then be viewed in https://netlog-viewer.appspot.com/#import
    private void stopNetLog() {
        mCronetEngine.stopNetLog();
    }
}
