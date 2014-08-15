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
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.http.HttpServer2;
import org.apache.hadoop.net.NetUtils;
import org.apache.hadoop.hbase.HBaseConfiguration;

public class HBaseSpanViewerServer {
  private static final Log LOG = LogFactory.getLog(HBaseSpanViewerServer.class);
  public static final String HTRACE_VIEWER_HTTP_ADDRESS_KEY = "htrace.viewer.http.address";
  public static final String HTRACE_VIEWER_HTTP_ADDRESS_DEFAULT = "0.0.0.0:16900";
  private Configuration conf;
  private HttpServer2 httpServer;
  private InetSocketAddress httpAddress;

  public HBaseSpanViewerServer(Configuration conf) {
    this.conf = conf;
  }

  void start() throws IOException {
    httpAddress = NetUtils.createSocketAddr(
        conf.get(HTRACE_VIEWER_HTTP_ADDRESS_KEY, HTRACE_VIEWER_HTTP_ADDRESS_DEFAULT));
    conf.set(HTRACE_VIEWER_HTTP_ADDRESS_KEY, NetUtils.getHostPortString(httpAddress));
    String name = "htrace";
    HttpServer2.Builder builder = new HttpServer2.Builder().setName(name)
                                                           .setConf(conf);
    if (httpAddress.getPort() == 0) {
      builder.setFindPort(true);
    }
    URI uri = URI.create("http://" + NetUtils.getHostPortString(httpAddress));
    builder.addEndpoint(uri);
    LOG.info("Starting Web-server for " + name + " at: " + uri);
    httpServer = builder.build();
    httpServer.addServlet("getspans",
                          HBaseSpanViewerServlet.PREFIX + "/*",
                          HBaseSpanViewerServlet.class);
    httpServer.start();

    int connIdx = 0;
    httpAddress = httpServer.getConnectorAddress(connIdx++);
  }

  void join() throws Exception {
    if (httpServer != null) {
      httpServer.join();
    }
  }

  void stop() throws Exception {
    if (httpServer != null) {
      httpServer.stop();
    }
  }

  InetSocketAddress getHttpAddress() {
    return httpAddress;
  }

  /**
   * @throws IOException
   */
  public static void main(String[] args) throws Exception {
    HBaseSpanViewerServer server =
        new HBaseSpanViewerServer(HBaseConfiguration.create());
    server.start();
    server.join();
    server.stop();
  }
}
