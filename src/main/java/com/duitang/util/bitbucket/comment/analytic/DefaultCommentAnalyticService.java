package com.duitang.util.bitbucket.comment.analytic;

import com.atlassian.bitbucket.i18n.I18nService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.throttle.ThrottleService;
import com.atlassian.bitbucket.throttle.Ticket;

import java.io.IOException;
import java.io.OutputStream;

public class DefaultCommentAnalyticService implements CommentAnalyticService {

  /**
   * Size of the buffer used to copy the output from git-archive to the supplied output stream.
   */
  private static final int BUFFER_SIZE = 32 * 1024;

  /**
   * Used to (eventually) handle i18n. This plugin doesn't currently bundle any additional i18n
   * bundles though.
   */
  private final I18nService i18nService;
  /**
   * Used to throttle the number of concurrent "expensive" operations that the server will perform
   * at any one time, to conserve resources.
   */
  private final ThrottleService throttleService;

  // This constructor's dependencies are wired automatically by the plugin system
  public DefaultCommentAnalyticService(I18nService i18nService, ThrottleService throttleService) {
    this.i18nService = i18nService;
    this.throttleService = throttleService;
  }

  public void stream(Repository repository, CommentAnalyticFormat format, String ref, OutputStream outputStream) {
    // Since git-archive operations can be reasonably expensive, this resource first acquires an "scm-hosting"
    // ticket. This limits the number of concurrent archive operations that can occur simultaneously and conserves
    // precious server resources - see ThrottleService for more details. Note that repository hosting resources
    // also use the "scm-hosting" name, so archive operations will be lumped in the same bucket as a push or clone.
    try (Ticket ignored = throttleService.acquireTicket("scm-hosting")) {
      // Create & call a new git-archive command in the target repository with the requested parameters
      //            gitScm.getCommandBuilderFactory()
      //                    .builder(repository)
      //                    .command("archive")
      //                    .argument("--format=" + format.getExtension())
      //                    .argument(ref)
      //                    .exitHandler(new CommentAnalyticExitHandler(i18nService, repository, ref))
      //                    .build(new ArchiveOutputHandler(BUFFER_SIZE, outputStream))
      //                    .call();
      try {
        outputStream.write("Hello".getBytes());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}
