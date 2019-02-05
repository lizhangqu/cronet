// Copyright 2014 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.net.impl;

// Version based on chrome/VERSION.
public class ImplVersion {
    private static final String CRONET_VERSION = "73.0.3653.0";
    private static final int API_LEVEL = 13;
    private static final String LAST_CHANGE = "3611196465ac1f3cb0960bd18d60d827a96ec9e1-refs/heads/master@{#618915}";

   /**
    * Private constructor. All members of this class should be static.
    */
    private ImplVersion() {}

    public static String getCronetVersionWithLastChange() {
        return CRONET_VERSION + "@" + LAST_CHANGE.substring(0, 8);
    }

    public static int getApiLevel() {
        return API_LEVEL;
    }

    public static String getCronetVersion() {
        return CRONET_VERSION;
    }

    public static String getLastChange() {
        return LAST_CHANGE;
    }
}
