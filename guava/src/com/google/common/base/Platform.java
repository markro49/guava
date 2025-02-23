/*
 * Copyright (C) 2009 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.ServiceConfigurationError;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.annotation.CheckForNull;
import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.AnnotatedFor;

/**
 * Methods factored out so that they can be emulated differently in GWT.
 *
 * @author Jesse Wilson
 */
@AnnotatedFor({"nullness"})
@GwtCompatible(emulated = true)
@ElementTypesAreNonnullByDefault
final class Platform {
  private static final Logger logger = Logger.getLogger(Platform.class.getName());
  private static final PatternCompiler patternCompiler = loadPatternCompiler();

  private Platform() {}

  /** Calls {@link System#nanoTime()}. */
  @SuppressWarnings("GoodTime") // reading system time without TimeSource
  static long systemNanoTime() {
    return System.nanoTime();
  }

  static CharMatcher precomputeCharMatcher(CharMatcher matcher) {
    return matcher.precomputedInternal();
  }

  @SuppressWarnings("nullness:argument") // ref.get returns null in case the object
  // that it refers to is cleared or garbage collected which is unlikely during normal program
  // execution. Optional.of always return a non-null value for non-null argument.
  static <T extends Enum<T>> Optional<T> getEnumIfPresent(Class<T> enumClass, String value) {
    WeakReference<? extends Enum<?>> ref = Enums.getEnumConstants(enumClass).get(value);
    return ref == null ? Optional.<T>absent() : Optional.of(enumClass.cast(ref.get()));
  }

  static String formatCompact4Digits(double value) {
    return String.format(Locale.ROOT, "%.4g", value);
  }

  static boolean stringIsNullOrEmpty(@CheckForNull String string) {
    return string == null || string.isEmpty();
  }

  /**
   * Returns the string if it is not null, or an empty string otherwise.
   *
   * @param string the string to test and possibly return
   * @return {@code string} if it is not null; {@code ""} otherwise
   */
  static String nullToEmpty(@CheckForNull String string) {
    return (string == null) ? "" : string;
  }

  /**
   * Returns the string if it is not empty, or a null string otherwise.
   *
   * @param string the string to test and possibly return
   * @return {@code string} if it is not empty; {@code null} otherwise
   */
  @CheckForNull
  static String emptyToNull(@CheckForNull String string) {
    return stringIsNullOrEmpty(string) ? null : string;
  }

  static CommonPattern compilePattern(String pattern) {
    Preconditions.checkNotNull(pattern);
    return patternCompiler.compile(pattern);
  }

  static boolean patternCompilerIsPcreLike() {
    return patternCompiler.isPcreLike();
  }

  private static PatternCompiler loadPatternCompiler() {
    return new JdkPatternCompiler();
  }

  private static void logPatternCompilerError(ServiceConfigurationError e) {
    logger.log(Level.WARNING, "Error loading regex compiler, falling back to next option", e);
  }

  private static final class JdkPatternCompiler implements PatternCompiler {
    @Override
    public CommonPattern compile(String pattern) {
      return new JdkPattern(Pattern.compile(pattern));
    }

    @Override
    public boolean isPcreLike() {
      return true;
    }
  }

  static void checkGwtRpcEnabled() {
    String propertyName = "guava.gwt.emergency_reenable_rpc";

    if (!Boolean.parseBoolean(System.getProperty(propertyName, "false"))) {
      throw new UnsupportedOperationException(
          Strings.lenientFormat(
              "We are removing GWT-RPC support for Guava types. You can temporarily reenable"
                  + " support by setting the system property %s to true. For more about system"
                  + " properties, see %s. For more about Guava's GWT-RPC support, see %s.",
              propertyName,
              "https://stackoverflow.com/q/5189914/28465",
              "https://groups.google.com/d/msg/guava-announce/zHZTFg7YF3o/rQNnwdHeEwAJ"));
    }
    logger.log(
        java.util.logging.Level.WARNING,
        "Later in 2020, we will remove GWT-RPC support for Guava types. You are seeing this"
            + " warning because you are sending a Guava type over GWT-RPC, which will break. You"
            + " can identify which type by looking at the class name in the attached stack trace.",
        new Throwable());
  }
}
