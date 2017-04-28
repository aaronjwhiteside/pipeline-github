package org.jenkinsci.plugins.pipeline.github;

import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingPropertyException;
import groovy.lang.ReadOnlyPropertyException;
import org.eclipse.egit.github.core.CommitFile;

/**
 * @author Aaron Whiteside
 */
public class CommitFileGroovyObject extends GroovyObjectSupport {
    private final CommitFile file;

    CommitFileGroovyObject(final CommitFile file) {
        this.file = file;
    }

    @Override
    public Object getProperty(final String property) {
        if (property == null) {
            throw new MissingPropertyException("null", this.getClass());
        }

        switch (property) {
            case "sha":
                return file.getSha();
            case "filename":
                return file.getFilename();
            case "status":
                return file.getStatus();
            case "patch":
                return file.getPatch();
            case "additions":
                return file.getAdditions();
            case "deletions":
                return file.getDeletions();
            case "changes":
                return file.getChanges();
            case "raw_url":
                return file.getRawUrl();
            case "blob_url":
                return file.getBlobUrl();
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
            case "sha":
            case "filename":
            case "status":
            case "patch":
            case "additions":
            case "deletions":
            case "changes":
            case "raw_url":
            case "blob_url":
                throw new ReadOnlyPropertyException(property, getClass());
            default:
                throw new MissingPropertyException(property, getClass());
        }
    }
}
