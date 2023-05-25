package com.shoppiem.api.service.user;


import com.shoppiem.api.UserProfile;

/**
 * @author Biz Melesse created on 12/6/22
 */
public interface UserServiceHelper {

  void createDbUserAsync(UserProfile userProfile);
  void createDbUser(UserProfile userProfile);

}
