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


import es.wakamiti.commons.lang.Lazy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


class LazyTest {

    @Test
    @DisplayName("Computes value only once until reset is called")
    void shouldComputeValueOnlyOnceUntilReset() {
        AtomicInteger calls = new AtomicInteger();
        Lazy<Integer> lazy = Lazy.of(calls::incrementAndGet);

        int first = lazy.get();
        int second = lazy.get();

        assertEquals(1, first);
        assertEquals(1, second);
        assertEquals(1, calls.get());
    }

    @Test
    @DisplayName("Caches null values after first computation")
    void shouldCacheNullValues() {
        AtomicInteger calls = new AtomicInteger();
        Lazy<String> lazy = Lazy.of(() -> {
            calls.incrementAndGet();
            return null;
        });

        assertNull(lazy.get());
        assertNull(lazy.get());
        assertEquals(1, calls.get());
    }

    @Test
    @DisplayName("Invalidates cache when reset is called")
    void shouldInvalidateCacheWhenResetIsCalled() {
        AtomicInteger calls = new AtomicInteger();
        Lazy<Integer> lazy = Lazy.of(calls::incrementAndGet);

        assertEquals(1, lazy.get());
        lazy.reset();
        assertEquals(2, lazy.get());
        assertEquals(2, calls.get());
    }

    @Test
    @DisplayName("Uses source lazy cache when mapping values")
    void shouldUseSourceLazyCacheWhenMappingValues() {
        AtomicInteger calls = new AtomicInteger();
        Lazy<Integer> source = Lazy.of(calls::incrementAndGet);
        Lazy<String> mapped = source.map(v -> "v" + v);

        assertEquals("v1", mapped.get());
        assertEquals("v1", mapped.get());
        assertEquals(1, calls.get());
    }

    @Test
    @DisplayName("Unwraps value from optional supplier")
    void shouldUnwrapValueFromOptionalSupplier() {
        Lazy<String> lazy = Lazy.ofOptional(() -> Optional.of("ok"));
        assertEquals("ok", lazy.get());
    }

    @Test
    @DisplayName("Throws when optional supplier returns empty")
    void shouldThrowWhenOptionalSupplierReturnsEmpty() {
        Lazy<String> lazy = Lazy.ofOptional(Optional::<String>empty);
        assertThrows(NoSuchElementException.class, lazy::get);
    }

    @Test
    @DisplayName("Throws when optional supplier returns null")
    void shouldThrowWhenOptionalSupplierReturnsNull() {
        Lazy<String> lazy = Lazy.ofOptional(() -> null);
        assertThrows(NullPointerException.class, lazy::get);
    }

    @Test
    @DisplayName("Rejects null supplier in of factory")
    void shouldRejectNullSupplierInOfFactory() {
        assertThrows(NullPointerException.class, () -> Lazy.of(null));
    }

    @Test
    @DisplayName("Rejects null supplier in ofOptional factory")
    void shouldRejectNullSupplierInOfOptionalFactory() {
        assertThrows(NullPointerException.class, () -> Lazy.ofOptional(null));
    }

    @Test
    @DisplayName("Rejects null mapper in map operation")
    void shouldRejectNullMapperInMapOperation() {
        Lazy<Integer> lazy = Lazy.of(() -> 1);
        assertThrows(NullPointerException.class, () -> lazy.map(null));
    }

}
