package com.atlassian.stash.archive.rest;

import com.atlassian.plugins.rest.common.interceptor.InterceptorChain;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.stash.archive.ArchiveService;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.rest.interceptor.ResourceContextInterceptor;
import com.atlassian.stash.rest.util.ResourcePatterns;
import com.atlassian.stash.rest.util.ResponseFactory;
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

    public ArchiveResource(ArchiveService archiveService) {
        this.archiveService = archiveService;
    }

    @GET
    public Response stream(final @Context Repository repository,
                           final @QueryParam("format") @DefaultValue("zip") String type,
                           final @QueryParam("ref") @DefaultValue("HEAD") String ref) {
        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                archiveService.stream(repository, type, ref, outputStream);
            }
        };
        return ResponseFactory.ok(stream).build();
    }

}
