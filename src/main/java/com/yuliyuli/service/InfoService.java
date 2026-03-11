package com.yuliyuli.service;

import com.yuliyuli.vo.VideoVO;
import java.util.List;

public interface InfoService {
  /**
   * 获取作者页面信息,传视频的信息，包括制作的视频
   *
   * @param userId 作者ID
   * @return 作者页面所有视频
   */
  public List<VideoVO> getAuthorPageVideo(Long userId);

  /**
   * 删除视频
   *
   * @param videoIUrl 视频ID
   * @param coverUrl 封面URL
   * @return 删除结果
   */
  public void videoDelete(String videoIUrl, Long userId);
}
