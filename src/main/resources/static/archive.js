define('plugin/comment-analytic', [
    'jquery',
    'aui',
    'bitbucket/util/state',
    'bitbucket/util/navbuilder',
    'exports'
], function ($,
             AJS,
             pageState,
             navBuilder,
             exports) {

    exports.onReady = function (buttonSelector) {
        var $button = $(buttonSelector);

        /**
         * Update the "$ archive" button's URL to target the specified ref.
         *
         * @param revisionRef the ref that the archive button should target. Falsey means no ref to
         *     be set, which will cause the server to default to HEAD of the default branch.
         */
        var updateCommentAnalyticRef = function (revisionRef) {
            $button.attr("href", AJS.contextPath() + "/plugins/servlet/comment-analytic/projects/" +
                                 encodeURIComponent(pageState.getProject().key) + "/repos/" +
                                 encodeURIComponent(pageState.getRepository().slug)
            );
        };

        // On page load, grab the current ref out of page-state
        updateCommentAnalyticRef();
    }
});

AJS.$(function () {
    require('plugin/comment-analytic').onReady('#comment-analytic-button');
});
