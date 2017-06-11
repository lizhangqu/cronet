package io.github.lizhangqu.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.chromium.net.UrlRequest;
import org.chromium.net.UrlRequestException;
import org.chromium.net.UrlResponseInfo;

import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CronetUtils.getsInstance().getHtml("https://fucknmb.com/", new UrlRequest.Callback() {
                    @Override
                    public void onRedirectReceived(UrlRequest urlRequest, UrlResponseInfo urlResponseInfo, String s) throws Exception {
                        Log.e("TAG", "onRedirectReceived");
                        urlRequest.followRedirect();

                    }

                    @Override
                    public void onResponseStarted(UrlRequest urlRequest, UrlResponseInfo urlResponseInfo) throws Exception {
                        Log.e("TAG", "onResponseStarted");
                        Log.e("TAG", "urlResponseInfo getUrl:" + urlResponseInfo.getUrl());
                        Log.e("TAG", "urlResponseInfo getProxyServer:" + urlResponseInfo.getProxyServer());
                        Log.e("TAG", "urlResponseInfo getHttpStatusText:" + urlResponseInfo.getHttpStatusText());
                        Log.e("TAG", "urlResponseInfo getNegotiatedProtocol:" + urlResponseInfo.getNegotiatedProtocol());
                        Log.e("TAG", "urlResponseInfo getAllHeaders:" + urlResponseInfo.getAllHeaders());
                        Log.e("TAG", "urlResponseInfo getReceivedBytesCount:" + urlResponseInfo.getReceivedBytesCount());
                        Log.e("TAG", "urlResponseInfo getUrlChain:" + urlResponseInfo.getUrlChain());

                    }

                    @Override
                    public void onReadCompleted(UrlRequest urlRequest, UrlResponseInfo urlResponseInfo, ByteBuffer byteBuffer) throws Exception {
                        Log.e("TAG", "onReadCompleted");
                    }

                    @Override
                    public void onSucceeded(UrlRequest urlRequest, UrlResponseInfo urlResponseInfo) {
                        Log.e("TAG", "onSucceeded");
                    }

                    @Override
                    public void onFailed(UrlRequest urlRequest, UrlResponseInfo urlResponseInfo, UrlRequestException e) {
                        Log.e("TAG", "onFailed");
                        e.printStackTrace();
                    }
                });
            }
        });
    }
}
