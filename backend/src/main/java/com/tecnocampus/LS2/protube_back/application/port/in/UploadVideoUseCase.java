package com.tecnocampus.LS2.protube_back.application.port.in;

import com.tecnocampus.LS2.protube_back.domain.model.VideoSummary;

public interface UploadVideoUseCase {
    VideoSummary upload(String ownerEmail, String title, String description, String fileName, byte[] content);
}
