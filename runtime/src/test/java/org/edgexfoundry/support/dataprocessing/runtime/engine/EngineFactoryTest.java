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

package org.edgexfoundry.support.dataprocessing.runtime.engine;

import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData.EngineType;
import org.junit.Assert;
import org.junit.Test;

public class EngineFactoryTest {

  @Test
  public void testCreateEngine() throws Exception {
    Engine fw = EngineFactory.createEngine(WorkflowData.EngineType.FLINK, "localhost", 8081);
    Assert.assertNotNull(fw);
    fw = EngineFactory.createEngine(WorkflowData.EngineType.KAPACITOR, "localhost", 8081);
    Assert.assertNotNull(fw);
  }

  @Test
  public void testInvalidEngine() {
    try {
      EngineFactory.createEngine(null, "localhost", 1111);
      Assert.fail("Should not reach here.");
    } catch (Exception e) {

    }

    try {
      EngineFactory.createEngine(EngineType.UNKNOWN, "localhost", 1111);
      Assert.fail("Should not reach here.");
    } catch (Exception e) {

    }
  }

  @Test
  public void testConstructor() {
    // Is this really necessary?
    EngineFactory engineFactory = new EngineFactory();
    Assert.assertNotNull(engineFactory);
  }
}
