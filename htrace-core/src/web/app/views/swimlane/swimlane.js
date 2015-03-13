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

  onShow: function() {
    console.log(this)
    const limit = 100;
    const height_span = 20;
    const width_span = 700;
    const size_tl = 6;
    const margin = {top: 50, bottom: 50, left: 50, right: 1000, process: 250};
    this.addLane(this.options.spanId, limit);
  },

  addLane: function addLane(spanId, limit) {
    console.log(spanId);
    d3.json("/span/" + spanId, function(span) {
      console.log(span);
    });
    d3.json("/span/" + spanId + "/children?lim=" + limit, function(children) {
      console.log(children);
      children.forEach(function(childId) {
        addLane(childId, limit);
      })
    });
  }
});
