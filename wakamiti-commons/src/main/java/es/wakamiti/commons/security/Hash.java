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
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * Comparable hash wrapper used to identify resource content.
 * <p>
 * Hashes are generated using MessageDigest algorithms and encoded as Base64 strings.
 */
public final class Hash implements Comparable<Hash> {

    private static final String INVALID_HASH_VALUE_TEMPLATE = "Hash value must be a Base64 encoded digest for algorithm %s";
    private static final Base64.Encoder encoder = Base64.getEncoder();
    private static final Base64.Decoder decoder = Base64.getDecoder();

    public static final Map<String, DigestAlgorithm> ALGORITHMS = Security.getAlgorithms("MessageDigest")
            .stream()
            .collect(Collectors.toUnmodifiableMap(algorithm -> algorithm, DigestAlgorithm::new));
    public static final DigestAlgorithm MD2 = algorithm("MD2");
    public static final DigestAlgorithm MD5 = algorithm("MD5");
    public static final DigestAlgorithm SHA_1 = algorithm("SHA-1");
    public static final DigestAlgorithm SHA_224 = algorithm("SHA-224");
    public static final DigestAlgorithm SHA_256 = algorithm("SHA-256");
    public static final DigestAlgorithm SHA_384 = algorithm("SHA-384");
    public static final DigestAlgorithm SHA_512 = algorithm("SHA-512");
    public static final DigestAlgorithm SHA_512_224 = algorithm("SHA-512/224");
    public static final DigestAlgorithm SHA_512_256 = algorithm("SHA-512/256");
    public static final DigestAlgorithm SHA3_224 = algorithm("SHA3-224");
    public static final DigestAlgorithm SHA3_256 = algorithm("SHA3-256");
    public static final DigestAlgorithm SHA3_384 = algorithm("SHA3-384");
    public static final DigestAlgorithm SHA3_512 = algorithm("SHA3-512");
    
    private final String algorithm;
    private final String value;

    private Hash(
            String algorithm,
            String value
    ) {
        this.algorithm = validateAlgorithm(algorithm);
        this.value = validateEncoded(value, this.algorithm);
    }

    private static String validateAlgorithm(
            String algorithm
    ) {
        return algorithm(algorithm).name();
    }

    private static String validateEncoded(
            String value,
            String algorithm
    ) {
        Objects.requireNonNull(value, "value");
        byte[] decoded;
        try {
            decoded = decoder.decode(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(INVALID_HASH_VALUE_TEMPLATE.formatted(algorithm), e);
        }
        if (decoded.length != digestSizeBytes(algorithm)) {
            throw new IllegalArgumentException(INVALID_HASH_VALUE_TEMPLATE.formatted(algorithm));
        }
        return value;
    }

    private static int digestSizeBytes(
            String algorithm
    ) {
        MessageDigest digest = newDigest(algorithm);
        int digestSize = digest.getDigestLength();
        return digestSize > 0 ? digestSize : digest.digest(new byte[0]).length;
    }

    private static MessageDigest newDigest(
            String algorithm
    ) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new WakamitiException("Error obtaining hash algorithm {}. Must be one of {}",
                    algorithm, Security.getAlgorithms("MessageDigest"), e);
        }
    }

    /**
     * Returns the pre-created digest algorithm instance for the requested name.
     *
     * @param algorithm digest algorithm name supported by the JVM
     * @return pre-created digest algorithm instance
     */
    public static DigestAlgorithm algorithm(
            String algorithm
    ) {
        Objects.requireNonNull(algorithm, "algorithm");
        DigestAlgorithm digestAlgorithm = ALGORITHMS.get(algorithm);
        if (digestAlgorithm == null) {
            throw new WakamitiException("Error obtaining hash algorithm {}. Must be one of {}",
                    algorithm, ALGORITHMS.keySet());
        }
        return digestAlgorithm;
    }

    /**
     * Computes the hash of a string using UTF-8 bytes and the requested MessageDigest algorithm.
     *
     * @param content source text
     * @param algorithm digest algorithm name supported by the JVM
     * @return computed hash
     */
    public static Hash of(
            String content,
            String algorithm
    ) {
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(algorithm, "algorithm");
        var bytes = newDigest(algorithm).digest(content.getBytes(StandardCharsets.UTF_8));
        return fromDigest(algorithm, bytes);
    }

    /**
     * Computes the hash of the bytes pointed by the URI using the requested MessageDigest algorithm.
     *
     * @param uri source URI
     * @param algorithm digest algorithm name supported by the JVM
     * @return computed hash
     * @throws WakamitiException when the URI cannot be read
     */
    public static Hash of(
            URI uri,
            String algorithm
    ) {
        Objects.requireNonNull(uri, "uri");
        Objects.requireNonNull(algorithm, "algorithm");
        try {
            return of(uri.toURL(), algorithm);
        } catch (MalformedURLException ex) {
            throw new WakamitiException("Cannot calculate hash of {resource}.", uri, ex);
        }
    }

    /**
     * Computes the hash of the bytes pointed by the URL using the requested MessageDigest algorithm.
     *
     * @param url source URL
     * @param algorithm digest algorithm name supported by the JVM
     * @return computed hash
     * @throws WakamitiException when the URL cannot be read
     */
    public static Hash of(
            URL url,
            String algorithm
    ) {
        Objects.requireNonNull(url, "url");
        Objects.requireNonNull(algorithm, "algorithm");
        try (var stream = new DigestInputStream(url.openStream(), newDigest(algorithm))) {
            byte[] buffer = new byte[8192];
            while (stream.read(buffer) != -1) {
                // Consume full stream so digest is computed from content bytes.
            }
            return fromDigest(algorithm, stream.getMessageDigest().digest());
        } catch (IOException ex) {
            throw new WakamitiException("Cannot calculate hash of {resource}.", url, ex);
        }
    }

    /**
     * Rebuilds a hash from an already encoded Base64 value and algorithm.
     *
     * @param value encoded hash value
     * @param algorithm digest algorithm name supported by the JVM
     * @return hash instance
     */
    public static Hash fromEncoded(
            String value,
            String algorithm
    ) {
        return new Hash(algorithm, value);
    }

    /**
     * Returns the algorithm used to compute the hash value.
     *
     * @return digest algorithm name
     */
    public String algorithm() {
        return algorithm;
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
            String algorithm,
            byte[] digest
    ) {
        return new Hash(algorithm, encoder.encodeToString(digest));
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
        int algorithmComparison = algorithm.compareTo(other.algorithm);
        if (algorithmComparison != 0) {
            return algorithmComparison;
        }
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
        return algorithm.equals(other.algorithm) && value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(algorithm, value);
    }

    /**
     * Pre-created digest algorithm facade.
     */
    public static final class DigestAlgorithm {

        private final String name;

        private DigestAlgorithm(
                String name
        ) {
            this.name = name;
        }

        /**
         * Returns the digest algorithm name.
         *
         * @return algorithm name
         */
        public String name() {
            return name;
        }

        /**
         * Computes hash from content bytes using this algorithm.
         *
         * @param content source text
         * @return computed hash
         */
        public Hash of(
                String content
        ) {
            return Hash.of(content, name);
        }

        /**
         * Computes hash from URI resource bytes using this algorithm.
         *
         * @param uri source URI
         * @return computed hash
         */
        public Hash of(
                URI uri
        ) {
            return Hash.of(uri, name);
        }

        /**
         * Computes hash from URL resource bytes using this algorithm.
         *
         * @param url source URL
         * @return computed hash
         */
        public Hash of(
                URL url
        ) {
            return Hash.of(url, name);
        }

        /**
         * Rebuilds a hash from encoded value using this algorithm.
         *
         * @param value encoded hash value
         * @return hash instance
         */
        public Hash fromEncoded(
                String value
        ) {
            return Hash.fromEncoded(value, name);
        }
    }
}
