package com.atlassian.stash.archive;

import com.atlassian.stash.scm.CommandOutputHandler;
import com.atlassian.utils.process.ProcessException;
import com.atlassian.utils.process.Watchdog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ArchiveOutputHandler implements CommandOutputHandler<Void> {

    private static final Logger log = LoggerFactory.getLogger(ArchiveOutputHandler.class);

    private final int bufferSize;
    private final OutputStream outputStream;

    private Watchdog watchdog;

    public ArchiveOutputHandler(int bufferSize, OutputStream outputStream) {
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
            throw new ProcessException("Failed to copy git archive process output to response output stream.", e);
        }
    }

    @Override
    public void complete() throws ProcessException {
    }

    public void setWatchdog(Watchdog watchdog) {
        this.watchdog = watchdog;
    }

    protected void copyStream(final InputStream inputStream, final OutputStream outputStream) throws IOException {
        final byte[] buffer = new byte[bufferSize];
        long bytesCopied = 0;
        int n;
        while (-1 != (n = inputStream.read(buffer))) {
            outputStream.write(buffer, 0, n);
            bytesCopied += n;
            if (watchdog != null) {
                watchdog.resetWatchdog();
            } else {
                throw new IllegalStateException("Watchdog not set on " + getClass().getSimpleName());
            }
        }
        log.trace(bytesCopied + " bytes copied");
    }
}
