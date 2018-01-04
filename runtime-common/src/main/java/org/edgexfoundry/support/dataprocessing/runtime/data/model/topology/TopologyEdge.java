package org.edgexfoundry.support.dataprocessing.runtime.data.model.topology;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TopologyEdge extends Format {

  private Long id;
  private Long versionId;
  private Long topologyId;
  private Long fromId;
  private Long toId;
  private Long versionTimestamp;
  private List<StreamGrouping> streamGroupings;

  public TopologyEdge() {

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

  public Long getTopologyId() {
    return topologyId;
  }

  public void setTopologyId(Long topologyId) {
    this.topologyId = topologyId;
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

  @JsonProperty("timestamp")
  public Long getVersionTimestamp() {
    return versionTimestamp;
  }

  @JsonProperty("timestamp")
  public void setVersionTimestamp(Long versionTimestamp) {
    this.versionTimestamp = versionTimestamp;
  }

  public List<StreamGrouping> getStreamGroupings() {
    return streamGroupings;
  }

  public void setStreamGroupings(
      List<StreamGrouping> streamGroupings) {
    this.streamGroupings = streamGroupings;
  }

  public static class StreamGrouping {

    private Long streamId;
    private Grouping grouping;
    private List<String> fields;

    public enum Grouping {
      SHUFFLE, FIELDS
    }

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
