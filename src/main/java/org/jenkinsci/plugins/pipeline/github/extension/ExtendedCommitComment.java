package org.jenkinsci.plugins.pipeline.github.extension;

import org.eclipse.egit.github.core.Comment;

/**
 * @author Aaron Whiteside
 */
public class ExtendedCommitComment extends Comment {
    private static final long serialVersionUID = 4834285683963788350L;

    private Integer line;
    private Integer position;
    private Integer originalPosition;
    private String commitId;
    private String originalCommitId;
    private String path;
    private String diffHunk;

    public Integer getLine() {
        return line;
    }

    public void setLine(final Integer line) {
        this.line = line;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(final Integer position) {
        this.position = position;
    }

    public Integer getOriginalPosition() {
        return originalPosition;
    }

    public void setOriginalPosition(final Integer originalPosition) {
        this.originalPosition = originalPosition;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(final String commitId) {
        this.commitId = commitId;
    }

    public String getOriginalCommitId() {
        return originalCommitId;
    }

    public void setOriginalCommitId(final String originalCommitId) {
        this.originalCommitId = originalCommitId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public String getDiffHunk() {
        return diffHunk;
    }

    public void setDiffHunk(final String diffHunk) {
        this.diffHunk = diffHunk;
    }
}
