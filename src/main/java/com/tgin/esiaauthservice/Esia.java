package com.tgin.esiaauthservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Map;

public class Esia {


/*
    private void lol() {
        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        OAuthJSONAccessTokenResponse oauthResponse = oAuthClient
                .accessToken(oAuthClientRequest, OAuth.HttpMethod.POST, OAuthJSONAccessTokenResponse.class);
        String[] accessParts = oauthResponse.getAccessToken().split("\\.");
        ObjectMapper mapper = new ObjectMapper();
        HttpClient client = HttpClientBuilder.create().build();
        Map<String, String> info = mapper.readValue(new String(Base64.getUrlDecoder().decode(accessParts[1]), "UTF-8"),
                new TypeReference<Map<String, String>>() {
                });
        String username = info.get("urn:esia:sbj_id");
        Map<String, Object> userInfo = getUserInfo(client, mapper, username, oauthResponse);
    }*/
}
