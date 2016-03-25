package com.duitang.util.bitbucket.comment.analytic;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Delegating {@link OutputStream} that allows behaviour to be triggered immediately before the first byte is written.
 */
@NotThreadSafe
public abstract class CommentAnalyticOutputStream extends FilterOutputStream {

    private boolean written;

    public CommentAnalyticOutputStream(OutputStream outputStream) {
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
