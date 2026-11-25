package com.tecnocampus.LS2.protube_back.application.port.in;

/** Input port: imports the store comments of the catalog videos that have none imported yet. Returns how many. */
public interface ImportCommentsUseCase {
    int importCommentsFromStore();
}
