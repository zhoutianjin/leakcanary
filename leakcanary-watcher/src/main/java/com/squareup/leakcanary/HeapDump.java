/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.leakcanary;

import java.io.File;
import java.io.Serializable;

import static com.squareup.leakcanary.Preconditions.checkNotNull;

/** Data structure holding information about a heap dump. */
public final class HeapDump implements Serializable {

  /** Receives a heap dump to analyze. */
  public interface Listener {
    Listener NONE = new Listener() {
      @Override public void analyze(HeapDump heapDump) {
      }
    };

    void analyze(HeapDump heapDump);
  }

  /** The heap dump file, which you might want to upload somewhere. */
  public final File heapDumpFile;

  /**
   * Key associated to the {@link KeyedWeakReference} used to detect the memory leak.
   * When analyzing a heap dump, search for all {@link KeyedWeakReference} instances, then open
   * the one that has its "key" field set to this value. Its "referent" field contains the
   * leaking object. Computing the shortest path to GC roots on that leaking object should enable
   * you to figure out the cause of the leak.
   */
  public final String referenceKey;

  /**
   * User defined name to help identify the leaking instance.
   */
  public final String referenceName;

  /** References that should be ignored when analyzing this heap dump. */
  public final ExcludedRefs excludedRefs;

  /** Time from the request to watch the reference until the GC was triggered. */
  public final long watchDurationMs;
  public final long gcDurationMs;
  public final long heapDumpDurationMs;
  public final boolean computeRetainedHeapSize;

  /**
   * Calls {@link #HeapDump(File, String, String, ExcludedRefs, boolean, Durations)}
   * with computeRetainedHeapSize set to true.
   *
   * @deprecated Use
   * {@link #HeapDump(File, String, String, ExcludedRefs, boolean, Durations)}  instead.
   */
  @Deprecated
  public HeapDump(File heapDumpFile, String referenceKey, String referenceName,
      ExcludedRefs excludedRefs, long watchDurationMs, long gcDurationMs, long heapDumpDurationMs) {
    this(heapDumpFile, referenceKey, referenceName, excludedRefs, true,
        new Durations(watchDurationMs, gcDurationMs, heapDumpDurationMs));
  }

  public HeapDump(File heapDumpFile, String referenceKey, String referenceName,
      ExcludedRefs excludedRefs, boolean computeRetainedHeapSize, Durations durations) {
    this.heapDumpFile = checkNotNull(heapDumpFile, "heapDumpFile");
    this.referenceKey = checkNotNull(referenceKey, "referenceKey");
    this.referenceName = checkNotNull(referenceName, "referenceName");
    this.excludedRefs = checkNotNull(excludedRefs, "excludedRefs");
    this.computeRetainedHeapSize = computeRetainedHeapSize;
    this.watchDurationMs = durations.watchDurationMs;
    this.gcDurationMs = durations.gcDurationMs;
    this.heapDumpDurationMs = durations.heapDumpDurationMs;
  }

  /**
   * A group of duration related parameters required when constructing a {@link HeapDump} instance.
   */
  public static final class Durations {
    final long watchDurationMs;
    final long gcDurationMs;
    final long heapDumpDurationMs;

    public Durations(long watchDurationMs, long gcDurationMs, long heapDumpDurationMs) {
      this.watchDurationMs = watchDurationMs;
      this.gcDurationMs = gcDurationMs;
      this.heapDumpDurationMs = heapDumpDurationMs;
    }
  }
}
