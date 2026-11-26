package com.jobportal;

import static org.junit.jupiter.api.Assertions.*;

import com.jobportal.config.AppProps;
import com.jobportal.service.CryptoService;
import org.junit.jupiter.api.Test;

class CryptoServiceTest {
    @Test void roundTripAndRandomIv() {
        CryptoService c = new CryptoService(new AppProps("secret", "./data", null));
        String a = c.encrypt("p@ssw0rd"), b = c.encrypt("p@ssw0rd");
        assertNotEquals(a, b);
        assertEquals("p@ssw0rd", c.decrypt(a));
    }
    @Test void blankKeyRejected() {
        assertThrows(IllegalStateException.class, () -> new CryptoService(new AppProps(" ", "./data", null)));
    }
    @Test void wrongKeyFails() {
        String enc = new CryptoService(new AppProps("k1", "./data", null)).encrypt("x");
        assertThrows(IllegalStateException.class, () -> new CryptoService(new AppProps("k2", "./data", null)).decrypt(enc));
    }
}
