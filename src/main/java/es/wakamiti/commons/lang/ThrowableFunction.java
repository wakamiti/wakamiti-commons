/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.commons.lang;


@FunctionalInterface
public interface ThrowableFunction<T, U> {

    U applyThrowing(
            T value
    ) throws Exception;

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
