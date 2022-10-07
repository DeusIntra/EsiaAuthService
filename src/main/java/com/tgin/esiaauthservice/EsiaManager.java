package com.tgin.esiaauthservice;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.tgin.esiaauthservice.helper.CryptoHelper;
import com.tgin.esiaauthservice.helper.HttpRequestHelper;
import com.tgin.esiaauthservice.helper.JsonHelper;

import lombok.RequiredArgsConstructor;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EsiaManager {

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

        String redirectUrlEncoded = urlFormat(esiaProperties.getReturnUrl());

        String url = esiaProperties.getAuthCodeUrl() +
                "?client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&scope=" + esiaProperties.getScope() +
                "&response_type=" + responseType +
                "&state=" + state +
                "&access_type=" + accessType +
                "&timestamp=" + timestampUrlEncoded +
                "&redirect_uri=" + redirectUrlEncoded;

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

        String personInfoJsonStr = getPersonInfo(username, accessToken);

        ArrayList<String> contactsList = getContacts(username, accessToken);
        String contactsJsonStr = JsonHelper.toJsonString(contactsList);

        return personInfoJsonStr + "\n\n" + contactsJsonStr;
    }

    private String getPersonInfo(String username, String accessToken) throws IOException {
        NameValuePair header = new BasicNameValuePair("Authorization", "Bearer " + accessToken);
        String url = esiaProperties.getInfoUrl() + "/" + username;
        return HttpRequestHelper.getRequest(url, header);
    }

    private ArrayList<String> getContacts(String username, String accessToken) throws IOException {
        NameValuePair header = new BasicNameValuePair("Authorization", "Bearer " + accessToken);
        // адрес для получения всех контактных данных пользователя
        String contactsUrl = esiaProperties.getInfoUrl() + "/" + username + "/ctts";
        // json со списком адресовов, каждый адрес возвращает один из контактов пользователя
        String contactsUrlUrlJson = HttpRequestHelper.getRequest(contactsUrl, header);
        // список адресов
        ArrayList<String> personContactsUrlList = JsonHelper.getJsonValues(contactsUrlUrlJson, "elements");
        // из каждого адреса получаем контакт
        ArrayList<String> contacts = new ArrayList<>();
        for (String personContactUrl : personContactsUrlList) {
            String contactJson = HttpRequestHelper.getRequest(personContactUrl, header);
            String contact = JsonHelper.getJsonValue(contactJson, "value");
            contacts.add(contact);
        }
        return contacts;
    }

    private String timestampUrlFormat(String timestamp) {
        return timestamp
                .replace("+", "%2B")
                .replace(":", "%3A")
                .replace(" ", "+");
    }

    private String urlFormat(String url) {
        return url
                .replace(":", "%3A")
                .replace("/", "%2F");
    }

    private String extractUsername(String accessToken) throws JsonProcessingException {
        String[] accessParts = accessToken.split("\\.");
        String content = new String(Base64.getUrlDecoder().decode(accessParts[1]), StandardCharsets.UTF_8);
        return JsonHelper.getJsonValue(content, "urn:esia:sbj_id");
    }
}
