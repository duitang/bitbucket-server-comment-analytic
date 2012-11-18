package com.atlassian.stash.archive.rest.exception;

import com.atlassian.stash.nav.NavBuilder;
import com.atlassian.stash.rest.exception.ServiceExceptionMapper;
import com.sun.jersey.spi.resource.Singleton;

import javax.ws.rs.ext.Provider;

@Provider
@Singleton
public class ArchiveServiceExceptionMapper extends ServiceExceptionMapper {
    public ArchiveServiceExceptionMapper(NavBuilder navBuilder) {
        super(navBuilder);
    }
}
