package com.shoppiem.api.service.user;


import com.shoppiem.api.data.postgres.entity.UserEntity;
import com.shoppiem.api.data.postgres.repo.UserRepo;
import com.shoppiem.api.service.ServiceTestConfiguration;
import com.shoppiem.api.utils.ServiceTestHelper;
import com.shoppiem.api.utils.migration.FlywayMigration;
import com.shoppiem.api.UserProfileResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.*;

import java.lang.reflect.Method;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Bizuwork Melesse
 * created on 2/13/22
 */
@Slf4j
@SpringBootTest(classes = ServiceTestConfiguration.class)
public class UserServiceIntegrationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private UserService userService;

    @Autowired
    private FlywayMigration flywayMigration;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ServiceTestHelper testHelper;

    @BeforeClass
    public void setup() {
        testHelper.prepareSecurity(null);
    }

    @AfterClass
    public void tearDown() {
    }

    @BeforeMethod
    public void beforeEachTest(Method method) {
        log.info("  Testcase: " + method.getName() + " has started");
        flywayMigration.migrate(true);
    }

    @AfterMethod
    public void afterEachTest(Method method) {
        log.info("  Testcase: " + method.getName() + " has ended");
    }

    @SneakyThrows
    @Test
    public void createUserTest() {
        UserProfileResponse response = userService.getOrCreateUserprofile();
        assertThat(response, is(notNullValue()));
        assertThat(response.getProfile().getName(), is(notNullValue()));
        assertThat(response.getProfile().getEmail(), is(notNullValue()));
        assertThat(response.getProfile().getFirebaseId(), is(notNullValue()));
        Thread.sleep(5000);
        List<UserEntity> allUsers = userRepo.findAll();
        assertThat(allUsers, is(notNullValue()));
        assertThat(allUsers.size(), is(equalTo(1)));
        UserEntity user = allUsers.get(0);
        assertThat(user.getCreatedAt(), is(notNullValue()));
        assertThat(user.getUpdatedAt(), is(notNullValue()));
        assertThat(user.getFullName(), startsWith(response.getProfile().getName()));
        assertThat(user.getEmail(), startsWith(response.getProfile().getEmail()));
        assertThat(user.getFirebaseId(), is(notNullValue()));
    }
}
