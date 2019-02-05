package org.chromium.net.impl;
public interface LoadState {
public static final int IDLE = 0;
public static final int WAITING_FOR_STALLED_SOCKET_POOL = 1;
public static final int WAITING_FOR_AVAILABLE_SOCKET = 2;
public static final int WAITING_FOR_DELEGATE = 3;
public static final int WAITING_FOR_CACHE = 4;
public static final int WAITING_FOR_APPCACHE = 5;
public static final int DOWNLOADING_PAC_FILE = 6;
public static final int RESOLVING_PROXY_FOR_URL = 7;
public static final int RESOLVING_HOST_IN_PAC_FILE = 8;
public static final int ESTABLISHING_PROXY_TUNNEL = 9;
public static final int RESOLVING_HOST = 10;
public static final int CONNECTING = 11;
public static final int SSL_HANDSHAKE = 12;
public static final int SENDING_REQUEST = 13;
public static final int WAITING_FOR_RESPONSE = 14;
public static final int READING_RESPONSE = 15;
}
