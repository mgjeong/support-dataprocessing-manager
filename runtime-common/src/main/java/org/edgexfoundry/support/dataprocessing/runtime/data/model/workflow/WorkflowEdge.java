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

package org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowStream.Grouping;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowEdge extends Format {

  private Long id;
  private Long workflowId;
  private Long fromId;
  private Long toId;
  private List<StreamGrouping> streamGroupings = new ArrayList<>();

  public WorkflowEdge() {

  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getWorkflowId() {
    return workflowId;
  }

  public void setWorkflowId(Long workflowId) {
    this.workflowId = workflowId;
  }

  public Long getFromId() {
    return fromId;
  }

  public void setFromId(Long fromId) {
    this.fromId = fromId;
  }

  public Long getToId() {
    return toId;
  }

  public void setToId(Long toId) {
    this.toId = toId;
  }

  public List<StreamGrouping> getStreamGroupings() {
    return streamGroupings;
  }

  public void setStreamGroupings(
      List<StreamGrouping> streamGroupings) {
    if (streamGroupings == null) {
      throw new RuntimeException("Invalid stream groupings");
    }
    this.streamGroupings = streamGroupings;
  }

  @JsonIgnore
  public String getStreamGroupingsStr() {
    if (this.streamGroupings.isEmpty()) {
      return "[]";
    } else {
      try {
        return mapper.writeValueAsString(this.streamGroupings);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @JsonIgnore
  public void setStreamGroupingsStr(String groupings) {
    try {
      if (StringUtils.isEmpty(groupings)) {
        throw new RuntimeException("Invalid groupings");
      }
      this.streamGroupings = mapper
          .readValue(groupings, new TypeReference<List<StreamGrouping>>() {
          });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @JsonInclude(Include.NON_NULL)
  public static class StreamGrouping extends Format {

    private Long streamId;
    private Grouping grouping;
    private List<String> fields;

    public Long getStreamId() {
      return streamId;
    }

    public void setStreamId(Long streamId) {
      this.streamId = streamId;
    }

    public Grouping getGrouping() {
      return grouping;
    }

    public void setGrouping(
        Grouping grouping) {
      this.grouping = grouping;
    }

    public List<String> getFields() {
      return fields;
    }

    public void setFields(List<String> fields) {
      this.fields = fields;
    }
  }
}
