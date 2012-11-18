package com.atlassian.stash.archive;

import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.scm.git.GitScm;

import java.io.OutputStream;

public class DefaultArchiveService implements ArchiveService {

    private static final int BUFFER_SIZE = 32 * 1024;

    private final GitScm gitScm;

    public DefaultArchiveService(GitScm gitScm) {
        this.gitScm = gitScm;
    }

    public void stream(Repository repository, ArchiveFormat format, String ref, OutputStream outputStream) {
        gitScm.getCommandBuilderFactory()
                .builder(repository)
                .command("archive")
                .argument("--format=" + format.getExtension())
                .argument(ref)
                .build(new ArchiveOutputHandler(BUFFER_SIZE, outputStream))
                .call();
    }

}
