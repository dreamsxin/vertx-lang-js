/*
 * Copyright (c) 2011-2016 The original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *      The Eclipse Public License is available at
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *      The Apache License v2.0 is available at
 *      http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

var ServiceDiscovery = require("vertx-service-discovery-js/service_discovery");
var HelloService = require("test-services-js/hello_service.js");
var EventBusService = require("vertx-service-discovery-js/event_bus_service.js");

var discovery = ServiceDiscovery.create(vertx, {
  "backendConfiguration":
    {
      "backend-name": "io.vertx.servicediscovery.impl.DefaultServiceDiscoveryBackend"
    }
  });


EventBusService.getServiceProxyWithJsonFilter(discovery,
  { "service.interface" : "io.vertx.test.it.discovery.service.HelloService"},
  HelloService,
  function(ar, ar_err) {
    var proxy = ar;
    proxy.hello({"name" : "vert.x"}, function(r, err) {
        if (err) {
            vertx.eventBus().send("result", {
                "status" : "ko"
            });
        } else {
            vertx.eventBus().send("result", {
                "status": "ok",
                "message": r
            });
            ServiceDiscovery.releaseServiceObject(discovery, proxy);
        }
    });
});
