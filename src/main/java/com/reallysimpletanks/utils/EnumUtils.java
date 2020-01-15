/*
(The MIT License)

Copyright (c) 2019 SilentChaos512

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the 'Software'), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

This class was taken from https://github.com/SilentChaos512/silent-utils
 */
package com.reallysimpletanks.utils;


import javax.annotation.Nullable;
import java.util.function.Function;

public final class EnumUtils {
    private EnumUtils() {throw new IllegalAccessError("Utility class");}

    /**
     * Get an enum constant that produces the given value from {@code getter}. Returns the first
     * result, even if multiple constants would match.
     *
     * @return The first match, if one exists, or defaultValue otherwise.
     */
    public static <E extends Enum<E>> E byIndex(int value, E defaultValue, Function<E, Integer> getter) {
        for (E e : defaultValue.getDeclaringClass().getEnumConstants()) {
            if (getter.apply(e) == value) {
                return e;
            }
        }
        return defaultValue;
    }

    /**
     * Get the enum constant with the given name (ignoring case), or {@code defaultValue} if no
     * match is found.
     *
     * @return The enum constant with the given name, or {@code defaultValue} if invalid.
     */
    public static <E extends Enum<E>> E byName(String name, E defaultValue) {
        for (E e : defaultValue.getDeclaringClass().getEnumConstants()) {
            if (e.name().equalsIgnoreCase(name)) {
                return e;
            }
        }
        return defaultValue;
    }

    /**
     * Get the enum constant with the given ordinal, or {@code defaultValue} if out-of-bounds.
     *
     * @return The enum constant with the given ordinal, or {@code defaultValue} if ordinal is not
     * valid.
     */
    public static <E extends Enum<E>> E byOrdinal(int ordinal, E defaultValue) {
        E[] enumConstants = defaultValue.getDeclaringClass().getEnumConstants();
        if (ordinal >= 0 && ordinal < enumConstants.length) {
            return enumConstants[ordinal];
        }
        return defaultValue;
    }

    /**
     * Check the object is a valid constant of the enum class, ignoring case.
     *
     * @return True if obj is non-null and {@code obj.toString()} matches a constant in the enum
     * class (ignoring case), false otherwise.
     */
    public static <E extends Enum<E>> boolean validate(@Nullable Object obj, Class<E> enumClass) {
        if (obj != null) {
            for (E e : enumClass.getEnumConstants()) {
                if (e.name().equalsIgnoreCase(obj.toString())) {
                    return true;
                }
            }
        }
        return false;
    }
}