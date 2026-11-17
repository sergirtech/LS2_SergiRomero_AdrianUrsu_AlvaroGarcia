package com.tecnocampus.LS2.protube_back.infrastructure.store;

import com.tecnocampus.LS2.protube_back.domain.exception.VideoStorageException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileSystemVideoFileStoreTest {

    @TempDir
    Path storeDir;

    @Test
    void savesTheVideoAsFileKeyDotMp4() throws IOException {
        FileSystemVideoFileStore store = new FileSystemVideoFileStore(storeDir.toString());
        byte[] content = {1, 2, 3};

        store.saveVideo("upload-1", new ByteArrayInputStream(content));

        assertArrayEquals(content, Files.readAllBytes(storeDir.resolve("upload-1.mp4")));
        assertFalse(Files.exists(storeDir.resolve("upload-1.mp4.part")));
    }

    @Test
    void createsTheStoreFolderIfMissing() {
        Path missing = storeDir.resolve("new-folder");
        FileSystemVideoFileStore store = new FileSystemVideoFileStore(missing.toString());

        store.saveVideo("upload-1", new ByteArrayInputStream(new byte[] {1}));

        assertTrue(Files.exists(missing.resolve("upload-1.mp4")));
    }

    @Test
    void aFailedCopyLeavesNoFileBehind() {
        FileSystemVideoFileStore store = new FileSystemVideoFileStore(storeDir.toString());
        InputStream broken = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("connection reset");
            }
        };

        assertThrows(VideoStorageException.class, () -> store.saveVideo("upload-1", broken));
        assertFalse(Files.exists(storeDir.resolve("upload-1.mp4")));
        assertFalse(Files.exists(storeDir.resolve("upload-1.mp4.part")));
    }

    @Test
    void deleteRemovesEveryFileOfTheVideoAndIgnoresMissingOnes() throws IOException {
        FileSystemVideoFileStore store = new FileSystemVideoFileStore(storeDir.toString());
        Files.writeString(storeDir.resolve("upload-1.mp4"), "video");
        Files.writeString(storeDir.resolve("upload-1.webp"), "thumbnail");
        Files.writeString(storeDir.resolve("upload-2.mp4"), "other video");

        store.deleteVideo("upload-1");

        assertFalse(Files.exists(storeDir.resolve("upload-1.mp4")));
        assertFalse(Files.exists(storeDir.resolve("upload-1.webp")));
        assertTrue(Files.exists(storeDir.resolve("upload-2.mp4")));
    }

    @Test
    void keysThatLeaveTheStoreFolderAreRejected() {
        FileSystemVideoFileStore store = new FileSystemVideoFileStore(storeDir.resolve("store").toString());

        assertThrows(VideoStorageException.class,
                () -> store.saveVideo("../escape", new ByteArrayInputStream(new byte[] {1})));
        assertThrows(VideoStorageException.class, () -> store.deleteVideo("../escape"));
    }
}
