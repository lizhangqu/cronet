cronet is a framework that using chromium net to send network request for android
====================================

[ ![Download](https://api.bintray.com/packages/lizhangqu/maven/io.github.lizhangqu:cronet-api/images/download.svg) ](https://bintray.com/lizhangqu/maven/io.github.lizhangqu:cronet/_latestVersion)

Changelog
---------

Current version 73.0.3653.4 released on 19th Jun 2019.

See details in [CHANGELOG](https://github.com/lizhangqu/cronet/blob/master/CHANGELOG.md).


Examples
--------

I have provided a sample.

See sample [here on Github](https://github.com/lizhangqu/cronet/tree/master/app).

To run the sample application, simply clone this repository and use android studio to compile, install it on a connected device.


Feature
---------

 - Full platform supports the latest version of TLS.
 - The platform supports the latest network protocols such as HTTP/2 and QUIC.

Usage
-----

**Maven**

```
<dependency>
	<groupId>io.github.lizhangqu</groupId>
	<artifactId>cronet-native</artifactId>
	<version>73.0.3653.0.4</version>
</dependency>
```

**Gradle**

```
compile 'io.github.lizhangqu:cronet-native:73.0.3653.0.4'
```

**Remote so**

The cronet's so file is big, you can use remote mode to reduce the apk size by exclude cronet-so module.

```
compile ('io.github.lizhangqu:cronet-native:73.0.3653.0.3'){
    exclude group: 'io.github.lizhangqu', module: 'cronet-so'
}
```

And add custom library loader when init cronet.

```
try {
    CronetEngine.Builder myBuilder = new CronetEngine.Builder(this);
    myBuilder.enableHttpCache(CronetEngine.Builder.HTTP_CACHE_IN_MEMORY, 100 * 1024)
            .setLibraryLoader(new ChromiumLibraryLoader(this)) //set library to such as ChromiumLibraryLoader impl
            .enableHttp2(true)
            .enableQuic(false);
    Log.i(TAG, "setup");
    CronetEngine cronetEngine = myBuilder.build();
} catch (Throwable e) {

}
```

You should use the httpurlconnection style api for downgrade

```
public HttpURLConnection createHttpURLConnection(CronetEngine cronetEngine String url) {
    try {
        return (HttpURLConnection) cronetEngine.openConnection(new URL(url));
    } catch (Exception e) {
        try {
            return (HttpURLConnection) new URL(url).openConnection();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    return null;
}


private void sendHeadRequestByHurl() {
    InputStream inputStream = null;
    try {
        HttpURLConnection urlConnection = createHttpURLConnection(cronetEngine, "url");
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        urlConnection.setRequestMethod("HEAD");
        urlConnection.getOutputStream().write("a=b&b=c".getBytes());
    
        Map<String, List<String>> headerFields = urlConnection.getHeaderFields();
    
        if (urlConnection.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST) {
            InputStream errorStream = urlConnection.getErrorStream();
            readInputStream(errorStream);
        } else {
            inputStream = urlConnection.getInputStream();
            readInputStream(inputStream);
           
        }
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

```

**NDK abiFilters**

This library add all so default, if you need add only one, you should use ndk abiFilters yourself.

I suggest that you only add **abiFilters "armeabi-v7a"**.

```
android {
    defaultConfig {
        ndk {
            abiFilters "armeabi-v7a"
            
//          default is no filters       
//          abiFilters "armeabi"
//          abiFilters "armeabi-v7a"
//          abiFilters "arm64-v8a"
//          abiFilters "x86"
//          abiFilters "x86_64"
//          abiFilters "mips"
//          abiFilters "mips64"
        }
    }
}
```

**Create Engine**

```
CronetEngine.Builder builder = new CronetEngine.Builder(context);
builder.
        enableHttpCache(CronetEngine.Builder.HTTP_CACHE_IN_MEMORY,
                100 * 1024) // cache
        .enableHttp2(true)  // Http/2.0 Supprot
        .enableQuic(true)   // Quic Supprot
        .setHostResolver(new HostResolver() {
            @Override
            public List<InetAddress> resolve(String hostname) throws UnknownHostException {
                if (hostname == null)
                    throw new UnknownHostException("hostname == null");
                return Arrays.asList(InetAddress.getAllByName(hostname));
            }
        })                  // custom dns, you can use httpdns here
        .enableSDCH(true)   // SDCH Supprot
        .setLibraryName("cronet");  // lib so name
CronetEngine cronetEngine = builder.build();
//see more config in the code
```

**Use For HttpUrlConnection**

You can use the method like OkHttp 

```
URL.setURLStreamHandlerFactory(new OkUrlFactory(new OkHttpClient()));
```

Cronet also support it.

```
CronetURLStreamHandlerFactory cronetURLStreamHandlerFactory = new CronetURLStreamHandlerFactory(cronetEngine);
URL.setURLStreamHandlerFactory(cronetURLStreamHandlerFactory);
```

And then you don't need to modify your java code like this.

```
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

```

**Attentation Please**

If you use the HttpURLConnection style api, you must read the inputstream anyway.

```
static void readInputStream(InputStream inputStream) {
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


InputStream inputStream = null;
try {
    inputStream = urlConnection.getInputStream();
} catch (IOException e) {
    inputStream = urlConnection.getErrorStream();
}
readInputStream(inputStream);
```


**Send GET Request**

```
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
```

**Send POST Request**

```
public void startWithURL(String url, UrlRequest.Callback callback, Executor executor, String postData) {
    UrlRequest.Builder builder = new UrlRequest.Builder(url, callback, executor, mCronetEngine);
    applyPostDataToUrlRequestBuilder(builder, executor, postData);
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
```

And then reuse the callback in **Send GET Request**

Thanks
-------

 - [chromium-net-android-porting](http://hanpfei.github.io/2016/10/18/chromium-net-android-porting/)
 - [chromium-compile-guide-for-android](http://hanpfei.github.io/2016/10/16/Chromium_Android%E7%BC%96%E8%AF%91%E6%8C%87%E5%8D%97/)
 - [lazy-chromium-net-android-porting-guide](http://hanpfei.github.io/2016/11/11/lazy-chromium-net-android-porting-guide/)
 - [chromium-gn-build-tools](http://hanpfei.github.io/2016/11/16/ChromiumGN%E6%9E%84%E5%BB%BA%E5%B7%A5%E5%85%B7%E7%9A%84%E4%BD%BF%E7%94%A8/)

License
--------
chromium net for android(cronet) is under the BSD license. See the [LICENSE](https://github.com/lizhangqu/chromium-net-for-android/blob/master/LICENSE) file for details.
