package org.jenkinsci.plugins.pipeline.github;

import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingPropertyException;
import groovy.lang.ReadOnlyPropertyException;
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.IssueService;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * @author Aaron Whiteside
 */
public class IssueCommentGroovyObject extends GroovyObjectSupport {
    private final RepositoryId base;
    private final IssueService issueService;
    private Comment comment;

    public IssueCommentGroovyObject(final Comment comment, final RepositoryId base, final IssueService issueService) {
        this.comment = comment;
        this.base = base;
        this.issueService = issueService;
    }

    @Override
    public Object getProperty(final String property) {
        if (property == null) {
            throw new MissingPropertyException("null", getClass());
        }
        switch (property) {
            case "id":
                return comment.getId();
            case "url":
                return comment.getUrl();
            case "user":
                return GitHubHelper.userToLogin(comment.getUser());
            case "body":
                return comment.getBody();
            case "created_at":
                return comment.getCreatedAt();
            case "updated_at":
                return comment.getUpdatedAt();

            default:
                throw new MissingPropertyException(property, getClass());
        }
    }

    @Override
    public void setProperty(final String property, final Object newValue) {
        if (property == null) {
            throw new MissingPropertyException("null", getClass());
        }
        switch (property) {
            case "id":
            case "url":
            case "user":
            case "created_at":
            case "updated_at":
                throw new ReadOnlyPropertyException(property, getClass());

            case "body":
                setBody(newValue.toString());
                break;

            default:
                throw new MissingPropertyException(property, getClass());
        }
    }

    private void setBody(final String body) {
        Comment edit = new Comment();
        edit.setId(comment.getId());
        edit.setBody(body);
        try {
            comment = issueService.editComment(base, edit);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Whitelisted
    public void delete() {
        try {
            issueService.deleteComment(base, comment.getId());
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
