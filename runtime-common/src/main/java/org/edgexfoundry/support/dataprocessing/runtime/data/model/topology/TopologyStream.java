package org.edgexfoundry.support.dataprocessing.runtime.data.model.topology;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.Schema.Type;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TopologyStream extends Format {

  private Long id;
  private Long versionId;
  private String streamId;
  private String description;
  private Long topologyId;
  private List<Field> fields;
  private Long versionTimestamp;

  public TopologyStream() {

  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getVersionId() {
    return versionId;
  }

  public void setVersionId(Long versionId) {
    this.versionId = versionId;
  }

  public String getStreamId() {
    return streamId;
  }

  public void setStreamId(String streamId) {
    this.streamId = streamId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Long getTopologyId() {
    return topologyId;
  }

  public void setTopologyId(Long topologyId) {
    this.topologyId = topologyId;
  }

  public List<Field> getFields() {
    return fields;
  }

  public void setFields(List<Field> fields) {
    this.fields = fields;
  }

  public Long getVersionTimestamp() {
    return versionTimestamp;
  }

  public void setVersionTimestamp(Long versionTimestamp) {
    this.versionTimestamp = versionTimestamp;
  }

  @JsonInclude(Include.NON_NULL)
  public static class Field {

    private String name;
    private Schema.Type type;
    boolean optional;

    public Field() {

    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public Type getType() {
      return type;
    }

    public void setType(Type type) {
      this.type = type;
    }

    public boolean isOptional() {
      return optional;
    }

    public void setOptional(boolean optional) {
      this.optional = optional;
    }
  }
}
