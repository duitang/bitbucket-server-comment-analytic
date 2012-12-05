package com.atlassian.stash.archive;

import com.atlassian.stash.exception.CommandFailedException;
import com.atlassian.stash.exception.NoSuchEntityException;
import com.atlassian.stash.i18n.I18nService;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.scm.DefaultCommandExitHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
        if ("fatal: Not a valid object name".equals(stdErr)) {
            throw new NoSuchEntityException(i18nService.getKeyedText("stash.archive.object.not.found",
                    "{0} does not exist in repository ''{1}''", ref, repository.getName()));
        }
        throw new CommandFailedException(i18nService.getKeyedText("stash.archive.command.failed", "{0} failed",
                command));
    }

}
