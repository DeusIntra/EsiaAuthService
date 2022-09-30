/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.cryptopro.jtlsconnector;

import com.sun.enterprise.security.ssl.GlassfishServerSocketFactory;
import java.io.IOException;

import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

/**
 *
 * @author Yevgeniy
 */
public class JTLSProxyServerSocketFactory extends GlassfishServerSocketFactory {

    private static final String CONFIG_FILE = "jtls.properties";
    
    private static final String TRUST_STORE_PROVIDER = "trustStoreProvider";
    private static final String TRUST_STORE_TYPE = "trustStoreType";
    private static final String TRUST_STORE = "trustStore";
    private static final String TRUST_STORE_PASSWORD = "trustStorePassword";
    
    private static final String KEY_STORE_PROVIDER = "keyStoreProvider";
    private static final String KEY_STORE_TYPE = "keyStoreType";
    private static final String KEY_STORE_PASSWORD = "keyStorePassword";
    private static final String KEY_STORE = "keyStore";
    private static final String KEY_STORE_ALIAS = "keyStoreAlias";
    
    private static final String PROTOCOL = "protocol";
    private static final String PROTOCOLS = "protocols";
    private static final String ALGORITHM = "algorithm";
    private static final String CLIENT_AUTH = "clientAuth";
    private static final String CIPHERS = "ciphers";
    
    private final String[] PROPERTIES;
    
    public JTLSProxyServerSocketFactory() {
        
        super();
        logD("JTLSProxyServerSocketFactory()");
        
        this.PROPERTIES = new String[] {
            TRUST_STORE_PROVIDER,
            TRUST_STORE_TYPE,
            TRUST_STORE,
            TRUST_STORE_PASSWORD,
            KEY_STORE_PROVIDER,
            KEY_STORE_TYPE,
            KEY_STORE_PASSWORD,
            KEY_STORE,
            KEY_STORE_ALIAS,
            PROTOCOL,
            PROTOCOLS,
            ALGORITHM,
            CLIENT_AUTH,
            CIPHERS
        };

        Properties config = new Properties();
        try {
            logD("Loading " + CONFIG_FILE);
            config.load(JTLSProxyServerSocketFactory.class.getResourceAsStream(CONFIG_FILE));
            for (String prop : PROPERTIES) {
                setAttribute(prop, config.get(prop));
            }
        } catch (IOException ex) {
            logE(null, ex);
        }

        Set<String> keys = attributes.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            logA(key, attributes.get(key));
        }
            
    }
    
    private void logD(String message) {
        logger.log(Level.INFO, message);
    }
    
    private void logE(String message, Exception e) {
        logger.log(Level.SEVERE, message, e);
    }
    
    private void logA(String key, Object value) {
        logger.log(Level.INFO, "{0}: {1}", new Object[]{key, value});
    }
 
}
