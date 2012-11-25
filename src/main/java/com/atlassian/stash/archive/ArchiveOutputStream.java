package com.atlassian.stash.archive;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public abstract class ArchiveOutputStream extends FilterOutputStream {

    boolean written;

    public ArchiveOutputStream(OutputStream outputStream) {
        super(outputStream);
    }

    @Override
    public void write(int i) throws IOException {
        commitIfUncommitted();
        super.write(i);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        commitIfUncommitted();
        super.write(bytes);
    }

    @Override
    public void write(byte[] bytes, int i, int i1) throws IOException {
        commitIfUncommitted();
        super.write(bytes, i, i1);
    }

    private void commitIfUncommitted() {
        if (!written) {
            onFirstByte();
        }
    }

    protected abstract void onFirstByte();

}
