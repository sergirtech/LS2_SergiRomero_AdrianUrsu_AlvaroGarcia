package com.tecnocampus.LS2.protube_back.infrastructure.web;

import com.tecnocampus.LS2.protube_back.application.port.in.ManageProfilePhotoUseCase;
import com.tecnocampus.LS2.protube_back.domain.model.ProfilePhoto;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/users/me/photo")
public class ProfilePhotoController {
    private final ManageProfilePhotoUseCase photos;

    public ProfilePhotoController(ManageProfilePhotoUseCase photos) {
        this.photos = photos;
    }

    @GetMapping
    public ResponseEntity<byte[]> get(Authentication authentication) {
        return photos.get(authentication.getName()).map(this::response)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> update(Authentication authentication, @RequestParam("file") MultipartFile file)
            throws IOException {
        photos.update(authentication.getName(), file.getOriginalFilename(), file.getContentType(), file.getBytes());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(Authentication authentication) {
        photos.delete(authentication.getName());
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<byte[]> response(ProfilePhoto photo) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.noStore())
                .body(photo.content());
    }
}
