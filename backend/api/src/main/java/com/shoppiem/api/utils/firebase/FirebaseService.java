package com.shoppiem.api.utils.firebase;

import com.google.firebase.auth.UserRecord;

import java.util.List;

/**
 * @author Bizuwork Melesse
 * created on 2/13/21
 */
public interface FirebaseService {

    UserRecord createUser(String email, String password, String phoneNumber, String displayName, String photoUrl);

    UserRecord getUserByEmail(String email);

    String getIdToken(String uid);

    void deleteUser(String uid);

    void createCustomClaims(String uid, List<String> claims);

    void updateCustomClaims(String uid, List<String> claims);

    String login(String email, String password);

    UserRecord getUserByPhoneNumber(String phone);

}
