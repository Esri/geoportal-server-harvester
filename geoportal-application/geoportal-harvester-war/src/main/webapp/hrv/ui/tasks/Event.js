/*
 * Copyright 2016 Esri, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

define([
  "dojo/_base/declare",
  "dijit/_WidgetBase",
  "dijit/_TemplatedMixin",
  "dijit/_WidgetsInTemplateMixin",
  "dojo/i18n!../../nls/resources",
  "dojo/text!./templates/Event.html",
  "dojo/date/locale",
  "dojo/_base/lang",
  "dojo/on",
  "dojo/dom-construct",
], function (
  declare,
  _WidgetBase,
  _TemplatedMixin,
  _WidgetsInTemplateMixin,
  i18n,
  template,
  locale,
  lang,
  on,
  domConstruct
) {
  return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin], {
    i18n: i18n,
    templateString: template,
    startTime: null,
    endTime: null,
    hasDetails: false,

    constructor: function (args) {
      this.data = args;
      this.startTime = this.format(this.data.startTimestamp);
      this.endTime = this.format(this.data.endTimestamp);
    },

    postCreate: function () {
      if (this.data.details && this.data.details.length > 0) {
        this.hasDetails = true;
      }
      if (this.data.failedToHarvest == null || this.data.failedToPublish == null) {
        if (this.data.failed > 0) {
          var failedLink = domConstruct.create(
            "a",
            { href: "#", innerHTML: this.data.failed },
            this.failedNode
          );
          this.own(on(failedLink, "click", lang.hitch(this, this._onFailedDetails)));
        } else {
          domConstruct.create("span", { innerHTML: this.data.failed }, this.failedNode);
        }
      } else {
        if (this.data.failedToPublish > 0) {
          var failedLink = domConstruct.create(
            "a",
            {
              href: "#",
              innerHTML: "" + this.data.failedToHarvest + " / " + this.data.failedToPublish,
            },
            this.failedNode
          );
          this.own(on(failedLink, "click", lang.hitch(this, this._onFailedDetails)));
        } else {
          domConstruct.create(
            "span",
            { innerHTML: "" + this.data.failedToHarvest + " / " + this.data.failedToPublish },
            this.failedNode
          );
        }
      }
      if (this.hasDetails) {
        var moreLink = domConstruct.create(
          "a",
          { href: "#", innerHTML: "[?]", className: "h-event-more" },
          this.failedNode
        );
        this.own(on(moreLink, "click", lang.hitch(this, this._onMore)));
      }
    },

    format: function (date) {
      if (date) {
        return locale.format(new Date(date), { datePattern: "yyyy-MM-dd HH:mm", selector: "date" });
      } else {
        return "?";
      }
    },

    _onFailedDetails: function (evt) {
      this.emit("event-clicked", { data: this.data });
    },

    _onMore: function (evt) {
      this.emit("more-clicked", { data: this.data });
    },
  });
});
