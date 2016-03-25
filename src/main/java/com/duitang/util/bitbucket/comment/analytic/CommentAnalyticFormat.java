package com.duitang.util.bitbucket.comment.analytic;

import java.util.HashMap;
import java.util.Map;

/**
 * Archive file formats supported by git-archive.
 */
public enum CommentAnalyticFormat {

  TXT("txt");

  private final String extension;

  CommentAnalyticFormat(String extension) {
    this.extension = extension;
  }

  public String getExtension() {
    return extension;
  }

  private static final Map<String, CommentAnalyticFormat> BY_EXTENSTION = new HashMap<>();

  static {
    for (CommentAnalyticFormat format : CommentAnalyticFormat.values()) {
      BY_EXTENSTION.put(format.getExtension(), format);
    }
  }

  public static CommentAnalyticFormat forExtension(String extension) {
    return BY_EXTENSTION.get(extension.toLowerCase());
  }

}
