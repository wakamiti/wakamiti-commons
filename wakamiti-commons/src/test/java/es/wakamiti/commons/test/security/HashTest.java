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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class HashTest {

    private static final String SHA3_256 = Hash.SHA3_256.name();

    @Test
    @DisplayName("Computes expected SHA3-256 Base64 digest from string content")
    void shouldComputeExpectedSha3256Base64DigestFromStringContent() {
        assertEquals(Hash.fromEncoded("Ophdp0/iJbIEXBcta9OQvYVfCG4+nVJbRr/iRRFDFTI=", SHA3_256), Hash.of("abc", SHA3_256));
    }

    @Test
    @DisplayName("Computes expected Base64 digest for custom algorithm from string content")
    void shouldComputeExpectedBase64DigestForCustomAlgorithmFromStringContent() {
        assertEquals(Hash.fromEncoded("kAFQmDzST7DWlj99KOF/cg==", Hash.MD5.name()), Hash.MD5.of("abc"));
    }

    @Test
    @DisplayName("Exposes pre-created digest algorithm instances")
    void shouldExposePreCreatedDigestAlgorithmInstances() {
        Hash.DigestAlgorithm sha3 = Hash.SHA3_256;

        assertSame(sha3, Hash.algorithm(SHA3_256));
        assertEquals(SHA3_256, sha3.name());
        assertEquals(Hash.of("abc", SHA3_256), Hash.SHA3_256.of("abc"));
        assertEquals("SHA-256", Hash.SHA_256.name());
    }

    @Test
    @DisplayName("Exposes all available digest algorithms in static map")
    void shouldExposeAllAvailableDigestAlgorithmsInStaticMap() {
        assertTrue(Hash.ALGORITHMS.containsKey(SHA3_256));
        assertSame(Hash.algorithm(SHA3_256), Hash.ALGORITHMS.get(SHA3_256));
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

        assertEquals(Hash.of(content, SHA3_256), Hash.of(uri, SHA3_256));
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

        assertEquals(Hash.of(content, SHA3_256), Hash.of(url, SHA3_256));
    }

    @Test
    @DisplayName("Rejects invalid Base64 value in fromEncoded")
    void shouldRejectInvalidBase64ValueInFromEncoded() {
        assertThrows(IllegalArgumentException.class, () -> Hash.fromEncoded("not-base64", SHA3_256));
    }

    @Test
    @DisplayName("Rejects wrong digest length in fromEncoded")
    void shouldRejectWrongDigestLengthInFromEncoded() {
        assertThrows(IllegalArgumentException.class, () -> Hash.fromEncoded("YWJj", SHA3_256));
    }

    @Test
    @DisplayName("Rejects wrong digest length in fromEncoded for custom algorithm")
    void shouldRejectWrongDigestLengthInFromEncodedForCustomAlgorithm() {
        assertThrows(IllegalArgumentException.class, () -> Hash.fromEncoded("kAFQmDzST7DWlj99KOF/cg==", "SHA-256"));
    }

    @Test
    @DisplayName("Rejects unknown digest algorithms")
    void shouldRejectUnknownDigestAlgorithms() {
        assertThrows(WakamitiException.class, () -> Hash.of("abc", "NOT_A_REAL_ALGO"));
        assertThrows(WakamitiException.class, () -> Hash.fromEncoded("kAFQmDzST7DWlj99KOF/cg==", "NOT_A_REAL_ALGO"));
        assertThrows(WakamitiException.class, () -> Hash.algorithm("NOT_A_REAL_ALGO"));
    }

    @Test
    @DisplayName("Rejects null arguments in public API")
    void shouldRejectNullArgumentsInPublicApi() {
        assertThrows(NullPointerException.class, () -> Hash.of((String) null, SHA3_256));
        assertThrows(NullPointerException.class, () -> Hash.of("abc", null));
        assertThrows(NullPointerException.class, () -> Hash.of((URI) null, SHA3_256));
        assertThrows(NullPointerException.class, () -> Hash.of(URI.create("file:/tmp"), null));
        assertThrows(NullPointerException.class, () -> Hash.of((URL) null, SHA3_256));
        assertThrows(NullPointerException.class, () -> Hash.of(HashTest.class.getResource("/"), null));
        assertThrows(NullPointerException.class, () -> Hash.fromEncoded(null, SHA3_256));
        assertThrows(NullPointerException.class, () -> Hash.fromEncoded("kAFQmDzST7DWlj99KOF/cg==", null));
        assertThrows(NullPointerException.class, () -> Hash.of("abc", SHA3_256).compareTo(null));
    }

    @Test
    @DisplayName("Wraps URI read errors in WakamitiException")
    void shouldWrapUriReadErrorsInWakamitiException() {
        URI missingFile = Path.of("target", "temp-test", "does-not-exist-uri.txt").toUri();

        WakamitiException error = assertThrows(WakamitiException.class, () -> Hash.of(missingFile, SHA3_256));
        assertTrue(error.getMessage().contains("Cannot calculate hash"));
        assertInstanceOf(IOException.class, error.getCause());
    }

    @Test
    @DisplayName("Wraps URL read errors in WakamitiException")
    void shouldWrapUrlReadErrorsInWakamitiException() throws Exception {
        URL missingFile = Path.of("target", "temp-test", "does-not-exist-url.txt").toUri().toURL();

        WakamitiException error = assertThrows(WakamitiException.class, () -> Hash.of(missingFile, SHA3_256));
        assertTrue(error.getMessage().contains("Cannot calculate hash"));
        assertInstanceOf(IOException.class, error.getCause());
    }

    @Test
    @DisplayName("Compares hashes lexicographically")
    void shouldCompareHashesLexicographically() {
        Hash lower = Hash.of("a", SHA3_256);
        Hash higher = Hash.of("b", SHA3_256);

        assertTrue(lower.compareTo(higher) < 0);
        assertTrue(higher.compareTo(lower) > 0);
    }

    @Test
    @DisplayName("Returns zero in compareTo for equal hashes")
    void shouldReturnZeroInCompareToForEqualHashes() {
        Hash first = Hash.of("same-content", SHA3_256);
        Hash second = Hash.of("same-content", SHA3_256);

        assertEquals(0, first.compareTo(second));
    }

    @Test
    @DisplayName("Implements equals and hashCode based on encoded value")
    void shouldImplementEqualsAndHashCodeBasedOnEncodedValue() {
        Hash first = Hash.of("same-content", SHA3_256);
        Hash second = Hash.of("same-content", SHA3_256);
        Hash different = Hash.of("different-content", SHA3_256);
        Hash sameValueDifferentAlgorithm = Hash.fromEncoded(first.value(), "SHA-256");

        assertEquals(first, second);
        assertEquals(first, first);
        assertEquals(first.hashCode(), second.hashCode());
        assertEquals(first.value(), second.value());
        assertEquals(SHA3_256, first.algorithm());
        assertNotEquals(first, different);
        assertNotEquals(first, sameValueDifferentAlgorithm);
        assertNotEquals(null, first);
        assertNotEquals(first, "not-a-hash");
    }

}
