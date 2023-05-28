package com.shoppiem.api.utils;

import com.shoppiem.api.UserProfile;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.nio.file.Files;

/**
 * @author Bizuwork Melesse
 * created on 2/17/22
 */
@Service
@RequiredArgsConstructor
public class ServiceTestHelper {

    public void prepareSecurity(String userId) {
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(new UserProfile()
            .name("TestNG User")
            .email("testuser@exampe.com")
            .uid(userId == null ? UUID.randomUUID().toString() : userId)
        );

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @SneakyThrows
    public static String loadFile(String fileName) {
        File file = ResourceUtils.getFile("classpath:testData/" + fileName);
        return new String(Files.readAllBytes(file.toPath()));
    }
}
