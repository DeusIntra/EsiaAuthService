package com.tgin.esiaauthservice.controller;

import com.tgin.esiaauthservice.EsiaProperties;
import com.tgin.esiaauthservice.helper.EsiaAuthUrlService;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.springframework.http.MediaType;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.TimeZone;

@RestController
@RequiredArgsConstructor
public class EsiaReturnController {
    private final EsiaProperties esiaProperties;
    private final EsiaAuthUrlService esiaAuthUrlService;

    @GetMapping(value = "/esia/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public String login(
            @RequestParam(name = "TimeZone", required = false) TimeZone timeZone
    ) {

        String esiaAuthUrl = esiaAuthUrlService.generateAuthCodeUrl();
        return "redirect:" + esiaAuthUrl;
    }

    @GetMapping(path = "/esia_return", produces = "text/plain")
    public String handleReturn(
            @RequestParam(name = "code", required = false) String authCode,
            @RequestParam(name = "error", required = false) String error,
            @RequestParam(name = "error_description", required = false) String errorDescription
    ) throws IOException {

        String str = "TOKEN: \n\n";

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("https://esia-portal1.test.gosuslugi.ru/aas/oauth2/te");
        HttpEntity entity = getHttpEntity(authCode, httpclient, httpPost);
        str = getEntityContent(str, entity);

        return PrintGetEsiaReturn(authCode, error, errorDescription, str);
    }

    @GetMapping(path = "/esia/logout")
    public String handleLogout() throws IOException {

        return "redirect:" + "https://esia-portal1.test.gosuslugi.ru/idp/ext/Logout?client_id=PILOT10";
    }

    private String getEntityContent(String str, HttpEntity entity) throws IOException {
        InputStream inputStream = entity.getContent();
        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader
                (inputStream, Charset.forName(StandardCharsets.UTF_8.name())))) {
            int c = 0;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
        }
        str += textBuilder.toString();
        return str;
    }

    private HttpEntity getHttpEntity(String authCode, CloseableHttpClient httpclient, HttpPost httpPost) throws IOException {
        String timestamp = esiaAuthUrlService.generateTimestamp();
        String stateNew = esiaAuthUrlService.generateState();
        String scope = "openid";
        String clientSecret = esiaAuthUrlService.generateClientSecret(esiaProperties.getClientId(), stateNew, timestamp);

        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("client_id", esiaProperties.getClientId()));
        nvps.add(new BasicNameValuePair("code", authCode));
        nvps.add(new BasicNameValuePair("grant_type", "authorization_code"));
        nvps.add(new BasicNameValuePair("client_secret", clientSecret));
        nvps.add(new BasicNameValuePair("state", stateNew));
        nvps.add(new BasicNameValuePair("redirect_uri", esiaProperties.getReturnUrl()));
        nvps.add(new BasicNameValuePair("scope", scope));
        nvps.add(new BasicNameValuePair("timestamp", timestamp));
        nvps.add(new BasicNameValuePair("token_type", "Bearer"));
        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        CloseableHttpResponse response = httpclient.execute(httpPost);
        HttpEntity entity = response.getEntity();
        return entity;
    }

    private String PrintGetEsiaReturn(String authCode, String error, String errorDescription, String token){
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

        if(token != null && token.length() > 0){
            joiner.add(token);
        }

        return joiner.toString();
    }

}
