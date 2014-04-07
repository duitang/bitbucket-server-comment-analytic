define('plugin/download-archive', [
    'jquery',
    'aui',
    'model/page-state',
    'util/navbuilder',
    'exports'
], function(
    $,
    AJS,
    pageState,
    navBuilder,
    exports
) {


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
                encodeURIComponent(pageState.getProject().getKey()) + "/repos/" +
                encodeURIComponent(pageState.getRepository().getSlug()) +
                (revisionRef ? "?at=" + encodeURIComponent(revisionRef) : ""));
        };

        // On page load, grab the current ref out of page-state
        var currentRef = pageState.getRevisionRef() ? pageState.getRevisionRef().id : null;
        updateDownloadRef(currentRef);

        // Also, bind to the branch selector's change event to grab the newly selected ref
        eve.on('stash.feature.repository.revisionReferenceSelector.revisionRefChanged', function(revisionRef, context) {
            updateDownloadRef(revisionRef.id);
        });
    }
});

AJS.$(function() {
    require('plugin/download-archive').onReady('#download-archive-button');
});
