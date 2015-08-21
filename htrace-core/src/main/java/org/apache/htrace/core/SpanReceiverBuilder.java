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

import java.lang.reflect.Constructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A {@link SpanReceiver} builder.
 *
 * Unqualified class names are interpreted as members of the {@code
 * org.apache.htrace.core} package. The {@link #build()} method constructs an
 * instance of that class, initialized with the provided configuration.
 */
public class SpanReceiverBuilder {
  private static final Log LOG = LogFactory.getLog(SpanReceiverBuilder.class);

  private final static String DEFAULT_PACKAGE = "org.apache.htrace.core";
  private final HTraceConfiguration conf;
  private boolean logErrors;
  private String className;
  private ClassLoader classLoader = SpanReceiverBuilder.class.getClassLoader();

  public SpanReceiverBuilder(HTraceConfiguration conf) {
    this.conf = conf;
    reset();
  }

  /**
   * Set this builder back to defaults.
   *
   * @return this instance.
   */
  public SpanReceiverBuilder reset() {
    this.logErrors = true;
    this.className = null;
    return this;
  }

  public SpanReceiverBuilder className(final String className) {
    this.className = className;
    return this;
  }

  /**
   * Configure whether we should log errors during build().
   * @return This instance
   */
  public SpanReceiverBuilder logErrors(boolean logErrors) {
    this.logErrors = logErrors;
    return this;
  }

  public SpanReceiverBuilder classLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
    return this;
  }

  private void throwError(String errorStr) {
    if (logErrors) {
      LOG.error(errorStr);
    }
    throw new RuntimeException(errorStr);
  }

  private void throwError(String errorStr, Throwable e) {
    if (logErrors) {
      LOG.error(errorStr, e);
    }
    throw new RuntimeException(errorStr, e);
  }

  public SpanReceiver build() {
    SpanReceiver spanReceiver = newSpanReceiver();
    if (LOG.isTraceEnabled()) {
      LOG.trace("Created new span receiver of type " +
                spanReceiver.getClass().getName());
    }
    return spanReceiver;
  }

  private SpanReceiver newSpanReceiver() {
    if ((className == null) || className.isEmpty()) {
      throwError("No span receiver class specified.");
    }
    String str = className;
    if (!str.contains(".")) {
      str = DEFAULT_PACKAGE + "." + str;
    }
    Class cls = null;
    try {
      cls = classLoader.loadClass(str);
    } catch (ClassNotFoundException e) {
      throwError("SpanReceiverBuilder cannot find SpanReceiver class " + str);
    }
    Constructor<SpanReceiver> ctor = null;
    try {
      ctor = cls.getConstructor(HTraceConfiguration.class);
    } catch (NoSuchMethodException e) {
      throwError("SpanReceiverBuilder cannot find a constructor for class " +
          str + "which takes an HTraceConfiguration.");
    }
    SpanReceiver receiver = null;
    try {
      LOG.debug("Creating new instance of " + str + "...");
      receiver = ctor.newInstance(conf);
    } catch (ReflectiveOperationException e) {
      throwError("SpanReceiverBuilder reflection error when constructing " +
          str + ".", e);
    } catch (Throwable t) {
      throwError("SpanReceiverBuilder newInstance error when constructing " +
          str + ".", t);
    }
    return receiver;
  }
 }
