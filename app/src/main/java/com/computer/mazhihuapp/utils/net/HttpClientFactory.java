package com.computer.mazhihuapp.utils.net;

import org.apache.http.HttpVersion;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

public class HttpClientFactory {
    private static final int MAX_CONNECTIONS = 10;

    private static final int TIMEOUT = 10 * 1000;

    private static final int SOCKET_BUFFER_SIZE = 8 * 1024;

    public static DefaultHttpClient create(boolean isHttps) {
        HttpParams params = createHttpParams();
        DefaultHttpClient httpClient = null;
        if (isHttps) {
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("http", PlainSocketFactory
                    .getSocketFactory(), 80));
            schemeRegistry.register(new Scheme("https", SSLSocketFactory
                    .getSocketFactory(), 443));
            ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(
                    params, schemeRegistry);
            httpClient = new DefaultHttpClient(cm, params);
        } else {
            httpClient = new DefaultHttpClient(params);
        }
        return httpClient;
    }

    private static HttpParams createHttpParams() {
        final HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setStaleCheckingEnabled(params, false);
        HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, TIMEOUT);
        HttpConnectionParams.setSocketBufferSize(params, SOCKET_BUFFER_SIZE);
        HttpConnectionParams.setTcpNoDelay(params, true);
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setUseExpectContinue(params, true);
        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
        HttpClientParams.setRedirecting(params, false);

        ConnManagerParams.setTimeout(params, TIMEOUT);
        ConnManagerParams.setMaxConnectionsPerRoute(params,
                new ConnPerRouteBean(MAX_CONNECTIONS));
        ConnManagerParams.setMaxTotalConnections(params, 10);
        return params;
    }

}
