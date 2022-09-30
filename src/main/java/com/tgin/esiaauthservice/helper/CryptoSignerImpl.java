package com.tgin.esiaauthservice.helper;

import com.objsys.asn1j.runtime.*;
import com.tgin.esiaauthservice.EsiaProperties;
import org.springframework.stereotype.Service;
import ru.CryptoPro.Crypto.CryptoProvider;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.*;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.CertificateSerialNumber;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Name;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.params.AlgIdSpec;
import ru.CryptoPro.JCP.params.OID;
import ru.CryptoPro.reprov.RevCheck;

import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

/**
 * based on http://www.cryptopro.ru/forum2/default.aspx?g=posts&t=16526
 */
@Service
class CryptoSignerImpl implements CryptoSigner {

    private final PrivateKey privateKey;
    private final Certificate certificate;

    // for java 10 and higher https://www.cryptopro.ru/forum2/default.aspx?g=posts&m=105418#post105418
    static {
        Security.addProvider(new JCP());
        Security.addProvider(new RevCheck());
        Security.addProvider(new CryptoProvider());
    }

    public CryptoSignerImpl(EsiaProperties esiaProperties) {
        try {

            KeyStore keyStore = KeyStore.getInstance(JCP.HD_STORE_NAME);

            keyStore.load(null, null); // loads from system-wide CryptoPro container
            char[] pass = "2969719".toCharArray();
            String alias = "rnd-F-B0FC-C706-7DF9-61A1-97BA-A318-D338";

            Key key = keyStore.getKey(esiaProperties.getKeystoreAlias(), pass);
            privateKey = (PrivateKey) key; //keyStore.getKey(esiaProperties.getKeystoreAlias(), esiaProperties.getPrivateKeyPassword().toCharArray());

            certificate = keyStore.getCertificate(esiaProperties.getKeystoreAlias());

        } catch (Exception e) {
            throw new CryptoSignerException("Unable to create " + CryptoSignerImpl.class.getSimpleName(), e);
        }
    }

    /**
     * PKCS#7 detached signature
     */
    @Override
    public byte[] sign(String textToSign) {
        boolean detached = true;
        try {
            return cmsSign(textToSign.getBytes(StandardCharsets.UTF_8.name()), privateKey, certificate, detached);
        } catch (Exception e) {
            throw new CryptoSignerException("Unable to sign '" + textToSign.substring(0, 50) + '\'', e);
        }
    }



    private byte[] cmsSign(byte[] data, PrivateKey key, Certificate cert, boolean detached) throws Exception {
        Signature signature = Signature.getInstance(JCP.GOST_SIGN_2012_256_NAME);

        signature.initSign(key);
        signature.update(data);
        byte[] sign = signature.sign();
        return createCMS(data, sign, cert, detached);
    }

    private byte[] createCMS(byte[] buffer, byte[] sign, Certificate cert, boolean detached) throws Exception {
        ContentInfo all = new ContentInfo();
        all.contentType = new Asn1ObjectIdentifier((new OID("1.2.840.113549.1.7.2")).value);
        SignedData cms = new SignedData();
        all.content = cms;
        cms.version = new CMSVersion(1L);
        cms.digestAlgorithms = new DigestAlgorithmIdentifiers(1);
        DigestAlgorithmIdentifier a = new DigestAlgorithmIdentifier((new OID("1.2.643.2.2.9")).value);
        a.parameters = new Asn1Null();
        cms.digestAlgorithms.elements[0] = a;
        if (detached) {
            cms.encapContentInfo = new EncapsulatedContentInfo(new Asn1ObjectIdentifier((new OID("1.2.840.113549.1.7.1")).value), (Asn1OctetString) null);
        } else {
            cms.encapContentInfo = new EncapsulatedContentInfo(new Asn1ObjectIdentifier((new OID("1.2.840.113549.1.7.1")).value), new Asn1OctetString(buffer));
        }

        cms.certificates = new CertificateSet(1);
        ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Certificate cryptoProCertificate = new ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Certificate();
        Asn1BerDecodeBuffer decodeBuffer = new Asn1BerDecodeBuffer(cert.getEncoded());
        cryptoProCertificate.decode(decodeBuffer);
        cms.certificates.elements = new CertificateChoices[1];
        cms.certificates.elements[0] = new CertificateChoices();
        cms.certificates.elements[0].set_certificate(cryptoProCertificate);
        cms.signerInfos = new SignerInfos(1);
        cms.signerInfos.elements[0] = new SignerInfo();
        cms.signerInfos.elements[0].version = new CMSVersion(1L);
        cms.signerInfos.elements[0].sid = new SignerIdentifier();
        byte[] encodedName = ((X509Certificate) cert).getIssuerX500Principal().getEncoded();
        Asn1BerDecodeBuffer nameBuf = new Asn1BerDecodeBuffer(encodedName);
        Name name = new Name();
        name.decode(nameBuf);
        CertificateSerialNumber num = new CertificateSerialNumber(((X509Certificate) cert).getSerialNumber());
        cms.signerInfos.elements[0].sid.set_issuerAndSerialNumber(new IssuerAndSerialNumber(name, num));

        cms.signerInfos.elements[0].digestAlgorithm = new DigestAlgorithmIdentifier(AlgIdSpec.OID_DIGEST_2012_256.value);
        cms.signerInfos.elements[0].digestAlgorithm.parameters = new Asn1Null();

        cms.signerInfos.elements[0].signatureAlgorithm = new SignatureAlgorithmIdentifier(AlgIdSpec.OID_PARAMS_SIG_2012_256.value);

        cms.signerInfos.elements[0].signatureAlgorithm.parameters = new Asn1Null();
        cms.signerInfos.elements[0].signature = new SignatureValue(sign);
        Asn1BerEncodeBuffer asnBuf = new Asn1BerEncodeBuffer();
        all.encode(asnBuf, true);
        return asnBuf.getMsgCopy();
    }
}
