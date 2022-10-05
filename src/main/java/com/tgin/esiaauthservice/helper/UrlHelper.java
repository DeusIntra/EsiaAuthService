package com.tgin.esiaauthservice.helper;

import com.tgin.esiaauthservice.EsiaProperties;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@Service
@RequiredArgsConstructor
public class UrlHelper {

    private final EsiaProperties esiaProperties;
    private final CryptoHelper cryptoHelper;
    private final String personDataUrl = "https://esia-portal1.test.gosuslugi.ru/rs/prns"; // pg 88
    private final String authCoderUrl = "https://esia-portal1.test.gosuslugi.ru/aas/oauth2/ac"; // pg 134
    private final String accessMarkerUrl = "https://esia-portal1.test.gosuslugi.ru/aas/oauth2/te"; // pg 152
    private final String logoutUrl = "https://esia-portal1.test.gosuslugi.ru/idp/ext/Logout";

    public String secretCached;
    public String codeCached;


    public String getLoginUrl() {
        String accessType = "offline"; // "online";
        String scope = "openid"; // fullname // "fullname+email"
        String responseType = "code";

        try {
            String clientId = esiaProperties.getClientId();
            String state = cryptoHelper.generateState();
            String timestamp = cryptoHelper.generateTimestamp();
            String clientSecret = cryptoHelper.generateClientSecret(clientId, state, timestamp, scope);
            secretCached = clientSecret;

            String timestampUrlEncoded = timestamp
                    .replace("+", "%2B")
                    .replace(":", "%3A")
                    .replace(" ", "+");

            String redirectUrlEncoded = esiaProperties.getReturnUrl()
                    .replace(":", "%3A")
                    .replace("/", "%2F");

            UriComponentsBuilder accessTokenRequestBuilder = UriComponentsBuilder.fromHttpUrl(esiaProperties.getAuthCodeUrl())
                    .queryParam("client_id", clientId)
                    .queryParam("client_secret", clientSecret)
                    .queryParam("scope", scope)
                    .queryParam("response_type", responseType)
                    .queryParam("state", state)
                    .queryParam("access_type", accessType);

            String url = accessTokenRequestBuilder.toUriString();
            url += "&timestamp=" + timestampUrlEncoded;
            url += "&redirect_uri=" + redirectUrlEncoded;

            Logger logger = LoggerFactory.getLogger(CryptoHelper.class);
            logger.debug("generated url: {}", url);

            return url;

        } catch (Exception e) {
            throw new EsiaAuthUrlServiceException("Unable to generate access token url", e);
        }
    }

    public String isLoggedIn(String client_secret) throws IOException { // pg 152

        String timestamp = cryptoHelper.generateTimestamp();
        String scope = "openid";
        String response_type = "code";
        String newState = cryptoHelper.generateState();
        String client_id = esiaProperties.getClientId();

        if (client_secret == null) client_secret = secretCached;
/*
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("timestamp", timestamp));
        nvps.add(new BasicNameValuePair("scope", scope));
        nvps.add(new BasicNameValuePair("response_type", response_type));
        nvps.add(new BasicNameValuePair("redirect_uri", esiaProperties.getReturnUrl()));
        nvps.add(new BasicNameValuePair("state", newState));
        nvps.add(new BasicNameValuePair("prompt", "none"));
        nvps.add(new BasicNameValuePair("client_id", client_id));
        nvps.add(new BasicNameValuePair("client_secret", client_secret));
        nvps.add(new BasicNameValuePair("code", codeCached));

 */

        String url = authCoderUrl +
                "?timestamp=" + timestamp +
                "&scope=" + scope +
                "&response_type=" + response_type +
                "&redirect_uri=" + esiaProperties.getReturnUrl() +
                "&state=" + newState +
                "&prompt=" + "none" +
                "&client_id=" + client_id +
                "&client_secret=" + client_secret +
                "&code=" + codeCached;

        return getRequest(url);
    }

    public String getLogoutUrl() {
        return logoutUrl + "?client_id=" + esiaProperties.getClientId();
    }

    public String getIdToken() {
        return "";
    }

    public String getAccessToken(String authCode) throws IOException {

        String clientId = esiaProperties.getClientId();
        String state = cryptoHelper.generateState();
        String timestamp = cryptoHelper.generateTimestamp();
        String scope = "openid";
        String secret = cryptoHelper.generateClientSecret(clientId, state, timestamp, scope);

        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("client_id", clientId));
        nvps.add(new BasicNameValuePair("code", authCode));
        nvps.add(new BasicNameValuePair("grant_type", "authorization_code"));
        nvps.add(new BasicNameValuePair("client_secret", secret));
        nvps.add(new BasicNameValuePair("state", state));
        nvps.add(new BasicNameValuePair("redirect_uri", esiaProperties.getReturnUrl()));
        nvps.add(new BasicNameValuePair("scope", scope));
        nvps.add(new BasicNameValuePair("timestamp", timestamp));
        nvps.add(new BasicNameValuePair("token_type", "Bearer"));

        return postRequest(accessMarkerUrl, nvps);
    }

    public String handleReturn(String authCode, String state, String error, String errorDescription) throws IOException {

        String timestamp = cryptoHelper.generateTimestamp();
        String newState = cryptoHelper.generateState();
        String scope = "openid";
        String clientSecret = cryptoHelper.generateClientSecret(esiaProperties.getClientId(), state, timestamp, scope);

        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("client_id", esiaProperties.getClientId()));
        nvps.add(new BasicNameValuePair("code", authCode));
        nvps.add(new BasicNameValuePair("grant_type", "authorization_code"));
        nvps.add(new BasicNameValuePair("client_secret", clientSecret));
        nvps.add(new BasicNameValuePair("state", newState));
        nvps.add(new BasicNameValuePair("redirect_uri", esiaProperties.getReturnUrl()));
        nvps.add(new BasicNameValuePair("scope", scope));
        nvps.add(new BasicNameValuePair("timestamp", timestamp));
        nvps.add(new BasicNameValuePair("token_type", "Bearer"));

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(authCoderUrl);
        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        CloseableHttpResponse response = httpclient.execute(httpPost);
        HttpEntity entity = response.getEntity();

        String str = ""; //= "TOKEN: \n\n";
        str += "\n\nsecret:" + clientSecret + "\n\n";
        str = getEntityContent(entity);

        StringJoiner joiner = new StringJoiner("\n\n");
        if (StringUtils.hasText(authCode)) {
            joiner.add("сode=" + authCode);

            String[] jwtParts = authCode.split("\\.");
            if (jwtParts.length == 3) {
                joiner.add("try base64decode jwt");
                joiner.add(new String(Base64Utils.decodeFromUrlSafeString(jwtParts[0]), StandardCharsets.UTF_8));
                joiner.add(new String(Base64Utils.decodeFromUrlSafeString(jwtParts[1]), StandardCharsets.UTF_8));
            }
        }
        if (StringUtils.hasText(error)) {
            joiner.add("error=" + error);
        }
        if (StringUtils.hasText(errorDescription)) {
            joiner.add("error_description=" + errorDescription);
        }

        if (joiner.length() == 0) {
            joiner.add("No expected parameters received.");
        }

        if(str != null && str.length() > 0) {
            joiner.add(str);
        }

        return joiner.toString();
    }

    private String getEntityContent(HttpEntity entity) throws IOException {
        InputStream inputStream = entity.getContent();
        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader
                (inputStream, Charset.forName(StandardCharsets.UTF_8.name())))) {
            int c = 0;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
        }
        return textBuilder.toString();
    }

    private String postRequest(String url, List<NameValuePair> nvps) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        String str = httpPost.toString();
        CloseableHttpResponse response = httpclient.execute(httpPost);
        HttpEntity entity = response.getEntity();
        return getEntityContent(entity);
    }

    private String getRequest(String url) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        //httpGet.setEntity(new UrlEncodedFormEntity(nvps));
        CloseableHttpResponse response = httpclient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        return getEntityContent(entity);
    }


    /*
    public String getAccessMarker(String authCode, String client_secret) throws IOException {

        String client_id = esiaProperties.getClientId();
        String code = authCode;
        String grant_type = "authorization_code"; // pg 137 ??
        String state = esiaAuthUrlService.generateState();
        String redirect_uri = esiaProperties.getReturnUrl();
        String scope = "openid";
        String timestamp = esiaAuthUrlService.generateTimestamp();
        String token_type = "Bearer";

        String clientSecret = esiaAuthUrlService.generateClientSecret(client_id, state, timestamp);

        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("client_id", client_id));
        nvps.add(new BasicNameValuePair("code", code));
        nvps.add(new BasicNameValuePair("grant_type", grant_type));
        nvps.add(new BasicNameValuePair("client_secret", client_secret));
        nvps.add(new BasicNameValuePair("state", state));
        nvps.add(new BasicNameValuePair("redirect_uri", redirect_uri));
        nvps.add(new BasicNameValuePair("scope", scope));
        nvps.add(new BasicNameValuePair("timestamp", timestamp));
        nvps.add(new BasicNameValuePair("token_type", token_type));

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(accessMarkerUrl);
        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        CloseableHttpResponse response = httpclient.execute(httpPost);
        HttpEntity entity = response.getEntity();

        return getEntityContent(entity);
    }

     */



}
