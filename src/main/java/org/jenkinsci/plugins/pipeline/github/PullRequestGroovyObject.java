package org.jenkinsci.plugins.pipeline.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingPropertyException;
import groovy.lang.ReadOnlyPropertyException;
import hudson.model.Job;
import hudson.model.Run;
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.CommitStatus;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.PullRequestMarker;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;
import org.jenkinsci.plugins.github_branch_source.PullRequestSCMHead;
import org.jenkinsci.plugins.pipeline.github.client.ExtendedCommitComment;
import org.jenkinsci.plugins.pipeline.github.client.ExtendedCommitService;
import org.jenkinsci.plugins.pipeline.github.client.ExtendedGitHubClient;
import org.jenkinsci.plugins.pipeline.github.client.ExtendedIssueService;
import org.jenkinsci.plugins.pipeline.github.client.ExtendedMergeStatus;
import org.jenkinsci.plugins.pipeline.github.client.ExtendedPullRequest;
import org.jenkinsci.plugins.pipeline.github.client.ExtendedPullRequestService;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import org.jenkinsci.plugins.workflow.cps.CpsScript;
import org.jenkinsci.plugins.workflow.cps.persistence.PersistIn;
import org.jenkinsci.plugins.workflow.cps.persistence.PersistenceContext;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

/**
 * Groovy object that represents a GitHub PullRequest.
 *
 * TODO: better javadoc
 *
 * @author Aaron Whiteside
 * @see ExtendedPullRequest
 */
@PersistIn(PersistenceContext.NONE)
@SuppressFBWarnings("SE_BAD_FIELD")
public class PullRequestGroovyObject extends GroovyObjectSupport implements Serializable {

    private final CpsScript script;

    private final PullRequestSCMHead pullRequestHead;
    private final RepositoryId base;
    private final RepositoryId head;
    private final ExtendedGitHubClient gitHubClient;

    private final ExtendedPullRequestService pullRequestService;
    private final ExtendedIssueService issueService;
    private final ExtendedCommitService commitService;
    private ExtendedPullRequest pullRequest;

    public PullRequestGroovyObject(@Nonnull final CpsScript script) throws Exception {
        this.script = script;
        Run<?, ?> build = script.$build();
        if (build == null) {
            throw new IllegalStateException("No associated build");
        }
        Job job = build.getParent();

        this.pullRequestHead = GitHubHelper.getPullRequest(job);

        this.base = GitHubHelper.getRepositoryId(job);
        this.head = RepositoryId.create(pullRequestHead.getSourceOwner(), pullRequestHead.getSourceRepo());

        this.gitHubClient = GitHubHelper.getGitHubClient(job);
        this.pullRequestService = new ExtendedPullRequestService(gitHubClient);
        this.issueService = new ExtendedIssueService(gitHubClient);
        this.commitService = new ExtendedCommitService(gitHubClient);
        this.pullRequest = pullRequestService.getPullRequest(base, pullRequestHead.getNumber());
    }

    @Override
    public Object getProperty(final String property) {
        if (property == null) {
            throw new MissingPropertyException("null", this.getClass());
        }

        switch (property) {
            case "id":
                return pullRequest.getId();
            case "state":
                return pullRequest.getState();
            case "number":
                return pullRequest.getNumber();
            case "url":
                return pullRequest.getHtmlUrl();
            case "patch_url":
                return pullRequest.getPatchUrl();
            case "diff_url":
                return pullRequest.getDiffUrl();
            case "issue_url":
                return pullRequest.getIssueUrl();
            case "title":
                return pullRequest.getTitle();
            case "body":
                return pullRequest.getBody();
            case "locked":
                return pullRequest.isLocked();

            case "milestone":
                return pullRequest.getMilestone().getNumber();
            case "head":
                return pullRequest.getHead().getSha();
            case "base":
                return pullRequest.getBase().getRef();

            case "files":
                return getFiles();
            case "assignees":
                return getAssignees();
            case "commits":
                return getCommits();
            case "comments":
                return getComments();
            case "review_comments":
                return getReviewComments();
            case "labels":
                return getLabels();
            case "statuses":
                return getStatuses();
            case "requested_reviewers":
                return getRequestedReviewers();

            case "updated_at":
                return pullRequest.getUpdatedAt();
            case "created_at":
                return pullRequest.getCreatedAt();
            case "created_by":
                return GitHubHelper.userToLogin(pullRequest.getUser());
            case "closed_at":
                return pullRequest.getCreatedAt();
            case "closed_by":
                return GitHubHelper.userToLogin(pullRequest.getClosedBy());
            case "merged_at":
                return pullRequest.getMergedAt();
            case "merged_by":
                return GitHubHelper.userToLogin(pullRequest.getMergedBy());

            case "commit_count":
                return pullRequest.getCommits();
            case "comment_count":
                return pullRequest.getComments();
            case "additions":
                return pullRequest.getAdditions();
            case "deletions":
                return pullRequest.getDeletions();
            case "changed_files":
                return pullRequest.getChangedFiles();

            case "merged":
                return pullRequest.isMerged();
            case "mergeable":
                return pullRequest.isMergeable();
            case "merge_commit_sha":
                return pullRequest.getMergeCommitSha();
            case "maintainer_can_modify":
                return pullRequest.isMaintainerCanModify();

            default:
                throw new MissingPropertyException(property, this.getClass());
        }
    }

    private Iterable<String> getRequestedReviewers() {
        Stream<String> stream = StreamSupport
                .stream(pullRequestService.pageRequestedReviewers(base, pullRequest.getNumber())
                        .spliterator(), false)
                .flatMap(Collection::stream)
                .map(User::getLogin);

        return stream::iterator;
    }

    private List<CommitStatusGroovyObject> getStatuses() {
        try {
            return commitService.getStatuses(base, pullRequest.getHead().getSha())
                    .stream()
                    .map(CommitStatusGroovyObject::new)
                    .collect(toList());
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Iterable<String> getLabels() {
        Stream<String> stream = StreamSupport
                .stream(issueService.getLabels(base, pullRequest.getNumber())
                        .spliterator(), false)
                .flatMap(Collection::stream)
                .map(Label::getName);

        return stream::iterator;
    }

    private List<String> getAssignees() {
        return pullRequest.getAssignees()
                .stream()
                .map(User::getLogin)
                .collect(toList());
    }

    private Iterable<CommitGroovyObject> getCommits() {
        try {
            Stream<CommitGroovyObject> steam = pullRequestService
                    .getCommits(base, pullRequestHead.getNumber())
                    .stream()
                    .map(c -> new CommitGroovyObject(c, commitService, base));

            return steam::iterator;
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Iterable<IssueCommentGroovyObject> getComments() {
        try {
            Stream<IssueCommentGroovyObject> stream = issueService
                    .getComments(base, pullRequestHead.getNumber())
                    .stream()
                    .map(c -> new IssueCommentGroovyObject(c, base, issueService));

            return stream::iterator;
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Iterable<ReviewCommentGroovyObject> getReviewComments() {
        Stream<ReviewCommentGroovyObject> stream = StreamSupport
                .stream(pullRequestService.pageComments2(base,
                        pullRequestHead.getNumber()).spliterator(), false)
                .flatMap(Collection::stream)
                .map(c -> new ReviewCommentGroovyObject(c, base, commitService));
        return stream::iterator;
    }

    private List<CommitFileGroovyObject> getFiles() {
        try {
            return pullRequestService.getFiles(base, pullRequestHead.getNumber())
                    .stream()
                    .map(CommitFileGroovyObject::new)
                    .collect(toList());
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void setProperty(final String property, final Object newValue) {
        if (property == null) {
            throw new MissingPropertyException("null", this.getClass());
        }

        switch (property) {
            // writable properties
            case "state":
                Objects.requireNonNull(newValue, "state cannot be null");
                setState(newValue.toString());
                break;
            case "title":
                Objects.requireNonNull(newValue, "title cannot be null");
                setTitle(newValue.toString());
                break;
            case "body":
                Objects.requireNonNull(newValue, "body cannot be null");
                setBody(newValue.toString());
                break;
            case "base":
                Objects.requireNonNull(newValue, "base cannot be null");
                setBase(newValue.toString());
                break;
            case "locked":
                Objects.requireNonNull(newValue, "locked cannot be null");
                setLocked(Boolean.valueOf(newValue.toString()));
                break;
            case "labels":
                setLabels(newValue);
                break;
            case "milestone":
//                setMilestone(Integer.valueOf(newValue.toString()));
                break;
            case "maintainer_can_modify":
                Objects.requireNonNull(newValue, "maintainer_can_modify cannot be null");
                setMaintainerCanModify(Boolean.valueOf(newValue.toString()));
                break;

            // read only properties
            case "id":
            case "number":
            case "url":
            case "patch_url":
            case "diff_url":
            case "issue_url":

            case "head":

            case "files":
            case "assignees":
            case "commits":
            case "comments":
            case "review_comments":
            case "statuses":
            case "requested_reviewers":

            case "updated_at":
            case "created_at":
            case "created_by":
            case "closed_at":
            case "closed_by":
            case "merged_at":
            case "merged_by":

            case "commit_count":
            case "comment_count":
            case "additions":
            case "deletions":
            case "changed_files":
            case "merged":
            case "mergeable":
            case "merge_commit_sha":
                throw new ReadOnlyPropertyException(property, this.getClass());

            // unknown properties
            default:
                throw new MissingPropertyException(property, this.getClass());
        }
    }

    private void setMilestone(final int milestoneNumber) {
        // todo
    }

    private void setLocked(final boolean locked) {
        try {
            if (locked) {
                issueService.lockIssue(base, pullRequest.getNumber());
            } else {
                issueService.unlockIssue(base, pullRequest.getNumber());
            }
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void setTitle(final String title) {
        ExtendedPullRequest edit = new ExtendedPullRequest();
        edit.setNumber(pullRequest.getNumber());
        edit.setTitle(title);
        try {
            pullRequest = pullRequestService.editPullRequest(base, edit);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void setBody(final String body) {
        ExtendedPullRequest edit = new ExtendedPullRequest();
        edit.setNumber(pullRequest.getNumber());
        edit.setBody(body);
        try {
            pullRequest = pullRequestService.editPullRequest(base, edit);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void setState(final String state) {
        ExtendedPullRequest edit = new ExtendedPullRequest();
        edit.setNumber(pullRequest.getNumber());
        edit.setState(state);
        try {
            pullRequest = pullRequestService.editPullRequest(base, edit);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void setBase(final String newBase) {
        ExtendedPullRequest edit = new ExtendedPullRequest();
        edit.setNumber(pullRequest.getNumber());
        edit.setBase(new PullRequestMarker().setRef(newBase));
        try {
            pullRequest = pullRequestService.editPullRequest(base, edit);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void setMaintainerCanModify(final boolean value) {
        ExtendedPullRequest edit = new ExtendedPullRequest();
        edit.setNumber(pullRequest.getNumber());
        edit.setMaintainerCanModify(value);
        try {
            pullRequest = pullRequestService.editPullRequest(base, edit);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void setLabels(final Object labels) {
        if (labels == null) {
            setLabels(null);
        } else if (labels instanceof Collection) {
            @SuppressWarnings("unchecked")
            List<String> temp = ((Collection<Object>)labels)
                    .stream()
                    .map(Object::toString)
                    .collect(toList());
            setLabels(temp);
        } else {
            throw new IllegalArgumentException("must be of type Collection<String>");
        }
    }

    private void setLabels(List<String> labels) {
        if (labels == null) {
            labels = Collections.emptyList();
        }
        try {
            issueService.setLabels(base,
                    pullRequest.getNumber(),
                    labels.toArray(new String[labels.size()]));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Whitelisted
    public void createReviewRequests(final String...reviewers) {
        Objects.requireNonNull(reviewers, "reviewers cannot be null");
        try {
            pullRequestService.createReviewRequests(base, pullRequest.getNumber(), reviewers);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Whitelisted
    public void deleteReviewRequests(final String...reviewers) {
        Objects.requireNonNull(reviewers, "reviewers cannot be null");
        try {
            pullRequestService.deleteReviewRequests(base, pullRequest.getNumber(), reviewers);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Whitelisted
    public void addLabels(final String...labels) {
        Objects.requireNonNull(labels, "labels is a required argument");
        try {
            issueService.addLabels(base, pullRequest.getNumber(), labels);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Whitelisted
    public void removeLabel(final String label) {
        Objects.requireNonNull(label, "label is a required argument");
        try {
            issueService.removeLabel(base, pullRequest.getNumber(), label);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Whitelisted
    public void addAssignees(final String...assignees) {
        Objects.requireNonNull(assignees, "assignees is a required argument");
        try {
            issueService.addAssignees(base, pullRequest.getNumber(), assignees);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Whitelisted
    public void removeAssignees(final String...assignees) {
        Objects.requireNonNull(assignees, "assignees is a required argument");
        try {
            issueService.removeAssignees(base, pullRequest.getNumber(), assignees);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Whitelisted
    public CommitStatusGroovyObject createStatus(final Map<String, String> params) {
        return createStatus(params.get("status"),
                            params.get("context"),
                            params.get("description"),
                            params.get("targetUrl"));
    }

    @Whitelisted
    public CommitStatusGroovyObject createStatus(final String status,
                                                 final String context,
                                                 final String description,
                                                 final String targetUrl) {
        Objects.requireNonNull(status, "status is a required argument");

        CommitStatus commitStatus = new CommitStatus();
        commitStatus.setState(status);
        commitStatus.setContext(context);
        commitStatus.setDescription(description);
        commitStatus.setTargetUrl(targetUrl);
        try {
            return new CommitStatusGroovyObject(
                    commitService.createStatus(head, pullRequest.getHead().getSha(), commitStatus));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Whitelisted
    public ReviewCommentGroovyObject reviewComment(final String commitId,
                                                   final String path,
                                                   final int position,
                                                   final String body) {
        Objects.requireNonNull(commitId, "commitId is a required argument");
        Objects.requireNonNull(path, "path is a required argument");
        Objects.requireNonNull(body, "body is a required argument");

        ExtendedCommitComment comment = new ExtendedCommitComment();
        comment.setCommitId(commitId);
        comment.setPath(path);
        comment.setPosition(position);
        comment.setBody(body);
        try {
            return new ReviewCommentGroovyObject(
                    pullRequestService.createComment2(base, pullRequestHead.getNumber(), comment),
                    base,
                    commitService);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Whitelisted
    public ReviewCommentGroovyObject replyToReviewComment(final long commentId, final String body) {
        Objects.requireNonNull(body, "body is a required argument");
        try {
            return new ReviewCommentGroovyObject(
                    pullRequestService.replyToComment2(base, pullRequestHead.getNumber(), (int) commentId, body),
                    base,
                    commitService);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Whitelisted
    public void deleteReviewComment(final long commentId) {
        try {
            pullRequestService.deleteComment(base, commentId);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Whitelisted
    public ReviewCommentGroovyObject editReviewComment(final long commentId, final String body) {
        Objects.requireNonNull(body, "body is a required argument");

        ExtendedCommitComment comment = new ExtendedCommitComment();
        comment.setId(commentId);
        comment.setBody(body);
        try {
            return new ReviewCommentGroovyObject(pullRequestService.editComment2(base, comment), base, commitService);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Whitelisted
    public IssueCommentGroovyObject comment(final String body) {
        Objects.requireNonNull(body, "body is a required argument");

        try {
            return new IssueCommentGroovyObject(
                    issueService.createComment(base, pullRequestHead.getNumber(), body),
                    base,
                    issueService);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Whitelisted
    public IssueCommentGroovyObject editComment(final long commentId, final String body) {
        Objects.requireNonNull(body, "body is a required argument");

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setBody(body);
        try {
            return new IssueCommentGroovyObject(
                    issueService.editComment(base, comment),
                    base,
                    issueService);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Whitelisted
    public void deleteComment(final long commentId) {
        try {
            issueService.deleteComment(base, commentId);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Whitelisted
    public String merge(final Map<String, String> params) {
        return merge(params.get("commitTitle"),
                params.get("commitMessage"),
                params.get("sha"),
                params.get("mergeMethod"));
    }

    @Whitelisted
    public String merge(final String commitTitle,
                        final String commitMessage,
                        final String sha,
                        final String mergeMethod) {
        try {
            ExtendedMergeStatus status = pullRequestService.merge(base,
                    pullRequestHead.getNumber(),
                    commitTitle,
                    commitMessage,
                    sha,
                    mergeMethod);
            if (status.isMerged()) {
                return status.getSha();
            } else {
                throw new RuntimeException(status.getMessage());
            }
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Whitelisted
    public void refresh() {
        try {
            pullRequest = pullRequestService.getPullRequest(base, pullRequest.getNumber());
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Whitelisted
    public void setCredentials(final String userName, final String password) {
        gitHubClient.setCredentials(userName, password);
    }
}
