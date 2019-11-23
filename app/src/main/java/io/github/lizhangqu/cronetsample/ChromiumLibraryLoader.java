package io.github.lizhangqu.cronetsample;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.chromium.net.BuildConfig;
import org.chromium.net.CronetEngine;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * native loader extension
 *
 * @author lizhangqu
 * @version V1.0
 * @since 2019-04-29 09:40
 */
public class ChromiumLibraryLoader extends CronetEngine.Builder.LibraryLoader {

    private static final String TAG = "ChromiumLibraryLoader";

    private Context context;

    public ChromiumLibraryLoader(Context context) {
        this.context = context.getApplicationContext();
    }

    @SuppressLint("UnsafeDynamicallyLoadedCode")
    @Override
    public void loadLibrary(String libName) {
        Log.w(TAG, "libName:" + libName);
        long start = System.currentTimeMillis();
        try {
            //非cronet的so调用系统方法加载
            if (!libName.contains("cronet")) {
                System.loadLibrary(libName);
                return;
            }
            //以下逻辑为cronet加载，优先加载本地，否则从远程加载
            //首先调用系统行为进行加载
            System.loadLibrary(libName);
            Log.w(TAG, "load from system");
        } catch (Throwable e) {
            //如果找不到，则从远程下载
            File dir = new File(context.getFilesDir(), "so_chromium");
            String mappedLibraryName = System.mapLibraryName(libName);
            final File destSuccessFile = new File(dir, mappedLibraryName);
            File downloadFile = new File(new File(context.getCacheDir(), "so_chromium_download"), mappedLibraryName);
            //删除历史文件
            deleteHistoryFile(dir, destSuccessFile);

            String md5 = null;
            String url = null;

            try {
                JSONObject jsonObject = new JSONObject(BuildConfig.REMOTE_JSON);
                String abi = getABI(context, getCurrentInstructionSetString());
                Log.w(TAG, "abi:" + abi);
                JSONObject armeabiV7aJSON = jsonObject.getJSONObject(abi);
                md5 = armeabiV7aJSON.optString("md5");
                url = armeabiV7aJSON.optString("url");
                Log.w(TAG, "md5:" + md5);
                Log.w(TAG, "url:" + url);
            } catch (Exception e1) {
                e1.printStackTrace();
            }

            if (md5 == null || md5.length() == 0 || url == null || url.length() == 0) {
                //如果md5或下载的url为空，则调用系统行为进行加载
                System.loadLibrary(libName);
                return;
            }

            if (!destSuccessFile.exists() || !destSuccessFile.isFile()) {
                //noinspection ResultOfMethodCallIgnored
                destSuccessFile.delete();
                download(url, md5, downloadFile, destSuccessFile);
                //如果文件不存在或不是文件，则调用系统行为进行加载
                System.loadLibrary(libName);
                return;
            }

            if (destSuccessFile.exists()) {
                //如果文件存在，则校验md5值
                String fileMD5 = getFileMD5(destSuccessFile);
                if (fileMD5 != null && fileMD5.equalsIgnoreCase(md5)) {
                    //md5值一样，则加载
                    System.load(destSuccessFile.getAbsolutePath());
                    Log.w(TAG, "load from:" + destSuccessFile);
                    return;
                }
                //md5不一样则删除
                //noinspection ResultOfMethodCallIgnored
                destSuccessFile.delete();

            }
            Log.w(TAG, "删除历史文件");
            //删除历史文件
            deleteHistoryFile(dir, destSuccessFile);
            //不存在则下载
            download(url, md5, downloadFile, destSuccessFile);
            //使用系统加载方法
            System.loadLibrary(libName);
        } finally {
            Log.w(TAG, "time:" + (System.currentTimeMillis() - start));
        }
    }

    /**
     * 获得abi，可覆写
     */
    protected String getABI(Context context, String nowABI) {
        return nowABI;
    }

    private String getCurrentInstructionSetString() {
        if (Build.VERSION.SDK_INT < 21) {
            return "armeabi-v7a";
        }
        try {
            Class<?> clazz = Class.forName("dalvik.system.VMRuntime");
            Method currentGet = clazz.getDeclaredMethod("getCurrentInstructionSet");
            String invoke = (String) currentGet.invoke(null);
            if ("arm".equals(invoke)) {
                return "armeabi-v7a";
            } else if ("arm64".equals(invoke)) {
                return "arm64-v8a";
            } else if ("x86".equals(invoke)) {
                return "x86";
            } else if ("x86_64".equals(invoke)) {
                return "x86_64";
            } else if ("mips".equals(invoke)) {
                return "mips";
            } else if ("mips64".equals(invoke)) {
                return "mips64";
            } else if ("none".equals(invoke)) {
                return "armeabi-v7a";
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "armeabi-v7a";
    }

    /**
     * 删除历史文件
     */
    private static void deleteHistoryFile(File dir, File currentFile) {
        File[] files = dir.listFiles();
        if (files != null && files.length > 0) {
            for (File f : files) {
                if (f.exists() && (currentFile == null || !f.getAbsolutePath().equals(currentFile.getAbsolutePath()))) {
                    boolean delete = f.delete();
                    Log.w(TAG, "delete file: " + f + " result: " + delete);
                    if (!delete) {
                        f.deleteOnExit();
                    }
                }
            }
        }
    }

    /**
     * 下载文件
     */
    private boolean downloadFileIfNotExist(String url, File destFile) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            inputStream = connection.getInputStream();
            if (destFile.exists()) {
                return true;
            }
            destFile.getParentFile().mkdirs();
            destFile.createNewFile();
            outputStream = new FileOutputStream(destFile);
            byte[] buffer = new byte[32768];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
                outputStream.flush();
            }
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            if (destFile.exists() && !destFile.delete()) {
                destFile.deleteOnExit();
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    static boolean download = false;
    Executor executor = Executors.newSingleThreadExecutor();

    /**
     * 下载并拷贝文件
     */
    private synchronized void download(final String url, final String md5, final File downloadTempFile, final File destSuccessFile) {
        if (download) {
            return;
        }
        download = true;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                boolean result = downloadFileIfNotExist(url, downloadTempFile);
                Log.w(TAG, "download result:" + result);
                //文件md5再次校验
                String fileMD5 = getFileMD5(downloadTempFile);
                if (md5 != null && !md5.equalsIgnoreCase(fileMD5)) {
                    boolean delete = downloadTempFile.delete();
                    if (!delete) {
                        downloadTempFile.deleteOnExit();
                    }
                    download = false;
                    return;
                }
                Log.w(TAG, "download success, copy to " + destSuccessFile);
                //下载成功拷贝文件
                copyFile(downloadTempFile, destSuccessFile);
                File parentFile = downloadTempFile.getParentFile();
                deleteHistoryFile(parentFile, null);
            }
        });

    }


    /**
     * 拷贝文件
     */
    private boolean copyFile(File source, File dest) {
        if (source == null || !source.exists() || !source.isFile() || dest == null) {
            return false;
        }
        if (source.getAbsolutePath().equals(dest.getAbsolutePath())) {
            return true;
        }
        FileInputStream is = null;
        FileOutputStream os = null;
        File parent = dest.getParentFile();
        if (parent != null && (!parent.exists())) {
            boolean mkdirs = parent.mkdirs();
            if (!mkdirs) {
                mkdirs = parent.mkdirs();
            }
        }
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest, false);

            byte[] buffer = new byte[1024 * 512];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * 获得文件md5
     */
    private static String getFileMD5(File file) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[1024 * 1024];
            int numRead = 0;
            while ((numRead = fileInputStream.read(buffer)) > 0) {
                md5.update(buffer, 0, numRead);
            }
            return String.format("%032x", new BigInteger(1, md5.digest())).toLowerCase();
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
