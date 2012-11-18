package com.atlassian.stash.archive.rest.exception;

import com.atlassian.stash.rest.exception.ResourceExceptionMapper;
import com.sun.jersey.spi.resource.Singleton;

import javax.ws.rs.ext.Provider;

@Provider
@Singleton
public class ArchiveResourceExceptionMapper extends ResourceExceptionMapper {
}
