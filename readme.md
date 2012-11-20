# Stash Archive Plugin

Provides a button on repository views for downloading a zip archive of your source, and a REST API with some additional features.

### Download Button

The Download button appears next to the clone URL on all repository views. By default it will download your the content at the HEAD of your default branch. Use the branch/tag selector on the Files or Commits view to select a different branch or tag to download.

### REST API

The basic form for an archive REST URL is:

```https://<stash-base-url>/rest/archive/latest/projects/<projectKey>/repos/<repoSlug>```

This will provide a file named ```<repoSlug>-<branch-name>.zip``` that contains your source at the HEAD of the repository's default branch.

You can change the output using the following query parameters:

* ```format=[zip|tar|tar.gz]``` will change the output format (default is zip)

* ```filename=<name>``` will change the name of the downloaded file

* ```at=<branch|tag|sha|ref>``` will specify the ref to download

For example, the URL:

```https://<stash-base-url>/rest/archive/latest/projects/TEST/repos/my-cool-repo?format=tar.gz&at=release-1.3.0&filename=cool-1.3.0.tar.gz```

Will yield ```cool-1.3.0.tar.gz```, containing the repository contents at the  ```release-1.3.0``` tag.

