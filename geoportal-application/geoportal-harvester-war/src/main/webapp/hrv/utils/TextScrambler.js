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

define(["dojo/_base/declare",
        "dojo/_base/lang",
        "dojox/encoding/digests/MD5"
      ],
  function(declare,lang,MD5){
  
    return {
      encode: function(text) {
        var crc = this._hash(text);
        var encoded = btoa(encodeURIComponent(text));
        return crc + encoded;
      },
      
      decode: function(text) {
        if (text.length<24) {
          return text;
        }
        var crc = text.substr(0,24);
        var encoded = text.slice(24);
        
        var decoded = decodeURIComponent(atob(encoded));
        if (this._hash(decoded)!=crc) {
          return text;
        }
        
        return decoded;
      },

      _hash: function (string) {
        return MD5(string);
      }
    };
});



