/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.cryptopro.jtlsconnector;

import com.sun.enterprise.security.ssl.GlassfishSSLSupport;
import java.net.Socket;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocket;

import org.glassfish.grizzly.config.ssl.SSLImplementation;
import org.glassfish.grizzly.config.ssl.ServerSocketFactory;
import org.glassfish.grizzly.ssl.SSLSupport;

/**
 *
 * @author Yevgeniy
 */
public class JTLSProxy extends SSLImplementation {

    public JTLSProxy() {
        ;
    }
    
    @Override
    public String getImplementationName() {
        return "JTLSProxy";
    }

    @Override
    public ServerSocketFactory getServerSocketFactory() {
        return new JTLSProxyServerSocketFactory();
    }

    @Override
    public SSLSupport getSSLSupport(Socket socket) {
        return new GlassfishSSLSupport((SSLSocket)socket);
    }

    @Override
    public SSLSupport getSSLSupport(SSLEngine ssle) {
        return new GlassfishSSLSupport(ssle);
    }
 
}
