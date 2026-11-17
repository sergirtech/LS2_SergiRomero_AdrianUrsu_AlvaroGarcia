package com.tecnocampus.LS2.protube_back.application.port.out;

import com.tecnocampus.LS2.protube_back.domain.model.ProfilePhoto;

import java.util.Optional;

public interface ProfilePhotoRepository {
    Optional<ProfilePhoto> findByUserId(Long userId);

    void save(Long userId, ProfilePhoto photo);

    void delete(Long userId);
}
