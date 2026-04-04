/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.commons.lang;


import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;


class ThrowableRunnableTest {

    @Test
    void runDelegatesToRunThrowingWithArguments() {
        AtomicInteger sum = new AtomicInteger();
        ThrowableRunnable runnable = arguments -> {
            int total = 0;
            for (Object argument : arguments) total += (int) argument;
            sum.set(total);
        };

        runnable.run(1, 2, 3);
        assertEquals(6, sum.get());
    }

    @Test
    void runWrapsCheckedExceptionInRuntimeException() {
        ThrowableRunnable runnable = arguments -> {
            throw new IOException("boom");
        };

        RuntimeException error = assertThrows(RuntimeException.class, () -> runnable.run(1));
        assertInstanceOf(IOException.class, error.getCause());
        assertEquals("boom", error.getCause().getMessage());
    }

}
