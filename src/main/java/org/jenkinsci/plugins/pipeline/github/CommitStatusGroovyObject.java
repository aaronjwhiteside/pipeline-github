package org.jenkinsci.plugins.pipeline.github;

import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingPropertyException;
import groovy.lang.ReadOnlyPropertyException;
import org.eclipse.egit.github.core.CommitStatus;

/**
 * @author Aaron Whiteside
 */
public class CommitStatusGroovyObject extends GroovyObjectSupport {
    private final CommitStatus commitStatus;

    CommitStatusGroovyObject(final CommitStatus commitStatus) {
        this.commitStatus = commitStatus;
    }

    @Override
    public Object getProperty(final String property) {
        if (property == null) {
            throw new MissingPropertyException("null", this.getClass());
        }

        switch (property) {
            case "id":
                return commitStatus.getId();
            case "url":
                return commitStatus.getUrl();
            case "status":
                return commitStatus.getState();
            case "context":
                return commitStatus.getContext();
            case "description":
                return commitStatus.getDescription();
            case "target_url":
                return commitStatus.getTargetUrl();
            case "created_at":
                return commitStatus.getCreatedAt();
            case "updated_at":
                return commitStatus.getUpdatedAt();
            case "creator":
                return commitStatus.getCreator().getLogin();
            default:
                throw new MissingPropertyException(property, this.getClass());
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
            case "status":
            case "context":
            case "description":
            case "target_url":
            case "created_at":
            case "updated_at":
            case "creator":
                throw new ReadOnlyPropertyException(property, getClass());
            default:
                throw new MissingPropertyException(property, getClass());
        }
    }
}
