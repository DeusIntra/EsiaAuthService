package ru.CryptoPro.ssl.tomcat.jsse;

import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.apache.tomcat.util.net.SSLUtil;
import org.apache.tomcat.util.net.jsse.JSSEImplementation;

public class JCPJSSEImplementation extends JSSEImplementation {
	
	public JCPJSSEImplementation() {
	    super();
	}

	@Override
	public SSLUtil getSSLUtil(SSLHostConfigCertificate certificate) {
	    return new JCPJSSEUtil(certificate); // Собственный класс
	 }

}
