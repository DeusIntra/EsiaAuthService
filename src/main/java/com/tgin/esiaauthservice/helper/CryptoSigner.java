package com.tgin.esiaauthservice.helper;

public interface CryptoSigner {
    byte[] sign(String textToSign);
}
