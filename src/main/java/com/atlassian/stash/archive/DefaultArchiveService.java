package com.atlassian.stash.archive;

import com.atlassian.stash.i18n.I18nService;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.scm.git.GitScm;
import com.atlassian.stash.throttle.ThrottleService;
import com.atlassian.stash.throttle.Ticket;

import java.io.OutputStream;

public class DefaultArchiveService implements ArchiveService {

    private static final int BUFFER_SIZE = 32 * 1024;

    private final GitScm gitScm;
    private final I18nService i18nService;
    private final ThrottleService throttleService;

    public DefaultArchiveService(GitScm gitScm, I18nService i18nService, ThrottleService throttleService) {
        this.gitScm = gitScm;
        this.i18nService = i18nService;
        this.throttleService = throttleService;
    }

    public void stream(Repository repository, ArchiveFormat format, String ref, OutputStream outputStream) {
        Ticket ticket = throttleService.acquireTicket("scm-hosting");
        try {
            gitScm.getCommandBuilderFactory()
                    .builder(repository)
                    .command("archive")
                    .argument("--format=" + format.getExtension())
                    .argument(ref)
                    .exitHandler(new ArchiveExitHandler(i18nService, repository, ref))
                    .build(new ArchiveOutputHandler(BUFFER_SIZE, outputStream))
                    .call();
        } finally {
            ticket.release();
        }
    }

}
