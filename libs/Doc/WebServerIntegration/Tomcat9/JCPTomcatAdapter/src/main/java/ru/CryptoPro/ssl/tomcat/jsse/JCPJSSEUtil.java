package ru.CryptoPro.ssl.tomcat.jsse;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import org.apache.tomcat.util.compat.JreVendor;
import org.apache.tomcat.util.net.SSLContext;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;

public class JCPJSSEUtil extends JCPSSLUtilBase {

	private static final Log log = LogFactory.getLog(JCPJSSEUtil.class);
	private volatile Set<String> implementedProtocols;
	private volatile Set<String> implementedCiphers;
    private volatile boolean initialized = false;

	public JCPJSSEUtil(SSLHostConfigCertificate certificate) {
		this(certificate, true);
	}

	public JCPJSSEUtil(SSLHostConfigCertificate certificate, boolean warnOnSkip) {
		super(certificate, warnOnSkip);
	}

	@Override
	protected Set<String> getImplementedCiphers() {
        initialise();
		return implementedCiphers;
	}

	@Override
	protected Set<String> getImplementedProtocols() {
        initialise();
		return implementedProtocols;
	}

	@Override
    protected boolean isTls13RenegAuthAvailable() {
        return false;
    }

    @Override
    public SSLContext createSSLContextInternal(List<String> negotiableProtocols)
        throws NoSuchAlgorithmException {
        return new JCPSSLContext(this.sslHostConfig.getSslProtocol());
    }

	@Override
	protected Log getLog() {
		return log;
	}

    private void initialise() {

        if (!this.initialized) {

            synchronized (this) {

                if (!this.initialized) {

                    SSLContext context;

                    try {

                        context = new JCPSSLContext("GostTLS"); // Прямо указан алгоритм из cpSSL
                        context.init(null, null, null);

                    } catch (NoSuchAlgorithmException e) {
                        throw new IllegalArgumentException(e);
                    } catch (KeyManagementException e) {
                        throw new IllegalArgumentException(e);
                    }

                    String[] implementedProtocolsArray = context.getSupportedSSLParameters().getProtocols();
                    this.implementedProtocols = new HashSet(implementedProtocolsArray.length);

                    for (String protocol : implementedProtocolsArray)
                    {
                        String tmpProtocol = protocol.toUpperCase(Locale.ENGLISH);
                        if ((!"SSLV2HELLO".equals(tmpProtocol)) && (!"SSLV3".equals(tmpProtocol)) && (tmpProtocol.contains("SSL"))) {
                            log.debug("Exclude protocol: " + protocol);
                        } else {
                            this.implementedProtocols.add(protocol);
                        }
                    }

                    if (implementedProtocols.size() == 0) {
                        log.warn("No default protocols");
                    }

                    String[] implementedCipherSuiteArray = context.
                        getSupportedSSLParameters().getCipherSuites();

                    if (JreVendor.IS_IBM_JVM) {
                        implementedCiphers = new HashSet(implementedCipherSuiteArray.length * 2);
                        for (String name : implementedCipherSuiteArray) {
                            implementedCiphers.add(name);
                            if (name.startsWith("SSL")) {
                                implementedCiphers.add("TLS" + name.substring(3));
                            }
                        }
                    } else {
                        implementedCiphers = new HashSet(implementedCipherSuiteArray.length);
                        implementedCiphers.addAll(Arrays.asList(implementedCipherSuiteArray));
                    }

                    this.initialized = true;

                }

            }

        }

    }

}
