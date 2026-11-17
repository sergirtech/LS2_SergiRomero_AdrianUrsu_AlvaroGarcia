package com.tecnocampus.LS2.protube_back.application.port.in;

/** Input port: the owner deletes their video, file included (US-07). */
public interface DeleteVideoUseCase {
    /**
     * @throws com.tecnocampus.LS2.protube_back.domain.exception.VideoAccessDeniedException if the user is not the owner
     */
    void delete(String ownerEmail, Long videoId);
}
