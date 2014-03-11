#!/usr/bin/env python

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import sys
import gv
import json
from json import JSONDecoder
from datetime import datetime
from pygraph.classes.graph import graph
from pygraph.classes.digraph import digraph
from pygraph.readwrite.dot import write
from collections import defaultdict

ROOT_SPAN_ID = 0x74ace

def buildGraph(nid):
  for child in spansByParent[nid]:
    desc = spansBySpanId[child]["Description"] + "(" + str(spansBySpanId[child]["Stop"] - spansBySpanId[child]["Start"]) +  ")"
    #graphviz can't handle '\'
    desc = desc.replace("\\", "")
    gr.add_node(child, [("label", desc)])
    gr.add_edge((nid, child))
    buildGraph(child)

def loads_spans(lines):
  decoder = JSONDecoder()
  objs = []
  for line in lines:
    obj = decoder.decode(line)
    objs.append(obj)
  return objs

if __name__ == '__main__':
  nodes = loads_spans(sys.stdin)
  spansBySpanId = dict((s["SpanID"], s) for s in nodes)
  spansByParent = defaultdict(set)
  
  for node in spansBySpanId.values():
    spansByParent[node["ParentID"]].add(node["SpanID"])
  
  count = 0
  for x in spansByParent[ROOT_SPAN_ID]:
    count += 1
    gr = digraph()
    gr.add_node(x, [("label", spansBySpanId[x]["Description"]
                     + "("
                     + str(spansBySpanId[x]["Stop"] - spansBySpanId[x]["Start"])
                     +  ")")])
    
    buildGraph(x)
    dot = write(gr)
    print dot
    gvv = gv.readstring(dot)
    gv.layout(gvv, 'dot')
    gv.render(gvv, 'png',
              datetime.now().isoformat() + '_'
              + str(spansBySpanId[x]["Description"])[:10] + '.png')
  
  print("Created " + str(count)  + " images.")
  
