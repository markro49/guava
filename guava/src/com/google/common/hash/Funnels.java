/*
 * Copyright (C) 2011 The Guava Authors
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

package com.google.common.hash;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import javax.annotation.CheckForNull;
import org.checkerframework.checker.index.qual.IndexOrHigh;
import org.checkerframework.checker.index.qual.LTLengthOf;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.signedness.qual.PolySigned;
import org.checkerframework.checker.signedness.qual.UnknownSignedness;
import org.checkerframework.common.value.qual.MinLen;

/**
 * Funnels for common types. All implementations are serializable.
 *
 * @author Dimitris Andreou
 * @since 11.0
 */
@Beta
@ElementTypesAreNonnullByDefault
public final class Funnels {
  private Funnels() {}

  /** Returns a funnel that extracts the bytes from a {@code byte} array. */
  public static Funnel<byte[]> byteArrayFunnel() {
    return ByteArrayFunnel.INSTANCE;
  }

  private enum ByteArrayFunnel implements Funnel<byte[]> {
    INSTANCE;

    @Override
    @SuppressWarnings("value:argument") // 'from' should be annotated @MinLen(1)
    public void funnel(byte [] from, PrimitiveSink into) {
      into.putBytes(from);
    }

    @Override
    public String toString() {
      return "Funnels.byteArrayFunnel()";
    }
  }

  /**
   * Returns a funnel that extracts the characters from a {@code CharSequence}, a character at a
   * time, without performing any encoding. If you need to use a specific encoding, use {@link
   * Funnels#stringFunnel(Charset)} instead.
   *
   * @since 15.0 (since 11.0 as {@code Funnels.stringFunnel()}.
   */
  public static Funnel<CharSequence> unencodedCharsFunnel() {
    return UnencodedCharsFunnel.INSTANCE;
  }

  private enum UnencodedCharsFunnel implements Funnel<CharSequence> {
    INSTANCE;

    @Override
    public void funnel(CharSequence from, PrimitiveSink into) {
      into.putUnencodedChars(from);
    }

    @Override
    public String toString() {
      return "Funnels.unencodedCharsFunnel()";
    }
  }

  /**
   * Returns a funnel that encodes the characters of a {@code CharSequence} with the specified
   * {@code Charset}.
   *
   * @since 15.0
   */
  public static Funnel<CharSequence> stringFunnel(Charset charset) {
    return new StringCharsetFunnel(charset);
  }

  private static class StringCharsetFunnel implements Funnel<CharSequence>, Serializable {
    private final Charset charset;

    StringCharsetFunnel(Charset charset) {
      this.charset = Preconditions.checkNotNull(charset);
    }

    @Override
    @SuppressWarnings("value:argument") // 'from' should be annotated @MinLen(1)
    public void funnel(CharSequence from, PrimitiveSink into) {
      into.putString(from, charset);
    }

    @Override
    public String toString() {
      return "Funnels.stringFunnel(" + charset.name() + ")";
    }

    @Override
    public boolean equals(@CheckForNull Object o) {
      if (o instanceof StringCharsetFunnel) {
        StringCharsetFunnel funnel = (StringCharsetFunnel) o;
        return this.charset.equals(funnel.charset);
      }
      return false;
    }

    @Override
    public int hashCode(@UnknownSignedness StringCharsetFunnel this) {
      return StringCharsetFunnel.class.hashCode() ^ charset.hashCode();
    }

    Object writeReplace() {
      return new SerializedForm(charset);
    }

    private static class SerializedForm implements Serializable {
      private final String charsetCanonicalName;

      SerializedForm(Charset charset) {
        this.charsetCanonicalName = charset.name();
      }

      private Object readResolve() {
        return stringFunnel(Charset.forName(charsetCanonicalName));
      }

      private static final long serialVersionUID = 0;
    }
  }

  /**
   * Returns a funnel for integers.
   *
   * @since 13.0
   */
  public static Funnel<Integer> integerFunnel() {
    return IntegerFunnel.INSTANCE;
  }

  private enum IntegerFunnel implements Funnel<Integer> {
    INSTANCE;

    @Override
    public void funnel(Integer from, PrimitiveSink into) {
      into.putInt(from);
    }

    @Override
    public String toString() {
      return "Funnels.integerFunnel()";
    }
  }

  /**
   * Returns a funnel that processes an {@code Iterable} by funneling its elements in iteration
   * order with the specified funnel. No separators are added between the elements.
   *
   * @since 15.0
   */
  public static <E extends @Nullable Object> Funnel<Iterable<? extends E>> sequentialFunnel(
      Funnel<E> elementFunnel) {
    return new SequentialFunnel<>(elementFunnel);
  }

  private static class SequentialFunnel<E extends @Nullable Object>
      implements Funnel<Iterable<? extends E>>, Serializable {
    private final Funnel<E> elementFunnel;

    SequentialFunnel(Funnel<E> elementFunnel) {
      this.elementFunnel = Preconditions.checkNotNull(elementFunnel);
    }

    @Override
    public void funnel(Iterable<? extends E> from, PrimitiveSink into) {
      for (E e : from) {
        elementFunnel.funnel(e, into);
      }
    }

    @Override
    public String toString() {
      return "Funnels.sequentialFunnel(" + elementFunnel + ")";
    }

    @Override
    public boolean equals(@CheckForNull Object o) {
      if (o instanceof SequentialFunnel) {
        SequentialFunnel<?> funnel = (SequentialFunnel<?>) o;
        return elementFunnel.equals(funnel.elementFunnel);
      }
      return false;
    }

    @Override
    public int hashCode(@UnknownSignedness SequentialFunnel<E> this) {
      return SequentialFunnel.class.hashCode() ^ elementFunnel.hashCode();
    }
  }

  /**
   * Returns a funnel for longs.
   *
   * @since 13.0
   */
  public static Funnel<Long> longFunnel() {
    return LongFunnel.INSTANCE;
  }

  private enum LongFunnel implements Funnel<Long> {
    INSTANCE;

    @Override
    public void funnel(Long from, PrimitiveSink into) {
      into.putLong(from);
    }

    @Override
    public String toString() {
      return "Funnels.longFunnel()";
    }
  }

  /**
   * Wraps a {@code PrimitiveSink} as an {@link OutputStream}, so it is easy to {@link Funnel#funnel
   * funnel} an object to a {@code PrimitiveSink} if there is already a way to write the contents of
   * the object to an {@code OutputStream}.
   *
   * <p>The {@code close} and {@code flush} methods of the returned {@code OutputStream} do nothing,
   * and no method throws {@code IOException}.
   *
   * @since 13.0
   */
  public static OutputStream asOutputStream(PrimitiveSink sink) {
    return new SinkAsStream(sink);
  }

  private static class SinkAsStream extends OutputStream {
    final PrimitiveSink sink;

    SinkAsStream(PrimitiveSink sink) {
      this.sink = Preconditions.checkNotNull(sink);
    }

    @Override
    public void write(@PolySigned int b) {
      sink.putByte((byte) b);
    }

    @SuppressWarnings("override.param") // OutputStream#write() should be annotated as void write(byte @MinLen(1)[] bytes)
    @Override
    public void write(byte @MinLen(1)[] bytes) {
      sink.putBytes(bytes);
    }

    @Override
    public void write(@PolySigned byte[] bytes, @IndexOrHigh({"#1"}) int off, @LTLengthOf(value={"#1"}, offset={"#2 - 1"}) @NonNegative int len) throws IOException {
      sink.putBytes(bytes, off, len);
    }

    @Override
    public String toString() {
      return "Funnels.asOutputStream(" + sink + ")";
    }
  }
}
