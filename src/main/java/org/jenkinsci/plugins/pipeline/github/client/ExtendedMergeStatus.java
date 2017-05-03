package org.jenkinsci.plugins.pipeline.github.client;

import org.eclipse.egit.github.core.MergeStatus;

/**
 * @author Aaron Whiteside
 */
public class ExtendedMergeStatus extends MergeStatus {
    private String documentationUrl;

    public String getDocumentationUrl() {
        return documentationUrl;
    }

    public void setDocumentationUrl(final String documentationUrl) {
        this.documentationUrl = documentationUrl;
    }
}
