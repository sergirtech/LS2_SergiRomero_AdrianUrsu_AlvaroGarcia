package com.tecnocampus.LS2.protube_back;

import com.tecnocampus.LS2.protube_back.application.service.VideoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;


@SpringBootTest({
        "pro_tube.store.dir=c:",
        "pro_tube.load_initial_data=false"
})
class ProtubeBackApplicationTests {

    @Autowired
    VideoService videoService;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    void shouldStartAppWithoutGoogleCredentials() {
        Assertions.assertNotNull(videoService);
        Assertions.assertFalse(applicationContext.containsBean("googleClientRegistrationRepository"));
        Assertions.assertFalse(applicationContext.getBeansOfType(ClientRegistrationRepository.class).size() > 0);
    }

}
