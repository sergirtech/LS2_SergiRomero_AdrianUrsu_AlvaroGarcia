package com.tecnocampus.LS2.protube_back.infrastructure.persistence;

import com.tecnocampus.LS2.protube_back.application.port.out.ProfilePhotoRepository;
import com.tecnocampus.LS2.protube_back.domain.exception.UserNotFoundException;
import com.tecnocampus.LS2.protube_back.domain.model.ProfilePhoto;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
class ProfilePhotoPersistenceAdapter implements ProfilePhotoRepository {
    private final UserProfilePhotoJpaRepository photos;
    private final UserJpaRepository users;

    ProfilePhotoPersistenceAdapter(UserProfilePhotoJpaRepository photos, UserJpaRepository users) {
        this.photos = photos;
        this.users = users;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProfilePhoto> findByUserId(Long userId) {
        return photos.findById(userId).map(UserProfilePhotoJpaEntity::toDomain);
    }

    @Override
    @Transactional
    public void save(Long userId, ProfilePhoto photo) {
        UserProfilePhotoJpaEntity entity = photos.findById(userId).orElseGet(() -> {
            UserJpaEntity user = users.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            return UserProfilePhotoJpaEntity.create(user, photo);
        });
        entity.update(photo);
        photos.save(entity);
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        photos.deleteById(userId);
    }
}
