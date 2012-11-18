package com.atlassian.stash.archive.rest.exception;

import com.atlassian.stash.rest.exception.UnrecognizedPropertyExceptionMapper;
import com.sun.jersey.spi.resource.Singleton;

import javax.ws.rs.ext.Provider;

@Provider
@Singleton
public class ArchiveUnrecognizedPropertyExceptionMapper extends UnrecognizedPropertyExceptionMapper {
}
