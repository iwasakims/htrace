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
package org.apache.htrace;

import java.io.Closeable;
import java.lang.Thread;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TraceScope implements Closeable {
  private static final Log LOG = LogFactory.getLog(TraceScope.class);

  /**
   * the span for this scope
   */
  private final Span span;

  /**
   * the span that was "current" before this scope was entered
   */
  private final Span savedSpan;

  private boolean detached = false;

  TraceScope(Span span, Span saved) {
    this.span = span;
    this.savedSpan = saved;
  }

  public Span getSpan() {
    return span;
  }

  /**
   * Remove this span as the current thread, but don't stop it yet or
   * send it for collection. This is useful if the span object is then
   * passed to another thread for use with Trace.continueTrace().
   *
   * @return the same Span object
   */
  public Span detach() {
    if (span == null) {
      return null;
    }
    if (detached) {
      Tracer.clientError("Tried to detach trace span " + span + " but " +
          "it has already been detached.");
    }
    detached = true;

    Span cur = Tracer.getInstance().currentSpan();
    if (cur != span) {
      Tracer.clientError("Tried to detach trace span " + span + " but " +
          "it is not the current span for the " +
          Thread.currentThread().getName() + " thread.  You have " +
          "probably forgotten to close or detach " + cur);
    } else {
      Tracer.getInstance().setCurrentSpan(savedSpan);
    }
    return span;
  }

  /**
   * Return true when {@link #detach()} has been called. Helpful when debugging
   * multiple threads working on a single span.
   */
  public boolean isDetached() {
    return detached;
  }

  @Override
  public void close() {
    if ((span == null) || detached) {
      return;
    }
    detached = true;
    Span cur = Tracer.getInstance().currentSpan();
    if (cur != span) {
      Tracer.clientError("Tried to close trace span " + span + " but " +
          "it is not the current span for the " +
          Thread.currentThread().getName() + " thread.  You have " +
          "probably forgotten to close or detach " + cur);
    } else {
      span.stop();
      Tracer.getInstance().setCurrentSpan(savedSpan);
    }
  }
}
