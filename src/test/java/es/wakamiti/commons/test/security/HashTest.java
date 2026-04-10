/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.wakamiti.commons.test.security;


import es.wakamiti.commons.junit.TargetTempDir;
import es.wakamiti.commons.lang.WakamitiException;
import es.wakamiti.commons.security.Hash;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class HashTest {

    @Test
    @DisplayName("Computes expected SHA3-256 Base64 digest from string content")
    void shouldComputeExpectedSha3256Base64DigestFromStringContent() {
        assertEquals(Hash.fromEncoded("Ophdp0/iJbIEXBcta9OQvYVfCG4+nVJbRr/iRRFDFTI="), Hash.of("abc"));
    }

    @Test
    @DisplayName("Hashes resource content when using URI input")
    void shouldHashResourceContentWhenUsingUriInput(
            @TargetTempDir Path tempDir
    ) throws IOException {
        Path file = tempDir.resolve("hash-uri.txt");
        String content = "contenido de prueba";
        Files.writeString(file, content);
        URI uri = file.toUri();

        assertEquals(Hash.of(content), Hash.of(uri));
    }

    @Test
    @DisplayName("Hashes resource content when using URL input")
    void shouldHashResourceContentWhenUsingUrlInput(
            @TargetTempDir Path tempDir
    ) throws IOException {
        Path file = tempDir.resolve("hash-url.txt");
        String content = "contenido de prueba URL";
        Files.writeString(file, content);
        URL url = file.toUri().toURL();

        assertEquals(Hash.of(content), Hash.of(url));
    }

    @Test
    @DisplayName("Rejects invalid Base64 value in fromEncoded")
    void shouldRejectInvalidBase64ValueInFromEncoded() {
        assertThrows(IllegalArgumentException.class, () -> Hash.fromEncoded("not-base64"));
    }

    @Test
    @DisplayName("Rejects wrong digest length in fromEncoded")
    void shouldRejectWrongDigestLengthInFromEncoded() {
        assertThrows(IllegalArgumentException.class, () -> Hash.fromEncoded("YWJj"));
    }

    @Test
    @DisplayName("Rejects null arguments in public API")
    void shouldRejectNullArgumentsInPublicApi() {
        assertThrows(NullPointerException.class, () -> Hash.of((String) null));
        assertThrows(NullPointerException.class, () -> Hash.of((URI) null));
        assertThrows(NullPointerException.class, () -> Hash.of((URL) null));
        assertThrows(NullPointerException.class, () -> Hash.fromEncoded(null));
        assertThrows(NullPointerException.class, () -> Hash.of("abc").compareTo(null));
    }

    @Test
    @DisplayName("Wraps URI read errors in WakamitiException")
    void shouldWrapUriReadErrorsInWakamitiException() {
        URI missingFile = Path.of("target", "temp-test", "does-not-exist-uri.txt").toUri();

        WakamitiException error = assertThrows(WakamitiException.class, () -> Hash.of(missingFile));
        assertTrue(error.getMessage().contains("Cannot calculate hash"));
        assertInstanceOf(IOException.class, error.getCause());
    }

    @Test
    @DisplayName("Wraps URL read errors in WakamitiException")
    void shouldWrapUrlReadErrorsInWakamitiException() throws Exception {
        URL missingFile = Path.of("target", "temp-test", "does-not-exist-url.txt").toUri().toURL();

        WakamitiException error = assertThrows(WakamitiException.class, () -> Hash.of(missingFile));
        assertTrue(error.getMessage().contains("Cannot calculate hash"));
        assertInstanceOf(IOException.class, error.getCause());
    }

    @Test
    @DisplayName("Compares hashes lexicographically by encoded value")
    void shouldCompareHashesLexicographicallyByEncodedValue() {
        Hash lower = Hash.of("a");
        Hash higher = Hash.of("b");

        assertTrue(lower.compareTo(higher) < 0);
        assertTrue(higher.compareTo(lower) > 0);
    }

    @Test
    @DisplayName("Returns zero in compareTo for equal hashes")
    void shouldReturnZeroInCompareToForEqualHashes() {
        Hash first = Hash.of("same-content");
        Hash second = Hash.of("same-content");

        assertEquals(0, first.compareTo(second));
    }

    @Test
    @DisplayName("Implements equals and hashCode based on encoded value")
    void shouldImplementEqualsAndHashCodeBasedOnEncodedValue() {
        Hash first = Hash.of("same-content");
        Hash second = Hash.of("same-content");
        Hash different = Hash.of("different-content");

        assertEquals(first, second);
        assertEquals(first, first);
        assertEquals(first.hashCode(), second.hashCode());
        assertEquals(first.value(), second.value());
        assertNotEquals(first, different);
        assertNotEquals(null, first);
        assertNotEquals(first, "not-a-hash");
    }

}
