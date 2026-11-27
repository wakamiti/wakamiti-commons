/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.commons.lang;


/**
 * Functional interface similar to {@link Runnable} whose main operation can
 * throw checked exceptions and receive arguments.
 */
@FunctionalInterface
public interface ThrowableRunnable {

    /**
     * Runs this action and allows checked exceptions.
     *
     * @param arguments optional action arguments
     * @throws Exception when execution fails
     */
    void runThrowing(
            Object... arguments
    ) throws Exception;

    /**
     * Runs this action converting checked exceptions into
     * {@link RuntimeException}.
     *
     * @param arguments optional action arguments
     * @throws RuntimeException wrapping any checked exception
     */
    default void run(
            Object... arguments
    ) {
        try {
            runThrowing(arguments);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
