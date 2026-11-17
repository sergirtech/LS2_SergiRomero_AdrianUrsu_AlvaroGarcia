package com.tecnocampus.LS2.protube_back.application.port.in;

import com.tecnocampus.LS2.protube_back.domain.model.ProfilePhoto;

import java.util.Optional;

public interface ManageProfilePhotoUseCase {
    Optional<ProfilePhoto> get(String ownerEmail);

    void update(String ownerEmail, String filename, String contentType, byte[] content);

    void delete(String ownerEmail);
}
