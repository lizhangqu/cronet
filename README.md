cronet is a framework that using chromium net to send network request for android
====================================

[ ![Download](https://api.bintray.com/packages/lizhangqu/maven/io.github.lizhangqu:cronet/images/download.svg) ](https://bintray.com/lizhangqu/maven/io.github.lizhangqu:cronet/_latestVersion)

Changelog
---------

Current version 0.0.1 released on 11th June 2017.

See details in [CHANGELOG](https://github.com/lizhangqu/chromium-net-for-android/blob/master/CHANGELOG.md)



Examples
--------

I have provided a sample.
See sample [here on Github](https://github.com/lizhangqu/chromium-net-for-android/tree/master/sample)
To run the sample application, simply clone this repository and use android studio to compile, install it on a connected device


Usage
-----

**maven**

```
<dependency>
  <groupId>io.github.lizhangqu</groupId>
  <artifactId>cronet</artifactId>
  <version>0.0.1</version>
</dependency>
```

**gradle**

```
compile 'io.github.lizhangqu:cronet:0.0.1'
```

**create engine**

```
CronetEngine.Builder builder = new CronetEngine.Builder(context);
builder.
        enableHttpCache(CronetEngine.Builder.HTTP_CACHE_IN_MEMORY,
                100 * 1024)
        .enableHttp2(true)
        .enableQuic(true)
        .enableLegacyMode(false)
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
CronetEngine mCronetEngine = builder.build();
```

**send request**

```
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
```