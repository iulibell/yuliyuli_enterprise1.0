package com.yuliyuli.service.impl;

import com.yuliyuli.config.RabbitMqConfig;
import com.yuliyuli.entity.UserHolder;
import com.yuliyuli.mapper.VideoMapper;
import com.yuliyuli.service.InfoService;
import com.yuliyuli.util.VideoConvertUtil;
import com.yuliyuli.vo.VideoVO;
import com.yuliyuli.wrapper.VideoWrapper;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class InfoServiceImpl implements InfoService {

  @Resource private RabbitTemplate rabbitTemplate;

  @Resource private VideoWrapper videoWrapper;

  @Resource private VideoMapper videoMapper;

  /**
   * 删除视频
   *
   * @param videoIUrl 视频ID
   * @param coverUrl 封面URL
   * @return 删除结果
   */
  @Override
  public String videoDelete(String videoIUrl, Long userId) {
    if (!checkIsLogin()) {
      log.error("用户未登录，无法删除视频");
      return "请完成登录";
    }
    try {
      Map<String, Object> map = new HashMap<>();
      map.put("videoUrl", videoIUrl);
      map.put("userId", userId);
      rabbitTemplate.convertAndSend(
          RabbitMqConfig.DELETE_EXCHANGE_NAME, RabbitMqConfig.DELETE_ROUTING_KEY, map);
      log.info("删除视频传至mq成功,视频ID:{}", videoIUrl);
      return "删除视频成功";
    } catch (Exception e) {
      log.error("删除视频传至mq失败,视频ID:{}", videoIUrl, e);
      return "删除视频失败,请稍后重试";
    }
  }

  /**
   * 获取作者页面信息,传视频的信息，包括制作的视频
   *
   * @param userId 作者ID
   * @return 作者页面所有视频
   */
  @Override
  public List<VideoVO> getAuthorPageVideo(Long userId) {
    try {
      return VideoConvertUtil.convertVideoListToVideoVOList(
          videoMapper.selectList(videoWrapper.getAuthorPageVideo(userId)));
    } catch (Exception e) {
      log.error("获取作者页面视频失败,作者ID:{}", userId, e);
      throw new IllegalArgumentException("获取作者页面视频失败,作者ID:" + userId);
    }
  }

  public String userFollow(Long followUserId, Long fanUserId){
    if (!checkIsLogin()) {
      log.error("用户未登录，无法关注用户");
      return "请完成登录";
    }
    try{
      Map<String, Object> map = new HashMap<>();
      map.put("followUserId", followUserId);
      map.put("fanUserId", fanUserId);
      rabbitTemplate.convertAndSend(
          RabbitMqConfig.FOLLOW_EXCHANGE_NAME, RabbitMqConfig.FOLLOW_ROUTING_KEY, map);
      log.info("关注用户传至mq成功,关注用户ID:{}", followUserId);
      return "关注成功"; 
    } catch (Exception e) {
      log.error("关注用户传至mq失败,关注用户ID:{}", followUserId, e);
      return "关注失败,请稍后重试";
    }
  }

  public boolean checkIsLogin() {
    if (UserHolder.getUser() == null) {
      return false;
    }
    return true;
  }
}
