package com.shoppiem.api.controller;

import com.shoppiem.api.UserApi;
import com.shoppiem.api.service.user.UserService;
import com.shoppiem.api.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Biz Melesse
 * created on 10/17/22
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {
  private final UserService userService;
  @Override
  public ResponseEntity<UserProfileResponse> getUserprofile() {
    return ResponseEntity.ok(userService.getOrCreateUserprofileAsync());
  }
}
