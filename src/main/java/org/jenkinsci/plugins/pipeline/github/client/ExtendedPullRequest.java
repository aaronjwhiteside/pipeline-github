package org.jenkinsci.plugins.pipeline.github.client;

import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.User;

import java.util.List;

/**
 * @author Aaron Whiteside
 */
public class ExtendedPullRequest extends PullRequest {
    private static final long serialVersionUID = 4674327177035503955L;

    private User closedBy;
    private List<User> assignees;
    private boolean locked;
    private String mergeCommitSha;
    private Boolean maintainerCanModify;

    public User getClosedBy() {
        return closedBy;
    }

    public void setClosedBy(final User closedBy) {
        this.closedBy = closedBy;
    }

    public List<User> getAssignees() {
        return assignees;
    }

    public void setAssignees(final List<User> assignees) {
        this.assignees = assignees;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(final boolean locked) {
        this.locked = locked;
    }

    public String getMergeCommitSha() {
        return mergeCommitSha;
    }

    public void setMergeCommitSha(final String mergeCommitSha) {
        this.mergeCommitSha = mergeCommitSha;
    }

    public Boolean isMaintainerCanModify() {
        return maintainerCanModify;
    }

    public void setMaintainerCanModify(final Boolean maintainerCanModify) {
        this.maintainerCanModify = maintainerCanModify;
    }
}
