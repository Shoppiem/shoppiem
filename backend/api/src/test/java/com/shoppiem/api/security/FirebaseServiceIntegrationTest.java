package com.shoppiem.api.security;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.shoppiem.api.utils.firebase.FirebaseSDKConfig;
import com.shoppiem.api.utils.firebase.FirebaseService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Bizuwork Melesse
 * created on 8/22/21
 */
@Slf4j
@SpringBootTest(classes = SecurityTestConfiguration.class)
public class FirebaseServiceIntegrationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private FirebaseSDKConfig firebaseSDKConfig;

    @Autowired
    private FirebaseService firebaseService;

    private final String demoUserEmail = UUID.randomUUID() + "@shoppiem.com";
    private final String demoUserDisplayName = "Recipe Man";

    @BeforeClass
    public void setup() {
        try {
            FirebaseAuth.getInstance().deleteUser(FirebaseAuth.getInstance().getUserByEmail(demoUserEmail).getUid());
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    @AfterTest
    public void teardown() {
        UserRecord user = firebaseService.getUserByEmail(demoUserEmail);
        if (user != null) {
            firebaseService.deleteUser(user.getUid());
        }
    }

    @BeforeMethod
    public void beforeEachTest(Method method) {
        log.info("  Testcase: " + method.getName() + " has started");
    }

    @AfterMethod
    public void afterEachTest(Method method) {
        log.info("  Testcase: " + method.getName() + " has ended");
    }

    @Test(priority = 0)
    public void credentialsParserTest() {
        GoogleCredentials googleCredentials =  firebaseSDKConfig.getFirebaseCredentials();
        assertThat(googleCredentials, is(notNullValue()));
    }

    @Test(dependsOnMethods = "credentialsParserTest")
    public void createFirebaseUserTest() {
        String demoUserPassword = "rip-repo-main";
        String demoUserPhoneNumber = "+11234567890";
        String demoUserAvatar = "https://picsum.photos/200";
        final UserRecord userRecord = firebaseService.createUser(
                demoUserEmail,
                demoUserPassword,
                demoUserPhoneNumber,
                demoUserDisplayName,
                demoUserAvatar);
        assertThat(userRecord.getDisplayName(), is(equalTo(demoUserDisplayName)));
        assertThat(userRecord.getEmail(), is(equalTo(demoUserEmail)));
        assertThat(userRecord.getPhoneNumber(), is(equalTo(demoUserPhoneNumber)));
    }

    @SneakyThrows
    @Test(dependsOnMethods = "createFirebaseUserTest")
    public void createCustomClaimsTest() {
        List<String> claims = Arrays.asList("ROLE_ADMIN", "ROLE_TEST", "ROLE_PREMIUM_USER");
        final UserRecord userRecord = firebaseService.getUserByEmail(demoUserEmail);
        assertThat(userRecord, is(notNullValue()));
        firebaseService.createCustomClaims(userRecord.getUid(), claims);
        final UserRecord userRecordWithClaims = firebaseService.getUserByEmail(demoUserEmail);
        assertThat(userRecordWithClaims, is(notNullValue()));
        assertThat(userRecordWithClaims.getUid(), is(equalTo(userRecord.getUid())));
        assertThat(userRecordWithClaims.getCustomClaims().size(), is(equalTo(3)));
        for (String claim : claims) {
            assertThat(userRecordWithClaims.getCustomClaims().containsKey(claim), is(equalTo(true)));
        }
    }

    @SneakyThrows
    @Test(dependsOnMethods = "createCustomClaimsTest")
    public void updateCustomClaimsTest() {
        final UserRecord userRecord = firebaseService.getUserByEmail(demoUserEmail);
        assertThat(userRecord, is(notNullValue()));
        assertThat(userRecord.getDisplayName(), is(equalTo(demoUserDisplayName)));
        final List<String> claims = userRecord.getCustomClaims().keySet()
                .stream()
                .map(String::toString)
                .collect(Collectors.toList());
        claims.add("ROLE_AFFLUENT_USER");
        claims.add("ROLE_VIP_USER");
        firebaseService.updateCustomClaims(userRecord.getUid(), claims);
        final UserRecord userWithUpdatedClaims = firebaseService.getUserByEmail(demoUserEmail);
        assertThat(userWithUpdatedClaims, is(notNullValue()));
        assertThat(userWithUpdatedClaims.getUid(), is(equalTo(userRecord.getUid())));
        assertThat(userWithUpdatedClaims.getCustomClaims().size(), is(equalTo(5)));
        for (String claim : claims) {
            assertThat(userWithUpdatedClaims.getCustomClaims().containsKey(claim), is(equalTo(true)));
        }
    }

    @Test(dependsOnMethods = "updateCustomClaimsTest")
    public void deleteUserTest() {
        firebaseService.deleteUser(firebaseService.getUserByEmail(demoUserEmail).getUid());
        UserRecord userRecord = firebaseService.getUserByEmail(demoUserEmail);
        assertThat(userRecord, is(nullValue()));
    }
}
