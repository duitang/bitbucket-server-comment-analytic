package com.atlassian.stash.archive;

import com.atlassian.stash.exception.NoSuchEntityException;
import com.atlassian.stash.exception.ResourceBusyException;
import com.atlassian.stash.i18n.I18nService;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.repository.RepositoryMetadataService;
import com.atlassian.stash.repository.RepositoryService;
import com.atlassian.stash.user.StashAuthenticationContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.trimToNull;

public class ArchiveServlet extends HttpServlet {

    private static final Pattern PATH_RX = Pattern.compile("/projects/([^/]+)/repos/([^/]+)/?$");

    // http codes
    private static final int OK = 200;
    private static final int BAD_REQUEST = 400;
    private static final int UNAUTHORIZED = 401;
    private static final int NOT_FOUND = 404;
    private static final int UNAVAILABLE = 503;

    private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    private final ArchiveService archiveService;
    private final RepositoryMetadataService repositoryMetadataService;
    private final RepositoryService repositoryService;
    private final I18nService i18nService;
    private final StashAuthenticationContext authenticationContext;

    public ArchiveServlet(ArchiveService archiveService, RepositoryMetadataService repositoryMetadataService,
                               RepositoryService repositoryService, I18nService i18nService,
                               StashAuthenticationContext authenticationContext) {
        this.archiveService = archiveService;
        this.repositoryMetadataService = repositoryMetadataService;
        this.repositoryService = repositoryService;
        this.i18nService = i18nService;
        this.authenticationContext = authenticationContext;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // resolve the repository
        Matcher m = PATH_RX.matcher(req.getPathInfo());
        if (!m.find()) {
            resp.sendError(BAD_REQUEST, i18nService.getText("stash.archive.bad.path",
                    "The end of the request path must match ''{0}''.", PATH_RX.pattern()));
            return;
        }
        Repository repository = repositoryService.findBySlug(m.group(1), m.group(2));
        if (repository == null) {
            if (authenticationContext.getCurrentUser() == null) {
                resp.sendError(UNAUTHORIZED, i18nService.getText("stash.archive.not.authenticated",
                    "You are not currently logged in."));
                return;
            } else {
                resp.sendError(NOT_FOUND, i18nService.getText("stash.archive.no.such.repository",
                    "The specified repository does not exist or you have insufficient permissions to access it."));
                return;
            }
        }

        // resolve the request archive format (or default to ZIP)
        String extension = trimToNull(req.getParameter("format"));
        ArchiveFormat format;
        if (extension == null) {
            format = ArchiveFormat.ZIP;
        } else {
            format = ArchiveFormat.forExtension(extension);
            if (format == null) {
                resp.sendError(BAD_REQUEST, i18nService.getText("stash.archive.unsupported.format",
                    "Unsupported format: ''{0}''", extension));
                return;
            }
        }

        // resolve the requested ref (or default to HEAD of the default branch)
        String at = trimToNull(req.getParameter("at"));
        if (at == null) {
            at = repositoryMetadataService.getDefaultBranch(repository).getId();
        }
        final String resolvedRef = at;

        // set the archive name as specified by query param, or default to <repository>-<ref>.<extension>
        String filename = trimToNull(req.getParameter("filename"));
        if (filename == null) {
            filename = String.format("%s-%s.%s", repository.getSlug(),
                    resolvedRef.substring(resolvedRef.lastIndexOf("/") + 1), format.getExtension());
        }
        resp.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", filename));

        // stream the response
        try {
            resp.setStatus(OK);
            resp.setContentType(APPLICATION_OCTET_STREAM);
            archiveService.stream(repository, format, resolvedRef, resp.getOutputStream());
        } catch (ResourceBusyException e) {
            resp.sendError(UNAVAILABLE, e.getLocalizedMessage());
        } catch (NoSuchEntityException e) {
            resp.sendError(NOT_FOUND, e.getLocalizedMessage());
        }
    }

}
