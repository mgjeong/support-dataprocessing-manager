/*******************************************************************************
 * Copyright 2018 Samsung Electronics All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.zmq;

import static org.mockito.Mockito.mock;

import java.nio.charset.Charset;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.zmq.common.ZmqConnectionConfig;
import org.junit.Assert;
import org.junit.Test;

public class ZmqSourceTest {

  @Test
  public void testConstructor() {
    ZmqConnectionConfig.Builder builder = new ZmqConnectionConfig.Builder();
    builder.setHost("localhost").setPort(5588).setIoThreads(1);
    ZmqConnectionConfig config = builder.build();
    new ZmqSource(config, "topic", new SimpleStringSchema(Charset.defaultCharset()));
  }

  @Test
  public void testProducedType() {
    ZmqConnectionConfig.Builder builder = new ZmqConnectionConfig.Builder();
    builder.setHost("localhost").setPort(5588).setIoThreads(1);
    ZmqConnectionConfig config = builder.build();
    SimpleStringSchema schema = new SimpleStringSchema(Charset.defaultCharset());
    ZmqSource source = new ZmqSource(config, "topic", schema);
    Assert.assertNotNull(source.getProducedType());
    Assert.assertEquals(schema.getProducedType(), source.getProducedType());
  }

  @Test
  public void testConnection() throws Exception {
    ZmqConnectionConfig.Builder builder = new ZmqConnectionConfig.Builder();
    builder.setHost("localhost").setPort(5588).setIoThreads(1);
    ZmqConnectionConfig config = builder.build();
    ZmqSource source = new ZmqSource(config, "topic",
        new SimpleStringSchema(Charset.defaultCharset()));
    source.open(null);
    source.cancel();
    source.close();
  }

  @Test(timeout = 3000L)
  public void testRun() throws Exception {
    ZmqConnectionConfig.Builder builder = new ZmqConnectionConfig.Builder();
    builder.setHost("localhost").setPort(5588).setIoThreads(1);
    final ZmqConnectionConfig config = builder.build();

    SourceFunction.SourceContext sourceContext = mock(SourceFunction.SourceContext.class);
    SourceThread sourceThread = new SourceThread(config, sourceContext);
    SinkThread sinkThread = new SinkThread(config);

    try {
      sinkThread.start();
      sourceThread.start();

      Thread.sleep(500L);

      sinkThread.publish("Hello World");

      Thread.sleep(500L);
    } finally {
      System.out.println("Killing source/sink threads");
      sourceThread.close();
      sinkThread.close();
    }
  }

  private static class SourceThread extends Thread {

    private final ZmqConnectionConfig config;
    private final SourceFunction.SourceContext sourceContext;
    private ZmqSource source = null;

    public SourceThread(ZmqConnectionConfig config, SourceFunction.SourceContext sourceContext) {
      this.config = config;
      this.sourceContext = sourceContext;
    }

    @Override
    public void run() {
      try {
        this.source = new ZmqSource(config, "topic", new SimpleStringSchema());
        this.source.open(null);
        this.source.run(this.sourceContext);
      } catch (Exception e) {
        e.printStackTrace();
        Assert.fail(e.getMessage());
      } finally {
        close();
      }
    }

    public void close() {
      if (this.source != null) {
        try {
          this.source.cancel();
          this.source.close();
        } catch (Exception e) {
        }
      }
    }
  }

  private static class SinkThread extends Thread {

    private final ZmqConnectionConfig config;
    private ZmqSink sink = null;
    private boolean running = false;

    public SinkThread(ZmqConnectionConfig config) {
      this.config = config;
    }

    public void publish(String message) throws Exception {
      sink.invoke(message);
    }

    @Override
    public void run() {
      try {
        sink = new ZmqSink(config, "topic", new SimpleStringSchema());
        sink.open(null);

        this.running = true;
        while (this.running) {
          Thread.sleep(10);
        }
      } catch (Exception e) {
        e.printStackTrace();
        Assert.fail(e.getMessage());
      } finally {
        close();
      }
    }

    public void close() {
      if (sink != null) {
        try {
          sink.close();
        } catch (Exception e) {
        }
      }
      this.running = false;
    }
  }
}
