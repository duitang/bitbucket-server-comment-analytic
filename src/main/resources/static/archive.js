define('plugin/download-archive', ['jquery', 'aui', 'page/util/pageUtil', 'util/navbuilder', 'exports'], function ($, AJS, pageUtil, navBuilder, exports) {
    exports.onReady = function (buttonSelector) {
        var $button = $(buttonSelector);

        var updateDownloadRef = function(revisionRef) {
            $button.attr("href", AJS.contextPath() + "/plugins/servlet/archive/projects/" +
                encodeURIComponent(pageUtil.getProjectKey()) + "/repos/" + encodeURIComponent(pageUtil.getRepoSlug()) +
                (revisionRef ? "?at=" + encodeURIComponent(revisionRef) : ""));
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
