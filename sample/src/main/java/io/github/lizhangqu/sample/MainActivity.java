package io.github.lizhangqu.sample;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.chromium.net.CronetEngine;
import org.chromium.net.HostResolver;
import org.chromium.net.urlconnection.CronetURLStreamHandlerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button mBtnSend;
    private Button mBtnHttpUrlConnection;
    private EditText mEditTextUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initHttpURLConnection(getApplicationContext());
        mEditTextUrl = (EditText) findViewById(R.id.url);
        mBtnSend = (Button) findViewById(R.id.send);
        mBtnHttpUrlConnection = (Button) findViewById(R.id.httpurlconnection);
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
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

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
