package com.atlassian.stash.archive.rest;

import com.sun.jersey.api.core.HttpResponseContext;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CommittingOutputStream extends FilterOutputStream {

    private final HttpResponseContext responseContext;

    boolean committed;

    public CommittingOutputStream(OutputStream outputStream, HttpResponseContext responseContext) {
        super(outputStream);
        this.responseContext = responseContext;
        committed = responseContext.isCommitted();
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
        if (!committed) {
            responseContext.setStatus(200);
        }
    }

}
