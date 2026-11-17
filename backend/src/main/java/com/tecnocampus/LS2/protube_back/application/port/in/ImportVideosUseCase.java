package com.tecnocampus.LS2.protube_back.application.port.in;

/** Input port: imports into the catalog the store videos that are not stored yet. Returns how many it added. */
public interface ImportVideosUseCase {
    int importFromStore();
}
