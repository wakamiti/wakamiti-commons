/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.commons.lang;


import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;


/**
 * Thread-safe lazy holder that computes a value on first access and caches the result.
 * <p>
 * The supplier is invoked at most once between calls to {@link #reset()}. The computed
 * value is cached even when it is {@code null}, so repeated calls to {@link #get()} do
 * not recompute it.
 * </p>
 * <p>
 * This type is intentionally minimal:
 * </p>
 * <ul>
 *     <li>it guarantees lazy initialization,</li>
 *     <li>it guarantees cached reads after initialization,</li>
 *     <li>it allows explicit cache invalidation via {@link #reset()}.</li>
 * </ul>
 * <p>
 * A mapped lazy created through {@link #map(Function)} depends on this instance and
 * therefore reuses this instance cache.
 * </p>
 *
 * @param <T> type of the lazily computed value
 */
public class Lazy<T> {

    private final Supplier<? extends T> supplier;
    private T instance;
    private volatile boolean initialized;

    private Lazy(
            Supplier<? extends T> supplier
    ) {
        this.supplier = Objects.requireNonNull(supplier, "supplier must not be null");
    }

    /**
     * Creates a lazy value from a supplier.
     * <p>
     * The supplier is not executed at creation time; it is executed when {@link #get()}
     * is called for the first time (or after a {@link #reset()}).
     * </p>
     *
     * @param supplier supplier used to create the value lazily
     * @param <T> value type
     * @return a lazy wrapper around the supplier
     */
    public static <T> Lazy<T> of(
            Supplier<? extends T> supplier
    ) {
        return new Lazy<>(supplier);
    }

    /**
     * Creates a lazy value from an optional supplier.
     * <p>
     * The resulting lazy throws {@link NoSuchElementException} when the optional is empty.
     * Returning {@code null} instead of an {@link Optional} is treated as an error.
     * </p>
     *
     * @param supplier supplier returning an optional value
     * @param <T> value type
     * @return a lazy wrapper that unwraps the optional value
     */
    public static <T> Lazy<T> ofOptional(
            Supplier<Optional<T>> supplier
    ) {
        Objects.requireNonNull(supplier, "supplier must not be null");
        return new Lazy<>(() -> Objects
                .requireNonNull(supplier.get(), "supplier returned null Optional")
                .orElseThrow(() -> new NoSuchElementException("Optional supplier produced an empty value")));
    }

    /**
     * Returns the cached value, computing it when needed.
     * <p>
     * The supplier is evaluated once per lifecycle (from creation or last reset). The
     * initialization path is synchronized; already initialized reads are non-blocking.
     * </p>
     *
     * @return cached or freshly computed value (possibly {@code null})
     */
    public T get() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    instance = supplier.get();
                    initialized = true;
                }
            }
        }
        return instance;
    }


    /**
     * Clears the cached value so it is recomputed on next {@link #get()} call.
     * <p>
     * This method is synchronized to avoid races with concurrent initialization.
     * </p>
     */
    public synchronized void reset() {
        instance = null;
        initialized = false;
    }


    /**
     * Maps this lazy value into another lazy value.
     * <p>
     * The returned lazy does not evaluate eagerly. When evaluated, it first reads this
     * lazy through {@link #get()}, then applies the mapping function.
     * </p>
     *
     * @param function mapper applied to this lazy value
     * @param <U> mapped value type
     * @return mapped lazy value
     */
    public <U> Lazy<U> map(
            Function<T, U> function
    ) {
        Objects.requireNonNull(function, "function must not be null");
        return new Lazy<>(() -> function.apply(get()));
    }

}
