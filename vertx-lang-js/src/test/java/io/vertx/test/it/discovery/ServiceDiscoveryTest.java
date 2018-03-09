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

package io.vertx.test.it.discovery;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import io.vertx.servicediscovery.impl.DiscoveryImpl;
import io.vertx.servicediscovery.types.*;
import io.vertx.serviceproxy.ProxyHelper;
import io.vertx.test.it.discovery.service.HelloService;
import io.vertx.test.it.discovery.service.HelloServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.jayway.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class ServiceDiscoveryTest {

  public static final ServiceDiscoveryOptions DISCOVERY_OPTIONS = new ServiceDiscoveryOptions()
    .setBackendConfiguration(new JsonObject().put("backend-name", "io.vertx.servicediscovery.impl.DefaultServiceDiscoveryBackend"));

  private Vertx vertx;
  private ServiceDiscovery discovery;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
    discovery = new DiscoveryImpl(vertx, DISCOVERY_OPTIONS);
  }

  @After
  public void tearDown() {
    discovery.close();
    AtomicBoolean completed = new AtomicBoolean();
    vertx.close((v) -> completed.set(true));
    await().untilAtomic(completed, is(true));
  }

  @Test
  public void testWithJavaScriptConsumer() {
    // Step 1 - register the service
    HelloService svc = new HelloServiceImpl("stuff");
    ProxyHelper.registerService(HelloService.class, vertx, svc, "address");
    Record record = EventBusService.createRecord("Hello", "address", HelloService.class);

    discovery.publish(record, (r) -> {
    });
    await().until(() -> record.getRegistration() != null);

    // Step 2 - register a consumer that get the result
    AtomicReference<JsonObject> result = new AtomicReference<>();
    vertx.eventBus().<JsonObject>consumer("result", message -> result.set(message.body()));

    // Step 3 - deploy the verticle
    vertx.deployVerticle("it/discovery/HelloServiceConsumer.js", ar -> {
      if (ar.failed()) {
        // Will fail anyway.
        ar.cause().printStackTrace();
      }
    });

    await().until(() -> result.get() != null);

    assertThat(result.get().getString("status")).isEqualTo("ok");
    assertThat(result.get().getString("message")).isEqualTo("stuff vert.x");
  }

  @Test
  public void testJavaScript(TestContext tc) {


    HelloService svc = new HelloServiceImpl();
    ProxyHelper.registerService(HelloService.class, vertx, svc, "my-service");

    AtomicBoolean httpEndpointPublished = new AtomicBoolean();
    AtomicBoolean serviceProxyPublished = new AtomicBoolean();
    AtomicBoolean jdbcDataSourcePublished = new AtomicBoolean();
    AtomicBoolean messageSource1Published = new AtomicBoolean();
    AtomicBoolean messageSource2Published = new AtomicBoolean();
    AtomicBoolean redisDataSourcePublished = new AtomicBoolean();
    AtomicBoolean mongoDataSourcePublished = new AtomicBoolean();

    discovery.publish(
      HttpEndpoint.createRecord("my-http-service", "localhost", 8080, "/"),
      ar -> httpEndpointPublished.set(ar.succeeded()));

    discovery.publish(
      EventBusService.createRecord("my-service", "my-service", HelloService.class.getName()),
      ar -> serviceProxyPublished.set(ar.succeeded()));

    discovery.publish(
      JDBCDataSource.createRecord("my-data-source",
        new JsonObject().put("url", "jdbc:hsqldb:file:target/dumb-db;shutdown=true"),
        new JsonObject().put("database", "some-raw-data")),
      ar -> jdbcDataSourcePublished.set(ar.succeeded())
    );

    discovery.publish(
      MessageSource.createRecord("my-message-source-1", "source1"),
      ar -> messageSource1Published.set(ar.succeeded())
    );

    discovery.publish(
      MessageSource.createRecord("my-message-source-2", "source2", JsonObject.class.getName()),
      ar -> messageSource2Published.set(ar.succeeded())
    );

    discovery.publish(
      RedisDataSource.createRecord("my-redis-data-source",
        new JsonObject().put("url", "localhost"),
        new JsonObject().put("database", "some-raw-data")),
      ar -> redisDataSourcePublished.set(ar.succeeded())
    );

    discovery.publish(
      MongoDataSource.createRecord("my-mongo-data-source",
        new JsonObject().put("connection_string", "mongodb://localhost:12345"),
        new JsonObject().put("database", "some-raw-data")),
      ar -> mongoDataSourcePublished.set(ar.succeeded())
    );

    await().untilAtomic(httpEndpointPublished, is(true));
    await().untilAtomic(serviceProxyPublished, is(true));
    await().untilAtomic(jdbcDataSourcePublished, is(true));
    await().untilAtomic(messageSource1Published, is(true));
    await().untilAtomic(messageSource2Published, is(true));
    await().untilAtomic(redisDataSourcePublished, is(true));
    await().untilAtomic(mongoDataSourcePublished, is(true));



    Async deployment = tc.async();
    Async http_ref = tc.async();
    Async http_sugar = tc.async();
    Async web_ref = tc.async();
    Async web_sugar = tc.async();
    Async svc_ref = tc.async();
    Async svc_sugar = tc.async();
    Async ds_ref = tc.async();
    Async ds_sugar = tc.async();
    Async ms_ref = tc.async();
    Async ms_sugar = tc.async();
    Async redis_ref = tc.async();
    Async redis_sugar = tc.async();
    Async mongo_ref = tc.async();
    Async mongo_sugar = tc.async();

    vertx.deployVerticle("it/discovery/polyglot/my-verticle.js", deployed -> {

      vertx.eventBus().<JsonObject>send("http-ref", "", reply -> {
        if (! reply.succeeded()) {
          reply.cause().printStackTrace();
          tc.fail(reply.cause());
        }
        tc.assertTrue(reply.succeeded());
        tc.assertTrue(reply.result().body().getString("ref_del").contains("HttpEndpointReference"));
        tc.assertTrue(reply.result().body().getString("client_del").contains("HttpClient"));
        tc.assertTrue(reply.result().body().getString("cached_del").contains("HttpClient"));
        tc.assertTrue(reply.result().body().getJsonArray("bindings").isEmpty());

        http_ref.complete();
      });

      vertx.eventBus().<JsonObject>send("http-sugar", "", reply -> {
        tc.assertTrue(reply.succeeded());
        tc.assertTrue(reply.result().body().getString("client_del").contains("HttpClient"));
        tc.assertTrue(reply.result().body().getJsonArray("bindings").isEmpty());

        http_sugar.complete();
      });

      vertx.eventBus().<JsonObject>send("web-ref", "", reply -> {
        tc.assertTrue(reply.succeeded());
        tc.assertTrue(reply.result().body().getString("ref_del").contains("HttpEndpointReference"));
        tc.assertTrue(reply.result().body().getString("client_del").contains("WebClient"));
        tc.assertTrue(reply.result().body().getString("cached_del").contains("WebClient"));
        tc.assertTrue(reply.result().body().getJsonArray("bindings").isEmpty());

        web_ref.complete();
      });

      vertx.eventBus().<JsonObject>send("web-sugar", "", reply -> {
        tc.assertTrue(reply.succeeded());
        tc.assertTrue(reply.result().body().getString("client_del").contains("WebClient"));
        tc.assertTrue(reply.result().body().getJsonArray("bindings").isEmpty());

        web_sugar.complete();
      });

      vertx.eventBus().<JsonObject>send("service-sugar", "", reply -> {
        tc.assertTrue(reply.succeeded());
        tc.assertTrue(reply.result().body().getString("client_del").contains("HelloServiceVertxEBProxy"));
        tc.assertTrue(reply.result().body().getString("client").contains("[object"));
        tc.assertTrue(reply.result().body().getJsonArray("bindings").isEmpty());

        svc_sugar.complete();
      });

      vertx.eventBus().<JsonObject>send("service-ref", "", reply -> {
        tc.assertTrue(reply.succeeded());
        tc.assertTrue(reply.result().body().getString("ref_del").contains("EventBusServiceReference"));
        tc.assertTrue(reply.result().body().getString("client_del").contains("HelloServiceVertxEBProxy"));
        tc.assertTrue(reply.result().body().getString("cached_del").contains("HelloServiceVertxEBProxy"));
        tc.assertTrue(reply.result().body().getString("client").contains("[object"));
        tc.assertTrue(reply.result().body().getJsonArray("bindings").isEmpty());

        svc_ref.complete();
      });

      vertx.eventBus().<JsonObject>send("ds-sugar", "", reply -> {
        tc.assertTrue(reply.succeeded());
        tc.assertTrue(reply.result().body().getString("client_del").contains("JDBCClient"));
        tc.assertTrue(reply.result().body().getJsonArray("bindings").isEmpty());

        ds_sugar.complete();
      });

      vertx.eventBus().<JsonObject>send("ds-ref", "", reply -> {
        tc.assertTrue(reply.succeeded());
        tc.assertTrue(reply.result().body().getString("ref_del").contains("JdbcServiceReference"));
        tc.assertTrue(reply.result().body().getString("client_del").contains("JDBCClientImpl"));
        tc.assertTrue(reply.result().body().getJsonArray("bindings").isEmpty());

        ds_ref.complete();
      });

      vertx.eventBus().<JsonObject>send("redis-sugar", "", reply -> {
        tc.assertTrue(reply.succeeded());
        tc.assertTrue(reply.result().body().getString("client_del").contains("RedisClient"));
        tc.assertTrue(reply.result().body().getJsonArray("bindings").isEmpty());

        redis_sugar.complete();
      });

      vertx.eventBus().<JsonObject>send("redis-ref", "", reply -> {
        tc.assertTrue(reply.succeeded());
        tc.assertTrue(reply.result().body().getString("ref_del").contains("RedisServiceReference"));
        tc.assertTrue(reply.result().body().getString("client_del").contains("RedisClientImpl"));
        tc.assertTrue(reply.result().body().getJsonArray("bindings").isEmpty());

        redis_ref.complete();
      });

      vertx.eventBus().<JsonObject>send("mongo-sugar", "", reply -> {
        tc.assertTrue(reply.succeeded());
        tc.assertTrue(reply.result().body().getString("client_del").contains("MongoClient"));
        tc.assertTrue(reply.result().body().getJsonArray("bindings").isEmpty());

        mongo_sugar.complete();
      });

      vertx.eventBus().<JsonObject>send("mongo-ref", "", reply -> {
        tc.assertTrue(reply.succeeded());
        tc.assertTrue(reply.result().body().getString("ref_del").contains("MongoServiceReference"));
        tc.assertTrue(reply.result().body().getString("client_del").contains("MongoClientImpl"));
        tc.assertTrue(reply.result().body().getJsonArray("bindings").isEmpty());

        mongo_ref.complete();
      });


      vertx.eventBus().<JsonObject>send("source1-sugar", "", reply -> {
        tc.assertTrue(reply.succeeded());
        tc.assertTrue(reply.result().body().getString("client_del").contains("HandlerRegistration"));
        tc.assertTrue(reply.result().body().getJsonArray("bindings").isEmpty());

        ms_sugar.complete();
      });

      vertx.eventBus().<JsonObject>send("source1-ref", "", reply -> {
        tc.assertTrue(reply.succeeded());
        tc.assertTrue(reply.result().body().getString("ref_del").contains("MessageSourceReference"));
        tc.assertTrue(reply.result().body().getString("client_del").contains("HandlerRegistration"));
        tc.assertTrue(reply.result().body().getJsonArray("bindings").isEmpty());

        ms_ref.complete();
      });

      deployment.complete();
    });
  }
}
