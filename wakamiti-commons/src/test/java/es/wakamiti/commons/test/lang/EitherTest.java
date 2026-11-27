/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.commons.test.lang;


import es.wakamiti.commons.lang.Either;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("Computes each supplier only once with memoized evaluation")
    void shouldComputeEachSupplierOnlyOnceWhenEvaluationIsMemoized() {
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
    @DisplayName("Recomputes suppliers on each access with live evaluation")
    void shouldRecomputeSuppliersOnEachAccessWhenEvaluationIsLive() {
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
    @DisplayName("Returns primary value without invoking value mapper")
    void shouldReturnPrimaryValueWithoutCallingValueMapper() {
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
    @DisplayName("Uses fallback mapper when primary value is missing")
    void shouldMapFallbackWhenPrimaryValueIsMissing() {
        Either<String, Integer> either = Either.fallback(5);
        String result = either.value(fallback -> "mapped-" + fallback);
        assertEquals("mapped-5", result);
    }

    @Test
    @DisplayName("Returns fallback value without invoking fallback mapper")
    void shouldReturnFallbackValueWithoutCallingFallbackMapper() {
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
    @DisplayName("Uses primary mapper when fallback value is missing")
    void shouldMapPrimaryWhenFallbackValueIsMissing() {
        Either<String, Integer> either = Either.of("abcd");
        Integer result = either.fallback(String::length);
        assertEquals(4, result);
    }

    @Test
    @DisplayName("Uses null fallback when created with only primary value")
    void shouldUseNullFallbackWhenCreatedWithSingleValue() {
        Either<String, Integer> either = Either.of("value");
        assertEquals(Optional.of("value"), either.value());
        assertNull(either.fallback());
    }

    @Test
    @DisplayName("Uses null primary when created with fallback factory")
    void shouldUseNullPrimaryWhenCreatedWithFallbackFactory() {
        Either<String, Integer> either = Either.fallback(42);
        assertEquals(Optional.empty(), either.value());
        assertEquals(42, either.fallback());
    }

    @Test
    @DisplayName("Exposes selected evaluation strategy from explicit factory")
    void shouldExposeSelectedEvaluationStrategyFromFactory() {
        Either<String, String> either = Either.of(() -> "v", () -> "f", Either.Evaluation.LIVE);
        assertEquals(Either.Evaluation.LIVE, either.evaluation());
    }

    @Test
    @DisplayName("Rejects null value and fallback mappers")
    void shouldRejectNullValueAndFallbackMappers() {
        Either<String, Integer> either = Either.of("value", 10);
        assertThrows(NullPointerException.class, () -> either.value((Function<Integer, String>) null));
        assertThrows(NullPointerException.class, () -> either.fallback((Function<String, Integer>) null));
    }

    @Test
    @DisplayName("Rejects null suppliers and null evaluation strategy")
    void shouldRejectNullSuppliersAndEvaluationStrategy() {
        Supplier<String> nullSupplier = null;

        assertThrows(NullPointerException.class, () -> Either.of(nullSupplier, () -> "f"));
        assertThrows(NullPointerException.class, () -> Either.of(() -> "v", (Supplier<String>) null));
        assertThrows(NullPointerException.class, () -> Either.of(() -> "v", () -> "f", null));
        assertThrows(NullPointerException.class, () -> Either.of(nullSupplier, Either.Evaluation.MEMOIZED));
        assertThrows(NullPointerException.class, () -> Either.fallback((Supplier<String>) null, Either.Evaluation.LIVE));
    }

}
