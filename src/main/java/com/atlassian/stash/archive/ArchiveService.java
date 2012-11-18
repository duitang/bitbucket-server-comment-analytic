package com.atlassian.stash.archive;

import com.atlassian.stash.repository.Repository;

import java.io.OutputStream;

public interface ArchiveService {

    void stream(Repository repository, ArchiveFormat format, String ref, OutputStream outputStream);

}
