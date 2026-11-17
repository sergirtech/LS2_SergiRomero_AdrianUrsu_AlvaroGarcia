package com.tecnocampus.LS2.protube_back;

import com.tecnocampus.LS2.protube_back.application.port.in.ImportVideosUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AppStartupRunnerTest {

    @Mock
    ImportVideosUseCase importVideosUseCase;

    @Test
    void importsVideosWhenEnabled() {
        new AppStartupRunner(importVideosUseCase, true).run(null);
        verify(importVideosUseCase).importFromStore();
    }

    @Test
    void skipsImportWhenDisabled() {
        new AppStartupRunner(importVideosUseCase, false).run(null);
        verifyNoInteractions(importVideosUseCase);
    }
}
