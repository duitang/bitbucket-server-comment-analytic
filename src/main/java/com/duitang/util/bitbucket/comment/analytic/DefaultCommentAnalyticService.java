package com.duitang.util.bitbucket.comment.analytic;

import com.atlassian.bitbucket.comment.CommentAction;
import com.atlassian.bitbucket.i18n.I18nService;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestActivity;
import com.atlassian.bitbucket.pull.PullRequestActivitySearchRequest;
import com.atlassian.bitbucket.pull.PullRequestActivityType;
import com.atlassian.bitbucket.pull.PullRequestSearchRequest;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.atlassian.bitbucket.throttle.ThrottleService;
import com.atlassian.bitbucket.throttle.Ticket;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.bitbucket.util.Page;
import com.atlassian.bitbucket.util.PageRequest;
import com.atlassian.bitbucket.util.PageRequestImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultCommentAnalyticService implements CommentAnalyticService {

  /**
   * Size of the buffer used to copy the output from git-archive to the supplied output stream.
   */
  private static final int BUFFER_SIZE = 32 * 1024;

  /**
   * Used to (eventually) handle i18n. This plugin doesn't currently bundle any additional i18n
   * bundles though.
   */
  private final I18nService i18nService;
  /**
   * Used to throttle the number of concurrent "expensive" operations that the server will perform
   * at any one time, to conserve resources.
   */
  private final ThrottleService throttleService;

  // This constructor's dependencies are wired automatically by the plugin system
  public DefaultCommentAnalyticService(I18nService i18nService, ThrottleService throttleService) {
    this.i18nService = i18nService;
    this.throttleService = throttleService;
  }

  /**
   * forgive me, when query more than one, it raise error
   * it will be SLOW!!!
   */
  protected List<PullRequestActivity> fuckBatchQuery(PullRequestService pullRequestService,
      PullRequestActivitySearchRequest pullRequestActivitySearchRequest) {
    List<PullRequestActivity> pullRequestActivities = Lists.newArrayList();
    int i = 0;
    while (i < PageRequestImpl.MAX_PAGE_LIMIT) {
      Page<PullRequestActivity> pullRequestActivityPage = pullRequestService.searchActivities(
          pullRequestActivitySearchRequest, new PageRequestImpl(i, 1));
      if (pullRequestActivityPage.getSize() == 0) {
        break;
      }
      pullRequestActivities.add(pullRequestActivityPage.getValues().iterator().next());
      i++;
    }
    return pullRequestActivities;
  }
  
  protected String generateAnalytic(Repository repository, PullRequestService pullRequestService) {
//    List<CommentAction> commentActions = Lists.newArrayList();
//    List<Long> commentIds = Lists.newArrayList();
    List<PullRequestActivity> pullRequestActivities = Lists.newArrayList();
    Map<ApplicationUser, Integer> counter = new LinkedHashMap<>();
    PullRequestSearchRequest pullRequestSearchRequest = new PullRequestSearchRequest.Builder()
        .toRepositoryId(repository.getId())
        .withProperties(false)
        .build();
    Page<PullRequest> pullRequests = pullRequestService.search(pullRequestSearchRequest,
        new PageRequestImpl (0, PageRequest.MAX_PAGE_LIMIT - 1));
//    List<Long> pullRequestIds = Lists.newArrayList();
    
    for (PullRequest pullRequest: pullRequests.getValues()) {  // N + 1
//      pullRequestIds.add(pullRequest.getId());
      PullRequestActivitySearchRequest pullRequestActivitySearchRequest =
          new PullRequestActivitySearchRequest
              .Builder(pullRequest)
              .types(PullRequestActivityType.COMMENT)
              .commentActions(CommentAction.ADDED, CommentAction.REPLIED)
              .withProperties(false)
              .build();
//      commentIds.addAll(pullRequestActivitySearchRequest.getCommentIds());
      pullRequestActivities.addAll(this.fuckBatchQuery(pullRequestService,
          pullRequestActivitySearchRequest));
    }
    
    for (PullRequestActivity pullRequestActivity: pullRequestActivities) {
      ApplicationUser user = pullRequestActivity.getUser();
      if (!counter.containsKey(user)) {
        counter.put(user, 1);
      }
      counter.put(user, counter.get(user) + 1);
    }
    String output = counter.entrySet().stream()
        .sorted(Comparator.comparing(e -> -e.getValue()))
        .map(x -> String.format("%s: %s\n", x.getKey()
        .getDisplayName(), x.getValue()))
        .collect(Collectors.joining());
    output += "\n";
    output += "Powered by 3D(wx @alswl)\n";
    return output;
  }

  public void stream(Repository repository, PullRequestService pullRequestService,
      CommentAnalyticFormat format, String ref, OutputStream outputStream) {
    // Since git-archive operations can be reasonably expensive, this resource first acquires an "scm-hosting"
    // ticket. This limits the number of concurrent archive operations that can occur simultaneously and conserves
    // precious server resources - see ThrottleService for more details. Note that repository hosting resources
    // also use the "scm-hosting" name, so archive operations will be lumped in the same bucket as a push or clone.
    try (Ticket ignored = throttleService.acquireTicket("scm-hosting")) {
      try {
        outputStream.write(this.generateAnalytic(repository, pullRequestService).getBytes());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}
