package com.tecnocampus.LS2.protube_back;

import com.tecnocampus.LS2.protube_back.application.port.in.ImportCommentsUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.ImportVideosUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AppStartupRunnerTest {

    @Mock
    ImportVideosUseCase importVideosUseCase;

    @Mock
    ImportCommentsUseCase importCommentsUseCase;

    @Test
    void importsVideosAndThenCommentsWhenEnabled() {
        new AppStartupRunner(importVideosUseCase, importCommentsUseCase, true).run(null);
        InOrder order = inOrder(importVideosUseCase, importCommentsUseCase);
        order.verify(importVideosUseCase).importFromStore();
        order.verify(importCommentsUseCase).importCommentsFromStore();
    }

    @Test
    void skipsImportWhenDisabled() {
        new AppStartupRunner(importVideosUseCase, importCommentsUseCase, false).run(null);
        verifyNoInteractions(importVideosUseCase, importCommentsUseCase);
    }
}
