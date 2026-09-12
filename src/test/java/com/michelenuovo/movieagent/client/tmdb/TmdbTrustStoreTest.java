package com.michelenuovo.movieagent.client.tmdb;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.TrustManagerFactory;

import org.junit.jupiter.api.Test;

class TmdbTrustStoreTest {

    @Test
    void loadsPkcs12TrustStoreFromDisk()
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        Path trustStoreFile = Files.createTempFile("tmdb-truststore", ".p12");
        char[] password = "changeit".toCharArray();

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, password);
        try (OutputStream outputStream = Files.newOutputStream(trustStoreFile)) {
            keyStore.store(outputStream, password);
        }

        TrustManagerFactory trustManagerFactory =
                TmdbClient.buildTrustManagerFactory(trustStoreFile.toString(), "changeit", "PKCS12");

        assertNotNull(trustManagerFactory);
    }

    @Test
    void throwsWhenTrustStorePathIsInvalid() {
        assertThrows(IllegalStateException.class,
                () -> TmdbClient.buildTrustManagerFactory("/path/that/does/not/exist.p12", "changeit", "PKCS12"));
    }
}

