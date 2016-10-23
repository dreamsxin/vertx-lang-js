/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

/** @module testmodel-js/concrete_handler_user_type */
var utils = require('vertx-js/util/utils');
var RefedInterface1 = require('testmodel-js/refed_interface1');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JConcreteHandlerUserType = io.vertx.codegen.testmodel.ConcreteHandlerUserType;

/**

 @class
*/
var ConcreteHandlerUserType = function(j_val) {

  var j_concreteHandlerUserType = j_val;
  var that = this;

  /**

   @public
   @param arg0 {RefedInterface1} 
   */
  this.handle = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_concreteHandlerUserType["handle(io.vertx.codegen.testmodel.RefedInterface1)"](arg0._jdel);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_concreteHandlerUserType;
};

ConcreteHandlerUserType._jclass = utils.getJavaClass("io.vertx.codegen.testmodel.ConcreteHandlerUserType");
ConcreteHandlerUserType._create = function(jdel) {
  // A bit of jiggery pokery to create the object given a reference to the constructor function
  var obj = Object.create(ConcreteHandlerUserType.prototype, {});
  ConcreteHandlerUserType.apply(obj, arguments);
  return obj;
}
// We export the Constructor function
module.exports = ConcreteHandlerUserType;