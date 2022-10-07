package com.tgin.esiaauthservice.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tgin.esiaauthservice.EsiaProperties;
import lombok.RequiredArgsConstructor;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UrlHelper {

    private final EsiaProperties esiaProperties;
    private final CryptoHelper cryptoHelper;


    public String getLoginUrl() {
        String accessType = "offline"; // "online";
        String responseType = "code";

        String clientId = esiaProperties.getClientId();
        String state = cryptoHelper.generateState();
        String timestamp = cryptoHelper.generateTimestamp();
        String clientSecret = cryptoHelper.generateClientSecret(clientId, state, timestamp, esiaProperties.getScope());

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
    }

    /*
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

     */

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

        String json = HttpRequestHelper.postRequest(esiaProperties.getTokenUrl(), nvps);
        return JsonHelper.getJsonValue(json, "access_token");
    }

    public String getPersonData(String accessToken) throws IOException {

        String username = extractUsername(accessToken);
        String url = esiaProperties.getInfoUrl() + "/" + username;
        NameValuePair header = new BasicNameValuePair("Authorization", "Bearer " + accessToken);

        String result = HttpRequestHelper.getRequest(url, header);

        // адрес по которому получаем контактные данные
        String contactsUrl = esiaProperties.getInfoUrl() + "/" + username + "/ctts";
        // json со списком адресовов, каждый адрес возвращает один из контактов пользователя
        String contactsUrlUrlJson = HttpRequestHelper.getRequest(contactsUrl, header);
        // берем список адресов
        ArrayList<String> personContactsUrlList = JsonHelper.getJsonValues(contactsUrlUrlJson, "elements");
        ArrayList<String> contacts = new ArrayList<>();
        // из каждого адреса получаем контакт
        for (String personContactUrl : personContactsUrlList) {
            String contactJson = HttpRequestHelper.getRequest(personContactUrl, header);
            String contact = JsonHelper.getJsonValue(contactJson, "value");
            contacts.add(contact);
        }
        String contactsJsonStr = JsonHelper.toJsonString(contacts);
        return result + "\n\n" + contactsJsonStr;
    }

    private String timestampUrlFormat(String timestamp) {
        return timestamp
                .replace("+", "%2B")
                .replace(":", "%3A")
                .replace(" ", "+");
    }

    public String extractUsername(String accessToken) throws JsonProcessingException {
        String[] accessParts = accessToken.split("\\.");
        String content = new String(Base64.getUrlDecoder().decode(accessParts[1]), StandardCharsets.UTF_8);
        return JsonHelper.getJsonValue(content, "urn:esia:sbj_id");
    }
}
