package com.yuliyuli.vo;

import com.yuliyuli.document.VideoDocument;
import java.util.List;
import lombok.Data;

@Data
public class SearchVideoVO {
  // 搜索到的视频列表
  private List<VideoDocument> videoDocuments;
}
