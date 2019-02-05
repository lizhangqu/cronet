# Copyright 2016 The Chromium Authors. All rights reserved.
# Use of this source code is governed by a BSD-style license that can be
# found in the LICENSE file.

# Contains flags that can be safely shared with Cronet, and thus would be
# appropriate for third-party apps to include.

# Keep all annotation related attributes that can affect runtime
-keepattributes RuntimeVisible*Annotations
-keepattributes AnnotationDefault

# Keep the annotations, because if we don't, the ProGuard rules that use them
# will not be respected. These classes then show up in our final dex, which we
# do not want - see crbug.com/628226.
-keep @interface org.chromium.base.annotations.AccessedByNative
-keep @interface org.chromium.base.annotations.CalledByNative
-keep @interface org.chromium.base.annotations.CalledByNativeUnchecked
-keep @interface org.chromium.base.annotations.DoNotInline
-keep @interface org.chromium.base.annotations.RemovableInRelease
-keep @interface org.chromium.base.annotations.UsedByReflection

# Keeps for class level annotations.
-keep @org.chromium.base.annotations.UsedByReflection class * {}

# Keeps for method level annotations.
-keepclasseswithmembers class * {
  @org.chromium.base.annotations.AccessedByNative <fields>;
}
-keepclasseswithmembers,includedescriptorclasses class * {
  @org.chromium.base.annotations.CalledByNative <methods>;
}
-keepclasseswithmembers,includedescriptorclasses class * {
  @org.chromium.base.annotations.CalledByNativeUnchecked <methods>;
}
-keepclasseswithmembers class * {
  @org.chromium.base.annotations.UsedByReflection <methods>;
}
-keepclasseswithmembers class * {
  @org.chromium.base.annotations.UsedByReflection <fields>;
}
-keepclasseswithmembers,includedescriptorclasses class * {
  native <methods>;
}

# Remove methods annotated with this if their return value is unused.
-assumenosideeffects class ** {
  @org.chromium.base.annotations.RemovableInRelease <methods>;
}

# Never inline classes or methods with this annotation, but allow shrinking and
# obfuscation.
-keepnames,allowobfuscation @org.chromium.base.annotations.DoNotInline class * {
  *;
}
-keepclassmembernames,allowobfuscation class * {
  @org.chromium.base.annotations.DoNotInline <methods>;
}

# Keep all CREATOR fields within Parcelable that are kept.
-keepclassmembers class org.chromium.** implements android.os.Parcelable {
  public static *** CREATOR;
}

# Don't obfuscate Parcelables as they might be marshalled outside Chrome.
# If we annotated all Parcelables that get put into Bundles other than
# for saveInstanceState (e.g. PendingIntents), then we could actually keep the
# names of just those ones. For now, we'll just keep them all.
-keepnames class org.chromium.** implements android.os.Parcelable

# Keep all enum values and valueOf methods. See
# http://proguard.sourceforge.net/index.html#manual/examples.html
# for the reason for this. Also, see http://crbug.com/248037.
-keepclassmembers enum org.chromium.** {
    public static **[] values();
}
# Proguard config for apps that depend on cronet_impl_native_java.jar.

# This constructor is called using the reflection from Cronet API (cronet_api.jar).
-keep class org.chromium.net.impl.NativeCronetProvider {
    public <init>(android.content.Context);
}

# Suppress unnecessary warnings.
-dontnote org.chromium.net.ProxyChangeListener$ProxyReceiver
-dontnote org.chromium.net.AndroidKeyStore
# Needs 'void setTextAppearance(int)' (API level 23).
-dontwarn org.chromium.base.ApiCompatibilityUtils
# Needs 'boolean onSearchRequested(android.view.SearchEvent)' (API level 23).
-dontwarn org.chromium.base.WindowCallbackWrapper

# Generated for chrome apk and not included into cronet.
-dontwarn org.chromium.base.library_loader.NativeLibraries
-dontwarn org.chromium.base.multidex.ChromiumMultiDexInstaller
-dontwarn org.chromium.base.metrics.CachedMetrics
-dontwarn org.chromium.base.library_loader.LibraryLoader
-dontwarn org.chromium.base.SysUtils

# Objects of this type are passed around by native code, but the class
# is never used directly by native code. Since the class is not loaded, it does
# not need to be preserved as an entry point.
-dontnote org.chromium.net.UrlRequest$ResponseHeadersMap
# https://android.googlesource.com/platform/sdk/+/marshmallow-mr1-release/files/proguard-android.txt#54
-dontwarn android.support.**

# This class should be explicitly kept to avoid failure if
# class/merging/horizontal proguard optimization is enabled.
-keep class org.chromium.base.CollectionUtil

