package com.shoppiem.api.service.utils;

import java.io.File;
import java.util.Random;
import lombok.SneakyThrows;

/**
 * @author Biz Melesse created on 1/2/23
 */
public class ShoppiemUtils {

  @SneakyThrows
  public static String getCanonicalPath(String path) {
    String cPath = new File(path).getCanonicalPath();
    if (cPath.startsWith("/private")) {
      return cPath.replace("/private", "");
    }
    return cPath;
  }

  public static String generateUid() {
    StringBuilder builder = new StringBuilder();
    // An ID length of N gives 62^N unique IDs
    int contentIdLength = 8;
    for (int i = 0; i < contentIdLength; i++) {
      builder.append(getRandomCharacter());
    }
    return builder.toString();
  }

  public static Character getRandomCharacter() {
    Random random = new Random();
    String uidAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnoqprstuvwxyz0123456789";
    int index = random.nextInt(uidAlphabet.length());
    return uidAlphabet.charAt(index);
  }
}
