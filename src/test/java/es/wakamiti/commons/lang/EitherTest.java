/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.commons.lang;


import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


class EitherTest {

    @Test
    void memoizedEvaluationComputesEachSupplierOnlyOnce() {
        AtomicInteger valueCalls = new AtomicInteger();
        AtomicInteger fallbackCalls = new AtomicInteger();
        Either<Integer, Integer> either = Either.memoized(valueCalls::incrementAndGet, fallbackCalls::incrementAndGet);

        assertEquals(Optional.of(1), either.value());
        assertEquals(Optional.of(1), either.value());
        assertEquals(1, either.fallback());
        assertEquals(1, either.fallback());
        assertEquals(Either.Evaluation.MEMOIZED, either.evaluation());
        assertEquals(1, valueCalls.get());
        assertEquals(1, fallbackCalls.get());
    }

    @Test
    void liveEvaluationRecomputesOnEachAccess() {
        AtomicInteger valueCalls = new AtomicInteger();
        AtomicInteger fallbackCalls = new AtomicInteger();
        Either<Integer, Integer> either = Either.live(valueCalls::incrementAndGet, fallbackCalls::incrementAndGet);

        assertEquals(Optional.of(1), either.value());
        assertEquals(Optional.of(2), either.value());
        assertEquals(1, either.fallback());
        assertEquals(2, either.fallback());
        assertEquals(Either.Evaluation.LIVE, either.evaluation());
    }

    @Test
    void valueWithMapperReturnsPrimaryWithoutCallingMapper() {
        AtomicBoolean mapperCalled = new AtomicBoolean(false);
        Either<String, Integer> either = Either.of("value", 10);

        String result = either.value(fallback -> {
            mapperCalled.set(true);
            return "mapped-" + fallback;
        });

        assertEquals("value", result);
        assertFalse(mapperCalled.get());
    }

    @Test
    void valueWithMapperUsesFallbackWhenPrimaryMissing() {
        Either<String, Integer> either = Either.fallback(5);
        String result = either.value(fallback -> "mapped-" + fallback);
        assertEquals("mapped-5", result);
    }

    @Test
    void fallbackWithMapperReturnsFallbackWithoutCallingMapper() {
        AtomicBoolean mapperCalled = new AtomicBoolean(false);
        Either<String, Integer> either = Either.of("value", 7);

        Integer result = either.fallback(value -> {
            mapperCalled.set(true);
            return value.length();
        });

        assertEquals(7, result);
        assertFalse(mapperCalled.get());
    }

    @Test
    void fallbackWithMapperUsesPrimaryWhenFallbackMissing() {
        Either<String, Integer> either = Either.of("abcd");
        Integer result = either.fallback(String::length);
        assertEquals(4, result);
    }

    @Test
    void singleValueFactoriesUseNullFallback() {
        Either<String, Integer> either = Either.of("value");
        assertEquals(Optional.of("value"), either.value());
        assertNull(either.fallback());
    }

    @Test
    void fallbackFactoriesUseNullPrimary() {
        Either<String, Integer> either = Either.fallback(42);
        assertEquals(Optional.empty(), either.value());
        assertEquals(42, either.fallback());
    }

    @Test
    void explicitStrategyFactoryExposesSelectedEvaluation() {
        Either<String, String> either = Either.of(() -> "v", () -> "f", Either.Evaluation.LIVE);
        assertEquals(Either.Evaluation.LIVE, either.evaluation());
    }

    @Test
    void valueAndFallbackMappersRejectNull() {
        Either<String, Integer> either = Either.of("value", 10);
        assertThrows(NullPointerException.class, () -> either.value((Function<Integer, String>) null));
        assertThrows(NullPointerException.class, () -> either.fallback((Function<String, Integer>) null));
    }

    @Test
    void factoriesRejectNullSuppliersAndStrategy() {
        Supplier<String> nullSupplier = null;

        assertThrows(NullPointerException.class, () -> Either.of(nullSupplier, () -> "f"));
        assertThrows(NullPointerException.class, () -> Either.of(() -> "v", (Supplier<String>) null));
        assertThrows(NullPointerException.class, () -> Either.of(() -> "v", () -> "f", null));
        assertThrows(NullPointerException.class, () -> Either.of(nullSupplier, Either.Evaluation.MEMOIZED));
        assertThrows(NullPointerException.class, () -> Either.fallback((Supplier<String>) null, Either.Evaluation.LIVE));
    }

}
