package com.tecnocampus.LS2.protube_back.application.port.out;

import com.tecnocampus.LS2.protube_back.domain.model.Video;

import java.util.List;

/** Output port: metadata of the videos already in the store (the videoGrabber .json files), without ids. */
public interface VideoMetadataSource {
    List<Video> readAll();
}
