/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.commons.lang;


import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;


/**
 * Utility class with small functional helpers for null-safe transformation,
 * casting and list operations.
 */
public class Functions {

    /**
     * Utility class; not meant to be instantiated.
     */
    private Functions() {
    }

    /**
     * Applies a mapper when the value is not {@code null}.
     *
     * @param value value to map
     * @param mapper transformation function
     * @param <T> input type
     * @param <U> output type
     * @return mapped value or {@code null} when input is {@code null}
     */
    public static <T, U> U let(
            T value,
            Function<T, U> mapper
    ) {
        if (value == null) return null;
        return mapper.apply(value);
    }

    /**
     * Returns the value when present, otherwise returns a fallback value.
     *
     * @param value preferred value
     * @param fallback fallback value
     * @param <T> value type
     * @return preferred value when non-null, otherwise fallback
     */
    public static <T> T or(
            T value,
            T fallback
    ) {
        return value == null ? fallback : value;
    }

    /**
     * Returns the value when present, otherwise returns a lazily computed fallback.
     *
     * @param value preferred value
     * @param fallback fallback supplier
     * @param <T> value type
     * @return preferred value when non-null, otherwise supplier result
     */
    public static <T> T or(
            T value,
            Supplier<T> fallback
    ) {
        return value == null ? fallback.get() : value;
    }

    /**
     * Executes an action with the given value and returns the original value.
     *
     * @param value value passed to the action
     * @param action side-effect action
     * @param <T> value type
     * @return original value or {@code null} when input is {@code null}
     */
    public static <T> T also(
            T value,
            Consumer<T> action
    ) {
        if (value == null) return null;
        action.accept(value);
        return value;
    }

    /**
     * Executes an action only when the value is not {@code null}.
     *
     * @param value value to inspect
     * @param action action executed for non-null values
     * @param <T> value type
     */
    public static <T> void ifPresent(
            T value,
            Consumer<T> action
    ) {
        if (value != null) action.accept(value);
    }

    /**
     * Performs a safe cast, returning {@code null} when the value does not match the type.
     *
     * @param value value to cast
     * @param type expected type
     * @param <T> source type
     * @param <U> target type
     * @return cast value or {@code null} when cast is not possible
     */
    public static <T, U extends T> U cast(
            T value,
            Class<U> type
    ) {
        if (value == null) return null;
        return type.isInstance(value) ? type.cast(value) : null;
    }

    /**
     * Returns the first element of a list.
     *
     * @param list source list
     * @param <T> element type
     * @return first element or {@code null} for empty lists
     */
    public static <T> T first(
            List<T> list
    ) {
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Maps each list element with access to its index.
     *
     * @param list source list
     * @param mapper index-aware mapper
     * @param <T> source element type
     * @param <U> mapped element type
     * @return immutable mapped list, or empty list when input list is {@code null}
     */
    public static <T, U> List<U> indexMapped(
            List<T> list,
            BiFunction<Integer, T, U> mapper
    ) {
        if (list == null) return List.of();
        List<U> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            result.add(mapper.apply(i, list.get(i)));
        }
        return List.copyOf(result);
    }

    /**
     * Maps each list element.
     *
     * @param list source list
     * @param mapper element mapper
     * @param <T> source element type
     * @param <U> mapped element type
     * @return mapped list, or empty list when input list is {@code null}
     */
    public static <T, U> List<U> mapped(
            List<T> list,
            Function<T, U> mapper
    ) {
        if (list == null) return List.of();
        return list.stream().map(mapper).toList();
    }

    /**
     * Concatenates two lists preserving order.
     *
     * @param list1 first list
     * @param list2 second list
     * @param <T> element type
     * @return combined list
     */
    public static <T> List<T> concat(
            List<T> list1,
            List<T> list2
    ) {
        return Stream.concat(list1.stream(), list2.stream()).toList();
    }

}
