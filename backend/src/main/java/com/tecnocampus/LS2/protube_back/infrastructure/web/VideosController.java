package com.tecnocampus.LS2.protube_back.infrastructure.web;

import com.tecnocampus.LS2.protube_back.application.port.in.DeleteVideoUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.EditVideoUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.GetVideoUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.ListMyVideosUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.ListVideosUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.UploadVideoUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.UploadVideoUseCase.UploadVideoCommand;
import com.tecnocampus.LS2.protube_back.domain.exception.VideoStorageException;
import com.tecnocampus.LS2.protube_back.domain.model.Video;
import com.tecnocampus.LS2.protube_back.infrastructure.web.dto.VideoDetailResponse;
import com.tecnocampus.LS2.protube_back.infrastructure.web.dto.VideoPageResponse;
import com.tecnocampus.LS2.protube_back.infrastructure.web.dto.VideoUpdateRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/videos")
public class VideosController {

    private final ListVideosUseCase listVideosUseCase;
    private final GetVideoUseCase getVideoUseCase;
    private final ListMyVideosUseCase listMyVideosUseCase;
    private final UploadVideoUseCase uploadVideoUseCase;
    private final EditVideoUseCase editVideoUseCase;
    private final DeleteVideoUseCase deleteVideoUseCase;

    public VideosController(ListVideosUseCase listVideosUseCase, GetVideoUseCase getVideoUseCase,
                            ListMyVideosUseCase listMyVideosUseCase, UploadVideoUseCase uploadVideoUseCase,
                            EditVideoUseCase editVideoUseCase, DeleteVideoUseCase deleteVideoUseCase) {
        this.listVideosUseCase = listVideosUseCase;
        this.getVideoUseCase = getVideoUseCase;
        this.listMyVideosUseCase = listMyVideosUseCase;
        this.uploadVideoUseCase = uploadVideoUseCase;
        this.editVideoUseCase = editVideoUseCase;
        this.deleteVideoUseCase = deleteVideoUseCase;
    }

    @GetMapping
    public VideoPageResponse listVideos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + ListVideosUseCase.DEFAULT_PAGE_SIZE) int size) {
        return VideoPageResponse.from(listVideosUseCase.listVideos(page, size));
    }

    /** Videos the signed-in user uploaded, newest first. Needs a session (SecurityConfig). */
    @GetMapping("/mine")
    public VideoPageResponse listMyVideos(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + ListVideosUseCase.DEFAULT_PAGE_SIZE) int size) {
        return VideoPageResponse.from(listMyVideosUseCase.listMyVideos(authentication.getName(), page, size));
    }

    @GetMapping("/{id}")
    public VideoDetailResponse getVideo(@PathVariable Long id) {
        return VideoDetailResponse.from(getVideoUseCase.getVideo(id));
    }

    // "tags" may come as repeated fields or as one comma-separated value: Spring turns both into a list.
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VideoDetailResponse> uploadVideo(
            Authentication authentication,
            @RequestParam String title,
            @RequestParam(defaultValue = "") String description,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) String category,
            @RequestParam MultipartFile file) {
        try (InputStream content = file.getInputStream()) {
            Video video = uploadVideoUseCase.upload(authentication.getName(), new UploadVideoCommand(title,
                    description, tags, category, file.getOriginalFilename(), file.getSize(), content));
            return ResponseEntity.created(URI.create("/api/videos/" + video.id()))
                    .body(VideoDetailResponse.from(video));
        } catch (IOException e) {
            throw new VideoStorageException("Could not read the uploaded file", e);
        }
    }

    @PutMapping("/{id}")
    public VideoDetailResponse editVideo(Authentication authentication, @PathVariable Long id,
                                         @RequestBody VideoUpdateRequest body) {
        return VideoDetailResponse.from(editVideoUseCase.edit(authentication.getName(), id, body.title(),
                body.description(), body.tags(), body.category()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVideo(Authentication authentication, @PathVariable Long id) {
        deleteVideoUseCase.delete(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
