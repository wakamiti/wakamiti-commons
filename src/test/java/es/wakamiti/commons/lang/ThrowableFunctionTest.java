/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.commons.lang;


import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;


class ThrowableFunctionTest {

    @Test
    void applyDelegatesToApplyThrowing() {
        ThrowableFunction<String, Integer> function = String::length;
        assertEquals(4, function.apply("test"));
    }

    @Test
    void applyWrapsCheckedExceptionInRuntimeException() {
        ThrowableFunction<String, Integer> function = value -> {
            throw new IOException("boom");
        };

        RuntimeException error = assertThrows(RuntimeException.class, () -> function.apply("x"));
        assertInstanceOf(IOException.class, error.getCause());
        assertEquals("boom", error.getCause().getMessage());
    }

}
