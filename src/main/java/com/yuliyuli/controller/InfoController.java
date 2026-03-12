package com.yuliyuli.controller;

import com.yuliyuli.common.Result;
import com.yuliyuli.service.InfoService;
import com.yuliyuli.vo.VideoVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 信息返回控制器
 * 提供获取作者、视频公开信息的接口
 */
@RestController
@RequestMapping("/info")
@Tag(name = "信息接口")
@Slf4j
public class InfoController {

  @Resource private InfoService infoService;

  /**
   * 获取作者页面信息,传视制作的视频
   * @param userId 作者ID
   * @return 作者页面信息
   */
  @GetMapping("/authorPage")
  public Result<List<VideoVO>> getAuthorPageInfo(@RequestParam Long userId) {
    if(userId == null){
      return Result.fail("该作者不存在!");
    }
    try {
      return Result.success(infoService.getAuthorPageVideo(userId));
    } catch (Exception e) {
      log.error("获取作者页面视频失败", e);
      return Result.fail("获取作者页面视频失败,请稍后重试!");
    }
  }
}
