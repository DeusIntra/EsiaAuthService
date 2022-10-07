package com.tgin.esiaauthservice.helper;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class HttpRequestHelper {

    private static final CloseableHttpClient httpclient = HttpClients.createDefault();

    public static String postRequest(String url) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        CloseableHttpResponse response = httpclient.execute(httpPost);
        HttpEntity entity = response.getEntity();
        return getEntityContent(entity);
    }

    public static String postRequest(String url, List<NameValuePair> entities) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new UrlEncodedFormEntity(entities));
        CloseableHttpResponse response = httpclient.execute(httpPost);
        HttpEntity entity = response.getEntity();
        return getEntityContent(entity);
    }

    public static String getRequest(String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = httpclient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        return getEntityContent(entity);
    }

    public static String getRequest(String url, String headerName, String headerValue) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader(headerName, headerValue);
        CloseableHttpResponse response = httpclient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        return getEntityContent(entity);
    }

    public static String getRequest(String url, NameValuePair header) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader(header.getName(), header.getValue());
        CloseableHttpResponse response = httpclient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        return getEntityContent(entity);
    }

    public static String getRequest(String url, List<NameValuePair> headers) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        for (NameValuePair header : headers) {
            httpGet.addHeader(header.getName(), header.getValue());
        }
        CloseableHttpResponse response = httpclient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        return getEntityContent(entity);
    }

    private static String getEntityContent(HttpEntity entity) throws IOException {
        InputStream inputStream = entity.getContent();
        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader
                (inputStream, Charset.forName(StandardCharsets.UTF_8.name())))) {
            int c;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
        }
        return textBuilder.toString();
    }
}
