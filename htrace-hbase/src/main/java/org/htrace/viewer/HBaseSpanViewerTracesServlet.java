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

package org.htrace.viewer;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.http.HttpServer2;
import org.apache.hadoop.util.ServletUtil;
import org.htrace.protobuf.generated.SpanProtos;

public class HBaseSpanViewerTracesServlet extends HttpServlet {
  private static final Log LOG = LogFactory.getLog(HBaseSpanViewerTracesServlet.class);
  public static final String PREFIX = "/gettraces";
  private static final ThreadLocal<HBaseSpanViewer> tlviewer =
      new ThreadLocal<HBaseSpanViewer>() {
        @Override
        protected HBaseSpanViewer initialValue() {
          return null;
        }
      };

  @Override
  @SuppressWarnings("unchecked")
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    HBaseSpanViewer viewer = tlviewer.get();
    if (viewer == null) {
      final Configuration conf = (Configuration) getServletContext()
        .getAttribute(HttpServer2.CONF_CONTEXT_ATTRIBUTE);
      viewer = new HBaseSpanViewer(conf);
      tlviewer.set(viewer);
    }
    response.setContentType("application/javascript");
    PrintWriter out = response.getWriter();
    out.print("[");
    boolean first = true;
    for (SpanProtos.Span span : viewer.getRootSpans()) {
      if (first) {
        first = false;
      } else {
        out.print(",");
      }
      out.print("{");
      // print long as string for handling in JavaScript.
      out.print("\"trace_id\":\"");
      out.print(span.getTraceId());
      out.print("\",");
      out.print("\"process_id\":\"");
      out.print(span.getProcessId());
      out.print("\",");
      out.print("\"description\":\"");
      out.print(span.getDescription());
      out.print("\",");
      out.print("\"start\":"); // start time as numeric.
      out.print(span.getStart()); 
      out.print("}");
    }
    out.print("]");
  }

  @Override
  public void init() throws ServletException {
  }

  @Override
  public void destroy() {
    HBaseSpanViewer viewer = tlviewer.get();
    if (viewer != null) {
      viewer.close();
    }
  }
}
