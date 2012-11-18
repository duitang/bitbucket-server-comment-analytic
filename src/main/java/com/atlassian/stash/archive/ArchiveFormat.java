package com.atlassian.stash.archive;

import java.util.HashMap;
import java.util.Map;

public enum ArchiveFormat {

    TAR("tar"),
    TAR_GZ("tar.gz"),
    ZIP("zip");

    private final String extension;

    ArchiveFormat(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

    private final static Map<String, ArchiveFormat> BY_EXTENSTION = new HashMap<String, ArchiveFormat>();

    static {
        for (ArchiveFormat format : ArchiveFormat.values()) {
            BY_EXTENSTION.put(format.getExtension(), format);
        }
    }

    public static ArchiveFormat forExtension(String extension) {
        return BY_EXTENSTION.get(extension.toLowerCase());
    }

}
