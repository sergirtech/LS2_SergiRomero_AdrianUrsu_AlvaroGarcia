package com.tecnocampus.LS2.protube_back;

import com.tecnocampus.LS2.protube_back.application.port.in.ImportVideosUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** On startup, imports into the catalog the store videos that are not in the database yet (idempotent). */
@Component
public class AppStartupRunner implements ApplicationRunner {
    private static final Logger LOG = LoggerFactory.getLogger(AppStartupRunner.class);

    private final ImportVideosUseCase importVideosUseCase;
    private final boolean loadInitialData;

    public AppStartupRunner(ImportVideosUseCase importVideosUseCase,
                            @Value("${pro_tube.load_initial_data:false}") boolean loadInitialData) {
        this.importVideosUseCase = importVideosUseCase;
        this.loadInitialData = loadInitialData;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!loadInitialData) {
            LOG.info("pro_tube.load_initial_data=false: video import skipped");
            return;
        }
        LOG.info("Imported {} new videos from the store", importVideosUseCase.importFromStore());
    }
}
