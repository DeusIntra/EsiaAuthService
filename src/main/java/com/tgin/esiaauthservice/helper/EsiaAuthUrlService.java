package com.tgin.esiaauthservice.helper;

public interface EsiaAuthUrlService {
    String generateAuthCodeUrl();
    String generateClientSecret(String clientId, String state, String timestamp);
    String generateState();
    String generateTimestamp();
}
