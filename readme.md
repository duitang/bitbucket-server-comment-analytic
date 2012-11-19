# Stash Archive Plugin

Provides a REST end-point for downloading repositories in popular archive formats.

The basic form for an archive URL is:

```https://<stash-base-url>/rest/archive/latest/projects/<projectKey>/repos/<repoSlug>```

This will provide a file named ```<repoSlug>.zip``` that contains your source at the HEAD of the repository's default branch.

You can change the output using the following query parameters:

* ```format=[zip|tar|tar.gz]``` will change the output format (default is zip)

* ```filename=<name>``` will change the name of the downloaded file

* ```ref=<branch|tag|sha|ref>``` will specify the ref to download

For example, the URL:

```https://<stash-base-url>/rest/archive/latest/projects/TEST/repos/my-cool-repo?format=tar.gz&ref=release-1.3.0&filename=cool-1.3.0.tar.gz```

Will yield ```cool-1.3.0.tar.gz```, containing the repository contents at the  ```release-1.3.0``` tag.

