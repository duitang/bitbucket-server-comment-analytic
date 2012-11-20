define('plugin/download-archive', ['jquery', 'aui', 'page/util/pageUtil', 'util/navbuilder', 'exports'], function ($, AJS, pageUtil, navBuilder, exports) {
    exports.onReady = function (buttonSelector) {
        var $button = $(buttonSelector);

        var updateDownloadRef = function(revisionRef) {
            $button.attr("href", AJS.contextPath() + "/rest/archive/latest/projects/" + pageUtil.getProjectKey() +
                                 "/repos/" + pageUtil.getRepoSlug() + (revisionRef ? "?at=" + revisionRef : ""));
        };

        // let the server choose the default ref
        updateDownloadRef(navBuilder.parse(window.location.href).getQueryParamValue('at'));

        // update to reflect the branch selector
        eve.on('stash.widget.branchselector.revisionRefChanged', function(revisionRef, context) {
            if (!revisionRef.isDefault()) {
                updateDownloadRef(revisionRef.id);
            }
        });
    }
});
