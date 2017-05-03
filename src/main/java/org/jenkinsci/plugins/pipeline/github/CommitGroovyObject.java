package org.jenkinsci.plugins.pipeline.github;

import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingPropertyException;
import groovy.lang.ReadOnlyPropertyException;
import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.CommitStatus;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.RepositoryId;
import org.jenkinsci.plugins.pipeline.github.client.ExtendedCommitComment;
import org.jenkinsci.plugins.pipeline.github.client.ExtendedCommitService;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

/**
 * Groovy wrapper over a {@link RepositoryCommit}.
 *
 * Provides useful properties that allow one to iterate over a commits:
 * - Comments
 * - Files
 * - Statuses
 *
 * And a few methods to create:
 * - Review comments
 * - Statuses
 *
 * @author Aaron Whiteside
 * @see RepositoryCommit
 */
public class CommitGroovyObject extends GroovyObjectSupport {
    private final RepositoryCommit commit;
    private final ExtendedCommitService commitService;
    private final RepositoryId base;

    CommitGroovyObject(final RepositoryCommit commit,
                       final ExtendedCommitService commitService,
                       final RepositoryId base) {
        this.commit = commit;
        this.commitService = commitService;
        this.base = base;
    }

    @Override
    public Object getProperty(final String property) {
        if (property == null) {
            throw new MissingPropertyException("null", this.getClass());
        }

        switch (property) {
            case "sha":
                return commit.getSha();
            case "url":
                return commit.getUrl();
            case "author":
                return commit.getAuthor().getLogin();
            case "committer":
                return commit.getCommitter().getLogin();
            case "parents":
                return getParents();
            case "message":
                return commit.getCommit().getMessage();
            case "comment_count":
                return commit.getCommit().getCommentCount();
            case "comments":
                return getComments();
            case "additions":
                return commit.getStats().getAdditions();
            case "deletions":
                return commit.getStats().getDeletions();
            case "total_changes":
                return commit.getStats().getTotal();
            case "files":
                return getFiles();
            case "statuses":
                return getStatuses();

            default:
                throw new MissingPropertyException(property, this.getClass());
        }
    }

    private Iterable<ReviewCommentGroovyObject> getComments() {
        Stream<ReviewCommentGroovyObject> stream = StreamSupport.stream(
                commitService.pageComments2(base, commit.getSha()).spliterator(), false)
                .flatMap(Collection::stream)
                .map(c -> new ReviewCommentGroovyObject(c, base, commitService));
        return stream::iterator;
    }

    private List<CommitStatusGroovyObject> getStatuses() {
        try {
            return commitService.getStatuses(base, commit.getSha())
                    .stream()
                    .map(CommitStatusGroovyObject::new)
                    .collect(toList());
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<String> getParents() {
        return commit.getParents()
                .stream()
                .map(Commit::getSha)
                .collect(toList());
    }

    private List<CommitFileGroovyObject> getFiles() {
        return commit.getFiles()
                .stream()
                .map(CommitFileGroovyObject::new)
                .collect(toList());
    }

    @Override
    public void setProperty(final String property, final Object newValue) {
        if (property == null) {
            throw new MissingPropertyException("null", getClass());
        }
        switch (property) {
            case "sha":
            case "url":
            case "author":
            case "committer":
            case "parents":
            case "message":
            case "comment_count":
            case "comments":
            case "additions":
            case "deletions":
            case "total_changes":
            case "files":
            case "statuses":
                throw new ReadOnlyPropertyException(property, getClass());
            default:
                throw new MissingPropertyException(property, getClass());
        }
    }

    @Whitelisted
    public ReviewCommentGroovyObject comment(final Map<String, Object> params) {
        return comment((String)params.get("body"),
                       (String)params.get("path"),
                       (Integer)params.get("position"));
    }

    @Whitelisted
    public ReviewCommentGroovyObject comment(final String body,
                                             final String path,
                                             final Integer position) {
        Objects.requireNonNull(body, "body is a required argument");

        ExtendedCommitComment comment = new ExtendedCommitComment();
        comment.setBody(body);
        comment.setPath(path);
        comment.setPosition(position);
        try {
            return new ReviewCommentGroovyObject(
                    commitService.addComment(base, commit.getSha(), comment),
                    base,
                    commitService);
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
                    commitService.createStatus(base, commit.getSha(), commitStatus));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
