package com.tecnocampus.LS2.protube_back.domain.model;

public record ProfilePhoto(String contentType, byte[] content) {
    public ProfilePhoto {
        content = content.clone();
    }

    @Override
    public byte[] content() {
        return content.clone();
    }
}
