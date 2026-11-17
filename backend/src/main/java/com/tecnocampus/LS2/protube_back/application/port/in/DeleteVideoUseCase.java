package com.tecnocampus.LS2.protube_back.application.port.in;

public interface DeleteVideoUseCase {
    void delete(String ownerEmail, Long videoId);
}
