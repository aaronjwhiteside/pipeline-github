package org.jenkinsci.plugins.pipeline.github.trigger;

import hudson.model.Cause;

/**
 * Represents the user who authored the comment that triggered the build.
 *
 * @author Aaron Whiteside
 */
public class IssueCommentCause extends Cause {
    private final String userLogin;

    public IssueCommentCause(final String userLogin) {
        this.userLogin = userLogin;
    }

    @Override
    public String getShortDescription() {
        return String.format("Started by an IssueComment from user: %s", userLogin);
    }
}
