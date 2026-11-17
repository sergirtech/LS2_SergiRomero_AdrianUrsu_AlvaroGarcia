package com.tecnocampus.LS2.protube_back.infrastructure.web;

import com.tecnocampus.LS2.protube_back.application.port.in.DeleteVideoUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.EditVideoUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.GetVideoFileUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.GetVideoUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.ListVideosUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.ListMyVideosUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.UploadVideoUseCase;
import com.tecnocampus.LS2.protube_back.domain.model.VideoFile;
import com.tecnocampus.LS2.protube_back.domain.model.VideoPage;
import com.tecnocampus.LS2.protube_back.domain.model.VideoSummary;
import com.tecnocampus.LS2.protube_back.infrastructure.web.dto.VideoPageResponse;
import com.tecnocampus.LS2.protube_back.infrastructure.web.dto.VideoResponse;
import com.tecnocampus.LS2.protube_back.infrastructure.web.dto.VideoUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
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
import java.net.URI;

@RestController
@RequestMapping("/api/videos")
public class VideosController {
    private final ListVideosUseCase listVideosUseCase;
    private final ListMyVideosUseCase listMyVideosUseCase;
    private final UploadVideoUseCase uploadVideoUseCase;
    private final EditVideoUseCase editVideoUseCase;
    private final DeleteVideoUseCase deleteVideoUseCase;
    private final GetVideoUseCase getVideoUseCase;
    private final GetVideoFileUseCase getVideoFileUseCase;

    public VideosController(ListVideosUseCase listVideosUseCase,
                            ListMyVideosUseCase listMyVideosUseCase,
                            UploadVideoUseCase uploadVideoUseCase,
                            EditVideoUseCase editVideoUseCase,
                            DeleteVideoUseCase deleteVideoUseCase,
                            GetVideoUseCase getVideoUseCase,
                            GetVideoFileUseCase getVideoFileUseCase) {
        this.listVideosUseCase = listVideosUseCase;
        this.listMyVideosUseCase = listMyVideosUseCase;
        this.uploadVideoUseCase = uploadVideoUseCase;
        this.editVideoUseCase = editVideoUseCase;
        this.deleteVideoUseCase = deleteVideoUseCase;
        this.getVideoUseCase = getVideoUseCase;
        this.getVideoFileUseCase = getVideoFileUseCase;
    }

    @GetMapping
    public VideoPageResponse getVideos(@RequestParam(required = false) Long cursor,
                                       @RequestParam(required = false) Integer limit) {
        int pageSize = limit == null ? (cursor == null ? 20 : 10) : limit;
        VideoPage page = listVideosUseCase.getVideos(cursor, pageSize);
        return VideoPageResponse.from(page);
    }

    @GetMapping("/mine")
    public VideoPageResponse getMyVideos(Authentication authentication,
                                         @RequestParam(required = false) Long cursor,
                                         @RequestParam(required = false) Integer limit) {
        int pageSize = limit == null ? (cursor == null ? 20 : 10) : limit;
        VideoPage page = listMyVideosUseCase.getMyVideos(authentication.getName(), cursor, pageSize);
        return VideoPageResponse.from(page);
    }

    @GetMapping("/{id}")
    public VideoResponse getVideo(@PathVariable Long id) {
        return VideoResponse.from(getVideoUseCase.getVideo(id));
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<byte[]> getVideoFile(@PathVariable Long id) {
        VideoFile file = getVideoFileUseCase.getVideoFile(id);
        byte[] content = file.content();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .contentLength(content.length)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename("video-" + id + ".mp4").build().toString())
                .body(content);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VideoResponse> uploadVideo(Authentication authentication,
                                                     @RequestParam("title") String title,
                                                     @RequestParam("description") String description,
                                                     @RequestParam("file") MultipartFile file) throws IOException {
        VideoSummary uploaded = uploadVideoUseCase.upload(authentication.getName(), title, description,
                file.getOriginalFilename(), file.getBytes());
        return ResponseEntity.created(URI.create("/api/videos/" + uploaded.id()))
                .body(VideoResponse.from(uploaded));
    }

    @PutMapping("/{id}")
    public VideoResponse editVideo(Authentication authentication, @PathVariable Long id,
                                   @Valid @RequestBody VideoUpdateRequest body) {
        return VideoResponse.from(editVideoUseCase.edit(authentication.getName(), id, body.title(),
                body.description()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVideo(Authentication authentication, @PathVariable Long id) {
        deleteVideoUseCase.delete(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
