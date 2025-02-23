/*
 * Copyright (C) 2007 The Guava Authors
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

package com.google.common.io;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.GwtIncompatible;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import javax.annotation.CheckForNull;
import org.checkerframework.checker.index.qual.GTENegativeOne;
import org.checkerframework.checker.index.qual.IndexOrHigh;
import org.checkerframework.checker.index.qual.LTEqLengthOf;
import org.checkerframework.checker.index.qual.LTLengthOf;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An {@link InputStream} that concatenates multiple substreams. At most one stream will be open at
 * a time.
 *
 * @author Chris Nokleberg
 * @since 1.0
 */
@GwtIncompatible
@ElementTypesAreNonnullByDefault
final class MultiInputStream extends InputStream {

  private Iterator<? extends ByteSource> it;
  @CheckForNull private InputStream in;

  /**
   * Creates a new instance.
   *
   * @param it an iterator of I/O suppliers that will provide each substream
   */
  public MultiInputStream(Iterator<? extends ByteSource> it) throws IOException {
    this.it = checkNotNull(it);
    advance();
  }

  @Override
  public void close() throws IOException {
    if (in != null) {
      try {
        in.close();
      } finally {
        in = null;
      }
    }
  }

  /** Closes the current input stream and opens the next one, if any. */
  private void advance() throws IOException {
    close();
    if (it.hasNext()) {
      in = it.next().openStream();
    }
  }

  @Override
  public @NonNegative int available() throws IOException {
    if (in == null) {
      return 0;
    }
    return in.available();
  }

  @Override
  public boolean markSupported() {
    return false;
  }

  @Override
  public @GTENegativeOne int read() throws IOException {
    while (in != null) {
      int result = in.read();
      if (result != -1) {
        return result;
      }
      advance();
    }
    return -1;
  }

  @Override
  public @GTENegativeOne @LTEqLengthOf("#1") int read(byte [] b, @IndexOrHigh("#1") int off, @NonNegative @LTLengthOf(value = "#1", offset = "#2 - 1") int len) throws IOException {
    checkNotNull(b);
    while (in != null) {
      int result = in.read(b, off, len);
      if (result != -1) {
        return result;
      }
      advance();
    }
    return -1;
  }

  @Override
  public @NonNegative long skip(long n) throws IOException {
    if (in == null || n <= 0) {
      return 0;
    }
    long result = in.skip(n);
    if (result != 0) {
      return result;
    }
    if (read() == -1) {
      return 0;
    }
    return 1 + in.skip(n - 1);
  }
}
