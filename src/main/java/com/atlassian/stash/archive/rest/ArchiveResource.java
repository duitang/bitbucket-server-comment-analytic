package com.atlassian.stash.archive.rest;

import com.atlassian.plugins.rest.common.interceptor.InterceptorChain;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.stash.archive.ArchiveFormat;
import com.atlassian.stash.archive.ArchiveService;
import com.atlassian.stash.exception.ArgumentValidationException;
import com.atlassian.stash.exception.NoSuchEntityException;
import com.atlassian.stash.i18n.I18nService;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.repository.RepositoryMetadataService;
import com.atlassian.stash.rest.interceptor.ResourceContextInterceptor;
import com.atlassian.stash.rest.util.ResourcePatterns;
import com.atlassian.stash.rest.util.ResponseFactory;
import com.atlassian.stash.throttle.ThrottleService;
import com.atlassian.stash.throttle.Ticket;
import com.sun.jersey.spi.resource.Singleton;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;

@Path(ResourcePatterns.REPOSITORY_URI)
@InterceptorChain(ResourceContextInterceptor.class)
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_OCTET_STREAM})
@Singleton
@AnonymousAllowed
public class ArchiveResource {

    private final ArchiveService archiveService;
    private final RepositoryMetadataService repositoryMetadataService;
    private final I18nService i18nService;
    private final ThrottleService throttleService;

    public ArchiveResource(ArchiveService archiveService, RepositoryMetadataService repositoryMetadataService,
                           I18nService i18nService, ThrottleService throttleService) {
        this.archiveService = archiveService;
        this.repositoryMetadataService = repositoryMetadataService;
        this.i18nService = i18nService;
        this.throttleService = throttleService;
    }

    @GET
    public Response stream(final @Context Repository repository,
                           final @QueryParam("format") @DefaultValue("zip") String extension,
                           @QueryParam("at") String at,
                           @QueryParam("filename") String filename) {
        final ArchiveFormat format = ArchiveFormat.forExtension(extension);
        if (format == null) {
            throw new ArgumentValidationException(i18nService.getKeyedText("stash.archive.unsupported.format",
                    "Unsupported format: ''{0}''", extension));
        }

        if (filename == null) {
            filename = repository.getSlug() + "." + format.getExtension();
        }

        if (at == null) {
            at = repositoryMetadataService.getDefaultBranch(repository).getId();
        } else if (repositoryMetadataService.resolveRef(repository, at) == null) {
            // The ArchiveService will throw a NoSuchEntityException if the ref doesn't exist but using StreamingOutput
            // means the response will be committed by that point, so our ExceptionMappers won't kick in unless we
            // validate the ref exists up front.
            throw new NoSuchEntityException(i18nService.getKeyedText("stash.archive.object.not.found",
                    "{0} does not exist in repository ''{1}''", at, repository.getName()));
        }
        final String resolvedRef = at;

        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                archiveService.stream(repository, format, resolvedRef, outputStream);
            }
        };

        // The ArchiveService will acquire a ticket for us, but let's acquire one eagerly so we can take advantage
        // of out ExceptionMappers translating the ResourceBusyException automatically for us
        Ticket ticket = throttleService.acquireTicket("scm-hosting");
        try {
            return ResponseFactory
                    .ok(stream)
                    .header("Content-Disposition", String.format("attachment; filename=\"%s\"", filename))
                    .build();
        } finally {
            ticket.release();
        }
    }

}
