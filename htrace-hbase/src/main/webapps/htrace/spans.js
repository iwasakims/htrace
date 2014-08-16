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
    var rootid = 477902;
    var maxwidth = 500;
    var left = 200;
    var tstart = d3.min(spans, function(s) {return s.start});
    var tstop = d3.max(spans, function(s) {return s.stop});
    var xscale = d3.scale.linear().domain([tstart, tstop]).range([0, maxwidth]);

    /*
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

  */
  var byparent = d3.nest()
    .sortValues(function(a, b) {return a.start < b.start ? -1 : a.start > b.start ? 1 : 0;})
    .key(function(e) {return e.parent_id}).map(spans, d3.map);
  //console.log(byparent);
  //var parents = d3.keys(byparent)
  //console.log(parents);
  addchildren(byparent.get(rootid), byparent);
  console.log(byparent.get(rootid));

  var packed = {span_id: "root", children: byparent.get(rootid)};
  var treeChart = d3.layout.tree().size([500,500]);
  var depthScale = d3.scale.category10([0,1,2]);
  var linkGenerator = d3.svg.diagonal();
  linkGenerator.projection(function (d) {return [d.y, d.x]})

  d3.select("svg")
    .append("g")
    .attr("id", "treeG")
    .selectAll("g")
    .data(treeChart(packed))
    .enter()
    .append("g")
    .attr("class", "node")
    .attr("transform", function(d) {return "translate(" +d.y+","+d.x+")"});

  d3.selectAll("g.node")
    .append("circle")
    .attr("r", 10)
    .style("fill", function(d) {return depthScale(d.depth)})
    .style("stroke", "white")
    .style("stroke-width", "2px");

  d3.selectAll("g.node")
    .append("text")
    .text(function(d) {return d.span_id || d.process_id || d.description});
  
  d3.select("#treeG").selectAll("path")
    .data(treeChart.links(treeChart(packed)))
    .enter().insert("path","g")
    .attr("d", linkGenerator)
    .style("fill", "none")
    .style("stroke", "black")
    .style("stroke-width", "2px");
  });

function addchildren (children, byparent) {
  children.forEach(function(e) {
                       e.children = byparent.get(e.span_id);
                       addchildren(e.children, byparent);
                   });
}
