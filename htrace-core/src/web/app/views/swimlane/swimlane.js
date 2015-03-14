/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

app.SwimlaneView = Backbone.Marionette.LayoutView.extend({
  "template": "#swimlane-layout-template",
  "regions": {
    "swimlane": "div[role='complementary']",
    "content": "div[role='main']"
  }
});

app.SwimlaneGraphView = Backbone.Marionette.View.extend({
  className: "swimlane",

  initialize: function() {
    const lim = 100;
    const height_span = 20;
    const width_span = 700;
    const size_tl = 6;
    const margin = {top: 50, bottom: 50, left: 50, right: 1000, process: 250};

    var svg = d3.select(document.body)
      .append("svg")
      .append("g")
      .attr("class", "svg")
      .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    const rootId = "0000000000000000"

    d3.json("/span/" + this.options.spanId, function(span) {
      parent = svg.selectAll()
        .data([span])
        .enter()
        .append("g")
        .attr("class", toClassName(rootId));
      addChildren(parent);
      console.log(svg.selectAll("." + toClassName(rootId)));
    });

    console.log(svg.selectAll("g." + toClassName(rootId)));
    
    function addChildren(parent) {
      var parentId = parent.datum().s
      d3.json("/span/" + parentId + "/children?lim=" + lim, function(children) {
        children.forEach(function(childId) {
          d3.json("/span/" + childId, function(span) {
            child = parent.selectAll()
              .data([span])
              .enter()
              .append("g")
              .attr("class", toClassName(parentId));
            addChildren(child);
          });
        });
      });
    }

    function toClassName(spanId) {
      return "P" + spanId;
    }

    function getSpans(sel, spanId, lim, parentId) {
      d3.json("/span/" + parentId, function(span) {
        sel.append("g").attr("class", "desc")
          .selectAll()
          .data([span])
          .enter()
          .append("g")
          .attr("class", "lane " + parentId);
      });
      console.log(sel.selectAll(".desc"))
      d3.json("/span/" + spanId + "/children?lim=" + lim, function(children) {
        children.forEach(function(childId) {
          getSpans(svg, childId, lim, spanId);
        });
      });
    }
  },
});
