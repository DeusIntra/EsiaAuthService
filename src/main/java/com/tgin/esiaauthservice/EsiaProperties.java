package com.tgin.esiaauthservice;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "esia")
@ConstructorBinding
@Data
public class EsiaProperties {
    private final String clientId;
    private final String scope;

    private final String authCodeUrl;
    private final String tokenUrl;
    private final String infoUrl;
    private final String logoutUrl;

    private final String loginReturnUrl;
    private final String logoutReturnUrl;
    private final String keystoreAlias;
    private final String privateKeyPassword;
}
