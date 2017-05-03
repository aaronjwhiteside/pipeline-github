package org.jenkinsci.plugins.pipeline.github;

import org.jenkinsci.plugins.workflow.cps.CpsScript;
import org.jenkinsci.plugins.workflow.cps.GlobalVariable;

import javax.annotation.Nonnull;

/**
 * Factory for our {@link PullRequestGroovyObject} instance.
 *
 * @author Aaron Whiteside
 * @see PullRequestGroovyObject
 */
public class PullRequestGlobalVariable extends GlobalVariable {

    @Nonnull
    @Override
    public String getName() {
        return "pullRequest";
    }

    @Nonnull
    @Override
    public Object getValue(@Nonnull final CpsScript script) throws Exception {
        return new PullRequestGroovyObject(script);
    }

}
