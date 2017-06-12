package io.github.lizhangqu.sample;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.chromium.net.CronetEngine;
import org.chromium.net.HostResolver;
import org.chromium.net.UrlRequest;
import org.chromium.net.UrlRequestException;
import org.chromium.net.UrlResponseInfo;
import org.chromium.net.urlconnection.CronetURLStreamHandlerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private Button mBtnSend;
    private Button mBtnHttpUrlConnection;
    private Button mBtnEasyCronet;
    private EditText mEditTextUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initHttpURLConnection(getApplicationContext());
        mEditTextUrl = (EditText) findViewById(R.id.url);
        mBtnSend = (Button) findViewById(R.id.send);
        mBtnHttpUrlConnection = (Button) findViewById(R.id.httpurlconnection);
        mBtnEasyCronet = (Button) findViewById(R.id.easy_cronet);
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.addCategory("io.github.lizhangqu.WEBVIEW");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse(mEditTextUrl.getText().toString()));
                startActivity(intent);
            }
        });
        mBtnHttpUrlConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            URL url = new URL(mEditTextUrl.getText().toString());
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            Log.e("TAG", "connection:" + connection);
                            connection.setDoInput(true);
                            connection.setConnectTimeout(10000);
                            connection.setReadTimeout(10000);
                            connection.setRequestMethod("GET");
                            connection.connect();
                            int responseCode = connection.getResponseCode();
                            InputStream inputStream = connection.getInputStream();
                            ByteArrayOutputStream output = new ByteArrayOutputStream();
                            copy(inputStream, output);
                            output.close();
                            inputStream.close();
                            byte[] bytes = output.toByteArray();
                            String response = new String(bytes);
                            Log.e("TAG", "responseCode:" + responseCode);
                            Log.e("TAG", "response body:" + response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        mBtnEasyCronet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CronetEngine.Builder myBuilder = new CronetEngine.Builder(getApplicationContext());
                myBuilder.enableHttpCache(CronetEngine.Builder.HTTP_CACHE_IN_MEMORY, 100 * 1024)
                        .enableHttp2(true)
                        .enableQuic(true)
                        .enableSDCH(true)
                        .setHostResolver(new HostResolver() {
                            @Override
                            public List<InetAddress> resolve(String hostname) throws UnknownHostException {
                                if (hostname == null)
                                    throw new UnknownHostException("hostname == null");
                                return Arrays.asList(InetAddress.getAllByName(hostname));
                            }
                        })
                        .enableSDCH(true)
                        .setLibraryName("cronet");
                CronetEngine cronetEngine = myBuilder.build();
                Executor executor = Executors.newFixedThreadPool(4);
                UrlRequest.Builder builder = new UrlRequest.Builder(mEditTextUrl.getText().toString(), new UrlRequest.Callback() {

                    private ByteArrayOutputStream mBytesReceived = new ByteArrayOutputStream();
                    private WritableByteChannel mReceiveChannel = Channels.newChannel(mBytesReceived);

                    @Override
                    public void onRedirectReceived(UrlRequest urlRequest, UrlResponseInfo urlResponseInfo, String s) throws Exception {
                        Log.i("TAG", "onRedirectReceived");
                        urlRequest.followRedirect();
                    }

                    @Override
                    public void onResponseStarted(UrlRequest urlRequest, UrlResponseInfo urlResponseInfo) throws Exception {
                        Log.i("TAG", "onResponseStarted");
                        urlRequest.read(ByteBuffer.allocateDirect(32 * 1024));
                    }

                    @Override
                    public void onReadCompleted(UrlRequest urlRequest, UrlResponseInfo urlResponseInfo, ByteBuffer byteBuffer) throws Exception {
                        Log.i("TAG", "onReadCompleted");
                        byteBuffer.flip();

                        try {
                            mReceiveChannel.write(byteBuffer);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        byteBuffer.clear();
                        urlRequest.read(byteBuffer);
                    }

                    @Override
                    public void onSucceeded(UrlRequest urlRequest, UrlResponseInfo urlResponseInfo) {
                        Log.i("TAG", "onSucceeded");
                        Log.i("TAG", String.format("Request Completed, status code is %d, total received bytes is %d",
                                urlResponseInfo.getHttpStatusCode(), urlResponseInfo.getReceivedBytesCount()));

                        final String receivedData = mBytesReceived.toString();
                        final String url = urlResponseInfo.getUrl();
                        final String text = "Completed " + url + " (" + urlResponseInfo.getHttpStatusCode() + ")";

                        Log.i("TAG", "text:" + text);
                        Log.i("TAG", "receivedData:" + receivedData);
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "onSucceeded", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onFailed(UrlRequest urlRequest, UrlResponseInfo urlResponseInfo, UrlRequestException e) {
                        Log.i("TAG", "onFailed");
                        Log.i("TAG", "error is: %s" + e.getMessage());

                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "onFailed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }, executor, cronetEngine);
                builder.build().start();
            }
        });
    }

    private static void initHttpURLConnection(Context context) {
        CronetEngine.Builder myBuilder = new CronetEngine.Builder(context);
        myBuilder.enableHttpCache(CronetEngine.Builder.HTTP_CACHE_IN_MEMORY, 100 * 1024)
                .enableHttp2(true)
                .enableQuic(true)
                .enableSDCH(true)
                .setHostResolver(new HostResolver() {
                    @Override
                    public List<InetAddress> resolve(String hostname) throws UnknownHostException {
                        if (hostname == null)
                            throw new UnknownHostException("hostname == null");
                        return Arrays.asList(InetAddress.getAllByName(hostname));
                    }
                })
                .enableSDCH(true)
                .setLibraryName("cronet");
        CronetEngine cronetEngine = myBuilder.build();
        CronetURLStreamHandlerFactory cronetURLStreamHandlerFactory = new CronetURLStreamHandlerFactory(cronetEngine);
        URL.setURLStreamHandlerFactory(cronetURLStreamHandlerFactory);
    }

    public static long copy(InputStream input, OutputStream output) throws IOException {
        return copyLarge(input, output, new byte[2048]);
    }

    public static long copyLarge(InputStream input, OutputStream output, byte[] buffer)
            throws IOException {
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
}
