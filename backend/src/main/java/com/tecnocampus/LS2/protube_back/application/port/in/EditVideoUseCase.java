package com.tecnocampus.LS2.protube_back.application.port.in;

import com.tecnocampus.LS2.protube_back.domain.model.Video;

import java.util.List;

/** Input port: the owner edits the metadata of their video (US-07). */
public interface EditVideoUseCase {
    /**
     * @throws com.tecnocampus.LS2.protube_back.domain.exception.VideoAccessDeniedException if the user is not the owner
     */
    Video edit(String ownerEmail, Long videoId, String title, String description, List<String> tags, String category);
}
