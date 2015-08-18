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

package org.apache.htrace.viewer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.hbase.HBaseConfiguration;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.webapp.WebAppContext;

public class HBaseSpanViewerServer implements Tool {
  private static final Log LOG = LogFactory.getLog(HBaseSpanViewerServer.class);
  public static final String HTRACE_VIEWER_HTTP_ADDRESS_KEY = "htrace.viewer.http.address";
  public static final String HTRACE_VIEWER_HTTP_ADDRESS_DEFAULT = "0.0.0.0:16900";
  public static final String HTRACE_CONF_ATTR = "htrace.conf";
  private Configuration conf;
  private Server server;

  @Override
  public void setConf(Configuration conf) {
    this.conf = conf;
  }

  @Override
  public Configuration getConf() {
    return this.conf;
  }

  public void stop() throws Exception {
    if (server != null) {
      server.stop();
    }
  }

  public int run(String[] args) throws Exception {
    URI uri = new URI("http://" + conf.get(HTRACE_VIEWER_HTTP_ADDRESS_KEY,
                                           HTRACE_VIEWER_HTTP_ADDRESS_DEFAULT));
    InetSocketAddress addr = new InetSocketAddress(uri.getHost(), uri.getPort());
    server = new Server(addr);
    HandlerList handlerList = new HandlerList();
    //server.setHandler(handlerList);

    ServletHandler servletHandler = new ServletHandler();
    servletHandler.addServletWithMapping(HBaseSpanViewerTracesServlet.class,
                                         "/gettraces");
    servletHandler.addServletWithMapping(HBaseSpanViewerSpansServlet.class,
                                         "/getspans/*");
    handlerList.addHandler(servletHandler);

    //ResourceHandler resourceHandler = new ResourceHandler();
    //String resourceBase =
    //    server.getClass().getClassLoader().getResource("webapps/htrace").toString();
    //resourceHandler.setResourceBase(resourceBase);
    //handlerList.addHandler(resourceHandler);

    WebAppContext webapp = new WebAppContext();
    webapp.setContextPath("/");
    String resourceBase =
        server.getClass().getClassLoader().getResource("webapps/htrace").toString();
    webapp.setResourceBase(resourceBase);
    handlerList.addHandler(webapp);
    webapp.addServlet(HBaseSpanViewerTracesServlet.class, "/gettraces");
    webapp.addServlet(HBaseSpanViewerSpansServlet.class, "/getspans/*");
    server.setHandler(webapp);

    server.setAttribute(HTRACE_CONF_ATTR, conf);
    server.start();
    server.join();
    return 0;
  }

  /**
   * @throws IOException
   */
  public static void main(String[] args) throws Exception {
    ToolRunner.run(HBaseConfiguration.create(), new HBaseSpanViewerServer(), args);
  }
}
