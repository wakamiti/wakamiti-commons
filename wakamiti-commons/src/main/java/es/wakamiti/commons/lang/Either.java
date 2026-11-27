/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.commons.lang;


import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;


/**
 * Container that models a primary value with an alternative fallback value.
 * <p>
 * Despite its name, this class does not enforce strict "left xor right" semantics.
 * Both sides may be present, both may be {@code null}, and each side is represented by a
 * supplier-based accessor.
 * </p>
 * <p>
 * Evaluation strategy is explicit through {@link Evaluation}:
 * </p>
 * <ul>
 *     <li>{@link Evaluation#MEMOIZED}: each side is evaluated at most once and cached,</li>
 *     <li>{@link Evaluation#LIVE}: each access re-evaluates the original supplier.</li>
 * </ul>
 * <p>
 * Backward-compatible factory methods named {@code of(...)} and {@code fallback(...)}
 * default to {@link Evaluation#MEMOIZED}.
 * </p>
 * <p>
 * Typical usage:
 * </p>
 * <ul>
 *     <li>provide a preferred value and a fallback,</li>
 *     <li>request the preferred value directly via {@link #value()},</li>
 *     <li>derive one side from the other only when needed via mapping methods.</li>
 * </ul>
 *
 * @param <T> primary value type
 * @param <U> fallback value type
 */
public class Either<T, U> {

    /**
     * Strategy controlling whether suppliers are memoized or re-evaluated on each access.
     */
    public enum Evaluation {
        MEMOIZED,
        LIVE
    }

    private final Supplier<T> value;
    private final Supplier<U> fallback;
    private final Evaluation evaluation;

    private Either(
            Supplier<T> value,
            Supplier<U> fallback,
            Evaluation evaluation
    ) {
        this.evaluation = Objects.requireNonNull(evaluation, "evaluation must not be null");
        this.value = wrap(Objects.requireNonNull(value, "value supplier must not be null"), this.evaluation);
        this.fallback = wrap(Objects.requireNonNull(fallback, "fallback supplier must not be null"), this.evaluation);
    }

    private static <X> Supplier<X> adapt(
            Supplier<? extends X> supplier
    ) {
        Objects.requireNonNull(supplier, "supplier must not be null");
        return supplier::get;
    }

    private static <X> Supplier<X> wrap(
            Supplier<X> supplier,
            Evaluation evaluation
    ) {
        if (evaluation == Evaluation.LIVE) return supplier;
        Lazy<X> cache = Lazy.of(supplier);
        return cache::get;
    }

    /**
     * Creates an instance from primary and fallback suppliers with an explicit strategy.
     * <p>
     * Use {@link Evaluation#MEMOIZED} for stable cached values or {@link Evaluation#LIVE}
     * for dynamic values that may change across accesses.
     * </p>
     *
     * @param value primary supplier
     * @param fallback fallback supplier
     * @param evaluation evaluation strategy
     * @param <T> primary type
     * @param <U> fallback type
     * @return new either-like container
     */
    public static <T, U> Either<T, U> of(
            Supplier<? extends T> value,
            Supplier<? extends U> fallback,
            Evaluation evaluation
    ) {
        return new Either<>(adapt(value), adapt(fallback), evaluation);
    }

    /**
     * Creates an instance from primary and fallback suppliers using memoized evaluation.
     * <p>
     * This method is kept for compatibility and delegates to
     * {@link #memoized(Supplier, Supplier)}.
     * </p>
     *
     * @param value primary supplier
     * @param fallback fallback supplier
     * @param <T> primary type
     * @param <U> fallback type
     * @return new either-like container
     */
    public static <T, U> Either<T, U> of(
            Supplier<? extends T> value,
            Supplier<? extends U> fallback
    ) {
        return memoized(value, fallback);
    }

    /**
     * Creates a memoized instance from primary and fallback suppliers.
     *
     * @param value primary supplier
     * @param fallback fallback supplier
     * @param <T> primary type
     * @param <U> fallback type
     * @return new either-like container
     */
    public static <T, U> Either<T, U> memoized(
            Supplier<? extends T> value,
            Supplier<? extends U> fallback
    ) {
        return of(value, fallback, Evaluation.MEMOIZED);
    }

    /**
     * Creates a live instance from primary and fallback suppliers.
     *
     * @param value primary supplier
     * @param fallback fallback supplier
     * @param <T> primary type
     * @param <U> fallback type
     * @return new either-like container
     */
    public static <T, U> Either<T, U> live(
            Supplier<? extends T> value,
            Supplier<? extends U> fallback
    ) {
        return of(value, fallback, Evaluation.LIVE);
    }

    /**
     * Creates an instance with a primary supplier and a {@code null} fallback using an
     * explicit strategy.
     *
     * @param value primary supplier
     * @param evaluation evaluation strategy
     * @param <T> primary type
     * @param <U> fallback type
     * @return new either-like container
     */
    public static <T, U> Either<T, U> of(
            Supplier<? extends T> value,
            Evaluation evaluation
    ) {
        return of(value, () -> null, evaluation);
    }

    /**
     * Creates an instance with a primary supplier and a {@code null} fallback.
     * <p>
     * This method is kept for compatibility and delegates to {@link #memoized(Supplier)}.
     * </p>
     *
     * @param value primary supplier
     * @param <T> primary type
     * @param <U> fallback type
     * @return new either-like container
     */
    public static <T, U> Either<T, U> of(
            Supplier<? extends T> value
    ) {
        return memoized(value);
    }

    /**
     * Creates a memoized instance with a primary supplier and a {@code null} fallback.
     *
     * @param value primary supplier
     * @param <T> primary type
     * @param <U> fallback type
     * @return new either-like container
     */
    public static <T, U> Either<T, U> memoized(
            Supplier<? extends T> value
    ) {
        return of(value, Evaluation.MEMOIZED);
    }

    /**
     * Creates a live instance with a primary supplier and a {@code null} fallback.
     *
     * @param value primary supplier
     * @param <T> primary type
     * @param <U> fallback type
     * @return new either-like container
     */
    public static <T, U> Either<T, U> live(
            Supplier<? extends T> value
    ) {
        return of(value, Evaluation.LIVE);
    }

    /**
     * Creates an instance with a fallback supplier and a {@code null} primary value using
     * an explicit strategy.
     *
     * @param fallback fallback supplier
     * @param evaluation evaluation strategy
     * @param <T> primary type
     * @param <U> fallback type
     * @return new either-like container
     */
    public static <T, U> Either<T, U> fallback(
            Supplier<? extends U> fallback,
            Evaluation evaluation
    ) {
        return of(() -> null, fallback, evaluation);
    }

    /**
     * Creates an instance with a fallback supplier and a {@code null} primary value.
     * <p>
     * This method is kept for compatibility and delegates to
     * {@link #memoizedFallback(Supplier)}.
     * </p>
     *
     * @param fallback fallback supplier
     * @param <T> primary type
     * @param <U> fallback type
     * @return new either-like container
     */
    public static <T, U> Either<T, U> fallback(
            Supplier<? extends U> fallback
    ) {
        return memoizedFallback(fallback);
    }

    /**
     * Creates a memoized instance with a fallback supplier and a {@code null} primary
     * value.
     *
     * @param fallback fallback supplier
     * @param <T> primary type
     * @param <U> fallback type
     * @return new either-like container
     */
    public static <T, U> Either<T, U> memoizedFallback(
            Supplier<? extends U> fallback
    ) {
        return fallback(fallback, Evaluation.MEMOIZED);
    }

    /**
     * Creates a live instance with a fallback supplier and a {@code null} primary value.
     *
     * @param fallback fallback supplier
     * @param <T> primary type
     * @param <U> fallback type
     * @return new either-like container
     */
    public static <T, U> Either<T, U> liveFallback(
            Supplier<? extends U> fallback
    ) {
        return fallback(fallback, Evaluation.LIVE);
    }

    /**
     * Creates an instance from eager values.
     *
     * @param value primary value
     * @param fallback fallback value
     * @param <T> primary type
     * @param <U> fallback type
     * @return new either-like container
     */
    public static <T, U> Either<T, U> of(
            T value,
            U fallback
    ) {
        return memoized(value, fallback);
    }

    /**
     * Creates a memoized instance from eager values.
     *
     * @param value primary value
     * @param fallback fallback value
     * @param <T> primary type
     * @param <U> fallback type
     * @return new either-like container
     */
    public static <T, U> Either<T, U> memoized(
            T value,
            U fallback
    ) {
        return memoized(() -> value, () -> fallback);
    }

    /**
     * Creates a live instance from eager values.
     * <p>
     * For eager immutable values, live and memoized behavior are effectively equivalent.
     * </p>
     *
     * @param value primary value
     * @param fallback fallback value
     * @param <T> primary type
     * @param <U> fallback type
     * @return new either-like container
     */
    public static <T, U> Either<T, U> live(
            T value,
            U fallback
    ) {
        return live(() -> value, () -> fallback);
    }

    /**
     * Creates an instance with an eager primary value and a {@code null} fallback.
     *
     * @param value primary value
     * @param <T> primary type
     * @param <U> fallback type
     * @return new either-like container
     */
    public static <T, U> Either<T, U> of(
            T value
    ) {
        return memoized(value);
    }

    /**
     * Creates a memoized instance with an eager primary value and a {@code null}
     * fallback.
     *
     * @param value primary value
     * @param <T> primary type
     * @param <U> fallback type
     * @return new either-like container
     */
    public static <T, U> Either<T, U> memoized(
            T value
    ) {
        return memoized(() -> value);
    }

    /**
     * Creates a live instance with an eager primary value and a {@code null} fallback.
     *
     * @param value primary value
     * @param <T> primary type
     * @param <U> fallback type
     * @return new either-like container
     */
    public static <T, U> Either<T, U> live(
            T value
    ) {
        return live(() -> value);
    }

    /**
     * Creates an instance with an eager fallback value and a {@code null} primary value.
     *
     * @param fallback fallback value
     * @param <T> primary type
     * @param <U> fallback type
     * @return new either-like container
     */
    public static <T, U> Either<T, U> fallback(
            U fallback
    ) {
        return memoizedFallback(fallback);
    }

    /**
     * Creates a memoized instance with an eager fallback value and a {@code null}
     * primary value.
     *
     * @param fallback fallback value
     * @param <T> primary type
     * @param <U> fallback type
     * @return new either-like container
     */
    public static <T, U> Either<T, U> memoizedFallback(
            U fallback
    ) {
        return memoizedFallback(() -> fallback);
    }

    /**
     * Creates a live instance with an eager fallback value and a {@code null} primary
     * value.
     *
     * @param fallback fallback value
     * @param <T> primary type
     * @param <U> fallback type
     * @return new either-like container
     */
    public static <T, U> Either<T, U> liveFallback(
            U fallback
    ) {
        return liveFallback(() -> fallback);
    }

    /**
     * Returns the configured evaluation strategy.
     *
     * @return evaluation strategy
     */
    public Evaluation evaluation() {
        return evaluation;
    }

    /**
     * Returns the primary value wrapped in an {@link Optional}.
     *
     * @return optional primary value
     */
    public Optional<T> value() {
        return Optional.ofNullable(value.get());
    }

    /**
     * Returns the primary value when present; otherwise computes it from fallback.
     * <p>
     * The mapper is invoked only when the primary value is {@code null}.
     * </p>
     *
     * @param mapper function used to derive primary value from fallback
     * @return primary value or mapped fallback
     */
    public T value(
            Function<U, T> mapper
    ) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        T primary = value.get();
        if (primary != null) return primary;
        return mapper.apply(fallback.get());
    }

    /**
     * Returns the fallback value (possibly {@code null}).
     *
     * @return fallback value
     */
    public U fallback() {
        return fallback.get();
    }

    /**
     * Returns the fallback value when present; otherwise computes it from primary value.
     * <p>
     * The mapper is invoked only when the fallback value is {@code null}.
     * </p>
     *
     * @param mapper function used to derive fallback value from primary
     * @return fallback value or mapped primary value
     */
    public U fallback(
            Function<T, U> mapper
    ) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        U alternative = fallback.get();
        if (alternative != null) return alternative;
        return mapper.apply(value.get());
    }

}
