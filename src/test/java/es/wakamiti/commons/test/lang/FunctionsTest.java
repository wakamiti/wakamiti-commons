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


import es.wakamiti.commons.lang.Functions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class FunctionsTest {

    @Test
    @DisplayName("Returns mapped value when let input is non-null")
    void shouldReturnMappedValueWhenLetInputIsNonNull() {
        Integer result = Functions.let("abc", String::length);
        assertEquals(3, result);
    }

    @Test
    @DisplayName("Returns null when let input is null")
    void shouldReturnNullWhenLetInputIsNull() {
        Integer result = Functions.let(null, String::length);
        assertNull(result);
    }

    @Test
    @DisplayName("Returns primary value when present")
    void shouldReturnPrimaryValueWhenPresent() {
        String result = Functions.or("value", "fallback");
        assertEquals("value", result);
    }

    @Test
    @DisplayName("Returns fallback value when primary is null")
    void shouldReturnFallbackValueWhenPrimaryIsNull() {
        String result = Functions.or(null, "fallback");
        assertEquals("fallback", result);
    }

    @Test
    @DisplayName("Evaluates fallback supplier only when primary is null")
    void shouldEvaluateFallbackSupplierOnlyWhenPrimaryIsNull() {
        AtomicInteger calls = new AtomicInteger();
        String primary = Functions.or("value", () -> {
            calls.incrementAndGet();
            return "fallback";
        });
        String fallback = Functions.or(null, () -> {
            calls.incrementAndGet();
            return "fallback";
        });

        assertEquals("value", primary);
        assertEquals("fallback", fallback);
        assertEquals(1, calls.get());
    }

    @Test
    @DisplayName("Executes action and returns original value")
    void shouldRunActionAndReturnOriginalValue() {
        AtomicReference<String> seen = new AtomicReference<>();
        String result = Functions.also("data", seen::set);

        assertEquals("data", result);
        assertEquals("data", seen.get());
    }

    @Test
    @DisplayName("Returns null and skips action when value is null")
    void shouldReturnNullAndSkipActionWhenValueIsNull() {
        AtomicInteger calls = new AtomicInteger();
        String result = Functions.also(null, ignored -> calls.incrementAndGet());

        assertNull(result);
        assertEquals(0, calls.get());
    }

    @Test
    @DisplayName("Runs action only for non-null values")
    void shouldRunIfPresentActionOnlyForNonNullValues() {
        AtomicInteger sum = new AtomicInteger();

        Functions.ifPresent(7, sum::addAndGet);
        Functions.ifPresent(null, sum::addAndGet);

        assertEquals(7, sum.get());
    }

    @Test
    @DisplayName("Returns typed value when cast is compatible")
    void shouldReturnTypedValueWhenCastIsCompatible() {
        Number value = 12;
        Integer result = Functions.cast(value, Integer.class);
        assertEquals(12, result);
    }

    @Test
    @DisplayName("Returns null when cast is incompatible or input is null")
    void shouldReturnNullWhenCastIsIncompatibleOrInputIsNull() {
        Number integer = 12;
        Double incompatible = Functions.cast(integer, Double.class);
        Integer nullInput = Functions.cast(null, Integer.class);

        assertNull(incompatible);
        assertNull(nullInput);
    }

    @Test
    @DisplayName("Returns first element or null for empty list")
    void shouldReturnFirstElementOrNullForEmptyList() {
        assertEquals("a", Functions.first(List.of("a", "b")));
        assertNull(Functions.first(List.of()));
    }

    @Test
    @DisplayName("Returns immutable list with index-based mapped values")
    void shouldReturnImmutableIndexedMappedValues() {
        List<String> mapped = Functions.indexMapped(List.of("a", "b", "c"), (i, value) -> i + ":" + value);

        assertEquals(List.of("0:a", "1:b", "2:c"), mapped);
        assertThrows(UnsupportedOperationException.class, () -> mapped.add("x"));
    }

    @Test
    @DisplayName("Returns empty list for null source in indexMapped")
    void shouldReturnEmptyListForNullSourceInIndexMapped() {
        List<String> mapped = Functions.indexMapped(null, (i, value) -> i + ":" + value);
        assertTrue(mapped.isEmpty());
    }

    @Test
    @DisplayName("Returns mapped list or empty list when source is null")
    void shouldReturnMappedListOrEmptyListWhenSourceIsNull() {
        List<Integer> mapped = Functions.mapped(List.of("a", "bb"), String::length);
        List<Integer> empty = Functions.mapped(null, String::length);

        assertEquals(List.of(1, 2), mapped);
        assertTrue(empty.isEmpty());
    }

    @Test
    @DisplayName("Concatenates both lists preserving order")
    void shouldConcatenateBothListsInOrder() {
        List<String> result = Functions.concat(List.of("a"), List.of("b", "c"));
        assertEquals(List.of("a", "b", "c"), result);
    }

    @Test
    @DisplayName("Concatenates empty lists into an empty result")
    void shouldConcatenateEmptyListsIntoEmptyResult() {
        List<String> result = Functions.concat(List.of(), List.of());
        assertFalse(result.iterator().hasNext());
    }

}
