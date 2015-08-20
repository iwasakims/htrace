/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.htrace.core;

import java.io.Closeable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The collector within a process that is the destination of Spans when a trace is running.
 * {@code SpanReceiver} implementations are expected to provide a constructor with the signature
 * <p>
 * <pre>
 * <code>public SpanReceiverImpl(HTraceConfiguration)</code>
 * </pre>
 */
public abstract class SpanReceiver implements Closeable {
  /**
   * An ID which uniquely identifies this SpanReceiver.
   */
  private final long id;

  private static final AtomicLong HIGHEST_SPAN_RECEIVER_ID = new AtomicLong(0);

  /**
   * Get an ID uniquely identifying this SpanReceiver.
   */
  public final long getId() {
    return id;
  }

  protected SpanReceiver() {
    this.id = HIGHEST_SPAN_RECEIVER_ID.incrementAndGet();
  }

  /**
   * Called when a Span is stopped and can now be stored.
   */
  public abstract void receiveSpan(Span span);
}
