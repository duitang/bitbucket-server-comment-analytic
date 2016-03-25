package com.duitang.util.bitbucket.comment.analytic;

import com.atlassian.bitbucket.NoSuchEntityException;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.throttle.ResourceBusyException;

import java.io.OutputStream;

/**
 * Service for handling {@code git-archive} invocations in Bitbucket.
 */
public interface CommentAnalyticService {

  /**
   * Stream the content of a repository at a particular ref in an archive file format.
   *
   * @param repository   the repository containing the specified ref
   * @param format       the {@link CommentAnalyticFormat} used to compress the content
   * @param ref          the ref specifying the content to stream
   * @param outputStream the output stream to stream the archive file to.
   * @throws NoSuchEntityException if the specified ref doesn't exist
   * @throws ResourceBusyException if the server is under too much load to process an archive
   *                               command at the moment
   */
  void stream(Repository repository, CommentAnalyticFormat format, String ref, OutputStream outputStream)
      throws NoSuchEntityException, ResourceBusyException;

}
