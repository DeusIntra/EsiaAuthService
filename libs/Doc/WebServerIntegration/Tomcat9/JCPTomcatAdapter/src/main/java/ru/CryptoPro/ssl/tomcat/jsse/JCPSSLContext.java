package ru.CryptoPro.ssl.tomcat.jsse;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

public class JCPSSLContext implements 
	org.apache.tomcat.util.net.SSLContext {
	
	private javax.net.ssl.SSLContext context;
	private KeyManager[] kms;
	private TrustManager[] tms;

	JCPSSLContext(String protocol) throws NoSuchAlgorithmException {
		this.context = javax.net.ssl.SSLContext.getInstance(protocol);
	}

	@Override
	public void init(KeyManager[] kms, TrustManager[] tms, SecureRandom sr)
	    throws KeyManagementException {
        this.kms = kms;
        this.tms = tms;
		this.context.init(kms, tms, sr);
	}

    @Override
	public void destroy() {}

    @Override
	public SSLSessionContext getServerSessionContext() {
		return this.context.getServerSessionContext();
	}

    @Override
	public SSLEngine createSSLEngine() {
		return this.context.createSSLEngine();
	}

    @Override
	public SSLServerSocketFactory getServerSocketFactory() {
		return this.context.getServerSocketFactory();
	}

    @Override
	public SSLParameters getSupportedSSLParameters() {
		return this.context.getSupportedSSLParameters();
	}

	@Override
	public X509Certificate[] getCertificateChain(String alias) {
        X509Certificate[] result = null;
        if (this.kms != null) {
            for (int i = 0; (i < this.kms.length) && (result == null); i++) {
                if ((this.kms[i] instanceof X509KeyManager)) {
                    result = ((X509KeyManager)this.kms[i]).getCertificateChain(alias);
                }
            }
        }
        return result;
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
        Set<X509Certificate> certs = new HashSet();
        if (this.tms != null) {
            for (TrustManager tm : this.tms) {
                if ((tm instanceof X509TrustManager))
                {
                    X509Certificate[] accepted = ((X509TrustManager)tm).getAcceptedIssuers();
                    if (accepted != null) {
                        for (X509Certificate c : accepted) {
                            certs.add(c);
                        }
                    }
                }
            }
        }
        return (X509Certificate[])certs.toArray(new X509Certificate[certs.size()]);
	}

}
