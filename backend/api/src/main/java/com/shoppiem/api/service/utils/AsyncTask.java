package com.shoppiem.api.service.utils;

import com.shoppiem.api.RootConfiguration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Biz Melesse created on 12/24/22
 */
@Slf4j
public class AsyncTask {

  public static void submit(Runnable task, Runnable callback) {
    CompletableFuture
        .runAsync(task, RootConfiguration.executor)
        .exceptionally(e -> { log.error(e.getLocalizedMessage()); return null;})
        .thenRun(() -> {
          if (callback != null) {
            callback.run();
          }
        });
  }


}
