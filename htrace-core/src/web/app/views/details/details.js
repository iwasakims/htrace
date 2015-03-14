/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

app.DetailsView = Backbone.Marionette.LayoutView.extend({
  "template": "#details-layout-template",
  "regions": {
    "span": "div[role='complementary']",
    "content": "div[role='main']"
  }
});

app.SpanDetailsView = Backbone.Marionette.ItemView.extend({
  "className": "span",
  "template": "#span-details-template",

  "serializeData": function() {
    var context = {
      "span": this.model.toJSON()
    };
    context["span"]["duration"] = this.model.duration();
    return context;
  },
  
  "events": {
    "click": "swimlane"
  },
  "swimlane": function() {
    Backbone.history.navigate("!/swimlane/" + this.model.get("spanId"),
                              {"trigger": true});
  }
});
