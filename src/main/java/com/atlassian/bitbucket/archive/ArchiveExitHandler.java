package com.atlassian.stash.archive;

import com.atlassian.stash.exception.CommandFailedException;
import com.atlassian.stash.exception.NoSuchEntityException;
import com.atlassian.stash.i18n.I18nService;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.scm.CommandExitHandler;
import com.atlassian.stash.scm.DefaultCommandExitHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Custom {@link CommandExitHandler} for that looks for a particular error message returned by {@code git-archive} when
 * a non-existent ref is specified.
 */
public class ArchiveExitHandler extends DefaultCommandExitHandler {

    private final I18nService i18nService;
    private final Repository repository;
    private final String ref;

    public ArchiveExitHandler(I18nService i18nService, Repository repository, String ref) {
        super(i18nService);
        this.i18nService = i18nService;
        this.repository = repository;
        this.ref = ref;
    }

    @Override
    public void onError(@Nonnull String command, int exitCode, @Nullable String stdErr, @Nullable Throwable thrown) {
        // Check if the error message is the standard output given by git-archive if the ref doesn't exist. If so,
        // throw a more specialized exception which the client can translate into a 404, for example.
        if ("fatal: Not a valid object name".equals(stdErr)) {
            throw new NoSuchEntityException(i18nService.getKeyedText("stash.archive.object.not.found",
                    "{0} does not exist in repository ''{1}''", ref, repository.getName()));
        }

        // Otherwise, something else went wrong - fail.
        throw new CommandFailedException(i18nService.getKeyedText("stash.archive.command.failed", "{0} failed",
                command));
    }

}
