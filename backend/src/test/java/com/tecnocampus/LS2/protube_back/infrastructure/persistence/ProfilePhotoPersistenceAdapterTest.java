package com.tecnocampus.LS2.protube_back.infrastructure.persistence;

import com.tecnocampus.LS2.protube_back.application.port.out.ProfilePhotoRepository;
import com.tecnocampus.LS2.protube_back.domain.model.ProfilePhoto;
import com.tecnocampus.LS2.protube_back.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(ProfilePhotoPersistenceAdapter.class)
class ProfilePhotoPersistenceAdapterTest {
    @Autowired ProfilePhotoRepository photos;
    @Autowired UserJpaRepository users;

    @Test
    void createsUpdatesReadsAndDeletesPhotoBytes() {
        UserJpaEntity user = users.save(UserJpaEntity.fromDomain(
                User.register("photo-owner@example.com", "photo-owner", "hash")));
        byte[] first = {(byte) 0x89, 0x50, 0x4e, 0x47};
        byte[] changed = {(byte) 0xff, (byte) 0xd8, (byte) 0xff};

        photos.save(user.getId(), new ProfilePhoto("image/png", first));
        assertEquals("image/png", photos.findByUserId(user.getId()).orElseThrow().contentType());
        assertArrayEquals(first, photos.findByUserId(user.getId()).orElseThrow().content());

        photos.save(user.getId(), new ProfilePhoto("image/jpeg", changed));
        assertEquals("image/jpeg", photos.findByUserId(user.getId()).orElseThrow().contentType());
        assertArrayEquals(changed, photos.findByUserId(user.getId()).orElseThrow().content());
        photos.delete(user.getId());
        assertTrue(photos.findByUserId(user.getId()).isEmpty());
    }
}
