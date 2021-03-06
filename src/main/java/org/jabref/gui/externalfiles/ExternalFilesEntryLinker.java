package org.jabref.gui.externalfiles;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.jabref.gui.externalfiletype.ExternalFileType;
import org.jabref.gui.externalfiletype.ExternalFileTypes;
import org.jabref.gui.externalfiletype.UnknownExternalFileType;
import org.jabref.logic.cleanup.MoveFilesCleanup;
import org.jabref.logic.cleanup.RenamePdfCleanup;
import org.jabref.logic.util.io.FileUtil;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.LinkedFile;
import org.jabref.model.metadata.FilePreferences;

public class ExternalFilesEntryLinker {

    private final ExternalFileTypes externalFileTypes;
    private final FilePreferences filePreferences;
    private final BibDatabaseContext bibDatabaseContext;
    private final MoveFilesCleanup moveFilesCleanup;
    private final RenamePdfCleanup renameFilesCleanup;

    public ExternalFilesEntryLinker(ExternalFileTypes externalFileTypes, FilePreferences filePreferences, BibDatabaseContext bibDatabaseContext) {
        this.externalFileTypes = externalFileTypes;
        this.filePreferences = filePreferences;
        this.bibDatabaseContext = bibDatabaseContext;
        this.moveFilesCleanup = new MoveFilesCleanup(bibDatabaseContext, filePreferences);
        this.renameFilesCleanup = new RenamePdfCleanup(false, bibDatabaseContext, filePreferences);
    }

    public Optional<Path> copyFileToFileDir(Path file) {
        Optional<Path> firstExistingFileDir = bibDatabaseContext.getFirstExistingFileDir(filePreferences);
        if (firstExistingFileDir.isPresent()) {
            Path targetFile = firstExistingFileDir.get().resolve(file.getFileName());
            if (FileUtil.copyFile(file, targetFile, false)) {
                return Optional.ofNullable(targetFile);
            }
        }
        return Optional.empty();
    }

    public void renameLinkedFilesToPattern(BibEntry entry) {
        renameFilesCleanup.cleanup(entry);
    }

    public void moveLinkedFilesToFileDir(BibEntry entry) {
        moveFilesCleanup.cleanup(entry);
    }

    public void addFileToEntry(BibEntry entry, Path file) {
        addFilesToEntry(entry, Arrays.asList(file));
    }

    public void addFilesToEntry(BibEntry entry, List<Path> files) {
        for (Path file : files) {
            FileUtil.getFileExtension(file).ifPresent(ext -> {

                ExternalFileType type = externalFileTypes.getExternalFileTypeByExt(ext)
                                                         .orElse(new UnknownExternalFileType(ext));
                Path relativePath = FileUtil.relativize(file, bibDatabaseContext.getFileDirectoriesAsPaths(filePreferences));
                LinkedFile linkedfile = new LinkedFile("", relativePath.toString(), type.getName());
                entry.addFile(linkedfile);
            });

        }

    }
}
