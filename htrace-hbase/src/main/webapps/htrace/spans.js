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

var traceid = window.location.search.substring(1).split("=")[1];
d3.json("/getspans/" + traceid, function(spans) {
    var maxwidth = 500;
    var left = 200;
    var tstart = d3.min(spans, function(s) {return s.start});
    var tstop = d3.max(spans, function(s) {return s.stop});
    var xscale = d3.scale.linear().domain([tstart, tstop]).range([0, maxwidth]);

    gs = d3.select("svg")
      .selectAll("g")
      .data(spans)
      .enter()
      .append("g")
      .attr("transform",
            function(s, i) {
              return "translate(0, " + (i * 20 + 10) + ")";
            });

    gs.append("rect")
      .attr("height", 20)
      .attr("width",
            function (s) {
              return (maxwidth * (s.stop - s.start)) / (tstop - tstart) + 1;
            })
      .style("fill", "lightblue")
      .attr("transform",
            function(s, i) {
              return "translate(" + (xscale(s.start) + left) + ", 0)";
            });

    gs.append("text")
      .text(function(s){return s.description})
      .style("alignment-baseline", "hanging")
      .attr("transform",
            function(s, i) {
              return "translate(" + (xscale(s.start) + left) + ", 0)";
            });

    gs.append("text")
      .text(function(s){return s.process_id})
      .style("alignment-baseline", "hanging");
  });
