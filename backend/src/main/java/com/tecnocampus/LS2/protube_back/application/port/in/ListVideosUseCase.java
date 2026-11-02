package com.tecnocampus.LS2.protube_back.application.port.in;

import java.util.List;

/** Input port: video catalog (US-04). Still returns a placeholder list. */
public interface ListVideosUseCase {

    List<String> getVideos();
}
