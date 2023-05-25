package com.shoppiem.api.utils.firebase;



import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.shoppiem.api.props.FirebaseProps;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Bizuwork Melesse
 * created on 2/13/21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseServiceImpl implements FirebaseService {

    private final TypeReference<HashMap<String, String>> typeRef = new TypeReference<>() {
    };
    private final ObjectMapper objectMapper;
    private final FirebaseProps firebaseProps;

    @SneakyThrows
    @Override
    public UserRecord createUser(String email, String password, String phoneNumber, String displayName, String photoUrl) {
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(email)
                .setEmailVerified(false)
                .setPassword(password)
                .setPhoneNumber(phoneNumber)
                .setDisplayName(displayName)
                .setPhotoUrl(photoUrl)
                .setDisabled(false);
        return FirebaseAuth.getInstance().createUser(request);
    }

    @Override
    public UserRecord getUserByEmail(String email) {
        try {
            return FirebaseAuth.getInstance().getUserByEmail(email);
        } catch (FirebaseAuthException e) {
            return null;
        }
    }

    @SneakyThrows
    @Override
    public String getIdToken(String uid) {
        String customToken = FirebaseAuth.getInstance().createCustomToken(uid);
        ObjectNode jsonNode = objectMapper.createObjectNode();
        jsonNode.put("token", customToken);
        jsonNode.put("returnSecureToken", true);
        final CloseableHttpClient httpClient = HttpClients.createDefault();
        final StringEntity params = new StringEntity(jsonNode.toString());
        HttpPost postRequest = new HttpPost(String.format("%s?key=%s", firebaseProps.getCustomTokenVerificationUrl(),
                firebaseProps.getClientApiKey()));
        postRequest.addHeader("content-type", "application/json");
        postRequest.setEntity(params);
        Map<String, String> response = objectMapper.readValue(httpClient.execute(postRequest).getEntity().getContent(),
                typeRef);
        httpClient.close();
        return response.get("idToken");
    }

    @SneakyThrows
    @Override
    public void deleteUser(String uid) {
        FirebaseAuth.getInstance().deleteUser(uid);
    }

    @SneakyThrows
    @Override
    public void createCustomClaims(String uid, List<String> claims) {
        Map<String, Object> customClaims = new HashMap<>();
        try {
            for (String claim : claims) {
                customClaims.put(claim, true);
            }
            FirebaseAuth.getInstance().setCustomUserClaims(uid, customClaims);
        } catch (FirebaseAuthException | NullPointerException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    @Override
    public void updateCustomClaims(String uid, List<String> claims) {
        createCustomClaims(uid, claims);
    }

    @SneakyThrows
    @Override
    public String login(String email, String password) {
        ObjectNode jsonNode = objectMapper.createObjectNode();
        jsonNode.put("email", email);
        jsonNode.put("password", password);
        jsonNode.put("returnSecureToken", true);
        final CloseableHttpClient httpClient = HttpClients.createDefault();
        final StringEntity params = new StringEntity(jsonNode.toString());
        HttpPost postRequest = new HttpPost(String.format("%s?key=%s", firebaseProps.getPasswordVerificationUrl(),
                firebaseProps.getClientApiKey()));
        postRequest.addHeader("content-type", "application/json");
        postRequest.setEntity(params);
        String token = "";
        try {
            Map<String, String> response = objectMapper.readValue(httpClient.execute(postRequest).getEntity().getContent(),
                    typeRef);
            token = response.get("idToken");
        } catch (Exception e) {
            log.error("Login failure: {}", e.getLocalizedMessage());
            httpClient.close();
        }
        return token;
    }

    @Override
    public UserRecord getUserByPhoneNumber(String phone) {
        try {
            return FirebaseAuth.getInstance().getUserByPhoneNumber(phone);
        } catch (FirebaseAuthException e) {
            return null;
        }
    }
}
