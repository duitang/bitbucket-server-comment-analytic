package com.atlassian.stash.archive;

import com.atlassian.stash.i18n.I18nService;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.scm.git.GitScm;
import com.atlassian.stash.throttle.ThrottleService;
import com.atlassian.stash.throttle.Ticket;

import java.io.OutputStream;

public class DefaultArchiveService implements ArchiveService {

    /**
     * Size of the buffer used to copy the output from git-archive to the supplied output stream.
     */
    private static final int BUFFER_SIZE = 32 * 1024;

    /**
     * Used to execute git commands in Stash repositories.
     */
    private final GitScm gitScm;
    /**
     * Used to (eventually) handle i18n. This plugin doesn't currently bundle any additional i18n bundles though.
     */
    private final I18nService i18nService;
    /**
     * Used to throttle the number of concurrent "expensive" operations that the server will perform at any one time,
     * to conserve resources.
     */
    private final ThrottleService throttleService;

    // This constructor's dependencies are wired automatically by the plugin system
    public DefaultArchiveService(GitScm gitScm, I18nService i18nService, ThrottleService throttleService) {
        this.gitScm = gitScm;
        this.i18nService = i18nService;
        this.throttleService = throttleService;
    }

    public void stream(Repository repository, ArchiveFormat format, String ref, OutputStream outputStream) {
        // Since git-archive operations can be reasonably expensive, this resource first acquires an "scm-hosting"
        // ticket. This limits the number of concurrent archive operations that can occur simultaneously and conserves
        // precious server resources - see ThrottleService for more details. Note that repository hosting resources
        // also use the "scm-hosting" name, so archive operations will be lumped in the same bucket as a push or clone.
        Ticket ticket = throttleService.acquireTicket("scm-hosting");
        try {
            // Create & call a new git-archive command in the target repository with the requested parameters
            gitScm.getCommandBuilderFactory()
                    .builder(repository)
                    .command("archive")
                    .argument("--format=" + format.getExtension())
                    .argument(ref)
                    .exitHandler(new ArchiveExitHandler(i18nService, repository, ref))
                    .build(new ArchiveOutputHandler(BUFFER_SIZE, outputStream))
                    .call();
        } finally {
            // Release the "scm-hosting" ticket back to the pool
            ticket.release();
        }
    }

}
