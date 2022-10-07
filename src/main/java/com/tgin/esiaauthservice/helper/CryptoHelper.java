package com.tgin.esiaauthservice.helper;

import com.tgin.esiaauthservice.crypto.CryptoProSigner;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CryptoHelper {
    private static final Logger logger = LoggerFactory.getLogger(CryptoHelper.class);

    private final CryptoProSigner cryptoProSigner;

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss Z")
            .withZone(ZoneId.systemDefault());

    public String generateClientSecret(String clientId, String state, String timestamp, String scope) {
        String clientSecretUnsigned = String.join("", scope, timestamp, clientId, state);

        byte[] signedClientSecretBytes = cryptoProSigner.sign(clientSecretUnsigned);
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
