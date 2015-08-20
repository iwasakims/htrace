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
 * A {@link Sampler} builder.  Unqualified class names are interpreted as
 * members of the {@code org.apache.htrace.core} package. The {@link #build()}
 * method constructs an instance of that class, initialized with the same
 * configuration.
 */
public class SamplerBuilder {
  private static final Log LOG = LogFactory.getLog(SamplerBuilder.class);

  private final static String DEFAULT_PACKAGE = "org.apache.htrace.core";
  private final HTraceConfiguration conf;
  private String className;
  private ClassLoader classLoader = SamplerBuilder.class.getClassLoader();

  public SamplerBuilder(HTraceConfiguration conf) {
    this.conf = conf;
    reset();
  }

  public SamplerBuilder reset() {
    this.className = null;
    return this;
  }

  public SamplerBuilder className(String className) {
    this.className = className;
    return this;
  }

  public SamplerBuilder classLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
    return this;
  }

  private void throwError(String errorStr) {
    LOG.error(errorStr);
    throw new RuntimeException(errorStr);
  }

  private void throwError(String errorStr, Throwable e) {
    LOG.error(errorStr, e);
    throw new RuntimeException(errorStr, e);
  }

  public Sampler build() {
    Sampler sampler = newSampler();
    if (LOG.isTraceEnabled()) {
      LOG.trace("Created new sampler of type " +
          sampler.getClass().getName(), new Exception());
    }
    return sampler;
  }

  private Sampler newSampler() {
    if (className == null || className.isEmpty()) {
      throwError("No sampler class specified.");
    }
    String str = className;
    if (!str.contains(".")) {
      str = DEFAULT_PACKAGE + "." + str;
    }
    Class cls = null;
    try {
      cls = classLoader.loadClass(str);
    } catch (ClassNotFoundException e) {
      throwError("SamplerBuilder cannot find Sampler class " + str);
    }
    Constructor<Sampler> ctor = null;
    try {
      ctor = cls.getConstructor(HTraceConfiguration.class);
    } catch (NoSuchMethodException e) {
      throwError("SamplerBuilder cannot find a constructor for class " +
          str + "which takes an HTraceConfiguration.");
    }
    Sampler sampler = null;
    try {
      LOG.debug("Creating new instance of " + str + "...");
      sampler = ctor.newInstance(conf);
    } catch (ReflectiveOperationException e) {
      throwError("SamplerBuilder reflection error when constructing " +
          str + ".", e);
    } catch (Throwable t) {
      throwError("SamplerBuilder newInstance error when constructing " +
          str + ".", t);
    }
    return sampler;
  }
}
