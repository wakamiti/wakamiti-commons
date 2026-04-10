/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.commons.security;


import es.wakamiti.commons.lang.WakamitiException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Base64;
import java.util.Objects;


/**
 * Comparable hash wrapper used to identify resource content.
 * <p>
 * Hashes are generated using SHA3-256 and encoded as Base64 strings.
 */
public final class Hash implements Comparable<Hash> {

    private static final String ALGORITHM = "SHA3-256";
    private static final String INVALID_HASH_VALUE = "Hash value must be a Base64 encoded SHA3-256 digest";
    private static final int DIGEST_SIZE_BYTES = 32;
    private static final Base64.Encoder encoder = Base64.getEncoder();
    private static final Base64.Decoder decoder = Base64.getDecoder();
    private final String value;

    private Hash(
            String value
    ) {
        this.value = validateEncoded(value);
    }

    private static String validateEncoded(
            String value
    ) {
        Objects.requireNonNull(value, "value");
        byte[] decoded;
        try {
            decoded = decoder.decode(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(INVALID_HASH_VALUE, e);
        }
        if (decoded.length != DIGEST_SIZE_BYTES) {
            throw new IllegalArgumentException(INVALID_HASH_VALUE);
        }
        return value;
    }

    private static MessageDigest newDigest() {
        try {
            return MessageDigest.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new WakamitiException("Error obtaining hash algorithm {}. Must be one of {}",
                    ALGORITHM, Security.getAlgorithms("MessageDigest"), e);
        }
    }

    /**
     * Computes the hash of a string using UTF-8 bytes.
     *
     * @param content source text
     * @return computed hash
     */
    public static Hash of(
            String content
    ) {
        Objects.requireNonNull(content, "content");
        var bytes = newDigest().digest(content.getBytes(StandardCharsets.UTF_8));
        return fromDigest(bytes);
    }

    /**
     * Computes the hash of the bytes pointed by the URI.
     *
     * @param uri source URI
     * @return computed hash
     * @throws WakamitiException when the URI cannot be read
     */
    public static Hash of(
            URI uri
    ) {
        Objects.requireNonNull(uri, "uri");
        try {
            return of(uri.toURL());
        } catch (MalformedURLException ex) {
            throw new WakamitiException("Cannot calculate hash of {resource}.", uri, ex);
        }
    }

    /**
     * Computes the hash of the bytes pointed by the URL.
     *
     * @param url source URL
     * @return computed hash
     * @throws WakamitiException when the URL cannot be read
     */
    public static Hash of(
            URL url
    ) {
        Objects.requireNonNull(url, "url");
        try (var stream = new DigestInputStream(url.openStream(), newDigest())) {
            byte[] buffer = new byte[8192];
            while (stream.read(buffer) != -1) {
                // Consume full stream so digest is computed from content bytes.
            }
            return fromDigest(stream.getMessageDigest().digest());
        } catch (IOException ex) {
            throw new WakamitiException("Cannot calculate hash of {resource}.", url, ex);
        }
    }

    /**
     * Rebuilds a hash from an already encoded Base64 SHA3-256 value.
     *
     * @param value encoded hash value
     * @return hash instance
     */
    public static Hash fromEncoded(
            String value
    ) {
        return new Hash(value);
    }

    /**
     * Returns the encoded Base64 hash value.
     *
     * @return encoded hash
     */
    public String value() {
        return value;
    }

    private static Hash fromDigest(
            byte[] digest
    ) {
        return new Hash(encoder.encodeToString(digest));
    }

    /**
     * Compares hash values lexicographically.
     *
     * @param other other hash
     * @return comparison result
     */
    @Override
    public int compareTo(
            Hash other
    ) {
        Objects.requireNonNull(other, "other");
        return value.compareTo(other.value);
    }

    @Override
    public boolean equals(
            Object o
    ) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Hash other)) {
            return false;
        }
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
