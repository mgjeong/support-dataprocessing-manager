package org.edgexfoundry.support.dataprocessing.runtime.db;

import java.util.Collection;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.Job;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class JobTableManagerTest extends DatabaseTest {

  private static JobTableManager jobTable;

  @BeforeClass
  public static void setup() throws Exception {
    jobTable = JobTableManager.getInstance();
    java.lang.reflect.Field databaseField = AbstractStorageManager.class.getDeclaredField("database");
    databaseField.setAccessible(true);
    databaseField.set(jobTable,
        DatabaseManager.getInstance().getDatabase("jdbc:sqlite:" + testDB.getAbsolutePath()));

    ResourceLoader loader = new DefaultResourceLoader(ClassLoader.getSystemClassLoader());
    Resource resource = loader.getResource("db/sqlite/create_tables.sql");
    jobTable.executeSqlScript(resource);
  }

  @Test
  public void testGettingExistingJob() {
    Job job = Job.create(1L);
    job = jobTable.addOrUpdateWorkflowJob(job);
    Assert.assertNotNull(job);

    try {
      Collection<Job> jobs = jobTable.listWorkflowJobs(1L);
      Assert.assertNotNull(jobs);
      Assert.assertEquals(1, jobs.size());

      jobTable.getWorkflowJob(job.getId());
    } finally {
      // Remove job
      jobTable.removeWorkflowJob(job.getId());
    }
  }

  @Test
  public void testGettingNonExistingJob() {
    // Invalid param
    try {
      jobTable.getWorkflowJob(null);
      Assert.fail("Should not reach here");
    } catch (Exception e) {
      // success
    }

    try {
      jobTable.getWorkflowJob("non-existing-job");
      Assert.fail("Should not reach here");
    } catch (Exception e) {
      Assert.assertTrue(e.getMessage().contains("not found"));
      // success
    }
  }

  @Test
  public void testAddWorkflowJobState() {
    Job job = Job.create(2L);
    try {
      jobTable.addOrUpdateWorkflowJobState(job.getId(), job.getState());
    } finally {
      jobTable.removeWorkflowJob(job.getId());
    }
  }

  @Test
  public void testAddInvalidWorkflowJobState() {
    Job job = Job.create(3L);
    try {
      jobTable.addOrUpdateWorkflowJobState(job.getId(), null);
      Assert.fail("Should not reach here.");
    } catch (Exception e) {
      // success
    }
  }

  @Test
  public void testAddWorkflowJob() {
    Job job = Job.create(4L);
    try {
      jobTable.addOrUpdateWorkflowJob(job);
    } finally {
      jobTable.removeWorkflowJob(job.getId());
    }
  }

  @Test
  public void testAddInvalidWorkflowJob() {
    try {
      jobTable.addOrUpdateWorkflowJob(null);
      Assert.fail("Should not reach here.");
    } catch (Exception e) {
      // success
    }
  }
}