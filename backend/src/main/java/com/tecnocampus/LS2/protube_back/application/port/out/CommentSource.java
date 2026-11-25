package com.tecnocampus.LS2.protube_back.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Output port: comments that come with the store videos (the videoGrabber .json files), by video file key. */
public interface CommentSource {
    Map<String, List<StoredComment>> readAll();

    record StoredComment(String author, String text, Instant createdAt) {
    }
}
