package org.jenkinsci.plugins.pipeline.github.extension;

import com.google.gson.reflect.TypeToken;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.client.PagedRequest;
import org.eclipse.egit.github.core.service.CommitService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Aaron Whiteside
 */
public class ExtendedCommitService extends CommitService {

    public ExtendedCommitService(final ExtendedGitHubClient client) {
        super(client);
    }

    @Override
    public ExtendedGitHubClient getClient() {
        return (ExtendedGitHubClient) super.getClient();
    }

    public ExtendedCommitComment addComment(final IRepositoryIdProvider repository,
                                            final String sha, final ExtendedCommitComment comment) throws IOException {
        Objects.requireNonNull(sha, "sha cannot be null");
        if(sha.isEmpty()) {
            throw new IllegalArgumentException("sha cannot be empty");
        }

        String id = this.getId(repository);
        StringBuilder uri = new StringBuilder("/repos");
        uri.append('/').append(id);
        uri.append("/commits");
        uri.append('/').append(sha);
        uri.append("/comments");
        return (ExtendedCommitComment) getClient().post(uri.toString(), comment, ExtendedCommitComment.class);
    }

    public ExtendedCommitComment replyToComment(final IRepositoryIdProvider repository, final String sha,
                                                final int commentId, final String body) throws IOException {
        Objects.requireNonNull(sha, "sha cannot be null");
        if(sha.isEmpty()) {
            throw new IllegalArgumentException("Sha cannot be empty");
        }

        String id = this.getId(repository);
        StringBuilder uri = new StringBuilder("/repos");
        uri.append('/').append(id);
        uri.append("/commits");
        uri.append('/').append(sha);
        uri.append("/comments");

        Map<String, String> params = new HashMap<>();
        params.put("in_reply_to", Integer.toString(commentId));
        params.put("body", body);
        return (ExtendedCommitComment) getClient().post(uri.toString(), params, ExtendedCommitComment.class);
    }

    public ExtendedCommitComment editComment2(final IRepositoryIdProvider repository, final ExtendedCommitComment comment)
            throws IOException {
        Objects.requireNonNull(comment, "comment cannot be null");

        String id = this.getId(repository);
        StringBuilder uri = new StringBuilder("/repos");
        uri.append('/').append(id);
        uri.append("/comments");
        uri.append('/').append(comment.getId());
        return (ExtendedCommitComment)this.client.post(uri.toString(), comment, ExtendedCommitComment.class);
    }

    public PageIterator<ExtendedCommitComment> pageComments2(final IRepositoryIdProvider repository,
                                                             final String sha) {
        return this.pageComments2(repository, sha, 100);
    }

    public PageIterator<ExtendedCommitComment> pageComments2(final IRepositoryIdProvider repository,
                                                             final String sha,
                                                             final int size) {
        return this.pageComments2(repository, sha, 1, size);
    }

    public PageIterator<ExtendedCommitComment> pageComments2(final IRepositoryIdProvider repository,
                                                             final String sha,
                                                             final int start,
                                                             final int size) {
        Objects.requireNonNull(sha, "sha cannot be null");
        if(sha.isEmpty()) {
            throw new IllegalArgumentException("sha cannot be empty");
        }

        String id = this.getId(repository);
        StringBuilder uri = new StringBuilder("/repos");
        uri.append('/').append(id);
        uri.append("/commits");
        uri.append('/').append(sha);
        uri.append("/comments");
        PagedRequest<ExtendedCommitComment> request = createPagedRequest(start, size);
        request.setUri(uri);
        request.setType((new TypeToken<List<ExtendedCommitComment>>() {}).getType());
        return createPageIterator(request);
    }
}
