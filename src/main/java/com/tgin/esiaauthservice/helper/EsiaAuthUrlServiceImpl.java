package com.tgin.esiaauthservice.helper;

import com.tgin.esiaauthservice.EsiaProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class EsiaAuthUrlServiceImpl implements EsiaAuthUrlService {
    private static final Logger logger = LoggerFactory.getLogger(EsiaAuthUrlServiceImpl.class);

    private final CryptoSigner cryptoSigner;
    private final EsiaProperties esiaProperties;

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss Z")
            .withZone(ZoneId.systemDefault());

    private final String accessType = "offline"; // "online";
    //private final String scope = "fullname"; // "fullname+email"
    private final String scope = "openid"; // "fullname+email"
    private final String responseType = "code";


    @Override
    public String generateAuthCodeUrl() {
        try {
            String clientId = esiaProperties.getClientId();
            String state = generateState();
            String timestamp = generateTimestamp();
            String clientSecret = generateClientSecret(clientId, state, timestamp);

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

            logger.debug("generated url: {}", url);

            return url;
        } catch (Exception e) {
            throw new EsiaAuthUrlServiceException("Unable to generate access token url", e);
        }
    }

    public String generateClientSecret(String clientId, String state, String timestamp) {
        String clientSecretUnsigned = String.join("", scope, timestamp, clientId, state);

        byte[] signedClientSecretBytes = cryptoSigner.sign(clientSecretUnsigned);
        String clientSecret = Base64.getEncoder().encodeToString(signedClientSecretBytes);
        String clientSecretUrlEncoded = clientSecret.replace("+", "-")
                .replace("/", "_")
                .replace("=", "");

        logger.debug("clientSecretUnsigned: {}", clientSecretUnsigned);
        return clientSecretUrlEncoded;
    }

    public String generateState() {
        return UUID.randomUUID().toString();
    }

    public String generateTimestamp() {
        return dateTimeFormatter.format(Instant.now());
    }

}
