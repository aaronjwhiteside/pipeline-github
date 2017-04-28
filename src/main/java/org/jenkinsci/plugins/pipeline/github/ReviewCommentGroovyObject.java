package org.jenkinsci.plugins.pipeline.github;

import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingPropertyException;
import groovy.lang.ReadOnlyPropertyException;
import org.eclipse.egit.github.core.CommitComment;
import org.eclipse.egit.github.core.RepositoryId;
import org.jenkinsci.plugins.pipeline.github.extension.ExtendedCommitService;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * @author Aaron Whiteside
 */
public class ReviewCommentGroovyObject extends GroovyObjectSupport {
    private final CommitComment commitComment;
    private final RepositoryId base;
    private final ExtendedCommitService commitService;

    public ReviewCommentGroovyObject(final CommitComment commitComment,
                                     final RepositoryId base,
                                     final ExtendedCommitService commitService) {
        this.commitComment = commitComment;
        this.base = base;
        this.commitService = commitService;
    }

    @Override
    public Object getProperty(final String property) {
        if (property == null) {
            throw new MissingPropertyException("null", this.getClass());
        }

        switch (property) {
            case "id":
                return commitComment.getId();
            case "url":
                return commitComment.getUrl();
            case "user":
                return GitHubHelper.userToLogin(commitComment.getUser());
            case "created_at":
                return commitComment.getCreatedAt();
            case "updated_at":
                return commitComment.getUpdatedAt();
            case "commit_id":
                return commitComment.getCommitId();
            case "original_commit_id":
                return commitComment.getOriginalCommitId();
            case "body":
                return commitComment.getBody();
            case "path":
                return commitComment.getPath();
            case "line":
                return commitComment.getLine();
            case "position":
                return commitComment.getPosition();
            case "original_position":
                return commitComment.getPosition();
            case "diff_hunk":
                return commitComment.getDiffHunk();

            default:
                throw new MissingPropertyException(property, this.getClass());
        }
    }

    @Override
    public void setProperty(final String property, final Object newValue) {
        if (property == null) {
            throw new MissingPropertyException("null", this.getClass());
        }

        switch (property) {
            case "id":
            case "url":
            case "user":
            case "created_at":
            case "updated_at":
            case "commit_id":
            case "original_commit_id":
            case "path":
            case "line":
            case "position":
            case "original_position":
            case "diff_hunk":
                throw new ReadOnlyPropertyException(property, getClass());

            case "body":
                Objects.requireNonNull(newValue, "body cannot be null");
                setBody(newValue.toString());
                break;

            default:
                throw new MissingPropertyException(property, this.getClass());
        }
    }

    private void setBody(final String body) {
        CommitComment edit = new CommitComment();
        edit.setId(commitComment.getId());
        edit.setBody(body);
        try {
            commitService.editComment(base, edit);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Whitelisted
    public void delete() {
        try {
            commitService.deleteComment(base, commitComment.getId());
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

//    @Whitelisted
    public void replyTo(final String body) {
        Objects.requireNonNull(body, "body is a required argument");
        try {
            commitService.replyToComment(base, commitComment.getCommitId(), (int) commitComment.getId(), body);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
