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
    this.spans = this.getSpans([], this.options.spanId, lim);
    console.log(this.spans);
  },
  
  render: function() {
    this.drawSVG(this.spans);
  },

  getSpans: function getSpans(spans, spanId, lim) {
    span = getJsonSync("/span/" + spanId);
    spans.push(span);
    childIds = getJsonSync("/span/" + span.s + "/children?lim=" + lim);
    children = [];
    childIds.forEach(function(childId) {
      children.push(getJsonSync("/span/" + childId));
    });
    children.sort(function(x, y) {
      return x.b < y.b ? -1 : x.b > y.b ? 1 : 0;
    });
    children.forEach(function(child) {
      spans = getSpans(spans, child.s, lim);
    });
    return spans;

    function getJsonSync(url) {
      return $.ajax({
        type: "GET",
        url: url,
        async: false,
        dataType: "json"
      }).responseJSON;
    }
  },

  drawSVG: function drawSVG(spans) {
    const height_span = 20;
    const width_span = 700;
    const size_tl = 6;
    const margin = {top: 50, bottom: 50, left: 50, right: 1000, process: 250};

    var height_screen = spans.length * height_span;
    var tmin = d3.min(spans, function(s) { return s.b; });
    var tmax = d3.max(spans, function(s) { return s.e; });
    var xscale = d3.time.scale()
      .domain([new Date(tmin), new Date(tmax)]).range([0, width_span]);

    var svg = d3.select("body").append("svg")
      .attr("width", width_span + margin.process + margin.left + margin.right)
      .attr("height", height_screen + margin.top + margin.bottom)
      .append("g")
      .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    var bars = svg.append("g")
      .attr("id", "bars")
      .attr("width", width_span)
      .attr("height", height_screen)
      .attr("transform", "translate(" + margin.process + ", 0)");
    
    var span_g = bars.selectAll("g.span")
      .data(spans)
      .enter()
      .append("g")
      .attr("transform", function(s, i) {
        return "translate(0, " + (i * height_span + 5) + ")";
      })
      .classed("timeline", function(d) { return d.t; });

    span_g.append("text")
      .text(function(s) { return s.process_id; })
      .style("alignment-baseline", "hanging")
      .attr("transform", function(s) {
        return "translate(" + (s.depth * 10 - margin.process) + ", 0)";
      });

    var rect_g = span_g.append("g")
      .attr("transform", function(s) {
        return "translate(" + xscale(new Date(s.b)) + ", 0)";
      });

    rect_g.append("rect")
      .attr("height", height_span - 1)
      .attr("width", function (s) {
        return (width_span * (s.e - s.b)) / (tmax - tmin) + 1;
      })
      .style("fill", "lightblue")
      .attr("class", "span")

    rect_g.append("text")
      .text(function(s){ return s.d; })
      .style("alignment-baseline", "hanging");

    rect_g.append("text")
      .text(function(s){ return s.e - s.b; })
      .style("alignment-baseline", "baseline")
      .style("text-anchor", "end")
      .style("font-size", "10px")
      .attr("transform", function(s, i) { return "translate(0, 10)"; });

    bars.selectAll("g.timeline").selectAll("rect.timeline")
      .data(function(s) { return s.t; })
      .enter()
      .append("rect")
      .style("fill", "red")
      .attr("height", size_tl)
      .attr("width", size_tl)
      .attr("transform", function(t) {
        return "translate(" + xscale(t.t) + "," + (height_span - 1 - size_tl) + ")";
      })
      .classed("timeline");

    var popup = d3.select("body").append("div")
      .attr("class", "popup")
      .style("opacity", 0);

    bars.selectAll("g.timeline")
      .on("mouseover", function(d) {
        popup.transition().duration(300).style("opacity", .95);
        var text = "<table>";
        d.t.forEach(function (t) {
          text += "<tr><td>" + (t.t - tmin) + "</td>";
          text += "<td> : " + t.m + "<td/></tr>";
        });
        text += "</table>"
        popup.html(text)
          .style("left", (document.body.scrollLeft + 30) + "px")
          .style("top", (document.body.scrollTop + 30) + "px")
          .style("width", "700px")
          .style("background", "orange")
          .style("position", "absolute");
      })
      .on("mouseout", function(d) {
        popup.transition().duration(300).style("opacity", 0);
      });
    
    var axis = d3.svg.axis()
      .scale(xscale)
      .orient("top")
      .tickValues(xscale.domain())
      .tickFormat(d3.time.format("%x %X.%L"))
      .tickSize(6, 3);

    bars.append("g").attr("class", "axis").call(axis);
  }
});
