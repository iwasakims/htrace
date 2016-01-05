<!---
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License. See accompanying LICENSE file.
-->

Apache HTrace is an <a href="http://htrace.incubator.apache.org">Apache Incubator</a>
project. To add HTrace to your project, see detail on how to add it as a
<a href="dependency-info.html">dependency</a>. Formerly, HTrace was available at `org.htrace`.

* htrace-4.0.0-incubating published September 15th, 2015. [Download it!](http://www.apache.org/dyn/closer.cgi/incubator/htrace/)
* htrace-3.2.0-incubating published June 2nd, 2015. [Download it!](http://www.apache.org/dyn/closer.cgi/incubator/htrace/)
* We made our first release from Apache Incubator, htrace-3.1.0-incubating, January 20th, 2015. [Download it!](http://www.apache.org/dyn/closer.cgi/incubator/htrace/)


## API
Using HTrace requires adding some instrumentation to your application.
Before we get into the details, lets review our terminology.  HTrace
borrows [Dapper's](http://research.google.com/pubs/pub36356.html)
terminology.

<b>Span:</b> The basic unit of work. For example, sending an RPC is a new span,
as is sending a response to an RPC.  Spans are identified by a unique 128-bit
ID.  Spans also have other data, such as descriptions, key-value annotations,
the ID of the span that caused them, and tracer IDs.

Spans are started and stopped, and they keep track of their timing
information.  Once you create a span, you must stop it at some point
in the future.

<b>TracerId:</b> Identifies the Tracer object which created a specific Span.
The TracerId should identify the source of the Span.  "10.0.0.2/DataNode" is a
typical TracerID.

<b>SpanReceiver:</b> SpanReceivers handle spans once they have been created.
Typically, Span Receivers send spans to an external data store to be
analyzed.  HTrace comes with several standard span receivers, such as
`LocalFileSpanReceiver`.

<b>Sampler:</b> Samplers determine when tracing should be enabled, and when it
should be disabled.   The goal of HTrace is to trace an entire request.

## How to add tracing to your application
To instrument your system you must:

### Create a Tracer object
You can create a Tracer object via the `Tracer#Builder`.

    Tracer tracer = new Tracer.Builder("MyApp").conf(conf).build();

The `Tracer#Builder` will take care of creating the appropriate Sampler and
SpanReceiver objects, as well as the Tracer itself.   If a SpanReceiver was
created, we will install a shutdown hook to close it when the JVM shuts down.

### Attach additional information to your RPCs
In order to create the causal links necessary for a trace, HTrace needs to know
about the causal relationships between spans.  The only information you need to
add to your RPCs is the 128-bit span ID.  If tracing is enabled when you send an
RPC, attach the ID of the current span to the message.  On the receiving end of
the RPC, check to see if the message has a span ID attached.  If it does, start
a new trace scope with that span as a parent.

### Wrap your thread changes
HTrace stores span information in java's ThreadLocals, which causes
the trace to be "lost" on thread changes. The only way to prevent
this is to "wrap" your thread changes. For example, if your code looks
like this:

    Thread t1 = new Thread(new MyRunnable());
    ...

Just change it to look this:

    Thread t1 = new Thread(tracer.wrap(new MyRunnable(), "MyRunnable"));

That's it! `Tracer.wrap()` takes two argument
(a runnable or a callable and span description)
and if the current thread is a part of a trace,
returns a wrapped version of the runnable/callable.
The wrapped version of a runnable/callable
just knows about the span that created it and will start a new span
with given description in the new thread that is the child of the span that
created the runnable/callable.  There may be situations in which a
simple `Tracer.wrap()` does not suffice.  In these cases all you need
to do is keep a reference to the "parent span" (the span before the
thread change) and once you're in the new thread start a new span that
is the "child" of the parent span you stored.

For example:

Say you have some object representing a "put" operation.  When the
client does a "put," the put is first added to a list so another
thread can batch together the puts. In this situation, you
might want to add another field to the Put class that could store the
current span at the time the put was created.  Then when the put is
pulled out of the list to be processed, you can start a new span as
the child of the span stored in the Put.

### Add custom spans and annotations
Once you've augmented your RPC's and wrapped the necessary thread
changes, you can add more spans and annotations wherever you want.
For example, you might do some expensive computation that you want to
see on your traces.  In this case,
you could create a new trace scope which starts a new span internally
before the computation that you then close after the computation has
finished. It might look like this:

    TraceScope computationScope = tracer.newScope("Expensive computation.");
    try {
        //expensive computation here
    } finally {
        computationScope.close();
    }

HTrace also supports key-value annotations on a per-trace basis.

Example:

    scope.addKVAnnotation("faultyRecordCounter", Integer.toString(1));

## htrace-zipkin
htrace-zipkin provides the `SpanReceiver` implementation
which sends spans to [Zipkin](https://github.com/twitter/zipkin) collector.
You can build the uber-jar (htrace-zipkin-*-jar-withdependency.jar) for manual
setup as shown below.  This uber-jar contains all dependencies except
htrace-core and its dependencies.

    $ cd htrace-zipkin
    $ mvn compile assembly:single

## htrace-hbase
See htrace-hbase for an Span Receiver implementation that writes HBase.

