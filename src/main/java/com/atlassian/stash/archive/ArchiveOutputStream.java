package com.atlassian.stash.archive;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Delegating {@link OutputStream} that allows behaviour to be triggered immediately before the first byte is written.
 */
public abstract class ArchiveOutputStream extends FilterOutputStream {

    private boolean written;

    public ArchiveOutputStream(OutputStream outputStream) {
        super(outputStream);
    }

    @Override
    public void write(int i) throws IOException {
        checkFirstByte();
        super.write(i);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        checkFirstByte();
        super.write(bytes);
    }

    @Override
    public void write(byte[] bytes, int i, int i1) throws IOException {
        checkFirstByte();
        super.write(bytes, i, i1);
    }

    private void checkFirstByte() {
        if (!written) {
            onFirstByte();
            written = true;
        }
    }

    protected abstract void onFirstByte();

}
