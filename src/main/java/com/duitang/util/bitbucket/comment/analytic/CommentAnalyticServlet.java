package com.duitang.util.bitbucket.comment.analytic;

import com.atlassian.bitbucket.NoSuchEntityException;
import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.i18n.I18nService;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.atlassian.bitbucket.repository.RefService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.repository.RepositoryService;
import com.atlassian.bitbucket.throttle.ResourceBusyException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.apache.commons.lang.StringUtils.trimToNull;

public class CommentAnalyticServlet extends HttpServlet {

  /**
   * {@link Pattern} for parsing the project key and repository name from the URI. This mirrors the
   * Bitbucket core URIs for consistency.
   */
  private static final Pattern PATH_RX = Pattern.compile("/projects/([^/]+)/repos/([^/]+)/?$");

  private static final String APPLICATION_TEXT_PLAIN = "text/plain";

  private final CommentAnalyticService commentAnalyticService;
  private final RefService refService;
  private final RepositoryService repositoryService;
  private final PullRequestService pullRequestService;
  private final I18nService i18nService;
  private final AuthenticationContext authenticationContext;

  // This constructor's dependencies are wired automatically by the plugin system
  public CommentAnalyticServlet(CommentAnalyticService commentAnalyticService, RefService refService,
      RepositoryService repositoryService, PullRequestService pullRequestService, I18nService 
      i18nService,
      AuthenticationContext authenticationContext) {
    this.commentAnalyticService = commentAnalyticService;
    this.refService = refService;
    this.repositoryService = repositoryService;
    this.pullRequestService = pullRequestService;
    this.i18nService = i18nService;
    this.authenticationContext = authenticationContext;
  }

  @Override
  protected void doGet(HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException, IOException {
    // Resolve the repository from the servlet path,
    // flushing a friendly error message to the user if there are any
    // problems parsing the URI or if the repository is missing.
    Matcher m = PATH_RX.matcher(req.getPathInfo());
    if (!m.find()) {
      resp.sendError(SC_BAD_REQUEST, i18nService.getText("stash.archive.bad.path",
          "The end of the request path must match ''{0}''.", PATH_RX.pattern()));
      return;
    }
    Repository repository = repositoryService.getBySlug(m.group(1), m.group(2));
    if (repository == null) {
      // Couldn't resolve the repository.. check if this is because the user isn't logged in
      // (Bitbucket didn't support anonymous access at time of writing) or because the context
      // user doesn't have the REPO_READ
      // permission.
      if (authenticationContext.getCurrentUser() == null) {
        resp.sendError(SC_UNAUTHORIZED, i18nService.getText("stash.archive.not.authenticated",
            "You are not currently logged in."));
        return;
      } else {
        resp.sendError(SC_NOT_FOUND, i18nService.getText("stash.archive.no.such.repository",
            "The specified repository does not exist or you have insufficient permissions"
                + " to access it."));
        return;
      }
    }

    // Resolve the request archive format (or default to ZIP if unspecified)
    String extension = trimToNull(req.getParameter("format"));
    CommentAnalyticFormat format;
    if (extension == null) {
      format = CommentAnalyticFormat.TXT;
    } else {
      format = CommentAnalyticFormat.forExtension(extension);
      if (format == null) {
        resp.sendError(SC_BAD_REQUEST, i18nService.getText("stash.archive.unsupported.format",
            "Unsupported format: ''{0}''", extension));
        return;
      }
    }

    // If the ref is unspecified, default to HEAD of the default branch
    String at = trimToNull(req.getParameter("at"));
    if (at == null) {
      at = refService.getDefaultBranch(repository).getId();
    }
    final String resolvedRef = at;


    // Stream the output from git-archive to the response
    try {
      OutputStream wrapper = new CommentAnalyticOutputStream(resp.getOutputStream()) {
        @Override
        protected void onFirstByte() {
          // Only set the content headers and status once we successfully start streaming
          // the archive. We do
          // this so we can handle the case where stream() throws an exception that
          // we want to transform into
          // a specific HTTP code.
          resp.setContentType(APPLICATION_TEXT_PLAIN);
          resp.setStatus(SC_OK);
        }
      };
      commentAnalyticService.stream(repository, pullRequestService, format, resolvedRef, wrapper);
    } catch (ResourceBusyException e) {
      // the server is currently under too much load to service this request
      // (see ThrottleService for more details)
      resp.sendError(SC_SERVICE_UNAVAILABLE, e.getLocalizedMessage());
    } catch (NoSuchEntityException e) {
      // the requested ref does not exist
      resp.sendError(SC_NOT_FOUND, e.getLocalizedMessage());
    }
  }

}
