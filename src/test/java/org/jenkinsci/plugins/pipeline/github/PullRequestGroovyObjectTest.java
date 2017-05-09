package org.jenkinsci.plugins.pipeline.github;

import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * @author Aaron Whiteside
 */
public class PullRequestGroovyObjectTest {

    @Rule
    public JenkinsRule r = new JenkinsRule();

    @Test
    public void testReadOnlyProperties() throws Exception {
        WorkflowJob job = r.createProject(WorkflowJob.class, "p");
        r.getInstance().getAllItems().forEach(System.out::println);
    }

}