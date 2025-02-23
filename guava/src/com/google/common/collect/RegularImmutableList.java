/*
 * Copyright (C) 2009 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.VisibleForTesting;
import java.util.Spliterator;
import java.util.Spliterators;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.signedness.qual.UnknownSignedness;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.framework.qual.AnnotatedFor;

/**
 * Implementation of {@link ImmutableList} backed by a simple array.
 *
 * @author Kevin Bourrillion
 */
@AnnotatedFor({"nullness"})
@GwtCompatible(serializable = true, emulated = true)
@SuppressWarnings("serial") // uses writeReplace(), not default serialization
@ElementTypesAreNonnullByDefault
class RegularImmutableList<E> extends ImmutableList<E> {
  static final ImmutableList<Object> EMPTY = new RegularImmutableList<>(new Object[0]);

  @VisibleForTesting final transient Object[] array;

  RegularImmutableList(Object[] array) {
    this.array = array;
  }

  @Pure
  @Override
  public @NonNegative int size() {
    return array.length;
  }

  @Override
  boolean isPartialView() {
    return false;
  }

  @Override
  Object[] internalArray() {
    return array;
  }

  @Override
  int internalArrayStart() {
    return 0;
  }

  @Override
  int internalArrayEnd() {
    return array.length;
  }

  @Override
  int copyIntoArray(@Nullable Object[] dst, int dstOff) {
    System.arraycopy(array, 0, dst, dstOff, array.length);
    return dstOff + array.length;
  }

  // The fake cast to E is safe because the creation methods only allow E's
  @Override
  @SuppressWarnings("unchecked")
  public E get(int index) {
    return (E) array[index];
  }

  @SuppressWarnings("unchecked")
  @Override
  public UnmodifiableListIterator<E> listIterator(int index) {
    // for performance
    // The fake cast to E is safe because the creation methods only allow E's
    return (UnmodifiableListIterator<E>) Iterators.forArray(array, 0, array.length, index);
  }

  @Override
  public Spliterator<E> spliterator() {
    return Spliterators.spliterator(array, SPLITERATOR_CHARACTERISTICS);
  }

  // TODO(lowasser): benchmark optimizations for equals() and see if they're worthwhile

@Override
public boolean contains(@Nullable @UnknownSignedness Object arg0) { return super.contains(arg0); }

@Pure
@Override
public boolean equals(@Nullable @UnknownSignedness Object arg0) { return super.equals(arg0); }

@Override
public int indexOf(@Nullable @UnknownSignedness Object arg0) { return super.indexOf(arg0); }

@Override
public int lastIndexOf(@Nullable @UnknownSignedness Object arg0) { return super.lastIndexOf(arg0); }

@SideEffectFree
@Override
public ImmutableList<E> subList(int arg0, int arg1) { return super.subList(arg0, arg1); }

@Pure
@Override
public int hashCode(@UnknownSignedness RegularImmutableList<E> this) { return super.hashCode(); }

@Pure
@Override
public String toString() { return super.toString(); }
}
