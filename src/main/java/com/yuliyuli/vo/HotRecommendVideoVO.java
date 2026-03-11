package com.yuliyuli.vo;

import com.yuliyuli.document.VideoDocument;
import java.util.List;
import lombok.Data;

@Data
public class HotRecommendVideoVO {
  // 热门推荐视频列表
  private List<VideoDocument> videoDocuments;
}
