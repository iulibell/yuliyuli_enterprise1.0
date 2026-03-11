package com.yuliyuli.timer;

import com.yuliyuli.init.SearchVideoInit;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HotVideoRefreshTask {

  @Resource private SearchVideoInit searchVideoInit;

  /** 每2小时刷新一次前10缓存 */
  @Scheduled(cron = "0 0 */2 * * *")
  @Async
  public void refreshHotVideoCache() {
    log.info("定时任务:刷新前10热门视频缓存");
    searchVideoInit.refreshTopTenCache();
  }
}
