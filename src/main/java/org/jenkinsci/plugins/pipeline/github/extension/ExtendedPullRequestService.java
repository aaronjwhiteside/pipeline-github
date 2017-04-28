package org.jenkinsci.plugins.pipeline.github.extension;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.service.PullRequestService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Aaron Whiteside
 */
public class ExtendedPullRequestService extends PullRequestService {

    public ExtendedPullRequestService(final ExtendedGitHubClient client) {
        super(client);
    }

    @Override
    public ExtendedGitHubClient getClient() {
        return (ExtendedGitHubClient) super.getClient();
    }

    public ExtendedPullRequest editPullRequest(final IRepositoryIdProvider repository, final ExtendedPullRequest request) throws IOException {
        Objects.requireNonNull(request, "request cannot be null");
        String id = this.getId(repository);
        StringBuilder uri = new StringBuilder("/repos");
        uri.append('/').append(id);
        uri.append("/pulls");
        uri.append('/').append(request.getNumber());
        Map<String, Object> params = editPrMap(request);
        return (ExtendedPullRequest) getClient().patch(uri.toString(), params, ExtendedPullRequest.class);
    }

    protected Map<String, Object> editPrMap(final ExtendedPullRequest request) {
        Map<String, Object> params = new HashMap<>();
        if (request.getTitle() != null) {
            params.put("title", request.getTitle());
        }

        if (request.getBody() != null) {
            params.put("body", request.getBody());
        }

        if (request.getState() != null) {
            params.put("state", request.getState());
        }

        if (request.getBase() != null) {
            params.put("base", request.getBase().getRef());
        }

        if (request.isMaintainerCanModify() != null) {
            params.put("maintainer_can_modify", request.isMaintainerCanModify());
        }

        return params;
    }


    @Override
    public ExtendedPullRequest getPullRequest(final IRepositoryIdProvider repository, final int id) throws IOException {
        String repoId = this.getId(repository);
        StringBuilder uri = new StringBuilder("/repos");
        uri.append('/').append(repoId);
        uri.append("/pulls");
        uri.append('/').append(id);
        GitHubRequest request = this.createRequest();
        request.setUri(uri);
        request.setType(ExtendedPullRequest.class);
        return (ExtendedPullRequest) getClient().get(request).getBody();
    }

    public ExtendedMergeStatus merge(final IRepositoryIdProvider repository,
                                     final int id,
                                     final String commitTitle,
                                     final String commitMessage,
                                     final String sha,
                                     final String mergeMethod) throws IOException {
        String repoId = this.getId(repository);
        StringBuilder uri = new StringBuilder("/repos");
        uri.append('/').append(repoId);
        uri.append("/pulls");
        uri.append('/').append(id);
        uri.append("/merge");

        Map<String, String> params = new HashMap<>();
        params.put("commit_title", commitTitle);
        params.put("commit_message", commitMessage);
        params.put("sha", sha);
        params.put("merge_method", mergeMethod);
        return getClient().put(uri.toString(), params, ExtendedMergeStatus.class);
    }
}
