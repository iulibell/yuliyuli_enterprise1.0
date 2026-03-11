package com.yuliyuli.service;

import com.yuliyuli.vo.SearchVideoVO;
import java.util.List;

public interface SearchService {
  /**
   * 在推荐栏的热度前十视频
   *
   * @return top10热度视频标题
   */
  public List<com.yuliyuli.vo.SearchVideoVO> getTopTenVideo();

  /**
   * 普通搜索视频功能
   *
   * @param title 视频标题
   * @return 视频列表
   */
  public List<SearchVideoVO> findByTitleSuggest(String title);
}
