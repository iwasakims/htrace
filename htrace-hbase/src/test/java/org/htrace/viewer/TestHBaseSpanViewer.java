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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;
import org.htrace.SpanReceiver;
import org.htrace.TraceCreator;
import org.htrace.impl.HBaseTestUtil;


public class TestHBaseSpanViewer {
  private static final Log LOG = LogFactory.getLog(TestHBaseSpanViewer.class);
  private static final HBaseTestingUtility UTIL = new HBaseTestingUtility();

  /*
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    UTIL.startMiniCluster(1);
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    UTIL.shutdownMiniCluster();
  }

  @Test
  public void testHBaseSpanViewer() {
    HTableInterface htable = HBaseTestUtil.createTable(UTIL);
    SpanReceiver receiver = HBaseTestUtil.startReceiver(UTIL);
    TraceCreator tc = new TraceCreator(receiver);
    tc.createThreadedTrace();
    tc.createSimpleTrace();
    tc.createSampleRpcTrace();
    HBaseTestUtil.stopReceiver(receiver);
    try {
    } catch (IOException e) {
      Assert.fail("failed to get spans from HBase. " + e.getMessage());
    }
  }
  */
}
