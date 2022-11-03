/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.fat;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;


/**
 * DirectoryEntryImpl.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022/10/31 nsano initial version <br>
 */
public class DirectoryEntryImpl implements DirectoryEntry {

    /** */
    private FileEntry entry;

    /** */
    public DirectoryEntryImpl(FileEntry entry) throws IOException {
        this.entry = entry;
    }

    /** */
    protected DirectoryEntryImpl(List<FileEntry> entries) {
        this.entries = entries;
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public String getName() {
        return entry.getName();
    }

    @Override
    public int getStartCluster() {
        return entry.getStartCluster();
    }

    @Override
    public void setLongName(Collection<LongNameFileEntry> longNames) {
        entry.setLongName(longNames);
    }

    @Override
    public long length() {
        return entry.length();
    }

    @Override
    public long lastModified() {
        return entry.lastModified();
    }

    @Override
    public long created() {
        return entry.created();
    }

    @Override
    public long lastAccessed() {
        return entry.lastAccessed();
    }

    /** */
    private List<FileEntry> entries;

    @Override
    public List<FileEntry> entries() {
        return entries;
    }

    @Override
    public void setEntries(List<FileEntry> entries) {
        this.entries = entries;
    }

    @Override
    public String toString() {
        return getName() + "\\, " + length() + ", " + LocalDateTime.ofInstant(Instant.ofEpochMilli(lastModified()), ZoneId.of("+9"));
    }

    @Override
    public FileEntry find(String filename) {
        return entries.stream().filter(e -> e.getName().equals(filename)).findFirst().orElse(null);
    }

    /** */
    public static class RootDosDirectoryEntry extends DirectoryEntryImpl {

        /** */
        public RootDosDirectoryEntry(List<FileEntry> entries) {
            super(entries);
        }

        @Override
        public String getName() {
            return "\\";
        }

        @Override
        public int getStartCluster() {
            return 0;
        }

        @Override
        public void setLongName(Collection<LongNameFileEntry> longNames) {
        }

        @Override
        public long length() {
            return 0;
        }

        @Override
        public long lastModified() {
            return 0;
        }

        @Override
        public long created() {
            return 0;
        }

        @Override
        public long lastAccessed() {
            return 0;
        }
    }
}
