package com.atlassian.stash.archive.rest;

import com.atlassian.plugins.rest.common.interceptor.InterceptorChain;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.stash.archive.ArchiveFormat;
import com.atlassian.stash.archive.ArchiveService;
import com.atlassian.stash.exception.ArgumentValidationException;
import com.atlassian.stash.i18n.I18nService;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.repository.RepositoryMetadataService;
import com.atlassian.stash.rest.interceptor.ResourceContextInterceptor;
import com.atlassian.stash.rest.util.ResourcePatterns;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.HttpResponseContext;
import com.sun.jersey.spi.resource.Singleton;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

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

    public ArchiveResource(ArchiveService archiveService, RepositoryMetadataService repositoryMetadataService,
                           I18nService i18nService) {
        this.archiveService = archiveService;
        this.repositoryMetadataService = repositoryMetadataService;
        this.i18nService = i18nService;
    }

    @GET
    public Response stream(final @Context Repository repository,
                           final @QueryParam("format") @DefaultValue("zip") String extension,
                           @QueryParam("at") String at,
                           @QueryParam("filename") String filename,
                           @Context HttpContext httpContext) {
        final ArchiveFormat format = ArchiveFormat.forExtension(extension);
        if (format == null) {
            throw new ArgumentValidationException(i18nService.getKeyedText("stash.archive.unsupported.format",
                    "Unsupported format: ''{0}''", extension));
        }

        if (at == null) {
            at = repositoryMetadataService.getDefaultBranch(repository).getId();
        }
        final String resolvedRef = at;

        if (filename == null) {
            filename = String.format("%s-%s.%s", repository.getSlug(),
                    resolvedRef.substring(resolvedRef.lastIndexOf("/") + 1), format.getExtension());
        }

        final HttpResponseContext responseContext = httpContext.getResponse();
        responseContext.getHttpHeaders()
                .add("Content-Disposition", String.format("attachment; filename=\"%s\"", filename));

        try {
            archiveService.stream(repository, format, resolvedRef, responseContext.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException("Failed to get output stream for response", e);
        }

        responseContext.setStatus(200);

        return responseContext.getResponse();
    }

}
