package org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowDetailed extends Format {

  public enum RunningStatus {
    RUNNING, NOT_RUNNING, UNKNOWN
  }

  private Workflow workflow;
  private RunningStatus running;

  public WorkflowDetailed() {

  }

  public Workflow getWorkflow() {
    return workflow;
  }

  public void setWorkflow(Workflow workflow) {
    if (workflow == null) {
      throw new RuntimeException("Invalid workflow");
    }
    this.workflow = workflow;
  }

  public RunningStatus getRunning() {
    return running;
  }

  public void setRunning(RunningStatus running) {
    if (running == null) {
      throw new RuntimeException("Invalid running status");
    }
    this.running = running;
  }
}