package com.duitang.util.bitbucket.comment.analytic;

import com.atlassian.bitbucket.scm.CommandOutputHandler;
import com.atlassian.utils.process.ProcessException;
import com.atlassian.utils.process.Watchdog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Implementation of {@link CommandOutputHandler} that simply copies the output from the underlying
 * process to the supplied {@link OutputStream}. This can be used to stream content from a process
 * to a response, a file or any other output stream.
 */
public class CommentAnalyticOutputHandler implements CommandOutputHandler<Void> {

  private static final Logger log = LoggerFactory.getLogger(CommentAnalyticOutputHandler.class);

  private final int bufferSize;
  private final OutputStream outputStream;

  private Watchdog watchdog;

  /**
   * @param bufferSize   the size of the buffer used to copy bytes from the process to the output
   *                     stream
   * @param outputStream the output stream to write to
   */
  public CommentAnalyticOutputHandler(int bufferSize, OutputStream outputStream) {
    this.bufferSize = bufferSize;
    this.outputStream = outputStream;
  }

  @Override
  public Void getOutput() {
    return null;
  }

  @Override
  public void process(InputStream inputStream) throws ProcessException {
    try {
      copyStream(inputStream, outputStream);
    } catch (Exception e) {
      throw new ProcessException(
          "Failed to copy git archive process output to response output stream.", e);
    }
  }

  @Override
  public void complete() throws ProcessException {
  }

  /**
   * The role of the {@link Watchdog} is to terminate runaway processes. This watchdog is set and
   * periodically checked by the process handling framework. {@link CommandOutputHandler
   * OutputHandlers} must periodically reset watchdogs or risk premature termination.
   */
  public void setWatchdog(Watchdog watchdog) {
    this.watchdog = watchdog;
  }

  /**
   * Copy all bytes from the supplied {@link InputStream} to the supplied {@link OutputStream}.
   *
   * @param inputStream  an input stream from the underlying process. (git-archive in this case)
   * @param outputStream an output stream to copy bytes from the input stream to.
   * @throws IOException if there was an issue reading from or writing to one of the streams
   */
  private void copyStream(final InputStream inputStream, final OutputStream outputStream)
      throws IOException {
    final byte[] buffer = new byte[bufferSize];
    long bytesCopied = 0;
    int index;
    while (-1 != (index = inputStream.read(buffer))) {
      // Simply copy the input stream to the output stream
      outputStream.write(buffer, 0, index);
      bytesCopied += index;

      // Reset the process watchdog every buffer cycle to prevent early termination of the process
      if (watchdog != null) {
        watchdog.resetWatchdog();
      } else {
        throw new IllegalStateException("Watchdog not set on " + getClass().getSimpleName());
      }
    }
    log.trace(bytesCopied + " bytes copied");
  }
}
