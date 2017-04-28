package org.jenkinsci.plugins.pipeline.github;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.model.Job;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMSource;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.jenkinsci.plugins.github_branch_source.Connector;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.PullRequestSCMHead;
import org.jenkinsci.plugins.pipeline.github.extension.ExtendedGitHubClient;

import javax.annotation.Nonnull;
import java.net.URI;

/**
 * Various utility methods taken mostly from the GitHub Branch Source Plugin (because
 * they were not public).
 *
 * @see org.jenkinsci.plugins.github_branch_source.GitHubBuildStatusNotification
 *
 * @author Aaron Whiteside
 */
public class GitHubHelper {

    private GitHubHelper(){
        // go away
    }

    public static ExtendedGitHubClient getGitHubClient(@Nonnull final Job<?,?> job) {
        SCMSource scmSource = SCMSource.SourceByItem.findSource(job);
        if (scmSource instanceof GitHubSCMSource) {
            GitHubSCMSource gitHubSource = (GitHubSCMSource) scmSource;

            ExtendedGitHubClient client;
            if (gitHubSource.getApiUri() == null) {
                client = new ExtendedGitHubClient();
            } else {
                URI uri = URI.create(gitHubSource.getApiUri());
                client = new ExtendedGitHubClient(uri.getHost(), uri.getPort(), uri.getScheme());
            }

            // configure credentials
            if (gitHubSource.getCredentialsId() != null) {
                StandardCredentials credentials = Connector.lookupScanCredentials(
                        job, gitHubSource.getApiUri(), gitHubSource.getCredentialsId());

                if (credentials instanceof StandardUsernamePasswordCredentials) {
                    StandardUsernamePasswordCredentials c = (StandardUsernamePasswordCredentials) credentials;
                    String userName = c.getUsername();
                    String password = c.getPassword().getPlainText();
                    client.setCredentials(userName, password);
                }
            }
            return client;
        }
        throw new IllegalArgumentException("Job's SCM is not GitHub.");
    }

    public static RepositoryId getRepositoryId(@Nonnull final Job<?,?> job) {
        SCMSource src = SCMSource.SourceByItem.findSource(job);
        if (src instanceof GitHubSCMSource) {
            GitHubSCMSource source = (GitHubSCMSource) src;
            if (source.getScanCredentialsId() != null) {
                return RepositoryId.create(source.getRepoOwner(), source.getRepository());
            }
        }
        return null;
    }

    public static PullRequestSCMHead getPullRequest(@Nonnull final Job job) throws Exception {
        PullRequestSCMHead head = (PullRequestSCMHead) SCMHead.HeadByItem.findHead(job);
        if (head == null) {
            throw new IllegalStateException("Build is not a pull request");
        }
        return head;
    }

    public static String userToLogin(final User user) {
        return user == null ? null : user.getLogin();
    }

}
