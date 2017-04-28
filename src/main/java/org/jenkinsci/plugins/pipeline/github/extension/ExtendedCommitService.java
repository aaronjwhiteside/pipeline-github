package org.jenkinsci.plugins.pipeline.github.extension;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
        String id = this.getId(repository);
        if(sha == null) {
            throw new IllegalArgumentException("Sha cannot be null");
        } else if(sha.length() == 0) {
            throw new IllegalArgumentException("Sha cannot be empty");
        } else {
            StringBuilder uri = new StringBuilder("/repos");
            uri.append('/').append(id);
            uri.append("/commits");
            uri.append('/').append(sha);
            uri.append("/comments");
            return (ExtendedCommitComment) getClient().post(uri.toString(), comment, ExtendedCommitComment.class);
        }
    }

    public ExtendedCommitComment replyToComment(final IRepositoryIdProvider repository, final String sha,
                                                final int commentId, final String body) throws IOException {
        String id = this.getId(repository);
        if(sha == null) {
            throw new IllegalArgumentException("Sha cannot be null");
        } else if(sha.length() == 0) {
            throw new IllegalArgumentException("Sha cannot be empty");
        } else {
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
    }


}
