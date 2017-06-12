cronet is a framework that using chromium net to send network request for android
====================================

[ ![Download](https://api.bintray.com/packages/lizhangqu/maven/io.github.lizhangqu:cronet/images/download.svg) ](https://bintray.com/lizhangqu/maven/io.github.lizhangqu:cronet/_latestVersion)

Changelog
---------

Current version 0.0.1 released on 11th June 2017.

See details in [CHANGELOG](https://github.com/lizhangqu/chromium-net-for-android/blob/master/CHANGELOG.md).


Examples
--------

I have provided a sample.

See sample [here on Github](https://github.com/lizhangqu/chromium-net-for-android/tree/master/sample).

To run the sample application, simply clone this repository and use android studio to compile, install it on a connected device.

Chromium Net Source
-------
I have copied part of source from [chromium/src/net](https://chromium.googlesource.com/chromium/src/net/+/master).

See source [here on Github](https://github.com/lizhangqu/chromium-net-for-android/tree/master/cronet-source).

It's just for viewing the chromium net code more convenient. And it can't be compiled.

Usage
-----

**Maven**

```
<dependency>
  <groupId>io.github.lizhangqu</groupId>
  <artifactId>cronet</artifactId>
  <version>0.0.1</version>
</dependency>
```

**Gradle**

```
compile 'io.github.lizhangqu:cronet:0.0.1'
```

**Proguard**

```
-keep class org.chromium.** { *;}
-dontwarn org.chromium.**
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

And then you don't need to modify you java code like this.

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

License
--------
chromium net for android(cronet) is under the BSD license. See the [LICENSE](https://github.com/lizhangqu/chromium-net-for-android/blob/master/LICENSE) file for details.