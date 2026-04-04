/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.commons.lang;


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
    void letReturnsMappedValueForNonNullInput() {
        Integer result = Functions.let("abc", String::length);
        assertEquals(3, result);
    }

    @Test
    void letReturnsNullForNullInput() {
        Integer result = Functions.let(null, String::length);
        assertNull(result);
    }

    @Test
    void orReturnsPrimaryValueWhenPresent() {
        String result = Functions.or("value", "fallback");
        assertEquals("value", result);
    }

    @Test
    void orReturnsFallbackValueWhenPrimaryIsNull() {
        String result = Functions.or(null, "fallback");
        assertEquals("fallback", result);
    }

    @Test
    void orWithSupplierEvaluatesOnlyWhenPrimaryIsNull() {
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
    void alsoRunsActionAndReturnsOriginalValue() {
        AtomicReference<String> seen = new AtomicReference<>();
        String result = Functions.also("data", seen::set);

        assertEquals("data", result);
        assertEquals("data", seen.get());
    }

    @Test
    void alsoReturnsNullAndSkipsActionForNullValue() {
        AtomicInteger calls = new AtomicInteger();
        String result = Functions.also(null, ignored -> calls.incrementAndGet());

        assertNull(result);
        assertEquals(0, calls.get());
    }

    @Test
    void ifPresentRunsActionOnlyForNonNullValues() {
        AtomicInteger sum = new AtomicInteger();

        Functions.ifPresent(7, sum::addAndGet);
        Functions.ifPresent(null, sum::addAndGet);

        assertEquals(7, sum.get());
    }

    @Test
    void castReturnsTypedValueWhenCompatible() {
        Number value = 12;
        Integer result = Functions.cast(value, Integer.class);
        assertEquals(12, result);
    }

    @Test
    void castReturnsNullWhenIncompatibleOrNullInput() {
        Number integer = 12;
        Double incompatible = Functions.cast(integer, Double.class);
        Integer nullInput = Functions.cast(null, Integer.class);

        assertNull(incompatible);
        assertNull(nullInput);
    }

    @Test
    void firstReturnsFirstElementOrNullForEmptyList() {
        assertEquals("a", Functions.first(List.of("a", "b")));
        assertNull(Functions.first(List.of()));
    }

    @Test
    void indexMappedReturnsImmutableMappedValuesWithIndex() {
        List<String> mapped = Functions.indexMapped(List.of("a", "b", "c"), (i, value) -> i + ":" + value);

        assertEquals(List.of("0:a", "1:b", "2:c"), mapped);
        assertThrows(UnsupportedOperationException.class, () -> mapped.add("x"));
    }

    @Test
    void indexMappedReturnsEmptyListForNullSource() {
        List<String> mapped = Functions.indexMapped(null, (i, value) -> i + ":" + value);
        assertTrue(mapped.isEmpty());
    }

    @Test
    void mappedReturnsMappedListOrEmptyForNullSource() {
        List<Integer> mapped = Functions.mapped(List.of("a", "bb"), String::length);
        List<Integer> empty = Functions.mapped(null, String::length);

        assertEquals(List.of(1, 2), mapped);
        assertTrue(empty.isEmpty());
    }

    @Test
    void concatJoinsBothListsInOrder() {
        List<String> result = Functions.concat(List.of("a"), List.of("b", "c"));
        assertEquals(List.of("a", "b", "c"), result);
    }

    @Test
    void concatCanJoinEmptyLists() {
        List<String> result = Functions.concat(List.of(), List.of());
        assertFalse(result.iterator().hasNext());
    }

}
