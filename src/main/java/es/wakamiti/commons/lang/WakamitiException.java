/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.commons.lang;


import java.io.Serial;
import java.util.Arrays;


/**
 * Base unchecked exception for runtime errors inside Wakamiti.
 * <p>
 * This exception supports lightweight `{}` message formatting through the
 * varargs constructor:
 * <pre>{@code
 * throw new WakamitiException("Cannot read resource {}", path);
 * }</pre>
 */
public class WakamitiException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 6196016511564036001L;

    /**
     * Creates an exception with message.
     *
     * @param message error message
     */
    public WakamitiException(
            String message
    ) {
        super(message);
    }

    /**
     * Creates an exception wrapping another throwable.
     *
     * @param throwable source throwable
     */
    public WakamitiException(
            Throwable throwable
    ) {
        super(throwable.getMessage(), throwable.getCause());
        setStackTrace(throwable.getStackTrace());
    }

    /**
     * Creates an exception with message and cause.
     *
     * @param message error message
     * @param throwable root cause
     */
    public WakamitiException(
            String message,
            Throwable throwable
    ) {
        super(message, throwable);
    }

    /**
     * Creates an exception using message template formatting.
     * <p>
     * Template placeholders {@code {}} are converted to {@code %s}. If the
     * last argument is a {@link Throwable}, it is used as exception cause.
     *
     * @param message message template
     * @param args formatting arguments, optionally ending with a throwable
     */
    public WakamitiException(
            String message,
            Object... args
    ) {
        super(format(message, argsWithoutThrowable(args)), throwableCandidate(args));
    }

    protected static String format(
            String message,
            Object... args
    ) {
        Object[] safeArgs = args == null ? new Object[0] : args;
        return message.replace("{}", "%s").formatted(safeArgs);
    }


    protected static Object[] argsWithoutThrowable(
            Object[] args
    ) {
        if (args == null) {
            return new Object[0];
        }
        return throwableCandidate(args) == null ? args : Arrays.copyOf(args, args.length - 1);
    }

    protected static Throwable throwableCandidate(
            Object[] args
    ) {
        if (args == null || args.length == 0) {
            return null;
        }
        Object last = args[args.length - 1];
        return last instanceof Throwable throwable ? throwable : null;
    }

}
