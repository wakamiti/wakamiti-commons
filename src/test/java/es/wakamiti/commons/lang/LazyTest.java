/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.commons.lang;


import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


class LazyTest {

    @Test
    void getComputesOnlyOnceUntilReset() {
        AtomicInteger calls = new AtomicInteger();
        Lazy<Integer> lazy = Lazy.of(calls::incrementAndGet);

        int first = lazy.get();
        int second = lazy.get();

        assertEquals(1, first);
        assertEquals(1, second);
        assertEquals(1, calls.get());
    }

    @Test
    void getCachesNullValues() {
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
    void resetInvalidatesCache() {
        AtomicInteger calls = new AtomicInteger();
        Lazy<Integer> lazy = Lazy.of(calls::incrementAndGet);

        assertEquals(1, lazy.get());
        lazy.reset();
        assertEquals(2, lazy.get());
        assertEquals(2, calls.get());
    }

    @Test
    void mapUsesSourceLazyCache() {
        AtomicInteger calls = new AtomicInteger();
        Lazy<Integer> source = Lazy.of(calls::incrementAndGet);
        Lazy<String> mapped = source.map(v -> "v" + v);

        assertEquals("v1", mapped.get());
        assertEquals("v1", mapped.get());
        assertEquals(1, calls.get());
    }

    @Test
    void ofOptionalUnwrapsValue() {
        Lazy<String> lazy = Lazy.ofOptional(() -> Optional.of("ok"));
        assertEquals("ok", lazy.get());
    }

    @Test
    void ofOptionalFailsWhenEmpty() {
        Lazy<String> lazy = Lazy.ofOptional(Optional::<String>empty);
        assertThrows(NoSuchElementException.class, lazy::get);
    }

    @Test
    void ofOptionalFailsWhenSupplierReturnsNullOptional() {
        Lazy<String> lazy = Lazy.ofOptional(() -> null);
        assertThrows(NullPointerException.class, lazy::get);
    }

    @Test
    void ofRejectsNullSupplier() {
        assertThrows(NullPointerException.class, () -> Lazy.of(null));
    }

    @Test
    void ofOptionalRejectsNullSupplier() {
        assertThrows(NullPointerException.class, () -> Lazy.ofOptional(null));
    }

    @Test
    void mapRejectsNullMapper() {
        Lazy<Integer> lazy = Lazy.of(() -> 1);
        assertThrows(NullPointerException.class, () -> lazy.map(null));
    }

}
