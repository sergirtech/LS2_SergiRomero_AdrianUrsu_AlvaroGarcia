package com.tecnocampus.LS2.protube_back.infrastructure.store;

import com.tecnocampus.LS2.protube_back.application.port.out.VideoFileStore;
import com.tecnocampus.LS2.protube_back.domain.exception.VideoStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Output adapter: writes uploaded videos to the store folder ({@code pro_tube.store.dir}), next to the videoGrabber
 * files, so MvcConfig serves them under {@code /media/{fileKey}.mp4} like any other video.
 */
@Component
public class FileSystemVideoFileStore implements VideoFileStore {
    private static final Logger LOG = LoggerFactory.getLogger(FileSystemVideoFileStore.class);
    private static final List<String> EXTENSIONS = List.of(".mp4", ".webp", ".json");

    private final Path storeDir;

    public FileSystemVideoFileStore(@Value("${pro_tube.store.dir}") String storeDir) {
        this.storeDir = Paths.get(storeDir).toAbsolutePath().normalize();
    }

    @Override
    public void saveVideo(String fileKey, InputStream content) {
        Path target = resolve(fileKey + ".mp4");
        // Written under a temporary name and renamed at the end, so /media never serves half a file.
        Path partial = resolve(fileKey + ".mp4.part");
        try {
            Files.createDirectories(storeDir);
            Files.copy(content, partial, StandardCopyOption.REPLACE_EXISTING);
            Files.move(partial, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            deleteQuietly(partial);
            throw new VideoStorageException("Could not save the video file", e);
        }
    }

    @Override
    public void deleteVideo(String fileKey) {
        EXTENSIONS.forEach(extension -> deleteQuietly(resolve(fileKey + extension)));
    }

    // The key never comes from the user, but a path outside the store must not be written or deleted anyway.
    private Path resolve(String fileName) {
        Path path = storeDir.resolve(fileName).normalize();
        if (!path.getParent().equals(storeDir)) {
            throw new VideoStorageException("Invalid video file name: " + fileName);
        }
        return path;
    }

    // A file left behind is only wasted space: log it instead of failing a delete that already happened in the DB.
    private static void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            LOG.warn("Could not delete {}", path, e);
        }
    }
}
