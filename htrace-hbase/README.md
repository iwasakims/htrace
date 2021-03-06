<!--
 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->

htrace-hbase
============

htrace-hbase provides the span receiver which sends tracing spans to HBase
and a viewer which retrieves spans from HBase and displays them graphically.


Tutorial
--------

In the example below, we use the same HBase instance running in standalone-mode
as both tracee and storage for the tracing spans.

First, get HBase and build it:

    $ git clone https://github.com/apache/hbase
    $ cd hbase
    $ mvn package -DskipTests

Build htrace-hbase (by building all of htrace... just takes a second):

    $ git clone git@github.com:cloudera/htrace.git
    $ cd htrace.git
    $ mvn install

This will add the htrace jars including htrace-hbase to your local
maven repository.

Add a configuration that sets HBase as span receiver in hbase-site.xml:

    <property>
      <name>hbase.trace.spanreceiver.classes</name>
      <value>org.apache.htrace.impl.HBaseSpanReceiver</value>
    </property>

Starting HBase server in standalone-mode with htrace-hbase jar added
to the CLASSPATH (use appropriate 'version' -- below we are using 3.0.4).

    $ HBASE_CLASSPATH=$HOME/.m2/repository/org/apache/htrace/htrace-hbase/3.0.4/htrace-hbase-3.0.4.jar $HBASE_HOME/bin/hbase master start

Running HBase shell from another terminal, add the table in which
tracing spans are stored.  By default it uses the table named
"htrace" with two column families "s" and "i" by default:

    hbase(main):001:0> create 'htrace', 's', 'i'

Run some tracing from hbase shell (Make sure htrace is on the CLASSPATH when you start the shell):

    $ HBASE_CLASSPATH=$HOME/.m2/repository/org/apache/htrace/htrace-hbase/3.0.4/htrace-hbase-3.0.4.jar ./bin/hbase shell

    hbase(main):002:0> trace 'start'; create 't1', 'f'; trace 'stop'
    ...
    hbase(main):003:0> trace 'start'; put 't1', 'r1', 'f:c1', 'value'; trace 'stop'
    ...

Running the main class of receiver also generate a simple, artificial trace for test:

    $ bin/hbase org.apache.htrace.impl.HBaseSpanReceiver

Starting viewer process which listens 0.0.0.0:16900 by default.:

    $ HBASE_CLASSPATH=$HOME/.m2/repository/org/apache/htrace/htrace-hbase/3.0.4/htrace-hbase-3.0.4.jar ./bin/hbase org.apache.htrace.viewer.HBaseSpanViewerServer

Accessing http://host:16900/ with Web browser shows you list of traces like below.:

![list of traces](traces.png "traces list")

Clicking the trace in the list shows you the spans.:

![visualization of spans](spans.png "spans view")

Light blue rectangles represent spans.
The horizontal position of the rectangle represents relative time.
The width of a rectangle and the number at its lower left corner
is the time from start to stop of the span in milliseconds.
If you hover over any small red rectangle, you will see the annotation
associated with the span in a popup window.

![timeline annotations](timelines.png "timeline annotations")

Leading numbers are time of annotation relative to start of the trace.


Receiver Configuration
----------------------

Configurations for span receiver running in HBase
to connect to the HBase to which spans are sent.
These are different from the properties of usual HBase client:

    <property>
      <name>hbase.htrace.hbase.collector-quorum</name>
      <value>127.0.0.1</value>
    </property>
    <property>
      <name>hbase.htrace.hbase.zookeeper.property.clientPort</name>
      <value>2181</value>
    </property>
    <property>
      <name>hbase.htrace.hbase.zookeeper.znode.parent</name>
      <value>/hbase</value>
    </property>

You can specify the name of table and column families
for storing tracing spans by configurations shown below.:

    <property>
      <name>hbase.htrace.hbase.table</name>
      <value>htrace</value>
    </property>
    <property>
      <name>hbase.htrace.hbase.columnfamily</name>
      <value>s</value>
    </property>
    <property>
      <name>hbase.htrace.hbase.indexfamily</name>
      <value>i</value>
    </property>

Notice that these configurations are prefixed by `hbase.`
because the tracee are assumed to be HBase here.


Viewer Configuration
--------------------

You can set listen address of span viewer server by `htrace.viewer.http.address`.
In addition, span viewer server uses
- usual HBase client configuration to connect to HBase and
- receiver's configuration properties without prefix
  to specify the name of table and column families.

```
$ bin/hbase org.apache.htrace.viewer.HBaseSpanViewerServer \
    -Dhtrace.viewer.http.address=0.0.0.0:16900 \
    -Dhbase.zookeeper.quorum=127.0.0.1 \
    -Dhbase.zookeeper.znode.parent=/hbase \
    -Dhtrace.hbase.table=htrace \
    -Dhtrace.hbase.columnfamily=s \
    -Dhtrace.hbase.indexfamily=i
```

Todo
----

- enabling to focus in/out specific spans in trace.
- limiting the traces shown in list based on time period.
- adding utility shell script.
- adding tests.
