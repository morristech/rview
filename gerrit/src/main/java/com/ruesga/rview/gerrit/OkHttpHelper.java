/*
 * Copyright (C) 2016 Jorge Ruesga
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ruesga.rview.gerrit;

import android.annotation.SuppressLint;
import android.net.TrafficStats;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public class OkHttpHelper {

    // https://github.com/square/okhttp/blob/master/okhttp-tests/src/test/java/okhttp3/DelegatingSocketFactory.java
    private static class DelegatingSocketFactory extends SocketFactory {
        private final javax.net.SocketFactory mDelegate;

        private DelegatingSocketFactory(javax.net.SocketFactory delegate) {
            this.mDelegate = delegate;
        }

        @Override public Socket createSocket() throws IOException {
            Socket socket = mDelegate.createSocket();
            return configureSocket(socket);
        }

        @Override public Socket createSocket(String host, int port) throws IOException {
            Socket socket = mDelegate.createSocket(host, port);
            return configureSocket(socket);
        }

        @Override public Socket createSocket(String host, int port, InetAddress localAddress,
                int localPort) throws IOException {
            Socket socket = mDelegate.createSocket(host, port, localAddress, localPort);
            return configureSocket(socket);
        }

        @Override public Socket createSocket(InetAddress host, int port) throws IOException {
            Socket socket = mDelegate.createSocket(host, port);
            return configureSocket(socket);
        }

        @Override public Socket createSocket(InetAddress host, int port, InetAddress localAddress,
                int localPort) throws IOException {
            Socket socket = mDelegate.createSocket(host, port, localAddress, localPort);
            return configureSocket(socket);
        }

        private Socket configureSocket(Socket socket) throws IOException {
            try {
                TrafficStats.setThreadStatsTag(
                        Math.max(1, Math.min(0xFFFFFEFF, Thread.currentThread().hashCode())));
                TrafficStats.tagSocket(socket);
            } catch (Throwable cause) {
                // Ignore for testing
            }
            return socket;
        }
    }

    @SuppressLint("TrustAllX509TrustManager")
    private static final X509TrustManager TRUST_ALL_CERTS = new X509TrustManager() {
        @Override
        public void checkClientTrusted(
                X509Certificate[] x509Certificates, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(
                X509Certificate[] x509Certificates, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }
    };

    private static SSLSocketFactory sSSLSocketFactory;
    private static DelegatingSocketFactory sDelegatingSocketFactory;

    public static OkHttpClient.Builder getSafeClientBuilder() {
        if (sDelegatingSocketFactory == null) {
            sDelegatingSocketFactory = new DelegatingSocketFactory(SocketFactory.getDefault());
        }
        return new OkHttpClient.Builder().socketFactory(sDelegatingSocketFactory);
    }

    @SuppressLint("BadHostnameVerifier")
    static OkHttpClient.Builder getUnsafeClientBuilder() {
        OkHttpClient.Builder builder = getSafeClientBuilder();
        try {
            if (sSSLSocketFactory == null) {
                final SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, new X509TrustManager[]{TRUST_ALL_CERTS}, null);
                sSSLSocketFactory = sslContext.getSocketFactory();
            }

            builder.sslSocketFactory(sSSLSocketFactory, TRUST_ALL_CERTS);
            builder.hostnameVerifier((hostname, session) -> hostname != null);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            // Ignore
        }
        return builder;
    }

}
