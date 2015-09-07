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

public class Trace {
  public static TraceScope startSpan(String description) {
    return NullScope.INSTANCE;
  }

  public static TraceScope startSpan(String description, Span parent) {
    return NullScope.INSTANCE;
  }

  public static TraceScope startSpan(String description, TraceInfo tinfo) {
    return NullScope.INSTANCE;
  }

  public static <T> TraceScope startSpan(String description, Sampler<T> s) {
    return NullScope.INSTANCE;
  }

  public static TraceScope startSpan(String description, Sampler<TraceInfo> s, TraceInfo tinfo) {
    return NullScope.INSTANCE;
  }

  public static <T> TraceScope startSpan(String description, Sampler<T> s, T info) {
    return NullScope.INSTANCE;
  }

  public static TraceScope continueSpan(Span s) {
    return NullScope.INSTANCE;
  }
  public static void setProcessId(String processId) {
  }

  public static void addKVAnnotation(byte[] key, byte[] value) {
  }

  public static void addTimelineAnnotation(String msg) {
  }

  public static boolean isTracing() {
    return false;
  }

  public static Span currentSpan() {
    return null;
  }

  public static Runnable wrap(Runnable runnable) {
    return runnable;
  }

  public static Runnable wrap(String description, Runnable runnable) {
    return runnable;
  }
}
