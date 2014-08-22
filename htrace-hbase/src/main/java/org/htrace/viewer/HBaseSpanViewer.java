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

import com.googlecode.protobuf.format.JsonFormat;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.htrace.impl.HBaseSpanReceiver;
import org.htrace.protobuf.generated.SpanProtos;

public class HBaseSpanViewer {
  private static final Log LOG = LogFactory.getLog(HBaseSpanViewer.class);
  private Configuration conf;
  private HConnection hconnection;
  private HTableInterface htable;
  private byte[] table;
  private byte[] cf; 
  private byte[] icf; 

  public HBaseSpanViewer(Configuration conf) {
    this.conf = conf;
    this.table = Bytes.toBytes(conf.get(HBaseSpanReceiver.TABLE_KEY,
                                        HBaseSpanReceiver.DEFAULT_TABLE));
    this.cf = Bytes.toBytes(conf.get(HBaseSpanReceiver.COLUMNFAMILY_KEY,
                                     HBaseSpanReceiver.DEFAULT_COLUMNFAMILY));
    this.icf = Bytes.toBytes(conf.get(HBaseSpanReceiver.INDEXFAMILY_KEY,
                                      HBaseSpanReceiver.DEFAULT_INDEXFAMILY));
  }

  public void close() {
    stopClient();
  }

  public void startClient() {
    if (this.htable == null) {
      try {
        this.hconnection = HConnectionManager.createConnection(conf);
        this.htable = hconnection.getTable(table);
      } catch (IOException e) {
        LOG.warn("Failed to create HBase connection. " + e.getMessage());
      }
    }
  }

  public void stopClient() {
    try {
      if (this.htable != null) {
        this.htable.close();
        this.htable = null;
      }
      if (this.hconnection != null) {
        this.hconnection.close();
        this.hconnection = null;
      }
    } catch (IOException e) {
      LOG.warn("Failed to close HBase connection. " + e.getMessage());
    }
  }

  public List<SpanProtos.Span> getSpans(long traceid) throws IOException {
    startClient();
    List<SpanProtos.Span> spans = new ArrayList<SpanProtos.Span>();
    Get get = new Get(Bytes.toBytes(traceid));
    get.addFamily(this.cf);
    try {
      for (Cell cell : htable.get(get).listCells()) {
        InputStream in = new ByteArrayInputStream(cell.getQualifierArray(),
                                                  cell.getQualifierOffset(),
                                                  cell.getQualifierLength());
        spans.add(SpanProtos.Span.parseFrom(in));
      }
    } catch (IOException e) {
      LOG.warn("Failed to get spans from HBase. " + e.getMessage());
      stopClient();
    }
    return spans;
  }

  public List<SpanProtos.Span> getRootSpans() throws IOException {
    startClient();
    Scan scan = new Scan();
    scan.addColumn(this.icf, HBaseSpanReceiver.INDEX_SPAN_QUAL);
    List<SpanProtos.Span> spans = new ArrayList<SpanProtos.Span>();
    try {
      ResultScanner scanner = htable.getScanner(scan);
      Result result = null;
      while ((result = scanner.next()) != null) {
        for (Cell cell : result.listCells()) {
          InputStream in = new ByteArrayInputStream(cell.getValueArray(),
                                                    cell.getValueOffset(),
                                                    cell.getValueLength());
          spans.add(SpanProtos.Span.parseFrom(in));
        }
      }
    } catch (IOException e) {
      LOG.warn("Failed to get root spans from HBase. " + e.getMessage());
      stopClient();
    }
    return spans;
  }

  /**
   * Run basic test.
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    HBaseSpanViewer viewer = new HBaseSpanViewer(HBaseConfiguration.create());
    if (args.length == 0) {
      List<SpanProtos.Span> spans = viewer.getRootSpans();
      for (SpanProtos.Span span : spans) {
        System.out.println(JsonFormat.printToString(span));
      }
    } else {
      List<SpanProtos.Span> spans = viewer.getSpans(Long.parseLong(args[0]));
      for (SpanProtos.Span span : spans) {
        System.out.println(JsonFormat.printToString(span));
      }
    }
    viewer.close();
  }
}
