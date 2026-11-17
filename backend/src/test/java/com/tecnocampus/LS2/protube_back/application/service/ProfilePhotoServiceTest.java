package com.tecnocampus.LS2.protube_back.application.service;

import com.tecnocampus.LS2.protube_back.application.port.in.FindUserUseCase;
import com.tecnocampus.LS2.protube_back.application.port.out.ProfilePhotoRepository;
import com.tecnocampus.LS2.protube_back.domain.exception.InvalidProfilePhotoException;
import com.tecnocampus.LS2.protube_back.domain.model.ProfilePhoto;
import com.tecnocampus.LS2.protube_back.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfilePhotoServiceTest {
    private static final String EMAIL = "owner@example.com";
    private static final User OWNER = new User(4L, EMAIL, "owner", "hash");
    private static final byte[] PNG = {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 1};
    @Mock ProfilePhotoRepository photos;
    @Mock FindUserUseCase users;
    @InjectMocks ProfilePhotoService service;

    @Test
    void savesValidRasterAndSanitizedSvgAndReturnsStoredPhoto() {
        when(users.getByEmail(EMAIL)).thenReturn(OWNER);
        service.update(EMAIL, "avatar.png", "image/png", PNG);
        org.mockito.ArgumentCaptor<ProfilePhoto> firstPhoto = org.mockito.ArgumentCaptor.forClass(ProfilePhoto.class);
        verify(photos).save(org.mockito.Mockito.eq(4L), firstPhoto.capture());
        assertEquals("image/png", firstPhoto.getValue().contentType());
        assertArrayEquals(PNG, firstPhoto.getValue().content());

        service.update(EMAIL, "avatar.svg", "image/svg+xml",
                "<svg xmlns=\"http://www.w3.org/2000/svg\" onload=\"alert(1)\"><script>alert(1)</script><circle r=\"8\"/></svg>"
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8));
        org.mockito.ArgumentCaptor<ProfilePhoto> captor = org.mockito.ArgumentCaptor.forClass(ProfilePhoto.class);
        verify(photos, org.mockito.Mockito.times(2)).save(org.mockito.Mockito.eq(4L), captor.capture());
        String safeSvg = new String(captor.getAllValues().get(1).content(), java.nio.charset.StandardCharsets.UTF_8);
        org.junit.jupiter.api.Assertions.assertFalse(safeSvg.contains("script"));
        org.junit.jupiter.api.Assertions.assertFalse(safeSvg.contains("onload"));

        when(photos.findByUserId(4L)).thenReturn(Optional.of(new ProfilePhoto("image/png", PNG)));
        assertArrayEquals(PNG, service.get(EMAIL).orElseThrow().content());
    }

    @Test
    void rejectsUnsupportedFormatsMismatchedTypesAndInvalidImageSignatures() {
        when(users.getByEmail(EMAIL)).thenReturn(OWNER);
        assertThrows(InvalidProfilePhotoException.class,
                () -> service.update(EMAIL, "avatar.gif", "image/gif", PNG));
        assertThrows(InvalidProfilePhotoException.class,
                () -> service.update(EMAIL, "avatar.png", "image/jpeg", PNG));
        assertThrows(InvalidProfilePhotoException.class,
                () -> service.update(EMAIL, "avatar.png", "image/png", new byte[]{1, 2, 3}));
        assertThrows(InvalidProfilePhotoException.class,
                () -> service.update(EMAIL, "avatar.jpg", "image/jpeg", new byte[]{1, 2, 3}));
        assertThrows(InvalidProfilePhotoException.class,
                () -> service.update(EMAIL, "avatar.svg", "image/svg+xml",
                        "<!DOCTYPE svg [<!ENTITY x SYSTEM \"file:///etc/passwd\">]><svg/>"
                                .getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        verify(photos, org.mockito.Mockito.never()).save(org.mockito.ArgumentMatchers.eq(4L),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void deletesPhotoForTheAuthenticatedUser() {
        when(users.getByEmail(EMAIL)).thenReturn(OWNER);

        service.delete(EMAIL);

        verify(photos).delete(4L);
    }
}
