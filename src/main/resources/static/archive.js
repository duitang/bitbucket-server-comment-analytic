define('plugin/download-archive', ['jquery', 'aui', 'page/util/pageUtil', 'util/navbuilder', 'exports'], function ($, AJS, pageUtil, navBuilder, exports) {
    exports.onReady = function (buttonSelector) {
        var $button = $(buttonSelector);

        /**
         * Update the "download archive" button's URL to target the specified ref.
         *
         * @param revisionRef the ref that the archive button should target. Falsey means no ref to be set, which will
         * cause the server to default to HEAD of the default branch.
         */
        var updateDownloadRef = function(revisionRef) {
            $button.attr("href", AJS.contextPath() + "/plugins/servlet/archive/projects/" +
                encodeURIComponent(pageUtil.getProjectKey()) + "/repos/" + encodeURIComponent(pageUtil.getRepoSlug()) +
                (revisionRef ? "?at=" + encodeURIComponent(revisionRef) : ""));
        };

        // On page load, grab the current ref out of the current page's 'at' query parameter
        updateDownloadRef(navBuilder.parse(window.location.href).getQueryParamValue('at'));

        // Also, bind to the branch selector's change event to grab the newly selected ref
        eve.on('stash.widget.branchselector.revisionRefChanged', function(revisionRef, context) {
            if (!revisionRef.isDefault()) {
                updateDownloadRef(revisionRef.id);
            }
        });
    }
});
