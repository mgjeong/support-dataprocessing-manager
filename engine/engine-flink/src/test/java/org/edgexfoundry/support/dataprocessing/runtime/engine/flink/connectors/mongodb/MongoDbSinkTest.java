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
package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.mongodb;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MongoDbSink.class, MongoClient.class})
public class MongoDbSinkTest {

  @Test
  public void testOpen() throws Exception {
    MockClient mockClient = new MockClient();
    PowerMockito.mockStatic(MongoClient.class);
    PowerMockito.whenNew(MongoClient.class).withArguments(anyString(), anyInt())
        .thenReturn(mockClient);
    MongoDbSink sink = new MongoDbSink("localhost", 55555, "db", "table");
    sink.open(null);
    DataSet dataSet = DataSet.create();
    dataSet.addRecord("{\"key\":\"value\"}");
    sink.invoke(dataSet);
    sink.close();
  }

  private class MockClient extends MongoClient {

    @Override
    public MongoDatabase getDatabase(String dbName) {
      MongoCollection collection = Mockito.mock(MongoCollection.class);
      Mockito.doNothing().when(collection).insertMany(any());

      MongoDatabase db = Mockito.mock(MongoDatabase.class);
      Mockito.when(db.getCollection(any())).thenReturn(collection);
      return db;
    }
  }
}
