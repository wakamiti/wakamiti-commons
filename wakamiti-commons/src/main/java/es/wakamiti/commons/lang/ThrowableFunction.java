/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.commons.lang;


/**
 * Functional interface similar to {@link java.util.function.Function} whose
 * main operation can throw checked exceptions.
 *
 * @param <T> input type
 * @param <U> output type
 */
@FunctionalInterface
public interface ThrowableFunction<T, U> {

    /**
     * Applies this function and allows checked exceptions.
     *
     * @param value function input
     * @return mapped output
     * @throws Exception when mapping fails
     */
    U applyThrowing(
            T value
    ) throws Exception;

    /**
     * Applies this function converting checked exceptions into
     * {@link RuntimeException}.
     *
     * @param value function input
     * @return mapped output
     * @throws RuntimeException wrapping any checked exception
     */
    default U apply(
            T value
    ) {
        try {
            return applyThrowing(value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
