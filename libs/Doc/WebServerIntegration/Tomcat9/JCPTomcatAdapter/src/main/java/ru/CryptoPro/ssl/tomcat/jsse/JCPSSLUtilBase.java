package ru.CryptoPro.ssl.tomcat.jsse;

import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.juli.logging.Log;

import org.apache.tomcat.util.net.SSLContext;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.apache.tomcat.util.net.SSLUtil;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public abstract class JCPSSLUtilBase implements SSLUtil {

	protected final SSLHostConfig sslHostConfig;
	protected final SSLHostConfigCertificate certificate;
	private final String[] enabledProtocols;
	private final String[] enabledCiphers;

    protected JCPSSLUtilBase(SSLHostConfigCertificate certificate) {
        this(certificate, true);
    }

	protected JCPSSLUtilBase(SSLHostConfigCertificate certificate, boolean warnTls13) {
		
		this.certificate = certificate;
        this.sslHostConfig = certificate.getSSLHostConfig();

	    Set<String> configuredProtocols = sslHostConfig.getProtocols();
	    Set<String> implementedProtocols = getImplementedProtocols();

        if ((!implementedProtocols.contains("TLSv1.3"))) {
            configuredProtocols.remove("TLSv1.3");
        }

        if ((!implementedProtocols.contains("SSLv2Hello"))) {
            configuredProtocols.remove("SSLv2Hello");
        }
	    
	    List<String> enabledProtocols = getEnabled("protocols", getLog(),
            warnTls13, configuredProtocols, implementedProtocols);
	    
	    this.enabledProtocols = enabledProtocols.toArray(
	    	new String[enabledProtocols.size()]);
	    
	    List<String> configuredCiphers = getJsseCipherNames(); // Используем собственный список
	    Set<String> implementedCiphers = getImplementedCiphers();
	    
	    List<String> enabledCiphers = getEnabled("ciphers", getLog(), 
	    	false, configuredCiphers, implementedCiphers);
	    
	    this.enabledCiphers = enabledCiphers.toArray(
	    	new String[enabledCiphers.size()]);
		
	}
	
	static List<String> getJsseCipherNames() {
		List<String> cipherSuites = new ArrayList<String>();
		cipherSuites.add("TLS_CIPHER_2012");
		cipherSuites.add("TLS_CIPHER_2001");
		return cipherSuites;
	}
	
	static <T> List<T> getEnabled(String name, Log log, boolean warnOnSkip, 
	Collection<T> configured, Collection<T> implemented) {
		
	    List<T> enabled = new ArrayList();
	    if (implemented.size() == 0) {
	      enabled.addAll(configured);
	    }
	    else {

	        enabled.addAll(configured);
	        enabled.retainAll(implemented);

	        if (enabled.isEmpty()) {
	          throw new IllegalArgumentException("None supported: " + name);
	        }

	        if (((log.isDebugEnabled()) || (warnOnSkip)) &&
	          (enabled.size() != configured.size())) {
	          List<T> skipped = new ArrayList();
	          skipped.addAll(configured);
	          skipped.removeAll(enabled);
	          String msg = "Skipped: " + name;
	          if (warnOnSkip) {
	            log.warn(msg);
	          } else {
	            log.debug(msg);
	          }
	        }
	    }

	    return enabled;
	}

    @Override
    public void configureSessionContext(SSLSessionContext sslSessionContext) {
        if (this.sslHostConfig.getSessionCacheSize() >= 0) {
            sslSessionContext.setSessionCacheSize(this.sslHostConfig.getSessionCacheSize());
        }
        if (this.sslHostConfig.getSessionTimeout() >= 0) {
            sslSessionContext.setSessionTimeout(this.sslHostConfig.getSessionTimeout());
        }
    }

    @Override
    public SSLContext createSSLContext(List<String> negotiableProtocols)
        throws Exception {
        SSLContext sslContext = createSSLContextInternal(negotiableProtocols);
        sslContext.init(getKeyManagers(), getTrustManagers(), null);

        SSLSessionContext sessionContext = sslContext.getServerSessionContext();
        if (sessionContext != null) {
            configureSessionContext(sessionContext);
        }
        return sslContext;
    }

    @Override
    public KeyManager[] getKeyManagers() throws Exception { // Сокращен

        KeyStore ks = this.certificate.getCertificateKeystore();
        String keyPass = this.certificate.getCertificateKeystorePassword();

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("GostX509"); // Прямо указан алгоритм из cpSSL
        kmf.init(ks, keyPass.toCharArray());

        return kmf.getKeyManagers();

    }

    @Override
    public TrustManager[] getTrustManagers() throws Exception { // Сокращен

        KeyStore trustStore = this.sslHostConfig.getTruststore();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("GostX509"); // Прямо указан алгоритм из cpSSL

        tmf.init(trustStore);
        return tmf.getTrustManagers();

    }

    @Override
    public String[] getEnabledProtocols() {
	    return this.enabledProtocols;
	}

    @Override
    public String[] getEnabledCiphers() {
	    return this.enabledCiphers;
	  }
	  
    protected abstract Set<String> getImplementedProtocols();
	  
    protected abstract Set<String> getImplementedCiphers();
	  
    protected abstract Log getLog();

    protected abstract boolean isTls13RenegAuthAvailable();

    protected abstract SSLContext createSSLContextInternal(List<String> paramList)
        throws Exception;

}
