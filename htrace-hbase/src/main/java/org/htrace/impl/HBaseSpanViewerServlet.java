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

package org.htrace.impl;

import com.googlecode.protobuf.format.JsonFormat;
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
import org.apache.hadoop.util.ServletUtil;
import org.htrace.protobuf.generated.SpanProtos;

public class HBaseSpanViewerServlet extends HttpServlet {
  private static final Log LOG = LogFactory.getLog(HBaseSpanViewerServlet.class);
  public static final String PREFIX = "/getspans";

  @Override
  @SuppressWarnings("unchecked")
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    final Configuration conf = (Configuration) getServletContext()
        .getAttribute(HBaseSpanViewerServer.CURRENT_CONF);

    final String path =
        validatePath(ServletUtil.getDecodedPath(request, PREFIX));
    if (path == null) {
      response.setContentType("text/plain");
      response.getWriter().print("Invalid input");
      return;
    }
    Long traceid = Long.parseLong(path.substring(1));
    HBaseSpanViewer viewer = new HBaseSpanViewer(conf);
    response.setContentType("application/javascript");
    PrintWriter out = response.getWriter();
    out.print("[");
    boolean first = true;
    for (SpanProtos.Span span : viewer.getSpans(traceid)) {
      if (first) {
        first = false;
      } else {
        out.print(",");
      }
      out.print(JsonFormat.printToString(span));
    }
    out.print("]");
  }

  public static String validatePath(String p) {
    return p == null || p.length() == 0?
      null: new Path(p).toUri().getPath();
  }
}
