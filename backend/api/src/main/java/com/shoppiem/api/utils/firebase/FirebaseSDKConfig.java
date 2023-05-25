package com.shoppiem.api.utils.firebase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.shoppiem.api.props.FirebaseProps;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author Bizuwork Melesse
 * created on 2/13/21
 */
@Slf4j
@Configuration
@ConditionalOnProperty(value = "firebase.sdkEnabled")
@RequiredArgsConstructor
public class FirebaseSDKConfig {
    private final FirebaseProps firebaseProps;
    private final ObjectMapper objectMapper;

    @Bean
    @Primary
    @SneakyThrows
    public void firebaseInit() {
        final FirebaseOptions firebaseOptions = FirebaseOptions.builder()
                .setCredentials(getFirebaseCredentials()).build();
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(firebaseOptions);
        }
    }

    @SneakyThrows
    public GoogleCredentials getFirebaseCredentials() {
        final ObjectNode jsonNode = objectMapper.createObjectNode();
        jsonNode.put("type", firebaseProps.getType());
        jsonNode.put("project_id", firebaseProps.getProjectId());
        jsonNode.put("private_key_id", firebaseProps.getPrivateKeyId());
        jsonNode.put("private_key",  decodeKey(firebaseProps.getPrivateKey()));
        jsonNode.put("client_email", firebaseProps.getClientEmail());
        jsonNode.put("client_id", firebaseProps.getClientId());
        jsonNode.put("auth_uri", firebaseProps.getAuthUri());
        jsonNode.put("token_uri", firebaseProps.getTokenUri());
        jsonNode.put("auth_provider_x509_cert_url", firebaseProps.getAuthProviderX509CertUrl());
        jsonNode.put("client_x509_cert_url", firebaseProps.getClientX509CertUrl());
        InputStream inputStream = new ByteArrayInputStream(objectMapper.writeValueAsString(jsonNode).getBytes(StandardCharsets.UTF_8));
        return GoogleCredentials.fromStream(inputStream);
    }

    private String decodeKey(String encoded) {
        try {
           return new String(Base64.getDecoder().decode(encoded), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
