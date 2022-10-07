package com.tgin.esiaauthservice.helper;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UrlHelper {

    private final EsiaProperties esiaProperties;
    private final CryptoHelper cryptoHelper;
    //private final String personDataUrl = esiaProperties.getInfoUrl(); //"https://esia-portal1.test.gosuslugi.ru/rs/prns"; // pg 88
    //private final String accessMarkerUrl = esiaProperties.getTokenUrl(); //"https://esia-portal1.test.gosuslugi.ru/aas/oauth2/te"; // pg 134
    //private final String authCoderUrl = esiaProperties.getAuthCodeUrl(); //"https://esia-portal1.test.gosuslugi.ru/aas/oauth2/ac"; // pg 152

    //private final String logoutUrl = esiaProperties.getLogoutUrl(); //"https://esia-portal1.test.gosuslugi.ru/idp/ext/Logout";
    //private final String scope = esiaProperties.getScope(); //"openid fullname gender birthdate birthplace citizenship id_doc snils email mobile inn";
    public String secretCached;
    public String codeCached;


    public String getLoginUrl() {
        String accessType = "offline"; // "online";
        String responseType = "code";

        try {
            String clientId = esiaProperties.getClientId();
            String state = cryptoHelper.generateState();
            String timestamp = cryptoHelper.generateTimestamp();
            String clientSecret = cryptoHelper.generateClientSecret(clientId, state, timestamp, esiaProperties.getScope());
            secretCached = clientSecret;

            String timestampUrlEncoded = timestampUrlFormat(timestamp);

            String redirectUrlEncoded = esiaProperties.getReturnUrl()
                    .replace(":", "%3A")
                    .replace("/", "%2F");

            UriComponentsBuilder accessTokenRequestBuilder = UriComponentsBuilder.fromHttpUrl(esiaProperties.getAuthCodeUrl())
                    .queryParam("client_id", clientId)
                    .queryParam("client_secret", clientSecret)
                    .queryParam("scope", esiaProperties.getScope())
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

        String timestamp = timestampUrlFormat(cryptoHelper.generateTimestamp());
        String response_type = "code";
        String newState = cryptoHelper.generateState();
        String client_id = esiaProperties.getClientId();

        if (client_secret == null) client_secret = secretCached;

        String url = esiaProperties.getAuthCodeUrl() +
                "?timestamp=" + timestamp +
                "&scope=" + esiaProperties.getScope() +
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
        return esiaProperties.getLogoutUrl() + "?client_id=" + esiaProperties.getClientId();
    }

    /*
    public String getIdToken(String authCode) throws IOException {
        String clientId = esiaProperties.getClientId();
        String state = cryptoHelper.generateState();
        String timestamp = cryptoHelper.generateTimestamp(); //timestampUrlFormat(cryptoHelper.generateTimestamp());
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

        String json = postRequest(accessMarkerUrl, nvps);
        System.out.println("\n\n"+json+"\n\n");
        return json;
    }

     */

    public String getAccessToken(String authCode) throws IOException {

        String clientId = esiaProperties.getClientId();
        String state = cryptoHelper.generateState();
        String timestamp = cryptoHelper.generateTimestamp();
        String secret = cryptoHelper.generateClientSecret(clientId, state, timestamp, esiaProperties.getScope());

        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("client_id", clientId));
        nvps.add(new BasicNameValuePair("code", authCode));
        nvps.add(new BasicNameValuePair("grant_type", "authorization_code"));
        nvps.add(new BasicNameValuePair("client_secret", secret));
        nvps.add(new BasicNameValuePair("state", state));
        nvps.add(new BasicNameValuePair("redirect_uri", esiaProperties.getReturnUrl()));
        nvps.add(new BasicNameValuePair("scope", esiaProperties.getScope()));
        nvps.add(new BasicNameValuePair("timestamp", timestamp));
        nvps.add(new BasicNameValuePair("token_type", "Bearer"));

        String json = postRequest(esiaProperties.getTokenUrl(), nvps);
        JsonNode node = cryptoHelper.parseJson(json);
        return node.get("access_token").asText();
    }

    public String getPersonData(String accessToken) throws IOException {
        String username = cryptoHelper.extractUsername(accessToken);
        String url = esiaProperties.getInfoUrl() + "/" + username;
        String result = getRequest(url, accessToken) + "\n\n";

        String contactUrl = esiaProperties.getInfoUrl() + "/" + username + "/ctts";
        String emailUrlJson = getRequest(contactUrl, accessToken);
        String emailUrl = cryptoHelper.getJsonValue(emailUrlJson, "elements", 0);
        String emailJson = getRequest(emailUrl, accessToken);
        String email = cryptoHelper.getJsonValue(emailJson, "value");
        return result + email;
    }

    private String timestampUrlFormat(String timestamp) {
        return timestamp
                .replace("+", "%2B")
                .replace(":", "%3A")
                .replace(" ", "+");
    }

    private String getEntityContent(HttpEntity entity) throws IOException {
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

    private String postRequest(String url, List<NameValuePair> nvps) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        CloseableHttpResponse response = httpclient.execute(httpPost);
        HttpEntity entity = response.getEntity();
        return getEntityContent(entity);
    }

    private String getRequest(String url) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = httpclient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        return getEntityContent(entity);
    }

    private String getRequest(String url, String accessToken) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Authorization", "Bearer " + accessToken);
        CloseableHttpResponse response = httpclient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        return getEntityContent(entity);
    }
}
